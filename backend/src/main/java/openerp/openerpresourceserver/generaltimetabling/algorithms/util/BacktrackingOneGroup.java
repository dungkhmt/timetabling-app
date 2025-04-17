package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

class MaxClique{
    Map<AClass, SolutionClass> sol;
    int n;
    boolean[][] A;
    int[] x;
    private boolean check(int v, int k){
        for(int i = 0; i < k; i++)
            if(A[x[i]][v]==false) return false;
        return true;
    }
    private void tryValue(int k){
        //System.out.println("tryValue(" + k + "/" + n + ")");
        for(int v = x[k-1]+1; v < n; v++) {
            if(check(v,k)){
                x[k] = v;
                //System.out.println("tryValue(" + k + "/" + n + "), assign x[" + k + "] = " + v);
                if(k+1 > res) res = k+1;
                if(k == n-1) {
                    if(k+1 > res) res = k+1;
                }
                else tryValue(k+1);
            }
        }
    }
    int res;
    public int computeMaxClique(Map<AClass, SolutionClass> sol){
        this.sol = sol;
        n  = sol.keySet().size();
        List<AClass> V = new ArrayList<>();
        for(AClass c: sol.keySet()){
            V.add(c);
        }
        A = new boolean[n][n];
        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                A[i][j] = false;
        for(int i = 0; i < n; i++){
            for(int j = i+1; j < n; j++){
                AClass ci = V.get(i);
                AClass cj = V.get(j);
                SolutionClass si = sol.get(ci);
                SolutionClass sj = sol.get(cj);
                if(si.overlap(sj)){
                    A[i][j] = true;
                    A[j][i] = true;
                }
            }
        }
        x = new int[n];
        res = 1;
        for(int v = 0; v < n; v++){
            x[0] = v;
            tryValue(1);
        }
        return res;
    }
}
class AClassSegment{
    int id;
    String course;
    int duration;

    public AClassSegment(int id, String course, int duration) {
        this.id = id;
        this.course = course;
        this.duration = duration;
    }
}
class AClass{
    int id;
    String course;
    List<AClassSegment> classSegments;

    public String toString(){
        String s = course;
        for(AClassSegment cs: classSegments)
            s = s + " [" + cs.id + "," + cs.duration + "] ";
        return s;
    }
    public AClass(int id, String course, List<AClassSegment> classSegments) {
        this.id = id;
        this.course = course;
        this.classSegments = classSegments;
    }
}
class SolutionClass{
    AClass cls;
    List<int[]> periods;// start slots of class-segments: p[0] is start-slot, p[1] is end-slot, p[2] is session

    public SolutionClass(AClass cls, List<int[]> periods) {
        this.cls = cls;
        this.periods = periods;
    }
    public String toString(){
        String s = cls.course + "[" + cls.id + "]: ";
        for(int[] p: periods) s = s + "(" + p[0] + "," + p[1] + "," + p[2] + ") ";
        return s;
    }
    public boolean overlap(SolutionClass sci){
        for(int[] p: periods)
        {
            for(int[] pi: sci.periods){
                if(p[2] != pi[2]) return false;// sessions are different
                if(Util.overLap(p[0],p[1]+1-p[0],pi[0],pi[1]+1-pi[0])) return true;
            }
        }
        return false;
    }
    public boolean overlap(int start, int end, int session){
        for(int[] p: periods) {
                if(p[2] != session) continue;
                if(Util.overLap(p[0],p[1]+1-p[0],start,end-start+1)) return true;
        }
        return false;
    }
}
class ClassSolver{
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
    public List<SolutionClass> solve(AClass cls, int startSlot, List<Combination> combinations, boolean checkCombination){
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
}
class CourseSolver{
    List<AClass> classes;
    List<Combination> combinations;
    boolean checkCombination;
    List<Map<AClass, SolutionClass>> solutionCourse; // solutionCourse[i] is a map of assignment for classes
    SolutionClass[] x;

