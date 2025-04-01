package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Log4j2
public class ClassSegmentsSlotsMatching {
    List<List<ClassSegment>> L;
    List<int[]> slots;
    int n;
    boolean[] visited;
    int[] x;
    int session;
    boolean found = false;
    Map<ClassSegment, Integer> mClassSegment2Slot = null;
    private void solution(){
        // check if slots.get(x[0]), slots.get(x[1]), ..., slots.get(x[n-1]) match with
        // L.get(0), L.get(1), . . ., L.get(n-1)
        boolean ok = true;

        for(int i = 0; i < L.size(); i++){
            ClassSegmentsSlotsMatchingChecker checker = new ClassSegmentsSlotsMatchingChecker(session);
            if(!checker.check(L.get(i),slots.get(x[i]))){
                ok = false; break;
            }else{
                for(ClassSegment cs: checker.mClassSegment2Slot.keySet()){
                    mClassSegment2Slot.put(cs,checker.mClassSegment2Slot.get(cs));
                }
            }
        }
        if(ok) {
            found = true;
            //mClassSegment2Slot = new HashMap<>();

        }
    }
    private void tryValue1(int k){
        if(found) return;
        for(int v = 0; v < n; v++){
            if(!visited[v]){
                x[k] = v; visited[v] = true;
                if(k == n-1) solution();
                else tryValue1(k+1);
                visited[v] = false;
            }
        }
    }

    public ClassSegmentsSlotsMatching(List<List<ClassSegment>> l, List<int[]> slots, int session) {
        // find a permutation of L and permutation of L.get(i) in order to match slots in term of durations
        this.session = session;
        this.L = l;
        this.n = L.size();
        this.slots = slots;
        log.info("ClassSegmentsSlotsMatching with infos");
        for(List<ClassSegment> Li: L){
            String msg = "class " + Li.get(0).getClassId() + " course " + Li.get(0).getCourseCode();
            for(ClassSegment cs: Li) msg = msg + " cls " + cs.getDuration() + ", ";
            log.info("ClassSegmentsSlotsMatching, " + msg);
        }
        for(int[] row: slots){
            String msg = "";
            for(int i : row) msg = msg + i + ",";
            log.info(msg);
        }
    }

    public boolean solve(){
        // find a permutation of L and permutation of L.get(i) in order to match slots in term of durations
        visited = new boolean[n];
        x = new int[n];

        for(int v = 0; v < n; v++) visited[v] = false;
        found = false;
        mClassSegment2Slot = new HashMap<>();
        tryValue1(0);
        return found;
    }
    public static void main(String[] args){
        int[] a = new int[]{1,2,3,4};
        System.out.println(Arrays.asList(a));
    }
}
