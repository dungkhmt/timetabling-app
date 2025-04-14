package openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedays;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.*;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;

import java.util.*;
@Log4j2
public class BacktrackingSolverOneCluster implements Solver {
    private MapDataScheduleTimeSlotRoom I;
    Map<Integer, Integer> x_solutionSlot;
    Map<Integer, Integer> solutionSlot;

    Map<Integer, Integer> solutionRoom;
    Map<Integer, Set<Integer>> conflictClassSegment;
    Map<String, List<ClassSegment>> mCourseGroup2ClassSegments;
    List<String> courseGroups;
    int timeLimit;
    boolean foundSolution;
    Map<Integer, List<ClassSegment>> classesScheduledInSlot; // classesScheduledInSlot.get(s) is the list of classes scheduled in time slot s

    // data structures for checking combination not overlap
    ClassSegment[] x_cs;
    boolean foundCombination;

    public BacktrackingSolverOneCluster(MapDataScheduleTimeSlotRoomWrapper W) {
        this.I = W.data;
        conflictClassSegment= new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            conflictClassSegment.put(cs.getId(),new HashSet<>());
        }

        for(Integer[] p: I.getConflict()){
            int i = p[0]; int j = p[1];
            if(conflictClassSegment.get(i) != null && conflictClassSegment.get(j)!= null){
                conflictClassSegment.get(i).add(j);
                conflictClassSegment.get(j).add(i);
            }
        }
        mCourseGroup2ClassSegments = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            String courseCode = cs.getCourseCode();
            if(mCourseGroup2ClassSegments.get(courseCode)==null)
                mCourseGroup2ClassSegments.put(courseCode,new ArrayList<>());
            mCourseGroup2ClassSegments.get(courseCode).add(cs);
        }
        courseGroups = new ArrayList<>();
        for(String cg: mCourseGroup2ClassSegments.keySet()){
            courseGroups.add(cg);
        }
        x_cs = new ClassSegment[mCourseGroup2ClassSegments.keySet().size()];
    }
    private boolean checkNotOverLap(int slot, int duration, int i, ClassSegment cs){
        int sl = x_solutionSlot.get(cs.getId());
        int d = cs.getDuration();
        //log.info("checkNotOverLap(" + slot + "," + duration  + "," + i + ", cs.slot " + sl + ", cs.dur = " + d + " cs.info = " + cs.str());
        if(Util.overLap(slot,duration,sl,d)){
            //log.info("checkNotOverLap(" + slot + "," + duration  + "," + i + ", cs.slot " + sl + ", cs.dur = " + d
            //        + " cs.info = " + cs.str() + " RETURN FALSE");

            return false;
        }
        for(int j = 0; j < i; j++){
            ClassSegment csj = x_cs[j];
            int slj = x_solutionSlot.get(csj.getId());
            int dj = csj.getDuration();
            //log.info("checkNotOverLap(" + slot + "," + duration  + "," + i + ", cs.slot " + sl + ", cs.dur = " + d
            //        + " cs.info = " + cs.str() + " consider csj with sl[" + j + "] = " + slj + ", dj = " + dj);

            if(Util.overLap(sl,d,slj,dj)){
                //log.info("checkNotOverLap(" + slot + "," + duration  + "," + i + ", cs.slot " + sl + ", cs.dur = " + d
                //        + " cs.info = " + cs.str() + " RETURN FALSE due to sl[" + j + "] = " + slj + ", dj = " + dj);
                return false;
            }
        }
        return true;
    }
    private void tryValueCG(int i, int icg, List<String> courseGroups, int slot, int duration){
        // select a class-segment for the ith course-group to establish a combination
        if(foundCombination) return;
        String cg = courseGroups.get(i);
        List<ClassSegment> CS = mCourseGroup2ClassSegments.get(cg);
        //log.info("tryValueCG(" + i + "/" + icg + ", CS = " + CS.size() + " slot = " + slot + " duration = " + duration);
        for(ClassSegment cs: CS){
            if(checkNotOverLap(slot, duration, i, cs)){
                x_cs[i] = cs;
                //log.info("tryValueCG, x_cs[" + i + "/" + icg + "] = " + cs.str());
                if(i == icg-1){
                    log.info("tryValueCG foundCombination!!!!");
                    foundCombination = true;
                }else{
                    tryValueCG(i+1,icg,courseGroups,slot,duration);
                }
             }
        }
    }
    private void assignTimeSlotRoom(ClassSegment cs, int timeSlot, int room){
        //solutionSlot[csi] = timeSlot;
        //solutionRoom[csi] = room;

        solutionSlot.put(cs.getId(),timeSlot);
        solutionRoom.put(cs.getId(),room);
        for(int s = timeSlot; s <= timeSlot + cs.getDuration()-1; s++) {
            if(classesScheduledInSlot.get(s) == null)
                classesScheduledInSlot.put(s,new ArrayList<>());
            classesScheduledInSlot.get(s).add(cs);
        }

        // update room occupation
        //ClassSegment cs = classSegments.get(csi);
        String os = "";
        for(int r: I.getRoomOccupations()[room]) os = os + r + ",";
        //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room + " occupied by slots " + os);

        for(int s = 0; s <= cs.getDuration()-1; s++){
            int sl = timeSlot + s;
            if(I.getRoomOccupations()[room].indexOf(sl) >= 0){
                log.info("assignTimeSlotRoom[" + cs.getId() + "] prepare to add slot " + sl + " to room occupation[" + room + "] BUG?? slot already exist");
                System.exit(-1);
            }
            I.getRoomOccupations()[room].add(sl);
            os = os + sl + ",";
            log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room
                    + " roomOccupation[" + room + "].add(" + sl + ") -> " + os);
        }
        foundSolution = true;
    }
    private void unAassignTimeSlotRoom(ClassSegment cs, int timeSlot){
        //solutionSlot[csi] = timeSlot;
        //solutionRoom[csi] = room;
        if(solutionRoom.get(cs.getId())==null || solutionSlot.get(cs.getId())==null) return;

        int room = solutionRoom.get(cs.getId());
        solutionSlot.put(cs.getId(),-1);
        for(int s = timeSlot; s <= timeSlot + cs.getDuration()-1; s++) {
            //if(classesScheduledInSlot.get(s) == null)
            //    classesScheduledInSlot.put(s,new ArrayList<>());
            //classesScheduledInSlot.get(s).add(cs);
            classesScheduledInSlot.get(s).remove(cs);
        }

        // update room occupation
        //ClassSegment cs = classSegments.get(csi);
        String os = "";
        for(int r: I.getRoomOccupations()[room]) os = os + r + ",";
        //log.info("unAssignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room + " occupied by slots " + os);

        for(int s = 0; s <= cs.getDuration()-1; s++){
            int sl = timeSlot + s;
            //I.getRoomOccupations()[room].add(sl);
            int idx = I.getRoomOccupations()[room].indexOf(sl);
            if(idx >= 0) {
                I.getRoomOccupations()[room].remove(idx);
                String msg = "";
                for(int si: I.getRoomOccupations()[room]) msg = msg + si + ",";
                log.info("unAssignTimeSlotRoom[" + cs.getId() + "] removed slot sl = " + sl + " from room occupation[" + room  + "], result slots = " + msg);
            }else{
                log.info("unAssignTimeSlotRoom[" + cs.getId() + "] with slot timeSlot " + timeSlot + " slot sl = " + sl + " does not exists in room occupation[" + room + "]");
                System.exit(-1);
            }
            os = os + sl + ",";
            //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room
            // + " roomOccupation[" + room + "].add(" + sl + ") -> " + os);
        }
        foundSolution = true;
    }

    private boolean check(int slot, ClassSegment cs, int icg, List<String> courseGroups){
        // return true if there exist combination (a class-segment from each course-group) of course-groups[0...icg-1]
        // such that slot of class-segment cs is not over-lap of class-segment of this combination
        log.info("check(" + slot + ", cs = " + cs.str() + ", icg = " + icg + ", courseGroups " + courseGroups.size());
        if(icg == 0) return true;
        foundCombination = false;
        tryValueCG(0,icg,courseGroups,slot,cs.getDuration());
        return foundCombination;
    }
    private void solution(){
        log.info("solution FOUND!!!!");
        foundSolution = true;
        solutionSlot = new HashMap();
        for(Integer id: x_solutionSlot.keySet())
            solutionSlot.put(id,x_solutionSlot.get(id));
    }
    List<Integer> sortDomain(String courseGroup, int ics){
        List<Integer> D = new ArrayList<>();
        List<ClassSegment> CS = mCourseGroup2ClassSegments.get(courseGroup);
        if(ics == 0) return CS.get(0).getDomainTimeSlots();
        ClassSegment cs = CS.get(ics);
        Set<Integer> slots = new HashSet<>();
        for(int j = 0; j < ics; j++){
            ClassSegment csj = CS.get(j);
            if(x_solutionSlot.get(csj.getId())!= null && x_solutionSlot.get(csj.getId())!=-1)
                slots.add(x_solutionSlot.get(csj.getId()));
        }
        List<Integer> aSlots = new ArrayList<>();
        for(int e: slots) aSlots.add(e); Collections.sort(aSlots);
        // FIRST: try to find predecessor or successor time-slot in the same day-session (ngay-kip)
        for(int e: aSlots){
            DaySessionSlot dss = new DaySessionSlot(e);
            int ns = e - cs.getDuration();// previous slot in the same session-day
            if(ns >= 1 && cs.getDomainTimeSlots().contains(ns)
                    && !slots.contains(ns)){
                DaySessionSlot ndss = new DaySessionSlot(ns);
                if(ndss.day == dss.day && ndss.session == dss.session) {
                    D.add(ns);
                }
            }
            ns = e + cs.getDuration();// successor slot in the same session-day
            if(cs.getDomainTimeSlots().contains(ns) && !slots.contains(ns)){
                DaySessionSlot ndss = new DaySessionSlot(ns);
                if(ndss.day == dss.day && ndss.session == dss.session) {
                    D.add(ns);
                }
            }
        }
        // add remain slots to the returned list D
        for(int s: cs.getDomainTimeSlots())if(!slots.contains(s)){
            if(!D.contains(s)) D.add(s);
        }
        for(int s: cs.getDomainTimeSlots()){
            if(!D.contains(s)) D.add(s);
        }

        return D;
    }
    private void tryValue(int icg, List<String> courseGroups, int ics){
        if(foundSolution) return;
        String courseGroup = courseGroups.get(icg);
        List<ClassSegment> CS = mCourseGroup2ClassSegments.get(courseGroup);
        ClassSegment cs = CS.get(ics);
        //log.info("tryValue, icg = " + icg + "/" + courseGroups.size() + " ics = " + ics + "/" + CS.size() + " cs = " + cs.str());
        List<Integer> D = sortDomain(courseGroup,ics);

        //for(int s: cs.getDomainTimeSlots()){
        for(int s: D){
            if(check(s,cs, icg,courseGroups)){
                x_solutionSlot.put(cs.getId(),s);
                //log.info("tryValue, icg = " + icg + "/" + courseGroups.size() + " ics = " + ics + "/" + CS.size() + " assign slot " + s + " to cs " + cs.str());
                if(ics == CS.size()-1) {
                    if (icg == courseGroups.size() - 1) {
                        solution();
                    }else{
                        tryValue(icg+1,courseGroups,0);
                    }
                }else{
                    tryValue(icg,courseGroups,ics+1);
                }
            }
        }
    }
    private SlotRoom findRoom(ClassSegment cs, int slot){
        SlotRoom sr = null;
        for(int room: cs.getDomainRooms()) {
            boolean ok = true;
            for(int s = slot; s <= slot + cs.getDuration() - 1; s++){
                if(I.getRoomOccupations()[room].contains(s)){
                    ok = false; break;
                }
            }
            if(ok) {
                sr = new SlotRoom(slot,room);
                break;
            }
        }
        return sr;
    }


    @Override
    public void solve() {
        x_solutionSlot = new HashMap<>();
        solutionSlot = new HashMap<>();

        solutionRoom = new HashMap<>();
        foundSolution = false;
        classesScheduledInSlot = new HashMap<>();
        tryValue(0,courseGroups,0);
        if(foundSolution){

            // find rooms for class-segments
            for(ClassSegment cs: I.getClassSegments()){
                SlotRoom sr = findRoom(cs,solutionSlot.get(cs.getId()));
                if(sr == null){
                    foundSolution = false; break;
                }else{
                    assignTimeSlotRoom(cs,sr.slot,sr.room);
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
}
