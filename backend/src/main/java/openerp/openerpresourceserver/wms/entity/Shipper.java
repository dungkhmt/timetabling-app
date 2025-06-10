package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms2_shipper")
public class Shipper {
    @Id
    @Column(name = "user_login_id")
    private String userLoginId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_login_id")
    private UserLogin userLogin;

    private String statusId;

    private BigDecimal lastLatitude;

    private BigDecimal lastLongitude;

    private String phone;

    public String getFullName() {
        return userLogin.getFullName();
    }
}
