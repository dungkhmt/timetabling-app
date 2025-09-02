package openerp.openerpresourceserver.teacherassignment.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.composite.CompositeTimeClass;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teacherclassassignment_time_class")
@IdClass(CompositeTimeClass.class)
public class TimeClass {
    @Id
    private Long classId;
    @Id
    private Integer sessionNumber;
    @Id
    private String week;

    private String shift;
    private String startTime;
    private String endTime;
    private String dayOfWeek;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classId", referencedColumnName = "classId", insertable = false, updatable = false)
    private OpenedClass openedClass;

}
