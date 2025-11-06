package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetabling_batch")
public class TimeTablingBatch {
    @Id
    @Column(name="id")
    private Long Id;

    @Column(name="name")
    private String name;

    @Column(name="created_by_user_id")
    private String createdByUserId;

    @Column(name="semester")
    private String semester;

    @Column(name="created_stamp")
    private Date createdStamp;
}
