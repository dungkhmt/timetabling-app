package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

//import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;

import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;

import java.io.*;
import java.util.*;

interface ClassSolver{
    public List<SolutionClass> solve(int indexClass, AClass cls, int startSlot, SolutionClass[] x, List<Combination> combinations, boolean checkCombination);
    public List<SolutionClass> solve(AClass cls);
    public List<SolutionClass> solve(AClass cls, int startSlot);

}
class ClassSolverLazyCheck implements  ClassSolver{
    AClass cls;
    List<SolutionClass> solutionClass;// solutionClass[i] is a map of assignment c
    int nbClassSegments;
    List<AClassSegment> classSegments;
    List<List<Integer>> domains;// domains[i] is the list of sorted values of class-segment i
    int[] x_session;// x[i] is the start slot of class segment i
    int[] x_slot;
    public int startSlot = 1;
    public int idxClass;
    public int idxCourse;
    public BacktrackingMaintainCombinations solver;

    public ClassSolverLazyCheck(int idxClass, int idxCourse, BacktrackingMaintainCombinations backtrackingMaintainCombinations){
        this.idxClass = idxClass; this.idxCourse = idxCourse;
        this.solver = backtrackingMaintainCombinations;
        int i = backtrackingMaintainCombinations.classIndicesOfCourse[idxCourse].get(idxClass);
        this.cls = backtrackingMaintainCombinations.classes.get(i);
        classSegments = cls.classSegments;
        domains = new ArrayList<>();
        for(int j = 0; j < classSegments.size(); j++){
            AClassSegment cs = classSegments.get(j);
            List<Integer> D = new ArrayList<>();
            if(!solver.SORT_DOMAIN) {
                for(int s = startSlot; s <= solver.nbSessions*solver.nbSlotPerSession;s++) {
                    SessionSlot ss = new SessionSlot(s);
                    if(ss.slot + cs.duration - 1 <= solver.nbSlotPerSession) {
                        D.add(s);
                    }
                }
            }else {
                if (idxClass == 0) {
                    //for(int s = startSlot; s <= Constants.nbSessions*Constants.nbSlotPerSession;s++) {
                    for (int s = startSlot; s <= solver.nbSessions * solver.nbSlotPerSession; s++) {
                        SessionSlot ss = new SessionSlot(s);
                        if (ss.slot + cs.duration - 1 <= solver.nbSlotPerSession) {
                            D.add(s);
                        }
                    }
                } else {
                    // try FIRST consecutive-same session with scheduled classes (same course) slots
                    for (int k = 0; k < idxClass; k++) {
                        int ik = backtrackingMaintainCombinations.classIndicesOfCourse[idxCourse].get(k);
                        SolutionClass sc = backtrackingMaintainCombinations.x[ik];
                        // find appropriate slot right-before or after scheduled class j
                        for (int[] p : sc.periods) {// p[0] is start, p[1] is end, p[2] is session
                            int sl = p[1] + 1;
                            SessionSlot ss = new SessionSlot(p[2], sl);
                            //log("sl = " + sl + ", ss.slot = " + ss.slot + ", cs.dur = " + cs.duration + ", slotPerSession = " + solver.nbSlotPerSession);
                            if (ss.slot + cs.duration - 1 <= solver.nbSlotPerSession) {
                                //    log("ACCEPT sl = " + sl + ", ss.slot = " + ss.slot + ", cs.dur = " + cs.duration + ", slotPerSession = " + solver.nbSlotPerSession);
                                if (ss.hash() >= startSlot) D.add(ss.hash());
                            }
                            sl = p[0] - cs.duration;
                            if (sl >= 1) {
                                ss = new SessionSlot(p[2], sl);
                                if (ss.hash() >= startSlot) D.add(ss.hash());
                            }
                        }
                    }
                    //
                    for (int sl = startSlot; sl <= solver.nbSessions * solver.nbSlotPerSession - cs.duration + 1; sl++) {
                        SessionSlot ss = new SessionSlot(sl);
                        if (ss.slot + cs.duration - 1 <= solver.nbSlotPerSession) {
                            boolean ok = true;
                            for (int k = 0; k < idxClass; k++) {
                                int ik = backtrackingMaintainCombinations.classIndicesOfCourse[idxCourse].get(k);
                                SolutionClass sc = backtrackingMaintainCombinations.x[ik];
                                //SessionSlot ss = new SessionSlot(sl);
                                if (sc.overlap(ss.slot, cs.duration + ss.slot - 1, ss.session)) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok) {
                                //log("ACCEPT " + sl);
                                if (!D.contains(sl)) D.add(sl);
                            }
                        }
                    }
                    for (int sl = startSlot; sl <= solver.nbSessions * solver.nbSlotPerSession - cs.duration + 1; sl++) {
                        SessionSlot ss = new SessionSlot(sl);
                        if (ss.slot + cs.duration - 1 <= solver.nbSlotPerSession) {
                            if (!D.contains(sl)) D.add(sl);
                        }
                    }
                }
            }
            domains.add(D);
        }
        // sort domains

    }
    @Override
    public List<SolutionClass> solve(int indexClass, AClass cls, int startSlot, SolutionClass[] x, List<Combination> combinations, boolean checkCombination) {
        return null;
    }
    private void solution(){
        List<int[]> periods = new ArrayList<>();
        for(int i = 0; i < classSegments.size(); i++){
            int[] S = new int[3];
            S[0] = x_slot[i]; S[1] = x_slot[i] + classSegments.get(i).duration-1;
            S[2] = x_session[i];
            periods.add(S);
        }
        SolutionClass sc = new SolutionClass(cls,periods);
        //System.out.println("ClassSolver, got solutionclass " + sc);
        solutionClass.add(sc);
    }
    private boolean check(int i, int ses, int sl){
        AClassSegment csi = classSegments.get(i);
        for(int j = 0; j <= i-1; j++){
            AClassSegment csj = classSegments.get(j);
            if(ses == x_session[j] && Util.overLap(sl,csi.duration,x_slot[j],csj.duration))
                return false;
        }
        return true;
    }
    public void log(String msg){
        if(solver.LOGGING && solver.log != null) solver.log.println(msg);
    }

