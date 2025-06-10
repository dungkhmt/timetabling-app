package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;

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

    private Integer invoiceItemSeqId;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
