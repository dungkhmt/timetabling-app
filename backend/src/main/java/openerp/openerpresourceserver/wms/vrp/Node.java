package openerp.openerpresourceserver.wms.vrp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Node {
    private int id;
    private String name;
    private String deliveryAddressId;
    private String deliveryAddressFullAddress;
    private double latitude;
    private double longitude;
    private double demand;
    
    // Service time in minutes (loading/unloading time)
    private double serviceTime;
    
    public Node(int id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.demand = 0;
        this.serviceTime = 0;
    }
    
    public Node(int id, String name, double latitude, double longitude, double demand) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.demand = demand;
        this.serviceTime = 0;
    }
}
