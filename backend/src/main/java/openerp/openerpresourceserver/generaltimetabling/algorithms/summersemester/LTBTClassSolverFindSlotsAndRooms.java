package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;

import java.util.*;
@Log4j2
public class LTBTClassSolverFindSlotsAndRooms {
    SummerSemesterSolverVersion3 baseSolver;
    ModelResponseTimeTablingClass[] cls;
    int nbClasses;
    int maxDayIndex = 8;
    int maxRoomIndex = 500;
    int session;
    int[] sortedRooms;

    // params
    int MAX_ROOM_USED_ALLOW = 1;
    boolean SAM_ROOM_EACH_DAY = true;// class-segment scheduled on the same must be scheduled same room (teachers do not need to change room)
    boolean BT_CLASS_DISJOINT = true;

    int bestObj = 10000000;// number of days used (tobe minimized)

    List<ClassSegment>[] CS;

    int[][] x_day;// x_day[i][j] is the day scheduled for the class segment j of the class i
    int[][] x_slot;
    int[][] x_room;// x_room[i][j] is the room scheduled for the class segment j of the class i
    int[][] best_x_day;// x_day[i][j] is the day scheduled for the class segment j of the class i
    int[][] best_x_slot;
    int[][] best_x_room;// x_room[i][j] is the room scheduled for the class segment j of the class i
    int[] loadDay;// loadDay[d] is the number of class-segment assigned to the day d
    int[] loadRooms;// loadRooms[r] is the number of class-segments scheduled in the room r
    int nbDaysUsed = 0;
    int nbRoomsUsed = 0;

    List<Integer>[][] domain_days;//domain_days[i][j] is the domain of x_day[i][j]
    List<Integer>[][] domain_slots;
    List<Integer>[][] domain_rooms;

    boolean[] dayVisited;
    int[] occupationDay;// occupationDay[d]: number of class segment scheduled on day d
    boolean found;
    boolean verboseDEBUG = false;

