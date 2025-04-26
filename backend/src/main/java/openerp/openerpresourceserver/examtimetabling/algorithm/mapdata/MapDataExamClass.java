package openerp.openerpresourceserver.examtimetabling.algorithm.mapdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MapDataExamClass {
    private int id;
    private String code;
    private String courseCode;
    private int groupId;
    private int nbStudents;
    private Set<Integer> domainSlot;

    public String toString(){
        String s = "";
        s = s + "[" + id + "], code = " + code + ", course " + courseCode + ", group = " + groupId + ", nbStudents = " + nbStudents;
        return s;
    }
}
