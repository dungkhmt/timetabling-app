package openerp.openerpresourceserver.wms.entity;

import jakarta.persistence.*;
import lombok.*;
import openerp.openerpresourceserver.wms.constant.enumrator.ShipmentStatus;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "wms2_shipment")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment extends BaseEntity {
    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wms2_shipment_sequences")
//    @GenericGenerator(
//            name = "wms2_shipment_sequences",
//            strategy = "openerp.openerpresourceserver.wms.entity.sequence.StringPrefixSequenceGenerator",
//            parameters = {
//                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.VALUE_PREFIX_PARAMETER, value = "SHM"),
//                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d"),
//                    @org.hibernate.annotations.Parameter(name = StringPrefixSequenceGenerator.SEQUENCE_TABLE_PARAMETER, value = "wms2_shipment_sequences"),
//                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1") // Fix lỗi
//            })
    private String id;

    private String shipmentTypeId;

    private String shipmentName;

    private String statusId;

    private String note;

    @ManyToOne
    @JoinColumn(name = "from_supplier_id")
    private Supplier fromSupplier;

    @ManyToOne
    @JoinColumn(name = "to_customer_id")
    private Customer toCustomer;

    private LocalDate expectedDeliveryDate;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    @ManyToOne
    @JoinColumn(name = "handled_by_user_id")
    private UserLogin handledByUser;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderHeader order;

    @Override
    public void customPrePersist() {
       if(statusId == null || statusId.isEmpty()) {
           statusId = ShipmentStatus.CREATED.name();
       }
    }
}
