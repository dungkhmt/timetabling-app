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
}
