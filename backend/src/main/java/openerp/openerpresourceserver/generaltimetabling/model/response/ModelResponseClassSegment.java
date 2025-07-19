package openerp.openerpresourceserver.generaltimetabling.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelResponseClassSegment {
    private Long id;
    private Long classId;
    private String classCode;
    private String courseCode;
    private String courseName;
    private String classType;
    private Integer maxNbStudents;
    private String groupNames;
    private Integer duration;
    private String learningWeeks;
    private String volumn;
    private String promotion;
    private Integer day;
    private String session; // S (morning) and C (afternoon)
    private Integer startTime;
    private Integer endTime;
    private String roomCode;

}
