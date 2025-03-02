package openerp.openerpresourceserver.wms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_invoice")
public class Invoice {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;
}
