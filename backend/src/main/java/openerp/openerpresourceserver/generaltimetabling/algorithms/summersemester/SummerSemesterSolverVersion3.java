package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import io.swagger.models.auth.In;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.*;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;



import java.util.*;


@Log4j2
class CourseSchedulerSummerSemesterVersion3{
    SummerSemesterSolverVersion3 baseSolver;
    GroupSchedulerSummerSemesterVersion3 groupSolver;
    public String courseCode;
    List<Long> CLS;// list of classes ids
    int[] occupationDay;
    int[] occupationSlot;
    Map<Long, Long> match;

    public CourseSchedulerSummerSemesterVersion3(SummerSemesterSolverVersion3 baseSolver, GroupSchedulerSummerSemesterVersion3 groupSolver, String courseCode, List<Long> CLS) {
        this.baseSolver = baseSolver;
        this.groupSolver = groupSolver;
        this.courseCode = courseCode;
        this.CLS = CLS;
        occupationDay = new int[Constant.daysPerWeek + 2];
        occupationSlot = new int[Constant.slotPerCrew*Constant.daysPerWeek*2 + 2];
        Arrays.fill(occupationDay,0); Arrays.fill(occupationSlot,0);
        match = new HashMap<>();
    }
    //public List<Long[]> matchClassInACourse(String courseCode, Map<String, Set<Long>> mCourse2ClassId){
    public List<Long[]> matchClassInACourse(List<Long> iCLS){

        List<Long> CLS = new ArrayList<>();
        for(Long id: iCLS) CLS.add(id);
        List<Long[]> res = new ArrayList<>();
        MatchScore[][] m = new MatchScore[CLS.size()][CLS.size()];
        while(CLS.size() > 0){
            MatchScore maxScore = null;
            Long sel_id1 = null; Long sel_id2 = null;
            for(int i = 0; i < CLS.size(); i++){
                for(int j = i+1; j < CLS.size(); j++){
                    Long id1 = CLS.get(i); Long id2 = CLS.get(j);
                    MatchScore ms = baseSolver.matchScore(id1,id2, Constant.slotPerCrew);
                    if(ms!= null && ms.score >= 0){
                        if(maxScore == null || maxScore.score < ms.score){
                            maxScore = ms; sel_id1 = id1; sel_id2 = id2;
                        }
                    }
                }
            }
            if(sel_id1 != null && sel_id2 != null && maxScore.m.keySet().size() > 0){
                CLS.remove(sel_id1); CLS.remove(sel_id2);
                Long[] p = new Long[2]; p[0] = sel_id1; p[1] = sel_id2; res.add(p);
            }else{
                break;
            }
        }
        return res;
    }

    public boolean findAssignSeparateSlotDay(Long id){
        log.info("findAssignSeparateSlotDay starts...");
        List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(id);
        int n = CS.size();
        if(n < 1 || n > 3){
            log.info("findAssignSeparateSlotDay, EXCEPTION!!! , number of class-segments of class " + id + " = " + n + " is out-of-scope");
            return false;
        }
        int[] selDay = null; int minOcc = 100000000;
        for(int[] days : baseSolver.optionDays.get(n-1)){
            int occ = 0;
            for(int d: days){
                occ += occupationDay[d];
            }
            if(occ < minOcc){
                minOcc = occ; selDay = days;
            }
        }
        log.info("findAssignSeparateSlotDay selDay = " + selDay);
        int[] slots = new int[selDay.length];
        for(int i = 0; i < CS.size(); i++){
            ClassSegment cs = CS.get(i);
            int day = selDay[i]; int session = groupSolver.session; int slot = -1;
            int s1 = 1; // tiet 1
            int s2 = Constant.slotPerCrew - cs.duration +1; // tiet cuoi
            if(i == 0){// first class segment
                if(occupationSlot[s1] > occupationSlot[s2]) slot = s2; else slot = s1;
            }else{
                if(slots[i-1] == 1) slot = s2; else slot = s1;
            }
            slots[i] = slot;
            int sl = new DaySessionSlot(day,session,slot).hash();
            boolean ok = baseSolver.assignTimeSlot(cs,sl);
            occupationSlot[sl] += 1; occupationDay[day] += 1;
        }
        return true;
    }
    public boolean findAssignSeparateSlotDay4FirstClassOfCourse(Long id){
        log.info("findAssignSeparateSlotDay starts...");
        List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(id);
        int n = CS.size();
        if(n < 1 || n > 3){
            log.info("findAssignSeparateSlotDay, EXCEPTION!!! , number of class-segments of class " + id + " = " + n + " is out-of-scope");
            return false;
        }
        int[] selDay = null; int minOcc = 100000000;
        for(int[] days : baseSolver.optionDays.get(n-1)){
            int occ = 0;
            for(int d: days){
                occ += groupSolver.occupationDay[d];
            }
            if(occ < minOcc){
                minOcc = occ; selDay = days;
            }
        }
        log.info("findAssignSeparateSlotDay selDay = " + selDay);
        int[] slots = new int[selDay.length];
        for(int i = 0; i < CS.size(); i++){
            ClassSegment cs = CS.get(i);
            int day = selDay[i]; int session = groupSolver.session; int slot = -1;
            int s1 = 1; // tiet 1
            int s2 = Constant.slotPerCrew - cs.duration +1; // tiet cuoi
            if(i == 0){// first class segment
                if(groupSolver.occupationSlot[s1] > groupSolver.occupationSlot[s2]) slot = s2; else slot = s1;
            }else{
                if(slots[i-1] == 1) slot = s2; else slot = s1;
            }
            slots[i] = slot;
            int sl = new DaySessionSlot(day,session,slot).hash();
            boolean ok = baseSolver.assignTimeSlot(cs,sl);
            groupSolver.occupationSlot[sl] += 1; groupSolver.occupationDay[day] += 1;
        }
        return true;
    }
    public int findSeparateSlot(ClassSegment cs, List<ClassSegment> scheduledCS){
        int session = groupSolver.session;
        int selDay = -1;
        Set<Integer> scheduledDays = new HashSet<>();
        for(ClassSegment scs: scheduledCS){
            if(baseSolver.solutionSlot.get(scs.getId())==null){
                log.info("findSeparateSlot, BUG?? ");
            }
            int sl = baseSolver.solutionSlot.get(scs.getId());
            DaySessionSlot dss = new DaySessionSlot(sl);
            scheduledDays.add(dss.day);
        }
        int minOcc = 10000000; int selSlot = -1;
        int s1 = 1; int s2 = Constant.slotPerCrew - cs.getDuration() + 1;
        for(int sl: cs.getDomainTimeSlots()){
            DaySessionSlot dss = new DaySessionSlot(sl);
            if(!scheduledDays.contains(dss.day)){
                if(dss.slot == s1 || dss.slot == s2){
                    if(occupationSlot[sl] < minOcc){
                        minOcc = occupationSlot[sl]; selSlot = sl;
                    }
                }
            }
        }
        return selSlot;
    }
    public boolean assignSlot4MatchClass(Long id, MatchScore ms){
        ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
        List<ClassSegment> CS = new ArrayList<>();
        for(ClassSegment cs: baseSolver.mClassId2ClassSegments.get(id)) CS.add(cs);
        List<ClassSegment> scheduledCS = new ArrayList<>();
        while(CS.size() > 0) {
            // consider first class-segment matched
            boolean ok = false;
            for (ClassSegment cs : CS) {
                if (ms.m.get(cs) != null) {
                    ClassSegment m_cs = ms.m.get(cs);
                    log.info("assignSlot4MatchClass, findConsecutiveSlot for class-segment " + cs.str() + " matched class segment = " + m_cs.str());
                    int startSlot = baseSolver.findConsecutiveSlot(cs, m_cs);
                    DaySessionSlot dss = new DaySessionSlot(startSlot);
                    log.info("assignSlot4MatchClass, findConsecutiveSlot for class-segment " + cs.str() + " of class " + cls.getClassCode() + " RETURN startSlot = " + startSlot + " dss.slot =  " + dss.slot);
                    baseSolver.assignTimeSlot(cs,startSlot);
                    occupationDay[dss.day] += 1; occupationSlot[startSlot] += 1;
                    CS.remove(cs); scheduledCS.add(cs); ok = true;
                    baseSolver.mClassSegment2MatchedClassSegment.put(cs,m_cs);
                    baseSolver.mClassSegment2MatchedClassSegment.put(m_cs,cs);

                    break;
                }
            }
            if(!ok) break;
        }
        // SECOND consider non-matched class segments
        for(ClassSegment cs: CS){
            int slot = findSeparateSlot(cs,scheduledCS);
            if(slot != -1){
                DaySessionSlot dss = new DaySessionSlot(slot);
                log.info("assignSlot4MatchClass, findSeparateSlot for class-segment " + cs.str() + " of class " + cls.getClassCode() + " RETURN slot = " + slot + " dss.slot =  " + dss.slot + " dayPerWeek = " + Constant.daysPerWeek + " slotPerCrew = " + Constant.slotPerCrew);
                baseSolver.assignTimeSlot(cs,slot);
                occupationSlot[slot] += 1; occupationDay[dss.day] += 1;
            }else{
                log.info("assignSlot4MatchClass, CANNOT find any slot for class segment " + cs.str() + "?????");
                return false;
            }
        }
        return true;
    }

    public boolean solve(){
        log.info("solve starts.... course = "  + courseCode);
        //List<Long> CLS = new ArrayList<>();
        //for(Long id: groupSolver.mCourse2ClassId.get(courseCode)) CLS.add(id);
        for(Long id: CLS){
            ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
            log.info("solve, has class " + cls.str());
        }
        List<Long[]> pairs = matchClassInACourse(CLS);
        log.info("solve matchClassInACourse OK -> pairs = " + (pairs == null ? "NULL": pairs.size()));
        if(pairs != null) for(Long[] p: pairs){
            match.put(p[0],p[1]); match.put(p[1],p[0]);
        }
        List<Long> ids = new ArrayList<>();
        //for(Long id: groupSolver.mCourse2ClassId.get(courseCode)) ids.add(id);
        for(Long id: CLS) ids.add(id);
        boolean firstClass = true;
        for(Long id: ids){
            //if(firstClass){
            //    firstClass = false;
            //    findAssignSeparateSlotDay4FirstClassOfCourse(id);
            //    continue;
            //}

            if(baseSolver.isScheduled(id)){ continue; }
            if(match.get(id) != null){
                Long mid = match.get(id);
                if(!baseSolver.isScheduled(mid)){
                    log.info("solve: findAssignSeparateSlotDay(" + id + ") -> start");
                    if(firstClass){
                        findAssignSeparateSlotDay4FirstClassOfCourse(id); firstClass = false;
                    }else {
                        findAssignSeparateSlotDay(id);
                    }
                    log.info("solve: findAssignSeparateSlotDay(" + id + ") -> OK");
                    MatchScore ms = baseSolver.matchScore(id,mid,Constant.slotPerCrew);
                    log.info("solve: assignSlot4MatchClass(" + mid + ") starts ");
                    if(ms.m != null){
                        assignSlot4MatchClass(mid,ms);
                    }else{
                        log.info("solve: assignSlot4MatchClass(" + mid + ") starts BUG???? ");
                        if(firstClass){
                            findAssignSeparateSlotDay4FirstClassOfCourse(id); firstClass = false;
                        }else {
                            findAssignSeparateSlotDay(id);
                        }
                    }
                    log.info("solve: assignSlot4MatchClass(" + mid + ") -> OK ");
                }else{
                    log.info("solve, BUG??? this case cannot happen");
                }
            }else{
                log.info("solve: NO MATCH findAssignSeparateSlotDay(" + id + ") -> start");
                if(firstClass){
                    findAssignSeparateSlotDay4FirstClassOfCourse(id); firstClass = false;
                }else {
                    findAssignSeparateSlotDay(id);
                }
                log.info("solve: NO MATCH findAssignSeparateSlotDay(" + id + ") -> OK");
            }
        }
        return true;
    }
}

@Log4j2
class CourseBTSchedulerSummerSemesterVersion3 extends CourseSchedulerSummerSemesterVersion3{
    public CourseBTSchedulerSummerSemesterVersion3(SummerSemesterSolverVersion3 baseSolver, GroupSchedulerSummerSemesterVersion3 groupSolver, String courseCode, List<Long> CLS) {
        super(baseSolver,groupSolver,courseCode, CLS);
    }

    @Override
    public boolean findAssignSeparateSlotDay4FirstClassOfCourse(Long id){
        return true;
    }

    @Override
    public boolean findAssignSeparateSlotDay(Long id){
        return true;
    }

    @Override
    public boolean assignSlot4MatchClass(Long id, MatchScore ms){
        return true;
    }

