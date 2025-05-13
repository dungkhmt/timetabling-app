package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_customer")
public class Customer {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "status_id", length = 40)
    private String statusId;

    @Column(name = "phone", length = 40)
    private String phone;
}