package openerp.openerpresourceserver.wms.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentGetListFilter {
    private String keyword;
    private String shipmentTypeId;
    private List<String> statusId;
    private LocalDate expectedDeliveryDate;
}
