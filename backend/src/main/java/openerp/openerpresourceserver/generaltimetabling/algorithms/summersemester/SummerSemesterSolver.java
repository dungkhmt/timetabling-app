package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import io.swagger.models.auth.In;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;

import java.util.*;
@Log4j2
public class SummerSemesterSolver implements Solver {
    MapDataScheduleTimeSlotRoom I;
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
    // output data structures
    List<Integer> unScheduledClassSegment;
    boolean foundSolution;
    int timeLimit;

    public SummerSemesterSolver(MapDataScheduleTimeSlotRoom I){
        this.I = I;
    }
    private void assignTimeSlotRoom(ClassSegment cs, int timeSlot, int room){
        //solutionSlot[csi] = timeSlot;
        //solutionRoom[csi] = room;

        solutionSlot.put(cs.getId(),timeSlot);
        solutionRoom.put(cs.getId(),room);
        // update room occupation
        //ClassSegment cs = classSegments.get(csi);
        String os = "";
        for(int r: I.getRoomOccupations()[room]) os = os + r + ",";
        //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room + " occupied by slots " + os);

        for(int s = 0; s <= cs.getDuration()-1; s++){
            int sl = timeSlot + s;
            I.getRoomOccupations()[room].add(sl);
            os = os + sl + ",";
            //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room
            // + " roomOccupation[" + room + "].add(" + sl + ") -> " + os);
        }
        foundSolution = true;
    }
    private boolean checkTimeSlotRoom(ClassSegment csi, int timeSlot, int room){
        int DEBUG_ID = 29;
        for(int s: I.getRoomOccupations()[room]){
            for(int j = 0; j < csi.getDuration(); j++) {
                int sl = timeSlot + j;// check all time-slot of the duration
                if (s == sl) {
                    if (csi.getId() == DEBUG_ID)
                        log.info("checkTimeSlotRoom(" + csi.getId() + "," + timeSlot + "," + room + ")" +
                                " --> return false as slot " + s + " occupied");
                    return false;
                }
            }
        }

        return true;
    }