    public Set<Integer> collectDaysScheduled(Long id){
        List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(id);
        Set<Integer> days = new HashSet<>();
        for(ClassSegment cs: CS){
            if(baseSolver.solutionSlot.get(cs.getId()) == null){
                log.info("collectDaysScheduled(" + id + ") BUG?? class-segment cs = " + cs.str() + " not scheduled");
            }
            int sl = baseSolver.solutionSlot.get(cs.getId());
            DaySessionSlot dss = new DaySessionSlot(sl);
            days.add(dss.day);
        }
        return days;
    }
    public boolean findAssignSlotForTwoMatchedChildrenClass(Long id, Long cid1, Long cid2, MatchScore ms){
        Set<Integer> days = collectDaysScheduled(id);
        Set<Integer> candDays = new HashSet<>();
        ModelResponseTimeTablingClass cls1 = baseSolver.mClassId2Class.get(cid1);
        ModelResponseTimeTablingClass cls2 = baseSolver.mClassId2Class.get(cid2);

        log.info("findAssignSlotForTwoMatchedChildrenClass, pid = " + id + " cid1  " + cid1 + " cid2 = " +
                cid2 + " days = " + days.size() + " cls1 = " + cls1.str() + " cls2 = " + cls2.str() + " match score = " + ms.score);


        List<ClassSegment> CS1 = baseSolver.mClassId2ClassSegments.get(cid1);
        List<ClassSegment> CS2 = baseSolver.mClassId2ClassSegments.get(cid2);
        if(CS1.size() == 0 || CS2.size() == 0) return true;
        for(int sl: CS1.get(0).getDomainTimeSlots()){
            DaySessionSlot dss = new DaySessionSlot(sl);
            candDays.add(dss.day);
        }
        for(ClassSegment cs: CS1){
            if(baseSolver.solutionSlot.get(cs.getId())!=null && baseSolver.solutionSlot.get(cs.getId())!=-1){
                log.info("findAssignSlotForTwoMatchedChildrenClass, cs " + cs.str() + " WAS ASSIGNED continue");
                continue;
            }

            int day = -1; int minOcc = 100000;
            //for(int d = 2; d < Constant.daysPerWeek + 2; d++)
            for(int d: candDays)
                if(!days.contains(d)){
                    if(occupationDay[d] < minOcc){
                        minOcc = occupationDay[d]; day= d;
                    }
                }
            int s1 = 1;
            int s2 = Constant.slotPerCrew - cs.getDuration() + 1;
            int slot = -1; minOcc = 100000000;
            DaySessionSlot dss = new DaySessionSlot(day, groupSolver.session, s1);
            if(occupationSlot[dss.hash()] < minOcc){
                minOcc = occupationSlot[dss.hash()]; slot = s1;
            }
            dss = new DaySessionSlot(day, groupSolver.session, s2);
            if(occupationSlot[dss.hash()] < minOcc){
                minOcc = occupationSlot[dss.hash()]; slot = s2;
            }
            log.info("findAssignSlotForTwoMatchedChildrenClass, found slot for cs " + cs.str() + " day " + day + " slot " + slot);
            int sl1 = new DaySessionSlot(day, groupSolver.session, slot).hash();
            baseSolver.assignTimeSlot(cs,sl1);
            for(int s = 0; s < cs.getDuration(); s++) occupationSlot[sl1 + s] += 1;
            occupationDay[day] += 1;
            days.add(day);
            if(ms.m != null && ms.m.get(cs)!=null){
                ClassSegment mcs = ms.m.get(cs);
                log.info("findAssignSlotForTwoMatchedChildrenClass, cs = " + cs.str() + " found matched mcs = " + mcs.str());
                int slot2 = slot + cs.getDuration();
                if(slot2 + mcs.getDuration() - 1 > Constant.slotPerCrew){
                    slot2 = slot - mcs.getDuration();
                }
                log.info("findAssignSlotForTwoMatchedChildrenClass, found mcs " + mcs.str() + " day " + day + ", slot = " + slot2);
                int sl2 = new DaySessionSlot(day, groupSolver.session, slot2).hash();
                baseSolver.assignTimeSlot(mcs,sl2);
                for(int s = 0; s < mcs.getDuration(); s++) occupationSlot[sl2 + s] += 1;
                occupationDay[day] += 1; days.add(day);

                baseSolver.mClassSegment2MatchedClassSegment.put(cs,mcs);
                baseSolver.mClassSegment2MatchedClassSegment.put(mcs,cs);

            }else{
                //log.info("findAssignSlotForTwoMatchedChildrenClass BUG???");
            }
        }

        for(ClassSegment cs: CS2){
            if(baseSolver.solutionSlot.get(cs.getId())!=null && baseSolver.solutionSlot.get(cs.getId())!=-1){
                log.info("findAssignSlotForTwoMatchedChildrenClass, cs2 " + cs.str() + " WAS ASSIGNED");
                continue;
            }

            int day = -1; int minOcc = 100000;
            //for(int d = 2; d < Constant.daysPerWeek + 2; d++)
            for(int d: candDays)
                if(!days.contains(d)){
                    if(occupationDay[d] < minOcc){
                        minOcc = occupationDay[d]; day= d;
                    }
                }
            int s1 = 1;
            int s2 = Constant.slotPerCrew - cs.getDuration() + 1;
            int slot = -1; minOcc = 100000000;
            DaySessionSlot dss = new DaySessionSlot(day, groupSolver.session, s1);
            if(occupationSlot[dss.hash()] < minOcc){
                minOcc = occupationSlot[dss.hash()]; slot = s1;
            }
            dss = new DaySessionSlot(day, groupSolver.session, s2);
            if(occupationSlot[dss.hash()] < minOcc){
                minOcc = occupationSlot[dss.hash()]; slot = s2;
            }
            log.info("findAssignSlotForTwoMatchedChildrenClass, found slot for cs2 " + cs.str() + " day " + day + " slot = " + slot);
            int sl2 = new DaySessionSlot(day, groupSolver.session, slot).hash();
            baseSolver.assignTimeSlot(cs,sl2);
            for(int s = 0; s < cs.getDuration(); s++) occupationSlot[sl2 + s] += 1;
            occupationDay[day] += 1;
            days.add(day);
        }

        return true;
    }
    public boolean findAssignSlotForAChildrenClass(Long pid, Long cid){
        Set<Integer> days = collectDaysScheduled(pid);
        Set<Integer> candDays = new HashSet<>();
        ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(cid);
        List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cid);
        if(CS.size() == 0) return true;
        for(int sl: CS.get(0).getDomainTimeSlots()){
            DaySessionSlot dss = new DaySessionSlot(sl);
            candDays.add(dss.day);
        }

        log.info("findAssignSlotForAChildrenClass, cid = " + cid + ", child cls = " + cls.str() + " CS = " + CS.size());
        for(ClassSegment cs: CS){
            if(baseSolver.solutionSlot.get(cs.getId())!=null &&
                    baseSolver.solutionSlot.get(cs.getId())!=-1) continue;

            int day = -1; int minOcc = 10000000;
            //for(int d = 2; d < Constant.daysPerWeek + 2; d++)
            for(int d: candDays)
                if(!days.contains(d)){
                    if(occupationDay[d] < minOcc){
                        minOcc = occupationDay[d]; day= d;
                    }
                }
            int s1 = 1;
            int s2 = Constant.slotPerCrew - cs.getDuration() + 1;
            int slot = -1; minOcc = 100000000;
            DaySessionSlot dss = new DaySessionSlot(day, groupSolver.session, s1);
            if(occupationSlot[dss.hash()] < minOcc){
                minOcc = occupationSlot[dss.hash()]; slot = s1;
            }
            dss = new DaySessionSlot(day, groupSolver.session, s2);
            if(occupationSlot[dss.hash()] < minOcc){
                minOcc = occupationSlot[dss.hash()]; slot = s2;
            }
            int sl = new DaySessionSlot(day, groupSolver.session, slot).hash();
            baseSolver.assignTimeSlot(cs,sl);
            for(int s = 0; s < cs.getDuration(); s++) occupationSlot[sl + s] += 1;
            occupationDay[day] += 1;
            days.add(day);

        }

        return true;
    }

    public boolean findAssignSlot2ChildClasses(Long parentClassId, List<Long> childIds){
        Set<Integer> days = collectDaysScheduled(parentClassId);
        ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(parentClassId);

        log.info("findAssignSlot2ChildClasses, days = " + days.size() + " course = " + cls.getModuleCode() + " cls = " + cls.str()) ;

        Map<Long, Long> match = new HashMap<>();

        List<Long[]> pairs = matchClassInACourse(childIds);

        if(pairs != null){
            log.info("findAssignSlot2ChildClasses, days = " + days.size() + " course = " +
                    cls.getModuleCode() + " cls = " + cls.str() + " pairs = " + pairs.size() + " chlidIds = " + childIds.size()) ;
            for(Long[] p: pairs){
                match.put(p[0],p[1]); match.put(p[1],p[0]);
            }
        }
        for(Long id: childIds){
            log.info("findAssignSlot2ChildClasses, child id = " + id + " match = " + match.get(id));
            if(match.get(id) != null){
                Long mid = match.get(id);
                MatchScore ms = baseSolver.matchScore(id,mid,Constant.slotPerCrew);
                boolean ok = findAssignSlotForTwoMatchedChildrenClass(parentClassId,id,mid,ms);
            }else{
                boolean ok = findAssignSlotForAChildrenClass(parentClassId,id);
            }
        }
        return true;
    }
    @Override
    public boolean solve(){
        log.info("solve starts.... course = "  + courseCode);
        //List<Long> CLS = new ArrayList<>();
        //for(Long id: groupSolver.mCourse2ClassId.get(courseCode)) CLS.add(id);
        //Set<Long> parentIds = new HashSet<>();
        Map<Long, List<Long>> mParentClassId2ChildIds = new HashMap<>();
        for(Long id: CLS){
            ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
            Long pid =cls.getParentClassId();
            if(pid != null) {
                if(mParentClassId2ChildIds.get(pid)==null)
                    mParentClassId2ChildIds.put(pid, new ArrayList<>());
                mParentClassId2ChildIds.get(pid).add(id);
            }else{
                log.info("solve, BUG??? class BT " + cls.str() + " does not have parent");
            }
            log.info("solve, has class " + cls.str());
        }
        log.info("solve, mParentClassId2ChildIds.sz = " + mParentClassId2ChildIds.keySet().size());
        for(Long pid: mParentClassId2ChildIds.keySet()){
            List<Long> childIds = mParentClassId2ChildIds.get(pid);
            if(childIds == null){
                log.info("solve, pid = " + pid + " childids = NULL, BUG???" ); return false;
            }
            log.info("solve, pid = " + pid + " childids = " + childIds.size());
            boolean ok = findAssignSlot2ChildClasses(pid,childIds);
        }
        return true;
    }

}
@Log4j2
class GroupSchedulerSummerSemesterVersion3{
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
    public boolean solve(){
        log.info("solve starts...nbCourses = " + mCourse2ClassId.keySet().size());
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
        return true;
    }
}

