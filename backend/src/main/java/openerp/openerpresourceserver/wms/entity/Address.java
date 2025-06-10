package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wms2_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {
    @Id
    private String id;

    private String entityId;

    private String entityType;

    private String addressType;

    private Double latitude;

    private Double longitude;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    private String fullAddress;
}

