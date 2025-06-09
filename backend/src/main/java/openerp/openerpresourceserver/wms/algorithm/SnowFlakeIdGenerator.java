package openerp.openerpresourceserver.wms.algorithm;

import java.security.SecureRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SnowFlakeIdGenerator {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATA_CENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = (1L << WORKER_ID_BITS) - 1; // 31
    private static final long MAX_DATA_CENTER_ID = (1L << DATA_CENTER_ID_BITS) - 1; // 31
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS;
    private static final long SEQUENCE_MASK = (1L << SEQUENCE_BITS) - 1; // 4095
    private static final SnowFlakeIdGenerator INSTANCE = new SnowFlakeIdGenerator();
    
    private final long workerId;
    private final long dataCenterId;
    private final long idEpoch;
    private final Lock lock;
    private long sequence;
    private long lastTimestamp;

    private SnowFlakeIdGenerator() {
        this(SECURE_RANDOM.nextInt((int) MAX_WORKER_ID + 1), 
             SECURE_RANDOM.nextInt((int) MAX_DATA_CENTER_ID + 1),
             1288834974657L); // Default Twitter epoch, 2010-11-04T01:42:54.657Z
    }
    
    private SnowFlakeIdGenerator(long workerId, long dataCenterId, long idEpoch) {
        this.lock = new ReentrantLock();
        this.sequence = 0L;
        this.lastTimestamp = -1L;
        
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                String.format("Worker ID can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException(
                String.format("Datacenter ID can't be greater than %d or less than 0", MAX_DATA_CENTER_ID));
        }
        
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
        this.idEpoch = idEpoch;
    }

    public static SnowFlakeIdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Generates a unique ID
     * @return a unique long ID
     */
    public synchronized long nextId() {
        lock.lock();
        try {
            long timestamp = timeGen();
            
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", 
                    lastTimestamp - timestamp));
            }
            
            if (lastTimestamp == timestamp) {
                sequence = (sequence + 1) & SEQUENCE_MASK;
                if (sequence == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0L;
            }
            
            lastTimestamp = timestamp;
            
            return ((timestamp - idEpoch) << TIMESTAMP_LEFT_SHIFT) |
                   (dataCenterId << DATA_CENTER_ID_SHIFT) |
                   (workerId << WORKER_ID_SHIFT) |
                   sequence;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Generates an ID with a prefix
     * @param prefix The prefix for the ID (e.g., "ORD", "PRD")
     * @return A string with the prefix followed by the generated ID
     */
    public String nextId(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Prefix cannot be null or empty");
        }
        return prefix + nextId();
    }
    
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
    
    private long timeGen() {
        return System.currentTimeMillis();
    }
}