@Log4j2
class GroupLTChildrenBTSchedulerSummerSemesterVersion3 extends GroupSchedulerSummerSemesterVersion3{
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
class ClassSolver{
    SummerSemesterSolverVersion3 baseSolver;
    ModelResponseTimeTablingClass cls;
    int[] days;
    int room;
    List<ClassSegment> CS;
    List<int[]> domains;
    int[] x_slot;
    int[] solutionSlot = null;
    boolean found = false;
    public ClassSolver(ModelResponseTimeTablingClass cls, int[] days, int room, SummerSemesterSolverVersion3 baseSolver){
        this.baseSolver = baseSolver;
        this.cls = cls; this.days = days;
        this.room = room;
        CS = baseSolver.mClassId2ClassSegments.get(cls.getId());
        domains = new ArrayList<>();
        for(ClassSegment cs: CS){
            int[] d = new int[2];// consider 2 options: tiet dau va tiet cuoi
            d[0] = 1;
            d[1] = Constant.slotPerCrew - cs.getDuration() + 1;
            domains.add(d);
        }
        x_slot = new int[CS.size()];
    }
    private void solution(){
        boolean ok = true;
        for(int i = 0; i < CS.size(); i++){
            if(!baseSolver.roomSolver.checkValidSlotAndRoom(CS.get(i),x_slot[i],room)){
                ok = false; break;
            }
        }
        if(ok){
            solutionSlot = new int[CS.size()];
            for(int i = 0; i < CS.size(); i++) solutionSlot[i] = x_slot[i];
            found = true;
        }
    }
    private void tryClassSegment(int i){
        if(found) return;
        for(int s: domains.get(i)){
            x_slot[i] = s;
            if(i == CS.size()-1){
                solution();
            }else{
                tryClassSegment(i+1);
            }
        }
    }
    public void solve(){
        found = false; solutionSlot = null;
        tryClassSegment(0);
    }
}
@Log4j2
class ClassClusterScheduleTimeSlotRoomSolver{
    SummerSemesterSolverVersion3 baseSolver = null;
    List<Long> classIds;
    int session;
    List<ModelResponseTimeTablingClass> classes;
    Set<Long> unScheduledClassIds;
    public ClassClusterScheduleTimeSlotRoomSolver(List<Long> classIds, int session, SummerSemesterSolverVersion3 baseSolver){
        this.classIds = classIds; this.baseSolver = baseSolver;
        this.session = session;
    }
    public boolean solve(){
        ModelResponseTimeTablingClass[] a = new ModelResponseTimeTablingClass[classIds.size()];
        for(int i = 0; i < classIds.size(); i++){
            Long id = classIds.get(i);
            ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
            a[i] = cls;
        }
        // sort
        for(int i = 0; i < a.length; i++)
        {
            for(int j = i+1 ; j < a.length; j++){
                if(a[i].getQuantityMax() < a[j].getQuantityMax()){
                    ModelResponseTimeTablingClass t = a[i]; a[i] = a[j]; a[j] = t;
                }
            }
        }
        Set<Integer> candRooms = new HashSet<>();
        for(Long id: classIds){
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(id);
            for(ClassSegment cs: CS){
                for(int r: cs.getDomainRooms()) candRooms.add(r);
            }
        }
        int[] sortedRooms = baseSolver.sortCapacityRoom(candRooms,true);

        unScheduledClassIds = new HashSet<>();
        for(ModelResponseTimeTablingClass cls: a){
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls.getId());
            int selectedRoom = -1;
            int[] selectedDays = null;
            int[] selectedSlots = null;
            for(int r: sortedRooms){
                for(int[] days: baseSolver.optionDays.get(CS.size())){
                    ClassSolver classSolver = new ClassSolver(cls,days,r,baseSolver);
                    classSolver.solve();
                    if(classSolver.found){
                        selectedRoom = r; selectedDays = days; selectedSlots = classSolver.solutionSlot;
                    }
                    if(selectedRoom != -1){
                        break;
                    }
                }
                if(selectedRoom != -1){
                    break;
                }
            }
            if(selectedRoom != -1) {// found solution slot-room for cls
                for (int i = 0; i < CS.size(); i++) {
                    ClassSegment cs = CS.get(i);
                    int sl = new DaySessionSlot(selectedDays[i],session,selectedSlots[i]).hash();
                    //baseSolver.assignTimeSlotRoom(cs,sl,selectedRoom);
                    baseSolver.roomSolver.assignTimeSlotRoom(cs,sl,selectedRoom);
                }
            }else{
                unScheduledClassIds.add(cls.getId());
            }
        }
        return true;
    }
}
@Log4j2
class ClassBasedRoomAssignmentSolverVersion3{
    SummerSemesterSolverVersion3 baseSolver;
    int[] roomOccupation;// roomOccupation[r] is number of slots the room is occupied
    int[][] roomSlotOccupation; // roomSlotOccupation[r][sl] = 1 means that room r is occupied at slot sl

    public ClassBasedRoomAssignmentSolverVersion3(SummerSemesterSolverVersion3 baseSolver) {
        this.baseSolver = baseSolver;
        roomOccupation = new int[baseSolver.I.getRoomCapacity().length];
        Arrays.fill(roomOccupation,0);

    }

    public boolean checkValidRoom(ClassSegment cs, int r){
        if(baseSolver.solutionSlot.get(cs.getId())==null) return false;
        int sl = baseSolver.solutionSlot.get(cs.getId());
        if(sl >= 0){
            for(int d = 0; d < cs.getDuration(); d++){
                if(roomSlotOccupation[r][sl+d] > 0) return false;
            }
        }
        return true;
    }
    public boolean checkValidSlotAndRoom(ClassSegment cs, int sl, int r){
        //if(baseSolver.solutionSlot.get(cs.getId())==null) return false;
        //int sl = baseSolver.solutionSlot.get(cs.getId());
        if(sl >= 0){
            for(int d = 0; d < cs.getDuration(); d++){
                if(roomSlotOccupation[r][sl+d] > 0) return false;
            }
        }
        return true;
    }
    //public boolean checkValidRoom(ModelResponseTimeTablingClass cls, int r){

    //}

    public int selectRoom4MatchedClassSegment(ClassSegment cs, ClassSegment mcs, List<Integer> sortedRooms){
        int sel_room = -1; int maxOcc = -1;
        for(int r: sortedRooms){
            if(cs.getDomainRooms().contains(r) && mcs.getDomainRooms().contains(r)){
                if(checkValidRoom(cs,r) && checkValidRoom(mcs,r)){
                    if(roomOccupation[r] > maxOcc){
                        maxOcc = roomOccupation[r]; sel_room = r;
                    }
                }
            }
        }
        return sel_room;
    }
    public int selectRoomClassSegment(ClassSegment cs, List<Integer> sortedRooms){
        int sel_room = -1; int maxOcc = -1;
        for(int r: sortedRooms){
            if(cs.getDomainRooms().contains(r)){
                if(checkValidRoom(cs,r)){
                    if(roomOccupation[r] > maxOcc){
                        maxOcc = roomOccupation[r]; sel_room = r;
                    }
                }
            }
        }
        return sel_room;
    }
    public void unAssignRoom(ClassSegment cs, int r){
        int sl = baseSolver.solutionSlot.get(cs.getId());
        baseSolver.unAssignTimeSlotRoom(cs,sl,r);
        for(int s = 0; s < cs.getDuration(); s++)
            roomSlotOccupation[r][sl + s] = 0;
        roomOccupation[r] -= cs.getDuration();
    }

    public void assignRoom(ClassSegment cs, int r){
        int sl = baseSolver.solutionSlot.get(cs.getId());
        baseSolver.assignTimeSlotRoom(cs,sl,r);
        for(int s = 0; s < cs.getDuration(); s++)
            roomSlotOccupation[r][sl + s] = 1;
        roomOccupation[r] += cs.getDuration();
    }
    public void assignTimeSlotRoom(ClassSegment cs, int sl, int r){
        //int sl = baseSolver.solutionSlot.get(cs.getId());
        baseSolver.assignTimeSlotRoom(cs,sl,r);
        for(int s = 0; s < cs.getDuration(); s++)
            roomSlotOccupation[r][sl + s] = 1;
        roomOccupation[r] += cs.getDuration();
    }

    public boolean solve(){
        List<ModelResponseTimeTablingClass> classes = new ArrayList<>();
        for(ModelResponseTimeTablingClass cls: baseSolver.classes) classes.add(cls);
        classes.sort(new Comparator<ModelResponseTimeTablingClass>() {
            @Override
            public int compare(ModelResponseTimeTablingClass o1, ModelResponseTimeTablingClass o2) {
                return o2.getQuantityMax() - o1.getQuantityMax();
            }
        });
        for(int i = 0; i <  classes.size(); i++){
            ModelResponseTimeTablingClass cls = classes.get(i);
            log.info("reAssignRooms, after sorting classes, cls[" + i + "/" + classes.size() + "] " + cls.getClassCode() + "," + cls.getModuleCode() + ", qty = " + cls.getQuantityMax());
        }
        Set<Integer> setRooms = new HashSet<>();
        for(ClassSegment cs: baseSolver.classSegments){
            for(int r: cs.getDomainRooms()) setRooms.add(r);
        }
        int[] arrRooms = new int[setRooms.size()];
        int idx = -1;
        for(int r: setRooms){ idx = idx + 1; arrRooms[idx] = r; }

        // SORT decreasing order of capacity
        for(int i = 0; i < arrRooms.length; i++){
            for(int j = i+1; j < arrRooms.length; j++) {
                //if(baseSolver.I.getRoomCapacity()[arrRooms[i]] > baseSolver.I.getRoomCapacity()[arrRooms[j]]){
                //if(baseSolver.I.getRoomCapacity()[arrRooms[i]] < baseSolver.I.getRoomCapacity()[arrRooms[j]]){
                //if(baseSolver.I.getRoomCapacity()[arrRooms[i]] > baseSolver.I.getRoomCapacity()[arrRooms[j]]){
                boolean swap = false;
                if(baseSolver.I.getRoomCapacity()[arrRooms[i]] < baseSolver.I.getRoomCapacity()[arrRooms[j]]){
                    swap = true;
                }else if(baseSolver.I.getRoomCapacity()[arrRooms[i]] == baseSolver.I.getRoomCapacity()[arrRooms[j]]){
                    String ri = baseSolver.W.mIndex2Room.get(arrRooms[i]).getClassroom();
                    String rj = baseSolver.W.mIndex2Room.get(arrRooms[j]).getClassroom();
                    int c = ri.compareTo(rj);
                    if(c > 0) swap = true;
                }
                if(swap) {
                    int tmp = arrRooms[i]; arrRooms[i] = arrRooms[j]; arrRooms[j] = tmp;
                }
            }
        }
        List<Integer> sortedRooms = new ArrayList<>();
        for(int i = 0; i < arrRooms.length; i++) sortedRooms.add(arrRooms[i]);
        /*
        sortedRooms.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return baseSolver.I.getRoomCapacity()[01] - baseSolver.I.getRoomCapacity()[o2];
            }
        });
        */
        int maxRoomIndex = 0;
        for(int r: sortedRooms){
            log.info("solve, after sorting, room[" + r + "] " + baseSolver.W.mIndex2Room.get(r).getClassroom() + " cap = " + baseSolver.I.getRoomCapacity()[r]);
            if(r > maxRoomIndex) maxRoomIndex = r;
        }
        int maxSlots = Constant.daysPerWeek*Constant.slotPerCrew*2 + 2;
        //roomSlotOccupation = new int[maxRoomIndex + 1][maxSlots];
        roomSlotOccupation = new int[baseSolver.I.getRoomCapacity().length + 1][maxSlots];
        for(int r = 0; r < sortedRooms.size(); r++){
            for(int sl = 0; sl < maxSlots; sl++) roomSlotOccupation[r][sl] = 0;
        }

        //Map<Integer, Integer> mClassSegmentId2AssignedRoom = new HashMap<>();
        for(ClassSegment cs: baseSolver.classSegments){
            baseSolver.solutionRoom.put(cs.getId(),-1);
        }
        for(ModelResponseTimeTablingClass cls: classes){
            log.info("solve, consider class " + cls.str() + " qty = " + cls.getQuantityMax());
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls.getId());
            for(ClassSegment cs: CS){
                //if(mClassSegmentId2AssignedRoom.get(cs.getId())!=null){
                if(baseSolver.solutionRoom.get(cs.getId()) != -1){
                    continue;
                }

                if(baseSolver.mClassSegment2MatchedClassSegment.get(cs)!=null){
                    ClassSegment mcs = baseSolver.mClassSegment2MatchedClassSegment.get(cs);
                    //log.info("solve selectRoom4MatchedClassSegment cs = " + cs.str() + " mcs = " + mcs.str());
                    int r = selectRoom4MatchedClassSegment(cs,mcs,sortedRooms);
                    // log.info("solve selectRoom4MatchedClassSegment cs = " + cs.str() + " mcs = " + mcs.str() + " got r = " + r + " cap = " + (r != -1 ? baseSolver.I.getRoomCapacity()[r] : -1));
                    if(r != -1) {
                        assignRoom(cs, r);
                        assignRoom(mcs, r);
                        log.info("solve, assign matched class segments cs " + cs.str() + ", mcs " + mcs.str() + " room " + baseSolver.I.getRoomCapacity()[r]);
                    }else{
                        log.info("solve, assign matched class segments cs " + cs.str() + ", mcs " + mcs.str() + " cannot find a room");
                    }
                }else{
                    //log.info("solve selectRoomClassSegment cs = " + cs.str());
                    int r = selectRoomClassSegment(cs,sortedRooms);
                    //log.info("solve selectRoomClassSegment cs = " + cs.str() + " r = " + r + " cap = " + (r != -1 ? baseSolver.I.getRoomCapacity()[r] : -1));
                    if(r != -1) {
                        assignRoom(cs, r);
                        log.info("solve, assign a class segments cs " + cs.str()  + " room " + baseSolver.I.getRoomCapacity()[r]);
                    }else{
                        log.info("solve, assign a class segments cs " + cs.str()  + " cannot find a room ");

                    }
                }
            }
            log.info("solve finished class cls " + cls.str() + "-------------------");
        }
        return true;
    }

    public ClassSegment findMaxGapRoomClassSegment(){
        ClassSegment selCS = null; int maxGap = -1;
        for(ClassSegment cs: baseSolver.classSegments){
            if(baseSolver.isScheduledClassSegment(cs) && baseSolver.isScheduledRoomClassSegment(cs)){
                int sl = baseSolver.solutionSlot.get(cs.getId());
                int r = baseSolver.solutionRoom.get(cs.getId());
                int gap = baseSolver.I.getRoomCapacity()[r] - cs.nbStudents;
                if(gap > maxGap){ maxGap = gap; selCS = cs; }
            }
        }
        return selCS;
    }

    public int findBestFitRoom(ClassSegment cs){
        log.info("findBestFitRoom for cs " + cs.str() +" starts....");
        int sl = baseSolver.solutionSlot.get(cs.getId());
        int selRoom = -1; int minGap = 100000000;
        for(int r = 0; r < baseSolver.I.getRoomCapacity().length; r++){
            boolean ok = true;
            for(int s = 0; s < cs.getDuration(); s++){
                if(roomSlotOccupation[r][sl+s] > 0){
                    ok = false; break;
                }
            }
            if(ok){
                int gap = baseSolver.I.getRoomCapacity()[r] - (int)(cs.nbStudents * 1.1);
                if(gap >= 0 && gap < minGap){
                    minGap = gap; selRoom = r;
                }
            }
        }
        return selRoom;
    }

    public void printGapRooms(){
        int maxGap = -10000000;
        for(ClassSegment cs: baseSolver.classSegments){
            if(baseSolver.isScheduledRoomClassSegment(cs)){
                int r = baseSolver.solutionRoom.get(cs.getId());
                int gap = baseSolver.I.getRoomCapacity()[r] - cs.nbStudents;
                if(gap > maxGap) maxGap = gap;
                log.info("ClassSegment " + cs.str() + " assigned to rppm " + baseSolver.W.mIndex2Room.get(r).getClassroom() + " gap = " + gap + " maxGap = " + maxGap);
            }
        }
    }
    public int refineRooms(){
        log.info("refineRooms start....");
        int cnt = 0;
        for(int i = 1; i <= 1000; i++){
            ClassSegment cs = findMaxGapRoomClassSegment();
            if(cs != null) {
                int sl = baseSolver.solutionSlot.get(cs.getId());
                int r = baseSolver.solutionRoom.get(cs.getId());
                int gap = baseSolver.I.getRoomCapacity()[r] - cs.nbStudents;
                log.info("refineRooms, discover max-gap class " + cs.str() + " gap = " + gap);
                if(gap <= 30){
                    log.info("refineRooms, gap = " + gap + " BREAK"); break;
                }
                int selRoom = findBestFitRoom(cs);
                if(selRoom < 0){
                    log.info("refineRooms, findBestFitRoom return -1 -> BREAK");
                    break;
                }
                int newGap = baseSolver.I.getRoomCapacity()[selRoom] - cs.nbStudents;
                log.info("refineRooms, found new room with newGap = " + newGap);
                if(newGap > 30){
                    log.info("refineRooms, new gap = " + newGap + " BREAK"); break;
                }
                log.info("refineRooms, iter " + i + " FOUND replacement for class segment " + cs.str() + " room " + baseSolver.W.mIndex2Room.get(selRoom).getClassroom() + " new Gap = " + newGap);
                unAssignRoom(cs,r);
                log.info("refineRooms, iter " + i + " FOUND replacement for class segment " + cs.str() + " room " + baseSolver.W.mIndex2Room.get(selRoom).getClassroom() + " new Gap = " + newGap + " UnAssign OK");

                assignRoom(cs,selRoom);

                log.info("refineRooms, iter " + i + " FOUND replacement for class segment " + cs.str() + " room " + baseSolver.W.mIndex2Room.get(selRoom).getClassroom() + " new Gap = " + newGap + " ReAssign OK");

                cnt++;
            }
        }
        return cnt;
    }
}
@Log4j2
public class SummerSemesterSolverVersion3 implements Solver {
    MapDataScheduleTimeSlotRoom I;
    MapDataScheduleTimeSlotRoomWrapper W;
    Map<Integer, Integer> solutionSlot;
    //int[] solutionRoom; // solutionRoom[i] is the room assigned to class-segment i
    Map<Integer, Integer> solutionRoom;
    //HashSet<Integer>[] conflictClassSegment;// conflictClassSegment[i] is the list of class-segment conflict with class segment i
    Map<Integer, Set<Integer>> conflictClassSegment;
    //HashSet<String>[] relatedCourseGroups;// relatedCourseGroups[i] is the set of related course-group of class-segment i
    Map<Integer, Set<String>> relatedCourseGroups;
    //int[] ins; // ins[i]: number of class having the same course with class segment i
    //ClassSegment[] classSegments = null;
    List<ClassSegment> classSegments = null;
    List<ModelResponseTimeTablingClass> classes;
    Map<Long, ModelResponseTimeTablingClass> mClassId2Class;
    Map<String, List<ModelResponseTimeTablingClass>> mCourse2Classes = new HashMap<>();
    Map<String, Set<Long>> mCourse2ClassId = new HashMap<>();
    Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
    Map<Long, List<Long>> mClassId2ChildrenIds = new HashMap<>();
    Map<Long, Long> mClassId2MatchedClassId = new HashMap<>();
    Map<ClassSegment, ClassSegment> mClassSegment2MatchedClassSegment = new HashMap<>();

