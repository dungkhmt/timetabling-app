package openerp.openerpresourceserver.wms.entity;


import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class OrderItemPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "order_id", length = 40)
    private String orderId;

    @Column(name = "order_item_seq_id", length = 10)
    private String orderItemSeqId;
}
