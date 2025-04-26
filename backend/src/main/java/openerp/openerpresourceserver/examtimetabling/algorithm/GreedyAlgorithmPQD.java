package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamClass;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamTimeTablingInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GreedyAlgorithmPQD implements ExamTimeTablingSolver{
    Map<Integer, List<MapDataExamClass>> mGroup2Classes;
    private Map<Integer, Integer> solutionMapSlot;
    private Map<Integer, Integer> solutionMapRoom;
    @Override
    public boolean solve(MapDataExamTimeTablingInput I) {
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
            OCS.solve(CI);
            for(int cId: OCS.getSolutionMapSlot().keySet()){
                solutionMapSlot.put(cId,OCS.getSolutionMapSlot().get(cId));
                solutionMapRoom.put(cId,OCS.getSolutionMapRoom().get(cId));
            }
        }
        return false;
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