    private void tryClassSegment(int i, int startSlot){
        AClassSegment cs = classSegments.get(i);
        //for(int s = startSlot; s <= Constants.nbSessions*Constants.nbSlotPerSession;s++){
        //solver.log.println("tryClassSegment(" + i + "), domain = " + domains.get(i));
        log("tryClassSegment(" + i + "), domain = " + domains.get(i));
        for(int s: domains.get(i)){
            SessionSlot ss = new SessionSlot(s);
            if(ss.slot + cs.duration - 1 <= Constants.nbSlotPerSession){
                if(check(i,ss.session,ss.slot)){
                    x_session[i] = ss.session; x_slot[i] = ss.slot;
                    if(i == classSegments.size()-1){
                        solution();
                    }else{
                        //tryClassSegment(i+1,s + cs.duration);
                        tryClassSegment(i+1,startSlot);// startSlot does matter in this case
                    }
                }
            }
        }
    }

    @Override
    public List<SolutionClass> solve(AClass cls) {
        this.cls = cls;
        solutionClass = new ArrayList<>();
        classSegments = cls.classSegments;
        x_session = new int[classSegments.size()];
        x_slot = new int[classSegments.size()];
        //tryClassSegment(0,1);
        tryClassSegment(0,startSlot);
        return solutionClass;
    }

    @Override
    public List<SolutionClass> solve(AClass cls, int startSlot) {
        this.startSlot = startSlot;
        return solve(cls);
    }
}
class ClassSolverNaive implements ClassSolver{
    List<Combination> combinations;
    boolean checkCombination;
    AClass cls;
    List<SolutionClass> solutionClass;// solutionClass[i] is a map of assignment c
    int nbSessions = 5;
    int nbSlotPerSession = 6;
    int nbClassSegments;
    List<AClassSegment> classSegments;
    int[] x_session;// x[i] is the start slot of class segment i
    int[] x_slot;
    private boolean check(int i, int ses, int sl){
        AClassSegment cs = classSegments.get(i);
        if(checkCombination) {
            for (Combination com : combinations) {
                boolean ok = true;
                for (SolutionClass sc : com.solutionClasses) {
                    if (sc.overlap(sl, sl + cs.duration - 1, ses)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) return true;
            }
            return false;
        }else{
            return true;
        }
        //if(i == 0) return true;
        //if(x_session[i-1] < ses) return true;
        //if(x_session[i-1] > ses) return false;
        //if(x_slot[i-1] + classSegments.get(i-1).duration <= sl) return true;
        //else return false;
    }
    private void solution(){
        List<int[]> periods = new ArrayList<>();
        for(int i = 0; i < classSegments.size(); i++){
            int[] S = new int[3];
            S[0] = x_slot[i]; S[1] = x_slot[i] + classSegments.get(i).duration-1;
            S[2] = x_session[i];
            periods.add(S);
        }
        SolutionClass sc = new SolutionClass(cls,periods);
        //System.out.println("ClassSolver, got solutionclass " + sc);
        solutionClass.add(sc);
    }
    private void tryClassSegment(int i, int startSlot){

        AClassSegment cs= classSegments.get(i);
        for(int s = startSlot; s <= Constants.nbSessions*Constants.nbSlotPerSession;s++){
            SessionSlot ss = new SessionSlot(s);
            if(ss.slot + cs.duration - 1 <= Constants.nbSlotPerSession){
                if(check(i,ss.session,ss.slot)){
                    x_session[i] = ss.session; x_slot[i] = ss.slot;
                    if(i == classSegments.size()-1){
                        solution();
                    }else{
                        tryClassSegment(i+1,s + cs.duration);
                    }
                }
            }
        }
        /*
        int startSession = 0;
        if(i > 0) startSession = x_session[i-1];
        //System.out.println("tryClassSegment(" + i + ", startSession = " + startSession);
        for(int ses = startSession; ses < nbSessions; ses++){
            int startSlot = 1;
            if(i > 0 && startSession == x_session[i-1]) startSlot = x_slot[i-1] + classSegments.get(i-1).duration;
            //System.out.println("tryClassSegment(" + i + ", startSession = " + startSession + " startSlot = " + startSlot);
            for(int sl = startSlot; sl <= nbSlotPerSession-classSegments.get(i).duration+1; sl++){
                if(check(i,ses,sl)){
                    x_session[i] = ses; x_slot[i] = sl;
                    if(i == classSegments.size()-1){
                        solution();
                    }else{
                        tryClassSegment(i+1);
                    }
                }
            }
        }
        */
    }
    public static final String name= "ClassSolverNaive";
    public List<SolutionClass> solve(int indexClass, AClass cls, int startSlot, SolutionClass[] x, List<Combination> combinations, boolean checkCombination){
        //System.out.println(name + "::solve");
        this.cls = cls;
        this.combinations = combinations;
        this.checkCombination = checkCombination;
        solutionClass = new ArrayList<>();
        classSegments = cls.classSegments;
        x_session = new int[classSegments.size()];
        x_slot = new int[classSegments.size()];
        tryClassSegment(0,startSlot);
        return solutionClass;
    }

    @Override
    public List<SolutionClass> solve(AClass cls) {
        return null;
    }

    @Override
    public List<SolutionClass> solve(AClass cls, int startSlot) {
        return null;
    }
}
class ClassSolverSorted implements ClassSolver{
    List<Combination> combinations;
    boolean checkCombination;
    int indexClass;
    SolutionClass[] x;
    AClass cls;
    List<SolutionClass> solutionClass;// solutionClass[i] is a map of assignment c
    int nbSessions = 5;
    int nbSlotPerSession = 6;
    int nbClassSegments;
    List<AClassSegment> classSegments;
    int[] x_session;// x[i] is the start slot of class segment i
    int[] x_slot;
    public static final String name= "ClassSolverSorted";
    private boolean check(int i, int ses, int sl){
        AClassSegment cs = classSegments.get(i);
        if(checkCombination) {
            for (Combination com : combinations) {
                boolean ok = true;
                for (SolutionClass sc : com.solutionClasses) {
                    if (sc.overlap(sl, sl + cs.duration - 1, ses)) {
                        ok = false;
                        break;
                    }
                }
                if (ok) return true;
            }
            return false;
        }else{
            return true;
        }
        //if(i == 0) return true;
        //if(x_session[i-1] < ses) return true;
        //if(x_session[i-1] > ses) return false;
        //if(x_slot[i-1] + classSegments.get(i-1).duration <= sl) return true;
        //else return false;
    }
    private void solution(){
        List<int[]> periods = new ArrayList<>();
        for(int i = 0; i < classSegments.size(); i++){
            int[] S = new int[3];
            S[0] = x_slot[i]; S[1] = x_slot[i] + classSegments.get(i).duration-1;
            S[2] = x_session[i];
            periods.add(S);
        }
        SolutionClass sc = new SolutionClass(cls,periods);
        //System.out.println("ClassSolver, got solutionclass " + sc);
        solutionClass.add(sc);
    }
    private List<Integer> sortedDomain(int i, int startSlot){
        AClassSegment cs = classSegments.get(i);
        List<Integer> D = new ArrayList<>();
        for(int j = 0; j < indexClass; j++){
            SolutionClass sc = x[j];
            // find appropriate slot right-before or after scheduled class j
            for(int[] p: sc.periods){// p[0] is start, p[1] is end, p[2] is session
                int sl = p[1] + 1;
                if(sl + cs.duration -1 <= Constants.nbSlotPerSession){
                    SessionSlot ss = new SessionSlot(p[2],sl);
                    if(ss.hash() >= startSlot)  D.add(ss.hash());

                }
                sl = p[0] - cs.duration;
                if(sl >= 1){
                    SessionSlot ss = new SessionSlot(p[2],sl);
                    if(ss.hash() >= startSlot)  D.add(ss.hash());
                }
            }
        }
        for(int sl = startSlot; sl <= Constants.nbSessions*Constants.nbSlotPerSession-cs.duration+1;sl++) {
            boolean ok = true;
            for(int j = 0;j < indexClass; j++)
            {
                SolutionClass sc = x[j];
                SessionSlot ss = new SessionSlot(sl);
                if(sc.overlap(ss.slot,cs.duration+ss.slot-1,ss.session)){
                    ok = false; break;
                }
            }
            if(ok) D.add(sl);
        }

        for(int sl = startSlot; sl <= Constants.nbSessions*Constants.nbSlotPerSession-cs.duration+1;sl++){
            if(!D.contains(sl)) D.add(sl);
        }
        return D;
    }
    private void tryClassSegment(int i, int startSlot){
        AClassSegment cs= classSegments.get(i);
        List<Integer> D = sortedDomain(i,startSlot);
        //System.out.print("tryClassSegment(" + i + ", indexClass = " + indexClass + "), D = ");
        //for(int s: D){
        //    SessionSlot ss = new SessionSlot(s);
        //    System.out.print(ss + " ");
        //}
        //System.out.println();
        //for(int s = startSlot; s <= Constants.nbSessions*Constants.nbSlotPerSession;s++){
        for(int s : D){
            SessionSlot ss = new SessionSlot(s);
            if(ss.slot + cs.duration - 1 <= Constants.nbSlotPerSession){
                if(check(i,ss.session,ss.slot)){
                    x_session[i] = ss.session; x_slot[i] = ss.slot;
                    if(i == classSegments.size()-1){
                        solution();
                    }else{
                        tryClassSegment(i+1,s + cs.duration);
                    }
                }
            }
        }
    }
    public List<SolutionClass> solve(int indexClass, AClass cls, int startSlot, SolutionClass[] x, List<Combination> combinations, boolean checkCombination){
        //System.out.println(name + "::solve");
        this.cls = cls;
        this.indexClass = indexClass;
        this.x = x;
        this.combinations = combinations;
        this.checkCombination = checkCombination;
        solutionClass = new ArrayList<>();
        classSegments = cls.classSegments;
        x_session = new int[classSegments.size()];
        x_slot = new int[classSegments.size()];

        tryClassSegment(0,startSlot);
        return solutionClass;
    }

