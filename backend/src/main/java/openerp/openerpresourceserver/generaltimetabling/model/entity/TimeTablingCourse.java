package openerp.openerpresourceserver.generaltimetabling.model.entity;

import jakarta.persistence.Column;
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
@Table(name = "timetabling_course")
public class TimeTablingCourse {
    @Id
    @Column(name="id")
    private String id;

    @Column(name="name")
    private String name;

    @Column(name="volumn")
    private String volumn;

    @Column(name="slots_priority")
    private String slotPriority;// 3,2,1

    @Column(name = "max_teacher_in_charge")
    private Integer maxTeacherInCharge;

    @Column(name="partition_lt_for_summer_semester")
    private String partitionLtForSummerSemester;

    @Column(name="partition_bt_for_summer_semester")
    private String partitionBtForSummerSemester;

    @Column(name="partition_lt_bt_for_summer_semester")
    private String partitionLtBtForSummerSemester;

    @Column(name = "max_student_lt")
    private Integer maxStudentLT;
    @Column(name = "max_student_bt")
    private Integer maxStudentBT;
    @Column(name = "max_student_lt_bt")
    private Integer maxStudentLTBT;

    @Column(name="duration_lt_bt")
    private Integer durationLtBt;
    @Column(name="duration_lt")
    private Integer durationLt;
    @Column(name="duration_bt")
    private Integer durationBt;

    @Column(name="separate_lt_bt")
    private String separateLTBT; // Y means that LT and BT classes must be separated

}
