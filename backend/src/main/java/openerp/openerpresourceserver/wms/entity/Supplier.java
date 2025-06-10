package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_supplier")
public class Supplier {
    @Id
    private String id;

    private String name;

    private String address;

    private String email;

    private String phone;

    private String statusId;
}
