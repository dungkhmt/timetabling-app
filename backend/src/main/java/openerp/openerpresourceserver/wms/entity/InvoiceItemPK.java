package openerp.openerpresourceserver.wms.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "invoice_id", length = 40)
    private String invoiceId;

    @Column(name = "invoice_item_seq_id", length = 10)
    private String invoiceItemSeqId;
}
