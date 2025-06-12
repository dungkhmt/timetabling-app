package openerp.openerpresourceserver.wms.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    private Integer orderItemSeqId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderHeader order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;

    private BigDecimal amount;

    private String statusId;

    private String unit;

    private BigDecimal price;

    private BigDecimal discount;

    private BigDecimal tax;

}
