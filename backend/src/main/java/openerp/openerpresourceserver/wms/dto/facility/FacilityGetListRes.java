package openerp.openerpresourceserver.wms.dto.facility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @version 1.0
 * @description: FacilityGetListRes
 * @author: ha.levan
 * @date: 6/3/25 3:01 PM
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class FacilityGetListRes {
    private String id;

    private String name;

    private Boolean isDefault;

    private String phone;

    private String postalCode;

    private String statusId;

    private String address;

    private Double latitude;

    private Double longitude;
}
