package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "wms2_inventory_item_detail")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemDetail extends BaseEntity {
    @Id
    private String id;

//    @ManyToOne
//    @JoinColumn(name = "inventory_item_id")
//    private InventoryItem inventoryItem;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private String unit;

    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    private String note;

    private String lotId;

    private LocalDate expirationDate;

    private LocalDate manufacturingDate;
}