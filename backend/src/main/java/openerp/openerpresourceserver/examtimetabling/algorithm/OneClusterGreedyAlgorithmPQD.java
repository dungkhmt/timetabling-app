package openerp.openerpresourceserver.examtimetabling.algorithm;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamClass;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamTimeTablingInput;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataRoom;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.ExamDaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;

import java.util.*;
import java.util.Map;

@Log4j2
class SlotRoom{
    Map<MapDataExamClass, MapDataRoom> solRoom;
    Map<MapDataExamClass, Integer> solSlot;

    public SlotRoom(Map<MapDataExamClass, MapDataRoom> solRoom, Map<MapDataExamClass, Integer> solSlot) {
        this.solRoom = solRoom;
        this.solSlot = solSlot;
    }
}
public class OneClusterGreedyAlgorithmPQD implements ExamTimeTablingSolver{
    private MapDataExamTimeTablingInput I;
    Map<String, List<MapDataExamClass>> mCourseCode2Classes = new HashMap<>();
    private Map<Integer, Integer> solutionMapSlot;
    private Map<Integer, Integer> solutionMapRoom;

    private Map<String, List<Integer>> solutionMapCourse2Slots;

    private int distance2ScheduledCourses(int startslot, int len){
        int minD = 10000000;
        for(MapDataExamClass c: I.getClasses()){
            if(solutionMapSlot.get(c.getId()) != null){
                int sl = solutionMapSlot.get(c.getId());
                for(int j = 0; j < len; j++){
                    int slot = startslot + j;
                    if(Math.abs(sl-slot) < minD) minD = Math.abs(sl-slot);
                }
            }
        }

        return minD;
    }

