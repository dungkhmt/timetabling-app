package openerp.openerpresourceserver.wms.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wms2_order_item")
public class OrderItem extends BaseEntity {
    @EmbeddedId
    private OrderItemPK id;

    @MapsId("orderId")
    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderHeader order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "amount", precision = 25, scale = 5)
    private BigDecimal amount;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @Column(name = "unit", length = 100)
    private String unit;
    @Column(name = "price", precision = 25, scale = 5)
    private BigDecimal price;
    @Column(name = "discount", precision = 25, scale = 5)
    private BigDecimal discount;
    @Column(name = "tax", precision = 25, scale = 5)
    private BigDecimal tax;

}
