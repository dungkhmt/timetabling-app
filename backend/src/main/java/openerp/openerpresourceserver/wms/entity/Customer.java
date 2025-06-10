package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_customer")
public class Customer {
    @Id
    private String id;

    private String name;

    private String email;

    private String currentAddressId;

    @Transient
    private String fullAddress;

    private String statusId;

    private String phone;
}