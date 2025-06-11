package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "wms2_inventory_item")
public class InventoryItem extends BaseEntity {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    private String lotId;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;

    private String statusId;

    private LocalDate receivedDate;
}