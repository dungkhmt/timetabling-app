package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;

import java.util.*;

@Log4j2
public class ClassBasedRoomAssignmentSolverVersion3{
    SummerSemesterSolverVersion3 baseSolver;
    int[] roomOccupation;// roomOccupation[r] is number of slots the room is occupied
    int[][] roomSlotOccupation; // roomSlotOccupation[r][sl] = 1 means that room r is occupied at slot sl

    public ClassBasedRoomAssignmentSolverVersion3(SummerSemesterSolverVersion3 baseSolver) {
        this.baseSolver = baseSolver;
        roomOccupation = new int[baseSolver.I.getRoomCapacity().length];
        Arrays.fill(roomOccupation,0);
        int maxSlots = Constant.daysPerWeek*Constant.slotPerCrew*2 + 2;
        //roomSlotOccupation = new int[maxRoomIndex + 1][maxSlots];
        roomSlotOccupation = new int[baseSolver.I.getRoomCapacity().length + 1][maxSlots];
        for(int r = 0; r < baseSolver.I.getRoomCapacity().length + 1; r++){
            for(int sl = 0; sl < maxSlots; sl++)
                roomSlotOccupation[r][sl] = 0;
        }
        //for(int r = 0; r < baseSolver.I.getRoomCapacity().length + 1; r++){
        for(int r = 0; r < baseSolver.I.getRoomCapacity().length; r++){
            for(int sl: baseSolver.I.getRoomOccupations()[r]){
                roomSlotOccupation[r][sl] = 1;
                log.info("ClassBasedRoomAssignmentSolver::Constructor, room " + baseSolver.W.mIndex2Room.get(r).getId() + " occupied at slot " + sl + "(" + new DaySessionSlot(sl).toString()+ ")");
            }
        }
        log.info("ClassBasedRoomAssignmentSolver::Constructor finished allocate roomSlotOccupation");
    }
    public void printRoomOccupation(int r){
        int maxSlots = Constant.daysPerWeek*Constant.slotPerCrew*2 + 2;
        int nbRooms= roomOccupation.length;
            String occ = "";
            for(int sl = 0; sl < maxSlots; sl++){
                if(roomSlotOccupation[r][sl] != 0){
                    DaySessionSlot dss = new DaySessionSlot(sl);
                    occ = occ + dss.toString()+ "; ";
                }
            }
            log.info("RoomOccupation info of room " + baseSolver.W.mIndex2Room.get(r).getId()+ ": " + occ);


    }

    public void printRoomOccupation(){
        int maxSlots = Constant.daysPerWeek*Constant.slotPerCrew*2 + 2;
        int nbRooms= roomOccupation.length;
        for(int r = 0; r < nbRooms; r++){
            String occ = "";
            for(int sl = 0; sl < maxSlots; sl++){
                if(roomSlotOccupation[r][sl] != 0){
                    DaySessionSlot dss = new DaySessionSlot(sl);
                    occ = occ + dss.toString()+ "; ";
                }
            }
            log.info("RoomOccupation info of room " + baseSolver.W.mIndex2Room.get(r).getId()+ ": " + occ);

        }

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
