package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ClassSolverFindSlots{
    SummerSemesterSolverVersion3 baseSolver;
    ModelResponseTimeTablingClass cls;
    int[] days;
    int room;
    List<ClassSegment> CS;
    List<int[]> domains;
    int[] x_slot;
    int[] solutionSlot = null;
    boolean found = false;
    public ClassSolverFindSlots(ModelResponseTimeTablingClass cls, int[] days, int room, SummerSemesterSolverVersion3 baseSolver){
        this.baseSolver = baseSolver;
        this.cls = cls; this.days = days;
        this.room = room;
        CS = baseSolver.mClassId2ClassSegments.get(cls.getId());
        domains = new ArrayList<>();
        for(ClassSegment cs: CS){
            int[] d = new int[2];// consider 2 options: tiet dau va tiet cuoi
            d[0] = 1;
            d[1] = Constant.slotPerCrew - cs.getDuration() + 1;
            domains.add(d);
        }
        x_slot = new int[CS.size()];
    }
    private void solution(){
        boolean ok = true;
        for(int i = 0; i < CS.size(); i++){
            if(!baseSolver.roomSolver.checkValidSlotAndRoom(CS.get(i),x_slot[i],room)){
                ok = false; break;
            }
        }
        if(ok){
            solutionSlot = new int[CS.size()];
            for(int i = 0; i < CS.size(); i++) solutionSlot[i] = x_slot[i];
            found = true;
        }
    }
    private void tryClassSegment(int i){
        if(found) return;
        for(int s: domains.get(i)){
            x_slot[i] = s;
            if(i == CS.size()-1){
                solution();
            }else{
                tryClassSegment(i+1);
            }
        }
    }
    public void solve(){
        found = false; solutionSlot = null;
        tryClassSegment(0);
    }
}

