package openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedayssearateclasssesamecourse;

import com.nimbusds.jose.shaded.gson.Gson;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.*;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.ModelSchedulingLog;

import java.io.File;
import java.util.*;

@Log4j2
public class OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver implements Solver {

    MapDataScheduleTimeSlotRoom I;
    Map<Integer, Integer> solutionSlot;
    Map<Integer, Integer> solutionRoom;
    Map<Integer, Set<Integer>> conflictClassSegment;
    Map<Integer, Set<String>> relatedCourseGroups;
    List<ClassSegment> classSegments = null;
    List<ClassSegment> unassignedSegments;
    boolean foundSolution;
    int timeLimit;

    public OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver(MapDataScheduleTimeSlotRoom I){
        this.I = I;
        this.conflictClassSegment = new HashMap<>();
        for (ClassSegment cs : I.getClassSegments()){
            this.conflictClassSegment.put(cs.getId(), new HashSet<>());
        }
        for (Integer[] p : I.getConflict()){
            int i = p[0];
            int j = p[1];
            if (this.conflictClassSegment.get(i) != null && this.conflictClassSegment.get(j) != null){
                this.conflictClassSegment.get(i).add(j);
                this.conflictClassSegment.get(j).add(i);
            }
        }
        this.classSegments = I.getClassSegments();
        this.relatedCourseGroups = new HashMap<>();
        for(ClassSegment cs: I.getClassSegments()){
            this.relatedCourseGroups.put(cs.getId(), new HashSet<>());
            for (ClassSegment csi : I.getClassSegments()){
                String courseGroupId = csi.hashCourseGroup();
                relatedCourseGroups.get(cs.getId()).add(courseGroupId);
            }
        }

    }
    public String name(){
        return "OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver";
    }

    @Override
    public List<ModelSchedulingLog> getLogs() {
        return null;
    }

    public void testPrint(){
        for(int i = 0; i < I.getClassSegments().size(); i++){
            log.info("testPrint class-segment[" + i + "]: " + I.getClassSegments().get(i));
        }
        ClassSegment cs = I.getClassSegments().get(0);
        for(int slotIndex: cs.getDomainTimeSlots()){
            DaySessionSlot dss = new DaySessionSlot(slotIndex);
            log.info("testPrint slotIndex " + slotIndex + " <-> " + dss);
        }
    }
    @Override
    public void solve() {
        log.info(name() + "::solve start... number of class-segments = " + I.getClassSegments().size());
        solutionRoom = new HashMap<>();
        solutionSlot = new HashMap<>();
        foundSolution = false;
        unassignedSegments = new ArrayList<>();

        // Group segments according to course index
        Map<String, List<ClassSegment>> courseMap = new HashMap<>();
        for (ClassSegment cs : classSegments) {
            courseMap.computeIfAbsent(String.valueOf(cs.getCourseIndex()), k -> new ArrayList<>()).add(cs);
        }

        // Log out the course mapping information
        for (String courseId : courseMap.keySet()) {
            List<Integer> segmentIds = courseMap.get(courseId).stream().map(ClassSegment::getId).toList();
            log.info("Course " + courseId + " has class segments: " + segmentIds);
        }

        // Iterate through every course -> Assign segments in each course
        for (String courseIndex : courseMap.keySet()) {
            List<ClassSegment> segments = courseMap.get(courseIndex);
            segments.sort(Comparator.comparingInt(ClassSegment::getInstanceIndex));

            List<ClassSegment> assignedSegments = new ArrayList<>();

            for (ClassSegment cs : segments) {
                List<Integer> domain = new ArrayList<>(cs.getDomainTimeSlots());

                if (assignedSegments.isEmpty()) {
                    // If it's the first segment to be assigned -> Assign it asap
                    domain.sort(Comparator.naturalOrder());
                } else {
                    // else remove overlapping time slots + assign it as closely as possible to assigned segments
                    domain.removeIf(t -> distance(t, cs.getDuration(), assignedSegments) == 0);
                    domain.sort(Comparator.comparingInt(t -> distance(t, cs.getDuration(), assignedSegments)));
                }

                log.info(cs.getId() + " trying domain without overlap: " + domain);

                boolean assigned = tryAssign(cs, domain, assignedSegments);

                if (!assigned) {
                    domain = new ArrayList<>(cs.getDomainTimeSlots());
                    domain.sort(Comparator.naturalOrder());
                    log.info(cs.getId() + " fallback: trying full domain again: " + domain);
                    assigned = tryAssign(cs, domain, assignedSegments);
                }

                if (!assigned) {
                    log.info("Could not assign class segment: " + cs.getId());
                    unassignedSegments.add(cs);
                }
            }
        }

        foundSolution = (solutionSlot.size() + unassignedSegments.size()) == classSegments.size();
    }
    private boolean tryAssign(ClassSegment cs, List<Integer> domain, List<ClassSegment> assignedSegments) {
        for (int time : domain) {
            for (int room : cs.getDomainRooms()) {
                if (isAssignable(cs, time, room)) {
                    assign(cs, time, room);
                    assignedSegments.add(cs);
                    return true;
                }
            }
        }
        return false;
    }
    private int distance(int start1, int duration1, int start2, int duration2) {
        int end1 = start1 + duration1 - 1;
        int end2 = start2 + duration2 - 1;
        boolean notOverlap = start1 > end2 || start2 > end1;
        if (!notOverlap) return 0;
        return Math.min(Math.abs(start1 - end2), Math.abs(start2 - end1));
    }

