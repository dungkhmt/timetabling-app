package openerp.openerpresourceserver.wms.entity;


import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class DeliveryBillItemPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "delivery_bill_id", length = 40)
    private String deliveryBillId;

    @Column(name = "delivery_bill_item_seq_id", length = 10)
    private String deliveryBillItemSeqId;
}
