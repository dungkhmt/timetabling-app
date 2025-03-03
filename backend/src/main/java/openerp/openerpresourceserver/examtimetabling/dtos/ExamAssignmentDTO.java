package openerp.openerpresourceserver.examtimetabling.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExamAssignmentDTO {
    private String id;
    private String examClassId;
    private String classId;
    private String courseId;
    private String groupId;
    private String courseName;
    private String description;
    private Integer numberOfStudents;
    private String period;
    private String managementCode;
    private String school;
    private String roomId; 
    private String sessionId;
    private Integer weekNumber;
    private String date;
}
