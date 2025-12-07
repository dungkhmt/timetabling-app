package openerp.openerpresourceserver.generaltimetabling.algorithms.summersemester;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
public class ClassClusterScheduleTimeSlotRoomSolver{
    SummerSemesterSolverVersion3 baseSolver = null;
    List<Long> classIds;
    int session;
    List<ModelResponseTimeTablingClass> classes;
    Set<Long> unScheduledClassIds;
    public ClassClusterScheduleTimeSlotRoomSolver(List<Long> classIds, int session, SummerSemesterSolverVersion3 baseSolver){
        this.classIds = classIds; this.baseSolver = baseSolver;
        this.session = session;
        log.info("ClassClusterScheduleTimeSlotRoomSolver:CConstructor, classIds = " + classIds.size());
    }
    public boolean solve(){
        ModelResponseTimeTablingClass[] a = new ModelResponseTimeTablingClass[classIds.size()];
        for(int i = 0; i < classIds.size(); i++){
            Long id = classIds.get(i);
            ModelResponseTimeTablingClass cls = baseSolver.mClassId2Class.get(id);
            a[i] = cls;
        }
        // sort
        for(int i = 0; i < a.length; i++)
        {
            for(int j = i+1 ; j < a.length; j++){
                if(a[i].getQuantityMax() < a[j].getQuantityMax()){
                    ModelResponseTimeTablingClass t = a[i]; a[i] = a[j]; a[j] = t;
                }
            }
        }
        Set<Integer> candRooms = new HashSet<>();
        for(Long id: classIds){
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(id);
            for(ClassSegment cs: CS){
                for(int r: cs.getDomainRooms()) candRooms.add(r);
            }
        }
        int[] sortedRooms = baseSolver.sortCapacityRoom(candRooms,true);

        unScheduledClassIds = new HashSet<>();
        for(ModelResponseTimeTablingClass cls: a){
            log.info("solve, sorted class " + cls.str() + " qty = " + cls.getQuantityMax());
        }
        for(int r: sortedRooms){
            Classroom clr = baseSolver.W.mIndex2Room.get(r);
            log.info("solve, sorted room " + clr.getClassroom() + " qty = " + baseSolver.I.getRoomCapacity()[r]);
        }
        for(ModelResponseTimeTablingClass cls: a){
            ClassSolverFindSlotsAndRooms SSFSR = new ClassSolverFindSlotsAndRooms(baseSolver,cls,session,sortedRooms);
            boolean ok = SSFSR.solve();
            if(ok){
                log.info("solve, found schedule for class " + cls.str() + " qty " + cls.getQuantityMax());
            }else{
                unScheduledClassIds.add(cls.getId());
                log.info("solve, cannot find any schedule for class " + cls.str() + " qty " + cls.getQuantityMax());
            }
        }
        /*
        for(ModelResponseTimeTablingClass cls: a){
            List<ClassSegment> CS = baseSolver.mClassId2ClassSegments.get(cls.getId());
            int selectedRoom = -1;
            int[] selectedDays = null;
            int[] selectedSlots = null;
            for(int r: sortedRooms){
                for(int[] days: baseSolver.optionDays.get(CS.size())){
                    ClassSolverFindSlots classSolver = new ClassSolverFindSlots(cls,days,r,baseSolver);
                    classSolver.solve();
                    if(classSolver.found){
                        selectedRoom = r; selectedDays = days; selectedSlots = classSolver.solutionSlot;
                    }
                    if(selectedRoom != -1){
                        break;
                    }
                }
                if(selectedRoom != -1){
                    break;
                }
            }
            if(selectedRoom != -1) {// found solution slot-room for cls
                for (int i = 0; i < CS.size(); i++) {
                    ClassSegment cs = CS.get(i);
                    int sl = new DaySessionSlot(selectedDays[i],session,selectedSlots[i]).hash();
                    //baseSolver.assignTimeSlotRoom(cs,sl,selectedRoom);
                    baseSolver.roomSolver.assignTimeSlotRoom(cs,sl,selectedRoom);
                }
            }else{
                unScheduledClassIds.add(cls.getId());
            }
        }

         */
        return true;
    }
}