    @Override
    public List<SolutionClass> solve(AClass cls) {
        return null;
    }

    @Override
    public List<SolutionClass> solve(AClass cls, int startSlot) {
        return null;
    }
}
class Constants{
    public static int nbSessions = 5;
    public static int nbSlotPerSession = 6;
}
class SessionSlot{
    int session;
    int slot;
    public SessionSlot(int hss){
        session = (hss-1)/Constants.nbSlotPerSession;
        slot = hss - session*Constants.nbSlotPerSession;
    }
    public SessionSlot(int session, int slot){
        this.slot = slot; this.session = session;
    }
    public int hash(){
        return session*Constants.nbSlotPerSession + slot;
    }
    public String toString(){
        return "(" + session + "," + slot + ")";
    }
    public static void main(String[] args){
        for(int hss = 1; hss <= 100; hss++){
            SessionSlot ss = new SessionSlot(hss);
            System.out.println(hss + " : " + ss + " : " + ss.hash());
        }
    }
}
class Combination{
    List<SolutionClass> solutionClasses;
    public Combination(Combination com, SolutionClass sc){
        solutionClasses = new ArrayList<>();
        for(SolutionClass sci: com.solutionClasses)
            solutionClasses.add(sci);
        solutionClasses.add(sc);
    }
    public Combination(SolutionClass sc){
        solutionClasses = new ArrayList<>();
        solutionClasses.add(sc);
    }
    public boolean feasible(SolutionClass sc){
        for (SolutionClass sci : solutionClasses) {
            //if(sci.toString().equals("IT3020_LT_BT[5]: (5,6,0) (5,6,1) ")
            //&& sc.toString().equals("MI3052_LT_BT[13]: (4,6,1) ")) System.out.println("SEE " + sci.overlap(sc));
            if(sci.overlap(sc)) return false;
        }
        return true;
    }
    public boolean valid(){
        for(int i = 0; i < solutionClasses.size(); i++){
            for(int j = i+1; j < solutionClasses.size(); j++){
                if(solutionClasses.get(i).overlap(solutionClasses.get(j))){
                    return false;
                }
            }
        }
        return true;
    }
    public String toString(){
        String s = "";
        for(SolutionClass sc: solutionClasses) s = s + sc + "; ";
        return s;
    }
}
class CombinationConstraint{
    BacktrackingMaintainCombinations solver = null;
    private List<AClass> classes;
    List<Integer>[] classIndicesOfCourse;
    Map<AClass, Integer> mClass2Index;
    int[] courseOfClass;
    public PrintWriter log = null;
    public List<Stack<Combination>> combinations;// combinations[i] is the stack of combinations for courses 0,1,...,i
    public CombinationConstraint(List<AClass> classes,  List<Integer>[] classIndicesOfCourse, int[] courseOfClass, BacktrackingMaintainCombinations solver){
        this.solver = solver;
        this.classes = classes; this.classIndicesOfCourse = classIndicesOfCourse; this.courseOfClass = courseOfClass;
        combinations = new ArrayList<>();
        for(int i = 0; i < classIndicesOfCourse.length;i++){
            combinations.add(new Stack<>());
        }
    }
    public List<Combination> check(SolutionClass sc, int idxClass, int idxCourse){
        if(solver.LOGGING && log != null){
            log.println("check(" + sc + ", idxClass " + idxClass + "," + idxCourse + ")");
        }
        List<Combination> res = new ArrayList<>();
        if(idxCourse == 0){
            Combination com = new Combination(sc); res.add(com);
            return res;
        }
        // explore combinations of classes from courses 0,1,...,idxCourse - 1
        boolean hasCombination = false;
        for(Combination com: combinations.get(idxCourse-1)){
            if(com.feasible(sc)){
                hasCombination = true;
                Combination nc = new Combination(com,sc);
                res.add(nc);
                //break;
            }
        }
        if(solver.LOGGING && log != null){
            log.println("check(" + sc + ", idxClass " + idxClass + "," + idxCourse + "), res = " + res.size());
        }
        if(!hasCombination) return new ArrayList<>();
        if(idxClass == classIndicesOfCourse[idxCourse].size()-1){
            // current class is the final class of list of classes of current course
            // check if combinations cover all classes of courses 0,1,...,idxCourse
            Set<Integer> ids = new HashSet<>();
            for(Combination com: combinations.get(idxCourse)){
                for(SolutionClass sci: com.solutionClasses){
                    ids.add(sci.cls.id);
                }
            }
            if(solver.LOGGING && log != null){
                log.println("check(" + sc + ", idxClass " + idxClass + "," + idxCourse + "), FIRST ids = " + ids);
            }
            // collect classes covered by new arriving combinations related to current class
            for(Combination com: res){
                for(SolutionClass sci: com.solutionClasses){
                    ids.add(sci.cls.id);
                }
            }
            if(solver.LOGGING && log != null){
                log.println("check(" + sc + ", idxClass " + idxClass + "," + idxCourse + "), SECOND ids = " + ids);
            }
            //for(Combination com: combinations.get(idxCourse-1)){
            //    if(com.feasible(sc)){
            //        ids.add(sc.cls.id);
            //        for(SolutionClass sci: com.solutionClasses)
            //            ids.add(sci.cls.id);
            //    }
            //}
            for(int j = 0; j <= idxCourse; j++){
                for(int i: classIndicesOfCourse[j]){
                    if(solver.LOGGING && log != null){
                        log.println("check(" + sc + ", idxClass " + idxClass + "," + idxCourse + "), j course = " + j + ", i = " + i + ": CHECK class " + classes.get(i).id);
                    }
                    if(!ids.contains(classes.get(i).id))
                        return new ArrayList<>();// class i is not covered by any combination
                }
            }
        }
        return res;
    }
    public void propagate(SolutionClass sc, int idxClass, int idxCourse, List<Combination> newComs){
        if(solver.LOGGING && log != null) log.println("propagate(" + sc + ")");
        for(Combination c: newComs){
            combinations.get(idxCourse).push(c);
            if(solver.LOGGING && log != null) log.println("propagate(" + sc + "), combinations[" + idxCourse + "].sz = " + combinations.get(idxCourse).size());
        }
    }
    public int getNbCombinations(){
        return combinations.get(combinations.size()-1).size();
    }
    public void backtrack(SolutionClass sc, int idxClass, int idxCourse, List<Combination> newComs){
        for(int i = 1; i <= newComs.size(); i++){
            combinations.get(idxCourse).pop();
        }
    }
    public void print(int idxCourse, PrintWriter log){
        for(int i = 0; i <= idxCourse; i++){
            log.println("CombinationConstraint, for course[" + i + "]: ");
            for(int j = 0; j < combinations.get(i).size(); j++){
                Combination com = combinations.get(i).get(j);
                log.print(com + "\t");
            }
            log.println();
        }
    }
}

class CourseSolver{
    //public static final String SOLVE_CLASS_WITH_SORTED_DOMAIN = "SOLVE_CLASS_WITH_SORTED_DOMAIN";
    //public static final String SOLVE_CLASS_WITH_NON_SORTED_DOMAIN = "SOLVE_CLASS_WITH_NON_SORTED_DOMAIN";

