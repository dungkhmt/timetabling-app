package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

public class BacktrackingOneGroupNonSortedDomainBaB extends BacktrackingOneGroup{
    public static void main(String[] args){
        BacktrackingOneGroupNonSortedDomainBaB solver = new BacktrackingOneGroupNonSortedDomainBaB();

        //solver.postCheck = true;
        solver.inputFile("data/it1-2nd.txt");
        //solver.input();
        solver.solveNew(120, true);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
    }
}
