package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.util.List;
import java.util.Objects;

public class AClass {
    public int id;
    public String course;
    public List<AClassSegment> classSegments;

    public String toString() {
        String s = course;
        for (AClassSegment cs : classSegments)
            s = s + " [" + cs.id + "," + cs.duration + "] ";
        return s;
    }

    public AClass(int id, String course, List<AClassSegment> classSegments) {
        this.id = id;
        this.course = course;
        this.classSegments = classSegments;
    }
}
