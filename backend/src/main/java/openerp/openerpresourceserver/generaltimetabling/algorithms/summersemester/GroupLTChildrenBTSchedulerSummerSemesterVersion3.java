package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class GroupLTChildrenBTSchedulerSummerSemesterVersion3 extends GroupSchedulerSummerSemesterVersion3{
    //SummerSemesterSolverVersion2 baseSolver;
    //public List<ClassSegment> classSegments;
    //int session;
    //Map<String, Set<Long>> mCourse2ClassId;
    //int[] occupationDay;
    //int[] occupationSlot;
    public GroupLTChildrenBTSchedulerSummerSemesterVersion3(SummerSemesterSolverVersion3 baseSolver, List<ClassSegment> classSegments, int session) {
        //this.baseSolver = baseSolver;
        //this.classSegments = classSegments;
        //this.session = session;

        super(baseSolver,classSegments,session);
        log.info("GroupLTChildrenBTSchedulerSummerSemester constructor classSegments = " + classSegments.size());
    }

    public boolean solve(){
        for(String courseCode: mCourse2ClassId.keySet()){
            List<Long> CLS_LT = new ArrayList<>();
            List<Long> CLS_BT = new ArrayList<>();
            for(Long id: mCourse2ClassId.get(courseCode)){
                ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
                if(cls.getClassType().equals("LT")) {
                    CLS_LT.add(id);
                    log.info("solve, course " + courseCode + " class LT " + cls.str());
                }else{
                    log.info("solve, course " + courseCode + " class BT " + cls.str());
                    CLS_BT.add(id);
                }
            }
            log.info("solve, course " + courseCode + " CLS_LT.sz = " + CLS_LT.size() + " CLS_BT.sz = " + CLS_BT.size());
            CourseSchedulerSummerSemesterVersion3 CSSS = new CourseSchedulerSummerSemesterVersion3(baseSolver,this,courseCode,CLS_LT);
            boolean ok = CSSS.solve();
            if(!ok){
                log.info("solve: CSSS(" + courseCode + ") failed -> RETURN FASLE");
            }

            CourseBTSchedulerSummerSemesterVersion3 CBSSS = new CourseBTSchedulerSummerSemesterVersion3(baseSolver,this,courseCode,CLS_BT);

            ok = CBSSS.solve();
            if(!ok){
                log.info("solve: CBSSS(" + courseCode + ") failed -> RETURN FASLE");
            }
        }
        return true;
    }
}

