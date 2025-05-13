package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

public class BacktrackingOneGroupSortedDomainBaB extends BacktrackingOneGroup{
    public static void main(String[] args){
        BacktrackingOneGroupSortedDomainBaB solver = new BacktrackingOneGroupSortedDomainBaB();
        solver.BRANCH_AND_BOUND = true;
        solver.STRATEGY = BacktrackingOneGroup.SOLVE_CLASS_WITH_SORTED_DOMAIN;

        //solver.postCheck = true;
        solver.inputFile("data/it1-2nd.txt");
        //solver.input();
        solver.solveNew(120, true);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
    }

}
