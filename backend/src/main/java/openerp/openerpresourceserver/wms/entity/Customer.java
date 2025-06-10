package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_customer")
public class Customer {
    @Id
    private String id;

    private String name;

    private String email;

    private String address;

    private String statusId;

    private String phone;
}