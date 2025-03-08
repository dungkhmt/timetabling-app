package openerp.openerpresourceserver.generaltimetabling.algorithms.classschedulingmaxregistrationopportunity;

import java.util.*;

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
    Map<String, Integer> solutionMap;
    boolean found;
    public CourseNotOverlapBackTrackingSolver(Set<String> courses, Map<String, List<Integer>> mCourse2Domain, Map<String, Integer> mCourse2Duration,Map<String, List<String>> mCourseGroup2ConflictCourseGroups){
        this.courses = courses;
        this.mCourse2Domain = mCourse2Domain;
        this.mCourse2Duration = mCourse2Duration;
        nbCourses= courses.size();
        domain = new List[nbCourses];
        duration = new int[nbCourses];

    }
    public void solve(){
        arrCourses = new String[nbCourses];
        mCourseId2Index = new HashMap<>();
        conflict = new Set[nbCourses];

        int idx = -1;
        for(String c: courses){
            System.out.println("Solver: course " + c);
            idx++;
            arrCourses[idx] = c;
            mCourseId2Index.put(c,idx);
            domain[idx] = mCourse2Domain.get(c);
            duration[idx] = mCourse2Duration.get(c);
            System.out.println("Solver: course " + c + " duration " + duration[idx] + " domain = " + domain[idx].toString());
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
        found = true;
        for(int i = 0; i < nbCourses; i++) sol[i] = x[i];
    }
    private void tryValue(int k){
        if(found) return;
        // try value for x[k]: start time-slot for course k
        for(int v: domain[k]){
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
