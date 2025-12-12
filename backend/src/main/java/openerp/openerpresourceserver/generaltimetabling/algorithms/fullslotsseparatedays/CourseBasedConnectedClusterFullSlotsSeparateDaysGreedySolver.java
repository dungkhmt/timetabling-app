package openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedays;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.*;
import openerp.openerpresourceserver.generaltimetabling.algorithms.classschedulingmaxregistrationopportunity.CourseNotOverlapBackTrackingSolver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.ModelSchedulingLog;
import org.aspectj.weaver.patterns.ConcreteCflowPointcut;

import java.util.*;

@Log4j2
public class CourseBasedConnectedClusterFullSlotsSeparateDaysGreedySolver implements Solver {
    MapDataScheduleTimeSlotRoom I;
    //int[] solutionSlot;// solutionSlot[i] is the start time-slot assigned to class-segment i
    Map<Integer, Integer> solutionSlot;
    //int[] solutionRoom; // solutionRoom[i] is the room assigned to class-segment i
    Map<Integer, Integer> solutionRoom;

    Map<Integer, List<ClassSegment>> classesScheduledInSlot; // classesScheduledInSlot.get(s) is the list of classes scheduled in time slot s


    //HashSet<Integer>[] conflictClassSegment;// conflictClassSegment[i] is the list of class-segment conflict with class segment i
    Map<Integer, Set<Integer>> conflictClassSegment;
    //HashSet<String>[] relatedCourseGroups;// relatedCourseGroups[i] is the set of related course-group of class-segment i
    Map<Integer, Set<String>> relatedCourseGroups;
    //int[] ins; // ins[i]: number of class having the same course with class segment i
    //ClassSegment[] classSegments = null;
    Map<Integer, ClassSegment> mId2ClassSegment;
    Map<String, List<ClassSegment>> mCourseCode2ClassSegments;
    List<ClassSegment> classSegments = null;
    // output data structures
    List<Integer> unScheduledClassSegment;
    boolean foundSolution;
    int timeLimit;

    class Course{
        String courseCode;
        List<ClassSegment> classSegmentIds;
        Set<Long> classIds;

        public Course(String courseCode, List<ClassSegment> classSegmentIds,Set<Long> classIds) {
            this.courseCode = courseCode;
            this.classSegmentIds = classSegmentIds;
            this.classIds = classIds;
        }
        public int getNumberClasses(){
            return classIds.size();
        }
    }
    class CourseGroup{
        String hashCode;
        int courseIndex = -1;
        int type = -1;
        int instanceIndex = -1;
        List<Integer> groupIndex = new ArrayList<>();
        public CourseGroup(String hashCode)
        {
            this.hashCode = hashCode;
            String[] s = hashCode.split("-");
            if(s == null || s.length < 4) return;
            try {
                courseIndex = Integer.valueOf(s[0]);
                type = Integer.valueOf(s[1]);
                instanceIndex = Integer.valueOf(s[2]);
                s = s[3].split(",");
                if(s == null || s.length < 1) return;
                for(String si: s){
                    int gi = Integer.valueOf(si);
                    groupIndex.add(gi);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public boolean commonGroup(CourseGroup cg){
            for(int gi: groupIndex){
                if(cg.groupIndex.contains(gi)) return true;
            }
            return false;
        }
        public boolean sameCourse(CourseGroup cg){
            return courseIndex == cg.courseIndex;
        }
        public boolean conflict(CourseGroup cg){
            return commonGroup(cg);
        }
    }

    private int distance(int start1, int duration1, int start2, int duration2){
        int end1 = start1 + duration1 - 1;     int end2 = start2 + duration2 - 1;
        boolean notOverlap = start1 > end2 || start2 > end1;
        if(notOverlap == false) return 0;
        return Math.min(Math.abs(start1 - end2),Math.abs(start2-end1));
    }
    private int distance(int start, int duration, List<ClassSegment> cls){
        int minD = Integer.MAX_VALUE;
        for(ClassSegment cs: cls){
            if(solutionSlot.get(cs.getId())==null) continue;
            int s = solutionSlot.get(cs.getId());       int d = cs.getDuration();
            int dis = distance(start, duration, s,d);
            minD = Math.min(minD,dis);
        }
        return minD;
    }
    public CourseBasedConnectedClusterFullSlotsSeparateDaysGreedySolver(MapDataScheduleTimeSlotRoom I, Map<Integer, List<ClassSegment>> classesScheduledInSlot){
        this.classesScheduledInSlot = classesScheduledInSlot;
        //log.info("CourseBasedConnectedClusterFullSlotsSeparateDaysGreedySolver constructor, classScheduledInSlot.keySet = " + classesScheduledInSlot.keySet().size());

        this.I = I;
        for(int s: classesScheduledInSlot.keySet()){
            if(classesScheduledInSlot.get(s).size() > 0){
                //log.info("CourseBasedConnectedClusterFullSlotsSeparateDaysGreedySolver constructor, classScheduledInSlot(" + s + ").sz = " + classesScheduledInSlot.get(s).size());
            }
        }
        mId2ClassSegment = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            mId2ClassSegment.put(cs.getId(),cs);
        }
        mCourseCode2ClassSegments = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            if(cs.getDomainRooms().size() <= 1 && cs.getDomainTimeSlots().size() <= 1) continue; // do not consider scheduled class segment
            if(mCourseCode2ClassSegments.get(cs.getCourseCode())==null)
                mCourseCode2ClassSegments.put(cs.getCourseCode(),new ArrayList<>());
            mCourseCode2ClassSegments.get(cs.getCourseCode()).add(cs);
        }
        //conflictClassSegment = new HashSet[I.getClassSegments().length];
        conflictClassSegment= new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            conflictClassSegment.put(cs.getId(),new HashSet<>());
        }

