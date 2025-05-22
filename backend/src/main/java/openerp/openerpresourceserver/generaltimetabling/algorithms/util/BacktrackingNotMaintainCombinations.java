package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.util.*;

public class BacktrackingNotMaintainCombinations extends BacktrackingOneGroup{
    List<Integer>[] classIndicesOfCourse;
    Map<AClass, Integer> mClass2Index;
    int[] courseOfClass;
    private SolutionClass[] x;

    private CombinationConstraint comCtrs;

    private void solution(){
        CombinationChecker CC = new CombinationChecker(classIndicesOfCourse,classes,x);
        for(int i = 0; i < x.length; i++){
            //System.out.println("solution x[" + i + "] = " + x[i]);
        }
        boolean ok = true;
        for(int i = 0; i < classes.size(); i++){
            if(!CC.checkInCombination(i)){
                ok  = false; break;
            }
        }
        //System.out.println("Check = " + ok);
        if(ok){// each class belongs to at least one combination
            nbSolutions ++;
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

                System.out.println("update best " + bestObj + " checkCombinations = " + ok);
            }
        }else{// failure
            nbFailures ++;
        }
    }
    private boolean check(SolutionClass sc, int i){
        return true;
    }
    private void tryClass(int i){
        nbTries++;
        AClass cls = classes.get(i);
        ClassSolver CS = new ClassSolverLazyCheck(0,0,null);
        List<SolutionClass> sol = CS.solve(cls);
        //System.out.println("tryClass(" + i + "), sol.sz = " + sol.size());
        for(SolutionClass sc: sol){
            if(check(sc,i)) {
                x[i] = sc;
                if (i == classes.size() - 1) {
                    solution();
                } else {
                    tryClass(i + 1);
                }
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
        tryClass(0);
    }
    public static void main(String[] args){
        BacktrackingNotMaintainCombinations solver = new BacktrackingNotMaintainCombinations();
        String filename = "data/fl2-3th-c-ext2.txt";
        //String filename = "data/fl2-4th-s.txt";

        int timeLimit = 300;
        solver.inputFile(filename);
        //solver.input();
        solver.solve(solver.classes);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
    }
}