    public LTBTClassSolverFindSlotsAndRooms(SummerSemesterSolverVersion3 baseSolver,
                                            //ModelResponseTimeTablingClass[] cls,
                                            ModelResponseTimeTablingClass parentLTClass,
                                            List<ModelResponseTimeTablingClass> childrenBTClasses,
                                            int session,
                                            int[] sortedRooms) {
        // cls[0] is the parent LT class, cls[1], cls[2],... are children BT classes
        cls = new ModelResponseTimeTablingClass[childrenBTClasses.size()+1];
        cls[0] =parentLTClass;
        for(int i = 0; i < childrenBTClasses.size(); i++){
            cls[i+1] = childrenBTClasses.get(i);
        }

        this.baseSolver = baseSolver;
        this.session = session;this.sortedRooms = sortedRooms;
        nbClasses = cls.length;
        CS = new List[cls.length];
        for(int i = 0; i < nbClasses; i++) CS[i] = baseSolver.mClassId2ClassSegments.get(cls[i].getId());
        domain_rooms= new List[cls.length][];
        domain_days= new List[cls.length][];
        domain_slots= new List[cls.length][];
        x_day = new int[cls.length][];
        x_room= new int[cls.length][];
        x_slot= new int[cls.length][];
        best_x_day = new int[cls.length][];
        best_x_room= new int[cls.length][];
        best_x_slot= new int[cls.length][];

        maxRoomIndex = 0;
        for(int i = 0; i < nbClasses; i++){
            List<ClassSegment> CS= baseSolver.mClassId2ClassSegments.get(cls[i].getId());
            int nbClassSegments = CS.size();

            domain_rooms[i] = new List[nbClassSegments];
            domain_days[i] = new List[nbClassSegments];
            domain_slots[i] =new List[nbClassSegments];
            for(int j = 0; j < nbClassSegments; j++){
                ClassSegment cs = CS.get(j);
                domain_rooms[i][j] = new ArrayList<Integer>();
                domain_days[i][j] = new ArrayList<>();
                domain_slots[i][j] = new ArrayList<>();

                Set<Integer> setDays = new HashSet<>();
                Set<Integer> setSlots =new HashSet<>();
                for(int sl: cs.getDomainTimeSlots()){
                    DaySessionSlot dss = new DaySessionSlot(sl);
                    setDays.add(dss.day); setSlots.add(dss.slot);
                }
                for(int d: setDays) domain_days[i][j].add(d);
                for(int s: setSlots) domain_slots[i][j].add(s);
                //d_slots.add(1);// tiet 1
                //d_slots.add(Constant.slotPerCrew - cs.getDuration() + 1); // tiet cuoi
                for(int r: sortedRooms){
                    if(r > maxRoomIndex) maxRoomIndex = r;

                    if(cs.getDomainRooms().contains(r))
                        domain_rooms[i][j].add(r);
                }
            }
            x_room[i]= new int[nbClassSegments];
            x_day[i]= new int[nbClassSegments];
            x_slot[i]= new int[nbClassSegments];
            best_x_room[i]= new int[nbClassSegments];
            best_x_day[i]= new int[nbClassSegments];
            best_x_slot[i]= new int[nbClassSegments];
        }

        loadDay = new int[maxDayIndex + 1];
        loadRooms= new int[maxRoomIndex + 1];
        Arrays.fill(loadDay, 0);
        Arrays.fill(loadRooms, 0);

    }
    private boolean check(int r, int d, int s, int i, int j){
        // return true if room r, day d, and slot s can be assigned to class-segment j of class i
        // s is in the range 1, 2, . . .,5 nbSlotPerSession
        if(loadRooms[r] == 0 && nbRoomsUsed >= MAX_ROOM_USED_ALLOW) return false;

        ClassSegment cs = CS[i].get(j);
        for(int k = 0; k < cs.getDuration(); k++){
            DaySessionSlot dss = new DaySessionSlot(d,session,k+s);
            int sl= dss.hash();
            if(baseSolver.roomSolver.roomSlotOccupation[r][sl] > 0){ return false; }
        }


        for(int j1 = 0; j1 <= j-1; j1++){
            if(x_day[i][j1] == d) return false;// days assigned to class-segments of the same class must be distinct
        }
        if(i == 0) return true;// enough check for the FIRST (LT) class

        // class-segment [i][j] must be disjoint with LT class-segments
        for(int i1=0; i1 <= 0; i1++){
            for(int j1 = 0; j1 < x_day[i1].length; j1++){
                if(x_day[i1][j1] == d){
                    boolean overlap = Util.overLap(x_slot[i1][j1],CS[i1].get(j1).getDuration(),s,cs.getDuration());
                    //log.info(name() + "::check(" + r + ","+ d + "," + s + "," + i + "," + j + "), overLap with (" + i1 + "," + j1 + ") = " + overlap);
                    if(overlap){ return false; }
                    //if(SAM_ROOM_EACH_DAY){// class-segments scheduled on the same ay must be scheduled in the same room
                    //    if(x_room[i1][j1] != r){ return false; }
                    //}
                }
            }
        }

        if(BT_CLASS_DISJOINT) {
            for (int i1 = 1; i1 <= i - 1; i1++) {
                for (int j1 = 0; j1 < x_day[i1].length; j1++) {
                    if (x_day[i1][j1] == d) {
                        if (Util.overLap(x_slot[i1][j1], CS[i1].get(j1).getDuration(), s, cs.getDuration())) {
                            return false;
                        }
                        if (SAM_ROOM_EACH_DAY) {// class-segments scheduled on the same ay must be scheduled in the same room
                            if (x_room[i1][j1] != r) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    private void submitSolution(){
        if(!found) return;
        for(int i = 0; i < nbClasses; i++) {
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls[i].getId());
            for (int j = 0; j < CS.size(); j++) {
                DaySessionSlot dss = new DaySessionSlot(best_x_day[i][j], session, best_x_slot[i][j]);
                int sl = dss.hash();
                log.info("submitSolution(), submit the solution for class " + cls[i].str() + " cs[" + i + "]-> slot " + dss.toString() + " room " + baseSolver.W.mIndex2Room.get(best_x_room[i][j]).getId());
                baseSolver.roomSolver.printRoomOccupation(best_x_room[i][j]);
                baseSolver.roomSolver.assignTimeSlotRoom(CS.get(j), sl, best_x_room[i][j]);
            }
        }

    }
    private void solution(){
        //log.info("solution FOUND solution!!");
        found = true;
        /*
        Set<Integer> daysUsed = new HashSet<>();
        for(int i = 0; i < nbClasses; i++){
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls[i].getId());
            for(int j = 0; j < CS.size(); j++) {
                daysUsed.add(x_day[i][j]);
            }
        }
         */
        //if(daysUsed.size() < bestObj){

        if(nbDaysUsed < bestObj){
            bestObj = nbDaysUsed;
            //log.info("solution FOUND solution -> update best " + bestObj);
            for(int i = 0; i < nbClasses; i++){
                List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls[i].getId());
                for(int j = 0; j < CS.size(); j++) {
                    best_x_room[i][j] = x_room[i][j];
                    best_x_day[i][j] = x_day[i][j];
                    best_x_slot[i][j] = x_slot[i][j];
                    log.info(name() + "::soluton, updated-best best_x_room[" + i + "][" + j + "]=" + best_x_room[i][j] + ", best_x_day[" + i + "][" + j + "]=" + best_x_day[i][j] + ", best_x_slot[" + i + "][" + j + "]=" + best_x_slot[i][j] + ", bestObj = " + bestObj);
                }
            }
        }
        /*
        for(int i = 0; i < nbClasses; i++) {
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls[i].getId());
            for (int j = 0; j < CS.size(); j++) {
                DaySessionSlot dss = new DaySessionSlot(x_day[i][j], session, x_slot[i][j]);
                int sl = dss.hash();
                log.info("solution, found a solution for class " + cls[i].str() + " cs[" + i + "]-> slot " + dss.toString() + " room " + baseSolver.W.mIndex2Room.get(x_room[i][j]).getId());
                baseSolver.roomSolver.printRoomOccupation(x_room[i][j]);
                baseSolver.roomSolver.assignTimeSlotRoom(CS.get(j), sl, x_room[i][j]);
            }
        }
        */
    }
    private void tryClassSegment(int i, int j){
        // try values for x_day[i][j], x_slot[i][j], x_room[i][j]
        //log.info("tryClassSegment(" + i + "," + j + ")");
        //if(found) return;
        for(int r: domain_rooms[i][j]){
            for(int d: domain_days[i][j]){
                for(int s: domain_slots[i][j]){
                    if(check(r,d,s,i,j)){
                        x_room[i][j] = r; x_day[i][j] = d; x_slot[i][j] = s;
                        if(loadDay[d] == 0) nbDaysUsed += 1;// one more day loaded (has class-segment scheduled)
                        loadDay[d] += 1;
                        if(loadRooms[r]==0) nbRoomsUsed += 1;// on more room loaded
                        loadRooms[r] += 1;
                        if(j == cls[i].getTimeSlots().size()-1){
                            if(i == nbClasses-1){
                                solution();
                            }else{
                                tryClassSegment(i+1,0);
                            }
                        }else{
                            tryClassSegment(i,j+1);
                        }
                        if(loadDay[d] == 1) nbDaysUsed -= 1;// reduce one more day loaded (has class-segment scheduled)
                        loadDay[d] -= 1;
                        if(loadRooms[r]==1) nbRoomsUsed -= 1;// reduce on more room loaded
                        loadRooms[r] -= 1;
                    }
                }
            }
        }
    }
    public String name(){
        return "LTBTClassSolverFindSlotsAndRooms";
    }
    public boolean solve(){
        log.info(name() + "::solve starts...");
        //MatchScore ms = baseSolver.matchScore(cls[0].getId(),cls[1].getId(), Constant.slotPerCrew);
        //ClassSolverFindSlotsAndRooms CSFSR = new ClassSolverFindSlotsAndRooms(baseSolver,cls[0],session,sortedRooms);
        //CSFSR.solve();

        found = false;
        nbDaysUsed = 0; nbRoomsUsed = 0;
        tryClassSegment(0, 0);
        submitSolution();
        return true;
    }

}