    List<AClass> classes;
    List<Combination> combinations;
    boolean checkCombination;
    List<Map<AClass, SolutionClass>> solutionCourse; // solutionCourse[i] is a map of assignment for classes
    SolutionClass[] x;
    int nbTries = 0;
    public String strategy = BacktrackingOneGroup.SOLVE_CLASS_WITH_SORTED_DOMAIN;

    private void solution(){
        Map m = new HashMap();
        for(int i = 0; i <= classes.size()-1;i++){
            AClass cls = classes.get(i);
            m.put(cls,x[i]);
        }
        solutionCourse.add(m);
    }
    private void tryClass(int i, int startSlot){
        nbTries ++;
        //System.out.println("tryClass(" + i + "/" + classes.size());
        AClass cls = classes.get(i);
        ClassSolver CS = null;
        if(strategy.equals(BacktrackingOneGroup.SOLVE_CLASS_WITH_NON_SORTED_DOMAIN))
            CS = new ClassSolverNaive();
        else if(strategy.equals(BacktrackingOneGroup.SOLVE_CLASS_WITH_SORTED_DOMAIN))
            CS = new ClassSolverSorted();

        List<SolutionClass> sol = CS.solve(i,cls,startSlot,x,combinations,checkCombination);
        for(SolutionClass sc: sol){
            //admit(sc);
            x[i] = sc;
            if(i == classes.size()-1){
                solution();
            }else{
                if(classes.get(i+1).classSegments.size() != classes.get(i).classSegments.size())
                    tryClass(i+1,1);
                else tryClass(i+1,startSlot);
            }
        }
    }
    public List<Map<AClass, SolutionClass>> solve(String crs, List<AClass> classes, List<Combination> inpCombinations, boolean checkCombination){
        if(inpCombinations.size()==0 && checkCombination) return new ArrayList<>();// no solution
        this.combinations = inpCombinations;
        this.checkCombination = checkCombination;
        this.classes = classes;
        Collections.sort(classes, new Comparator<AClass>() {
            @Override
            public int compare(AClass o1, AClass o2) {
                return o1.classSegments.size() - o2.classSegments.size();
            }
        });
        //for(AClass cls: classes){
        //    System.out.println("class " + cls.id + ", nbClassSegments = " + cls.classSegments.size());
        //}
        x = new SolutionClass[classes.size()];
        solutionCourse = new ArrayList();
        nbTries = 0;
        tryClass(0,1);
        return solutionCourse;
    }
}
public class BacktrackingOneGroup {
    public static final String SOLVE_CLASS_WITH_SORTED_DOMAIN = "SOLVE_CLASS_WITH_SORTED_DOMAIN";
    public static final String SOLVE_CLASS_WITH_NON_SORTED_DOMAIN = "SOLVE_CLASS_WITH_NON_SORTED_DOMAIN";