    private int detectSession(ClassSegment cs){
        // return 0 if cs is morning or afternoon
        // 1 if cs is morning
        // 2 if cs is afternoon
        Set<Integer> S = new HashSet<>();
        for(int s: cs.getDomainTimeSlots()){
            DaySessionSlot dss = new DaySessionSlot(s);
            S.add(dss.session);
        }
        if(S.size() > 1) return 0;// both morning and afternoon
        else{
            for(int s: S){
                if(s == 0) return 1; else return 2;
            }
        }
        return 0;
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
    private Long[] sortClassesByCourse(List<ClassSegment> L){
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        int idx = -1;
        Long[] a = new Long[mClassId2ClassSegments.keySet().size()];
        for(Long id: mClassId2ClassSegments.keySet()){
            idx++; a[idx] = id;
        }
        Map<Long, String> mClassId2CourseCode = new HashMap<>();
        for(ClassSegment cs: L){
            mClassId2CourseCode.put(cs.getClassId(),cs.getCourseCode());
        }

        for(int i = 0; i < a.length; i++){
            for(int j = i+1; j < a.length; j++){
                String ci = mClassId2CourseCode.get(a[i]);
                String cj = mClassId2CourseCode.get(a[j]);
                if(ci.compareTo(cj) > 0){
                    Long tmp = a[i]; a[i] = a[j]; a[j] = tmp;
                }
            }
        }
        return a;
    }
    private void sortOnDuration(List<ClassSegment> L){
        Collections.sort(L, new Comparator<ClassSegment>() {
            @Override
            public int compare(ClassSegment o1, ClassSegment o2) {
                return o1.getDuration() - o2.getDuration();
            }
        });
    }

    private void scheduleClassSegments444(List<ClassSegment> L, int session){
        log.info("scheduleClassSegments444, session = " + session + " L.sz = " + L.size());
        int[][] C444 = {
            {2,2,4,2,6,2},
            {2,1,4,1,6,1}
        };
        // session = 0: morning
        // session = 1; afternoon
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        int idx = -1;
        Set<ClassSegment> scheduled = new HashSet<>();
        for(Long id: mClassId2ClassSegments.keySet()){
            idx ++;
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            ClassSegment cs1 = Li.get(0);
            log.info("scheduleClassSegments444, class " + id + " course " + cs1.getCourseCode() + " Li = " + Li.size());
            if(Li.size() != 3) continue;
            //ClassSegment cs1 = Li.get(0);
            ClassSegment cs2 = Li.get(1); ClassSegment cs3 = Li.get(2);
            int day1 = C444[session][0]; int slot1 = C444[session][1];
            int day2 = C444[session][2]; int slot2 = C444[session][3];
            int day3 = C444[session][4]; int slot3 = C444[session][5];

            int s1 = new DaySessionSlot(day1,session,slot1).hash();
            int s2 = new DaySessionSlot(day2,session,slot2).hash();
            int s3 = new DaySessionSlot(day3,session,slot3).hash();

            for(int room: cs1.getDomainRooms()){
                if(checkTimeSlotRoom(cs1,s1,room) && checkTimeSlotRoom(cs2,s2,room)
                && checkTimeSlotRoom(cs3,s3,room)){
                    assignTimeSlotRoom(cs1,s1,room);
                    assignTimeSlotRoom(cs2,s2,room);
                    assignTimeSlotRoom(cs3,s3,room);
                    scheduled.add(cs1); scheduled.add(cs2); scheduled.add(cs3);
                    break;
                }
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
    }
    private void scheduleClassSegments45(List<ClassSegment> L, int session){
        log.info("scheduleClassSegments45, session = " + session + " L.sz = " + L.size());
        int[][] C = {
                {2,1,4,1},
                {3,1,5,1},
                {4,1,6,1}
        };
        // session = 0: morning
        // session = 1; afternoon
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        int idx = -1;
        Long[] a = sortClassesByCourse(L);
        Set<ClassSegment> scheduled = new HashSet<>();
        //for(Long id: mClassId2ClassSegments.keySet()){
        for(Long id: a){
            idx ++;
            int j = idx%C.length;
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            if(Li.size() != 2) continue;
            ClassSegment cs1 = Li.get(0); ClassSegment cs2 = Li.get(1);

            int day1 = C[j][0]; int slot1 = C[j][1];
            int day2 = C[j][2]; int slot2 = C[j][3];


            int s1 = new DaySessionSlot(day1,session,slot1).hash();
            int s2 = new DaySessionSlot(day2,session,slot2).hash();
                 for(int room: cs1.getDomainRooms()){
                if(checkTimeSlotRoom(cs1,s1,room) && checkTimeSlotRoom(cs2,s2,room)
                      ){
                    assignTimeSlotRoom(cs1,s1,room);
                    assignTimeSlotRoom(cs2,s2,room);
                    scheduled.add(cs1); scheduled.add(cs2);
                    break;
                }
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
    }
    private void scheduleClassSegments234(List<ClassSegment> L, int session){
        log.info("scheduleClassSegments234, session = " + session + " L.sz = " + L.size());
        int[][] C234 = {
                {2,1,4,1,5,1},
                {4,4,2,3,6,1}
        };
        // session = 0: morning
        // session = 1; afternoon
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        /*
        int idx = -1;
        Long[] a = new Long[mClassId2ClassSegments.keySet().size()];
        for(Long id: mClassId2ClassSegments.keySet()){
            idx++; a[idx] = id;
        }
        Map<Long, String> mClassId2CourseCode = new HashMap<>();
        for(ClassSegment cs: L){
            mClassId2CourseCode.put(cs.getClassId(),cs.getCourseCode());
        }

         for(int i = 0; i < a.length; i++){
            for(int j = i+1; j < a.length; j++){
                String ci = mClassId2CourseCode.get(a[i]);
                String cj = mClassId2CourseCode.get(a[j]);
                if(ci.compareTo(cj) > 0){
                    Long tmp = a[i]; a[i] = a[j]; a[j] = tmp;
                }
            }
        }

         */
        Long[] a = sortClassesByCourse(L);
        int idx = -1;
        //for(Long id: mClassId2ClassSegments.keySet()){
        for(Long id: a){
            idx ++;
            int j = idx%2;
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            if(Li.size() != 3) continue;
            sortOnDuration(Li);
            ClassSegment cs1 = Li.get(0); ClassSegment cs2 = Li.get(1); ClassSegment cs3 = Li.get(2);
            log.info("scheduleClassSegments234, class " + id + " course " + cs1.getCourseCode() + ", j = " + j + " cs1 = " + cs1.getDuration() + " cs2 = " + cs2.getDuration() + " cs3 = " + cs3.getDuration());

            int day1 = C234[j][0]; int slot1 = C234[j][1];
            int day2 = C234[j][2]; int slot2 = C234[j][3];
            int day3 = C234[j][4]; int slot3 = C234[j][5];

            int s1 = new DaySessionSlot(day1,session,slot1).hash();
            int s2 = new DaySessionSlot(day2,session,slot2).hash();
            int s3 = new DaySessionSlot(day3,session,slot3).hash();

            log.info("scheduleClassSegments234, class " + id + " course " + cs1.getCourseCode() + ", j = " + j + " cs1 = "
                    + cs1.getDuration() + " cs2 = " + cs2.getDuration() + " cs3 = " + cs3.getDuration()
            + " s1 = " + s1 + " s2 = "+ s2 + " s3 = " + s3);

            for(int room: cs1.getDomainRooms()){
                if(checkTimeSlotRoom(cs1,s1,room) && checkTimeSlotRoom(cs2,s2,room)
                        && checkTimeSlotRoom(cs3,s3,room)){
                    assignTimeSlotRoom(cs1,s1,room);
                    assignTimeSlotRoom(cs2,s2,room);
                    assignTimeSlotRoom(cs3,s3,room);
                    break;
                }
            }
        }
    }
    private void scheduleClassSegments24and33(List<ClassSegment> L24, List<ClassSegment> L33, int session){
        int[][] C33 = {
                {2,1,4,1},
                {3,1,5,1}
        };
        int[][] C24 = {
                {2,4,5,1},
                {3,4,6,1}
        };
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L24){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        for(ClassSegment cs: L33){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        Long[] a24 = sortClassesByCourse(L24);
        Long[] a33 = sortClassesByCourse(L33);
        int i2 = 0; int i3 = 0;
        Set<ClassSegment> scheduled24= new HashSet<>();
        Set<ClassSegment> scheduled33 = new HashSet<>();
        while(i2 < a24.length && i3 < a33.length){
            int j2 = i2%2; int j3 = i3%2;
            Long id2 = a24[i2]; Long id3 = a33[i3];
            List<ClassSegment> SL24 = mClassId2ClassSegments.get(id2);
            List<ClassSegment> SL33 = mClassId2ClassSegments.get(id3);
            if(SL24.size() != 2 && SL33.size() != 2) continue;
            ClassSegment cs241 = SL24.get(0); ClassSegment cs242 = SL24.get(1);
            ClassSegment cs331 = SL33.get(0); ClassSegment cs332 = SL33.get(1);
            int day241 = C24[j2][0]; int slot241 = C24[j2][1];
            int day242 = C24[j2][2]; int slot242 = C24[j2][3];

            int day331 = C33[j3][0]; int slot331 = C33[j3][1];
            int day332 = C33[j3][2]; int slot332 = C33[j3][3];

            int s241 = new DaySessionSlot(day241,session,slot241).hash();
            int s242 = new DaySessionSlot(day242,session,slot242).hash();
            int s331 = new DaySessionSlot(day331,session,slot331).hash();
            int s332 = new DaySessionSlot(day332,session,slot332).hash();
            for(int room : cs241.getDomainRooms()) {
                if(checkTimeSlotRoom(cs241,s241,room)
                        & checkTimeSlotRoom(cs242,s242,room)
                        & checkTimeSlotRoom(cs331,s331,room)
                        & checkTimeSlotRoom(cs332,s332,room)
                ){
                    assignTimeSlotRoom(cs241,s241,room);
                    assignTimeSlotRoom(cs242,s242,room);
                    assignTimeSlotRoom(cs331,s331,room);
                    assignTimeSlotRoom(cs332,s332,room);
                    scheduled24.add(cs241); scheduled24.add(cs242);
                    scheduled33.add(cs331); scheduled33.add(cs332);
                    break;
                }
            }
            i2++;i3++;
        }
        for(ClassSegment cs: scheduled33) L33.remove(cs);
        for(ClassSegment cs: scheduled24) L24.remove(cs);
    }
    private void scheduleClassSegments222and333(List<ClassSegment> L222, List<ClassSegment> L333, int session){
        // case in ME (do hoa 1 (3,3,3) and do hoa 2(2,2,2) are scheduled alternatively
        int[][] C2 = {
                {2,1,4,1,6,1},
                {2,1,3,1,5,1},
        };
        int[][] C3 = {
                {2,3,4,3,6,3},
                {2,3,3,3,5,3}
        };
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L222){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        for(ClassSegment cs: L333){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        Long[] a2 = sortClassesByCourse(L222);
        Long[] a3 = sortClassesByCourse(L333);
        int i2 = 0; int i3 = 0;
        Set<ClassSegment> scheduled222 = new HashSet<>();
        Set<ClassSegment> scheduled333 = new HashSet<>();
        while(i2 < a2.length && i3 < a3.length){
            int j2 = i2%2; int j3 = i3%2;
            Long id2 = a2[i2]; Long id3 = a3[i3];
            List<ClassSegment> L2 = mClassId2ClassSegments.get(id2);
            List<ClassSegment> L3 = mClassId2ClassSegments.get(id3);
            if(L2.size() != 3 && L3.size() != 3) continue;
            ClassSegment cs21 = L2.get(0); ClassSegment cs22 = L2.get(1); ClassSegment cs23 = L2.get(2);
            ClassSegment cs31 = L3.get(0); ClassSegment cs32 = L3.get(1); ClassSegment cs33 = L3.get(2);
            int day21 = C2[j2][0]; int slot21 = C2[j2][1];
            int day22 = C2[j2][2]; int slot22 = C2[j2][3];
            int day23 = C2[j2][4]; int slot23 = C2[j2][5];
            int day31 = C3[j3][0]; int slot31 = C3[j3][1];
            int day32 = C3[j3][2]; int slot32 = C3[j3][3];
            int day33 = C3[j3][4]; int slot33 = C3[j3][5];
            int s21 = new DaySessionSlot(day21,session,slot21).hash();
            int s22 = new DaySessionSlot(day22,session,slot22).hash();
            int s23 = new DaySessionSlot(day23,session,slot23).hash();
            int s31 = new DaySessionSlot(day31,session,slot31).hash();
            int s32 = new DaySessionSlot(day32,session,slot32).hash();
            int s33 = new DaySessionSlot(day33,session,slot33).hash();
            for(int room : cs21.getDomainRooms()) {
                if(checkTimeSlotRoom(cs21,s21,room)
                & checkTimeSlotRoom(cs22,s22,room)
                & checkTimeSlotRoom(cs23,s23,room)
                & checkTimeSlotRoom(cs31,s31,room)
                & checkTimeSlotRoom(cs32,s32,room)
                & checkTimeSlotRoom(cs33,s33,room)){
                    assignTimeSlotRoom(cs21,s21,room);
                    assignTimeSlotRoom(cs22,s22,room);
                    assignTimeSlotRoom(cs23,s23,room);
                    assignTimeSlotRoom(cs31,s31,room);
                    assignTimeSlotRoom(cs32,s32,room);
                    assignTimeSlotRoom(cs33,s33,room);
                    scheduled222.add(cs21); scheduled222.add(cs22); scheduled222.add(cs23);
                    scheduled333.add(cs31); scheduled333.add(cs32); scheduled333.add(cs33);

                    break;
                }
            }
            i2++;i3++;
        }
        for(ClassSegment cs: scheduled222) L222.remove(cs);
        for(ClassSegment cs: scheduled333) L333.remove(cs);
        /*
        while(i2 < a2.length){
            int j2 = i2%2;
            Long id2 = a2[i2];
            List<ClassSegment> L2 = mClassId2ClassSegments.get(id2);
            if(L2.size() != 3) continue;
            ClassSegment cs21 = L2.get(0); ClassSegment cs22 = L2.get(1); ClassSegment cs23 = L2.get(2);
            int day21 = C2[j2][0]; int slot21 = C2[j2][1];
            int day22 = C2[j2][2]; int slot22 = C2[j2][3];
            int day23 = C2[j2][4]; int slot23 = C2[j2][5];
            int s21 = new DaySessionSlot(day21,session,slot21).hash();
            int s22 = new DaySessionSlot(day22,session,slot22).hash();
            int s23 = new DaySessionSlot(day23,session,slot23).hash();
            for(int room : cs21.getDomainRooms()) {
                if(checkTimeSlotRoom(cs21,s21,room)
                        & checkTimeSlotRoom(cs22,s22,room)
                        & checkTimeSlotRoom(cs23,s23,room)
                ){
                    assignTimeSlotRoom(cs21,s21,room);
                    assignTimeSlotRoom(cs22,s22,room);
                    assignTimeSlotRoom(cs23,s23,room);
                    break;
                }
            }
            i2++;
        }

        while(i3 < a3.length){
            int j3 = i3%2;
            Long id3 = a3[i3];
            List<ClassSegment> L3 = mClassId2ClassSegments.get(id3);
            if(L3.size() != 3) continue;
            ClassSegment cs31 = L3.get(0); ClassSegment cs32 = L3.get(1); ClassSegment cs33 = L3.get(2);
            int day31 = C3[j3][0]; int slot31 = C3[j3][1];
            int day32 = C3[j3][2]; int slot32 = C3[j3][3];
            int day33 = C3[j3][4]; int slot33 = C3[j3][5];
            int s31 = new DaySessionSlot(day31,session,slot31).hash();
            int s32 = new DaySessionSlot(day32,session,slot32).hash();
            int s33 = new DaySessionSlot(day33,session,slot33).hash();
            for(int room : cs31.getDomainRooms()) {
                if(
                    checkTimeSlotRoom(cs31,s31,room)
                    & checkTimeSlotRoom(cs32,s32,room)
                    & checkTimeSlotRoom(cs33,s33,room)
                ){
                      assignTimeSlotRoom(cs31,s31,room);
                    assignTimeSlotRoom(cs32,s32,room);
                    assignTimeSlotRoom(cs33,s33,room);
                    break;
                }
            }
            i3++;
        }

         */
    }
    private void scheduleClassSegments23(List<ClassSegment> L, int session){
        int[][] C23 = {
                {2,1,4,3},// (day 2 - slot 1) and (day 4 - slot 3)
                {5,1,2,3},// (day 5 - slot 1) and (day 2 - slot 3)
                {3,1,5,3},// (day 3 -slot 1) and (day 5 - slot 3)
                {6,1,3,3},// (day 5 - slot 1) and (day 3 - slot 3)
                {4,1,6,3},// (day 4 - slot 1) and (day 6 - slot 3)
        };
        log.info("scheduleClassSegments23, session = " + session + " L.sz = " + L.size());
        // session = 0: morning
        // session = 1; afternoon
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        int idx = -1;
        Long[] a = sortClassesByCourse(L);
        Set<ClassSegment> scheduled = new HashSet<>();
        //for(Long id: mClassId2ClassSegments.keySet()){
        for(Long id: a){
            idx ++;
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            if(Li.size() != 2) continue;
            ClassSegment cs1 = Li.get(0); ClassSegment cs2 = Li.get(1);
            if(cs1.getDuration() > cs2.getDuration()){
                ClassSegment tmp = cs1; cs1 = cs2; cs2 = tmp;
            }
            int j = idx% C23.length;
            int day1 = C23[j][0]; int slot1 = C23[j][1];
            int day2 = C23[j][2]; int slot2 = C23[j][3];
            int s1 = new DaySessionSlot(day1,session,slot1).hash();
            int s2 = new DaySessionSlot(day2,session,slot2).hash();
            for(int room: cs1.getDomainRooms()){
                if(checkTimeSlotRoom(cs1,s1,room) && checkTimeSlotRoom(cs2,s2,room)){
                    assignTimeSlotRoom(cs1,s1,room);
                    assignTimeSlotRoom(cs2,s2,room);
                    scheduled.add(cs1); scheduled.add(cs2);
                    break;
                }
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
    }
    private void scheduleClassSegments33(List<ClassSegment> L, int session){
        int[][] C = {
                {2,2,4,2},
                {3,2,5,2},
                {4,2,6,2}
        };
        log.info("scheduleClassSegments33, session = " + session + " L.sz = " + L.size());
        // session = 0: morning
        // session = 1; afternoon
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        int idx = -1;
        Long[] a = sortClassesByCourse(L);
        Set<ClassSegment> scheduled = new HashSet<>();
        //for(Long id: mClassId2ClassSegments.keySet()){
        for(Long id: a){
            idx ++;
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            if(Li.size() != 2) continue;
            ClassSegment cs1 = Li.get(0); ClassSegment cs2 = Li.get(1);
            if(cs1.getDuration() > cs2.getDuration()){
                ClassSegment tmp = cs1; cs1 = cs2; cs2 = tmp;
            }
            int j = idx%C.length;
            int day1 = C[j][0]; int slot1 = C[j][1];
            int day2 = C[j][2]; int slot2 = C[j][3];
            int s1 = new DaySessionSlot(day1,session,slot1).hash();
            int s2 = new DaySessionSlot(day2,session,slot2).hash();
            for(int room: cs1.getDomainRooms()){
                if(checkTimeSlotRoom(cs1,s1,room) && checkTimeSlotRoom(cs2,s2,room)){
                    assignTimeSlotRoom(cs1,s1,room);
                    assignTimeSlotRoom(cs2,s2,room);
                    scheduled.add(cs1); scheduled.add(cs2);
                    break;
                }
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
    }
    private void scheduleLTandTwoBT(int[][][] C, List<Long> ids, Map<Long, List<Long>> mClassId2ChildrenIds, Map<Long,
            List<ClassSegment>> mClassId2ClassSegments, int session){
        /*
        int[][][] C = {
                {
                        {4, 2, 1, 5, 4, 1},
                        {2, 3, 1, 4, 5, 1},
                        {3, 3, 3, 3, 6, 1}
                },
                {
                        {4, 3, 1, 5, 5, 1},
                        {2, 2, 1, 4, 4, 1},
                        {3, 2, 3, 3, 6, 1}
                }
        };

         */
        for(Long id: ids){
            log.info("scheduleLTandTwoBT, class " + id + " has children.sz = " + mClassId2ChildrenIds.get(id).size());
        }
        int nbInstances = C.length;
        int idx = -1;
        Set<Long> scheduled = new HashSet<>();
        for(Long id: ids){
            idx++;
            int j = idx%nbInstances;
            int nbLTSegments = C[j][0].length/3;
            int nbChildren = C[j].length-1;
            List<Long> children = mClassId2ChildrenIds.get(id);
            if(children == null || children.size() != nbChildren) continue;
            List<ClassSegment> LT = mClassId2ClassSegments.get(id);
            List<Integer> LTSlots = new ArrayList<>();
            for(int k = 0; k < nbLTSegments; k++){
                ClassSegment cs = LT.get(k);
                int duration = C[j][0][3*k]; int day = C[j][0][3*k+1]; int slot = C[j][0][3*k+2];
                int s = new DaySessionSlot(day,session,slot).hash();
                LTSlots.add(s);
            }
            for(int r: LT.get(0).getDomainRooms()){
                boolean ok = true;
                for(int k = 0; k < nbLTSegments; k++)
                    if(!checkTimeSlotRoom(LT.get(k),LTSlots.get(k),r)) ok = false;
                if(ok){
                    for(int k = 0; k < nbLTSegments; k++){
                        assignTimeSlotRoom(LT.get(k),LTSlots.get(k),r);
                        scheduled.add(id);
                    }
                    break;
                }
            }
            List<List<ClassSegment>> BTs = new ArrayList<>();
            List<int[]> slots = new ArrayList<>();
            int q = 0;
            for(Long btId: mClassId2ChildrenIds.get(id)){
                q++;
                List<ClassSegment> BT = mClassId2ClassSegments.get(btId);
                BTs.add(BT);
                slots.add(C[j][q]);
            }
            ClassSegmentsSlotsMatching matcher = new ClassSegmentsSlotsMatching(BTs,slots,session);
            if(matcher.solve()){
                for(List<ClassSegment> BT: BTs){
                    for(int r: BT.get(0).getDomainRooms()){
                        boolean ok = true;
                        for(ClassSegment cs: BT){
                            int slot = matcher.mClassSegment2Slot.get(cs);
                            if(!checkTimeSlotRoom(cs,slot,r)){
                                ok = false; break;
                            }
                        }
                        if(ok){
                            for(ClassSegment cs: BT) {
                                int slot = matcher.mClassSegment2Slot.get(cs);
                                assignTimeSlotRoom(cs, slot, r);
                            }
                        }
                    }
                }
            }

            /*
            int q = 0;
            for(Long btId: mClassId2ChildrenIds.get(id)){
                q++;
                List<ClassSegment> BT = mClassId2ClassSegments.get(btId);
                int[] a = C[j][q];
                if(BT.size() != a.length/3) continue;
                List<Integer> BTSlots = new ArrayList<>();
                for(int k = 0; k < BT.size(); k++){
                    int duration = a[3*k]; int day = a[3*k+1]; int slot = a[3*k+2];
                    int s = new DaySessionSlot(day,session,slot).hash();
                    BTSlots.add(s);
                }
                for(int r: BT.get(0).getDomainRooms()){
                    boolean ok = true;
                    for(int k = 0; k < BT.size(); k++){
                        if(!checkTimeSlotRoom(BT.get(k),BTSlots.get(k),r)){
                            ok = false; break;
                        }
                    }
                    if(ok){
                        for(int k = 0; k < BT.size(); k++){
                            assignTimeSlotRoom(BT.get(k),BTSlots.get(k),r);
                        }
                        scheduled.add(btId); break;
                    }
                }
            }
             */
            //Long idBT1 = children.get(0);
            //Long idBT2 = children.get(1);
            //List<ClassSegment> BT1 = mClassId2ClassSegments.get(idBT1);
            //List<ClassSegment> BT2 = mClassId2ClassSegments.get(idBT2);
            //if(BT1.size() != 2 || BT2.size() != 2) continue;
            //ClassSegment csLT1 = LT.get(0); ClassSegment csLT2 = LT.get(1);
            //ClassSegment csBT11 = BT1.get(0); ClassSegment csBT12 = BT1.get(1);
            //ClassSegment csBT21 = BT2.get(0); ClassSegment csBT22 = BT2.get(1);
        }
        for(Long id: scheduled) ids.remove(id);
    }

    private void scheduleGroupLTandBT(List<ClassSegment> L, int session){
        for(ClassSegment cs: L){
            log.info("scheduleGroupLTandBT, cs = course " + cs.getCourseCode() + " classId " + cs.getClassId() + " type " + cs.getType() + " duration " + cs.getDuration());
        }

        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        Map<Long, List<Long>> mClassId2ChildrenIds = new HashMap<>();
        Map<Long, Long> mClassId2ParentId = new HashMap<>();
        for(Long id: mClassId2ClassSegments.keySet()){
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            for(ClassSegment cs: Li){
                Long pid = cs.getParentClassId();
                if(pid != null){
                    mClassId2ParentId.put(id,pid);
                    log.info("scheduleGroupLTandBT, id = " + id + " has parent pid = " + pid);
                }
            }
        }
        for(Long id: mClassId2ParentId.keySet()){
            Long pid = mClassId2ParentId.get(id);
            if(mClassId2ChildrenIds.get(pid)==null)
                mClassId2ChildrenIds.put(pid,new ArrayList<>());
            mClassId2ChildrenIds.get(pid).add(id);
            log.info("scheduleGroupLTandBT, add id = " + id + " -> children of " + pid + " size = " + mClassId2ChildrenIds.get(pid).size());
        }
        List<Long> LT45BT2433 = new ArrayList<>();// list of classIds having pattern 4,5
        List<Long> LT33BT2433 = new ArrayList<>();
        List<Long> LT33BT3 = new ArrayList<>();
        List<Long> LT4BT2 = new ArrayList<>();
        //for(Long id: mClassId2ClassSegments.keySet()){
        for(Long id: mClassId2ChildrenIds.keySet()){
            List<ClassSegment> CLS = mClassId2ClassSegments.get(id);
            String pattern = detectPattern(CLS);
            String childrenPattern = detectePatternChildren(mClassId2ChildrenIds.get(id),mClassId2ClassSegments);


            if(pattern.equals("4,5")){
                if(childrenPattern.equals("2,4-3,3")) {
                    LT45BT2433.add(id);
                    log.info("scheduleGroupLTandBT, LT45BT2433.add(" + id + ")");
                }
            }else if(pattern.equals("3,3")){
                if(childrenPattern.equals("2,4-3,3")) {
                    LT33BT2433.add(id);
                    log.info("scheduleGroupLTandBT, LT33BT2433.add(" + id + ")");
                }else if(childrenPattern.equals("3")){
                    LT33BT3.add(id);
                    log.info("scheduleGroupLTandBT, LT33BT3.add(" + id + ")");

                }
            }else if(pattern.equals("4")){
                if(childrenPattern.equals("2")){
                    LT4BT2.add(id);
                    log.info("scheduleGroupLTandBT, LT4BT2.add(" + id + ")");

                }
            }
        }
        int[][][] CLT45BT2433 = {
                {
                        {4, 2, 1, 5, 4, 1},
                        {2, 3, 1, 4, 5, 1},
                        {3, 3, 3, 3, 6, 1}
                },
                {
                        {4, 3, 1, 5, 5, 1},
                        {2, 2, 1, 4, 4, 1},
                        {3, 2, 3, 3, 6, 1}
                }
        };
        int[][][] CLT33BT2433 = {
                {
                        {3, 2, 3, 3, 4, 3},
                        {2, 3, 1, 4, 5, 1},
                        {3, 3, 3, 3, 6, 1}
                },
                {
                        {3, 3, 2, 3, 5, 3},
                        {2, 2, 1, 4, 4, 1},
                        {3, 2, 3, 3, 6, 1}
                }
        };
        int[][][] CLT4BT2 = {
                {
                        {4,2,1},
                        {2,3,2},
                        {2,3,4},
                        {2,5,2}
                },
                {
                        {4,4,1},
                        {2,5,4},
                        {2,6,2},
                        {2,6,4}
                },
                {
                        {4,3,1},
                        {2,2,1},
                        {2,2,3},
                        {2,4,1}
                },
                {
                        {4,5,1},
                        {2,4,3},
                        {2,6,1},
                        {2,6,3}
                }
        };

        int[][][] CLT33BT3 = {
                {
                        {3,3,1,3,6,3},
                        {3,2,2},
                        {3,4,2},
                        {3,5,2}
                },
                {
                        {2,3,4,4,5,1},
                        {3,3,1},
                        {3,4,2},
                        {3,6,2}
                },
                {
                        {2,6,1,4,4,1},
                        {3,3,2},
                        {3,5,2},
                        {3,6,3}
                }
        };


        log.info("scheduleGroupLTandBT, starts L45BT2433.sz = " + LT45BT2433.size());
        scheduleLTandTwoBT(CLT45BT2433,LT45BT2433,mClassId2ChildrenIds,mClassId2ClassSegments,session);
        log.info("scheduleGroupLTandBT, ends L45BT2433.sz = " + LT45BT2433.size());

        log.info("scheduleGroupLTandBT, starts L33BT3.sz = " + LT33BT3.size());
        scheduleLTandTwoBT(CLT33BT3,LT33BT3,mClassId2ChildrenIds,mClassId2ClassSegments,session);
        log.info("scheduleGroupLTandBT, ends L33BT3.sz = " + LT33BT3.size());

        log.info("scheduleGroupLTandBT, starts L33BT2433.sz = " + LT33BT2433.size());
        scheduleLTandTwoBT(CLT33BT2433,LT33BT2433,mClassId2ChildrenIds,mClassId2ClassSegments,session);
        log.info("scheduleGroupLTandBT, ends L33BT2433.sz = " + LT33BT2433.size());

        log.info("scheduleGroupLTandBT, starts L4BT2.sz = " + LT4BT2.size());
        scheduleLTandTwoBT(CLT4BT2,LT33BT3,mClassId2ChildrenIds,mClassId2ClassSegments,session);
        log.info("scheduleGroupLTandBT, ends L4BT2.sz = " + LT4BT2.size());




    }

    private void scheduleGroup(List<ClassSegment> L, int session){
        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        List<ClassSegment> L222 = new ArrayList<>();
        List<ClassSegment> L23 = new ArrayList<>();
        List<ClassSegment> L24 = new ArrayList<>();
        List<ClassSegment> L33 = new ArrayList<>();
        List<ClassSegment> L234 = new ArrayList<>();
        List<ClassSegment> L333 = new ArrayList<>();
        List<ClassSegment> L44 = new ArrayList<>();
        List<ClassSegment> L45 = new ArrayList<>();
        List<ClassSegment> L444 = new ArrayList<>();
        List<ClassSegment> L555 = new ArrayList<>();

        for(Long id: mClassId2ClassSegments.keySet()){
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            String pattern = detectPattern(Li);
            log.info("scheduleGroup, class " + id + " pattern = " + pattern);
            if(pattern.equals("2,2,2")){
                for(ClassSegment cs: Li) L222.add(cs);
            }else if(pattern.equals("2,3")){
                for(ClassSegment cs: Li) L23.add(cs);
            }else if(pattern.equals("2,4")){
                for(ClassSegment cs: Li) L24.add(cs);
            }else if(pattern.equals("2,3,4")){
                for(ClassSegment cs: Li) L234.add(cs);
            }else if(pattern.equals("3,3")){
                for(ClassSegment cs: Li) L33.add(cs);
            }else if(pattern.equals("3,3,3")){
                for(ClassSegment cs: Li) L333.add(cs);
            }else if(pattern.equals("4,4")){
                for(ClassSegment cs: Li) L44.add(cs);
            }else if(pattern.equals("4,5")){
                for(ClassSegment cs: Li) L45.add(cs);
            }else if(pattern.equals("4,4,4")){
                for(ClassSegment cs: Li) L444.add(cs);
            }else if(pattern.equals("5,5,5")){
                for(ClassSegment cs: Li) L555.add(cs);
            }

        }
        log.info("scheduleClassSegments24and33 starts, L24 = " + L24.size() + " L33 = " + L33.size());
        scheduleClassSegments24and33(L24,L33,session);
        log.info("scheduleClassSegments24and33 ends, L24 = " + L24.size() + " L33 = " + L33.size());

        log.info("scheduleClassSegments33 starts, L33 = " + L33.size());
        scheduleClassSegments33(L33,session);
        log.info("scheduleClassSegments33 ends, L33 = " + L33.size());

        log.info("scheduleClassSegments222and333 starts, L222 = " + L222.size() + " L333 = " + L333.size());
        scheduleClassSegments222and333(L222,L333,session);
        log.info("scheduleClassSegments222and333 ends, L222 = " + L222.size() + " L333 = " + L333.size());

        log.info("scheduleClassSegments23 starts, L23 = " + L23.size());
        scheduleClassSegments23(L23,session);
        log.info("scheduleClassSegments23 ends, L23 = " + L23.size());

        log.info("scheduleClassSegments234 starts, L234 = " + L234.size());
        scheduleClassSegments234(L234,session);
        log.info("scheduleClassSegments234 ends, L234 = " + L234.size());

        log.info("scheduleClassSegments444 starts, L444 = " + L444.size());
        scheduleClassSegments444(L444,session);
        log.info("scheduleClassSegments444 ends, L444 = " + L444.size());

        log.info("scheduleClassSegments45 starts, L45 = " + L45.size());
        scheduleClassSegments45(L45,session);
        log.info("scheduleClassSegments45 end, L45 = " + L45.size());

    }
    @Override
    public void solve() {
        log.info("solve...START");
        solutionSlot = new HashMap<>();
        solutionRoom = new HashMap<>();
        Map<Long, List<ClassSegment>> mClassId2ClassSegments= new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
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
                log.info("solver, classId " + id + " ses = " + ses);
                if(ses == 1) morningClassIds.add(id);
                else if(ses == 2) afternoonClassIds.add(id);
            }
        }
        //String[] CH = {"CH1012","CH1015","CH1018","CH3220","CH3323","CH3224","CH3316","CH3330","CH4780",
        //"CH3400","CH3412","CH3420","CH3051","CH3061","CH2020","CH3456","CH4486","CH3452","CH3454",
        //"CH3474","CH2021"};

        Set<String> sCH  = new HashSet<>(Arrays.asList(new String[]{"CH1012","CH1015","CH1018","CH3220","CH3323","CH3224","CH3316","CH3330","CH4780",
                "CH3400","CH3412","CH3420","CH3051","CH3061","CH2020","CH3456","CH4486","CH3452","CH3454",
                "CH3474","CH2021"}));
        Set<String> sETEE  = new HashSet<>(Arrays.asList(new String[]{"ET2012","ET2022","ET2031","ET2060","ET2100","ET3262","EE2012","EE2022","EE2023",
                "EE2000"}));

        Set<String> sME  = new HashSet<>(Arrays.asList(new String[]{"ME2011","ME2201","ME2020","ME2011","ME2015","ME2040","ME2112","ME2211","ME2101",
                "ME2202","ME3190","ME3190","ME2102","ME2030","ME2203","ME4159","ME4181","ME2021","ME3123",
        "ME3124","HE2012","HE2020","TE3602","TE2020","IT2030"}));

        Set<String> sEM  = new HashSet<>(Arrays.asList(new String[]{
            "EM1010","EM1170","EM1180","EM1100","EM3105","EM3004"
        }));

        Set<String> sED  = new HashSet<>(Arrays.asList(new String[]{
                "ED3280","ED3220"
        }));

        Set<String> sSSH  = new HashSet<>(Arrays.asList(new String[]{
                "SSH1151","SSH1111","SSH1121","SSH1131","SSH1141"
        }));

        Set<String> sMI  = new HashSet<>(Arrays.asList(new String[]{
                "MI1111","MI1121","MI1131","MI1141","MI1112","MI1142","MI1113","MI1036","MI1016","MI2020",
                "MI2021","MI3180","MI1026","MI1132"
        }));


        log.info("solver, morningids.sz = " + morningClassIds.size() + " afternoonids.sz = " + afternoonClassIds.size());
        for(int session = 0; session <= 1; session++) {
            Set<Long> ids = morningClassIds;
            if(session == 1) ids = afternoonClassIds;
            List<ClassSegment> CH = new ArrayList<>();
            List<ClassSegment> ETEE = new ArrayList<>();
            List<ClassSegment> ME = new ArrayList<>();
            List<ClassSegment> EM = new ArrayList<>();
            List<ClassSegment> ED = new ArrayList<>();
            List<ClassSegment> MI = new ArrayList<>();
            List<ClassSegment> SSH = new ArrayList<>();
            for (Long id : ids) {

                for(ClassSegment cs: mClassId2ClassSegments.get(id)){
                    if(sCH.contains(cs.getCourseCode())){
                        CH.add(cs);
                    }else if(sETEE.contains(cs.getCourseCode())){
                        ETEE.add(cs);
                    }else if(sEM.contains(cs.getCourseCode())){
                        EM.add(cs);
                    }else if(sED.contains(cs.getCourseCode())){
                        ED.add(cs);
                    }else if(sME.contains(cs.getCourseCode())){
                        ME.add(cs);
                    }else if(sMI.contains(cs.getCourseCode())){
                        MI.add(cs);
                    }else if(sSSH.contains(cs.getCourseCode())){
                        SSH.add(cs);
                    }
                }

            }
            log.info("solve group CH size = " + CH.size());
            scheduleGroup(CH,session);
            log.info("solve group ME size = " + ME.size());
            scheduleGroup(ME,session);
            log.info("solve group ETEE size = " + ETEE.size());
            scheduleGroup(ETEE,session);
            log.info("solve group EM size = " + EM.size());
            scheduleGroup(EM,session);
            log.info("solve group ED size = " + ED.size());
            scheduleGroup(ED,session);
            log.info("solve group MI size = " + MI.size());
            scheduleGroupLTandBT(MI,session);
            log.info("solve group SSH size = " + SSH.size());
            scheduleGroupLTandBT(SSH,session);

        }

        /*
        List<ClassSegment> L23M = new ArrayList<>();
        List<ClassSegment> L23A = new ArrayList<>();
        List<ClassSegment> L444M = new ArrayList<>();
        List<ClassSegment> L444A = new ArrayList<>();

        List<ClassSegment> CH234M = new ArrayList<>();
        List<ClassSegment> CH234A = new ArrayList<>();

        List<ClassSegment> ME222M = new ArrayList<>();
        List<ClassSegment> ME333M = new ArrayList<>();
        List<ClassSegment> ME222A = new ArrayList<>();
        List<ClassSegment> ME333A = new ArrayList<>();
        List<ClassSegment> ME234M = new ArrayList<>();
        List<ClassSegment> ME234A = new ArrayList<>();



        // collect classes of pattern "2,3
        for(Long id: morningClassIds){
            List<ClassSegment> M = mClassId2ClassSegments.get(id);
            String pattern = detectPattern(M);
            log.info("solver, morning classId " + id + " list.sz = " + M.size() + " pattern = " + pattern);
            if(pattern.equals("2,3")){
                for(ClassSegment cs: M) L23M.add(cs);
            }else if(pattern.equals("4,4,4")){
                for(ClassSegment cs: M) L444M.add(cs);
            }else if(pattern.equals("2,3,4")){
                for(ClassSegment cs: M) if(sCH.contains(cs.getCourseCode())) CH234M.add(cs);

                for(ClassSegment cs: M) if(sME.contains(cs.getCourseCode())) ME234M.add(cs);

            }else if(pattern.equals("2,2,2")){
                for(ClassSegment cs: M) if(sME.contains(cs.getCourseCode())) ME222M.add(cs);
            }else if(pattern.equals("3,3,3")){
                for(ClassSegment cs: M) if(sME.contains(cs.getCourseCode())) ME333M.add(cs);
            }
        }
        for(Long id: afternoonClassIds){
            List<ClassSegment> M = mClassId2ClassSegments.get(id);
            String pattern = detectPattern(M);
            log.info("solver, afternoon classId " + id + " list.sz = " + M.size() + " pattern = " + pattern);
            if(pattern.equals("2,3")){
                for(ClassSegment cs: M) L23A.add(cs);
            }else if(pattern.equals("4,4,4")){
                for(ClassSegment cs: M) L444A.add(cs);
            }else if(pattern.equals("2,3,4")){
                for(ClassSegment cs: M) if(sCH.contains(cs.getCourseCode())) CH234A.add(cs);

                for(ClassSegment cs: M) if(sME.contains(cs.getCourseCode())) ME234A.add(cs);
            }else if(pattern.equals("2,2,2")){
                for(ClassSegment cs: M) if(sME.contains(cs.getCourseCode())) ME222A.add(cs);
            }else if(pattern.equals("3,3,3")){
                for(ClassSegment cs: M) if(sME.contains(cs.getCourseCode())) ME333A.add(cs);
            }
        }
        scheduleClassSegments23(L23M,0);
        scheduleClassSegments23(L23A,1);
        scheduleClassSegments444(L444M,0);
        scheduleClassSegments444(L444A,1);
        scheduleClassSegments234(CH234M,0);
        scheduleClassSegments234(CH234A,1);
        scheduleClassSegments222and333(ME222M,ME333M,0);
        scheduleClassSegments222and333(ME222A,ME333A,1);
        scheduleClassSegments234(ME234M,0);
        scheduleClassSegments234(ME234A,1);
        */
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

    }

    @Override
    public void printSolution() {

    }
}
