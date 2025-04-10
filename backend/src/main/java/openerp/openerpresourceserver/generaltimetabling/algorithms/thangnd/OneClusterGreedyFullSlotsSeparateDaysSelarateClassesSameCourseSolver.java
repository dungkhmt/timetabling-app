package openerp.openerpresourceserver.generaltimetabling.algorithms.thangnd;

import com.nimbusds.jose.shaded.gson.Gson;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
@Log4j2
public class OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver implements Solver {

    MapDataScheduleTimeSlotRoom I;
    Map<Integer, Integer> solutionSlot;
    Map<Integer, Integer> solutionRoom;
    public OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver(MapDataScheduleTimeSlotRoom I){
        this.I = I;
    }
    public String name(){
        return "OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver";
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
        // TODO by ChauTT

        log.info(name() + "::solve start... number of class-segments = " + I.getClassSegments().size());
        testPrint();
        solutionRoom = new HashMap<>();
        solutionSlot = new HashMap<>();
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
