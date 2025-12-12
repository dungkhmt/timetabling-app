package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "timetabling_log_schedule")
public class TimetablingLogSchedule {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "version_id")
    private Long versionId;

    @Column(name = "class_segment_id")
    private Long classSegmentId;

    @Column(name = "class_code")
    private String classCode;

    @Column(name = "description")
    private String description;

    @Column(name = "created_stamp")
    private Date createdStamp;


}
