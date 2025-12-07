package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;

import java.util.*;

@Log4j2
public class CourseBTSchedulerSummerSemesterVersion3 extends CourseSchedulerSummerSemesterVersion3{
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

