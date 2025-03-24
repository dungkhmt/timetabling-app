package openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClassSegment {
    public int id;
    public int type; // 0 class cha (lop LT); 1 class con (lop BT)
    public int instanceIndex;// index (0,1,2,..) of the current class-segment in the corresponding general_class
                            // 0 means that class-segment is unique (no split) in the corresponding general_class
                            // 1,2,... current class-segment is real child (splited from general_class)
    public Long classId;
    public Long parentClassId;// id lop LT cua lop BT hien tai
    public List<Integer> groupIds;
    public Set<Integer> conflictClassSegmentIds; // set of conflicting class-segment
    public int duration;// so tiet
    public int courseIndex;
    public int nbStudents;
    public List<Integer> domainTimeSlots;
    public List<Integer> domainRooms;
    public boolean isScheduled;
    // additional and temp data for debugging (to be removed later)
    public String courseCode;
    public String groupNames;

    public String hashCourseGroup(){
        String s = courseIndex + "-" + type + "-" + instanceIndex + "-";
        for(int j = 0; j < groupIds.size(); j++){
            s = s + groupIds.get(j);
            if(j < groupIds.size()-1) s = s + ",";
        }
        return s;
    }
    public String toString(){
        String s = "ClassSegment[" + id + "], classId = " + classId;
        s = s + " courseIndex " + courseIndex + "(" + courseCode + ") ";
        s = s + " groups ";
        for(int g: getGroupIds()) s = s + g + ",";
        s = s + " domain timeslot = ";
        for(int v: domainTimeSlots) s = s + v + ", ";
        s = s + " domain room = ";
        for(int v: domainRooms) s = s + v + ", ";
        s = s + " conflict class-segments indices = ";
        for(int j: conflictClassSegmentIds) s = s + j + ", ";
        return s;
    }
}
