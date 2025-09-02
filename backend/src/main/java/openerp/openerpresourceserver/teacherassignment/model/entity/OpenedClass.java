package openerp.openerpresourceserver.teacherassignment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import openerp.openerpresourceserver.teacherassignment.model.entity.relationship.BatchClass;

import java.util.List;
import java.util.Set;

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

    @OneToOne(mappedBy = "openedClass", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private BatchClass batchClass;

    @OneToMany(mappedBy = "openedClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeClass> timeClasses;

}
