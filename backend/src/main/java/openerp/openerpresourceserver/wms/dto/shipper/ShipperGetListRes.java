package openerp.openerpresourceserver.wms.dto.shipper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ShipperGetListRes {
    private String userLoginId;

    private String email;

    private String fullName;

    private String statusId;

    private String phone;
}
