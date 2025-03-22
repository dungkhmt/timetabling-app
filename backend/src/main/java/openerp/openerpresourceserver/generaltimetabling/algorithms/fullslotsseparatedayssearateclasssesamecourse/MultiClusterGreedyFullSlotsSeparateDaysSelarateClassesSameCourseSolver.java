package openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedayssearateclasssesamecourse;

import openerp.openerpresourceserver.generaltimetabling.algorithms.MapDataScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Solver;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ConnectedComponentSolver;

import java.util.List;
import java.util.Map;

public class MultiClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver implements Solver {

    MapDataScheduleTimeSlotRoom I;
    MultiClusterGreedyFullSlotsSeparateDaysSelarateClassesSameCourseSolver(MapDataScheduleTimeSlotRoom I){
        this.I = I;
    }
    @Override
    public void solve() {
        ConnectedComponentSolver connectedComponentSolver = new ConnectedComponentSolver();
        List<List<ClassSegment>> clusters = connectedComponentSolver.computeConnectedComponent(I.getClassSegments());

    }

    @Override
    public boolean hasSolution() {
        return false;
    }

    @Override
    public Map<Integer, Integer> getMapSolutionSlot() {
        return null;
    }

    @Override
    public Map<Integer, Integer> getMapSolutionRoom() {
        return null;
    }

    @Override
    public void setTimeLimit(int timeLimit) {

    }

    @Override
    public void printSolution() {

    }
}
