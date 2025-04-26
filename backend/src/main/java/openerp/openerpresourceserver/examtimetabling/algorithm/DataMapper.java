package openerp.openerpresourceserver.examtimetabling.algorithm;

import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamClass;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataExamTimeTablingInput;
import openerp.openerpresourceserver.examtimetabling.algorithm.mapdata.MapDataRoom;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.ExamDaySessionSlot;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.TimeSlot;
import openerp.openerpresourceserver.examtimetabling.algorithm.model.TimetablingData;
import openerp.openerpresourceserver.examtimetabling.entity.ExamClass;
import openerp.openerpresourceserver.generaltimetabling.model.Constant;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.repo.ClassroomRepo;

import java.util.ArrayList;
import java.util.*;
public class DataMapper {
    Map<Integer, String> mIndex2Dates;
    public MapDataExamTimeTablingInput mapData(TimetablingData data, List<Classroom> rooms, List<String> examDates){
        MapDataExamTimeTablingInput I = new MapDataExamTimeTablingInput();
        List<MapDataExamClass> CLS = new ArrayList<>();

        Map<String, List<ExamClass>> mGroup2Classes = new HashMap<>();
        for(ExamClass c: data.getExamClasses()){
            if(mGroup2Classes.get(c.getGroupId())==null)
                mGroup2Classes.put(c.getGroupId(),new ArrayList<>());
            mGroup2Classes.get(c.getGroupId()).add(c);
        }
        Map<String, Integer> mGroup2Index = new HashMap<>();
        int gIdx = -1;
        for(String g: mGroup2Classes.keySet()){
            gIdx ++;
            mGroup2Index.put(g,gIdx);
        }
        for(int i = 0; i < data.getExamClasses().size(); i++){
            ExamClass ex = data.getExamClasses().get(i);
            MapDataExamClass mex =new MapDataExamClass();
            mex.setId(i);
            mex.setCourseCode(ex.getCourseId());
            mex.setNbStudents(ex.getNumberOfStudents());
            int groupId = mGroup2Index.get(ex.getGroupId());
            mex.setGroupId(groupId);
            mex.setCode(ex.getClassId());
            CLS.add(mex);
        }
        List<MapDataRoom> mrooms = new ArrayList<>();
        for(int i = 0; i < rooms.size(); i++){
            Classroom r = rooms.get(i);
            MapDataRoom mr = new MapDataRoom();
            int qty = (int)r.getQuantityMax().intValue();
            mr.setId(i); mr.setCapacity(qty); mr.setCode(r.getId());
            mrooms.add(mr);
        }
        //for(TimeSlot ts: data.getAvailableTimeSlots()){
        //    System.out.println("time-slot " + ts.getId() + ", " + ts.getSessionId() + ", " + ts.getStartTime() + ", " + ts.getEndTime());
        //}

        mIndex2Dates = new HashMap<>();
        List<Integer> days = new ArrayList<>();
        for(int i = 0; i < examDates.size(); i++){
            days.add(i);
            mIndex2Dates.put(i,examDates.get(i));
        }
        Map<Integer, List<Integer>> mRoom2OccupiedSlots = new HashMap<>();
        Map<String, Integer> mCourse2ConsecutiveSlots = new HashMap<>();
        for(MapDataRoom r: mrooms) mRoom2OccupiedSlots.put(r.getId(), new ArrayList<>());
        for(MapDataExamClass c: CLS) mCourse2ConsecutiveSlots.put(c.getCourseCode(),1);// by default
        List<Integer> slots = new ArrayList<>();
        for(int d: days){
            for(int s = 1; s <= Constant.examMorningSlots + Constant.examAfternoonSlots; s++){
                int slot = new ExamDaySessionSlot(d,s).hash();
                slots.add(slot);
            }
        }
        I.setRooms(mrooms);
        I.setClasses(CLS);
        I.setDays(days);
        I.setMCourse2NumberConsecutiveSlots(mCourse2ConsecutiveSlots);
        I.setMRoom2OccupiedSlots(mRoom2OccupiedSlots);
        I.setSlots(slots);
        return I;
    }
}