    private Map<MapDataExamClass, MapDataRoom> findRoomsForClassesAtSlot(int slot, List<MapDataExamClass> CLS){
        Map<MapDataExamClass,MapDataRoom> rooms = new HashMap<>();
        Set<MapDataRoom> roomUsed = new HashSet<>();
        for(MapDataExamClass c: CLS) {
            MapDataRoom sel_room = null;
            for (MapDataRoom r : I.getRooms()) {
                if(!roomUsed.contains(r) && r.getCapacity() >= c.getNbStudents() &
                !I.getMRoom2OccupiedSlots().get(r.getId()).contains(slot)){
                    sel_room = r; break;
                }
            }
            if(sel_room != null){
                rooms.put(c,sel_room); roomUsed.add(sel_room);
            }else{
                return null;
            }
        }
        if(rooms.keySet().size() < CLS.size()) return null;// not feasible
        return rooms;
    }
    private SlotRoom findSlotAndRoomForCourse(String crs){
        SlotRoom sr = null;
        List<MapDataExamClass> CLS = mCourseCode2Classes.get(crs);
        System.out.println("findSlotAndRoomForCourse, consider course " + crs + " classes = " + CLS.size());
        int maxD = 0;
        Map<MapDataExamClass, MapDataRoom> selRooms = new HashMap<>();
        Map<MapDataExamClass, Integer> selSlots = new HashMap<>();
        boolean foundSol = false ;
        int m = 1;
        if(I.getMCourse2NumberConsecutiveSlots().get(crs) != null)
            m = I.getMCourse2NumberConsecutiveSlots().get(crs);
        if(m == 1) {
            for (int slot : I.getSlots()) {
                int dis = distance2ScheduledCourses(slot,1);
                Map<MapDataExamClass, MapDataRoom> R = findRoomsForClassesAtSlot(slot, mCourseCode2Classes.get(crs));
                if (R != null) {
                    if (maxD < dis) {
                        maxD = dis;
                        selRooms = R;
                        //String msg = "";
                        //for(MapDataExamClass c: selRooms.keySet()) msg = msg + " class " + c.getId() + " is assigned room " + selRooms.get(c).getId() + "; ";
                        //System.out.println("findSlotAndRoomForCourse, consider course " + crs + " -> update maxD = " + maxD + " AND rooms assignment: " + msg);

                        for(MapDataExamClass c: R.keySet()){
                            selSlots.put(c,slot);
                        }
                    }
                }
            }
        }else{
            //int m = I.getMCourse2NumberConsecutiveSlots().get(crs);
            // find m consecutive slots, starting from slot 1, 2, 3, ... for classes
            int slotPerDay = Constant.examAfternoonSlots + Constant.examMorningSlots;
            int[] startSlots = {1,3,2,4,5};
            // distribute classes of course crs among m buckets by the round-robin strategy
            List<MapDataExamClass>[] lstClasses = new List[m];
            for(int j = 0; j < m; j++) lstClasses[j] = new ArrayList<>();
            int cnt = -1;
            for(MapDataExamClass c: mCourseCode2Classes.get(crs)){
                cnt = (cnt + 1)%m;
                lstClasses[cnt].add(c);
            }

            for(int startSlot: startSlots){
                for (int d: I.getDays()) {
                    foundSol = true;
                    Map<MapDataExamClass, MapDataRoom> sol_room = new HashMap<>();
                    for(int j = 0; j < m; j++) {
                        // consider slots: startSlot, startSlot + 1, startSlot + 2, ... startSlot + m-1
                        int slot = new ExamDaySessionSlot(d,startSlot+j).hash();
                        Map<MapDataExamClass, MapDataRoom> s_room = findRoomsForClassesAtSlot(slot, lstClasses[j]);
                        if(s_room == null){
                            foundSol = false; break;
                        }
                        for(MapDataExamClass c: s_room.keySet()){
                            sol_room.put(c,s_room.get(c));
                            selSlots.put(c,slot);
                        }
                    }
                    if(foundSol){
                        int dis = distance2ScheduledCourses(startSlot,m);
                        if(maxD < dis) {
                            maxD = dis;
                            selRooms = new HashMap<>();
                            selSlots = new HashMap<>();

                            for (MapDataExamClass c : sol_room.keySet()) {
                                selRooms.put(c, sol_room.get(c));
                            }
                            for (int j = 0; j < m; j++) {
                                // consider slots: startSlot, startSlot + 1, startSlot + 2, ... startSlot + m-1
                                int slot = new ExamDaySessionSlot(d, startSlot + j).hash();
                                for (MapDataExamClass c : lstClasses[j]) {
                                    selSlots.put(c, slot);
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("findSlotAndRoomForCourse, consider course " + crs + " classes = " + CLS.size() + " FOUND maxD = " + maxD);
        //for(MapDataExamClass c: selRooms.keySet()){
       //     int slot = selSlots.get(c);
        //    System.out.println("findSlotAndRoomForCourse, consider course " + crs + " -> class " + c.getId() + " with room " + selRooms.get(c).getCode() + " slot " + slot);
        //}
        if(maxD == 0) return null;// not found solution
        return new SlotRoom(selRooms,selSlots);
    }


    private void assign(MapDataExamClass c, int roomId, int slot){
        System.out.println("assign(room + " + roomId + " slot " + slot + " to class " + c.toString());
        solutionMapSlot.put(c.getId(),slot);
        solutionMapRoom.put(c.getId(),roomId);
        I.getMRoom2OccupiedSlots().get(roomId).add(slot);
    }
    @Override
    public boolean solve(MapDataExamTimeTablingInput I) {
        this.I = I;
        solutionMapSlot = new HashMap<>();
        solutionMapRoom = new HashMap<>();
        for(MapDataExamClass c: I.getClasses()){
            if(mCourseCode2Classes.get(c.getCourseCode())==null)
                mCourseCode2Classes.put(c.getCourseCode(),new ArrayList<>());
            mCourseCode2Classes.get(c.getCourseCode()).add(c);
        }

        for(String crs: mCourseCode2Classes.keySet()){
            SlotRoom sr = findSlotAndRoomForCourse(crs);
            if(sr == null){
                System.out.println("solve, cannot find solution slots-rooms for course " + crs);
                continue;
            }
            for(MapDataExamClass c: mCourseCode2Classes.get(crs)){
                int slot = sr.solSlot.get(c);
                int roomId = sr.solRoom.get(c).getId();
                assign(c,roomId,slot);

            }

        }

        return true;
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