    private void solution(){
        Map m = new HashMap();
        for(int i = 0; i <= classes.size()-1;i++){
            AClass cls = classes.get(i);
            m.put(cls,x[i]);
        }
        solutionCourse.add(m);
    }
    private void tryClass(int i, int startSlot){
        //System.out.println("tryClass(" + i + "/" + classes.size());
        AClass cls = classes.get(i);
        ClassSolver CS = new ClassSolver();
        List<SolutionClass> sol = CS.solve(cls,startSlot,combinations,checkCombination);
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
        x = new SolutionClass[classes.size()];
        solutionCourse = new ArrayList();
        tryClass(0,1);
        return solutionCourse;
    }
}
public class BacktrackingOneGroup {
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
    Map<AClass, SolutionClass>[] x;// x[i] is the solution classes for course i
    Map<AClass, SolutionClass>[] best_x;
    int bestObj;
    public void input(String filename){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader( new FileInputStream(filename)));
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
                String course = s[0] + "-" + s[1];
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
                AClass cls = new AClass(i+1,course,classSegmentList);
                if(mCourse2Classes.get(course)==null)
                    mCourse2Classes.put(course,new ArrayList<>());
                mCourse2Classes.get(course).add(cls);
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
            if(sci.overlap(sc)) return false;
        }
        return true;
    }
    private void tryCourse(int i,List<Combination> combinations){
        double t = System.currentTimeMillis() - t0;
        //System.out.println("tryCourse, t = " + t + " timeLimit = " + timeLimit);
        if(t * 0.001 > timeLimit) return;
        if(solutions.size() > 10000000) return;
        //System.out.println("tryCourse(" + i + ", combinations = " + combinations.size() + ")");
        String crs = courses.get(i);
        List<AClass> CLS = mCourse2Classes.get(crs);
        CourseSolver CS = new CourseSolver();

        List<Map<AClass, SolutionClass>> solutionCourse = CS.solve(crs,CLS,combinations,true);
        for(Map<AClass,SolutionClass> m: solutionCourse){
            // admit a solution class
            x[i] = m;
            List<Combination> childCombinations = new ArrayList<>();
            for(AClass cls: m.keySet()){
                SolutionClass sc = m.get(cls);
                for(Combination com: combinations){
                    if(feasible(com,sc)){
                        Combination newCom = new Combination(com,sc);
                        childCombinations.add(newCom);
                    }
                }
            }

            if(i == courses.size()-1) {
                solution(childCombinations);
            }else{
                tryCourse(i+1,childCombinations);
            }
        }
    }
    public void solveNew(int maxTime){
        this.timeLimit = maxTime;
        t0 = System.currentTimeMillis();
        List<Combination> combinations = new ArrayList<>();
        String crs = courses.get(0);
        List<AClass> CLS = mCourse2Classes.get(crs);
        CourseSolver CS = new CourseSolver();
        //System.out.println("course " + crs + ", classes = ");
        //for(AClass cls: CLS) System.out.println(cls);
        List<Map<AClass, SolutionClass>> solutionCourse = CS.solve(crs,CLS,combinations,false);
        solutions = new ArrayList<>();
        x = new Map[courses.size()];
        bestObj = 100000000;
        for(Map<AClass, SolutionClass> sol: solutionCourse){
            x[0] = sol;
            List<Combination> childCombinations = new ArrayList<>();
            for(AClass cls: sol.keySet()){
                SolutionClass sc = sol.get(cls);
                Combination newCom = new Combination(sc);
                childCombinations.add(newCom);
                //System.out.println(sc);
            }
            tryCourse(1,childCombinations);


        }
        System.out.println("number solutions = " + solutions.size());
        System.out.println("bestObj = " + bestObj);
    }


    private void solution(List<Combination> combinations){
        //System.out.println("FOUND solutions " + combinations.size());
        for(Combination com: combinations){
            //String info = "";
            //for(SolutionClass sc: com.solutionClasses) info += sc + "; ";
            //System.out.println(info);
            //solutions.add(com);
        }
        int f = 0;
        for(int i = 0; i <= x.length-1; i++){
            MaxClique maxCliqueSolver = new MaxClique();
            int mc = maxCliqueSolver.computeMaxClique(x[i]);
            f += mc;
            String info = "";
            for(AClass cls: x[i].keySet()){
                SolutionClass sc = x[i].get(cls);
                info += sc + "; ";
            }
            System.out.println("Solution " + info + " mc = " + mc + " f = " + f);
        }
        if(f < bestObj){
            bestObj = f;
            best_x= new Map[x.length];
            for(int i = 0; i < x.length; i++){
                best_x[i] = new HashMap<>();
                for(AClass c: x[i].keySet()){
                    best_x[i].put(c,x[i].get(c));
                }
            }
            System.out.println("update best " + bestObj);
        }
        System.out.println("-------------------");

        //for(Combination com: combinations) solutions.add(com);
    }
    public void printBestSolution(){
        System.out.println("Best Solution: ");
        for(int i = 0; i < best_x.length; i++){
            String info = "";
            for(AClass c: best_x[i].keySet()){
                SolutionClass sc = best_x[i].get(c);
                info = info + sc + "; ";
            }
            System.out.println(info);
        }
    }
    public static void main(String[] args){
        BacktrackingOneGroup solver = new BacktrackingOneGroup();
        solver.input("data/1.txt");
        solver.solveNew(600);
        solver.printBestSolution();
    }
}
