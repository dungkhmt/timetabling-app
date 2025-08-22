package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.generaltimetabling.model.entity.composite.CompositeCourse;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
@IdClass(CompositeCourse.class)
public class StudyingCourse {
    @Id
    private String courseId;
    @Id
    private String typeProgram;
    private String schoolId;
    private String volume;
    private String courseName;
}
