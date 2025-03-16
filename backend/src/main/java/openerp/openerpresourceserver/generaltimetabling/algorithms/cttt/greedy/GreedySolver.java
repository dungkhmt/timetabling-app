package openerp.openerpresourceserver.generaltimetabling.algorithms.cttt.greedy;

import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoomOneGroup;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;

import java.io.File;
import java.util.*;

@Log4j2
public class GreedySolver implements Solver {
    MapDataScheduleTimeSlotRoom I;
    //int[] solutionSlot;// solutionSlot[i] is the start time-slot assigned to class-segment i
    //int[] solutionRoom; // solutionRoom[i] is the room assigned to class-segment i
    Map<Integer, Integer> solutionSlot;
    Map<Integer, Integer> solutionRoom;
    Map<Integer, Set<Integer>> conflictClassSegment;
    //HashSet<Integer>[] conflictClassSegment;// conflictClassSegment[i] is the list of class-segment conflict with class segment i

    List<Integer> unScheduledClassSegment;
    boolean foundSolution;
    List<ClassSegment> classSegments;
    public GreedySolver(MapDataScheduleTimeSlotRoom I){

        this.I = I;
        this.classSegments = I.getClassSegments();
        conflictClassSegment = new HashMap<>();
        for(ClassSegment cs: classSegments){
            conflictClassSegment.put(cs.getId(),new HashSet<>());
        }
        for(Integer[] p: I.getConflict()) {
            int i = p[0];
            int j = p[1];
            conflictClassSegment.get(i).add(j);
            conflictClassSegment.get(j).add(i);
        }
        /*
        conflictClassSegment = new HashSet[I.getClassSegments().length];
        for(int i = 0; i < I.getClassSegments().length; i++){
            conflictClassSegment[i] = new HashSet();
        }
        for(Integer[] p: I.getConflict()){
            int i = p[0]; int j = p[1];
            conflictClassSegment[i].add(j); conflictClassSegment[j].add(i);
        }

         */
    }

    private boolean overLap(int startSlot1, int duration1, int startSlot2, int duration2){
        if(startSlot1 + duration1 <= startSlot2 || startSlot2 + duration2 <= startSlot1) return false;
        return true;
    }
    private boolean check(int i, int s, int r){
        // check and return true if slot s and room r can be assigned to class segment i without violating constraintsa
        // explore all class segment j before i (have been assigned slot and room)
        ClassSegment cs = classSegments.get(i);
        int duration_i = cs.getDuration();//I.getClassSegments()[i].getDuration();
        int startSlot_i = s;

        for(int j = 0; j <= i-1; j++){
            ClassSegment csj = classSegments.get(j);
            int duration_j = csj.getDuration();//I.getClassSegments()[j].getDuration();
            int startSlot_j = solutionSlot.get(csj.getId());
            if(i == 4)log.info("check(" + i + "," + s + "," + r + " compare class-segment " + j + " having start_slot_j = " + startSlot_j + " duration_j = " + duration_j + " room " + solutionRoom.get(csj.getId()));
            if(conflictClassSegment.get(cs.getId()).contains(csj.getId())){// class segments i and j conflict
                if(overLap(startSlot_i,duration_i,startSlot_j,duration_j))
                    return false;
            }
            if(overLap(startSlot_i, duration_i,startSlot_j,duration_j)){
                if(solutionRoom.get(csj.getId()) == r) return false;
            }
        }
        return true;
    }


