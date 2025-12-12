package openerp.openerpresourceserver.generaltimetabling.algorithms.aco;

import com.google.gson.Gson;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedayssearateclasssesamecourse.OneClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ConnectedComponentSolver;
import openerp.openerpresourceserver.generaltimetabling.model.ModelSchedulingLog;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Log4j2
public class FullClusters implements Solver {
    MapDataScheduleTimeSlotRoom I;
    Map<Integer, Integer> solutionSlot;
    Map<Integer, Integer> solutionRoom;
    boolean foundSolution;
    int timeLimit = 10000;
    int maxIterations = 50;
    int numAnts = 10;

    public FullClusters(MapDataScheduleTimeSlotRoom I){
        this.I = I;
    }

    public String name(){
        return "ACOSolver";
    }

    @Override
    public List<ModelSchedulingLog> getLogs() {
        return null;
    }

    public boolean checkTimeRoom(){
        for(int i = 0; i < I.getClassSegments().size(); i++){
            ClassSegment csi = I.getClassSegments().get(i);
            if(solutionRoom.get(csi.getId())==null) continue;
            for(int j = i+1; j < I.getClassSegments().size(); j++){
                ClassSegment csj = I.getClassSegments().get(j);
                if(solutionRoom.get(csj.getId())==null) continue;
                int si = solutionSlot.get(csi.getId());
                int sj = solutionSlot.get(csj.getId());
                if(Util.overLap(si,csi.getDuration(),sj,csj.getDuration())){
                    if(solutionRoom.get(csi.getId())==solutionRoom.get(csj.getId())){
                        log.info("check EXCEPTION class " + csi.getClassId() + " slot " + si + " duration " + csi.getDuration() + " and " + csj.getClassId() + " slot " + sj + " duration " + csj.getDuration() + " same room " + solutionRoom.get(csi.getId()));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void solve() {
        ConnectedComponentSolver connectedComponentSolver = new ConnectedComponentSolver();
        List<List<ClassSegment>> clusters = connectedComponentSolver.computeConnectedComponent(I.getClassSegments());

        solutionRoom = new HashMap<>();
        solutionSlot = new HashMap<>();
        foundSolution = false;
        int clusterCount = 0;
        for (List<ClassSegment> cluster : clusters) {
            clusterCount++;
            log.info("Solving cluster #" + clusterCount + " with " + cluster.size() + " segments...");

            MapDataScheduleTimeSlotRoom ICluster = new MapDataScheduleTimeSlotRoom(
                    I.getRoomCapacity(),
                    I.getMaxTeacherOfCourses(),
                    I.getConflict(),
                    I.getDomains(),
                    I.getRooms(),
                    I.getRoomOccupations(),
                    cluster,
                    I.getParams()
            );

            ACOTimetableSolver acoSolver = new ACOTimetableSolver(ICluster);
            acoSolver.solve();

            Map<Integer, Integer> clusterSlots = acoSolver.getMapSolutionSlot();
            Map<Integer, Integer> clusterRooms = acoSolver.getMapSolutionRoom();

            if (acoSolver.hasSolution()) {
                for (ClassSegment cs : cluster) {
                    Integer segId = cs.getId();
                    Integer assignedSlot = clusterSlots.get(segId);
                    Integer assignedRoom = clusterRooms.get(segId);

                    if (assignedSlot != null && assignedRoom != null) {
                        solutionSlot.put(segId, assignedSlot);
                        solutionRoom.put(segId, assignedRoom);
                        log.info("Cluster #" + clusterCount + " - Assigned Segment " + segId +
                                " to Slot " + assignedSlot + ", Room " + assignedRoom);
                    } else {
                        log.warn("Segment " + segId + " could not be assigned.");
                    }
                }
            } else {
                log.warn("ACO failed to solve cluster #" + clusterCount + ". Partial results may be used.");
            }
        }

        foundSolution = true;
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
        for (Integer segId : solutionSlot.keySet()) {
            log.info("Segment " + segId + " assigned to slot " + solutionSlot.get(segId) + ", room " + solutionRoom.get(segId));
        }
    }

    public static void main(String[] args){
        try{
            Gson gson = new Gson();
            Scanner in = new Scanner(new File("/Users/moctran/Desktop/HUST/2024.2/GraduationResearch/Web/deploy_version/timetabling-app/backend/data/timetable-647-cls-2025.03.21.22.23.14-T2-T6.json"));
            String json = in.nextLine();
            in.close();
            MapDataScheduleTimeSlotRoom I = gson.fromJson(json, MapDataScheduleTimeSlotRoom.class);
            FullClusters solver = new FullClusters(I);
            solver.solve();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
