package openerp.openerpresourceserver.wms.dto.facility;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.*;

import java.math.BigDecimal;

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

    private boolean isDefault;

    private String phone;

    private String postalCode;

    private String statusId;

    private String address;

    private Double latitude;

    private Double longitude;
}
