package openerp.openerpresourceserver.wms.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import openerp.openerpresourceserver.wms.constant.enumrator.SaleOrderStatus;

@Getter
@Setter
@Entity
@Builder
@Table(name = "wms2_order_header")
@NoArgsConstructor
@AllArgsConstructor
public class OrderHeader extends BaseEntity {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "order_type_id", length = 40)
    private String orderTypeId;

    private LocalDateTime orderDate;

    private String status;

    @ManyToOne
    @JoinColumn(name = "from_supplier_id")
    private Supplier fromSupplier;

    @ManyToOne
    @JoinColumn(name = "to_customer_id")
    private Customer toCustomer;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    @ManyToOne
    @JoinColumn(name = "facility_id")
    private Facility facility;



}