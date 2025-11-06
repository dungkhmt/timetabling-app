package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;

import java.util.List;

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

    @OneToMany(mappedBy = "studyingCourse",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OpenedClass> openedClass;
}
