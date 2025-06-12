package openerp.openerpresourceserver.wms.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryBillGetListFilter {
    private String statusId;
    private String deliveryStatusId;
    private String facilityId;
    private String keyword;
    private LocalDateTime startCreatedAt;
    private LocalDateTime endCreatedAt;
}
