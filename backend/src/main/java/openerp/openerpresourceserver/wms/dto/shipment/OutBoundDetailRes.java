package openerp.openerpresourceserver.wms.dto.shipment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class OutBoundDetailRes {
    private String id;
    private String shipmentType;
    private String shipmentName;
    private String customerName;
    private String statusId;
    private LocalDateTime createdStamp;
    private LocalDate expectedDeliveryDate;
    private List<OutBoundDetailProductRes> products;
}
