package openerp.openerpresourceserver.wms.dto.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPointDTO {
    private String id;
    private String customerName;
    private String fullAddress;
    private double latitude;
    private double longitude;
    private double demand;
    private int sequenceNumber;
}