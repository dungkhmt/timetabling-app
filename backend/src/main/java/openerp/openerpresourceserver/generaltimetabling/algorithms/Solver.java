package openerp.openerpresourceserver.generaltimetabling.algorithms;

public interface Solver {
    public void solve();
    public boolean hasSolution();
    public int[] getSolutionSlot();
    public int[] getSolutionRoom();
    public void printSolution();
}
