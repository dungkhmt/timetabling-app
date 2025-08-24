package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "courses")
public class StudyingCourse {
    @Id
    private String courseId;

    private String schoolId;
    private String volume;
    private String courseName;
}
