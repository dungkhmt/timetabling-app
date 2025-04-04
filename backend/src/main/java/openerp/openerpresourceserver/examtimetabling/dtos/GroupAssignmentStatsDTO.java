package openerp.openerpresourceserver.examtimetabling.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupAssignmentStatsDTO {
    private String groupName;
    private long totalClasses;
    private long assignedClasses;
    private long unassignedClasses;
    private double completionRate;
    private double averageRelaxTimeBetweenCourses; 
    private int daysWithMultipleExams;
}
