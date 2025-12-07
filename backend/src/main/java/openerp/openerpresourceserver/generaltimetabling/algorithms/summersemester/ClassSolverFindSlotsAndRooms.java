package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;

import java.util.*;

@Log4j2
public class ClassSolverFindSlotsAndRooms{
    SummerSemesterSolverVersion3 baseSolver;
    ModelResponseTimeTablingClass cls;
    int session;
    int[] sortedRooms;
    List<ClassSegment> CS;
    int[] x_day;
    int[] x_slot;
    int[] x_room;
    List<List<Integer>> domain_days;
    List<List<Integer>> domain_slots;
    List<List<Integer>> domain_rooms;
    boolean[] dayVisited;
    int[] occupationDay;// occupationDay[d]: number of class segment scheduled on day d
    boolean found;

    public ClassSolverFindSlotsAndRooms(SummerSemesterSolverVersion3 baseSolver,
                                        ModelResponseTimeTablingClass cls,
                                        int session,
                                        int[] sortedRooms) {
        this.baseSolver = baseSolver;
        this.cls = cls;
        this.session = session;
        this.sortedRooms = sortedRooms;
        CS = baseSolver.mClassId2ClassSegments.get(cls.getId());
        domain_rooms = new ArrayList<>();
        domain_slots = new ArrayList<>();
        domain_days = new ArrayList<>();
        occupationDay = new int[Constant.daysPerWeek + 2];
        Arrays.fill(occupationDay,0);
        for(int i = 0; i < CS.size(); i++){
            List<Integer> d_rooms = new ArrayList<>();
            List<Integer> d_slots = new ArrayList<>();
            List<Integer> d_days = new ArrayList<>();
            ClassSegment cs = CS.get(i);
            if(baseSolver.isScheduledClassSegment(cs)){
                int sl = baseSolver.solutionSlot.get(cs.getId());
                int r = baseSolver.solutionRoom.get(cs.getId());
                d_rooms.add(r);
                DaySessionSlot dss = new DaySessionSlot(sl);
                d_days.add(dss.day); d_slots.add(dss.slot);
            }else{
                boolean fixedSchedule = false;
                if(baseSolver.mClassId2MatchClass.get(cls.getId())!=null){
                    MatchScore ms = baseSolver.mClassId2MatchClass.get(cls.getId()).matchScore;
                    if(ms.m !=null && ms.m.get(cs) != null){
                        ClassSegment mcs = ms.m.get(cs);
                        if(baseSolver.isScheduledClassSegment(mcs)){
                            int msl = baseSolver.solutionSlot.get(mcs.getId());
                            int mr = baseSolver.solutionRoom.get(mcs.getId());
                            d_rooms.add(mr);
                            DaySessionSlot mdss = new DaySessionSlot(msl);
                            int slot = Util.findConsecutiveStartSlot(cs.getDuration(),mdss.slot,mcs.getDuration(),Constant.slotPerCrew);
                            d_slots.add(slot);
                            d_days.add(mdss.day);
                            fixedSchedule = true;
                        }
                    }
                }
                if(!fixedSchedule){
                    Set<Integer> setDays = new HashSet<>();
                    Set<Integer> setSlots =new HashSet<>();
                    for(int sl: cs.getDomainTimeSlots()){
                        DaySessionSlot dss = new DaySessionSlot(sl);
                        setDays.add(dss.day); setSlots.add(dss.slot);
                    }
                    for(int d: setDays) d_days.add(d);
                    //for(int s: setSlots) domain_slots.add(s);
                    d_slots.add(1);// tiet 1
                    d_slots.add(Constant.slotPerCrew - cs.getDuration() + 1); // tiet cuoi
                    for(int r: sortedRooms){
                        if(cs.getDomainRooms().contains(r))
                            d_rooms.add(r);
                    }
                }
            }
            log.info("ClassSolverFindSlotsAndRooms:constructor, class cls = " + cls.str() +
                    " classsegment " + i + " -> d_days " + d_days.size() + " d_slots = " + d_slots.size() + " d_rooms = " + d_rooms.size());

            domain_rooms.add(d_rooms);
            domain_slots.add(d_slots);
            domain_days.add(d_days);
        }
    }

