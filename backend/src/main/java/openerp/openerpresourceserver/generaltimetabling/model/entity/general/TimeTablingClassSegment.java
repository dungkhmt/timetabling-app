package openerp.openerpresourceserver.generaltimetabling.model.entity.general;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetabling_class_segment")
public class TimeTablingClassSegment {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "end_time")
    private Integer endTime;

    @Column(name="room")
    private String room;

    @Column(name="start_time")
    private Integer startTime;

    @Column(name="weekday")
    private Integer weekday;

    @Column(name="class_id")
    private Long classId;

    @Column(name="crew")
    private String crew;

    @Column(name="duration")
    private Integer duration;

    @Column(name="parent_id")
    private Long parentId;

    @Column(name="version_id")
    private Long versionId;
}
