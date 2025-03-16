package openerp.openerpresourceserver.wms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.wms.entity.sequence.StringPrefixSequenceGenerator;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Table(name = "wms2_invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {
    @Id
    @Column(name = "id", length = 40)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wms2_invoice_sequences")
    @GenericGenerator(
            name = "wms2_invoice_sequences",
            strategy = "openerp.openerpresourceserver.wms.entity.sequence.StringPrefixSequenceGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.VALUE_PREFIX_PARAMETER, value = "ORD"),
                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d"),
                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.SEQUENCE_TABLE_PARAMETER, value = "wms2_invoice_sequences")
            })
    private String id;

    private String invoiceType;

    private String invoiceName;

    private String invoiceStatus;

    @ManyToOne
    @JoinColumn(name = "from_supplier_id")
    private Supplier fromSupplier;

    @ManyToOne
    @JoinColumn(name = "to_customer_id")
    private Customer toCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;


}
