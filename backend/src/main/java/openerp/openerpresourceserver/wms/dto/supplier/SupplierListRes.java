package openerp.openerpresourceserver.wms.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupplierListRes {
    private String id;
    private String name;
    private String address;
    private String phone;
}
