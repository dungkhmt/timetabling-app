package openerp.openerpresourceserver.wms.dto.shipment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class CreateOutBounndReq {
    private String id;
    private String note;
    private String orderId;
    private List<CreateOutBoundProductReq> products;
}
