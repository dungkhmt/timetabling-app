package openerp.openerpresourceserver.wms.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.wms.dto.address.AddressReq;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerReq {
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Invalid email format")
    private String email;

    @NotBlank
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Invalid phone number format")
    private String phone;

    @NotNull
    private AddressReq address;
}
