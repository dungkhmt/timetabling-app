package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BacktrackingMaintainCombinations extends BacktrackingOneGroup{
    List<Integer>[] classIndicesOfCourse;
    Map<AClass, Integer> mClass2Index;
    int[] courseOfClass;
    public SolutionClass[] x;

    private CombinationConstraint comCtrs;
    public PrintWriter log = null;
    public boolean FINAL_CHECK = false;
    public boolean SORT_DOMAIN = true;
    public boolean USE_COMBINATION_CONSTRAINT = true;
    private void solution(){
        boolean ok = true;
        if(FINAL_CHECK) {
            CombinationChecker CC = new CombinationChecker(classIndicesOfCourse, classes, x);

            //for(int i = 0; i < x.length; i++){
            //System.out.println("solution x[" + i + "] = " + x[i]);
            //}

            for(int i = 0; i < classes.size(); i++){
                if(!CC.checkInCombination(i)){
                    ok  = false; break;
                }
            }
        }
        //System.out.println("Check = " + ok);
        if(ok){// each class belongs to at least one combination
            nbSolutions ++;
            /*
            // compute objective function
            obj = 0;
            for(int i = 0; i < courses.size(); i++){
                Map<AClass, SolutionClass> m = new HashMap<>();
                String crs = courses.get(i);
                List<AClass> CLS = mCourse2Classes.get(crs);
                for(AClass cls: CLS){
                    int idxClas = mClass2Index.get(cls);
                    m.put(cls,x[idxClas]);
                }
                MaxClique MC = new MaxClique();
                int a = MC.computeMaxClique(m);
                obj += a;
            }
            */
            if(obj < bestObj){
                bestObj = obj;
                best_x = new Map[courses.size()];
                for(int i = 0; i < courses.size(); i++){
                    best_x[i] = new HashMap<>();
                    for(AClass c: mCourse2Classes.get(courses.get(i))){
                        int idxClass = mClass2Index.get(c);
                        best_x[i].put(c,x[idxClass]);
                    }
                }
                if(comCtrs != null) nbCombinations = comCtrs.getNbCombinations();
                System.out.println("update best " + bestObj + " checkCombinations = " + ok);
            }
        }else{// failure
            nbFailures ++;
        }
    }
    private boolean check(SolutionClass sc, int i){
        return true;
    }
    private void tryClass(int idxClass, int idxCourse){
        double t = System.currentTimeMillis() - t0;
        if(t*0.001 > timeLimit){
            //System.out.println("RETURN time limit = " + timeLimit + " t = " + t);
            return;
        }
        nbTries++;
        int i = classIndicesOfCourse[idxCourse].get(idxClass);
        AClass cls = classes.get(i);
        ClassSolver CS = new ClassSolverLazyCheck(idxClass,idxCourse,this);
        int startSlot = 1;
        if(idxClass > 0){
            int ii = classIndicesOfCourse[idxCourse].get(idxClass-1);
            AClass clsi = classes.get(ii);
            if(cls.sameClassSegmentsWith(clsi) && cls.identicalClassSegment())
                startSlot = x[ii].periods.get(0)[0];
            else startSlot = 1;
        }

        List<SolutionClass> sol = CS.solve(cls,startSlot);
        if(LOGGING && log != null)log.println("tryClass(" + idxClass + "," + idxCourse + "), sol.sz = " + sol.size());
        if(LOGGING && log != null && comCtrs != null) comCtrs.print(idxCourse,log);
        for(SolutionClass sc: sol){
            boolean ok = true; List<Combination> newComs =  null;
            if(comCtrs != null) {
                newComs = comCtrs.check(sc, idxClass, idxCourse);
                ok = newComs.size() > 0;
            }
            //if(log != null) log.println("tryClass(" + idxClass + "," + idxCourse + ") newComs = " + newComs.size());
            //if(newComs.size() > 0) {
            if(ok){
                x[i] = sc;
                if(LOGGING && log != null) log.println("tryClass(" + idxClass + "," + idxCourse + ") accept " + sc.toString());

                if(comCtrs != null) comCtrs.propagate(sc,idxClass,idxCourse,newComs);
                if (idxClass == classIndicesOfCourse[idxCourse].size()-1) {
                    MaxClique MCSolver = new MaxClique();
                    Map<AClass, SolutionClass> Y = new HashMap<>();
                    for(int ii: classIndicesOfCourse[idxCourse]){
                        Y.put(x[ii].cls,x[ii]);
                    }
                    int mc = MCSolver.computeMaxClique(Y);
                    obj = obj + mc;
                    if(idxCourse == classIndicesOfCourse.length-1) {
                        solution();
                    }else{
                        if(BRANCH_AND_BOUND) {
                            if (obj + courses.size() - (idxCourse + 1) < bestObj) {
                                tryClass(0, idxCourse + 1);
                            }else{
                                //System.out.println("BOUND!!!");
                            }
                        }else{
                            tryClass(0, idxCourse + 1);
                        }
                    }
                    obj = obj - mc;
                } else {
                    tryClass(idxClass + 1,idxCourse);
                }
                if(comCtrs != null) comCtrs.backtrack(sc,idxClass,idxCourse,newComs);
            }
        }
    }
    public void solve(List<AClass> classes){
        this.classes = classes;
        mClass2Index = new HashMap<>();
        for(int i = 0; i < classes.size(); i++){
            mClass2Index.put(classes.get(i),i);
        }
        classIndicesOfCourse = new List[this.courses.size()];
        for(int i = 0; i < courses.size(); i++){
            classIndicesOfCourse[i] = new ArrayList<>();
            for(AClass cls: mCourse2Classes.get(courses.get(i))){
                int j = mClass2Index.get(cls);
                classIndicesOfCourse[i].add(j);
            }
        }
        t0 = System.currentTimeMillis();
        x = new SolutionClass[classes.size()];
        bestObj = 10000000;
        nbSolutions = 0;
        nbFailures = 0;
        nbTries = 0;
        if(USE_COMBINATION_CONSTRAINT)
            comCtrs = new CombinationConstraint(classes,classIndicesOfCourse,courseOfClass,this);
        else comCtrs = null;
        if(comCtrs != null) comCtrs.log = log;
        //comCtrs = null;
        if(comCtrs == null) FINAL_CHECK = true; else FINAL_CHECK = false;
        //BRANCH_AND_BOUND = true;
        tryClass(0,0);
    }
    public void start(){
        try{
            log = new PrintWriter("log.txt");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void finish(){
        if(log != null)log.close();
    }
    public static void main(String[] args){
        BacktrackingMaintainCombinations solver = new BacktrackingMaintainCombinations();
        //String filename = "data/fl2-3th-c-ext3.txt";
        //String filename = "data/ch1-3th-s.txt";
        //String filename = "data/it1-2nd.txt";

        String filename = "data/fl2-2nd-s-ext1.txt";
        solver.USE_COMBINATION_CONSTRAINT = true;
        solver.BRANCH_AND_BOUND = true;
        solver.SORT_DOMAIN = true;
        solver.LOGGING = false;
        int timeLimit = 600;
        solver.inputFile(filename);
        //solver.input();
        solver.start();
        solver.timeLimit = timeLimit;
        solver.solve(solver.classes);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
        solver.finish();
    }

}
