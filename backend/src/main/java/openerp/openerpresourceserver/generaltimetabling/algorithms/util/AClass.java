package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AClass {
    public int id;
    public String course;
    public List<AClassSegment> classSegments;

    public String toString(){
        String s = course;
        for(AClassSegment cs: classSegments)
            s = s + " [" + cs.id + "," + cs.duration + "] ";
        return s;
    }
    public AClass(int id, String course, List<AClassSegment> classSegments) {
        this.id = id;
        this.course = course;
        this.classSegments = classSegments;
        sortClassSegment();
    }
    public void sortClassSegment(){
        classSegments.sort(new Comparator<AClassSegment>() {
            @Override
            public int compare(AClassSegment o1, AClassSegment o2) {
                return o1.duration - o2.duration;
            }
        });
    }
    public boolean identicalClassSegment(){
        for(int i = 0; i < classSegments.size()-1; i++)
            if(classSegments.get(i).duration != classSegments.get(i+1).duration)
                return false;
        return true;
    }
    public boolean sameClassSegmentsWith(AClass cls){
        if(classSegments.size() != cls.classSegments.size()) return false;
        for(int i = 0; i < classSegments.size(); i++){
            if(classSegments.get(i).duration != cls.classSegments.get(i).duration)
                return false;
        }
        return true;
    }
}
