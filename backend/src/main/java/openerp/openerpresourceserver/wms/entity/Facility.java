package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_facility")
public class Facility extends BaseEntity {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "is_default", nullable = false, length = 1)
    private boolean isDefault;

    @Column(name = "phone", length = 200)
    private String phone;

    @Column(name = "postal_code", length = 200)
    private String postalCode;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @Column(name = "address", length = 200)
    private String address;

}
