package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.util.*;
class CombinationChecker{
    private List<AClass> classes;
    private List<String> courses;
    int nbCourses;
    int nbClasses;
    private SolutionClass[] x;//x[i] is the solution for class i
    private boolean[][] overlap; // overlap[i][j] = true means that class i and j overlap
    List<Integer>[] classIndicesOfCourse;
    int[] courseOfClass;
    private int idxClass;
    private int idxCourse;
    private int[] y;//
    private boolean ans;

    private boolean check(int v, int i){
        for(int j = 0; j <= i-1; j++){
            if(overlap[y[j]][v]) return false;
        }
        return true;
    }
    private void solution(){
        ans = true;
    }
    private void tryY(int i){
        if(ans) return;
        if(i == idxCourse){
            if(check(idxClass,i)){
                y[i] = idxClass;
                if(i == classIndicesOfCourse.length-1){
                    solution();
                }else{
                    tryY(i+1);
                }
            }
            return;
        }
        for(int v: classIndicesOfCourse[i]){
            if(check(v,i)){
                y[i] = v;
                if(i == classIndicesOfCourse.length-1){
                    solution();
                }else{
                    tryY(i+1);
                }
            }
        }
    }

    public CombinationChecker(List<Integer>[] classIndicesOfCourse, List<AClass> classes, SolutionClass[] x){
        this.classIndicesOfCourse = classIndicesOfCourse; this.classes = classes; this.x = x;
        nbCourses = classIndicesOfCourse.length;
        nbClasses = classes.size();
        courseOfClass = new int[nbClasses];
        for(int i = 0; i < nbCourses; i++){
            for(int j: classIndicesOfCourse[i]){
                courseOfClass[j] = i;
            }
        }
        overlap = new boolean[classes.size()][classes.size()];
        for(int i = 0; i < classes.size(); i++) {
            for (int j = 0; j < classes.size(); j++) {
                overlap[i][j] = x[i].overlap(x[j]);
            }
        }
    }
    public boolean checkInCombination(int idxClass){
        this.idxClass = idxClass;
        idxCourse = courseOfClass[idxClass];
        ans = false;
        y = new int[nbCourses];
        tryY(0);
        return ans;
    }
}
public class BacktrackingNotMaintainCombinations extends BacktrackingOneGroup{
    List<Integer>[] classIndicesOfCourse;
    Map<AClass, Integer> mClass2Index;
    int[] courseOfClass;
    private SolutionClass[] x;

    private void solution(){
        CombinationChecker CC = new CombinationChecker(classIndicesOfCourse,classes,x);
        boolean ok = true;
        for(int i = 0; i < classes.size(); i++){
            if(!CC.checkInCombination(i)){
                ok  = false; break;
            }
        }
        if(ok){// each class belongs to at least one combination
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

                if(LOGGING) System.out.println("update best " + bestObj + " checkCombinations = " + ok);
            }
        }
    }
    private void tryClass(int i){
        AClass cls = classes.get(i);
        ClassSolver CS = new ClassSolverLazyCheck();
        List<SolutionClass> sol = CS.solve(cls);
        for(SolutionClass sc: sol){
            x[i] = sc;
            if(i == classes.size()-1){
                solution();
            }else{
                tryClass(i+1);
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
        x = new SolutionClass[classes.size()];
        tryClass(0);
    }
    public static void main(String[] args){
        BacktrackingNotMaintainCombinations solver = new BacktrackingNotMaintainCombinations();
        String filename = "data/fl2-3th-c.txt";
        int timeLimit = 300;
        solver.inputFile(filename);
        //solver.input();
        solver.solve(solver.classes);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
    }
}