    public void greedy3() {
        unScheduledClassSegment = new ArrayList<>();
        for (int i = 0; i < I.getClassSegments().size(); i++) {
            ClassSegment cs = classSegments.get(i);
            boolean foundSlotRoom = false;
            Integer bestRoom = null;
            int minExcessCapacity = Integer.MAX_VALUE;
            for (int s : I.getDomains()[i]) { // Iterate over available time slots
                if (foundSlotRoom) break;
                for (int r : I.getRooms()[i]) { // Iterate over rooms in priority order
                    if (foundSlotRoom) break;
                    int excessCapacity = I.getRoomCapacity()[r] - cs.getNbStudents();
                    if (excessCapacity >= 0 && check(i, s, r)) {
                        // If it's the first valid room or a better fit, update bestRoom
                        if (excessCapacity < minExcessCapacity) {
                            bestRoom = r;
                            minExcessCapacity = excessCapacity;
                        }
                        // If we find a perfect fit (excessCapacity == 0), assign immediately
                        if (excessCapacity == 0) {
                            //solutionSlot[i] = s;
                            //solutionRoom[i] = r;
                            solutionSlot.put(cs.getId(),s);
                            solutionRoom.put(cs.getId(),r);
                            foundSlotRoom = true;
                            break;
                        }
                    }
                }
                // If no perfect fit was found, assign the best available room
                if (!foundSlotRoom && bestRoom != null) {
                    //solutionSlot[i] = s;
                    //solutionRoom[i] = bestRoom;
                    solutionSlot.put(cs.getId(),s);
                    solutionRoom.put(cs.getId(),bestRoom);
                    foundSlotRoom = true;
                }
            }
            if (!foundSlotRoom) {
                unScheduledClassSegment.add(i);
            }
        }
        foundSolution = unScheduledClassSegment.isEmpty();
    }
    public void greedy2(){
        // TODO by Chau
        // Try to make use of the rooms that have not been assigned first
        unScheduledClassSegment = new ArrayList<>();
        HashSet<Integer> usedRooms = new HashSet<>(); // Track rooms that have been assigned at any time
        for (int i = 0; i < I.getClassSegments().size(); i++) {
            ClassSegment cs = classSegments.get(i);
            boolean foundSlotRoom = false;
            //for (int s : I.getDomains()[i]) { // Iterate over possible time slots
            for (int s : cs.getDomainTimeSlots()){
                if (foundSlotRoom) break;
                //for (int r : I.getRooms()[i]) { // Iterate over possible rooms
                for (int r : cs.getDomainRooms()) { // Iterate over possible rooms
                    if (foundSlotRoom) break;
                    if (usedRooms.contains(r)) continue;
                    if (!usedRooms.contains(r) && check(i, s, r)) {
                        // Found a room that hasn't been assigned to any segment yet
                        //solutionSlot[i] = s;
                        //solutionRoom[i] = r;
                        solutionRoom.put(cs.getId(),r);
                        solutionSlot.put(cs.getId(),s);
                        usedRooms.add(r); // Mark this room as used
                        foundSlotRoom = true;
                    }
                }
                // If still no room found, iterate all over again to find the first-fit room
                if (!foundSlotRoom) {
                    //for (int r : I.getRooms()[i]) {
                    for (int r : cs.getDomainRooms()) {
                        if (check(i, s, r)) {
                            //solutionSlot[i] = s;
                            //solutionRoom[i] = r;
                            solutionSlot.put(cs.getId(),s);
                            solutionRoom.put(cs.getId(),r);
                            foundSlotRoom = true;
                            break; // Stop once we find a valid room
                        }
                    }
                }
            }
            if (!foundSlotRoom) {
                unScheduledClassSegment.add(i); // If no valid assignment, mark it as unscheduled
            }
        }
        foundSolution = unScheduledClassSegment.isEmpty();
    }
    public void simpleGreedy(){
        log.info("simpleGreedy START....");
        unScheduledClassSegment = new ArrayList<>();
        for(int i = 0; i < I.getClassSegments().size(); i++){
            ClassSegment cs = classSegments.get(i);
            // try to find a first-fit time-slot and room for class segment i
            log.info("simpleGreedy, start plan for class-segment " + i + ": list time-slots = " + I.getDomains()[i]);
            boolean foundSlotRoom = false;
            //for(int s: I.getDomains()[i]){
            for(int s: cs.getDomainTimeSlots()){
                if(foundSlotRoom) break;
                //for(int r: I.getRooms()[i]){
                for(int r: cs.getDomainRooms()){
                    if(foundSlotRoom) break;
                    if(check(i,s,r)){
                        //solutionSlot[i]= s; solutionRoom[i] = r; foundSlotRoom = true;
                        solutionSlot.put(cs.getId(),s);
                        solutionRoom.put(cs.getId(),r);
                        log.info("simpleGreedy, slot[" + i + "] = " + s + ", duration = " + cs.getDuration() + ", room[" + i + "] = ");
                    }
                }
            }
            if(!foundSlotRoom){
                unScheduledClassSegment.add(i);
            }
        }
        foundSolution = unScheduledClassSegment.size() == 0;
    }
    public boolean hasSolution(){
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
        // TODO
    }

    public void solve(){
        for(int i = 0; i < I.getClassSegments().size(); i++){
            ClassSegment cs = classSegments.get(i);
            System.out.println("class-segment " + i + ": nbSlot = " + cs.getDuration() + ", nbStudents = " + cs.getNbStudents());
            System.out.print("list of start time-slot for class-segment " + i + ": ");
            for(int s: I.getDomains()[i]) System.out.print(s + ", ");
            System.out.println();
            System.out.print("list of rooms can be assigned to class-segment " + i + ": ");
            for(int r: I.getRooms()[i]) System.out.print(r + ", ");
            System.out.println();
        }
        for(Integer[] p: I.getConflict()){
            System.out.println("Conflict between class-segment " + p[0] + " and " + p[1]);
        }
        //solutionSlot = new int[I.getClassSegments().length];
       // solutionRoom = new int[I.getClassSegments().length];
        //for(int i = 0; i < I.getClassSegments().length; i++){
        //    solutionRoom[i] = -1; solutionSlot[i] = -1; // NOT ASSIGNED/SCHEDULED
        //}
        solutionSlot = new HashMap<>();
        solutionRoom = new HashMap<>();
        for(ClassSegment cs: classSegments){
            solutionSlot.put(cs.getId(),-1);
            solutionRoom.put(cs.getId(),-1);
        }
        simpleGreedy();
        //greedy2();
        //greedy3();
        printSolution();
    }
    public void printSolution(){
        System.out.print("Unschedueld class-segments: ");
        for(int i: unScheduledClassSegment) System.out.print(i + ", ");
        System.out.println();
        for(int i = 0; i < I.getClassSegments().size(); i++){
            ClassSegment cs = classSegments.get(i);
            if(solutionSlot.get(cs.getId()) > -1){
                System.out.println("class-segment[" + i + "] slot = " + solutionSlot.get(cs.getId()) + " number students = " + cs.getNbStudents() + " room = " + solutionRoom.get(cs.getId())+ " room capacity = " + I.getRoomCapacity()[solutionRoom.get(cs.getId())]);
            }
        }
    }
    public static void main(String[] args){
        try{
            Gson gson = new Gson();
            Scanner in = new Scanner(new File("/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/Web/openerp-micro-service/timetabling-v2/backend/timetable.json"));
            String json = in.nextLine();
            in.close();
            MapDataScheduleTimeSlotRoom I = gson.fromJson(json,MapDataScheduleTimeSlotRoom.class);
            GreedySolver solver = new GreedySolver(I);
            solver.solve();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}