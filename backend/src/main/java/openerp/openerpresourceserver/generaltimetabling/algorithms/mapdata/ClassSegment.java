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
    public Long classId;
    public Long parentClassId;// id lop LT cua lop BT hien tai
    public List<Integer> groupIds;
    public Set<Integer> conflictClassSegmentIds; // set of conflicting class-segment
    public int duration;// so tiet
    public int courseIndex;
    public int nbStudents;
    public List<Integer> domainTimeSlots;
    public List<Integer> domainRooms;
    // additional and temp data for debugging (to be removed later)
    public String courseCode;
    public String groupNames;

    public String hashCourseGroup(){
        String s = courseIndex + "-";
        for(int j = 0; j < groupIds.size(); j++){
            s = s + groupIds.get(j);
            if(j < groupIds.size()-1) s = s + ",";
        }
        return s;
    }
    public String toString(){
        String s = "ClassSegment[" + id + "], classId = " + classId;
        s = s + " domain timeslot = ";
        for(int v: domainTimeSlots) s = s + v + ", ";
        s = s + " domain room = ";
        for(int v: domainRooms) s = s + v + ", ";
        s = s + " conflict class-segments indices = ";
        for(int j: conflictClassSegmentIds) s = s + j + ", ";
        return s;
    }
}
