package openerp.openerpresourceserver.wms.dto.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.address.AddressReq;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateFacilityReq {
    private String id;

    @NotBlank
    private String name;

    private Boolean isDefault = false;

    private String phone;

    private String postalCode;

    @NotNull
    private AddressReq address;
}
