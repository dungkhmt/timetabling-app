package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

//import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;

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
    public static boolean overLap(int startSlot1, int duration1, int startSlot2, int duration2){
        if(startSlot1 + duration1 <= startSlot2 || startSlot2 + duration2 <= startSlot1) return false;
        return true;
    }
    public boolean overlap(SolutionClass sci){
        for(int[] p: periods)
        {
            for(int[] pi: sci.periods){
                if(p[2] != pi[2]) continue;// sessions are different
                if(SolutionClass.overLap(p[0],p[1]+1-p[0],pi[0],pi[1]+1-pi[0])) return true;
            }
        }
        return false;
    }
    public boolean overlap(int start, int end, int session){
        for(int[] p: periods) {
                if(p[2] != session) continue;
                if(SolutionClass.overLap(p[0],p[1]+1-p[0],start,end-start+1)) return true;
        }
        return false;
    }
}
interface ClassSolver{
    public List<SolutionClass> solve(int indexClass, AClass cls, int startSlot, SolutionClass[] x, List<Combination> combinations, boolean checkCombination);
    public List<SolutionClass> solve(AClass cls);
}
class ClassSolverLazyCheck implements  ClassSolver{
    AClass cls;
    List<SolutionClass> solutionClass;// solutionClass[i] is a map of assignment c
    int nbClassSegments;
    List<AClassSegment> classSegments;
    int[] x_session;// x[i] is the start slot of class segment i
    int[] x_slot;
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
        return true;
    }
    private void tryClassSegment(int i, int startSlot){
        AClassSegment cs = classSegments.get(i);
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
    }

    @Override
    public List<SolutionClass> solve(AClass cls) {
        this.cls = cls;
        solutionClass = new ArrayList<>();
        classSegments = cls.classSegments;
        x_session = new int[classSegments.size()];
        x_slot = new int[classSegments.size()];
        tryClassSegment(0,1);
        return solutionClass;
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
    int maxCombinationLength = 0;
    int maxSolutions4Course = 0;
    int nbTries = 0;
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
        //System.out.println("FOUND solutions " + combinations.size());
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
        if(LOGGING) System.out.println("nbSolutions = " + nbSolutions + " obj = " + obj + " bestObj = " + bestObj + " maxCombinationLength = " + maxCombinationLength);
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
        System.out.println("nbTries = " + nbTries);
        System.out.println("maxCombinationLength = " + maxCombinationLength);
        System.out.println("maxSolution4Course = " + maxSolutions4Course);
        double t = System.currentTimeMillis() - t0;
        t = t*0.001;
        System.out.println("BestObj = " + bestObj + ", Time = " + t);
    }
    public static void BranchAndBoundSolveClassSortedDomain(String filename, int timeLimit){
        BacktrackingOneGroup solver = new BacktrackingOneGroup();
        solver.BRANCH_AND_BOUND = true;
        solver.STRATEGY = BacktrackingOneGroup.SOLVE_CLASS_WITH_SORTED_DOMAIN;
        //solver.postCheck = true;
        solver.inputFile(filename);
        //solver.input();
        solver.solveNew(timeLimit, false);
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
        solver.BRANCH_AND_BOUND = false;
        solver.STRATEGY = BacktrackingOneGroup.SOLVE_CLASS_WITH_NON_SORTED_DOMAIN;
        //solver.postCheck = true;
        solver.inputFile(filename);
        //solver.input();

        solver.solveNew(timeLimit, false);
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
        String fn = "it2-3th-c-ext1";
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
