package openerp.openerpresourceserver.wms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "wms2_shipment")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "shipment_type_id", length = 40)
    private String shipmentTypeId;

    @ManyToOne
    @JoinColumn(name = "from_supplier_id")
    private Supplier fromSupplier;

    @ManyToOne
    @JoinColumn(name = "to_customer_id")
    private Customer toCustomer;

    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderHeader order;
}
