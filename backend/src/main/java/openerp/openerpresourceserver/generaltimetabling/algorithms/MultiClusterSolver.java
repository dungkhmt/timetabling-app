package openerp.openerpresourceserver.generaltimetabling.algorithms;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedays.BacktrackingSolverOneCluster;
import openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedays.CourseBasedConnectedClusterFullSlotsSeparateDaysGreedySolver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.hechuan.CourseBasedConnectedClusterGreedySolver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.hechuan.CourseBasedMultiClusterGreedySolver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ConnectedComponentSolver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester.SummerSemesterSolver;
import openerp.openerpresourceserver.generaltimetabling.common.Constants;

import java.util.*;

@Log4j2
public class MultiClusterSolver implements Solver {
    MapDataScheduleTimeSlotRoom I;
    MapDataScheduleTimeSlotRoomWrapper D;
    Map<Integer, Integer> solutionSlot;
    Map<Integer, Integer> solutionRoom;
    Map<Integer, List<ClassSegment>> classesScheduledInSlot; // classesScheduledInSlot.get(s) is the list of classes scheduled in time slot s

    boolean foundSolution;
    int timeLimit = 10000;// 10 seconds by defalut
    String oneClusterAlgorithm = "";
    Solver oneClusterSolver = null;
    public MultiClusterSolver(MapDataScheduleTimeSlotRoomWrapper D){
        this.I = D.data; this.D = D;
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
        classesScheduledInSlot = new HashMap<>();
        Set<Integer> allDomainSlots = new HashSet<>();
        for(ClassSegment cs: I.getClassSegments()){
            for(int s: cs.getDomainTimeSlots())
                allDomainSlots.add(s);
        }
        for(int s: allDomainSlots) classesScheduledInSlot.put(s, new ArrayList<>());

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
            MapDataScheduleTimeSlotRoomWrapper W = new MapDataScheduleTimeSlotRoomWrapper(IC,D.mClassSegment2Class,D.mClassSegment2RoomReservation,D.mIndex2Room,null);
            if(oneClusterAlgorithm.equals(Constants.MANY_CLASS_PER_COURSE_MAX_REGISTRATION_OPPORTUNITY_GREEDY_1))
                oneClusterSolver = new CourseBasedConnectedClusterGreedySolver(IC);
            else if(oneClusterAlgorithm.equals(Constants.MANY_CLASS_PER_COURSE_FULL_SLOTS_SEPARATE_DAYS))
                oneClusterSolver = new CourseBasedConnectedClusterFullSlotsSeparateDaysGreedySolver(IC,classesScheduledInSlot);
            else if(oneClusterAlgorithm.equals(Constants.SUMMER_SEMESTER)){
                oneClusterSolver = new SummerSemesterSolver(W);
            }else if(oneClusterAlgorithm.equals(Constants.ALGO_BACKTRACKING_ONE_CLUSTER))
                oneClusterSolver = new BacktrackingSolverOneCluster(W);

            oneClusterSolver.setTimeLimit(timeLimit);
            oneClusterSolver.solve();
            if(oneClusterSolver.hasSolution()){
                foundSolution = true;
                for(ClassSegment cs: C){
                    int slot = oneClusterSolver.getMapSolutionSlot().get(cs.getId());
                    int room = oneClusterSolver.getMapSolutionRoom().get(cs.getId());
                    solutionRoom.put(cs.getId(),room);
                    solutionSlot.put(cs.getId(),slot);
                    for(int s = 1; s <= cs.getDuration(); s++){
                        int sl = slot + s - 1;
                        //I.getRoomOccupations()[room].add(sl);
                    }
                    //log.info("solve, Cluster " + cs.getGroupNames() + " SET solutionSlot.put(" + cs.getId() + "," + slot + ") solutionRoom.put(" + cs.getId() + "," + room + ")");
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
