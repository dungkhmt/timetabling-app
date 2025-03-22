package openerp.openerpresourceserver.generaltimetabling.algorithms.fullslotsseparatedays;

import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class CourseNotOverlapFullSlotsSeparateDaysBackTrackingSolver {
    Set<String> courses;
    Map<String, List<Integer>> mCourse2Domain;
    Map<String, Integer> mCourse2Duration;
    Map<String, List<String>> mCourseGroup2ConflictCourseGroups;
    String[] arrCourses;
    Map<String, Integer> mCourseId2Index;
    int nbCourses;
    List<Integer>[] domain;
    Set<Integer>[] conflict;
    int[] duration;
    int[] x;
    int[] sol;
    Map<String, Integer> solutionMap;
    boolean found;
    long timeLimit = 10000;// 10 seconds by default
    long t0;// starting time point;
    public CourseNotOverlapFullSlotsSeparateDaysBackTrackingSolver(Set<String> courses, Map<String, List<Integer>> mCourse2Domain, Map<String, Integer> mCourse2Duration, Map<String, List<String>> mCourseGroup2ConflictCourseGroups){
        this.courses = courses;
        this.mCourse2Domain = mCourse2Domain;
        this.mCourse2Duration = mCourse2Duration;
        nbCourses= courses.size();
        domain = new List[nbCourses];
        duration = new int[nbCourses];
        this.mCourseGroup2ConflictCourseGroups = mCourseGroup2ConflictCourseGroups;
    }
    public void solve(int timeLimit){
        this.timeLimit = timeLimit;
        arrCourses = new String[nbCourses];
        mCourseId2Index = new HashMap<>();
        conflict = new Set[nbCourses];
        log.info("solve, nbCourses = " + nbCourses);
        int idx = -1;
        for(String c: courses){
            //System.out.println("Solver: course " + c);
            idx++;
            arrCourses[idx] = c;
            mCourseId2Index.put(c,idx);
            domain[idx] = mCourse2Domain.get(c);
            duration[idx] = mCourse2Duration.get(c);
            log.info("Solver: course " + c + " duration " + duration[idx] + " domain = " + domain[idx].toString());
            conflict[idx] = new HashSet();
        }
        for(int i = 0; i < nbCourses; i++){
            for(String ci: mCourseGroup2ConflictCourseGroups.get(arrCourses[i])){
                int j = mCourseId2Index.get(ci);
                conflict[i].add(j);
            }
        }
        x = new int[nbCourses];
        sol = new int[nbCourses];
        found = false;
        solutionMap = new HashMap();
        t0 = System.currentTimeMillis();
        tryValue(0);
        if(found){
            for(int i = 0; i < nbCourses; i++){
                solutionMap.put(arrCourses[i],sol[i]);
            }
        }
    }
    public Map<String, Integer> getSolutionMap(){ return solutionMap; }
    public boolean hasSolution(){ return found;}
    public int[] getSolution(){ return sol; }
    private boolean check(int v, int k){
        for(int i = 0; i <= k-1; i++){
            if(conflict[k].contains(i)) {
                boolean notOverLap = (v >= x[i] + duration[i] || x[i] >= v + duration[k]);
                if (!notOverLap) return false;
            }
        }
        return true;
    }
    private void solution(){
        if(found) return;
        found = true;
        for(int i = 0; i < nbCourses; i++) sol[i] = x[i];
    }
    private void tryValue(int k){
        long t = System.currentTimeMillis() - t0;
        if(t > timeLimit) return;
        if(found) return;
        //log.info("tryValue(" + k + "), courseCode " + arrCourses[k] + " start");
        // try value for x[k]: start time-slot for course k
        for(int v: domain[k]){
            //log.info("tryValue(" + k + "), courseCode " + arrCourses[k] + " consider v = " + v);
            if(check(v,k)){
                x[k] = v;
                if(k == nbCourses-1) solution();
                else {
                    tryValue(k+1);
                }
            }
        }
    }
}
