package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamClass;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamTimeTablingInput;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataRoom;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.ExamDaySessionSlot;

import java.util.*;

public class GreedyAlgorithmPQD implements ExamTimeTablingSolver{
    MapDataExamTimeTablingInput I;
    Map<Integer, List<MapDataExamClass>> mGroup2Classes;
    private Map<Integer, Integer> solutionMapSlot;
    private Map<Integer, Integer> solutionMapRoom;
    @Override
    public boolean solve(MapDataExamTimeTablingInput I) {
        this.I = I;
        solutionMapRoom = new HashMap<>();
        solutionMapSlot = new HashMap<>();
        mGroup2Classes = new HashMap<>();
        for(MapDataExamClass c: I.getClasses()){
            int groupId = c.getGroupId();
            if(mGroup2Classes.get(groupId)==null)
                mGroup2Classes.put(c.getGroupId(),new ArrayList<>());
            mGroup2Classes.get(groupId).add(c);
        }
        for(int gId: mGroup2Classes.keySet()){
            MapDataExamTimeTablingInput CI = new MapDataExamTimeTablingInput();
            List<MapDataExamClass> CLS = new ArrayList<>();
            for(MapDataExamClass c: mGroup2Classes.get(gId)){
                CLS.add(c);
            }
            CI.setClasses(CLS);
            CI.setDays(I.getDays());
            CI.setRooms(I.getRooms());
            CI.setMRoom2OccupiedSlots(I.getMRoom2OccupiedSlots());
            CI.setMCourse2NumberConsecutiveSlots(I.getMCourse2NumberConsecutiveSlots());
            CI.setSlots(I.getSlots());
            OneClusterGreedyAlgorithmPQD OCS = new OneClusterGreedyAlgorithmPQD();
            System.out.println("START to process group " + gId + ", nbClasses = " + mGroup2Classes.get(gId).size() + " ..............");
            OCS.solve(CI);
            for(int cId: OCS.getSolutionMapSlot().keySet()){
                solutionMapSlot.put(cId,OCS.getSolutionMapSlot().get(cId));
                solutionMapRoom.put(cId,OCS.getSolutionMapRoom().get(cId));
            }
        }
        return false;
    }
    public void printSolution(){
        Map<MapDataRoom, List<MapDataExamClass>> mRoom2Class = new HashMap();
        List<MapDataExamClass> unScheduled = new ArrayList<>();
        for(MapDataExamClass c: I.getClasses()){
            if(solutionMapSlot.get(c.getId())==null){
                unScheduled.add(c);
                continue;
            }
            int slot = solutionMapSlot.get(c.getId());
            int roomId = solutionMapRoom.get(c.getId());
            MapDataRoom r = I.getRooms().get(roomId);
            if(mRoom2Class.get(r)==null) mRoom2Class.put(r,new ArrayList<>());
            mRoom2Class.get(r).add(c);
        }
        for(MapDataRoom r: mRoom2Class.keySet()){
            System.out.println("Room[" + r.getId() + "], code " + r.getCode() + ", cap " + r.getCapacity());
            for(MapDataExamClass c: mRoom2Class.get(r)){
                if(solutionMapSlot.get(c.getId())==null) continue;
                int slot = solutionMapSlot.get(c.getId());
                ExamDaySessionSlot dss = new ExamDaySessionSlot(slot);
                System.out.print("[" + c.getCode() + "-" + c.getCourseCode() + ": " + dss + "] ");
            }
            System.out.println();
        }
        for(int gId: mGroup2Classes.keySet()){
            System.out.println("Group " + gId);
            Map<String, List<MapDataExamClass>> mCourse2Classes = new HashMap<>();
            for(MapDataExamClass c: mGroup2Classes.get(gId)){
                String courseCode = c.getCourseCode();
                if(mCourse2Classes.get(courseCode)==null)
                    mCourse2Classes.put(courseCode,new ArrayList<>());
                mCourse2Classes.get(courseCode).add(c);
            }
            for(String crs: mCourse2Classes.keySet()){
                for(MapDataExamClass c: mCourse2Classes.get(crs)){
                    if(solutionMapSlot.get(c.getId())==null) continue;
                    int slot = solutionMapSlot.get(c.getId());
                    int roomId = solutionMapRoom.get(c.getId());
                    ExamDaySessionSlot dss = new ExamDaySessionSlot(slot);
                    System.out.println(c.toString() + " -> " + dss.toString() + ": " + I.getRooms().get(roomId).getCode());
                }
            }
        }
        System.out.println("Unscheduled = " + unScheduled.size());
    }

    @Override
    public Map<Integer, Integer> getSolutionMapSlot() {
        return solutionMapSlot;
    }

    @Override
    public Map<Integer, Integer> getSolutionMapRoom() {
        return solutionMapRoom;
    }
}
