package openerp.openerpresourceserver.generaltimetabling.algorithms.hechuan;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ConnectedComponentSolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class CourseBasedMultiClusterGreedySolver implements Solver {
    MapDataScheduleTimeSlotRoom I;
    Map<Integer, Integer> solutionSlot;
    Map<Integer, Integer> solutionRoom;
    boolean foundSolution;
    int timeLimit = 10000;// 10 seconds by defalut
    public CourseBasedMultiClusterGreedySolver(MapDataScheduleTimeSlotRoom I){
        this.I = I;
    }

    public String name(){
        return "CourseBasedMultiClusterGreedySolver";
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
        solutionRoom= new HashMap<>();
        solutionSlot = new HashMap<>();
        foundSolution = false;
        int cnt = 0;
        for(List<ClassSegment> C: clusters){
            cnt++;
            log.info(name() + "::solver, get a cluster " + cnt + "/" + clusters.size() + ": ");
            //for(ClassSegment cs: C){
            //    log.info(cs.toString());
            //}
            MapDataScheduleTimeSlotRoom IC = new MapDataScheduleTimeSlotRoom(
                    I.getRoomCapacity(),
                    I.getMaxTeacherOfCourses(),
                    I.getConflict(),
                    I.getDomains(),
                    I.getRooms(),
                    I.getRoomOccupations(),
                    C
            );

            CourseBasedConnectedClusterGreedySolver solver = new CourseBasedConnectedClusterGreedySolver(IC);
            solver.timeLimit = timeLimit;
            solver.solve();
            if(solver.hasSolution()){
                foundSolution = true;
                for(ClassSegment cs: C){
                    int slot = solver.getMapSolutionSlot().get(cs.getId());
                    int room = solver.getMapSolutionRoom().get(cs.getId());
                    solutionRoom.put(cs.getId(),room);
                    solutionSlot.put(cs.getId(),slot);
                    for(int s = 1; s <= cs.getDuration(); s++){
                        int sl = slot + s - 1;
                        //I.getRoomOccupations()[room].add(sl);
                    }
                    log.info("solve, Cluster " + cs.getGroupNames() + " SET solutionSlot.put(" + cs.getId() + "," + slot + ") solutionRoom.put(" + cs.getId() + "," + room + ")");
                }
                if(!checkTimeRoom()){
                    log.info("Post check time-slot and room conflict FAILED???"); break;
                }else{
                    log.info("Post check time-slot and room conflict PASS");
                }
            }else{

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