    List<AClassSegment> classSegments;
    List<AClass> classes;
    int nbClasses;// number of class segments
    int nbClassSegments; // number of classSegments
    int m;// number of courses;
    int[] cls;// cls[i] is the class of class-segment i
    int[] d;// d[i] is the duration of class-segment i
    //String[] course;// course[i] is the course of class-segment i
    int nbSlotPerSession;
    int nbSessions;
    boolean[][] conflict;
    Map<String, List<AClassSegment>> mCourse2ClassSegments;
    Map<String, List<AClass>> mCourse2Classes;
    List<String> courses;
    //int[] x_session;// x_session[i] is the session of class-segment i
    //int[] x_slot;// x_slot[i] is the start slot of class segment i
    List<Combination> solutions;
    double t0;
    int timeLimit;
    boolean LOGGING = false;
    boolean postCheck = false;
    public boolean BRANCH_AND_BOUND = true;
    public String STRATEGY = SOLVE_CLASS_WITH_SORTED_DOMAIN;
    Map<AClass, SolutionClass>[] x;// x[i] is the solution classes for course i
    Map<AClass, SolutionClass>[] best_x;
    int bestObj;
    int obj;
    int nbSolutions;
    int nbFailures;
    int maxCombinationLength = 0;
    int maxSolutions4Course = 0;
    int nbTries = 0;
    int nbCombinations = 0;
    public void inputFile(String filename){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader( new FileInputStream(filename)));
            String line = in.readLine();
            String[] s = line.split(" ");
            nbSlotPerSession = Integer.valueOf(s[0]);
            nbSessions = Integer.valueOf(s[1]);
            line = in.readLine();
            nbClasses = Integer.valueOf(line);
            if(LOGGING) System.out.println("Number classes = " + nbClasses);
            classSegments = new ArrayList<>();
            mCourse2ClassSegments = new HashMap<>();
            mCourse2Classes = new HashMap<>();
            int id = 0;
            classes = new ArrayList<>();
            for(int i = 0; i < nbClasses; i++)
            {
                line = in.readLine();
                s = line.split(" ");
                int classId = Integer.valueOf(s[0]);
                String course = s[1];
                //String course = s[0] + "-" + s[1];
                int nbCS = Integer.valueOf(s[2]);
                List<AClassSegment> classSegmentList = new ArrayList<>();
                for(int j = 1; j <= nbCS; j++){
                    int duration = Integer.valueOf(s[2+j]);
                    id++;

                    AClassSegment cs = new AClassSegment(id,course,duration);
                    classSegments.add(cs);
                    if(mCourse2ClassSegments.get(course)==null)
                        mCourse2ClassSegments.put(course, new ArrayList<>());
                    mCourse2ClassSegments.get(course).add(cs);
                    classSegmentList.add(cs);
                }
                AClass cls = new AClass(classId,course,classSegmentList);
                if(mCourse2Classes.get(course)==null)
                    mCourse2Classes.put(course,new ArrayList<>());
                mCourse2Classes.get(course).add(cls);
                classes.add(cls);
            }
            nbClassSegments = classSegments.size();
            conflict = new boolean[nbClassSegments][nbClassSegments];
            for(int i = 0; i < nbClassSegments; i++)
                for(int j = 0; j < nbClassSegments; j++)
                    conflict[i][j] = false;
            for(int i = 0; i < nbClassSegments; i++){
                for(int j = i+1; j < nbClassSegments; j++){
                    AClassSegment csi = classSegments.get(i);
                    AClassSegment csj = classSegments.get(j);

                }
            }
            courses = new ArrayList<>();
            for(String course: mCourse2Classes.keySet()){
                courses.add(course);
                mCourse2Classes.get(course).sort(new Comparator<AClass>() {
                    @Override
                    public int compare(AClass o1, AClass o2) {
                        return o1.classSegments.size() - o2.classSegments.size();
                    }
                });
                System.out.print("Classes of course [" + course + "]: ");
                for(AClass cls: mCourse2Classes.get(course)){
                    System.out.print(cls + "\t");
                }
                System.out.println();
            }
            System.out.println("data " + filename);
            System.out.println("#Courses = " + courses.size());
            System.out.println("#Classes = " + classes.size());
            System.out.println("#ClassSegments = " + classSegments.size());
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void input(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            String line = in.readLine();
            String[] s = line.split(" ");
            nbSlotPerSession = Integer.valueOf(s[0]);
            nbSessions = Integer.valueOf(s[1]);
            line = in.readLine();
            nbClasses = Integer.valueOf(line);
            classSegments = new ArrayList<>();
            mCourse2ClassSegments = new HashMap<>();
            mCourse2Classes = new HashMap<>();
            int id = 0;
            classes = new ArrayList<>();
            for(int i = 0; i < nbClasses; i++)
            {
                line = in.readLine();
                s = line.split(" ");
                int classId = Integer.valueOf(s[0]);
                String course = s[1];
                //String course = s[0] + "-" + s[1];
                int nbCS = Integer.valueOf(s[2]);
                List<AClassSegment> classSegmentList = new ArrayList<>();
                for(int j = 1; j <= nbCS; j++){
                    int duration = Integer.valueOf(s[2+j]);
                    id++;

                    AClassSegment cs = new AClassSegment(id,course,duration);
                    classSegments.add(cs);
                    if(mCourse2ClassSegments.get(course)==null)
                        mCourse2ClassSegments.put(course, new ArrayList<>());
                    mCourse2ClassSegments.get(course).add(cs);
                    classSegmentList.add(cs);
                }
                AClass cls = new AClass(classId,course,classSegmentList);
                if(mCourse2Classes.get(course)==null)
                    mCourse2Classes.put(course,new ArrayList<>());
                mCourse2Classes.get(course).add(cls);
                classes.add(cls);
            }
            nbClassSegments = classSegments.size();
            conflict = new boolean[nbClassSegments][nbClassSegments];
            for(int i = 0; i < nbClassSegments; i++)
                for(int j = 0; j < nbClassSegments; j++)
                    conflict[i][j] = false;
            for(int i = 0; i < nbClassSegments; i++){
                for(int j = i+1; j < nbClassSegments; j++){
                    AClassSegment csi = classSegments.get(i);
                    AClassSegment csj = classSegments.get(j);

                }
            }
            courses = new ArrayList<>();
            for(String course: mCourse2Classes.keySet()){
                courses.add(course);
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean feasible(Combination com, SolutionClass sc) {
        for (SolutionClass sci : com.solutionClasses) {
            //if(sci.toString().equals("IT3020_LT_BT[5]: (5,6,0) (5,6,1) ")
            //&& sc.toString().equals("MI3052_LT_BT[13]: (4,6,1) ")) System.out.println("SEE " + sci.overlap(sc));
            if(sci.overlap(sc)) return false;
        }
        return true;
    }
    private List<Combination> checkFeasibleCombination(int idxCourse, Map<AClass,SolutionClass> m,List<Combination> combinations){
        //System.out.println("checkFeasibleCombination, course " + courses.get(idxCourse));
        List<Combination> res = new ArrayList<>();
        Map<AClass, List<Combination>> mClass2Combinations = new HashMap<>();
        for(AClass cls: m.keySet()){
            mClass2Combinations.put(cls,new ArrayList<>());
        }
        /*
        boolean ok = true;
        for(AClass cls: m.keySet()){
            SolutionClass sc = m.get(cls);
            boolean okcls = false;
            for (Combination com : combinations) {
                if (feasible(com, sc)) {
                    okcls = true; break;
                    //Combination newCom = new Combination(com, sc);
                    //res.add(newCom);
                }
            }
            if(!okcls){ ok = false; break; }
        }
        if(!ok) return new ArrayList<>();// not feasible
        */
        // establish new combination

        for(AClass cls: m.keySet()){
            SolutionClass sc = m.get(cls);
            //boolean okcls = false;
            for (Combination com : combinations) {
                if (feasible(com, sc)) {
                    //okcls = true; break;
                    //if(sc.cls.course.equals("MI3052_LT_BT") && sc.cls.id == 13){
                    //if(sc.toString().equals("MI3052_LT_BT[13]: (4,6,1) ")){
                    //    System.out.println("Combination " + com + " COMBINE with " + sc.toString());
                    //}
                    Combination newCom = new Combination(com, sc);
                    //if(!newCom.valid()) System.out.println("Combination " + newCom + " -> not valid, BUG????");
                    mClass2Combinations.get(cls).add(newCom);
                    res.add(newCom);
                }
            }
            if(mClass2Combinations.get(cls).size()==0) return new ArrayList<>();// not feasible
            //if(!okcls){ ok = false; break; }
        }
        // check if res cover all classes already scheduled or not
        Set<Integer> ids = new HashSet<>();
        for(Combination com: res){
            for(SolutionClass sc: com.solutionClasses){
                ids.add(sc.cls.id);
            }
        }
        boolean ok = true;
        for(int j = 0; j <= idxCourse; j++){
            String crs = courses.get(j);
            for(AClass cls: mCourse2Classes.get(crs)){
                if(!ids.contains(cls.id)){
                    ok = false; break;
                }
            }
            if(!ok) break;
        }
        if(!ok) return new ArrayList<>();
        return res;
    }
    private void tryCourse(int i,List<Combination> combinations){
        //if(LOGGING) System.out.println("tryCourse(" + i + "/" + courses.size() + ")");
        if(combinations.size() >maxCombinationLength) maxCombinationLength = combinations.size();
        //if(i == 1){
        //    System.out.println("tryCourse(" + i + "), level 1 -> combinations = ");
        //    for(Combination com: combinations) System.out.println(com);
        //}
        double t = System.currentTimeMillis() - t0;
        //System.out.println("tryCourse, t = " + t + " timeLimit = " + timeLimit + " obj = " + obj + " bestObj = " + bestObj) ;
        if(t * 0.001 > timeLimit) return;
        //if(solutions.size() > 10000000) return;
        //if(nbSolutions >= 1) return;
        //System.out.println("tryCourse(" + i + ", combinations = " + combinations.size() + ")");
        String crs = courses.get(i);
        List<AClass> CLS = mCourse2Classes.get(crs);
        CourseSolver CS = new CourseSolver();
        CS.strategy = STRATEGY;

        List<Map<AClass, SolutionClass>> solutionCourse = CS.solve(crs,CLS,combinations,true);
        nbTries += CS.nbTries;
        if(maxSolutions4Course < solutionCourse.size()) maxSolutions4Course = solutionCourse.size();
        for(Map<AClass,SolutionClass> m: solutionCourse){
            List<Combination> childCombinations = checkFeasibleCombination(i,m,combinations);
            if(maxCombinationLength < childCombinations.size()) maxCombinationLength = childCombinations.size();

            if(childCombinations.size() > 0) {
                // admit a solution class
                x[i] = m;
                MaxClique MCSolver = new MaxClique();
                int mc = MCSolver.computeMaxClique(x[i]);
                obj = obj + mc;
                /*
                List<Combination> childCombinations = new ArrayList<>();
                for (AClass cls : m.keySet()) {
                    SolutionClass sc = m.get(cls);
                    for (Combination com : combinations) {
                        if (feasible(com, sc)) {
                            Combination newCom = new Combination(com, sc);
                            childCombinations.add(newCom);
                        }
                    }
                }
                */
                if (i == courses.size() - 1) {
                    solution(childCombinations);
                } else {
                    if(BRANCH_AND_BOUND) {// branch and bound applied
                        if (obj + courses.size() - (i + 1) < bestObj) {
                            tryCourse(i + 1, childCombinations);
                        } else {
                            //System.out.println("tryCourse(" + i + "/" + courses.size() + ") obj = " + obj + " bestObj = " + bestObj + " -> BOUND!!!") ;
                        }
                    }else{// pure exhaustive search
                        //System.out.println("Not BB");
                        tryCourse(i + 1, childCombinations);
                    }
                }
                obj = obj - mc;
            }
        }
    }
    public void solveNew(int maxTime, boolean LOGGING){
        this.LOGGING = LOGGING;
        this.timeLimit = maxTime;
        t0 = System.currentTimeMillis();
        List<Combination> combinations = new ArrayList<>();
        String crs = courses.get(0);
        List<AClass> CLS = mCourse2Classes.get(crs);
        CourseSolver CS = new CourseSolver();
        //System.out.println("course " + crs + ", classes = ");
        //for(AClass cls: CLS) System.out.println(cls);
        List<Map<AClass, SolutionClass>> solutionCourse = CS.solve(crs,CLS,combinations,false);
        nbTries += CS.nbTries;
        solutions = new ArrayList<>();
        x = new Map[courses.size()];
        bestObj = 100000000;
        nbSolutions = 0;
        obj = 0;
        //for(int i = 0; i < courses.size(); i++) System.out.println("course[" + i + "] = " + courses.get(i));
        if(maxSolutions4Course < solutionCourse.size()) maxSolutions4Course = solutionCourse.size();
        for(Map<AClass, SolutionClass> sol: solutionCourse){
            x[0] = sol;
            MaxClique MCSolver = new MaxClique();
            int mc = MCSolver.computeMaxClique(x[0]);
            obj += mc;
            List<Combination> childCombinations = new ArrayList<>();
            for(AClass cls: sol.keySet()){
                SolutionClass sc = sol.get(cls);
                Combination newCom = new Combination(sc);
                childCombinations.add(newCom);
                //System.out.println(sc);
            }
            if(BRANCH_AND_BOUND) {
                if (obj + courses.size() - 1 < bestObj) {
                    tryCourse(1, childCombinations);
                } else {
                    //System.out.println("BOUND!!!") ;
                }
            }else{
                //System.out.println("Not BB");
                tryCourse(1, childCombinations);
            }
            obj -= mc;
        }
        //System.out.println("number solutions = " + solutions.size());
        double t = System.currentTimeMillis() - t0;
        if(LOGGING) System.out.println("bestObj = " + bestObj + " time = " + t*0.001);
    }

    private boolean checkCombinationCoverAllClasses(List<Combination> combinations, boolean PRINT){
        Set<Integer> ids = new HashSet<>();
        for(Combination com: combinations){
            //if(PRINT)System.out.print("checkCombinationCoverAllClasses, com:");
            for(SolutionClass sc: com.solutionClasses){
                ids.add(sc.cls.id);
                //if(PRINT)System.out.print(sc + "; ");
            }
            //if(PRINT)System.out.println();
        }
        //for(int id: ids) System.out.println("ids item " + id);
        //System.out.println("------------");
        boolean ok = true;
        for(AClass cls: classes){
            //System.out.println("checkCombinationCoverAllClasses exam cls.id " + cls.id);
            if(!ids.contains(cls.id)){
                ok = false; break;
            }
        }
        return ok;
    }
    private void solution(List<Combination> combinations){
        if(postCheck) {
            if (!checkCombinationCoverAllClasses(combinations, false)) {
                System.out.println("solution, BUT checkCombinationCoverAllClasses failed???");
                return;
            }
        }
        nbSolutions += 1;
        //if(LOGGING) System.out.println("FOUND solutions " + combinations.size());
        for(Combination com: combinations){
            //String info = "";
            //for(SolutionClass sc: com.solutionClasses) info += sc + "; ";
            //System.out.println(info);
            //solutions.add(com);
        }
        //printSolution(x);
        //int f = 0;
        //for(int i = 0; i <= x.length-1; i++){
        //    MaxClique maxCliqueSolver = new MaxClique();
        //    int mc = maxCliqueSolver.computeMaxClique(x[i]);
        //    f += mc;
        //String info = "";
        //for(AClass cls: x[i].keySet()){
        //    SolutionClass sc = x[i].get(cls);
        //    info += sc + "; ";
        //}
        //System.out.println("Solution " + info + " mc = " + mc + " f = " + f);
        //}
        //if(LOGGING) System.out.println("nbSolutions = " + nbSolutions + " obj = " + obj + " bestObj = " + bestObj + " maxCombinationLength = " + maxCombinationLength);
        if(obj < bestObj){
            bestObj = obj;
            best_x= new Map[x.length];
            for(int i = 0; i < x.length; i++){
                best_x[i] = new HashMap<>();
                for(AClass c: x[i].keySet()){
                    best_x[i].put(c,x[i].get(c));
                }
            }
            boolean ok = checkCombinationCoverAllClasses(combinations,true);
            if(LOGGING) System.out.println("update best " + bestObj + " checkCombinations = " + ok);
        }
        //System.out.println("-------------------");

        //for(Combination com: combinations) solutions.add(com);
    }
    public void printBestSolution(){
        if(best_x == null){
            System.out.println("Cannot find any solution, maxCombinationLength = " + maxCombinationLength); return;
        }
        System.out.println("Best Solution: ");
        for(int i = 0; i < best_x.length; i++){
            String info = "";
            for(AClass c: best_x[i].keySet()){
                SolutionClass sc = best_x[i].get(c);
                info = info + sc + "; ";
            }
            System.out.println(info);
        }
        System.out.println("maxCombinationLength = " + maxCombinationLength);
    }
    public void writeSolutionFormat(){
        if(best_x == null){
            System.out.println(-1); return;
        }
        System.out.println(classes.size());
        for(int i = 0; i < best_x.length; i++){
            //String info = "";
            for(AClass c: best_x[i].keySet()){
                SolutionClass sc = best_x[i].get(c);
                //info = info + sc + "; ";
                System.out.print(c.id + " " + sc.periods.size() + " ");
                for(int[] p: sc.periods){
                    SessionSlot ss = new SessionSlot(p[0]);
                    //System.out.print((ss.session+1) + " " + ss.slot + " ");
                    System.out.print(p[2] + " " + p[0] + " ");
                }
                System.out.println();
            }
            //System.out.println(info);
        }
    }
    public void printSolution(Map<AClass, SolutionClass>[] x){
        System.out.println("Solution: ");
        for(int i = 0; i < x.length; i++){
            String info = "";
            for(AClass c: x[i].keySet()){
                SolutionClass sc = x[i].get(c);
                info = info + sc + "; ";
            }
            System.out.println(info);
        }
    }
    public void statistic(){
        System.out.println("Combinations length = " + nbCombinations);
        //System.out.println("maxSolution4Course = " + maxSolutions4Course);
        System.out.println("#Tries = " + nbTries);
        System.out.println("#Solutions = " + nbSolutions);
        System.out.println("#Failures = " + nbFailures);
        double t = System.currentTimeMillis() - t0;
        t = t*0.001;
        System.out.println("BestObj = " + bestObj + ", Time = " + t);
    }
    public static void BranchAndBoundSolveClassSortedDomain(String filename, int timeLimit){
        BacktrackingOneGroup solver = new BacktrackingOneGroup();
        solver.BRANCH_AND_BOUND = false;//true;
        //solver.BRANCH_AND_BOUND = true;
        solver.LOGGING = true;
        solver.STRATEGY = BacktrackingOneGroup.SOLVE_CLASS_WITH_SORTED_DOMAIN;
        //solver.postCheck = true;
        solver.inputFile(filename);
        //solver.input();
        solver.solveNew(timeLimit, true);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
    }
    public static void BranchAndBoundSolveClassNonSortedDomain(String filename,  int timeLimit){
        BacktrackingOneGroup solver = new BacktrackingOneGroup();
        solver.BRANCH_AND_BOUND = true;
        solver.STRATEGY = BacktrackingOneGroup.SOLVE_CLASS_WITH_NON_SORTED_DOMAIN;
        //solver.postCheck = true;
        solver.inputFile(filename);
        //solver.input();
        solver.solveNew(timeLimit, false);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
    }
    public static void pureExhaustiveSearch(String filename, int timeLimit){
        BacktrackingOneGroup solver = new BacktrackingOneGroup();
        //solver.BRANCH_AND_BOUND = false;
        solver.STRATEGY = BacktrackingOneGroup.SOLVE_CLASS_WITH_NON_SORTED_DOMAIN;
        //solver.postCheck = true;
        solver.inputFile(filename);
        //solver.input();

        solver.solveNew(timeLimit, true);
        solver.printBestSolution();
        solver.writeSolutionFormat();
        solver.statistic();
    }


    public static void main(String[] args){
        int timeLimit = 600;// 5  minutes;
        String filename = "data/it1-2nd.txt";
        //String filename = "data/em4-2nd-s.txt";
        String[] FN = {
                "em4-2nd-s",
                "fl2-2nd-s",
                "fl2-3th-c",
                "fl2-4th-s",
                "he1-3th-s",
                "it1-2nd",
                "it2-2nd",
                "it2-3th-c",
                "it2-5th-c",
                "me1-2nd",
                "mi1-3th",
                "mi1-5th-s",
                "ph1-3th-s",
                "ph2-3th-s",
                "te2-3th-c",
                "te2-3th-s",
                "ch1-3th-s",
                "et1-3th-s",
        };
        //String fn = "fl2-4th-s";
        String fn = "fl2-3th-c";

        //for(String fn: FN) {
        filename = "data/" + fn + ".txt";
        BranchAndBoundSolveClassSortedDomain(filename, timeLimit);
        System.out.println("------------------");
        //BranchAndBoundSolveClassNonSortedDomain(filename, timeLimit);
        //System.out.println("------------------");
        //pureExhaustiveSearch(filename, timeLimit);
        // }
    }
}
