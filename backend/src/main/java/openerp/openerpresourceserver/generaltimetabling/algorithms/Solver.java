package openerp.openerpresourceserver.generaltimetabling.algorithms;

import openerp.openerpresourceserver.generaltimetabling.model.ModelSchedulingLog;

import java.util.List;
import java.util.Map;

public interface Solver {
    public void solve();
    public boolean hasSolution();
    //public int[] getSolutionSlot();
    //public int[] getSolutionRoom();
    public Map<Integer, Integer> getMapSolutionSlot();
    public Map<Integer, Integer> getMapSolutionRoom();
    public void setTimeLimit(int timeLimit);
    public void printSolution();
    public String name();
    public List<ModelSchedulingLog> getLogs();
}
