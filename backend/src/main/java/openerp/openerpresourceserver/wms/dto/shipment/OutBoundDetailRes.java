package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.*;

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
    private List<OutBoundDetailProductRes> products;
}
