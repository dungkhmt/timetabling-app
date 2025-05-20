package openerp.openerpresourceserver.generaltimetabling.algorithms.util;

import java.util.HashSet;

public class BacktrackingCourseSolver {
    int nbSlotPerSession = 6;// so tiet trong 1 session (morning, afternoon)
    int nbSessions = 5;// sessions are indexed from 0, 1, . . ., nbSessions - 1
    String[] courses;
    int[] duration;
    int nbCourses;// courses are indexed from 0,1,...,nbCourses - 1
    int[] x_slot;// x_slot[i] is the start time-slot of course i
    int[] x_session;// x_session[i] is the session assigned to course i

    int separationScore(){
        HashSet<Integer> sessionsCovered = new HashSet<>();
        for(int i = 0; i <= nbCourses - 1; i++)
            sessionsCovered.add(x_session[i]);
        return sessionsCovered.size();
    }
    boolean check(int ses, int sl, int k){
        // return true if (ses, sl) can be assigned to course k without overlapping with courses 0,...,k-1

    }
    private void updateBest(){
        // compare current solution with the best solution found before
    }
    private void tryCourse(int k){
        for(int ses = 0; ses <= nbSessions-1; ses++) {
            for(int sl = 1; sl <= nbSlotPerSession; sl++) {
                if(check(ses, sl, k)) {
                    x_slot[k] = sl;
                    x_session[k] = ses;
                    if (k == courses.length - 1)
                        updateBest();
                    else tryCourse(k+1);
                }
            }
        }
    }
    public static void main(String[] args){

    }
}
