package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

public class AClassSegment {
    public int id;
    String course;
    public int duration;

    public AClassSegment(int id, String course, int duration) {
        this.id = id;
        this.course = course;
        this.duration = duration;
    }
    
    public String getCourse() {
        return course;
    }
}
