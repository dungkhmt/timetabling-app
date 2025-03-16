package openerp.openerpresourceserver.wms.entity;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wms2_order_item")
public class OrderItem extends BaseEntity {
    @Id
    private String id;

    @Column(name = "order_item_seq_id", length = 10)
    private String orderItemSeqId;

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
