package openerp.openerpresourceserver.wms.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_inventory_item_detail")
@Builder
public class InventoryItemDetail {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", precision = 18, scale = 2)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;
}