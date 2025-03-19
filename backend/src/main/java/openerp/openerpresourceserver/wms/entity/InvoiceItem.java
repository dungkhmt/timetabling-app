package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms2_invoice_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {
    @Id
    private String id;

    @Column(name = "invoice_item_seq_id", length = 10)
    private String invoiceItemSeqId;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
