package openerp.openerpresourceserver.wms.dto.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank
    private String name;

    private String email;

    private String phone;

    @NotNull
    private AddressReq address;
}
