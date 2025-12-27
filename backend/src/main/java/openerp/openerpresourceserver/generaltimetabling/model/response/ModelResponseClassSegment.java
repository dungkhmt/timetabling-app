package openerp.openerpresourceserver.generaltimetabling.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Group;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelResponseClassSegment {
    private Long id;
    private Long classId;
    private String classCode;
    private String parentClassCode;
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
    private List<Group> groups;
    private String color;

    public ModelResponseClassSegment(int day, String session, int startTime){
        setDay(day); setSession(session); setStartTime(startTime); setRoomCode("");
        setDuration(1);
    }
    public ModelResponseClassSegment(int day, String session, int startTime, String color){
        setDay(day); setSession(session); setStartTime(startTime); setRoomCode("");
        setDuration(1); setColor(color);
    }
}