    private int distance(int start, int duration, List<ClassSegment> cls) {
        int minD = Integer.MAX_VALUE;
        for (ClassSegment cs : cls) {
            if (solutionSlot.get(cs.getId()) == null) continue;
            int s = solutionSlot.get(cs.getId());
            int d = cs.getDuration();
            int dis = distance(start, duration, s, d);
            minD = Math.min(minD, dis);
        }
        return minD;
    }
    private boolean isAssignable(ClassSegment cs, int time, int room) {
        // Check if room is available for all time slots in duration
        for (int i = 0; i < cs.getDuration(); i++) {
            if (I.getRoomOccupations()[room].contains(time + i)) return false;
        }

        int overlappingSameCourse = 0;
        int maxTeacher = I.getMaxTeacherOfCourses()[cs.getCourseIndex()];

        for (ClassSegment other : classSegments) {
            if (!solutionSlot.containsKey(other.getId())) continue;

            int assignedTime = solutionSlot.get(other.getId());
            int duration = other.getDuration();

            // 1. Check for room-time conflicts via precomputed conflict list
            if (conflictClassSegment.get(cs.getId()).contains(other.getId()) &&
                    Util.overLap(time, cs.getDuration(), assignedTime, duration)) {
                return false;
            }

            // 2. Prevent overlapping segments of the same class
            if (other.getClassId().equals(cs.getClassId()) &&
                    Util.overLap(time, cs.getDuration(), assignedTime, duration)) {
                return false;
            }

            // 3. Count overlapping segments of the same course
            if (other.getCourseIndex() == cs.getCourseIndex() &&
                    Util.overLap(time, cs.getDuration(), assignedTime, duration)) {
                overlappingSameCourse++;
            }
        }

        if (overlappingSameCourse >= maxTeacher) {
            return false;  // Not enough teachers available
        }

        return true;
    }


    private void assign(ClassSegment cs, int time, int room) {
        solutionSlot.put(cs.getId(), time);
        solutionRoom.put(cs.getId(), room);
        for (int i = 0; i < cs.getDuration(); i++) {
            I.getRoomOccupations()[room].add(time + i);
        }
        log.info("Assigned segment " + cs.getId() + "  of course " + cs.getCourseIndex() + " with max teachers " + I.getMaxTeacherOfCourses()[cs.getCourseIndex()] + " with duration " + cs.getDuration() + " with index " + cs.getInstanceIndex() + " to time " + time + ", room " + room);
    }
    @Override
    public boolean hasSolution() {
        return false;
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

    public static void main(String[] args){
        try{
            Gson gson = new Gson();
            Scanner in = new Scanner(new File("data//timetable-22-cls-2025.03.21.22.27.55-T2-T6-3groups.json"));
            String json = in.nextLine();
            in.close();
            MapDataScheduleTimeSlotRoom I = gson.fromJson(json, MapDataScheduleTimeSlotRoom.class);
            OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver solver = new OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver(I);
            solver.solve();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


