package openerp.openerpresourceserver.generaltimetabling;

import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import java.util.*;
public class Utils {
    public static List<TimeTablingClass> sort(List<TimeTablingClass> CLS){
        // sort on courseCode; children BT classes must go right-after the parent LT class
        List<TimeTablingClass> res = new ArrayList<>();
        Map<String, List<TimeTablingClass>> mCourse2Classes = new HashMap<>();
        Map<Long, TimeTablingClass> mClassID2Class = new HashMap<>();
        Map<Long, List<Long>> mClassID2ChildrenClassIDs = new HashMap<>();
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
                if(mClassID2ChildrenClassIDs.get(cls.getId())!=null){
                    for(Long cid: mClassID2ChildrenClassIDs.get(cls.getId())){
                        TimeTablingClass child = mClassID2Class.get(cid);
                        if(child != null){
                            res.add(child);
                            appear.put(cid,true);
                        }
                    }
                }
            }
        }
        return res;
    }
}
