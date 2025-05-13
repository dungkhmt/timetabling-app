package openerp.openerpresourceserver.wms.dto.supplier;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.address.AddressReq;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplierReq {
    private String id;

    private String name;

    private String email;

    private String phone;

    private AddressReq address;
}
