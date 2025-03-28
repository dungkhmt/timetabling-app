package openerp.openerpresourceserver.generaltimetabling.algorithms;

import java.util.*;

public class ClassSegmentPartitionConfigForSummerSemester {
    public Map<Integer, List<List<Integer>>> partitions;
    public ClassSegmentPartitionConfigForSummerSemester() {
        partitions = new HashMap<>();
        for (int i = 1; i <= 20; i++) {
            partitions.put(i, new ArrayList<>());
            if (i <= 5) partitions.get(i).add(Arrays.asList(new Integer[]{i}));
        }
        partitions.get(4).add(Arrays.asList(new Integer[]{2,2}));
        partitions.get(5).add(Arrays.asList(new Integer[]{2,3}));
        partitions.get(6).add(Arrays.asList(new Integer[]{3,3}));
        partitions.get(6).add(Arrays.asList(new Integer[]{2,4}));
        partitions.get(9).add(Arrays.asList(new Integer[]{4,5}));
        partitions.get(9).add(Arrays.asList(new Integer[]{3,3,3}));
        partitions.get(9).add(Arrays.asList(new Integer[]{2,3,4}));
        partitions.get(12).add(Arrays.asList(new Integer[]{4,4,4}));
        partitions.get(15).add(Arrays.asList(new Integer[]{5,5,5}));
        partitions.get(15).add(Arrays.asList(new Integer[]{4,4,4,3}));

    }
}
