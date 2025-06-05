package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.*;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;

import java.util.*;
@Log4j2
public class SummerSemesterSolverVersion2 implements Solver {
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
    Map<Long, Long> mClassId2MatchedClassId = new HashMap<>();

    int[][] occupation; // occupation[day][s] is number of classes schedule at slot s

    // output data structures
    List<Integer> unScheduledClassSegment;
    boolean foundSolution;
    int timeLimit;
    public SummerSemesterSolverVersion2(MapDataScheduleTimeSlotRoomWrapper W){
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
        occupation = new int[Constant.daysPerWeek+2][Constant.slotPerCrew+1];
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
    private boolean isScheduled(Long classId){
        List<ClassSegment> CS = mClassId2ClassSegments.get(classId);
        if(CS != null && CS.size()> 0){
            ClassSegment cs = CS.get(0);
            if(solutionSlot.get(cs.getId())!=null) return true;
        }
        return false;
    }
    class MatchScore{
        int score;
        Map<ClassSegment, ClassSegment> m;

        public MatchScore(int score, Map<ClassSegment, ClassSegment> m) {
            this.score = score;
            this.m = m;
        }
    }
    private MatchScore matchScore(Long classId1, Long classId2, int nbSlotPerSession){
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
    private int findConsecutiveSlot(ClassSegment cs, ClassSegment matchClassSegment){
        if(solutionSlot.get(matchClassSegment.getId())==null){
            log.info("findConsecutiveSlot, BUG???"); return -1;
        }
        int sl = solutionSlot.get(matchClassSegment.getId());
        DaySessionSlot dss = new DaySessionSlot(sl);
        int slot = dss.slot - cs.getDuration();
        if(slot >= 1)
            return new DaySessionSlot(dss.day,dss.session,slot).hash();
        slot = sl + matchClassSegment.getDuration();
        if(slot + cs.getDuration() - 1 <= Constant.slotPerCrew)
            return new DaySessionSlot(dss.day,dss.session,slot).hash();
        log.info("findConsecutiveSlot, BUG???"); return -1;
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
        List<ClassSegment> CS = new ArrayList<>();
        for(ClassSegment cs: mClassId2ClassSegments.get(id)) CS.add(cs);
        while(CS.size() > 0) {
            // consider first class-segment matched
            boolean ok = false;
            for (ClassSegment cs : CS) {
                if (ms.m.get(cs.getId()) != null) {
                    ClassSegment m_cs = ms.m.get(cs.getId());
                    int startSlot = findConsecutiveSlot(cs, m_cs);
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
                assignTimeSlot(cs,slot);
            }else{
                log.info("assignSlot4Class, CANNOT find any slot for class segment " + cs.str() + "?????");
            }
        }
        return true;
    }
    private boolean findGreedyTimeSlotForClass(Long id, int session, Map<String, List<Long>> mCourse2ClassId){
        List<ClassSegment> CS = mClassId2ClassSegments.get(id);
        ModelResponseTimeTablingClass cls = mClassId2Class.get(id);
        String courseCode = cls.getModuleCode();
        // FIRST try to find another class of the same course that can be matched
        MatchScore maxScore = null;
        Long sel_id = null;
        for(ModelResponseTimeTablingClass clsi: mCourse2Classes.get(courseCode)){
            if(!isScheduled(clsi.getId())) continue;
            if(mClassId2MatchedClassId.get(clsi.getId())!=null) continue;
            List<ClassSegment> CSi = mClassId2ClassSegments.get(clsi.getId());
            MatchScore ms = matchScore(id,clsi.getId(), Constant.slotPerCrew);
            if(maxScore == null || ms.score > maxScore.score){
                maxScore = ms; sel_id = clsi.getId();
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
                    if(maxScore == null || ms.score > maxScore.score){
                        maxScore = ms; sel_id = clsi.getId();
                    }
                }
            }
            if(sel_id != null) {
                assignSlot4Class(id, maxScore, session);
            }else{
                for(ClassSegment cs: mClassId2ClassSegments.get(id)){
                    int slot = findSeparateSlot(cs,session);
                    if(slot != -1){
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
    private boolean scheduleGroup(List<ClassSegment> L, int session, boolean assignRoom) {
        //Map<Long, List<ClassSegment>> mClassId2ClassSegments = new HashMap<>();
        Map<String, List<Long>> mCourse2ClassId = new HashMap<>();
        for (ClassSegment cs : L) {
            //if (mClassId2ClassSegments.get(cs.getClassId()) == null)
            //    mClassId2ClassSegments.put(cs.getClassId(), new ArrayList<>());
            //mClassId2ClassSegments.get(cs.getClassId()).add(cs);

            String courseCode = cs.getCourseCode();
            if(mCourse2ClassId.get(courseCode)==null)
                mCourse2ClassId.put(courseCode,new ArrayList<>());
            mCourse2ClassId.get(courseCode).add(cs.getClassId());
        }
        for(String courseCode: mCourse2ClassId.keySet()) {
            for (Long id : mCourse2ClassId.get(courseCode)) {
                //List<ClassSegment> CS = mClassId2ClassSegments.get(id);
                boolean ok = findGreedyTimeSlotForClass(id,session,mCourse2ClassId);
                if(!ok){
                    log.info("scheduleGroup -> CANNOT schedule class " + id + "????");
                }
            }
        }
        return true;
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
            if(!scheduleGroupLTandChildrenBT(MI[session],session,false)) return;
            log.info("After solve scheduleGroupLTandBT MI remain size = " + MI[session].size());
            log.info("solve scheduleGroupLTandBT SSH size = " + SSH[session].size());
            if(!scheduleGroupLTandChildrenBT(SSH[session],session,false)) return;

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
    private boolean scheduleGroupLTandChildrenBT(List<ClassSegment> L, int session, boolean assignRoom) {
        return true;
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
