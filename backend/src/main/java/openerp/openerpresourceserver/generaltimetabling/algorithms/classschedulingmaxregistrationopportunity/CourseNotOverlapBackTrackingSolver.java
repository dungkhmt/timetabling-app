package openerp.openerpresourceserver.generaltimetabling.algorithms.classschedulingmaxregistrationopportunity;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;

import java.util.*;

@Log4j2
public class CourseNotOverlapBackTrackingSolver {
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
    int[] bestSol;
    int bestSessionUsed;// number of day-sessions occupied is minimal (first objective)
    int bestDistance; // min distance between day-session used is maximal (second objective)
    Map<String, Integer> solutionMap;
    boolean found;
    public boolean findFirstSolution = false;
    long timeLimit = 10000;// 10 seconds by default
    long t0;// starting time point;
    public CourseNotOverlapBackTrackingSolver(Set<String> courses, Map<String, List<Integer>> mCourse2Domain, Map<String, Integer> mCourse2Duration,Map<String, List<String>> mCourseGroup2ConflictCourseGroups){
        this.courses = courses;
        this.mCourse2Domain = mCourse2Domain;
        this.mCourse2Duration = mCourse2Duration;
        nbCourses= courses.size();
        domain = new List[nbCourses];
        duration = new int[nbCourses];
        this.mCourseGroup2ConflictCourseGroups = mCourseGroup2ConflictCourseGroups;
    }
    public void solve(int timeLimit){
        this.timeLimit = timeLimit*1000; // convert into millis seconds;
        arrCourses = new String[nbCourses];
        mCourseId2Index = new HashMap<>();
        conflict = new Set[nbCourses];
        log.info("solve, nbCourses = " + nbCourses);
        int idx = -1;
        bestSessionUsed = 100000000; bestDistance = 0; bestSol = null;
        for(String c: courses){
            //System.out.println("Solver: course " + c);
            idx++;
            arrCourses[idx] = c;
            mCourseId2Index.put(c,idx);
            domain[idx] = mCourse2Domain.get(c);
            duration[idx] = mCourse2Duration.get(c);
            //log.info("Solver: course " + c + " duration " + duration[idx] + " domain = " + domain[idx].toString());
            conflict[idx] = new HashSet();
        }
        for(int i = 0; i < nbCourses; i++){
            for(String ci: mCourseGroup2ConflictCourseGroups.get(arrCourses[i])){
                int j = mCourseId2Index.get(ci);
                conflict[i].add(j);
            }
        }
        for(idx = 0; idx < nbCourses; idx++){
            String c = arrCourses[idx];
            log.info("Solver: course " + c + " duration " + duration[idx] + ", conflict = " + conflict[idx] + ", domain = " + domain[idx].toString());
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
        if(findFirstSolution && found) return;
        found = true;
        Set<Integer> daySessions = new HashSet<>();
        for(int i = 0; i < nbCourses; i++){
            DaySessionSlot dss = new DaySessionSlot(x[i]);
            int ds = dss.day*2 + dss.session;
            daySessions.add(ds);
        }
        int minD = 100000;
        for(int s1: daySessions)
            for(int s2: daySessions) if(s1 < s2){
                if(minD > s2 - s1) minD = s2 - s1;
            }
        if(daySessions.size() < bestSessionUsed ||
        daySessions.size() == bestSessionUsed && minD > bestDistance) {
            bestSessionUsed = daySessions.size();
            bestDistance = minD;
            log.info("update bestSessionUsed = " + bestSessionUsed + " bestDistance = " + bestDistance);
            for (int i = 0; i < nbCourses; i++) sol[i] = x[i];
        }
    }
    private void tryValue(int k){
        long t = System.currentTimeMillis() - t0;
        //log.info("tryValue(" + k + "), courseCode " + arrCourses[k] + " t = " + t + " timeLimit = " + timeLimit);
        if(t > timeLimit) return;
        if(findFirstSolution) if(found) return;
        //log.info("tryValue(" + k + "), courseCode " + arrCourses[k] + ", Domain = " + domain[k].size() + " start");
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