        //for(int i = 0; i < I.getClassSegments().length; i++){
        //    conflictClassSegment[i] = new HashSet();
        //}
        for(Integer[] p: I.getConflict()){
            int i = p[0]; int j = p[1];
            //conflictClassSegment[i].add(j); conflictClassSegment[j].add(i);
            //conflictClassSegment.get(i).add(j);
            if(conflictClassSegment.get(i) != null && conflictClassSegment.get(j)!= null){
                conflictClassSegment.get(i).add(j);
                conflictClassSegment.get(j).add(i);
            }
        }
        classSegments = I.getClassSegments();
        /*
        classSegments = new ClassSegment[I.getNbClassSegments()];
        for(int i = 0; i < I.getNbClassSegments(); i++){
            int id = i;
            Long classId = I.getClassId()[i];
            Long parentClassId = I.getParentClassId()[i];
            List<Integer> groupIds = I.getRelatedGroupId()[i];
            int courseIndex = I.getCourseIndex()[i];
            Set<Integer> conflictClassSegmentIds = new HashSet();
            for(int j: conflictClassSegment[i]) conflictClassSegmentIds.add(j);
            int duration = I.getNbSlots()[i];
            //String courseCode = I.getCourseCode()[i];
            int nbStudents = I.getNbStudents()[i];
            List<Integer> domainTimeSlots = I.getDomains()[i];
            List<Integer> domainRooms = I.getRooms()[i];
            classSegments[i] = new ClassSegment(id, classId,parentClassId,groupIds,conflictClassSegmentIds,duration,courseIndex,nbStudents,domainTimeSlots,domainRooms);
            log.info("Constructor, class-segment[" + i + "] = " + classSegments[i]);

        }
        */
        /*
        relatedCourseGroups = new HashSet[classSegments.length];

        for(int i = 0; i < classSegments.length; i++){
            ClassSegment cs = classSegments[i];
            //String courseGroupId = hashCourseGroup(cs.getCourseIndex(),cs.getGroupIds());
            relatedCourseGroups[i]= new HashSet();
            for(int j = 0; j < classSegments.length; j++){
                ClassSegment csj = classSegments[j];
                String courseGroupId = hashCourseGroup(csj.getCourseIndex(),csj.getGroupIds());
                relatedCourseGroups[i].add(courseGroupId);
            }
        }
        */
        relatedCourseGroups = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            relatedCourseGroups.put(cs.getId(), new HashSet<>());
            for(ClassSegment csj: I.getClassSegments()){
                String courseGroupId = csj.hashCourseGroup();//hashCourseGroup(csj.getCourseIndex(),csj.getGroupIds())
                relatedCourseGroups.get(cs.getId()).add(courseGroupId);
            }
        }
    }

    private boolean check(int i, int s, int r){
        // check and return true if slot s and room r can be assigned to class segment i without violating constraintsa
        // explore all class segment j before i (have been assigned slot and room)
        //int duration_i = I.getNbSlots()[i];
        int duration_i = I.getClassSegments().get(i).getDuration();
        int startSlot_i = s;

        for(int j = 0; j <= i-1; j++){
            int duration_j = I.getClassSegments().get(j).getDuration();
            //int startSlot_j = solutionSlot[j];
            int startSlot_j = solutionSlot.get(j);

            //if(i == 4)log.info("check(" + i + "," + s + "," + r + " compare class-segment " + j + " having start_slot_j = " + startSlot_j + " duration_j = " + duration_j + " room " + solutionRoom[j]);
            //if(conflictClassSegment[i].contains(j)){// class segments i and j conflict
            if(conflictClassSegment.get(i).contains(j)){// class segments i and j conflict
                if(Util.overLap(startSlot_i,duration_i,startSlot_j,duration_j))
                    return false;
            }
            if(Util.overLap(startSlot_i, duration_i,startSlot_j,duration_j)){
                //if(solutionRoom[j] == r) return false;
                if(solutionRoom.get(j) == r) return false;
            }
        }
        return true;
    }
    /*
    private String hashCourseGroup(int courseIndex, List<Integer> groupIndex){
        String code = courseIndex + "-";
        for(int j = 0; j < groupIndex.size(); j++){
            int gIndex = groupIndex.get(j);
            code = code + gIndex;
            if(j < groupIndex.size()-1) code = code + ",";
        }
        return code;
    }

     */

    private SlotRoom selectMaxScoreSlotRoom(int i, List<ClassSegment> classInGroup, List<ClassSegment> sortedClassSegments, Map<String, Integer> mCourseGroup2TimeSlot){
        int maxScore = -1;
        ClassSegment cs = sortedClassSegments.get(i);
        SlotRoom sr = null;
        //log.info("selectMaxScoreSlotRoom -> Consider class-segment[" + cs.getId() + "] info " + cs.str() +
        //        " domain-timeSlots.sz = " + cs.getDomainTimeSlots().size() + " domain-rooms.sz = " + cs.getDomainRooms().size());
        HashSet<Integer> slotUsedInGroup = new HashSet<>();
        for(ClassSegment csi: classInGroup){
            if(solutionSlot.get(csi.getId())!=null && solutionSlot.get(csi.getId()) > -1){
                int s = solutionSlot.get(csi.getId());
                for(int d = 0; d <= csi.getDuration()-1; d++){
                    slotUsedInGroup.add(s + d);
                }
            }
        }
        // try FIRST with separate slot from class-segment in the same group
        for(int timeslot: cs.getDomainTimeSlots())if(!slotUsedInGroup.contains(timeslot)){
            for(int room: cs.getDomainRooms()){
                if(checkTimeSlotRoom(i,timeslot,room,sortedClassSegments)){
                    int score = computeScoreTimeSlotRoom(i,timeslot,room,sortedClassSegments);
                    //log.info("selectMaxScoreSlotRoom -> FIRST class-segment[" + cs.getId() + "], info " + cs.str() + " (slot,room) = " + timeslot + "," + room + " -> score = " + score);
                    if(score > maxScore){
                        maxScore = score;
                        sr = new SlotRoom(timeslot,room);
                    }
                }
            }
        }
        // SECOND: try all possible time slots
        if(sr == null){
            for(int timeslot: cs.getDomainTimeSlots()){
                for(int room: cs.getDomainRooms()){
                    if(checkTimeSlotRoom(i,timeslot,room,sortedClassSegments)){
                        int score = computeScoreTimeSlotRoom(i,timeslot,room,sortedClassSegments);
                        //log.info("selectMaxScoreSlotRoom -> FIRST class-segment[" + cs.getId() + "], info " + cs.str() + " (slot,room) = " + timeslot + "," + room + " -> score = " + score);
                        if(score > maxScore){
                            maxScore = score;
                            sr = new SlotRoom(timeslot,room);
                        }
                    }
                }
            }
        }
        return sr;
    }

    private SlotRoom findSlotRoom(int i, List<ClassSegment> classInGroup, List<ClassSegment> sortedClassSegments,Map<String, Integer> mCourseGroup2TimeSlot){
        // find an appropriate slot-room for class-segment i in the list sortedClassSegment (sortedClassSegment[i])
        // classInGroup is the list of class-segments in the same course-group

        ClassSegment cs = sortedClassSegments.get(i);
        String courseGroup = cs.hashCourseGroup();
        //log.info("findSlotRoom for class-segment[" + i + "], id = " + cs.getId() + ", course-group = " + courseGroup + ", classInGroup.sz = " + classInGroup.size());

        Set<Integer> slots = new HashSet<>();
        // collects slots of scheduled classes in the course-group
        for(ClassSegment csi: classInGroup){
            if(solutionSlot.get(csi.getId())!=null && solutionSlot.get(csi.getId()) > -1) {
                slots.add(solutionSlot.get(csi.getId()));
                //log.info("findSlotRoom for class-segment[" + i + "], id = " + cs.getId() +
                //        ", course-group = " + courseGroup + ", classInGroup.sz = "
                //        + classInGroup.size() + " csi " + csi.getId() + " scheduled to slot " + solutionSlot.get(csi.getId()));

            }
        }
        //log.info("findSlotRoom for class-segment[" + i + "], id = " + cs.getId() +
        //        ", course-group = " + courseGroup + ", classInGroup.sz = "
        //        + classInGroup.size() + " slots = " + slots.size());

        if(slots.size() == 0){// no class-segment of the group is scheduled
            int selectedSlot = mCourseGroup2TimeSlot.get(courseGroup);
            //log.info("findSlotRoom for class-segment[" + i + "], id = " + cs.getId() +
            //        ", course-group = " + courseGroup + ", slots = 0 => select slot of course-group " + selectedSlot);
            for(int room: cs.getDomainRooms()){
                if(checkTimeSlotRoom(i,selectedSlot,room,sortedClassSegments)){
                    SlotRoom sr = new SlotRoom(selectedSlot,room);
                    return sr;
                }
            }
        }
        List<Integer> aSlots = new ArrayList<>();
        for(int e: slots) aSlots.add(e); Collections.sort(aSlots);
        int selectedSlot = -1;
        int selecetdRoom = -1;
        SlotRoom sr = null;
        // FIRST: try to find predecessor or successor time-slot in the same day-session (ngay-kip)
        for(int e: aSlots){
            DaySessionSlot dss = new DaySessionSlot(e);
            int ns = e - cs.getDuration();// previous slot in the same session-day
            if(ns >= 1 && cs.getDomainTimeSlots().contains(ns)
                    && !slots.contains(ns)){
                DaySessionSlot ndss = new DaySessionSlot(ns);
                if(ndss.day == dss.day && ndss.session == dss.session) {
                    if(checkTimeSlot(i,ns,sortedClassSegments)) {
                        for(int room: cs.getDomainRooms()) {
                            if(checkTimeSlotRoom(i,ns,room,sortedClassSegments)) {
                                //selectedSlot = ns; selecetdRoom = room;
                                sr = new SlotRoom(ns,room);
                                break;
                            }
                        }
                    }
                }
            }
            if(sr != null) break;

            ns = e + cs.getDuration();// successor slot in the same session-day
            if(cs.getDomainTimeSlots().contains(ns) && !slots.contains(ns)){
                DaySessionSlot ndss = new DaySessionSlot(ns);
                if(ndss.day == dss.day && ndss.session == dss.session) {
                    if(checkTimeSlot(i,ns,sortedClassSegments)) {
                        for(int room: cs.getDomainRooms()) {
                            if(checkTimeSlotRoom(i,ns,room,sortedClassSegments)) {
                                //selectedSlot = ns; selecetdRoom = room;
                                sr = new SlotRoom(ns,room);
                                break;
                            }
                        }
                    }
                }
            }
            if(sr != null) break;
        }
        if(sr != null) return sr;

        // SECOND: Try to find time-slot, room maximizing score (registration possibility)
        sr = selectMaxScoreSlotRoom(i,classInGroup,sortedClassSegments, mCourseGroup2TimeSlot);
        if(sr == null){
            //log.info("findSlotRoom, selectMaxScoreSlotRoom failed to find a slot -> use slot of course-group");
            int slot = mCourseGroup2TimeSlot.get(cs.hashCourseGroup());
            for(int r: cs.getDomainRooms()){
                if(checkTimeSlotRoom(i,slot,r,sortedClassSegments)){
                    return new SlotRoom(slot,r);
                }
            }
        }
        return sr;
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
                sr = new SlotRoom(slot,room); break;
            }
        }
        return sr;
    }
    @Override
    public void solve() {
        log.info("solve START....");
        Set<String> courseGroupId = new HashSet<>();
        Map<String, List<ClassSegment>> mCourseGroup2ClassSegments = new HashMap();
        Map<String, List<Integer>> mCourseGroup2Domain = new HashMap<>();
        Map<String, Integer> mCourseGroup2Duration = new HashMap<>();
        Map<String, List<String>> mCourseGroup2ConflictCourseGroups = new HashMap<>();
        //classesScheduledInSlot = new HashMap<>();
        //for(int s = 0; s <= 1000; s++) classesScheduledInSlot.put(s,new ArrayList<>());

        //Map<Integer, List<ClassSegment>> mCourseCode2ClassSegments = new HashMap<>();

        //for(int i = 0; i < I.getNbClassSegments(); i++){
        //    String id = hashCourseGroup(I.getCourseIndex()[i],I.getRelatedGroupId()[i]);
        //    courseGroupId.add(id);
        //}
        for(int i = 0; i < classSegments.size(); i++){
            ClassSegment cs = classSegments.get(i);
            //String id = hashCourseGroup(cs.courseIndex,cs.getGroupIds());
            String id = cs.hashCourseGroup();
            if(mCourseGroup2ClassSegments.get(id)==null){
                mCourseGroup2ClassSegments.put(id, new ArrayList<>());
            }
            courseGroupId.add(id);
            mCourseGroup2ClassSegments.get(id).add(cs);


            //if(mCourseCode2ClassSegments.get(cs.getCourseIndex())==null)
            //    mCourseCode2ClassSegments.put(cs.getCourseIndex(),new ArrayList<>());
            //mCourseCode2ClassSegments.get(cs.getCourseIndex()).add(cs);
            if(cs.getDomainTimeSlots() == null || cs.getDomainTimeSlots().size() == 0) {
                log.info("solve, class-segment[" + i + "], id = " + cs.getId() + " has course-group " + id + " domain-timeslot empty");
            }
        }



        for(String id: courseGroupId){
            int duration = -1;
            List<Integer> domain = null;
            for(ClassSegment cs: mCourseGroup2ClassSegments.get(id)){
                if(duration < cs.getDuration()){
                    duration = cs.getDuration(); domain = cs.getDomainTimeSlots();
                }
            }
            log.info("solve, mCourseGroup2ClassSegments.get(" + id + ").sz = " + mCourseGroup2ClassSegments.get(id).size());
            if(domain == null)
                log.info("solve, consider courseGraph " + courseGroupId + " domain null");
            mCourseGroup2Domain.put(id,domain);
            log.info("solve, mCourseGroup2Domain.put(" + id + "," + domain.size() + ")");
            mCourseGroup2Duration.put(id,duration);
        }

        // sort domain time-slot of course-group based on the classes scheduled in time-slots
        for(String cg: mCourseGroup2Domain.keySet()){
            List<Integer> D = mCourseGroup2Domain.get(cg);
            if(D == null || D.size() == 0){
                log.info("solve mCourseGroup2Domain.get(" + cg + ") return empty domain D");
            }
            Collections.sort(D, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return classesScheduledInSlot.get(o1).size() - classesScheduledInSlot.get(o2).size();
                }
            });
        }
        for(String cg: mCourseGroup2Domain.keySet()){
            //log.info("solve, after sorting course-group " + cg + " domain = " + mCourseGroup2Domain.get(cg));
            for(int s: mCourseGroup2Domain.get(cg)){
                //log.info("solve, after sorting course-group " + cg + " at slot " + s + " -> scheduled classes.sz = " + classesScheduledInSlot.get(s).size());
            }
        }

        List<CourseGroup> courseGroups = new ArrayList<>();
        for(String cgi: courseGroupId){
            courseGroups.add(new CourseGroup(cgi));
        }

        for(CourseGroup cg1: courseGroups){
            mCourseGroup2ConflictCourseGroups.put(cg1.hashCode,new ArrayList<>());
            for(CourseGroup cg2: courseGroups)if(cg1 != cg2){
                if(cg1.conflict(cg2)){
                    mCourseGroup2ConflictCourseGroups.get(cg1.hashCode).add(cg2.hashCode);
                }
            }
        }
        CourseNotOverlapBackTrackingSolver CNOBS = new CourseNotOverlapBackTrackingSolver(courseGroupId,mCourseGroup2Domain,mCourseGroup2Duration, mCourseGroup2ConflictCourseGroups);
        CNOBS.findFirstSolution = false;// find the best solution
        CNOBS.solve(timeLimit);
        if(!CNOBS.hasSolution()){
            log.info("solve, CNOBS cannot find any solution!!!");
            return;
        }
        Map<String, Integer> mCourseGroup2TimeSlot = CNOBS.getSolutionMap();
        for(String cg: mCourseGroup2TimeSlot.keySet()){
            int slot = mCourseGroup2TimeSlot.get(cg);
            DaySessionSlot dss = new DaySessionSlot(slot);
            //log.info("solve, CNOBS returnd course-group " + cg + ", duration " + mCourseGroup2Duration.get(cg) + " scheduled to time-slot " + slot + " (" + dss + ")");
        }
        //solutionRoom = new int[classSegments.length];
        //solutionSlot = new int[classSegments.length];
        //for(int i = 0; i < classSegments.length; i++){
        //    solutionRoom[i] = -1; solutionSlot[i] = -1;
        //}
        solutionSlot = new HashMap<>();
        solutionRoom = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            solutionRoom.put(cs.getId(),-1);
            solutionSlot.put(cs.getId(),-1);
        }
        List<ClassSegment> sortedClassSegments = new ArrayList<>();
        Map<String, Integer> mCourseGroup2Pointer = new HashMap<>();
        for(String c: courseGroupId) mCourseGroup2Pointer.put(c,0);
        //boolean[] scheduled = new boolean[classSegments.length];
        Map<Integer, Boolean> scheduled = new HashMap<>();
        //for(int i = 0; i < classSegments.length; i++){
        //    scheduled[i] = false;
        //}
        for(ClassSegment cs: I.getClassSegments())
            scheduled.put(cs.getId(),false);

        for(int i = 0; i < classSegments.size(); i++){
            ClassSegment cs = classSegments.get(i);
            if(cs.getDomainRooms().size() == 1 && cs.getDomainTimeSlots().size() == 1){
                //scheduled[cs.getId()] = true;
                scheduled.put(cs.getId(),true);
                sortedClassSegments.add(cs);
            }
        }
        /*
        String[] courseCodes = new String[mCourseCode2ClassSegments.keySet().size()];
        int ic = -1;
        for(String cc: mCourseCode2ClassSegments.keySet()){
            ic++; courseCodes[ic] = cc;
        }
        // sort courseCodes in increasing order of number of class-segments
        for(int i = 0; i < courseCodes.length; i++){
            for(int j = i+1; j < courseCodes.length; j++){
                if(mCourseCode2ClassSegments.get(courseCodes[i]).size() >
                        mCourseGroup2ClassSegments.get(courseCodes[j]).size()){
                    String ts = courseCodes[i]; courseCodes[i] = courseCodes[j]; courseCodes[j] = ts;
                }
            }
        }

         */
        // sort courseGroupId in increasing order of number of class-segments belonging to
        Map<String, List<ClassSegment>> mCourseGroup2UnscheduledClassSegments = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            if(cs.getDomainTimeSlots().size() <= 1 && cs.getDomainRooms().size() <= 1) continue;
            String cg = cs.hashCourseGroup();
            if(mCourseGroup2UnscheduledClassSegments.get(cg)==null)
                mCourseGroup2UnscheduledClassSegments.put(cg,new ArrayList<>());
            mCourseGroup2UnscheduledClassSegments.get(cg).add(cs);
        }
        String[] sortedCourseGroupId = new String[mCourseGroup2UnscheduledClassSegments.keySet().size()];
        int is = -1;
        for(String cg: mCourseGroup2UnscheduledClassSegments.keySet()){
            is++; sortedCourseGroupId[is] = cg;
        }
        for(int i= 0; i < sortedCourseGroupId.length; i++){
            for(int j = i+1; j < sortedCourseGroupId.length; j++){
                if(mCourseGroup2UnscheduledClassSegments.get(sortedCourseGroupId[i]).size() >
                        mCourseGroup2UnscheduledClassSegments.get(sortedCourseGroupId[j]).size()){
                    String ts = sortedCourseGroupId[i];
                    sortedCourseGroupId[i] = sortedCourseGroupId[j];
                    sortedCourseGroupId[j] = ts;
                }
            }
        }
        //for(String cg: sortedCourseGroupId){
        //    for(ClassSegment cs: mCourseGroup2UnscheduledClassSegments.get(cg)){
        //        sortedClassSegments.add(cs);
        //    }
        //}

        while(true){
            boolean finished = true;
            //for(String c: courseGroupId){
            for(String c: sortedCourseGroupId){
                int idx = mCourseGroup2Pointer.get(c);
                //if(idx < mCourseGroup2ClassSegments.get(c).size()){
                if(idx < mCourseGroup2UnscheduledClassSegments.get(c).size()){

                    //ClassSegment cs = mCourseGroup2ClassSegments.get(c).get(idx);
                    ClassSegment cs = mCourseGroup2UnscheduledClassSegments.get(c).get(idx);
                    mCourseGroup2Pointer.put(c,idx+1);
                    //if(!scheduled[cs.getId()]) {
                    if(!scheduled.get(cs.getId())==true) {

                        sortedClassSegments.add(cs);
                        //scheduled[cs.getId()] = true;
                        scheduled.put(cs.getId(),true);
                    }
                    finished = false; break;
                }
            }
            if(finished) break;
        }

        //log.info("solve, after sorting, sortedClassSegments: ");
        //for(int i = 0; i < sortedClassSegments.size(); i++)
        //    log.info("sortedClassSegments[" + i + "] = " + sortedClassSegments.get(i).str());

        for(int i = 0;i < sortedClassSegments.size(); i++){
            ClassSegment cs = sortedClassSegments.get(i);
            if(cs.isScheduled){
                if(cs.getDomainRooms().size() != 1 || cs.getDomainRooms().size() != 1){
                    log.info("solve: BUG?? class " + cs.getClassId() + " is scheduled but domain time-slot and room is not singleton");
                }
                {
                    int timeSlot = cs.getDomainTimeSlots().get(0);
                    int room = cs.getDomainRooms().get(0);
                    assignTimeSlotRoom(cs, timeSlot, room);
                    //log.info("solve, assign fixed class-segment " + cs.str() + " with slot " + timeSlot + " room " + room);
                }
                continue;
            }
            // find a time slot and room for the class-segment cs
            //String courseGroup = hashCourseGroup(cs.getCourseIndex(),cs.getGroupIds());
            String courseGroup = cs.hashCourseGroup();
            List<ClassSegment> classInGroup = mCourseGroup2ClassSegments.get(courseGroup);
            String info = "conflict cs: ";
            for(int cid: cs.getConflictClassSegmentIds()) {
                info = info + mId2ClassSegment.get(cid).str();
            }
            info = info + " class in group: ";
            for(ClassSegment csi: classInGroup) info = info + csi.str();
            log.info("solve, consider " + i + "/" + sortedClassSegments.size() + " sorted class-segment " + cs.str() + info);

            SlotRoom sr = findSlotRoom(i,classInGroup,sortedClassSegments,mCourseGroup2TimeSlot);
            if(sr == null){
                log.info("solve: Cannot find a (time-slot, room) for class segment " + cs.str());

            }else{
                assignTimeSlotRoom(cs,sr.slot,sr.room);
                //log.info("solve: assign class-segment " + cs.getId() + " to slot " + sr.slot + ", room " + sr.room);
                log.info("solve, assign " + i + "/" + sortedClassSegments.size() + " class-segment " + cs.str() + " with slot " + sr.slot + " room " + sr.slot);

            }
        }
        postProcessing();

    }

    public void postProcessing(){
        // perform local search to change timeslot-room of class-segments
        // such that the timetable separate over days of the week
        Set<Integer> daysUsed = new HashSet<>();
        for(ClassSegment cs: I.getClassSegments()){
            int slot = solutionSlot.get(cs.getId());
            DaySessionSlot dss = new DaySessionSlot(slot);
            daysUsed.add(dss.day);
        }
        Map<Integer, Set<ClassSegment>> mDay2ClassSegments = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            int s = solutionSlot.get(cs.getId());
            DaySessionSlot dss = new DaySessionSlot(s);
            if(mDay2ClassSegments.get(dss.day)==null)
                mDay2ClassSegments.put(dss.day,new HashSet<>());
            mDay2ClassSegments.get(dss.day).add(cs);
        }
        Map<Integer, Set<ClassSegment>> mDay2ClassSegmentsBefore = new HashMap<>();
        for(int d = 2; d <= Constant.daysPerWeek+1; d++)
            mDay2ClassSegmentsBefore.put(d,new HashSet<>());
        for(int s: classesScheduledInSlot.keySet()){
            List<ClassSegment> cls = classesScheduledInSlot.get(s);
            DaySessionSlot dss = new DaySessionSlot(s);
            if(mDay2ClassSegmentsBefore.get(dss.day)==null)
                mDay2ClassSegmentsBefore.put(dss.day,new HashSet<>());
            for(ClassSegment cs: cls) {
                if(!I.getClassSegments().contains(cs))
                    mDay2ClassSegmentsBefore.get(dss.day).add(cs);
            }
        }
        Map<Integer, Integer> mDay2NewDay = new HashMap<>();
        // sort days of current in decreasing order of class-segments scheduled on
        int[] a = new int[mDay2ClassSegments.keySet().size()];
        int idx = -1;
        for(int k: mDay2ClassSegments.keySet()){
            idx++; a[idx] = k;//mDay2ClassSegments.get(k).size();
        }
        for(int i = 0; i < a.length; i++)
            for(int j = i+1; j < a.length; j++){
                //if(a[i] < a[j]){
                if(mDay2ClassSegments.get(a[i]).size() < mDay2ClassSegments.get(a[j]).size()){
                    int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
                }
            }
        for(int i = 0; i < a.length; i++){
            log.info("postProcessing, after sorting a[" + i + "] = " + a[i] + " sz = " + mDay2ClassSegments.get(a[i]).size());
        }

        // sort days before in increasing order of class-segment scheduled on
        int[] b = new int[mDay2ClassSegmentsBefore.keySet().size()];
        idx = -1;
        for(int k: mDay2ClassSegmentsBefore.keySet()){
            idx++; b[idx] = k;//mDay2ClassSegmentsBefore.get(k).size();
        }
        for(int i = 0; i < b.length; i++){
            for(int j = i+1; j < b.length; j++){
                //if(b[i] > b[j]){
                if(mDay2ClassSegmentsBefore.get(b[i]).size() > mDay2ClassSegmentsBefore.get(b[j]).size()){
                    int tmp = b[i]; b[i] = b[j]; b[j] = tmp;
                }
            }
        }
        for(int i = 0; i < b.length; i++){
            log.info("postProcessing, after sorting b[" + i + "] = " + b[i] + " sz = " + mDay2ClassSegmentsBefore.get(b[i]).size());
        }

        for(int i = 0; i < a.length; i++){
            mDay2NewDay.put(a[i],b[i]);
            //log.info("postProcessing, map day " + a[i] + " to new day " + b[i]);
        }
        //for(ClassSegment cs: I.getClassSegments()) {
        //    unAassignTimeSlotRoom(cs,solutionSlot.get(cs.getId()));
        //    log.info("postProcessing, unAssign class-segment " + cs.str());
        //}
        for(ClassSegment cs: I.getClassSegments()){
            int s = solutionSlot.get(cs.getId());
            DaySessionSlot dss = new DaySessionSlot(s);
            int newDay = mDay2NewDay.get(dss.day);
            int newSlot = new DaySessionSlot(newDay,dss.session,dss.slot).hash();
            unAassignTimeSlotRoom(cs,s);
            //log.info("postProcessing, reAssign from day-slot " + dss.day + " to new day " + newDay);
            SlotRoom sr = findRoom(cs,newSlot);
            if(sr != null)
            assignTimeSlotRoom(cs,sr.slot,sr.room);
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

    private int computeScoreTimeSlotRoom(int i, int timeSlot, int room, List<ClassSegment> sortedClassSegments) {
        ClassSegment csi = sortedClassSegments.get(i);
        // check if timeSlot overlap with conflicting and scheduled class-segment
        for(int j = 0; j < i; j++){
            ClassSegment csj = sortedClassSegments.get(j);
            int slotJ = solutionSlot.get(csj.getId());
            int durationJ = csj.getDuration();
            if(csi.getConflictClassSegmentIds().contains(csj.getId())){
                if(Util.overLap(timeSlot,csi.getDuration(),slotJ,durationJ))
                    return -100000000;
            }
        }
        // compute courseQty[c] the number of assigned class-segments of course index c
        // not overlap with class-segment i (to be assigned to time-slot timeSlot)
        //int[] courseQty = new int[I.getMaxTeacherOfCourses().length];
        //for(int j = 0; j < courseQty.length; j++) courseQty[j] = 0;
        String cgi = csi.hashCourseGroup();//hashCourseGroup(csi.getCourseIndex(),csi.getGroupIds());
        HashMap<String, Integer> mCourseGroup2NumberClass = new HashMap<>();
        //for(String cg: relatedCourseGroups[i]) mCourseGroup2NumberClass.put(cg,0);
        for(String cg: relatedCourseGroups.get(csi.getId())) mCourseGroup2NumberClass.put(cg,0);

        int di = csi.getDuration();
        for(int j = 0; j < i; j++){
            ClassSegment csj = sortedClassSegments.get(j);
            //int tsj = solutionSlot[csj.getId()];
            int tsj = solutionSlot.get(csj.getId());

            int dj = csj.getDuration();
            String cgj = csj.hashCourseGroup();//hashCourseGroup(csj.getCourseIndex(),csj.getGroupIds());
            if(!Util.overLap(timeSlot,di,tsj,dj)){
                //if(relatedCourseGroups[i].contains(cgj) && !cgi.equals(cgj)){
                if(relatedCourseGroups.get(csi.getId()).contains(cgj) && !cgi.equals(cgj)){

                    if(mCourseGroup2NumberClass.get(cgj)==null)
                        mCourseGroup2NumberClass.put(cgj,1);
                    else
                        mCourseGroup2NumberClass.put(cgj,mCourseGroup2NumberClass.get(cgj)+1);
                }
            }
        }
        int score = 1;
        //for(String cg: relatedCourseGroups[i]){
        for(String cg: relatedCourseGroups.get(csi.getId())){
            score =score * mCourseGroup2NumberClass.get(cg);
        }
        return score;
    }
    private boolean checkTimeSlotRoom(int i, int timeSlot, int room, List<ClassSegment> sortedClassSegments){
        int DEBUG_ID = -29;
        ClassSegment csi = sortedClassSegments.get(i);
        if(csi.getId() == DEBUG_ID) log.info("checkTimeSlotRoom(" + csi.getId() + "," + timeSlot+"," + room + ")");
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
        // check if the class-segment i can be assigned to timeSlot

        int maxTeacher = I.getMaxTeacherOfCourses()[csi.getCourseIndex()];
        int di = csi.getDuration();
        // explore class-segment scheduled
        int teachers = 0;
        for(int j = 0; j < i; j++){
            ClassSegment csj = sortedClassSegments.get(j);
            int dj = csj.getDuration();
            //int timeSlotJ = solutionSlot[csj.getId()];
            //int roomJ = solutionRoom[csj.getId()];
            int timeSlotJ = solutionSlot.get(csj.getId());
            int roomJ = solutionRoom.get(csj.getId());
            //log.info("checkTimeSlotRoom(slot " + timeSlot + ", room " + room + ": compare with class " + csj.getId() + " timeslot = " + timeSlotJ + " room = " + roomJ);
            if(Util.overLap(timeSlot,di,timeSlotJ,dj)){
                //if(conflictClassSegment[csi.getId()].contains(csj.getId())){
                if(conflictClassSegment.get(csi.getId()).contains(csj.getId())){
                    if(csi.getId() == DEBUG_ID){
                        log.info("checkTimeSlotRoom(" + i + "," + timeSlot + "," + room +"), conflict with class-segment[" + j + "] classId = " + csj.getClassId() + " -> RETURN FALSE");
                    }
                    return false;
                }
                if(csj.getCourseIndex()==csi.getCourseIndex()){
                    teachers++;
                }
                //log.info("checkTimeSlotRoom(slot " + timeSlot + ", room " + room + ": compare with class " + csj.getId() + " timeslot = " + timeSlotJ + " room = " + roomJ + " Overlap -> check room!!");

                if(room == roomJ){
                    if(csi.getId() == DEBUG_ID){
                        log.info("checkTimeSlotRoom(" + i + "," + timeSlot + "," + room +"), conflict with room(" + solutionRoom.get(csj.getId()) + ") of class-segment[" + j + "] classId = " + csj.getClassId() + " -> RETURN FALSE");
                    }
                    return false;
                }
            }
        }
        if(teachers + 1 > maxTeacher){
            return false;
        }

        return true;
    }

    private boolean checkTimeSlot(int i, int timeSlot, List<ClassSegment> sortedClassSegments){
        // check if the class-segment i can be assigned to timeSlot
        ClassSegment csi = sortedClassSegments.get(i);
        int maxTeacher = I.getMaxTeacherOfCourses()[csi.getCourseIndex()];
        int di = sortedClassSegments.get(i).getDuration();
        // explore class-segment scheduled
        int teachers = 0;
        for(int j = 0; j < i; j++){
            ClassSegment csj = sortedClassSegments.get(j);
            int dj = sortedClassSegments.get(j).getDuration();
            //int timeSlotJ = solutionSlot[sortedClassSegments.get(j).getId()];
            int timeSlotJ = solutionSlot.get(sortedClassSegments.get(j).getId());
            if(Util.overLap(timeSlot,di,timeSlotJ,dj)){
                //if(conflictClassSegment[csi.getId()].contains(csj.getId())){
                if(conflictClassSegment.get(csi.getId()).contains(csj.getId())){
                    return false;
                }
                if(csj.getCourseIndex()==csi.getCourseIndex()){
                    teachers++;
                }
            }
        }
        if(teachers + 1 > maxTeacher){
            return false;
        }

        return true;
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

    @Override
    public String name() {
        return "CourseBasedConnectedClusterFullSlotsSeparateDaysGreedySolver";
    }

    @Override
    public List<ModelSchedulingLog> getLogs() {
        return null;
    }

    public static void main(String[] args){
        try{
            List<Integer> L = new ArrayList<>();
            L.add(3); L.add(1); L.add(10); L.add(3);
            int idx = L.indexOf(10);
            L.remove(idx);
            for(int e: L) System.out.println(e);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


