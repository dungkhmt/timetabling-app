package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.ModelSchedulingLog;
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
    int MAX_ROOM_USED_ALLOW = 3;
    boolean SAM_ROOM_EACH_DAY = true;// class-segment scheduled on the same must be scheduled same room (teachers do not need to change room)
    boolean BT_CLASS_DISJOINT = true;
    boolean BT_CLASS_MUST_FOLOW_LT = true;


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

    long maxExecTime = 10000;// 10 seconds
    long startExecTime;

    // statistic
    int nbTrials = 0;
    int nbChecks = 0;
    int nbCheckSucc = 0;
    int nbCheckFail = 0;
    int depth_i = 0;
    int depth_j = 0;
    boolean timeExpired = false;
    public LTBTClassSolverFindSlotsAndRooms(SummerSemesterSolverVersion3 baseSolver,
                                            //ModelResponseTimeTablingClass[] cls,
                                            ModelResponseTimeTablingClass parentLTClass,
                                            List<ModelResponseTimeTablingClass> childrenBTClasses,
                                            int session,
                                            int[] sortedRooms) {
        maxExecTime = baseSolver.timeLimit;
        //log.info("LTBTClassSolverFindSlotsAndRooms, maxExecTime: " + maxExecTime);
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

        maxRoomIndex = 0; maxDayIndex = 0;
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
                    if(maxDayIndex < dss.day){ maxDayIndex = dss.day; }
                }
                for(int d: setDays) domain_days[i][j].add(d);
                for(int s: setSlots){
                    if(s == 1 || s == Constant.slotPerCrew - cs.getDuration() + 1)
                        domain_slots[i][j].add(s);
                }
                //for(int s: setSlots){
                //    if(!domain_slots[i][j].contains(s)) domain_slots[i][j].add(s);
                //        domain_slots[i][j].add(s);
                //}

                //d_slots.add(1);// tiet 1
                //d_slots.add(Constant.slotPerCrew - cs.getDuration() + 1); // tiet cuoi
                for(int r: sortedRooms){
                    if(r > maxRoomIndex) maxRoomIndex = r;

                    if(cs.getDomainRooms().contains(r)) {
                        // check availability of the room r (it was previously assigned to oter classes
                        boolean ok=false;
                        for(int d: setDays){
                            int L = baseSolver.roomSolver.computeLongestFreeSlots(r,d,session);
                            if(L >= cs.getDuration()){ ok = true; break; }
                        }
                        if(ok)  domain_rooms[i][j].add(r);
                        else{
                            log.info(name() + "::constructor,prune room[" + r + "] " + baseSolver.W.mIndex2Room.get(r).getClassroom() + " from class-segment "+ cs.str());
                            for(int d: setDays){
                                int L = baseSolver.roomSolver.computeLongestFreeSlots(r,d,session);
                                log.info(name() + "::constructor,prune room[" + r + "] " + baseSolver.W.mIndex2Room.get(r).getClassroom() + " free " + L + " consecutive slots on day " + d + " session " + session);
                            }
                        }
                    }
                }
                String s_rooms = "";
                for(int r: domain_rooms[i][j]){ s_rooms += baseSolver.W.mIndex2Room.get(r).getClassroom() + ","; }
                log.info(name() + "::constructor -> domain_rooms for class-segment " + cs.str() + " size = " + domain_rooms[i][j].size() + " rooms = " + s_rooms);
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
    private boolean checkAlternativeDays(List<Integer> L1, List<Integer> L2){
        L1.sort(Comparator.naturalOrder());
        L2.sort(Comparator.naturalOrder());

        for(int i = 0; i < L2.size(); i++){
            if(i < L1.size()) if(L1.get(i) >= L2.get(i)) return false;
            if(i+1 < L1.size()) if(L2.get(i) >= L1.get(i+1)) return false;
        }
        return true;
    }
    private boolean check(int r, int d, int s, int i, int j){
        //log.info("check(" + r + ", " + d + ", " + s + ", " + i + ", " + j + ") STARTS");
        nbChecks++;
        // return true if room r, day d, and slot s can be assigned to class-segment j of class i
        // s is in the range 1, 2, . . .,5 nbSlotPerSession
        if(loadRooms[r] == 0 && nbRoomsUsed >= MAX_ROOM_USED_ALLOW){
            //log.info("check(" + r + ", " + d + ", " + s + ", " + i + ", " + j + ") loadRoom[" + r+ "] = " + loadRooms[r] + " nbRoomUsed = " + nbRoomsUsed + " already exceed MAX_ROW_ALLOW = " +MAX_ROOM_USED_ALLOW + " -> return false");
            return false;
        }

        ClassSegment cs = CS[i].get(j);
        for(int k = 0; k < cs.getDuration(); k++){
            DaySessionSlot dss = new DaySessionSlot(d,session,k+s);
            int sl= dss.hash();
            //log.info("check(" + r + ", " + d + ", " + s + ", " + i + ", " + j + ") k = " +k + " sl = " + sl);
            if(baseSolver.roomSolver.roomSlotOccupation[r][sl] > 0){ return false; }
        }
        //log.info("check(" + r + ", " + d + ", " + s + ", " + i + ", " + j + ") A");

        for(int j1 = 0; j1 <= j-1; j1++){
            if(x_day[i][j1] == d) return false;// days assigned to class-segments of the same class must be distinct
        }
        if(i == 0) return true;// enough check for the FIRST (LT) class

        // from HERE, just check if current class-segment is BT
        //log.info("check(" + r + ", " + d + ", " + s + ", " + i + ", " + j + ") B");
        if(j == x_day[i].length - 1){
            if(BT_CLASS_MUST_FOLOW_LT){
                 List<Integer> day_LT = new ArrayList<>();
                 for(int j1 = 0; j1 < x_day[0].length; j1++){ day_LT.add(x_day[0][j1]); }
                 List<Integer> day_BT = new ArrayList<>();
                 for(int j1 = 0; j1 < x_day[i].length-1; j1++){ day_BT.add(x_day[i][j1]); }
                 day_BT.add(d);
                 if(!checkAlternativeDays(day_LT, day_BT)){ return false; }
            }
        }
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
        //log.info("check(" + r + ", " + d + ", " + s + ", " + i + ", " + j + ") C");
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
        //log.info("check(" + r + ", " + d + ", " + s + ", " + i + ", " + j + ") return true");
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
    public List<RoomDaySlotSession> sortDomain(int i, int j){
        // return list of (room, day, slot, session) of class-segment j of class i
        List<RoomDaySlotSession> P = new ArrayList<>();// prioritized items
        if(i ==0){// class-segment of the LT class
            if(j > 0){
                int r = x_room[i][j-1]; // same room
                int d = x_day[i][j-1] + 2;
                if(d <= maxDayIndex){
                    P.add(new RoomDaySlotSession(r,d,1,session));
                }
                d = x_day[i][j-1]-2;
                if(d >= 2){
                    P.add(new RoomDaySlotSession(r,d,1,session));
                }
                d = x_day[i][j-1] + 3;
                if(d <= maxDayIndex){
                    P.add(new RoomDaySlotSession(r,d,1,session));
                }
                d = x_day[i][j-1]-3;
                if(d >= 2){
                    P.add(new RoomDaySlotSession(r,d,1,session));
                }
            }
        }else{// class-segment of BT class
            if(i == 1){// first BT class
                if(j > 0){
                    int r = x_room[i][j-1]; // same room
                    int d = x_day[i][j-1] + 2;
                    if(d <= maxDayIndex){
                        P.add(new RoomDaySlotSession(r,d,1,session));
                    }
                    d = x_day[i][j-1]-2;
                    if(d >= 2){
                        P.add(new RoomDaySlotSession(r,d,1,session));
                    }
                    d = x_day[i][j-1] + 3;
                    if(d <= maxDayIndex){
                        P.add(new RoomDaySlotSession(r,d,1,session));
                    }
                    d = x_day[i][j-1]-3;
                    if(d >= 2){
                        P.add(new RoomDaySlotSession(r,d,1,session));
                    }
                }
            }else{// not first BT class
                for(int i1 = 1; i1 <= i-1; i1++){
                    for(int j1 = 0; j1 < CS[i1].size(); j1++){
                        ClassSegment cs1 = CS[i1].get(j1);
                        ClassSegment cs = CS[i].get(j);

                        int d = x_day[i1][j1] ;
                        int r = x_room[i1][j1];
                        int s = x_slot[i1][j1] + cs1.getDuration();
                        if(s + cs.getDuration()-1 <= maxDayIndex){
                           P.add(new RoomDaySlotSession(r,d,s,session));
                        }
                        s = x_slot[i1][j1] - cs.getDuration();
                        if(s >= 1){
                            P.add(new RoomDaySlotSession(r,d,s,session));
                        }
                    }
                }
            }
        }

        List<RoomDaySlotSession> res = new ArrayList<>();
        for(RoomDaySlotSession rdss: P) res.add(rdss);

        for(int r: domain_rooms[i][j]){
            for(int d: domain_days[i][j]){
                for(int s: domain_slots[i][j]){
                    boolean exist = false;
                    for(RoomDaySlotSession rdss: P){
                        if(rdss.room==r && rdss.day==d && rdss.slot==s && rdss.session==session){
                            exist = true; break;
                        }
                    }
                    if(!exist){
                        res.add(new RoomDaySlotSession(r,d,s,session));
                    }
                }
            }
        }
        return res;
    }
    private void tryClassSegment(int i, int j){
        long t = System.currentTimeMillis() - startExecTime;
        if(t > maxExecTime){
            if(timeExpired==false)// print log ONE time
                log.info("tryClassSegment(" + i + "," + j + ") maxTime = " + maxExecTime + " time = " + t + " expired!!");
            timeExpired = true;
            return;
        }

        nbTrials++;
        if(depth_i < i){
            depth_i = i; depth_j = j;
        }else if(depth_i == i && depth_j < j){
            depth_i = i; depth_j = j;
        }
        // try values for x_day[i][j], x_slot[i][j], x_room[i][j]
        //log.info("tryClassSegment(" + i + "," + j + ")");
        //if(found) return;
        //log.info("tryClassSegment(" + i + "," + j + ")");
        List<RoomDaySlotSession> D = sortDomain(i, j);
        //log.info("tryClassSegment(" + i + "," + j + "), D.sz = " + D.size());
        for(RoomDaySlotSession rdss: D){
            int r = rdss.room; int d = rdss.day; int s = rdss.slot;
            if(check(r,d,s,i,j)){
                nbCheckSucc++;
                x_room[i][j] = r; x_day[i][j] = d; x_slot[i][j] = s;
                //log.info("assign x_room[" + i + "," + j + "] = " + r + ", x_day[" + i + "," + j + "] = " + d + ", x_slot[" + i + "," + j + "] = " + s);

                if(loadDay[d] == 0) nbDaysUsed += 1;// one more day loaded (has class-segment scheduled)
                loadDay[d] += 1;
                if(loadRooms[r]==0) nbRoomsUsed += 1;// on more room loaded
                loadRooms[r] += 1;
                //log.info("tryClassSegment(" + i + "," + j + "), D.sz = " + D.size() + " nbClasses= "+ nbClasses);
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
            }else{
                nbCheckFail++;
            }

        }
        /*
        for(int r: domain_rooms[i][j]){
            for(int d: domain_days[i][j]){
                for(int s: domain_slots[i][j]){
                    if(check(r,d,s,i,j)){
                        nbCheckSucc++;
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
                    }else{
                        nbCheckFail++;
                    }
                }
            }
        }

         */
    }
    public void printStatistics() {
        log.info(name() + "::printStatistics, nbTrails = " + nbTrials + " nbChecks = " + nbChecks + " nbCheckSucc = "
                + nbCheckSucc + " nbCheckFail = "
                + nbCheckFail + " depth_i = " + depth_i + " depth_j = " + depth_j);
    }
    public String name(){
        String courseCode  = "COURSE NULL";
        if(cls != null && cls.length > 0) courseCode = cls[0].getModuleCode();
        return "LTBTClassSolverFindSlotsAndRooms[" + courseCode + "]";
    }
    public boolean preprocessing(){
        for(int i = 0; i < nbClasses; i++){
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls[i].getId());
            for(int j = 0; j < x_room[i].length; j++){
                if(domain_rooms[i][j].size() <= 0){
                    log.info(name() + "::preprocessing, detect empty domain room of class-segment " + CS.get(i).str());
                    return false;
                }
            }
        }
        return true;
    }
    public boolean solve(){
        log.info(name() + "::solve starts...");
        //MatchScore ms = baseSolver.matchScore(cls[0].getId(),cls[1].getId(), Constant.slotPerCrew);
        //ClassSolverFindSlotsAndRooms CSFSR = new ClassSolverFindSlotsAndRooms(baseSolver,cls[0],session,sortedRooms);
        //CSFSR.solve();

        boolean preprocssingOK = preprocessing();
        if(!preprocssingOK){
            return false;
        }
        found = false;
        nbDaysUsed = 0; nbRoomsUsed = 0;

        nbTrials = 0;
        nbChecks = 0;
        nbCheckSucc = 0;
        nbCheckFail = 0;
        depth_i = 0;
        depth_j = 0;
        startExecTime = System.currentTimeMillis();
        timeExpired = false;
        tryClassSegment(0, 0);
        if(found){
            submitSolution();
        }else{
            log.info(name() + "::solve ends CANNOT find any solution...");
            if(timeExpired){
                String classCode = "";
                if(cls != null && cls.length > 0) classCode = cls[0].getClassCode();
                ModelSchedulingLog log = new ModelSchedulingLog();
                log.setClassCode(classCode);
                log.setDescription("time expired with timeLimit = " + maxExecTime);
                log.setCreatedDate(new Date());
                baseSolver.logs.add(log);
            }
        }
        printStatistics();
        return true;
    }
    public static void main(String[] args){
        List<Integer> a = new ArrayList<>();
        a.add(2); a.add(9); a.add(4);
        a.sort(Comparator.naturalOrder());
        for(int i: a) System.out.println(i);
    }
}