    Map<Integer, List<ClassSegment>> mRoom2AssignedClassSegments = new HashMap<>();

    ClassBasedRoomAssignmentSolverVersion3 roomSolver;
    int[][] occupation; // occupation[day][s] is number of classes schedule at slot s

    int[][] day1 = {
            {2},{3},{4},{5},{6}
    };
    int[][] day2 = {
            {2,4},{3,5},{4,6},{2,5},{3,6}
    };
    int[][] day3 = {
            {2,4,6},{2,3,5},{3,4,6},{2,5,6},{2,4,5},{3,5,6}
    };
    List<int[][]> optionDays;

    // output data structures
    List<Integer> unScheduledClassSegment;
    boolean foundSolution;
    int timeLimit;
    public SummerSemesterSolverVersion3(MapDataScheduleTimeSlotRoomWrapper W){
        this.I = W.data; this.W = W;
        optionDays = new ArrayList<>();
        optionDays.add(day1); optionDays.add(day2); optionDays.add(day3);
        roomSolver = new ClassBasedRoomAssignmentSolverVersion3(this);

        mClassId2Class = new HashMap<>();
        for(ModelResponseTimeTablingClass cls: W.classes){
            mClassId2Class.put(cls.getId(),cls);
        }
        classSegments = I.getClassSegments();
        mCourse2Classes = new HashMap<>();
        mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: classSegments){
            Long classId = cs.getClassId();
            if(mClassId2ClassSegments.get(classId)==null) mClassId2ClassSegments.put(classId,new ArrayList<>());
            mClassId2ClassSegments.get(classId).add(cs);
            //ModelResponseTimeTablingClass cls = mClassId2Class.get(classId);
            //String course = cls.getCourse();
            //if(mCourse2Classes.get(course)==null) mCourse2Classes.put(course,new ArrayList<>());
            //mCourse2Classes.get(course).add(cls);
        }
        classes = new ArrayList<>();
        for(Long id: mClassId2ClassSegments.keySet()){
            ModelResponseTimeTablingClass cls = mClassId2Class.get(id);
            classes.add(cls);
            mClassId2ChildrenIds.put(id, new ArrayList<>());
        }
        for(ModelResponseTimeTablingClass cls: classes){
            Long pid = cls.getParentClassId();
            if(pid != null){
                mClassId2ChildrenIds.get(pid).add(cls.getId());
            }
        }
        for(ModelResponseTimeTablingClass cls: classes){
            String course = cls.getModuleCode();
            if(mCourse2Classes.get(course)==null) mCourse2Classes.put(course, new ArrayList<>());
            mCourse2Classes.get(course).add(cls);
        }
        occupation = new int[Constant.daysPerWeek+2][Constant.slotPerCrew+1];
        for(int d = 0; d <= Constant.daysPerWeek+1; d++)
            for(int s = 0; s <= Constant.slotPerCrew; s++)
                occupation[d][s] =  0;

