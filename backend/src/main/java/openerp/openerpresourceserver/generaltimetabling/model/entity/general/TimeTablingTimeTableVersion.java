package openerp.openerpresourceserver.generaltimetabling.model.entity.general;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetabling_timetable_version")
public class TimeTablingTimeTableVersion {
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timetabling_timetable_version_seq_gen")
    @SequenceGenerator(name = "timetabling_timetable_version_seq_gen", sequenceName = "timetabling_timetable_version_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "status")
    private String status;

    @Column(name = "semester")
    private String semester;

    @Column(name = "created_by_user_id")
    private String createdByUserId;

    @Column(name = "created_stamp")
    private Date createdStamp;

    @Column(name = "number_slots_per_session")
    private Integer numberSlotsPerSession;

    @Column(name="batch_id")
    private Long batchId;

    @PrePersist
    protected void onCreate() {
        createdStamp = new Date();
    }
}