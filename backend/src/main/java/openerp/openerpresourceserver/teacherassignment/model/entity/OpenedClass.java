package openerp.openerpresourceserver.teacherassignment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teacherclassassignment_opened_classes")
public class OpenedClass {
    @Id
    private Long classId;
    private Long accompaniedClassId;
    private String courseId;
    private String semester;
    private String note;
    private Long maxStudents;
    private String typeProgram;

    @OneToMany(mappedBy = "openedClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeClass> timeClasses;
}