    boolean check(int i, ClassSegment cs, int r, int d, int s){
        log.info(name()+ "::check(" + i + "," + cs + "," + r + "," + d + "," + s + ") starts..");
        if(dayVisited[d]) return false;
        if(cls.getParentClassId()!=null){// there is parent LT class
            List<ClassSegment> pCS = baseSolver.mClassId2ClassSegments.get(cls.getParentClassId());
            if(pCS != null) {
                for (int j = 0; j < pCS.size(); j++) {
                    ClassSegment pcs = pCS.get(j);
                    if (baseSolver.isScheduledClassSegment(pcs)) {
                        int sl = baseSolver.solutionSlot.get(pcs.getId());
                        DaySessionSlot dss = new DaySessionSlot(sl);
                        if (dss.day == d && dss.session == session) {
                            if (Util.overLap(s, cs.getDuration(), dss.slot, pcs.getDuration())) {
                                return false;
                            }
                        }
                    }
                }
            }else{
                log.info(name()+ "::check(" + i + "," + cs + "," + r + "," + d + "," + s + ") -> BUG?? parent class " + cls.getParentClassId() + " does not have class segment (NULL)");
            }
        }
        for(int si = 0; si < cs.getDuration(); si++){
            DaySessionSlot dss = new DaySessionSlot(d,session,s+si);
            int sl = dss.hash();
            if(baseSolver.roomSolver.roomSlotOccupation[r][sl] > 0){
                log.info(name()+ "::check(" + i + "," + cs + "," + r + "," + d + "," + s + ") roomSlotOccupation[" + baseSolver.W.mIndex2Room.get(r).getId() + "," + sl + "(" + dss.toString() + ")] = 1 -> return false");
                return false;
            }
        }
        // check free slot-room for matched class-segment
        if(baseSolver.mClassId2MatchClass.get(cls.getId())!=null){
            MatchClass mc = baseSolver.mClassId2MatchClass.get(cls.getId());
            MatchScore ms = mc.matchScore;
            if(ms.m != null){
                if(ms.m.get(cs)!=null){
                    ClassSegment mcs = ms.m.get(cs);
                    int m_slot = Util.findConsecutiveStartSlot(mcs.getDuration(),s,cs.getDuration(),Constant.slotPerCrew);
                    for(int si = 0; si < mcs.getDuration(); si++){
                        DaySessionSlot dss = new DaySessionSlot(d,session,m_slot+si);
                        int sl = dss.hash();
                        if(baseSolver.roomSolver.roomSlotOccupation[r][sl] > 0){
                            log.info(name()+ "::check(" + i + "," + cs + "," + r + "," + d + "," + s + "),matched cs " + mcs.getId() + " with m_slot= " + m_slot + "  roomSlotOccupation[" + baseSolver.W.mIndex2Room.get(r).getId() + "," + sl + "(" + dss.toString() + ")] = 1 -> return false");

                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    private void solution(){
        found = true;
        log.info("solution, found a solution for class " + cls.str() + " qty " + cls.getQuantityMax());
        for(int i = 0; i < CS.size(); i++){
            DaySessionSlot dss = new DaySessionSlot(x_day[i],session,x_slot[i]);
            int sl = dss.hash();
            log.info("solution, found a solution for class " + cls.str() + " cs[" + i + "]-> slot " + dss.toString() + " room " + baseSolver.W.mIndex2Room.get(x_room[i]).getId());
            baseSolver.roomSolver.printRoomOccupation(x_room[i]);
            baseSolver.roomSolver.assignTimeSlotRoom(CS.get(i),sl,x_room[i]);
        }

    }
    private void sortDaysIncreaseOcculation(int[] days, int[] occupationDay){
        for(int j1 = 0; j1 < days.length; j1++){
            for(int j2 = j1+1; j2 < days.length; j2++){
                if(occupationDay[days[j1]] > occupationDay[days[j2]]){
                    int tmp = days[j1]; days[j1] = days[j2]; days[j2] = tmp;
                }
            }
        }
    }
    private void tryClassSegment(int i){
        //log.info("tryClassSegment(" + i + "/" + CS.size() + "), domain_slots.sz = " + domain_slots.size());
        ClassSegment cs = CS.get(i);
        if(found) return;
        List<Integer> d_slots = domain_slots.get(i);
        List<Integer> d_days = domain_days.get(i);
        List<Integer> d_rooms = domain_rooms.get(i);
        int[] D = new int[d_slots.size()];
        for(int j = 0; j < d_slots.size(); j++) D[j] = d_slots.get(j);
        //D[0] = domain_slots.get(0);// default tiet dau uu tien truoc
        //D[1] = domain_slots.get(1);// tiet cuoi
        if(i > 0 && x_slot[i-1] == 1){ // class segment truoc do da tiet dau
            if(D.length >= 2) {
                D[0] = d_slots.get(1); // uu tien tiet cuoi truoc
                D[1] = 1; // tiet dau xet sau
            }
        }
        int[] sortedDays = new int[d_days.size()];
        for(int j = 0; j < d_days.size(); j++) sortedDays[j] = d_days.get(j);
        if(i == 0){
            // sorted days based on occupationDay of baseSolver globally
            sortDaysIncreaseOcculation(sortedDays, baseSolver.occupationDay);
        }else{
            // sorted days based on occupationDays of this solver locally
            sortDaysIncreaseOcculation(sortedDays,occupationDay);
        }
        for(int r: d_rooms){
            if(found) break;
            for(int d: d_days) {
                if(found) break;
                for(int s: D){//for (int s : domain_slots) {
                    if(found) break;
                    if(check(i,cs,r,d,s)){
                        x_room[i] = r;
                        x_day[i] = d;
                        x_slot[i] = s;
                        dayVisited[d] = true;
                        occupationDay[d]++;
                        if(i == CS.size()-1) solution();
                        else tryClassSegment(i+1);
                        dayVisited[d] = false;
                        occupationDay[d]--;
                    }
                }
            }
        }
    }
    public String name(){
        return "ClassSolverFindSlotsAndRooms";
    }
    public boolean solve(){
        dayVisited = new boolean[Constant.daysPerWeek + 2];
        Arrays.fill(dayVisited,false);
        x_day = new int[CS.size()];
        x_slot = new int[CS.size()];
        x_room = new int[CS.size()];
        log.info(name() + "::solve class cls = " + cls.str() + " -> starts with tryClassSegment(0)...");
        found = false;
        tryClassSegment(0);
        if(found){
            log.info(name() + "::solve, found a schedule for class " + cls.str() + " qty " + cls.getQuantityMax());
        }else{
            log.info(name() + "::solve, cannot find a schedule for class " + cls.str() + " qty " + cls.getQuantityMax());
        }
        return found;
    }
}
