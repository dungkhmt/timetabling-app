package openerp.openerpresourceserver.examtimetabling.algorithm.mapdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapDataExamTimeTablingInput {
    private List<MapDataExamClass> classes;
    private List<MapDataRoom> rooms;// rooms can be used for schedule
    private List<Integer> slots;// slots can be used for schedule
    private List<Integer> days;// days can be used for schedule
    private List<int[]> conflicts;// (classId1, classId2) must be scheduled in distinct time slots
    private Map<Integer, List<Integer>> mRoom2OccupiedSlots;
    private Map<String, Integer> mCourse2NumberConsecutiveSlots;
    //private int maxDayScheduled = 6;// from monday, tuesday,... saturday

    public void print(){
        for(MapDataExamClass c: classes){
            System.out.println(c.toString());
        }
    }
}
