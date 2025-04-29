package openerp.openerpresourceserver.wms.vrp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class Vehicle {
    private int id;              // Internal ID for VRP algorithm
    private String driverId;     // ID of the shipper/driver
    private String driverName;   // Name of the shipper/driver
    private double capacity;     // Maximum weight capacity
    private double currentLoad;  // Current load (changes during route planning)
    
    /**
     * Basic constructor with essential fields
     */
    public Vehicle(int id, String driverId, String driverName, double capacity, double currentLoad) {
        this.id = id;
        this.driverId = driverId;
        this.driverName = driverName;
        this.capacity = capacity;
        this.currentLoad = currentLoad;
    }
    
    /**
     * Create a Vehicle instance from an entity
     */
    public static Vehicle fromEntity(openerp.openerpresourceserver.wms.entity.Vehicle entity, int id, String driverName) {
        return Vehicle.builder()
            .id(id)
            .driverId(entity.getId())
            .driverName(driverName)
            .capacity(entity.getCapacity() != null ? entity.getCapacity().doubleValue() : 1000.0)
            .currentLoad(0.0)
            .build();
    }
    
    /**
     * Check if vehicle has capacity for additional load
     */
    public boolean hasCapacityFor(double additionalLoad) {
        return currentLoad + additionalLoad <= capacity;
    }
    
    /**
     * Add load to vehicle
     */
    public void addLoad(double additionalLoad) {
        this.currentLoad += additionalLoad;
    }
    
    /**
     * Reset current load to zero
     */
    public void resetLoad() {
        this.currentLoad = 0.0;
    }
}
