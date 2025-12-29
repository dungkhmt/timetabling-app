package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;

import java.util.*;

@Log4j2

public class CourseSchedulerSummerSemesterVersion3 {
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
                baseSolver.mClassId2MatchClass.put(sel_id1,new MatchClass(maxScore,sel_id2));
                baseSolver.mClassId2MatchClass.put(sel_id2,new MatchClass(maxScore,sel_id1));

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
    public String name(){
        return "CourseSchedulerSummerSemesterVersion3";
    }
    public boolean solve(){
        log.info(name() + "::solve starts.... course = "  + courseCode);
        //List<Long> CLS = new ArrayList<>();
        //for(Long id: groupSolver.mCourse2ClassId.get(courseCode)) CLS.add(id);
        for(Long id: CLS){
            ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
            log.info("solve, has class " + cls.str());
        }

        // process LT-BT class
        Map<Long, List<ModelResponseTimeTablingClass>> mId2ChildrenBTClass = new HashMap<>();
        for(Long id: CLS){
            ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
            Long parentClassId = cls.getParentClassId();
            if(parentClassId != null){
                if(!mId2ChildrenBTClass.containsKey(parentClassId)){
                    mId2ChildrenBTClass.put(parentClassId, new ArrayList<>());
                }
                mId2ChildrenBTClass.get(parentClassId).add(cls);
            }
        }
        for(Long classId: mId2ChildrenBTClass.keySet()){
            ModelResponseTimeTablingClass parentClass = baseSolver.mClassId2Class.get(classId);
            LTBTClassSolverFindSlotsAndRooms LTBTCSFSR = new LTBTClassSolverFindSlotsAndRooms(
                    baseSolver,
                    parentClass,
                    mId2ChildrenBTClass.get(classId),
                    groupSolver.session,baseSolver.sortedRooms
                    );
            boolean ok = LTBTCSFSR.solve();
        }
        Set<Long> LTBTClassIds = new HashSet<>();
        for(Long id: mId2ChildrenBTClass.keySet()){
            LTBTClassIds.add(id);
            for(ModelResponseTimeTablingClass ccls: mId2ChildrenBTClass.get(id)){
                LTBTClassIds.add(ccls.getId());
            }
        }
        log.info("solve, CLS.size = " + CLS.size() + " LTBTClassIds.size = " + LTBTClassIds.size());
        //for(Long id:scheduledClassIds){ CLS.remove(id); }
        for(Long id: LTBTClassIds){ CLS.remove(id); }
        log.info("solve, after solving LTBT, remain CLS.size = " + CLS.size());

        // process matched-pair classes
        List<Long[]> pairs = matchClassInACourse(CLS);
        log.info(name() + "::solve matchClassInACourse OK -> pairs = " + (pairs == null ? "NULL": pairs.size()));
        if(pairs != null) for(Long[] p: pairs){
            match.put(p[0],p[1]); match.put(p[1],p[0]);
            ModelResponseTimeTablingClass cls1 = baseSolver.mClassId2Class.get(p[0]);
            ModelResponseTimeTablingClass cls2 = baseSolver.mClassId2Class.get(p[1]);
            log.info(name() + "::solver matched pair: " + cls1.str() + " --- " + cls2.str());
            MatchedClassSolverFindSlotsAndRooms MCS = new MatchedClassSolverFindSlotsAndRooms(baseSolver,
                    new ModelResponseTimeTablingClass[]{cls1,cls2},
                    groupSolver.session,baseSolver.sortedRooms);
            boolean ok = MCS.solve();
        }
        Set<Long> scheduledMatchedClassIds = new HashSet<>();
        for(Long id: CLS) if(baseSolver.isScheduled(id)) scheduledMatchedClassIds.add(id);
        for(Long id: scheduledMatchedClassIds){ CLS.remove(id); }
        log.info("solve, after solving matched classes, scheduledMatchedClassIds.size = " + scheduledMatchedClassIds.size() + " remain CLS = " + CLS.size());


        List<Long> ids = new ArrayList<>();
        //for(Long id: groupSolver.mCourse2ClassId.get(courseCode)) ids.add(id);
        for(Long id: CLS) ids.add(id);
        for(Long id: ids){
            if(baseSolver.isScheduled(id)) continue;
            ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
            ClassSolverFindSlotsAndRooms CSFSR = new ClassSolverFindSlotsAndRooms(baseSolver,cls,
                    groupSolver.session,baseSolver.sortedRooms);
            boolean ok = CSFSR.solve();
            if(match.get(id)!=null){
                Long mid = match.get(id);
                if(baseSolver.isScheduled(mid)) continue;
                ModelResponseTimeTablingClass mcls = baseSolver.mClassId2Class.get(mid);
                ClassSolverFindSlotsAndRooms mCSFSR = new ClassSolverFindSlotsAndRooms(baseSolver,mcls,
                        groupSolver.session,baseSolver.sortedRooms);
                ok = mCSFSR.solve();
            }
        }

        /*
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
         */
        log.info(name() + "::solve finished.... course = "  + courseCode);
        return true;
    }
}

