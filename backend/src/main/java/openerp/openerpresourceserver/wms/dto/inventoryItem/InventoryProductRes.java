package openerp.openerpresourceserver.wms.dto.inventoryItem;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class InventoryProductRes {
    private String id;

    private String productId;

    private String productName;

    private Integer quantity;

    private String facilityId;

    private String facilityName;

    private String lotId;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private String statusId;

    private LocalDate receivedDate;
}
