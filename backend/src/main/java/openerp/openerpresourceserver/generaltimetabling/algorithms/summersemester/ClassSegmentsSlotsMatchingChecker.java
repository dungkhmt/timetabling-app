package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import java.util.*;
@Log4j2
public class ClassSegmentsSlotsMatchingChecker {
    List<ClassSegment> L;
    int[] slots;
    boolean[] visited;
    boolean found;
    int[] x;
    int[] sol;
    Map<ClassSegment, Integer> mClassSegment2Slot = null;
    int n;
    int session;
    public ClassSegmentsSlotsMatchingChecker(int session){
        this.session = session;
    }
    private void tryVal(int k){
        if(found) return;
        for(int v = 0; v < n; v++){
            if(!visited[v]){
                x[k] = v; visited[v] = true;
                if(k == n-1){
                    boolean ok = true;
                    for(int i = 0; i < L.size(); i++){
                        int j = x[i];
                        int d = slots[3*j];
                        if(L.get(i).getDuration() != d){
                            ok = false; break;
                        }
                    }
                    if(ok) {
                        found = true;
                        mClassSegment2Slot = new HashMap<>();
                        for(int i = 0; i < L.size(); i++) {
                            sol[i] = x[i];
                            int j = x[i];
                            int day = slots[3*j+1]; int slot = slots[3*j+2];
                            int s = new DaySessionSlot(day,session,slot).hash();
                            mClassSegment2Slot.put(L.get(i),s);
                        }
                    }
                }else tryVal(k+1);
                visited[v] = false;
            }
        }
    }
    public boolean check(List<ClassSegment> L, int[] slots){
        this.n = L.size();
        String msg = "";
        for(ClassSegment cs: L) msg = msg + cs.getDuration() + ",";
        String msgSlot = "";
        for(int i: slots) msgSlot = msgSlot + i + ",";
        //log.info("check, n = " + n + " class-segment msg = " + msg + " slots msg = " + msgSlot);

        this.L = L; this.slots = slots;
        found = false;
        //n = L.size();
        x = new int[n]; visited = new boolean[n];
        sol = new int[n];
        for(int v = 0; v < n; v++) visited[v] = false;
        tryVal(0);
        //log.info("check, class-segment msg = " + msg + " slots msg = " + msgSlot + " result = " + found);
        return found;
    }
    public int[] getSolution(){
        return sol;
    }

}
