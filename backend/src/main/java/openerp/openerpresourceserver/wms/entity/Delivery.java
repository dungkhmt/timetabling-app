package openerp.openerpresourceserver.wms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_delivery")
public class Delivery {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserLogin createdByUser;

    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;
}
