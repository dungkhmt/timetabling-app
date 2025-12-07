package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;

import java.util.*;

@Log4j2
public class GroupSchedulerSummerSemesterVersion3{
    SummerSemesterSolverVersion3 baseSolver;
    public List<ClassSegment> classSegments;
    int session;
    Map<String, Set<Long>> mCourse2ClassId;
    int[] occupationDay;
    int[] occupationSlot;
    public GroupSchedulerSummerSemesterVersion3(SummerSemesterSolverVersion3 baseSolver, List<ClassSegment> classSegments, int session) {
        this.baseSolver = baseSolver;
        this.classSegments = classSegments;
        this.session = session;
        occupationDay = new int[Constant.daysPerWeek + 2];
        occupationSlot = new int[Constant.slotPerCrew*Constant.daysPerWeek*2 + 2];
        Arrays.fill(occupationDay,0); Arrays.fill(occupationSlot,0);
        mCourse2ClassId = new HashMap<>();
        for(ClassSegment cs: classSegments){
            Long id = cs.getClassId();
            String courseCode = cs.getCourseCode();
            if(mCourse2ClassId.get(courseCode)==null)
                mCourse2ClassId.put(courseCode, new HashSet<>());
            mCourse2ClassId.get(courseCode).add(id);
            //if(courseCode.equals("MI2020"))
            //    log.info("GroupSchedulerSummerSemester constructor, consider class-segment " + cs.str() + " course " + courseCode + " sz = " + mCourse2ClassId.get(courseCode).size());
        }
    }
    public String name(){
        return "GroupSchedulerSummerSemesterVersion3";
    }
    public boolean solve(){
        log.info(name() + "::solve starts...nbCourses = " + mCourse2ClassId.keySet().size());
        for(String courseCode: mCourse2ClassId.keySet()){
            log.info("solve consider course " + courseCode);
            List<Long> CLS = new ArrayList<>();
            for(Long id: mCourse2ClassId.get(courseCode)) CLS.add(id);
            log.info("solve consider course " + courseCode + " CLS.sz = " + CLS.size());
            CourseSchedulerSummerSemesterVersion3 CSSS = new CourseSchedulerSummerSemesterVersion3(baseSolver,this,courseCode,CLS);
            boolean ok = CSSS.solve();
            if(!ok){
                log.info("solve: CSSS(" + courseCode + ") failed -> RETURN FASLE");
            }
        }
        log.info(name() + "::solve finished...nbCourses = " + mCourse2ClassId.keySet().size());
        return true;
    }
}