        mRoom2AssignedClassSegments = new HashMap<>();
        for(int r = 0; r < I.getRoomCapacity().length; r++) mRoom2AssignedClassSegments.put(r,new ArrayList<>());
    }

    public int[] sortCapacityRoom(Set<Integer> rooms, boolean desc){
        int[] res = new int[rooms.size()];
        int idx = -1;
        for(int r: rooms){ idx++; res[idx] = r; }
        for(int i = 0; i < res.length; i++){
            for(int j = i+1; j < res.length; j++){
                boolean swap = false;

                if(desc){// decreasing order
                    if(I.getRoomCapacity()[res[i]] < I.getRoomCapacity()[res[j]]){
                        swap = true;
                    }
                }else{
                    if(I.getRoomCapacity()[res[i]] > I.getRoomCapacity()[res[j]]){
                        swap = true;
                    }
                }
                if(swap){
                    int tmp = res[i]; res[i] = res[j]; res[j] = tmp;
                }
            }
        }
        return res;
    }
    public void printRoomsUsed(){
        Set<Classroom> R = new HashSet<>();
        for(int id: solutionRoom.keySet()){
            int r_idx = solutionRoom.get(id);
            Classroom room = W.mIndex2Room.get(r_idx);
            log.info("printRoomsUsed, class-segment " + id + " is assigned to room[" + r_idx + "] = " + room.getClassroom() + " has cap " + room.getQuantityMax());
            R.add(room);
        }
        int cnt = 0;
        for(Classroom r: R) {
            log.info("printRoomsUsed, room " + r.getClassroom() + " cap " + r.getQuantityMax());
            if (r.getQuantityMax() >= 150) {
                cnt++;
                log.info("printRoomsUsed, room " + r.getClassroom() + " cap " + r.getQuantityMax() + " >= 150 cnt = " + cnt);

            }
        }
    }
    public boolean  assignTimeSlot(ClassSegment cs, int timeSlot){
        solutionSlot.put(cs.getId(),timeSlot);
        DaySessionSlot dss = new DaySessionSlot(timeSlot);
        for(int s = 0; s < cs.getDuration(); s++)
            occupation[dss.day][dss.slot + s] += 1;
        return true;

    }
    public boolean  assignTimeSlotRoom(ClassSegment cs, int timeSlot, int room){
        //log.info("assignTimeSlotRoom start (cs.getId() " + cs.getId() + " timeSlot " + timeSlot + " room " + room + ")") ;
        //solutionSlot[csi] = timeSlot;
        //solutionRoom[csi] = room;
        if(solutionRoom.get(cs.getId())!=null && solutionRoom.get(cs.getId()) != -1){
            log.info("assignTimeSlotRoom, BUG??? class-segment id = " + cs.getId() + " classId = " + cs.getClassId() + " course " + cs.getCourseCode() + " was assign to room " + solutionRoom.get(cs.getId()));
            return false;
        }
        solutionSlot.put(cs.getId(),timeSlot);
        solutionRoom.put(cs.getId(),room);

        mRoom2AssignedClassSegments.get(room).add(cs);
        // update room occupation
        //ClassSegment cs = classSegments.get(csi);
        String os = "";
        for(int r: I.getRoomOccupations()[room]) os = os + r + ",";
        //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room + " occupied by slots " + os);
        //log.info("assignTimeSlotRoom[" + cs.getId() + "], classId " + cs.getClassId() + " course " + cs.getCourseCode() + " assigned to room used index = " + room + " name = " + W.mIndex2Room.get(room).getClassroom() + " capacity " + W.mIndex2Room.get(room).getQuantityMax());
        for(int s = 0; s <= cs.getDuration()-1; s++){
            int sl = timeSlot + s;
            //roomSolver.roomSlotOccupation[sl][room] = 1;
            I.getRoomOccupations()[room].add(sl);
            os = os + sl + ",";
            //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room
            // + " roomOccupation[" + room + "].add(" + sl + ") -> " + os);
        }
        //printRoomsUsed();
        foundSolution = true;
        return true;
    }
    public boolean  unAssignTimeSlotRoom(ClassSegment cs, int timeSlot, int room){
        //log.info("assignTimeSlotRoom start (cs.getId() " + cs.getId() + " timeSlot " + timeSlot + " room " + room + ")") ;
        //solutionSlot[csi] = timeSlot;
        //solutionRoom[csi] = room;
        if(solutionRoom.get(cs.getId())!=null && solutionRoom.get(cs.getId()) == -1){
            log.info("assignTimeSlotRoom, BUG??? class-segment id = " + cs.getId() + " classId = " + cs.getClassId() + " course " + cs.getCourseCode() + " was NOT assign to room ");
            return false;
        }
        solutionSlot.put(cs.getId(),timeSlot);
        solutionRoom.put(cs.getId(),-1);

        mRoom2AssignedClassSegments.get(room).remove(cs);
        // update room occupation
        //ClassSegment cs = classSegments.get(csi);
        //String os = "";
        //for(int r: I.getRoomOccupations()[room]) os = os + r + ",";
        //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room + " occupied by slots " + os);
        //log.info("assignTimeSlotRoom[" + cs.getId() + "], classId " + cs.getClassId() + " course " + cs.getCourseCode() + " assigned to room used index = " + room + " name = " + W.mIndex2Room.get(room).getClassroom() + " capacity " + W.mIndex2Room.get(room).getQuantityMax());
        for(int s = 0; s <= cs.getDuration()-1; s++){
            int sl = timeSlot + s;

            //I.getRoomOccupations()[room].remove(sl);

            //os = os + sl + ",";
            //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room
            // + " roomOccupation[" + room + "].add(" + sl + ") -> " + os);
        }
        //printRoomsUsed();
        //foundSolution = true;
        return true;
    }
    public boolean isScheduled(Long classId){
        List<ClassSegment> CS = mClassId2ClassSegments.get(classId);
        if(CS != null && CS.size()> 0){
            ClassSegment cs = CS.get(0);
            if(solutionSlot.get(cs.getId())!=null && solutionSlot.get(cs.getId()) >= 0) return true;
        }
        return false;
    }
    public MatchScore matchScore(Long classId1, Long classId2, int nbSlotPerSession){
        List<ClassSegment> CS1 = new ArrayList<>();
        List<ClassSegment> CS2 = new ArrayList<>();
        if(mClassId2ClassSegments.get(classId1)!=null)
            for(ClassSegment cs: mClassId2ClassSegments.get(classId1)) CS1.add(cs);
        if(mClassId2ClassSegments.get(classId2)!=null)
            for(ClassSegment cs: mClassId2ClassSegments.get(classId2)) CS2.add(cs);
        int score = 0; List<Integer> scores = new ArrayList<>();
        Map<ClassSegment, ClassSegment> m = new HashMap<>();
        while(CS1.size() > 0 && CS2.size() > 0){
            ClassSegment sel_cs1 = null; ClassSegment sel_cs2 = null;
            int d = 100000;
            for(ClassSegment cs1: CS1){
                for(ClassSegment cs2 : CS2)if(nbSlotPerSession >= cs1.getDuration() + cs2.getDuration()){
                    if(d > nbSlotPerSession - cs1.getDuration() - cs2.getDuration()){
                        d = nbSlotPerSession - cs1.getDuration() - cs2.getDuration();
                        sel_cs1= cs1; sel_cs2 = cs2;
                        if(d == 0) break;
                    }
                }
                if(d == 0) break;
            }
            if(sel_cs1 != null && sel_cs2 != null){
                CS1.remove(sel_cs1); CS2.remove(sel_cs2);
                scores.add(d);
                m.put(sel_cs1,sel_cs2);
                m.put(sel_cs2,sel_cs1);
            }else{
                break;
            }
        }
        score = 1;
        if(scores.size() == 0) return new MatchScore(-1,null);
        for(int s: scores) score = score * (nbSlotPerSession-s);
        MatchScore ms = new MatchScore(score,m);
        return ms;
    }
    public int findConsecutiveSlot(ClassSegment cs, ClassSegment matchClassSegment){
        if(solutionSlot.get(matchClassSegment.getId())==null){
            log.info("findConsecutiveSlot, matchClassSegment " + matchClassSegment.str() + " not scheduled -> BUG???"); return -1;
        }
        int sl = solutionSlot.get(matchClassSegment.getId());
        DaySessionSlot dss = new DaySessionSlot(sl);
        log.info("findConsecutiveSlot, matchClassSegment " + matchClassSegment.str() + " schedule = " + dss.toString());
        int slot = dss.slot - cs.getDuration();
        if(slot >= 1)
            return new DaySessionSlot(dss.day,dss.session,slot).hash();
        slot = dss.slot + matchClassSegment.getDuration();
        if(slot + cs.getDuration() - 1 <= Constant.slotPerCrew)
            return new DaySessionSlot(dss.day,dss.session,slot).hash();
        log.info("findConsecutiveSlot, before and after not legal -> BUG???"); return -1;
    }
    private int findSeparateDay(List<Integer> D){
        int d = -1;
        // to be considered
        return d;
    }
    private int findSeparateSlot(ClassSegment cs, int session){
        int res = -1;
        Long id = cs.getClassId();
        boolean[] fill = new boolean[Constant.daysPerWeek];
        Arrays.fill(fill,false);
        List<Integer> days_occupied = new ArrayList<>();
        for(ClassSegment csi: mClassId2ClassSegments.get(id)){
            if(solutionSlot.get(csi.getId())!=null){
                int sl = solutionSlot.get(csi.getId());
                DaySessionSlot dss = new DaySessionSlot(sl);
                fill[dss.day-2] = true;
                days_occupied.add(dss.day);
            }
        }
        int minOcc = 100000000;
        int sel_slot = -1;
        for(int sl: cs.getDomainTimeSlots()){
            DaySessionSlot dss = new DaySessionSlot(sl);
            if(fill[dss.day-2]==true) continue;
            if(dss.slot == 1 || dss.slot == Constant.slotPerCrew - cs.getDuration()+1) {
                if (occupation[dss.day][dss.slot] < minOcc) {
                    minOcc = occupation[dss.day][dss.slot];
                    sel_slot = sl;
                }
            }
        }

        return sel_slot;
    }
    private boolean assignSlot4Class(Long id, MatchScore ms, int session){
        ModelResponseTimeTablingClass cls = mClassId2Class.get(id);
        List<ClassSegment> CS = new ArrayList<>();
        for(ClassSegment cs: mClassId2ClassSegments.get(id)) CS.add(cs);
        while(CS.size() > 0) {
            // consider first class-segment matched
            boolean ok = false;
            for (ClassSegment cs : CS) {
                if (ms.m.get(cs) != null) {
                    ClassSegment m_cs = ms.m.get(cs);
                    log.info("assignSlot4Class, findConsecutiveSlot for class-segment " + cs.str() + " matched class segment = " + m_cs.str());
                    int startSlot = findConsecutiveSlot(cs, m_cs);
                    DaySessionSlot dss = new DaySessionSlot(startSlot);
                    log.info("assignSlot4Class, findConsecutiveSlot for class-segment " + cs.str() + " of class " + cls.getClassCode() + " RETURN startSlot = " + startSlot + " dss.slot =  " + dss.slot);
                    assignTimeSlot(cs,startSlot);
                    CS.remove(cs); ok = true; break;
                }
            }
            if(!ok) break;
        }
        // SECOND consider non-matched class segments
        for(ClassSegment cs: CS){
            int slot = findSeparateSlot(cs,session);
            if(slot != -1){
                DaySessionSlot dss = new DaySessionSlot(slot);
                log.info("assignSlot4Class, findSeparateSlot for class-segment " + cs.str() + " of class " + cls.getClassCode() + " RETURN slot = " + slot + " dss.slot =  " + dss.slot + " dayPerWeek = " + Constant.daysPerWeek + " slotPerCrew = " + Constant.slotPerCrew);
                assignTimeSlot(cs,slot);
            }else{
                log.info("assignSlot4Class, CANNOT find any slot for class segment " + cs.str() + "?????");
            }
        }
        return true;
    }
    private boolean findGreedyTimeSlotForClass(Long id, int session, Map<String, Set<Long>> mCourse2ClassId){
        if(isScheduled(id)) return true;
        List<ClassSegment> CS = mClassId2ClassSegments.get(id);
        ModelResponseTimeTablingClass cls = mClassId2Class.get(id);
        String courseCode = cls.getModuleCode();
        log.info("findGreedyTimeSlotForClass, consider course " + courseCode + " class cls = " + cls.str());
        // FIRST try to find another class of the same course that can be matched
        MatchScore maxScore = null;
        Long sel_id = null;
        //for(ModelResponseTimeTablingClass clsi: mCourse2Classes.get(courseCode)){
        for(Long idi: mCourse2ClassId.get(courseCode)){
            ModelResponseTimeTablingClass clsi = mClassId2Class.get(idi);
            if(!isScheduled(clsi.getId())) continue;
            if(mClassId2MatchedClassId.get(clsi.getId())!=null) continue;
            List<ClassSegment> CSi = mClassId2ClassSegments.get(clsi.getId());
            MatchScore ms = matchScore(id,clsi.getId(), Constant.slotPerCrew);
            log.info("findGreedyTimeSlotForClass class " + cls.str() + " matchScore = " + ms.toString() + " with " + clsi.str());
            if(ms.score >= 0) {
                if (maxScore == null || ms.score > maxScore.score) {
                    maxScore = ms;
                    sel_id = clsi.getId();
                }
            }
        }

        if(sel_id != null){
            assignSlot4Class(id,maxScore,session);
        }else{// SECOND try to find matching from other classes of the same group
            for(String courseCode1 : mCourse2ClassId.keySet()){
                if(courseCode1.equals(courseCode)) continue;
                for(ModelResponseTimeTablingClass clsi: mCourse2Classes.get(courseCode1)){
                    if(!isScheduled(clsi.getId())) continue;
                    if(mClassId2MatchedClassId.get(clsi.getId())!=null) continue;
                    List<ClassSegment> CSi = mClassId2ClassSegments.get(clsi.getId());
                    MatchScore ms = matchScore(id,clsi.getId(), Constant.slotPerCrew);
                    if(ms.score >= 0) {
                        if (maxScore == null || ms.score > maxScore.score) {
                            maxScore = ms;
                            sel_id = clsi.getId();
                        }
                    }
                }
            }
            if(sel_id != null) {
                assignSlot4Class(id, maxScore, session);
            }else{
                for(ClassSegment cs: mClassId2ClassSegments.get(id)){
                    int slot = findSeparateSlot(cs,session);
                    if(slot != -1){
                        DaySessionSlot dss = new DaySessionSlot(slot);
                        log.info("findGreedyTimeSlotForClass, NOT MATCHED -> findSeparateSlot for class-segment " + cs.getId() + " of class " + cls.getClassCode() + " RETURN slot = " + slot + " dss.slot = " + dss.slot);
                        assignTimeSlot(cs,slot);
                    }else{
                        log.info("assignSlot4Class, CANNOT find any slot for class segment " + cs.str() + "?????");
                        return false;
                    }
                }
            }
        }
        return true;
    }
    private boolean scheduleLargeClass(List<ClassSegment> L, int session, boolean assignRoom, int threshold){
        List<ClassSegment> largeClassSegments = new ArrayList<>();
        for(ClassSegment cs: L){
            Long classId = cs.getClassId();
            ModelResponseTimeTablingClass cls = mClassId2Class.get(classId);
            if(cls.getQuantityMax() >= threshold){
                largeClassSegments.add(cs);
            }
        }
        for(ClassSegment cs: largeClassSegments){
            L.remove(cs);
        }
        log.info("scheduleLargeClass, start solve large classes sz = " + largeClassSegments.size());
        boolean ok = scheduleGroup(largeClassSegments,session,assignRoom);
        log.info("scheduleLargeClass, finish solve large, remain classes sz = " + largeClassSegments.size());
        for(ClassSegment cs: largeClassSegments){
            //L.remove(cs);
            L.add(cs);
        }
        return true;
    }
    private boolean scheduleGroup(List<ClassSegment> L, int session, boolean assignRoom) {
        GroupSchedulerSummerSemesterVersion3 GSSS = new GroupSchedulerSummerSemesterVersion3(this,L,session);
        boolean ok = GSSS.solve();
        Set<ClassSegment> R = new HashSet<>();
        for(ClassSegment cs: L){
            if(isScheduledClassSegment(cs)){
                R.add(cs);
                log.info("scheduleGroup, cs " + cs.str() + " is scheduled, remove set R.sz = " + R.size());
            }
        }
        for(ClassSegment cs: R) L.remove(cs);
        return ok;
        /*
        //Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        Map<String, Set<Long>> mCourse2ClassId = new HashMap<>();
        for (ClassSegment cs : L) {
            //if (mClassId2ClassSegments.get(cs.getClassId()) == null)
            //    mClassId2ClassSegments.put(cs.getClassId(), new ArrayList<>());
            //mClassId2ClassSegments.get(cs.getClassId()).add(cs);

            String courseCode = cs.getCourseCode();
            if(mCourse2ClassId.get(courseCode)==null)
                mCourse2ClassId.put(courseCode,new HashSet<>());
            mCourse2ClassId.get(courseCode).add(cs.getClassId());
        }
        for(String courseCode: mCourse2ClassId.keySet()) {
            String ids = "";
            for(Long id: mCourse2ClassId.get(courseCode)) ids = ids + id + ",";
            log.info("scheduleGroup, session = " + session + " consider course " + courseCode + " nbClasses = " + mCourse2ClassId.size() + ": " + ids);
            List<Long[]> pairs = matchClassInACourse(courseCode,mCourse2ClassId);

            for (Long id : mCourse2ClassId.get(courseCode)) {
                //List<ClassSegment> CS = mClassId2ClassSegments.get(id);

                boolean ok = findGreedyTimeSlotForClass(id,session,mCourse2ClassId);
                if(!ok){
                    log.info("scheduleGroup -> CANNOT schedule class " + id + "????");
                }
            }
        }


        return true;

         */
    }
    private int detectSession(List<ClassSegment> L){
        // return 0 if cs is morning or afternoon
        // 1 if cs is morning
        // 2 if cs is afternoon
        Set<Integer> S = new HashSet<>();
        for(ClassSegment cs: L) {
            for (int s : cs.getDomainTimeSlots()) {
                DaySessionSlot dss = new DaySessionSlot(s);
                S.add(dss.session);
                //log.info("detectSession, class segment " + cs.str() + " -> dds = " + dss.toString() + " add session " + dss.session);
            }
        }
        if(S.size() > 1) return 0;// both morning and afternoon
        else{
            for(int s: S){
                if(s == 0) return 1; else return 2;
            }
        }
        return 0;
    }
    private String detectePatternChildren(List<Long> childrenIds, Map<Long, List<ClassSegment>> mClassId2ClassSegments){
        if(childrenIds == null || childrenIds.size() == 0){
            log.info("detectePatternChildren, childrenIds is empty. BUG????");
        }
        List<String> patternChild = new ArrayList<>();
        for(Long cid: childrenIds){
            List<ClassSegment> CLSi = mClassId2ClassSegments.get(cid);
            String patterni = detectPattern(CLSi);
            patternChild.add(patterni);
        }
        Collections.sort(patternChild);
        String res = "";
        for(int i = 0; i < patternChild.size(); i++){
            String s = patternChild.get(i);
            res = res + s;
            if(i < patternChild.size()-1) res = res + "-";
        }
        return res;
    }
    private String detectPattern(List<ClassSegment> L){
        // return sequence of duration sorted increasing order sepparated by comma
        // example: 2,3 or 3,3 or 2,3,4 or 3,3,3 or 4,4,4
        if(L == null || L.size() == 0){
            log.info("detectPattern, BUG L is NULL or empty");
        }
        String s = "";
        Collections.sort(L, new Comparator<ClassSegment>() {
            @Override
            public int compare(ClassSegment o1, ClassSegment o2) {
                return o1.getDuration() - o2.getDuration();
            }
        });
        for(int i = 0; i < L.size(); i++){
            s = s + L.get(i).getDuration();
            if(i < L.size()-1) s = s + ",";
        }
        return s;
    }

    @Override
    public void solve() {
        log.info("solve...START, nbClassSegments = " + I.getClassSegments().size());
        solutionSlot = new HashMap<>();
        solutionRoom = new HashMap<>();
        Map<Long, List<ClassSegment>> mClassId2ClassSegments= new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            //log.info("solve... cs = " + cs.getClassId() + " course " + cs.getCourseCode() + " type = " + cs.getType() + " duration " + cs.getDuration() + " domain-rooms = " + cs.getDomainRooms().size());
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        List<Long> LTBT = new ArrayList<>();
        Map<Long, String> mClassId2Type = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            if(cs.getType()==0) mClassId2Type.put(cs.getClassId(),"LT");
            else if(cs.getType()==1) mClassId2Type.put(cs.getClassId(),"BT");
            else mClassId2Type.put(cs.getClassId(),"LT+BT");
        }
        for(ClassSegment cs: I.getClassSegments()){
            if(mClassId2Type.get(cs.getClassId()).equals("LT+BT")) LTBT.add(cs.getClassId());
        }
        Set<Long> morningClassIds = new HashSet<>();
        Set<Long> afternoonClassIds = new HashSet();
        log.info("solve, classIds = " + mClassId2ClassSegments.keySet().size());
        for(Long id: mClassId2ClassSegments.keySet()){
            List<ClassSegment> CLS = mClassId2ClassSegments.get(id);
            if(CLS != null) {
                int ses = detectSession(CLS);
                //log.info("solver, classId " + id + " ses = " + ses);
                if(ses == 1) morningClassIds.add(id);
                else if(ses == 2) afternoonClassIds.add(id);
                else{
                    String info = "";
                    for(ClassSegment cs: CLS) info = info + "["  + cs.getClassId() + " course " + cs.getCourseCode()  + " duration " + cs.getDuration() + "]";

                    log.info("solve, unknown session " + ses + " for class " + id + " detail " + info);

                }
            }
        }
        //String[] CH = {"CH1012","CH1015","CH1018","CH3220","CH3323","CH3224","CH3316","CH3330","CH4780",
        //"CH3400","CH3412","CH3420","CH3051","CH3061","CH2020","CH3456","CH4486","CH3452","CH3454",
        //"CH3474","CH2021"};

        Set<String> sCH  = new HashSet<>(Arrays.asList(new String[]{"CH1012","CH1015","CH1018","CH3220","CH3323","CH3224","CH3316","CH3330","CH4780",
                "CH3400","CH3412","CH3420","CH3051","CH3061","CH2020","CH3456","CH4486","CH3452","CH3454",
                "CH3474","CH2021","CH3120","CH1017"}));

        Set<String> sETEE  = new HashSet<>(Arrays.asList(new String[]{"ET2012","ET2022","ET2031","ET2060","ET2100","ET3262",
                "EE2012","EE2022","EE2021","EE2023", "EE2000","EE2031","EE2130"}));


        Set<String> sME  = new HashSet<>(Arrays.asList(new String[]{"ME2011","ME2201","ME2020","ME2011","ME2015","ME2040","ME2112","ME2211","ME2101",
                "ME2202","ME3190","ME3190","ME2102","ME2030","ME2203","ME4159","ME4181","ME2021","ME3123",
                "ME3124","HE2012","HE2020","TE3602","TE2020","IT2030","ME2140Q","ME3230","ME3212"}));

        Set<String> sEM  = new HashSet<>(Arrays.asList(new String[]{
                "EM1010","EM1170","EM1180","EM1100","EM3105","EM3004","EM3222"
        }));

        Set<String> sED  = new HashSet<>(Arrays.asList(new String[]{
                "ED3280","ED3220"
        }));

        Set<String> sSSH  = new HashSet<>(Arrays.asList(new String[]{
                "SSH1151","SSH1111","SSH1121","SSH1131","SSH1141","SSH1131Q","MIL1220","MIL1210"
        }));

        Set<String> sMI  = new HashSet<>(Arrays.asList(new String[]{
                "MI1111","MI1121","MI1131","MI1141","MI1112","MI1142","MI1113","MI1036","MI1016","MI2020",
                "MI2021","MI3180","MI1026","MI1132","MI1143","MI2010","MI2110","MI1122","MI1133",
                "MI1144","MI1114","MI1124","MI1046","MI1134","MI1110Q","MI1140Q"
        }));


        log.info("solver, morningids.sz = " + morningClassIds.size() + " afternoonids.sz = " + afternoonClassIds.size());
        List<ClassSegment>[] CH = new ArrayList[2];
        List<ClassSegment>[] ETEE = new ArrayList[2];
        List<ClassSegment>[] ME = new ArrayList[2];
        List<ClassSegment>[] EM = new ArrayList[2];
        List<ClassSegment>[] ED = new ArrayList[2];
        List<ClassSegment>[] MI = new ArrayList[2];
        List<ClassSegment>[] SSH = new ArrayList[2];
        for(int session = 0; session <= 1; session++) {
            CH[session] = new ArrayList<>();
            ETEE[session] = new ArrayList<>();
            ME[session] = new ArrayList<>();
            EM[session] = new ArrayList<>();
            ED[session] = new ArrayList<>();
            MI[session] = new ArrayList<>();
            SSH[session] = new ArrayList<>();
        }


        for(int session = 0; session <= 1; session++) {
            Set<Long> ids = morningClassIds;
            if(session == 1) ids = afternoonClassIds;

            for (Long id : ids) {
                for(ClassSegment cs: mClassId2ClassSegments.get(id)){
                    //log.info("solver, id = " + id + " class-segment " + cs.toString());
                    if(sCH.contains(cs.getCourseCode())){
                        CH[session].add(cs);
                    }else if(sETEE.contains(cs.getCourseCode())){
                        ETEE[session].add(cs);
                    }else if(sEM.contains(cs.getCourseCode())){
                        EM[session].add(cs);
                    }else if(sED.contains(cs.getCourseCode())){
                        ED[session].add(cs);
                    }else if(sME.contains(cs.getCourseCode())){
                        ME[session].add(cs);
                    }else if(sMI.contains(cs.getCourseCode())){
                        MI[session].add(cs);
                        //log.info("solver, id = " + id + " class-segment " + cs.toString() + " MI.add(" + cs.getCourseCode() + ") -> sz = " + MI[session].size());
                    }else if(sSSH.contains(cs.getCourseCode())){
                        SSH[session].add(cs);
                    }
                }
            }
            log.info("Session " + session + ": nb class-segments = " + I.getClassSegments().size());
            log.info("Session " + session + ": A" +
                    "H = " + CH[session].size() + " ME = " + ME[session].size() + " ETEE = " + ETEE[session].size()
                    + " EM = " + EM[session].size() + " ED = " + ED[session].size() + " MI = " + MI[session].size() + " SSH = " + SSH[session].size() +
                    " -> SUM = " + (CH[session].size() + ME[session].size() + ETEE[session].size() + EM[session].size() + ED[session].size() + MI[session].size() + SSH[session].size()));

            log.info("solve large classes CH size = " + CH[session].size());
            scheduleLargeClass(CH[session],session,false,I.getParams().thresholdLargeClass);
            log.info("solve large classes ME size = " + ME[session].size());
            scheduleLargeClass(ME[session],session,false,I.getParams().thresholdLargeClass);
            log.info("solve large classes ETEE size = " + ETEE[session].size());
            scheduleLargeClass(ETEE[session],session,false,I.getParams().thresholdLargeClass);
            log.info("solve large classes EM size = " + EM[session].size());
            scheduleLargeClass(EM[session],session,false,I.getParams().thresholdLargeClass);
            log.info("solve large classes ED size = " + ED[session].size());
            scheduleLargeClass(ED[session],session,false,I.getParams().thresholdLargeClass);
            log.info("solve large classes MI size = " + MI[session].size());
            scheduleLargeClass(MI[session],session,false,I.getParams().thresholdLargeClass);
            log.info("solve large classes SSH size = " + SSH[session].size());
            scheduleLargeClass(SSH[session],session,false,I.getParams().thresholdLargeClass);


            log.info("solve group CH size = " + CH[session].size());
            scheduleGroup(CH[session],session,false);
            log.info("solve group ME size = " + ME[session].size());
            scheduleGroup(ME[session],session,false);
            log.info("solve group ETEE size = " + ETEE[session].size());
            scheduleGroup(ETEE[session],session,false);
            log.info("solve group EM size = " + EM[session].size());
            scheduleGroup(EM[session],session,false);
            log.info("solve group ED size = " + ED[session].size());
            scheduleGroup(ED[session],session,false);

            log.info("solve scheduleGroupLTandBT MI size = " + MI[session].size());
            if(!scheduleGroupLTandChildrenBT(MI[session],session,false)) return;
            log.info("After solve scheduleGroupLTandBT MI remain size = " + MI[session].size());
            log.info("solve scheduleGroupLTandBT SSH size = " + SSH[session].size());
            if(!scheduleGroupLTandChildrenBT(SSH[session],session,false)) return;

            log.info("solve, start call scheduleGroup(MI[session], sz = " + MI[session].size());
            scheduleGroup(MI[session],session,false);
            log.info("solve, start call scheduleGroup(MI[session], FINISHED remain sz = " + MI[session].size());

            scheduleGroup(SSH[session],session,false);

            /*
            log.info("solve group MI size = " + MI[session].size());
            if(!scheduleGroup(MI[session],session,false)) return;
            log.info("After solve group MI remains size = " + MI[session].size());

            log.info("solve group MSSH size = " + SSH[session].size());
            if(!scheduleGroup(SSH[session],session,false)) return;
            */
        }
        //ClassBasedRoomAssignmentSolver roomAssignmentSolver = new ClassBasedRoomAssignmentSolver(this);
        //roomAssignmentSolver.solve();
        roomSolver.solve();

        improve();

        int cnt = roomSolver.refineRooms();
        log.info("solve, after refineRoom, nbImprovements is cnt " + cnt);
        roomSolver.printGapRooms();

        Set<ClassSegment> CS=new HashSet<>();
        for(int s = 0; s <= 1; s++){
            for(ClassSegment cs: CH[s]) CS.add(cs);
            for(ClassSegment cs: ME[s]) CS.add(cs);
            for(ClassSegment cs: ETEE[s]) CS.add(cs);
            for(ClassSegment cs: EM[s]) CS.add(cs);
            for(ClassSegment cs: ED[s]) CS.add(cs);
            for(ClassSegment cs: MI[s]) CS.add(cs);
            for(ClassSegment cs: SSH[s]) CS.add(cs);
        }
        for(ClassSegment cs: I.getClassSegments())
        {
            if(!CS.contains(cs)){
                ModelResponseTimeTablingClass gc = W.mClassSegment2Class.get(cs.getId());
                log.info("solve: class-segment " + cs.getClassId() + " code " + gc.getClassCode() + ", course " + cs.getCourseCode()  +
                        " is NOT partitioned, not in any cluster");
            }
        }

    }
    public boolean isScheduledClassSegment(ClassSegment cs){
        return solutionSlot.get(cs.getId())!=null && solutionSlot.get(cs.getId())!=-1;
    }
    public boolean isScheduledRoomClassSegment(ClassSegment cs){
        return solutionRoom.get(cs.getId())!=null && solutionRoom.get(cs.getId())!=-1;
    }
    private boolean scheduleGroupLTandChildrenBT(List<ClassSegment> L, int session, boolean assignRoom) {

        GroupLTChildrenBTSchedulerSummerSemesterVersion3 GSSS = new GroupLTChildrenBTSchedulerSummerSemesterVersion3(this,L,session);
        boolean ok = GSSS.solve();
        Set<ClassSegment> R = new HashSet<>();
        for(ClassSegment cs: L){
            if(isScheduledClassSegment(cs)){
                log.info("scheduleGroupLTandChildrenBT, cs " + cs.str() + " is scheduled");
                R.add(cs);
            }
        }
        for(ClassSegment cs: R){
            L.remove(cs);
            log.info("scheduleGroupLTandChildrenBT, cs " + cs.str() + " is scheduled -> remove L.sz = " + L.size());
        }
        return ok;
    }
    private boolean overlapTimeTableClasses(Long classId1, Long classId2){
        List<ClassSegment> CS1 = mClassId2ClassSegments.get(classId1);
        List<ClassSegment> CS2 = mClassId2ClassSegments.get(classId2);
        if(CS1 == null || CS2 == null){
            log.info("overlapTimeTableClasses, CS1 or CS2 NULL"); return false;
        }
        for(ClassSegment cs1: CS1){
            for(ClassSegment cs2: CS2){
                if(overlapTimeTableClassSegments(cs1,cs2)) return true;
            }
        }
        return false;
    }
    private boolean overlapTimeTableClassSegments(ClassSegment cs1, ClassSegment cs2){
        if(solutionSlot.get(cs1.getId())==null || solutionSlot.get(cs2.getId())==null)
            return false;
        int s1 = solutionSlot.get(cs1.getId());
        int s2 = solutionSlot.get(cs2.getId());
        if(Util.overLap(s1,cs1.getDuration(),s2,cs2.getDuration())) return true;
        return false;
    }

    private boolean consecutiveSameSessionClassSegments(ClassSegment cs1, ClassSegment cs2){
        if(solutionSlot.get(cs1.getId())==null || solutionSlot.get(cs2.getId())==null)
            return false;
        int s1 = solutionSlot.get(cs1.getId());
        int s2 = solutionSlot.get(cs2.getId());
        DaySessionSlot dss1 = new DaySessionSlot(s1);
        DaySessionSlot dss2 = new DaySessionSlot(s2);
        if(dss1.day == dss2.day && dss1.session == dss2.session){
            if(dss1.slot + cs1.getDuration() == dss2.slot || dss2.slot + cs2.getDuration() == dss1.slot)
                return true;
        }
        return false;
    }
    private int consecutiveSameSession(Long id1, Long id2){
        List<ClassSegment> L1 = new ArrayList<>();
        List<ClassSegment> L2 = new ArrayList<>();
        for(ClassSegment cs1: mClassId2ClassSegments.get(id1))
            L1.add(cs1);
        for(ClassSegment cs2: mClassId2ClassSegments.get(id2))
            L2.add(cs2);
        int cnt = 0;
        while(L1.size() > 0 && L2.size() > 0) {
            //ClassSegment sel_cs1 = null; ClassSegment sel_cs2= null;
            int idx1 = -1; int idx2 = -1;
            for (int i1 = 0; i1 < L1.size(); i1++) {
                for (int i2 = 0; i2 < L2.size(); i2++) {
                    ClassSegment cs1 = L1.get(i1); ClassSegment cs2 = L2.get(i2);
                    if (consecutiveSameSessionClassSegments(cs1, cs2)) {
                        idx1 = i1; idx2 = i2; break;
                    }
                }
                if(idx1 != -1) break;
            }
            if(idx1 != -1 && idx2 != -1) {
                L1.remove(idx1);
                L2.remove(idx2);
                //L1.remove(sel_cs1);
                //L2.remove(sel_cs2);
                cnt++;
                log.info("reAssignRoom, detect consecutive, L1.sz = " + L1.size() + ", L2.sz = " + L2.size() + ", cnt = " + cnt);
            }else{
                break;
            }
        }
        return cnt;
    }

    private void reAssignRoomsAllClassSegmentSameRoom(){
        classes.sort(new Comparator<ModelResponseTimeTablingClass>() {
            @Override
            public int compare(ModelResponseTimeTablingClass o1, ModelResponseTimeTablingClass o2) {
                return o2.getQuantityMax() - o1.getQuantityMax();
            }
        });
        for(int i = 0; i <  classes.size(); i++){
            //ModelResponseTimeTablingClass cls = classes.get(i);
            //log.info("reAssignRooms, after sorting classes, cls[" + i + "/" + classes.size() + "] " + cls.getClassCode() + "," + cls.getModuleCode() + ", qty = " + cls.getQuantityMax());
        }
        Map<Long, List<Long>> mClassId2ConflictClassIds = new HashMap<>();
        for(Long id: mClassId2ClassSegments.keySet()){
            mClassId2ConflictClassIds.put(id,new ArrayList<>());
        }
        for(Long id: mClassId2ClassSegments.keySet()){
            ModelResponseTimeTablingClass cls = mClassId2Class.get(id);
            for(Long id1: mClassId2ClassSegments.keySet())if(id < id1){
                if(overlapTimeTableClasses(id,id1)){
                    mClassId2ConflictClassIds.get(id).add(id1);
                    mClassId2ConflictClassIds.get(id1).add(id);
                    ModelResponseTimeTablingClass cls1 = mClassId2Class.get(id1);
                    if(cls.getClassCode().equals("92051")|| cls.getClassCode().equals("91651"))
                        log.info("reAssignRoom, CONFLICT of class " + cls.getClassCode() + " AND " + cls1.getClassCode());
                }
            }
        }
        //log.info("reAssignRoom, prepare room list");
        Map<Long, List<Integer>> mClassId2Rooms = new HashMap<>();
        for(Long id: mClassId2ClassSegments.keySet()){
            mClassId2Rooms.put(id, new ArrayList<>());
            List<ClassSegment> CS = mClassId2ClassSegments.get(id);
            if(CS == null){
                log.info("reAssignRoom, CS of class " + id + " is NULL");
            }
            if(CS.size() > 0){
                ClassSegment cs = CS.get(0);
                //if(cs.getDomainRooms()==null) log.info("reAssignRoom, cs.getDomainRoom NULL");
                //if(I.getRoomCapacity() == null) log.info("reAssignRoom, roomCapacity NULL");
                for(int r: cs.getDomainRooms()){
                    mClassId2Rooms.get(id).add(r);
                    //log.info("reAssignRoom, add room " + r + " to class " + id + " roomCapacity.length = " + I.getRoomCapacity().length);
                }

                mClassId2Rooms.get(id).sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return I.getRoomCapacity()[o1]-I.getRoomCapacity()[o2];
                    }
                });
            }
        }
        for(int i = 0; i < classes.size(); i++){
            //ModelResponseTimeTablingClass cls = classes.get(i);
            //String msg = "";
            //for(int r: mClassId2Rooms.get(cls.getId())){
            //    msg = msg + "[" + r + "," + W.mIndex2Room.get(r).getClassroom() + "," + W.mIndex2Room.get(r).getQuantityMax() + "], ";
            //}
            //log.info("reAssignRoom: class " + cls.getClassCode() + "," + cls.getModuleCode() + ", qty " + cls.getQuantityMax() + ": rooms = " + msg);

        }
        Map<Long, Long> mClassId2ConsecutiveClassId = new HashMap<>();
        for(String course: mCourse2Classes.keySet()){
            List<ModelResponseTimeTablingClass> CLS = mCourse2Classes.get(course);
            for(ModelResponseTimeTablingClass cls: CLS){
                for(ModelResponseTimeTablingClass cls1: CLS){
                    if(cls != cls1){
                        if(consecutiveSameSession(cls.getId(),cls1.getId())==2){
                            mClassId2ConsecutiveClassId.put(cls.getId(),cls1.getId());
                        }
                    }
                }
            }
        }

        Map<Long, Integer> mClassId2Room = new HashMap<>();// solution room assignment
        //List<List<Long>> cluster = new ArrayList<>();
        // if a cluster has 2 classes, then they are same course, schedule in consecutive slot


        // apply greedy algorithm first-fit
        for(int i = 0; i < classes.size(); i++){
            ModelResponseTimeTablingClass cls = classes.get(i);
            if(mClassId2Room.get(cls.getId())!=null) continue;// already assigned room
            for(int r: mClassId2Rooms.get(cls.getId())){
                Long idConsecutive = null;
                if(mClassId2ConsecutiveClassId.get(cls.getId())!=null){
                    idConsecutive = mClassId2ConsecutiveClassId.get(cls.getId());
                    //mClassId2Room.put(id1,r);
                    //log.info("reAssignRoom, co-assign class[" + i + "/" + classes.size() + "] " + id1 + " to room " + r);

                }
                boolean ok = true;
                if(idConsecutive == null) {
                    for (Long id : mClassId2ConflictClassIds.get(cls.getId())) {
                        if (mClassId2Room.get(id) != null && mClassId2Room.get(id) == r) {
                            ok = false;
                            break;// has conflict class assigned to the same room r
                        }
                    }
                }else{// check both class
                    for (Long id : mClassId2ConflictClassIds.get(cls.getId())) {
                        if (mClassId2Room.get(id) != null && mClassId2Room.get(id) == r) {
                            ok = false;
                            break;// has conflict class assigned to the same room r
                        }
                    }
                    for (Long id : mClassId2ConflictClassIds.get(idConsecutive)) {
                        if (mClassId2Room.get(id) != null && mClassId2Room.get(id) == r) {
                            ok = false;
                            break;// has conflict class assigned to the same room r
                        }
                    }
                }
                if(ok){
                    mClassId2Room.put(cls.getId(),r);
                    log.info("reAssignRoom, assign class[" + i + "/" + classes.size() + "] " + cls.getId() + " to room " + r);
                    if(idConsecutive!=null){
                        //Long id1 = mClassId2ConsecutiveClassId.get(cls.getId());
                        mClassId2Room.put(idConsecutive,r);
                        log.info("reAssignRoom, co-assign class[" + i + "/" + classes.size() + "] " + idConsecutive + " to room " + r);

                    }
                    break;
                }
            }
        }

        for(int i = 0; i < classes.size(); i++){
            ModelResponseTimeTablingClass cls = classes.get(i);
            log.info("reAssign, start assign class-segment of class " + cls.getId());

            if(mClassId2ClassSegments.get(cls.getId())!=null){
                for(ClassSegment cs: mClassId2ClassSegments.get(cls.getId())){
                    if(solutionSlot.get(cs.getId())!=null) {
                        int slot = solutionSlot.get(cs.getId());
                        if(mClassId2Room.get(cls.getId())==null){
                            log.info("reAssignRoom, class " + cls.getId() + " was NOT assigned to any room");
                            continue;
                        }
                        assignTimeSlotRoom(cs, slot, mClassId2Room.get(cls.getId()));
                    }
                }
            }else{
                log.info("reAssignRoom, class " + cls.getId() + " does not have class segments NULL??");
            }
        }

    }
    private void roomBasedAssignRooms(){
        Set<Integer> rooms = new HashSet<>();
        for(ClassSegment cs: classSegments){
            for(int r: cs.getDomainRooms()) rooms.add(r);
        }

        classes.sort(new Comparator<ModelResponseTimeTablingClass>() {
            @Override
            public int compare(ModelResponseTimeTablingClass o1, ModelResponseTimeTablingClass o2) {
                return o2.getQuantityMax() - o1.getQuantityMax();
            }
        });
        for(int i = 0; i <  classes.size(); i++){
            //ModelResponseTimeTablingClass cls = classes.get(i);
            //log.info("reAssignRooms, after sorting classes, cls[" + i + "/" + classes.size() + "] " + cls.getClassCode() + "," + cls.getModuleCode() + ", qty = " + cls.getQuantityMax());
        }
        Map<Long, List<Long>> mClassId2ConflictClassIds = new HashMap<>();
        for(Long id: mClassId2ClassSegments.keySet()){
            mClassId2ConflictClassIds.put(id,new ArrayList<>());
        }
        for(Long id: mClassId2ClassSegments.keySet()){
            ModelResponseTimeTablingClass cls = mClassId2Class.get(id);
            for(Long id1: mClassId2ClassSegments.keySet())if(id < id1){
                if(overlapTimeTableClasses(id,id1)){
                    mClassId2ConflictClassIds.get(id).add(id1);
                    mClassId2ConflictClassIds.get(id1).add(id);
                    ModelResponseTimeTablingClass cls1 = mClassId2Class.get(id1);
                    if(cls.getClassCode().equals("92051")|| cls.getClassCode().equals("91651"))
                        log.info("reAssignRoom, CONFLICT of class " + cls.getClassCode() + " AND " + cls1.getClassCode());
                }
            }
        }
        //log.info("reAssignRoom, prepare room list");
        Map<Long, List<Integer>> mClassId2Rooms = new HashMap<>();
        for(Long id: mClassId2ClassSegments.keySet()){
            mClassId2Rooms.put(id, new ArrayList<>());
            List<ClassSegment> CS = mClassId2ClassSegments.get(id);
            if(CS == null){
                log.info("reAssignRoom, CS of class " + id + " is NULL");
            }
            if(CS.size() > 0){
                ClassSegment cs = CS.get(0);
                //if(cs.getDomainRooms()==null) log.info("reAssignRoom, cs.getDomainRoom NULL");
                //if(I.getRoomCapacity() == null) log.info("reAssignRoom, roomCapacity NULL");
                for(int r: cs.getDomainRooms()){
                    mClassId2Rooms.get(id).add(r);
                    //log.info("reAssignRoom, add room " + r + " to class " + id + " roomCapacity.length = " + I.getRoomCapacity().length);
                }

                mClassId2Rooms.get(id).sort(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return I.getRoomCapacity()[o1]-I.getRoomCapacity()[o2];
                    }
                });
            }
        }
        for(int i = 0; i < classes.size(); i++){
            //ModelResponseTimeTablingClass cls = classes.get(i);
            //String msg = "";
            //for(int r: mClassId2Rooms.get(cls.getId())){
            //    msg = msg + "[" + r + "," + W.mIndex2Room.get(r).getClassroom() + "," + W.mIndex2Room.get(r).getQuantityMax() + "], ";
            //}
            //log.info("reAssignRoom: class " + cls.getClassCode() + "," + cls.getModuleCode() + ", qty " + cls.getQuantityMax() + ": rooms = " + msg);

        }
        Map<Long, Long> mClassId2ConsecutiveClassId = new HashMap<>();
        for(String course: mCourse2Classes.keySet()){
            List<ModelResponseTimeTablingClass> CLS = mCourse2Classes.get(course);
            for(ModelResponseTimeTablingClass cls: CLS){
                for(ModelResponseTimeTablingClass cls1: CLS){
                    if(cls != cls1){
                        if(consecutiveSameSession(cls.getId(),cls1.getId())==2){
                            mClassId2ConsecutiveClassId.put(cls.getId(),cls1.getId());
                        }
                    }
                }
            }
        }

        Map<Long, Integer> mClassId2Room = new HashMap<>();// solution room assignment
        //List<List<Long>> cluster = new ArrayList<>();
        // if a cluster has 2 classes, then they are same course, schedule in consecutive slot


        // apply greedy algorithm first-fit
        for(int i = 0; i < classes.size(); i++){
            ModelResponseTimeTablingClass cls = classes.get(i);
            if(mClassId2Room.get(cls.getId())!=null) continue;// already assigned room
            for(int r: mClassId2Rooms.get(cls.getId())){
                Long idConsecutive = null;
                if(mClassId2ConsecutiveClassId.get(cls.getId())!=null){
                    idConsecutive = mClassId2ConsecutiveClassId.get(cls.getId());
                    //mClassId2Room.put(id1,r);
                    //log.info("reAssignRoom, co-assign class[" + i + "/" + classes.size() + "] " + id1 + " to room " + r);

                }
                boolean ok = true;
                if(idConsecutive == null) {
                    for (Long id : mClassId2ConflictClassIds.get(cls.getId())) {
                        if (mClassId2Room.get(id) != null && mClassId2Room.get(id) == r) {
                            ok = false;
                            break;// has conflict class assigned to the same room r
                        }
                    }
                }else{// check both class
                    for (Long id : mClassId2ConflictClassIds.get(cls.getId())) {
                        if (mClassId2Room.get(id) != null && mClassId2Room.get(id) == r) {
                            ok = false;
                            break;// has conflict class assigned to the same room r
                        }
                    }
                    for (Long id : mClassId2ConflictClassIds.get(idConsecutive)) {
                        if (mClassId2Room.get(id) != null && mClassId2Room.get(id) == r) {
                            ok = false;
                            break;// has conflict class assigned to the same room r
                        }
                    }
                }
                if(ok){
                    mClassId2Room.put(cls.getId(),r);
                    log.info("reAssignRoom, assign class[" + i + "/" + classes.size() + "] " + cls.getId() + " to room " + r);
                    if(idConsecutive!=null){
                        //Long id1 = mClassId2ConsecutiveClassId.get(cls.getId());
                        mClassId2Room.put(idConsecutive,r);
                        log.info("reAssignRoom, co-assign class[" + i + "/" + classes.size() + "] " + idConsecutive + " to room " + r);

                    }
                    break;
                }
            }
        }

        for(int i = 0; i < classes.size(); i++){
            ModelResponseTimeTablingClass cls = classes.get(i);
            log.info("reAssign, start assign class-segment of class " + cls.getId());

            if(mClassId2ClassSegments.get(cls.getId())!=null){
                for(ClassSegment cs: mClassId2ClassSegments.get(cls.getId())){
                    if(solutionSlot.get(cs.getId())!=null) {
                        int slot = solutionSlot.get(cs.getId());
                        if(mClassId2Room.get(cls.getId())==null){
                            log.info("reAssignRoom, class " + cls.getId() + " was NOT assigned to any room");
                            continue;
                        }
                        assignTimeSlotRoom(cs, slot, mClassId2Room.get(cls.getId()));
                    }
                }
            }else{
                log.info("reAssignRoom, class " + cls.getId() + " does not have class segments NULL??");
            }
        }

    }

    public int selectMinLoadRoom(){
        int minLoad = 1000000; int selRoom = -1;
        for(int r: mRoom2AssignedClassSegments.keySet()){
            if(mRoom2AssignedClassSegments.get(r).size()==0) continue;
            if(mRoom2AssignedClassSegments.get(r).size() < minLoad){
                minLoad = mRoom2AssignedClassSegments.get(r).size();
                selRoom = r;
            }
        }
        return selRoom;
    }
    int[] sortRooms(Set<Integer> candRooms){
        int[] sortedRooms = new int[candRooms.size()];
        int idx = -1;
        for(int r: candRooms){ idx++; sortedRooms[idx] = r; }
        for(int i = 0; i < sortedRooms.length; i++){
            for(int j = i+1; j < sortedRooms.length; j++){
                if(mRoom2AssignedClassSegments.get(sortedRooms[i]).size() >
                        mRoom2AssignedClassSegments.get(sortedRooms[j]).size()){
                    int tmp = sortedRooms[i]; sortedRooms[i] = sortedRooms[j]; sortedRooms[j] = tmp;
                }
            }
        }
        return sortedRooms;
    }
    public boolean overlap(int slot, ClassSegment cs, ClassSegment csi){
        if(!isScheduledClassSegment(csi)) return false;

        // return true if slot assigned to class segment cs overlap with the scheduled csi
        DaySessionSlot newDss = new DaySessionSlot(slot);
        int sli = solutionSlot.get(csi.getId());
        DaySessionSlot dssi = new DaySessionSlot(sli);
        if(dssi.day == newDss.day && dssi.session == newDss.session &&
                Util.overLap(newDss.slot,cs.getDuration(),dssi.slot,csi.getDuration())){
            return true;
        }
        return false;
    }
    public boolean isFeasibleSlot(ClassSegment cs, int slot){
        int sl = solutionSlot.get(cs.getId());
        DaySessionSlot dss = new DaySessionSlot(sl);
        DaySessionSlot newDss = new DaySessionSlot(slot);
        if(dss.session != newDss.session) return false;
        Long classId = cs.getClassId();
        for(ClassSegment csi: mClassId2ClassSegments.get(classId)){
            if(csi != cs){
                if(overlap(slot,cs,csi)) return false;
            }
        }
        ModelResponseTimeTablingClass cls = mClassId2Class.get(classId);
        if(cls.getParentClassId()!=null){
            Long pid = cls.getParentClassId();
            ModelResponseTimeTablingClass pcls = mClassId2Class.get(pid);
            for(ClassSegment csi: mClassId2ClassSegments.get(pid)){
                if(overlap(slot,cs,csi)) return false;
            }
        }
        for(Long cid: mClassId2ChildrenIds.get(classId)){
            for(ClassSegment csi: mClassId2ClassSegments.get(cid)){
                if(overlap(slot,cs,csi)) return false;
            }
        }
        return true;
    }
    public SlotRoom selectRoom(ClassSegment cs, Set<Integer> candRooms){
        int[] sortedRooms = sortRooms(candRooms);
        for(int r: sortedRooms){
            for(int sl: cs.getDomainTimeSlots()){
                boolean slotOK = true;
                for(int s = 0; s < cs.getDuration(); s++){
                    if(roomSolver.roomSlotOccupation[r][sl + s]==1){
                        slotOK = false; break;
                    }
                }
                if(slotOK){
                    if(isFeasibleSlot(cs,sl)){
                        return new SlotRoom(sl,r);
                    }
                }
            }
        }
        return null;// not found
    }
    public void improve(){
        for(int it = 1; it <= 1000; it++){
            int selRoom = selectMinLoadRoom();
            Set<Integer> candRooms = new HashSet<>();
            for(int r : mRoom2AssignedClassSegments.keySet()){
                if(mRoom2AssignedClassSegments.get(r).size() > 0 && r != selRoom)
                    candRooms.add(r);
            }
            if(selRoom == -1) break;
            for(ClassSegment cs: mRoom2AssignedClassSegments.get(selRoom)){
                if(mClassSegment2MatchedClassSegment.get(cs)!=null) continue;// do not consider matching
                SlotRoom sr = selectRoom(cs,candRooms);
                if(sr != null){
                    mRoom2AssignedClassSegments.get(selRoom).remove(cs);
                    int sl = solutionSlot.get(cs.getId());
                    for(int s = 0; s < cs.getDuration(); s++){
                        roomSolver.roomSlotOccupation[selRoom][sl + s] = 0;
                    }
                    solutionSlot.put(cs.getId(),-1); solutionRoom.put(cs.getId(),-1);
                    //assignTimeSlotRoom(cs,sr.slot,sr.room);
                    roomSolver.assignTimeSlotRoom(cs,sr.slot,sr.room);
                    mRoom2AssignedClassSegments.get(sr.room).add(cs);
                    log.info("improve, step " + it + ": discover one improvement cs " + cs.str());
                    break;
                }
            }
        }
    }
    @Override
    public boolean hasSolution() {
        return foundSolution;
    }

    @Override
    public Map<Integer, Integer> getMapSolutionSlot() {
        return solutionSlot;
    }

    @Override
    public Map<Integer, Integer> getMapSolutionRoom() {
        return solutionRoom;
    }

    @Override
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public void printSolution() {

    }

    public static void main(String[] args){
        String c = " ABC  ";
        System.out.println(c.trim());
    }
}
