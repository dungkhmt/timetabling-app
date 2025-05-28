package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import io.swagger.models.auth.In;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.*;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import org.springframework.security.core.parameters.P;

import java.util.*;
@Log4j2
public class SummerSemesterSolver implements Solver {
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
    Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
    // output data structures
    List<Integer> unScheduledClassSegment;
    boolean foundSolution;
    int timeLimit;

    public SummerSemesterSolver(MapDataScheduleTimeSlotRoomWrapper W){
        this.I = W.data; this.W = W;
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
        }
        for(ModelResponseTimeTablingClass cls: classes){
            String course = cls.getModuleCode();
            if(mCourse2Classes.get(course)==null) mCourse2Classes.put(course, new ArrayList<>());
            mCourse2Classes.get(course).add(cls);
        }

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
    private boolean  assignTimeSlot(ClassSegment cs, int timeSlot){
        solutionSlot.put(cs.getId(),timeSlot);
        return true;
    }
    private boolean  assignTimeSlotRoom(ClassSegment cs, int timeSlot, int room){
        log.info("assignTimeSlotRoom start (cs.getId() " + cs.getId() + " timeSlot " + timeSlot + " room " + room + ")") ;
        //solutionSlot[csi] = timeSlot;
        //solutionRoom[csi] = room;
        if(solutionRoom.get(cs.getId())!=null){
            log.info("assignTimeSlotRoom, BUG??? class-segment id = " + cs.getId() + " classId = " + cs.getClassId() + " course " + cs.getCourseCode() + " was assign to room " + room);
            return false;
        }
        solutionSlot.put(cs.getId(),timeSlot);
        solutionRoom.put(cs.getId(),room);
        // update room occupation
        //ClassSegment cs = classSegments.get(csi);
        String os = "";
        for(int r: I.getRoomOccupations()[room]) os = os + r + ",";
        //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room + " occupied by slots " + os);
        log.info("assignTimeSlotRoom[" + cs.getId() + "], classId " + cs.getClassId() + " course " + cs.getCourseCode() + " assigned to room used index = " + room + " name = " + W.mIndex2Room.get(room).getClassroom() + " capacity " + W.mIndex2Room.get(room).getQuantityMax());
        for(int s = 0; s <= cs.getDuration()-1; s++){
            int sl = timeSlot + s;
            I.getRoomOccupations()[room].add(sl);
            os = os + sl + ",";
            //log.info("assignTimeSlotRoom[" + cs.getId() + "], time-slot = " + timeSlot + ", room = " + room
            // + " roomOccupation[" + room + "].add(" + sl + ") -> " + os);
        }
        //printRoomsUsed();
        foundSolution = true;
        return true;
    }
    public void printSlotOccupationOfRoom(ClassSegment csi, int timeSlot, int room){
        //printRoomsUsed();
        String msg = "";
        for(int s: I.getRoomOccupations()[room]){
            for(int j = 0; j < csi.getDuration(); j++) {
                int sl = timeSlot + j;// check all time-slot of the duration
                if (s == sl) {
                    log.info("checkTimeSlotRoom(" + csi.getId() + ", duration " + csi.getDuration() + " timeSlot " + timeSlot + ", room = " + room + ")" +
                                " --> return false as slot " + s + " occupied");
                    return;
                }
            }
        }
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

    private boolean scheduleClassSegments444(List<ClassSegment> L, int session, boolean assignRoom){
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

            if(assignRoom) {
                for (int room : cs1.getDomainRooms()) {
                    if (checkTimeSlotRoom(cs1, s1, room) && checkTimeSlotRoom(cs2, s2, room)
                            && checkTimeSlotRoom(cs3, s3, room)) {
                        if (!assignTimeSlotRoom(cs1, s1, room)) return false;
                        if (!assignTimeSlotRoom(cs2, s2, room)) return false;
                        if (!assignTimeSlotRoom(cs3, s3, room)) return false;
                        scheduled.add(cs1);
                        scheduled.add(cs2);
                        scheduled.add(cs3);
                        break;
                    }
                }
            }else{// not assign room, only assign time-slot
                if (!assignTimeSlot(cs1, s1)) return false;
                if (!assignTimeSlot(cs2, s2)) return false;
                if (!assignTimeSlot(cs3, s3)) return false;
                scheduled.add(cs1);
                scheduled.add(cs2);
                scheduled.add(cs3);
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
        return true;
    }
    private boolean scheduleClassSegments45(List<ClassSegment> L, int session, boolean assignRoom){
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
            if(assignRoom) {
                for (int room : cs1.getDomainRooms()) {
                    if (checkTimeSlotRoom(cs1, s1, room) && checkTimeSlotRoom(cs2, s2, room)
                    ) {
                        if (!assignTimeSlotRoom(cs1, s1, room)) return false;
                        if (!assignTimeSlotRoom(cs2, s2, room)) return false;
                        scheduled.add(cs1);
                        scheduled.add(cs2);
                        break;
                    }
                }
            }else{
                if (!assignTimeSlot(cs1, s1)) return false;
                if (!assignTimeSlot(cs2, s2)) return false;
                scheduled.add(cs1);
                scheduled.add(cs2);
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
        return true;
    }
    private boolean scheduleClassSegments3(List<ClassSegment> L, int session, boolean assignRoom){
        log.info("scheduleClassSegments3, session = " + session + " L.sz = " + L.size());
        int[][] C = {
                {5,2},
                {6,2}

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
            int nbSegments = C[j].length/2;
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            if(Li.size() != C[j].length/2) continue;
            for(int k = 0; k < nbSegments;k++){
                ClassSegment cs = Li.get(k);
                int day = C[j][0]; int slot = C[j][1];
                int s = new DaySessionSlot(day,session,slot).hash();
                if(assignRoom) {
                    for (int room : cs.getDomainRooms()) {
                        if (checkTimeSlotRoom(cs, s, room)) {
                            if (!assignTimeSlotRoom(cs, s, room)) return false;
                            scheduled.add(cs);
                            break;
                        }
                    }
                }else{
                    if (!assignTimeSlot(cs, s)) return false;
                    scheduled.add(cs);
                }
            }
            /*
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

             */
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
        return true;
    }

    private boolean scheduleClassSegments234(List<ClassSegment> L, int session, boolean assignRoom){
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
        Set<ClassSegment> scheduled = new HashSet<>();
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

            if(assignRoom) {
                for (int room : cs1.getDomainRooms()) {
                    if (checkTimeSlotRoom(cs1, s1, room) && checkTimeSlotRoom(cs2, s2, room)
                            && checkTimeSlotRoom(cs3, s3, room)) {
                        if (!assignTimeSlotRoom(cs1, s1, room)) return false;
                        if (!assignTimeSlotRoom(cs2, s2, room)) return false;
                        if (!assignTimeSlotRoom(cs3, s3, room)) return false;
                        scheduled.add(cs1);
                        scheduled.add(cs2);
                        scheduled.add(cs3);
                        break;
                    }
                }
            }else{// only assign time-slot
                if (!assignTimeSlot(cs1, s1)) return false;
                if (!assignTimeSlot(cs2, s2)) return false;
                if (!assignTimeSlot(cs3, s3)) return false;
                scheduled.add(cs1);
                scheduled.add(cs2);
                scheduled.add(cs3);

            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
        return true;
    }
    private boolean scheduleClassSegments24and33(List<ClassSegment> L24, List<ClassSegment> L33,
                                                 int session, boolean assignRoom){
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
        log.info("scheduleClassSegments24and33, L24 = " + L24.size() + " L33 = " + L33.size());
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
            if(assignRoom) {
                for (int room : cs241.getDomainRooms()) {
                    if (checkTimeSlotRoom(cs241, s241, room)
                            & checkTimeSlotRoom(cs242, s242, room)
                            & checkTimeSlotRoom(cs331, s331, room)
                            & checkTimeSlotRoom(cs332, s332, room)
                    ) {

                        if(!assignTimeSlotRoom(cs241,s241,room)) return false;
                        if(!assignTimeSlotRoom(cs242,s242,room)) return false;
                        if(!assignTimeSlotRoom(cs331,s331,room)) return false;
                        if(!assignTimeSlotRoom(cs332,s332,room)) return false;
                        scheduled24.add(cs241);
                        scheduled24.add(cs242);
                        scheduled33.add(cs331);
                        scheduled33.add(cs332);
                        break;
                    }
                }
            }else{
                if (!assignTimeSlot(cs241, s241)) return false;
                if (!assignTimeSlot(cs242, s242)) return false;
                if (!assignTimeSlot(cs331, s331)) return false;
                if (!assignTimeSlot(cs332, s332)) return false;
                scheduled24.add(cs241);
                scheduled24.add(cs242);
                scheduled33.add(cs331);
                scheduled33.add(cs332);
            }
            i2++;i3++;
        }
        for(ClassSegment cs: scheduled33) L33.remove(cs);
        for(ClassSegment cs: scheduled24) L24.remove(cs);
        return true;
    }

    private boolean scheduleLTBTClassSegments(int[][][] C, List<ClassSegment> L,  int session, boolean assignRoom){
        log.info("scheduleLTBTClassSegments, L.sz = " + L.size() + " session = " + session);

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
            int nbSegments = C[j].length;
            List<ClassSegment> Li = mClassId2ClassSegments.get(id);
            if(Li.size() != nbSegments) continue;
            log.info("scheduleLTBTClassSegments, class " + Li.get(0).getClassId() + " course = " + Li.get(0).getCourseCode());
            //List<Integer> selectedRooms = new ArrayList<>();
            if(assignRoom) {
                int sel_room = -1;
                for (int r : Li.get(0).getDomainRooms()) {
                    boolean ok = true;
                    for (int k = 0; k < nbSegments; k++) {
                        ClassSegment cs = Li.get(k);
                        int duration = C[j][k][0];
                        int day = C[j][k][1];
                        int slot = C[j][k][2];
                        int s = new DaySessionSlot(day, session, slot).hash();
                        if (!checkTimeSlotRoom(cs, s, r)) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        sel_room = r;
                    }
                }
                if (sel_room != -1) {
                    for (int k = 0; k < nbSegments; k++) {
                        ClassSegment cs = Li.get(k);
                        int duration = C[j][k][0];
                        int day = C[j][k][1];
                        int slot = C[j][k][2];
                        int s = new DaySessionSlot(day, session, slot).hash();
                        if (!assignTimeSlotRoom(cs, s, sel_room)) return false;
                        scheduled.add(cs);
                    }
                } else {
                    log.info("scheduleLTBTClassSegments, class " + Li.get(0).getClassId() + " course = " + Li.get(0).getCourseCode() + " cannot find any room among " + Li.get(0).getDomainRooms().size() + " !!!");
                    printRoomsUsed();

                }
            }else{
                for (int k = 0; k < nbSegments; k++) {
                    ClassSegment cs = Li.get(k);
                    int duration = C[j][k][0];
                    int day = C[j][k][1];
                    int slot = C[j][k][2];
                    int s = new DaySessionSlot(day, session, slot).hash();
                    if (!assignTimeSlot(cs, s)) return false;
                    scheduled.add(cs);
                }
            }
            /*
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

             */
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
        return true;
    }
    private boolean scheduleClassSegments222and333(List<ClassSegment> L222, List<ClassSegment> L333,
                                                   int session, boolean assignRoom){
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
            if(assignRoom) {
                for (int room : cs21.getDomainRooms()) {
                    if (checkTimeSlotRoom(cs21, s21, room)
                            & checkTimeSlotRoom(cs22, s22, room)
                            & checkTimeSlotRoom(cs23, s23, room)
                            & checkTimeSlotRoom(cs31, s31, room)
                            & checkTimeSlotRoom(cs32, s32, room)
                            & checkTimeSlotRoom(cs33, s33, room)) {
                        if (!assignTimeSlotRoom(cs21, s21, room)) return false;
                        if (!assignTimeSlotRoom(cs22, s22, room)) return false;
                        if (!assignTimeSlotRoom(cs23, s23, room)) return false;
                        if (!assignTimeSlotRoom(cs31, s31, room)) return false;
                        if (!assignTimeSlotRoom(cs32, s32, room)) return false;
                        if (!assignTimeSlotRoom(cs33, s33, room)) return false;
                        scheduled222.add(cs21);
                        scheduled222.add(cs22);
                        scheduled222.add(cs23);
                        scheduled333.add(cs31);
                        scheduled333.add(cs32);
                        scheduled333.add(cs33);

                        break;
                    }
                }
            }else{
                if (!assignTimeSlot(cs21, s21)) return false;
                if (!assignTimeSlot(cs22, s22)) return false;
                if (!assignTimeSlot(cs23, s23)) return false;
                if (!assignTimeSlot(cs31, s31)) return false;
                if (!assignTimeSlot(cs32, s32)) return false;
                if (!assignTimeSlot(cs33, s33)) return false;
                scheduled222.add(cs21);
                scheduled222.add(cs22);
                scheduled222.add(cs23);
                scheduled333.add(cs31);
                scheduled333.add(cs32);
                scheduled333.add(cs33);
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
        return true;
    }
    private boolean scheduleClassSegments23(List<ClassSegment> L, int session, boolean assignRoom){
        /*
        int[][] C23 = {
                {2,1,4,3},// (day 2 - slot 1) and (day 4 - slot 3)
                {5,1,2,3},// (day 5 - slot 1) and (day 2 - slot 3)
                {3,1,5,3},// (day 3 -slot 1) and (day 5 - slot 3)
                {6,1,3,3},// (day 5 - slot 1) and (day 3 - slot 3)
                {4,1,6,3},// (day 4 - slot 1) and (day 6 - slot 3)
        };
         */
        int[][] C23 = {
                {2,1,4,3},// (day 2 - slot 1) and (day 4 - slot 3)
                {4,1,2,3},// (day 5 - slot 1) and (day 2 - slot 3)
                {3,1,5,3},// (day 3 -slot 1) and (day 5 - slot 3)
                {5,1,3,3},// (day 5 - slot 1) and (day 3 - slot 3)
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
        List<List<Long>> clusterClassId = new ArrayList<>();
        for(Long id: a){
            ModelResponseTimeTablingClass cls = mClassId2Class.get(id);
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
            log.info("scheduleClassSegments23, classCode " + cls.getClassCode() + ", j = " + j + " day1 = " + day1 + ", slot1 = " + slot1 + ", day2 = " + day2 + ", slot2 = " + slot2);
            int s1 = new DaySessionSlot(day1,session,slot1).hash();
            int s2 = new DaySessionSlot(day2,session,slot2).hash();
            assignTimeSlot(cs1,s1); scheduled.add(cs1);
            assignTimeSlot(cs2,s2); scheduled.add(cs2);
            boolean addNewCluster = false;
            if(clusterClassId.size() > 0){
                List<Long> last = clusterClassId.get(clusterClassId.size()-1);
                if(last.size() == 1){
                    Long lastId = last.get(0);
                    ModelResponseTimeTablingClass lastCls = mClassId2Class.get(lastId);
                    if(lastCls != null && lastCls.getModuleCode().equals(cls.getModuleCode())){
                        // add the second class to the cluster as it has the same courseCode with lastCls
                        addNewCluster = true;
                    }
                }
            }
            if(addNewCluster){
                List<Long> last = clusterClassId.get(clusterClassId.size()-1);
                last.add(id);
            }else{
                List<Long> newCluster = new ArrayList<>();
                newCluster.add(id);
                clusterClassId.add(newCluster);
            }
            /*
            for(int room: cs1.getDomainRooms()){
                if(checkTimeSlotRoom(cs1,s1,room) && checkTimeSlotRoom(cs2,s2,room)){
                    if(!assignTimeSlotRoom(cs1,s1,room)) return false;
                    if(!assignTimeSlotRoom(cs2,s2,room)) return false;
                    scheduled.add(cs1); scheduled.add(cs2);
                    break;
                }
            }
            */

        }

        // iterate over classIds of clusterIds
        // two classes of the same cluster are assign with the same room
        if(assignRoom) {
            for (List<Long> cl : clusterClassId) {
                if (cl.size() == 1) {
                    Long clsId = cl.get(0);
                    List<ClassSegment> Li = mClassId2ClassSegments.get(clsId);
                    ClassSegment cs1 = Li.get(0);
                    ClassSegment cs2 = Li.get(1);
                    int s1 = solutionSlot.get(cs1.getId());
                    int s2 = solutionSlot.get(cs2.getId());
                    for (int room : cs1.getDomainRooms()) {
                        if (checkTimeSlotRoom(cs1, s1, room) && checkTimeSlotRoom(cs2, s2, room)) {
                            if (!assignTimeSlotRoom(cs1, s1, room)) return false;
                            if (!assignTimeSlotRoom(cs2, s2, room)) return false;
                            scheduled.add(cs1);
                            scheduled.add(cs2);
                            break;
                        }
                    }
                } else if (cl.size() == 2) {
                    Long id1 = cl.get(0);
                    Long id2 = cl.get(1);
                    List<ClassSegment> Li1 = mClassId2ClassSegments.get(id1);
                    List<ClassSegment> Li2 = mClassId2ClassSegments.get(id2);

                    ClassSegment cs11 = Li1.get(0);
                    ClassSegment cs12 = Li1.get(1);
                    ClassSegment cs21 = Li2.get(0);
                    ClassSegment cs22 = Li2.get(1);

                    int s11 = solutionSlot.get(cs11.getId());
                    int s12 = solutionSlot.get(cs12.getId());
                    int s21 = solutionSlot.get(cs21.getId());
                    int s22 = solutionSlot.get(cs22.getId());
                    List<Integer> D = Util.intersectList2(cs11.getDomainRooms(), cs22.getDomainRooms());
                    for (int room : D) {
                        if (checkTimeSlotRoom(cs11, s11, room) && checkTimeSlotRoom(cs12, s12, room)
                                && checkTimeSlotRoom(cs21, s21, room) && checkTimeSlotRoom(cs22, s22, room)) {
                            if (!assignTimeSlotRoom(cs11, s11, room)) return false;
                            if (!assignTimeSlotRoom(cs12, s12, room)) return false;
                            if (!assignTimeSlotRoom(cs21, s21, room)) return false;
                            if (!assignTimeSlotRoom(cs22, s22, room)) return false;

                            scheduled.add(cs11);
                            scheduled.add(cs12);
                            scheduled.add(cs21);
                            scheduled.add(cs22);

                            break;
                        }
                    }
                } else {
                    log.info("scheduleClassSegments23, cluster contains more than 2 classes -> BUG????");
                    return false;
                }
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
        return true;
    }
    private boolean scheduleClassSegments33(List<ClassSegment> L, int session, boolean assignRoom){
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
            log.info("scheduleClassSegments33, classId " + Li.get(0).getClassId() + " course " + Li.get(0).getCourseCode() + " domain rooms = " + Li.get(0).getDomainRooms().size());
            ClassSegment cs1 = Li.get(0); ClassSegment cs2 = Li.get(1);
            if(cs1.getDuration() > cs2.getDuration()){
                ClassSegment tmp = cs1; cs1 = cs2; cs2 = tmp;
            }
            int j = idx%C.length;
            int day1 = C[j][0]; int slot1 = C[j][1];
            int day2 = C[j][2]; int slot2 = C[j][3];
            int s1 = new DaySessionSlot(day1,session,slot1).hash();
            int s2 = new DaySessionSlot(day2,session,slot2).hash();
            boolean ok = false;
            if(assignRoom) {
                for (int room : cs1.getDomainRooms()) {
                    if (checkTimeSlotRoom(cs1, s1, room) && checkTimeSlotRoom(cs2, s2, room)) {
                        if (!assignTimeSlotRoom(cs1, s1, room)) return false;
                        if (!assignTimeSlotRoom(cs2, s2, room)) return false;
                        scheduled.add(cs1);
                        scheduled.add(cs2);
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    log.info("scheduleClassSegments33, cannot find any room for classId " + Li.get(0).getClassId() + " course " + Li.get(0).getCourseCode() + " domain rooms = " + Li.get(0).getDomainRooms().size());

                }
            }else{
                if (!assignTimeSlot(cs1, s1)) return false;
                if (!assignTimeSlot(cs2, s2)) return false;
                scheduled.add(cs1);
                scheduled.add(cs2);
            }
        }
        for(ClassSegment cs: scheduled) L.remove(cs);
        return true;
    }
    private String getClassLTBTInfo(Long id, Map<Long, List<Long>> mClassId2ChildrenIds, Map<Long, List<ClassSegment>> mClassId2ClassSegment){
        List<ClassSegment> cls = mClassId2ClassSegment.get(id);
        String s = "LT classId " + id + ": ";
        for(ClassSegment cs: cls) {
            s = s + "[cs.id " + cs.getId() + ", " + cs.getCourseCode() + ", " + cs.getDuration() + "], ";
        }
        s = s + " children: ";
        for(Long idi: mClassId2ChildrenIds.get(id)){
            s = s + "BT classId " + idi + ": ";
            for(ClassSegment csi: mClassId2ClassSegment.get(idi)){
                s = s + "[cs.id " + csi.getId() + ", " + csi.getCourseCode() + ", " + csi.getDuration() + "], ";
            }
        }
        return s;
    }
    private boolean scheduleLTandTwoBT(int[][][] C, List<Long> ids, Map<Long, List<Long>> mClassId2ChildrenIds, Map<Long,
            List<ClassSegment>> mClassId2ClassSegments, int session, List<ClassSegment> scheduledClassSegment, boolean assignRoom){
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
            log.info("scheduleLTandTwoBT, consider class LT id = " + id + " detail " + getClassLTBTInfo(id,mClassId2ChildrenIds,mClassId2ClassSegments));
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
            if(assignRoom) {
                boolean LTOK = false;
                log.info("scheduleLTandTwoBT, consider class LT id = " + id + " domain-rooms.sz = " + LT.get(0).getDomainRooms().size());
                for (int r : LT.get(0).getDomainRooms()) {
                    Classroom clr = W.mIndex2Room.get(r);
                    boolean ok = true;
                    for (int k = 0; k < nbLTSegments; k++) {
                        ClassSegment csk = LT.get(k);
                        if (!checkTimeSlotRoom(csk, LTSlots.get(k), r)) {
                            //log.info("scheduleLTandTwoBT, checkTimeSlotRoom(" + csk.getClassId() + "," + csk.getCourseCode() +
                            //        ", duration " + csk.getDuration() + " slot " + LTSlots.get(k) + " room[" +
                            //                r + "] " + clr.getClassroom() + ", with cap " + clr.getQuantityMax() + " -> FAILED ");
                            //printSlotOccupationOfRoom(csk, LTSlots.get(k), r);
                            ok = false;
                        }
                    }
                    if (ok) {
                        for (int k = 0; k < nbLTSegments; k++) {
                            if (!assignTimeSlotRoom(LT.get(k), LTSlots.get(k), r)) return false;
                            scheduledClassSegment.add(LT.get(k));
                            scheduled.add(id);
                        }
                        LTOK = true;
                        break;
                    }
                }
                if (!LTOK) {
                    log.info("scheduleLTandTwoBT, cannot assign LT id = " + id + ": detail: " + getClassLTBTInfo(id, mClassId2ChildrenIds, mClassId2ClassSegments));
                }
            }else{
                for (int k = 0; k < nbLTSegments; k++) {
                    if (!assignTimeSlot(LT.get(k), LTSlots.get(k))) return false;
                    scheduledClassSegment.add(LT.get(k));
                    scheduled.add(id);
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
                String tmp = "";
                for(ClassSegment csj: BT) tmp = tmp + " [csj[" + csj.getId() + "], classId " + csj.getClassId() + "] ";
                log.info("scheduleLTandTwoBT, btId = " + btId + tmp);
            }
            ClassSegmentsSlotsMatching matcher = new ClassSegmentsSlotsMatching(BTs,slots,session);
            if(matcher.solve()){
                log.info("scheduleLTandTwoBT, matcher.solve start to assign BT");
                for(List<ClassSegment> BT: BTs){
                    String sBT = "";
                    for(ClassSegment cs: BT) sBT = sBT + cs.str() + " ";
                    log.info("scheduleLTandTwoBT, matcher.solve start sBT = " + sBT);
                }
                for(List<ClassSegment> BT: BTs){
                    if(assignRoom) {
                        for (int r : BT.get(0).getDomainRooms()) {
                            boolean ok = true;
                            for (ClassSegment cs : BT) {
                                int slot = matcher.mClassSegment2Slot.get(cs);
                                if (!checkTimeSlotRoom(cs, slot, r)) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok) {
                                for (ClassSegment cs : BT) {
                                    int slot = matcher.mClassSegment2Slot.get(cs);
                                    if (!assignTimeSlotRoom(cs, slot, r)) {
                                        log.info("scheduleLTandTwoBT, matcher.solve start to assign BT return FALSE");
                                        return false;
                                    }
                                    scheduledClassSegment.add(cs);
                                }
                                break;
                            } else {
                                //log.info("scheduleLTandTwoBT, cannot assign BT for " + getClassLTBTInfo(id,mClassId2ChildrenIds,mClassId2ClassSegments));
                            }
                        }
                    }else{// do not assign room, onyl assign slot
                        for (ClassSegment cs : BT) {
                            int slot = matcher.mClassSegment2Slot.get(cs);
                            if (!assignTimeSlot(cs, slot)) {
                                log.info("scheduleLTandTwoBT, matcher.solve start to assign BT return FALSE");
                                return false;
                            }
                            scheduledClassSegment.add(cs);
                        }
                    }
                }
                log.info("scheduleLTandTwoBT, matcher.solve end to assign BT");
            }else{
                log.info("scheduleLTandTwoBT, matcher.solve FAILED, cannot assign BT for " + getClassLTBTInfo(id,mClassId2ChildrenIds,mClassId2ClassSegments));
            }

        }
        for(Long id: scheduled) ids.remove(id);
        return true;
    }

    private boolean scheduleGroupLTandBT(List<ClassSegment> L, int session, boolean assignRoom){
        for(ClassSegment cs: L){
            //log.info("scheduleGroupLTandBT, cs = course " + cs.getCourseCode() + " classId " + cs.getClassId() + " type " + cs.getType() + " duration " + cs.getDuration());
        }

        Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(ClassSegment cs: L){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
            log.info("scheduleGroupLTandBT, classId = " + cs.getClassId() + " -> add class-segment " + cs.getId());
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
        List<Long> LT24BT3 = new ArrayList<>();

        List<Long> LT4BT2 = new ArrayList<>();
        //for(Long id: mClassId2ClassSegments.keySet()){
        for(Long id: mClassId2ChildrenIds.keySet()){
            List<ClassSegment> CLS = mClassId2ClassSegments.get(id);
            if(CLS == null || CLS.size() == 0){
                log.info("scheduleGroupLTandBT, classId = " + id + " do not have class-segments???");
                continue; // inconsistent in DB, classId id does not exist
            }
            String course = CLS.get(0).getCourseCode();
            String pattern = detectPattern(CLS);
            String childrenPattern = detectePatternChildren(mClassId2ChildrenIds.get(id),mClassId2ClassSegments);
            log.info("scheduleGroupLTandBT, course " + course + " classId = " + id + " -> pattern = " + pattern + " childrenPattern = " + childrenPattern);

            if(pattern.equals("4,5")){
                if(childrenPattern.equals("2,4-3,3")) {
                    LT45BT2433.add(id);
                    log.info("scheduleGroupLTandBT, LT45BT2433.add(" + id + ")");
                }
            }else if(pattern.equals("3,3")){
                if(childrenPattern.equals("2,4-3,3")) {
                    LT33BT2433.add(id);
                    log.info("scheduleGroupLTandBT, LT33BT2433.add(" + id + ")");
                }else if(childrenPattern.equals("3-3-3")){
                    LT33BT3.add(id);
                    log.info("scheduleGroupLTandBT, LT33BT3.add(" + id + ")");

                }
            }else if(pattern.equals("4")){
                if(childrenPattern.equals("2-2-2")){
                    LT4BT2.add(id);
                    log.info("scheduleGroupLTandBT, LT4BT2.add(" + id + ")");
                }
            }else if(pattern.equals("2,4")){
                if(childrenPattern.equals("3-3-3")){
                    LT24BT3.add(id);
                    log.info("scheduleGroupLTandBT, LT24BT3.add(" + id + ")");
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
        int[][][] CLT24BT3 = {
                {
                        {2,3,4,4,5,1},
                        {3,3,1},
                        {3,4,2},
                        {3,6,2}
                },
                {
                        {4,4,1,2,6,1},
                        {3,3,2},
                        {3,5,2},
                        {3,6,3}
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

        List<ClassSegment> scheduledClassSegments = new ArrayList<>();


        log.info("scheduleGroupLTandBT, starts L45BT2433.sz = " + LT45BT2433.size());
        if(!scheduleLTandTwoBT(CLT45BT2433,LT45BT2433,mClassId2ChildrenIds,mClassId2ClassSegments,session,scheduledClassSegments,assignRoom)) return false;
        log.info("scheduleGroupLTandBT, ends L45BT2433.sz = " + LT45BT2433.size());

        log.info("scheduleGroupLTandBT, starts L33BT3.sz = " + LT33BT3.size());
        if(!scheduleLTandTwoBT(CLT33BT3,LT33BT3,mClassId2ChildrenIds,mClassId2ClassSegments,session,scheduledClassSegments,assignRoom)) return false;
        log.info("scheduleGroupLTandBT, ends L33BT3.sz = " + LT33BT3.size());

        log.info("scheduleGroupLTandBT, starts L24BT3.sz = " + LT24BT3.size());
        if(!scheduleLTandTwoBT(CLT24BT3,LT24BT3,mClassId2ChildrenIds,mClassId2ClassSegments,session,scheduledClassSegments,assignRoom)) return false;
        log.info("scheduleGroupLTandBT, ends L24BT3.sz = " + LT24BT3.size());

        log.info("scheduleGroupLTandBT, starts L33BT2433.sz = " + LT33BT2433.size());
        if(!scheduleLTandTwoBT(CLT33BT2433,LT33BT2433,mClassId2ChildrenIds,mClassId2ClassSegments,session,scheduledClassSegments,assignRoom)) return false;
        log.info("scheduleGroupLTandBT, ends L33BT2433.sz = " + LT33BT2433.size());

        log.info("scheduleGroupLTandBT, starts L4BT2.sz = " + LT4BT2.size());
        if(!scheduleLTandTwoBT(CLT4BT2,LT4BT2,mClassId2ChildrenIds,mClassId2ClassSegments,session,scheduledClassSegments,assignRoom)) return false;
        log.info("scheduleGroupLTandBT, ends L4BT2.sz = " + LT4BT2.size());

        for(ClassSegment cs: scheduledClassSegments)
            L.remove(cs);

        return true;
    }

    private boolean scheduleGroup(List<ClassSegment> L, int session, boolean assignRoom){
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
        List<ClassSegment> L3 = new ArrayList<>();

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
            }else if(pattern.equals("3")){
                for(ClassSegment cs: Li) L3.add(cs);
            }

        }
        log.info("scheduleClassSegments24and33 starts, L24 = " + L24.size() + " L33 = " + L33.size());
        if(!scheduleClassSegments24and33(L24,L33,session,assignRoom)) return false;
        log.info("scheduleClassSegments24and33 ends, L24 = " + L24.size() + " L33 = " + L33.size());

        log.info("scheduleClassSegments33 starts, L33 = " + L33.size());
        if(!scheduleClassSegments33(L33,session,assignRoom)) return false;
        log.info("scheduleClassSegments33 ends, L33 = " + L33.size());

        log.info("scheduleClassSegments222and333 starts, L222 = " + L222.size() + " L333 = " + L333.size());
        if(!scheduleClassSegments222and333(L222,L333,session,assignRoom)) return false;
        log.info("scheduleClassSegments222and333 ends, L222 = " + L222.size() + " L333 = " + L333.size());

        log.info("scheduleClassSegments23 starts, L23 = " + L23.size());
        if(!scheduleClassSegments23(L23,session,assignRoom)) return false;
        log.info("scheduleClassSegments23 ends, L23 = " + L23.size());

        log.info("scheduleClassSegments234 starts, L234 = " + L234.size());
        if(!scheduleClassSegments234(L234,session,assignRoom)) return false;
        log.info("scheduleClassSegments234 ends, L234 = " + L234.size());

        log.info("scheduleClassSegments444 starts, L444 = " + L444.size());
        if(!scheduleClassSegments444(L444,session,assignRoom)) return false;
        log.info("scheduleClassSegments444 ends, L444 = " + L444.size());

        log.info("scheduleClassSegments45 starts, L45 = " + L45.size());
        if(!scheduleClassSegments45(L45,session,assignRoom)) return false;
        log.info("scheduleClassSegments45 end, L45 = " + L45.size());

        log.info("scheduleClassSegments3 starts, L3 = " + L3.size());
        if(!scheduleClassSegments3(L3,session,assignRoom)) return false;
        log.info("scheduleClassSegments3 end, L3 = " + L3.size());

        log.info("scheduleLTBTClassSegments starts, L333 = " + L333.size());
        int[][][] C333 = {
                {{3,2,2},{3,4,2},{3,6,2}}
        };
        if(!scheduleLTBTClassSegments(C333,L333,session,assignRoom)) return false;
        log.info("scheduleLTBTClassSegments ends, L333 = " + L333.size());

        int[][][] C24 = {
                {{2,2,1},{4,4,1}},
                {{2,3,1},{4,5,1}}
        };

        if(!scheduleLTBTClassSegments(C24,L24,session,assignRoom)) return false;
        log.info("scheduleLTBTClassSegments ends, L24 = " + L24.size());
        return true;
    }
    private boolean overlapTimeTableClassSegments(ClassSegment cs1, ClassSegment cs2){
        if(solutionSlot.get(cs1.getId())==null || solutionSlot.get(cs2.getId())==null)
            return false;
        int s1 = solutionSlot.get(cs1.getId());
        int s2 = solutionSlot.get(cs2.getId());
        if(Util.overLap(s1,cs1.getDuration(),s2,cs2.getDuration())) return true;
        return false;
    }
    private boolean overlapTimeTableClasses(Long classId1, Long classId2){
        List<ClassSegment> CS1 = mClassId2ClassSegments.get(classId1);
        List<ClassSegment> CS2 = mClassId2ClassSegments.get(classId2);
        if(CS1 == null || CS2 == null){
            log.info("overlapTimeTableClasses, CS1 or CS2 NULL");
        }
        for(ClassSegment cs1: CS1){
            for(ClassSegment cs2: CS2){
                if(overlapTimeTableClassSegments(cs1,cs2)) return true;
            }
        }
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
    private void reAssignRooms(){
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
            for(Long id1: mClassId2ClassSegments.keySet())if(id < id1){
                if(overlapTimeTableClasses(id,id1)){
                    mClassId2ConflictClassIds.get(id).add(id1);
                    mClassId2ConflictClassIds.get(id1).add(id);
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
                boolean ok = true;
                for(Long id: mClassId2ConflictClassIds.get(cls.getId())){
                    if(mClassId2Room.get(id)!=null && mClassId2Room.get(id)==r){
                        ok = false; break;// has conflict class assigned to the same room r
                    }
                }
                if(ok){
                    mClassId2Room.put(cls.getId(),r);
                    log.info("reAssignRoom, assign class[" + i + "/" + classes.size() + "] " + cls.getId() + " to room " + r);
                    if(mClassId2ConsecutiveClassId.get(cls.getId())!=null){
                        Long id1 = mClassId2ConsecutiveClassId.get(cls.getId());
                        mClassId2Room.put(id1,r);
                        log.info("reAssignRoom, co-assign class[" + i + "/" + classes.size() + "] " + id1 + " to room " + r);

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
    @Override
    public void solve() {
        log.info("solve...START, nbClassSegments = " + I.getClassSegments().size());
        solutionSlot = new HashMap<>();
        solutionRoom = new HashMap<>();
        Map<Long, List<ClassSegment>> mClassId2ClassSegments= new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            log.info("solve... cs = " + cs.getClassId() + " course " + cs.getCourseCode() + " type = " + cs.getType() + " duration " + cs.getDuration() + " domain-rooms = " + cs.getDomainRooms().size());
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
                "SSH1151","SSH1111","SSH1121","SSH1131","SSH1141"
        }));

        Set<String> sMI  = new HashSet<>(Arrays.asList(new String[]{
                "MI1111","MI1121","MI1131","MI1141","MI1112","MI1142","MI1113","MI1036","MI1016","MI2020",
                "MI2021","MI3180","MI1026","MI1132","MI1143","MI2010","MI2110","MI1122","MI1133",
                "MI1144","MI1114","MI1124","MI1046","MI1134"
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
                    log.info("solver, id = " + id + " class-segment " + cs.toString());
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
                        log.info("solver, id = " + id + " class-segment " + cs.toString() + " MI.add(" + cs.getCourseCode() + ") -> sz = " + MI[session].size());
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
            if(!scheduleGroupLTandBT(MI[session],session,false)) return;
            log.info("After solve scheduleGroupLTandBT MI remain size = " + MI[session].size());
            log.info("solve scheduleGroupLTandBT SSH size = " + SSH[session].size());
            if(!scheduleGroupLTandBT(SSH[session],session,false)) return;

            log.info("solve group MI size = " + MI[session].size());
            if(!scheduleGroup(MI[session],session,false)) return;
            log.info("After solve group MI remains size = " + MI[session].size());

            log.info("solve group MSSH size = " + SSH[session].size());
            if(!scheduleGroup(SSH[session],session,false)) return;

        }
        reAssignRooms();
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
