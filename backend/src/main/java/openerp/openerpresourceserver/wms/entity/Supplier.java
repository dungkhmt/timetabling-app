package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_supplier")
public class Supplier {
    @Id
    private String id;

    private String name;

    private String currentAddressId;

    @Transient
    private String fullAddress;

    private String email;

    private String phone;

    private String statusId;
}
