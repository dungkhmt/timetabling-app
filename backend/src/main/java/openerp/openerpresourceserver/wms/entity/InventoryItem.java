package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms2_inventory_item")
public class InventoryItem {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    private LocalDateTime createdStamp;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    private String lotId;
}