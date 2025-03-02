package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

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
}
