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
public class Address {
    @Id
    @Column(length = 40)
    private String id;

    @Column(name = "entity_id", length = 40, nullable = false)
    private String entityId;

    @Column(name = "entity_type", length = 20, nullable = false)
    private String entityType;

    @Column(name = "address_type", length = 20)
    private String addressType;

    @Column(precision = 9, scale = 6)
    private Double latitude;

    @Column(precision = 9, scale = 6)
    private Double longitude;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "full_address", length = 255)
    private String fullAddress;
}

