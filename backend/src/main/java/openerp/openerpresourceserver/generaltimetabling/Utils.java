package openerp.openerpresourceserver.generaltimetabling;

import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;

import java.util.*;
@Log4j2
public class Utils {
    public static List<Integer> fromString(String s, String delimiter){
        String[] a = s.split(delimiter);
        List<Integer> res = new ArrayList<>();
        if(a != null)for(String i: a){
            try {
                int x = Integer.valueOf(i);
                res.add(x);
            }catch (Exception e){
                return null;
            }
        }
        return res;
    }
    public static boolean consecutiveSameSessionClassSegments(TimeTablingClassSegment cs1, TimeTablingClassSegment cs2){
        if(cs1.getRoom() != cs2.getRoom()) return false;
        if(!cs1.getCrew().equals(cs2.getCrew())) return false;
        if(cs1.getWeekday() != cs2.getWeekday()) return false;
        if(cs1.getStartTime() + cs1.getDuration() == cs2.getStartTime() ||
        cs2.getStartTime() + cs2.getDuration() == cs1.getStartTime()) return true;
        else return false;
    }
    public static int consecutiveSameSessionClasses(TimeTablingClass cls1, TimeTablingClass cls2, Map<Long, List<TimeTablingClassSegment>> mID2ClassSegments){
        int cnt = 0;
        if(mID2ClassSegments.get(cls1.getId())==null){
            log.info("consecutiveSameSessionClasses, class " + cls1.getId() + " not have class segments");
            return 0;
        }
        if(mID2ClassSegments.get(cls2.getId())==null){
            log.info("consecutiveSameSessionClasses, class " + cls2.getId() + " not have class segments");
            return 0;
        }
        List<TimeTablingClassSegment> L1 = new ArrayList<>();
        for(TimeTablingClassSegment cs: mID2ClassSegments.get(cls1.getId())) L1.add(cs);
        List<TimeTablingClassSegment> L2 = new ArrayList<>();
        for(TimeTablingClassSegment cs: mID2ClassSegments.get(cls2.getId())) L2.add(cs);

        if(L1 == null || L2 == null) return 0;
        while(L1.size() > 0 && L2.size() > 0){
            int idx1 = -1; int idx2 = -1;
            for (int i1 = 0; i1 < L1.size(); i1++) {
                for (int i2 = 0; i2 < L2.size(); i2++) {
                    TimeTablingClassSegment cs1 = L1.get(i1); TimeTablingClassSegment cs2 = L2.get(i2);
                    if (consecutiveSameSessionClassSegments(cs1, cs2)) {
                        idx1 = i1; idx2 = i2; break;
                    }
                }
                if(idx1 != -1) break;
            }
            if(idx1 != -1 && idx2 != -1) {
                L1.remove(idx1);
                L2.remove(idx2);
                //L1.remove(sel_cs1);
                //L2.remove(sel_cs2);
                cnt++;
                //log.info("reAssignRoom, detect consecutive, L1.sz = " + L1.size() + ", L2.sz = " + L2.size() + ", cnt = " + cnt);
            }else{
                break;
            }
        }
        return cnt;
    }
    public static int extractFloor(String roomCode){
        int floor = 1;
        try {
            String[] s = roomCode.split("-");
            if (s != null && s.length == 2) {
                int n = Integer.parseInt(s[1].trim()); // index of the room
                floor = n / 100;
            }
        }catch (Exception e){ e.printStackTrace();}
        return floor;
    }
    public static List<TimeTablingClass> sort(List<TimeTablingClass> CLS, List<TimeTablingClassSegment> classSegments){
        // sort on courseCode; children BT classes must go right-after the parent LT class
        //                  2 classes consecutive are adjacent
        log.info("sort START......classSegments.sz = " + classSegments.size());

        List<TimeTablingClass> res = new ArrayList<>();
        Map<String, List<TimeTablingClass>> mCourse2Classes = new HashMap<>();
        Map<Long, TimeTablingClass> mClassID2Class = new HashMap<>();
        Map<Long, List<Long>> mClassID2ChildrenClassIDs = new HashMap<>();
        Map<Long, List<TimeTablingClassSegment>> mID2ClassSegments = new HashMap<>();
        for(TimeTablingClassSegment cs: classSegments){
            Long id = cs.getClassId();//cs.getParentId();
            if(mID2ClassSegments.get(id) == null)
                mID2ClassSegments.put(id, new ArrayList<>());
            mID2ClassSegments.get(id).add(cs);
            log.info("sort, add class segment " + cs.getId() + " to classId " + id);
        }
        for(TimeTablingClass cls: CLS){
            mClassID2Class.put(cls.getId(),cls);
        }
        for(TimeTablingClass cls: CLS){
            Long pId = cls.getParentClassId();
            if(pId != null){
                if(mClassID2ChildrenClassIDs.get(pId)==null){
                    mClassID2ChildrenClassIDs.put(pId, new ArrayList<>());
                }
                mClassID2ChildrenClassIDs.get(pId).add(cls.getId());
            }
        }
        Map<Long, Long> mClassID2ConsecutiveClassID = new HashMap<>();
        for(TimeTablingClass cls1: CLS){
            for(TimeTablingClass cls2: CLS)if(cls1.getId() < cls2.getId()){
                int cnt = consecutiveSameSessionClasses(cls1,cls2,mID2ClassSegments);
                if(cnt == 2){
                    mClassID2ConsecutiveClassID.put(cls1.getId(),cls2.getId());
                    mClassID2ConsecutiveClassID.put(cls2.getId(),cls1.getId());
                    log.info("sort: discover consecutive class [" + cls1.getClassCode() + "," + cls1.getModuleCode() + "] and [" + cls2.getClassCode() + "," + cls2.getModuleCode() + "]");
                }

            }
        }

        for(TimeTablingClass cls: CLS){
            if(mCourse2Classes.get(cls.getModuleCode())==null)
                mCourse2Classes.put(cls.getModuleCode(),new ArrayList<>());
            mCourse2Classes.get(cls.getModuleCode()).add(cls);
        }
        List<String> courses= new ArrayList<>();
        for(String c: mCourse2Classes.keySet()) courses.add(c);
        Collections.sort(courses);
        Map<Long, Boolean> appear = new HashMap<>();
        for(String c: courses){
            for(TimeTablingClass cls: mCourse2Classes.get(c)){
                if(appear.get(cls.getId())!=null) continue;
                appear.put(cls.getId(),true);
                res.add(cls);
                log.info("sort add class " + cls.getClassCode() + "," + cls.getModuleCode() );
                if(mClassID2ChildrenClassIDs.get(cls.getId())!=null){
                    for(Long cid: mClassID2ChildrenClassIDs.get(cls.getId())){
                        TimeTablingClass child = mClassID2Class.get(cid);
                        if(child != null){
                            res.add(child);
                            appear.put(cid,true);
                        }
                    }
                }else{
                    if(mClassID2ConsecutiveClassID.get(cls.getId())!=null){
                        Long consecutiveID = mClassID2ConsecutiveClassID.get(cls.getId());
                        TimeTablingClass consecutiveClass = mClassID2Class.get(consecutiveID);
                        res.add(consecutiveClass);
                        appear.put(consecutiveID,true);
                        log.info("sort, add consecutive class " +consecutiveClass.getClassCode() + "," + consecutiveClass.getModuleCode());
                    }
                }
            }
        }
        return res;
    }
}
