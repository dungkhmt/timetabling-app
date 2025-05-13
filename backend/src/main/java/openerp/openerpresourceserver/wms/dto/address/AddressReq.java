package openerp.openerpresourceserver.wms.dto.address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressReq {
    private String addressType;

    private Double latitude;

    private Double longitude;

    private Boolean isDefault = false;

    private String fullAddress;
}
