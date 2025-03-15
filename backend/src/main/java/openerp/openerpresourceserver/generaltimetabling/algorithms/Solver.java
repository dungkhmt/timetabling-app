package openerp.openerpresourceserver.generaltimetabling.algorithms;

import java.util.Map;

public interface Solver {
    public void solve();
    public boolean hasSolution();
    //public int[] getSolutionSlot();
    //public int[] getSolutionRoom();
    public Map<Integer, Integer> getMapSolutionSlot();
    public Map<Integer, Integer> getMapSolutionRoom();
    public void printSolution();
}
