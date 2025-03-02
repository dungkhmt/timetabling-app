package openerp.openerpresourceserver.wms.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wms2_facility")
public class Facility {
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "status_id", length = 100)
    private String statusId;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "created_stamp")
    private LocalDateTime createdStamp;
}
