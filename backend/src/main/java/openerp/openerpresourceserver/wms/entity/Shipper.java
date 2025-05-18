package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms2_shipper")
public class Shipper {
    @Id
    @Column(name = "user_login_id", length = 60)
    private String userLoginId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_login_id")
    private UserLogin userLogin;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @Column(name = "last_latitude")
    private BigDecimal lastLatitude;

    @Column(name = "last_longitude")
    private BigDecimal lastLongitude;

    @Column(name = "phone")
    private String phone;

    public String getFullName() {
        return userLogin.getFullName();
    }
}
