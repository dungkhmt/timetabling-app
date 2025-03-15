package openerp.openerpresourceserver.generaltimetabling.algorithms.hechuan;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
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
    public CourseBasedMultiClusterGreedySolver(MapDataScheduleTimeSlotRoom I){
        this.I = I;
    }

    public String name(){
        return "CourseBasedMultiClusterGreedySolver";
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
    public void printSolution() {

    }
}
