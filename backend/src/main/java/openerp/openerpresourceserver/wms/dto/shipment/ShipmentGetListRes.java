package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ShipmentGetListRes {
    private String id;

    private String shipmentTypeId;

    private String shipmentName;

    private String statusId;

    private String partnerId;

    private String partnerType;

    private String partnerName;

    private LocalDate expectedDeliveryDate;

    private String createdByUserName;

    private String handledByUserName;

    private String orderId;

    private LocalDateTime createdStamp;
}
