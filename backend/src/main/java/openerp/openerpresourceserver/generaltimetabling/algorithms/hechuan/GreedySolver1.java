package openerp.openerpresourceserver.generaltimetabling.algorithms.hechuan;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.classschedulingmaxregistrationopportunity.CourseNotOverlapBackTrackingSolver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;


import java.util.*;

@Log4j2
public class GreedySolver1 implements Solver {
    MapDataScheduleTimeSlotRoom I;
    int[] solutionSlot;// solutionSlot[i] is the start time-slot assigned to class-segment i
    int[] solutionRoom; // solutionRoom[i] is the room assigned to class-segment i
    HashSet<Integer>[] conflictClassSegment;// conflictClassSegment[i] is the list of class-segment conflict with class segment i
    HashSet<String>[] relatedCourseGroups;// relatedCourseGroups[i] is the set of related course-group of class-segment i
    int[] ins; // ins[i]: number of class having the same course with class segment i
    ClassSegment[] classSegments = null;
    // output data structures
    List<Integer> unScheduledClassSegment;
    boolean foundSolution;

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
        List<Integer> groupIndex = new ArrayList<>();
        public CourseGroup(String hashCode)
        {
            this.hashCode = hashCode;
            String[] s = hashCode.split("-");
            if(s == null || s.length < 2) return;
            try {
                courseIndex = Integer.valueOf(s[0]);
                s = s[1].split(",");
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
    public GreedySolver1(MapDataScheduleTimeSlotRoom I){

        this.I = I;
        conflictClassSegment = new HashSet[I.getNbClassSegments()];
        for(int i = 0; i < I.getNbClassSegments(); i++){
            conflictClassSegment[i] = new HashSet();
        }
        for(Integer[] p: I.getConflict()){
            int i = p[0]; int j = p[1];
            conflictClassSegment[i].add(j); conflictClassSegment[j].add(i);
        }
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
    }

    private boolean check(int i, int s, int r){
        // check and return true if slot s and room r can be assigned to class segment i without violating constraintsa
        // explore all class segment j before i (have been assigned slot and room)
        int duration_i = I.getNbSlots()[i];
        int startSlot_i = s;

        for(int j = 0; j <= i-1; j++){
            int duration_j = I.getNbSlots()[j];
            int startSlot_j = solutionSlot[j];
            //if(i == 4)log.info("check(" + i + "," + s + "," + r + " compare class-segment " + j + " having start_slot_j = " + startSlot_j + " duration_j = " + duration_j + " room " + solutionRoom[j]);
            if(conflictClassSegment[i].contains(j)){// class segments i and j conflict
                if(Util.overLap(startSlot_i,duration_i,startSlot_j,duration_j))
                    return false;
            }
            if(Util.overLap(startSlot_i, duration_i,startSlot_j,duration_j)){
                if(solutionRoom[j] == r) return false;
            }
        }
        return true;
    }

    private String hashCourseGroup(int courseIndex, List<Integer> groupIndex){
        String code = courseIndex + "-";
        for(int j = 0; j < groupIndex.size(); j++){
            int gIndex = groupIndex.get(j);
            code = code + gIndex;
            if(j < groupIndex.size()-1) code = code + ",";
        }
        return code;
    }
    @Override
    public void solve() {
        log.info("solve START....");
        Set<String> courseGroupId = new HashSet<>();
        Map<String, List<ClassSegment>> mCourseGroup2ClassSegments = new HashMap();
        Map<String, List<Integer>> mCourseGroup2Domain = new HashMap<>();
        Map<String, Integer> mCourseGroup2Duration = new HashMap<>();
        Map<String, List<String>> mCourseGroup2ConflictCourseGroups = new HashMap<>();
        //for(int i = 0; i < I.getNbClassSegments(); i++){
        //    String id = hashCourseGroup(I.getCourseIndex()[i],I.getRelatedGroupId()[i]);
        //    courseGroupId.add(id);
        //}
        for(int i = 0; i < classSegments.length; i++){
            ClassSegment cs = classSegments[i];
            //String id = hashCourseGroup(cs.courseIndex,cs.getGroupIds());
            String id = cs.hashCourseGroup();
            if(mCourseGroup2ClassSegments.get(id)==null){
                mCourseGroup2ClassSegments.put(id, new ArrayList<>());
            }
            courseGroupId.add(id);
            mCourseGroup2ClassSegments.get(id).add(cs);
            log.info("solve, class-segment[" + i + "], id = " + cs.getId() + " has course-group " + id);
        }
        for(String id: courseGroupId){
            int duration = 0;
            List<Integer> domain = null;
            for(ClassSegment cs: mCourseGroup2ClassSegments.get(id)){
                if(duration < cs.getDuration()){
                    duration = cs.getDuration(); domain = cs.getDomainTimeSlots();
                }
            }
            mCourseGroup2Domain.put(id,domain);
            mCourseGroup2Duration.put(id,duration);
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
        CNOBS.solve();
        if(!CNOBS.hasSolution()){
            log.info("solve, CNOBS cannot find any solution!!!");
            return;
        }
        Map<String, Integer> mCourseGroup2TimeSlot = CNOBS.getSolutionMap();
        for(String cg: mCourseGroup2TimeSlot.keySet()){
            log.info("solve, CNOBS returnd course-group " + cg + " scheduled to time-slot " + mCourseGroup2TimeSlot.get(cg));
        }
        solutionRoom = new int[classSegments.length];
        solutionSlot = new int[classSegments.length];
        for(int i = 0; i < classSegments.length; i++){
            solutionRoom[i] = -1; solutionSlot[i] = -1;
        }
        List<ClassSegment> sortedClassSegments = new ArrayList<>();
        Map<String, Integer> mCourseGroup2Pointer = new HashMap<>();
        for(String c: courseGroupId) mCourseGroup2Pointer.put(c,0);
        boolean[] scheduled = new boolean[classSegments.length];
        for(int i = 0; i < classSegments.length; i++){
            scheduled[i] = false;
        }
        for(int i = 0; i < classSegments.length; i++){
            ClassSegment cs = classSegments[i];
            if(cs.getDomainRooms().size() == 1 && cs.getDomainTimeSlots().size() == 1){
                scheduled[cs.getId()] = true;
                sortedClassSegments.add(cs);
            }
        }
        while(true){
            boolean finished = true;
            for(String c: courseGroupId){
                int idx = mCourseGroup2Pointer.get(c);
                if(idx < mCourseGroup2ClassSegments.get(c).size()){
                    ClassSegment cs = mCourseGroup2ClassSegments.get(c).get(idx);
                    mCourseGroup2Pointer.put(c,idx+1);
                    if(!scheduled[cs.getId()]) {
                        sortedClassSegments.add(cs);
                        scheduled[cs.getId()] = true;
                    }
                    finished = false; break;
                }
            }
            if(finished) break;
        }
        log.info("solve, after sorting, sortedClassSegments: ");
        for(int i = 0; i < sortedClassSegments.size(); i++)
            log.info("sortedClassSegments[" + i + "] = " + sortedClassSegments.get(i));

        for(int i = 0;i < sortedClassSegments.size(); i++){
            ClassSegment cs = sortedClassSegments.get(i);
            // find a time slot and room for the class-segment cs
            //String courseGroup = hashCourseGroup(cs.getCourseIndex(),cs.getGroupIds());
            String courseGroup = cs.hashCourseGroup();
            int maxTeacher = I.getMaxTeacherOfCourses()[cs.getCourseIndex()];// get max number of teacher in charge of the course courseIndex
            // try first the time-slot for the courseGroup
            int selectTimeSlot = mCourseGroup2TimeSlot.get(courseGroup);
            log.info("solve, scan sorted class-segments, consider " + cs.getId() + ", courseGroup = " + courseGroup + " selectedTimeSlot = " + selectTimeSlot);
            int selectedRoom = -1;

            if(checkTimeSlot(i,selectTimeSlot,sortedClassSegments)){
                for(int room: cs.getDomainRooms()){
                    if(checkTimeSlotRoom(i,selectTimeSlot,room,sortedClassSegments)){
                        selectedRoom = room; break;
                    }
                }
            }

            if(selectedRoom != -1) {
                //solutionSlot[cs.getId()] = selectTimeSlot;
                //solutionRoom[cs.getId()] = selectedRoom;
                //log.info("solve, assign time-slot[" + cs.getId() + "] " + selectedRoom + " room[" + cs.getId() + "] = " + selectedRoom);
                assignTimeSlotRoom(cs.getId(),selectTimeSlot,selectedRoom);
            }else{
                // try to find another time-slot and room for class-segment i
                int maxScore = -1;
                log.info("Consider class-segment[" + cs.getId() + "], classId " + cs.getClassId() +
                        " domain-timeSlots.sz = " + cs.getDomainTimeSlots().size() + " domain-rooms.sz = " + cs.getDomainRooms().size());
                for(int timeslot: cs.getDomainTimeSlots())if(timeslot != selectTimeSlot){
                    for(int room: cs.getDomainRooms()){
                        if(checkTimeSlotRoom(i,timeslot,room,sortedClassSegments)){
                            int score = computeScoreTimeSlotRoom(i,timeslot,room,sortedClassSegments);
                            log.info("Consider class-segment[" + cs.getId() + "], classId " + cs.getClassId() + " score = " + score);
                            if(score > maxScore){
                                maxScore = score; selectedRoom = room; selectTimeSlot = timeslot;
                            }
                        }
                    }
                }
                if(maxScore > -1){
                    //solutionSlot[cs.getId()] = selectTimeSlot;
                    //solutionRoom[cs.getId()] = selectedRoom;
                    //log.info("solve, assign time-slot[" + cs.getId() + "] " + selectedRoom + " room[" + cs.getId() + "] = " + selectedRoom);
                    assignTimeSlotRoom(cs.getId(),selectTimeSlot,selectedRoom);
                }else{
                    log.info("solve CANNOT find solution for class-segment[" + i + "], id = " + cs.getId() + " classId = " + cs.getClassId());
                }
            }
        }
    }
    private void assignTimeSlotRoom(int csi, int timeSlot, int room){
        solutionSlot[csi] = timeSlot;
        solutionRoom[csi] = room;
        log.info("assignTimeSlotRoom[" + csi + "], time-slot = " + timeSlot + ", room = " + room);
        foundSolution = true;
    }
    private int computeScoreTimeSlotRoom(int i, int timeSlot, int room, List<ClassSegment> sortedClassSegments) {
        ClassSegment csi = sortedClassSegments.get(i);
        // compute courseQty[c] the number of assigned class-segments of course index c
        // not overlap with class-segment i (to be assigned to time-slot timeSlot)
        //int[] courseQty = new int[I.getMaxTeacherOfCourses().length];
        //for(int j = 0; j < courseQty.length; j++) courseQty[j] = 0;
        String cgi = hashCourseGroup(csi.getCourseIndex(),csi.getGroupIds());
        HashMap<String, Integer> mCourseGroup2NumberClass = new HashMap<>();
        for(String cg: relatedCourseGroups[i]) mCourseGroup2NumberClass.put(cg,0);
        int di = csi.getDuration();
        for(int j = 0; j < i; j++){
            ClassSegment csj = sortedClassSegments.get(j);
            int tsj = solutionSlot[csj.getId()];
            int dj = csj.getDuration();
            String cgj = hashCourseGroup(csj.getCourseIndex(),csj.getGroupIds());
            if(!Util.overLap(timeSlot,di,tsj,dj)){
                if(relatedCourseGroups[i].contains(cgj) && !cgi.equals(cgj)){
                    if(mCourseGroup2NumberClass.get(cgj)==null)
                        mCourseGroup2NumberClass.put(cgj,1);
                    else
                        mCourseGroup2NumberClass.put(cgj,mCourseGroup2NumberClass.get(cgj)+1);
                }
            }
        }
        int score = 1;
        for(String cg: relatedCourseGroups[i]){
            score =score * mCourseGroup2NumberClass.get(cg);
        }
        return score;
    }
    private boolean checkTimeSlotRoom(int i, int timeSlot, int room, List<ClassSegment> sortedClassSegments){
        int DEBUG_ID = -1;
        // check if the class-segment i can be assigned to timeSlot
        ClassSegment csi = sortedClassSegments.get(i);
        int maxTeacher = I.getMaxTeacherOfCourses()[csi.getCourseIndex()];
        int di = sortedClassSegments.get(i).getDuration();
        // explore class-segment scheduled
        int teachers = 0;
        for(int j = 0; j < i; j++){
            ClassSegment csj = sortedClassSegments.get(j);
            int dj = sortedClassSegments.get(j).getDuration();
            int timeSlotJ = solutionSlot[csj.getId()];
            if(Util.overLap(timeSlot,di,timeSlotJ,dj)){
                if(conflictClassSegment[csi.getId()].contains(csj.getId())){
                    if(csi.getId() == DEBUG_ID){
                        log.info("checkTimeSlotRoom(" + i + "," + timeSlot + "," + room +"), conflict with class-segment[" + j + "] classId = " + csj.getClassId() + " -> RETURN FALSE");
                    }
                    return false;
                }
                if(csj.getCourseIndex()==csi.getCourseIndex()){
                    teachers++;
                }
                if(room == solutionRoom[csj.getId()]){
                    if(csi.getId() == DEBUG_ID){
                        log.info("checkTimeSlotRoom(" + i + "," + timeSlot + "," + room +"), conflict with room(" + solutionRoom[csj.getId()] + ") of class-segment[" + j + "] classId = " + csj.getClassId() + " -> RETURN FALSE");

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
            int timeSlotJ = solutionSlot[sortedClassSegments.get(j).getId()];
            if(Util.overLap(timeSlot,di,timeSlotJ,dj)){
                if(conflictClassSegment[csi.getId()].contains(csj.getId())){
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
    public int[] getSolutionSlot() {
        return solutionSlot;
    }

    @Override
    public int[] getSolutionRoom() {
        return solutionRoom;
    }

    @Override
    public void printSolution() {

    }
}
