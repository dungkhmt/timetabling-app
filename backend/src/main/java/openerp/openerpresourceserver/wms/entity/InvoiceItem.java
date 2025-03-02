package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_invoice_item")
public class InvoiceItem {
    @EmbeddedId
    private InvoiceItemPK id;

    @MapsId("invoiceId")
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
