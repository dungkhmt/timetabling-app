package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.algorithms.ClassSegmentPartitionConfigForSummerSemester;
import openerp.openerpresourceserver.generaltimetabling.algorithms.DaySessionSlot;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ClassSegment;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ConnectedComponentClassSolver;
import openerp.openerpresourceserver.generaltimetabling.exception.ConflictScheduleException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateClassSegmentRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputAssignSessionToClassesSummer;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputComputeClassCluster;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.V2UpdateClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.*;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputAdvancedFilter;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputManualAssignTimeTable;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputSearchRoom;
import openerp.openerpresourceserver.generaltimetabling.model.response.*;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
import openerp.openerpresourceserver.generaltimetabling.service.ScheduleService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Log4j2
@Slf4j
public class TimeTablingClassServiceImpl implements TimeTablingClassService {
    @Autowired
    private ClusterRepo clusterRepo;

    @Autowired
    private ClusterClassRepo clusterClassRepo;

    @Autowired
    private PlanGeneralClassRepo planGeneralClassRepository;

    @Autowired
    private TimeTablingClassSegmentRepo timeTablingClassSegmentRepo;

    @Autowired
    private TimeTablingClassRepo timeTablingClassRepo;

    @Autowired
    private TimeTablingCourseRepo timeTablingCourseRepo;

    @Autowired
    private ClassGroupRepo classGroupRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Autowired
    private ClassroomRepo classroomRepo;

    @Autowired
    private TimeTablingVersionRepo timeTablingVersionRepo;

    @Autowired
    private TimeTablingBatchRepo timeTablingBatchRepo;

    @Autowired
    private ScheduleService scheduleService;

    @Transactional
    private void createClassSegment(TimeTablingClass gc, List<Integer> seqSlots, Long versionId) {
        log.info("createClassSegments, classId " + gc.getId() + ", crew " + gc.getCrew() + "  course "
                + gc.getModuleCode() + " type " + gc.getClassType() + " slots = " + seqSlots);

        for (int s : seqSlots) {
            TimeTablingClassSegment cs = new TimeTablingClassSegment();
            Long csId = timeTablingClassSegmentRepo.getNextReferenceValue();
            cs.setId(csId);
            cs.setClassId(gc.getId());
            cs.setCrew(gc.getCrew());
            cs.setDuration(s);
            cs.setVersionId(versionId);
            log.info("createClassSegments, classId " + gc.getId() + ", crew " + gc.getCrew() + "  course "
                    + gc.getModuleCode() + " type " + gc.getClassType() + " slots = " + seqSlots
                    + " add slot " + s);
            cs = timeTablingClassSegmentRepo.save(cs);
        }

    }

    @Transactional
    private void createClassSegmentsOfASession(List<TimeTablingClass> LS,
                                               List<List<Integer>> PLT,
                                               List<List<Integer>> PBT,
                                               List<List<Integer>> PLTBT, Long versionId) {
        // consider morning classes first
        List<TimeTablingClass> LLT = new ArrayList<>();
        List<TimeTablingClass> LBT = new ArrayList<>();
        List<TimeTablingClass> LLTBT = new ArrayList<>();
        for (TimeTablingClass gc : LS) {
            if (gc.getClassType().equals("LT")) LLT.add(gc);
            else if (gc.getClassType().equals("BT")) LBT.add(gc);
            else if (gc.getClassType().equals("LT+BT")) LLTBT.add(gc);
        }
        // apply round robin
        int idx = 0;

        for (TimeTablingClass gc : LLT) {
            if (PLT != null && PLT.size() > 0) {
                List<Integer> seqSlots = PLT.get(idx);
                // create a.size class-segment for class gc
                //
                if (seqSlots.size() >= 1) {
                    createClassSegment(gc, seqSlots, versionId);
                }
                idx += 1;
                if (idx >= PLT.size()) idx = 0;
            }
        }
        idx = 0;
        for (TimeTablingClass gc : LBT) {
            if (PBT != null && PBT.size() > 0) {
                List<Integer> seqSlots = PBT.get(idx);
                // create a.size class-segment for class gc
                //if(seqSlots.size() > 1){
                if (seqSlots.size() >= 1) {
                    createClassSegment(gc, seqSlots,versionId);
                }
                idx += 1;
                if (idx >= PBT.size()) idx = 0;
            }
        }
        idx = 0;
        for (TimeTablingClass gc : LLTBT) {
            if (PLTBT != null && PLTBT.size() > 0) {
                List<Integer> seqSlots = PLTBT.get(idx);
                // create a.size class-segment for class gc
                //if(seqSlots.size() > 1){
                if (seqSlots.size() >= 1) {
                    createClassSegment(gc, seqSlots,versionId);
                }
                idx += 1;
                if (idx >= PLTBT.size()) idx = 0;
            }
        }
    }

    @Override
    public List<TimeTablingClassSegment> createClassSegment(CreateClassSegmentRequest I) {
        List<TimeTablingClassSegment> res = new ArrayList<>();
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(I.getSemester());
        log.info("createClassSegment, semester = " + I.getSemester() + " classes.sz = " + cls.size());
        int cnt = 0;
        for(TimeTablingClass c: cls){
            cnt++;
            TimeTablingClassSegment cs = new TimeTablingClassSegment();
            cs.setCrew(c.getCrew());
            cs.setDuration(c.getDuration());
            cs.setVersionId(I.getVersionId());
            Long id = timeTablingClassSegmentRepo.getNextReferenceValue();
            cs.setId(id);
            cs.setClassId(c.getId());
            cs = timeTablingClassSegmentRepo.save(cs);
            log.info("createClassSegment, semester = " + I.getSemester() + " saved " + cnt + "/" + cls.size() + " gen id = " + id + " after save object, cs.id = " + cs.getId());
            res.add(cs);
        }
        return res;
    }

    @Override
    public TimeTablingClass updateSession(TimeTablingClass cls, String crew) {
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassId(cls.getId());
        for(TimeTablingClassSegment cs: classSegments){
            cs.setCrew(crew);
        }
        classSegments = timeTablingClassSegmentRepo.saveAll(classSegments);
        cls.setCrew(crew);
        cls = timeTablingClassRepo.save(cls);
        return cls;
    }

    @Override
    public List<TimeTablingClass> assignSessionToClassesSummer(ModelInputAssignSessionToClassesSummer I) {
        List<TimeTablingClass> res = timeTablingClassRepo.findAllBySemester(I.getSemester());
        Map<String, List<TimeTablingClass>> mCourse2Classes = new HashMap<>();
        Map<Long, TimeTablingClass> mID2Class = new HashMap<>();
        Map<Long, List<Long>> mClassID2ChildrenIds = new HashMap<>();
        for(TimeTablingClass cls: res){
            String courseCode = cls.getModuleCode();
            if(mCourse2Classes.get(courseCode)==null)
                mCourse2Classes.put(courseCode, new ArrayList<>());
            mCourse2Classes.get(courseCode).add(cls);
            log.info("assignSessionToClassesSummer, add class " + cls.getClassCode() + " to course " + courseCode + " -> sz = " + mCourse2Classes.get(courseCode).size());
            mID2Class.put(cls.getId(),cls);
        }
        Set<Long> classIds = new HashSet<>();
        for(TimeTablingClass cls: res){
            mClassID2ChildrenIds.put(cls.getId(), new ArrayList<>());
            classIds.add(cls.getId());
        }
        log.info("assignSessionToClassesSummer, list classses = " + res.size());
        for(TimeTablingClass cls: res){
            Long parentId = cls.getParentClassId();
            if(parentId != null) {
                if (mClassID2ChildrenIds.get(parentId) == null)
                    mClassID2ChildrenIds.put(parentId, new ArrayList<>());
                mClassID2ChildrenIds.get(parentId).add(cls.getId());
                log.info("assignSessionToClassesSummer add child class " + cls.getClassCode() + " to parent " + parentId);
            }
        }
        List<TimeTablingClass> morningCLS = new ArrayList<>();
        List<TimeTablingClass> afternoonCLS = new ArrayList<>();

        for(String course: mCourse2Classes.keySet()) {
            List<TimeTablingClass> CLS = mCourse2Classes.get(course);
            List<TimeTablingClass> CLS_LT = new ArrayList<>();
            List<TimeTablingClass> CLS_LT_BT = new ArrayList<>();
            for(TimeTablingClass cls: CLS) {
                if (cls.getClassType().equals("LT")) {
                    CLS_LT.add(cls);
                    log.info("assignSessionToClassesSummer CONSIDER course " + course + " -> add LT class " + cls.getClassCode());
                } else if (cls.getClassType().equals("LT+BT")){
                    CLS_LT_BT.add(cls);
                    log.info("assignSessionToClassesSummer CONSIDER course " + course + " -> add LT+BT class " + cls.getClassCode());

                }else{
                    //log.info("assignSessionToClassesSummer, UNKNOWN ClassType " + cls.getClassType() + " classCode " + cls.getClassCode());
                }
            }
            int sz = CLS_LT.size();
            for(int i = 0; i < CLS_LT.size(); i++){
                TimeTablingClass cls = CLS_LT.get(i);
                if(morningCLS.size() > afternoonCLS.size()){
                    afternoonCLS.add(cls);
                    log.info("assignSessionToClassesSummer, course LT " + course + ", i = " + i + "/" + sz + " (" + cls.getClassCode() + ")" + "C");
                    for(Long cid: mClassID2ChildrenIds.get(cls.getId())){
                        TimeTablingClass childCLS = mID2Class.get(cid);
                        afternoonCLS.add(childCLS);
                        log.info("assignSessionToClassesSummer, course BT " + course + ", i = " + i + "/" + sz + " updateSession Child BTF (" + childCLS.getClassCode() + ")" + "C");
                    }
                }else{
                    morningCLS.add(cls);
                    log.info("assignSessionToClassesSummer, course LT " + course + ", i = " + i + "/" + sz + " (" + cls.getClassCode() + ")" + "S");
                    for(Long cid: mClassID2ChildrenIds.get(cls.getId())){
                        TimeTablingClass childCLS = mID2Class.get(cid);
                        morningCLS.add(childCLS);
                        log.info("assignSessionToClassesSummer, course BT " + course + ", i = " + i + "/" + sz + " updateSession Child BTF (" + childCLS.getClassCode() + ")" + "S");
                    }
                }
            }
            /*
            // divide LT classes and follows are BT classes
            if(CLS_LT.size() > 0){
                TimeTablingClass aCls = CLS_LT.get(0);
                int duration = aCls.getDuration();
                int sz = CLS_LT.size();
                int mid = sz/2;
                for (int i = 0; i < mid; i++) {
                    TimeTablingClass cls = CLS_LT.get(i);
                    //cls.setCrew("S");
                    updateSession(cls, "S");
                    classIds.remove(cls.getId());

                    for(Long cid: mClassID2ChildrenIds.get(cls.getId())){
                        TimeTablingClass childCLS = mID2Class.get(cid);
                        updateSession(childCLS,"S");
                        classIds.remove(childCLS.getId());
                        log.info("assignSessionToClassesSummer, course BT " + course + ", i = " + i + "/" + sz + " updateSession Child BTF (" + childCLS.getClassCode() + ")" + "S");
                    }
                    log.info("assignSessionToClassesSummer, course LT " + course + ", i = " + i + "/" + sz + " updateSession(" + cls.getClassCode() + ")," + "S");
                }
                for (int i = mid; i < sz; i++) {
                    TimeTablingClass cls = CLS_LT.get(i);
                    updateSession(cls, "C");
                    classIds.remove(cls.getId());

                    for(Long cid: mClassID2ChildrenIds.get(cls.getId())){
                        TimeTablingClass childCLS = mID2Class.get(cid);
                        updateSession(childCLS,"C");
                        classIds.remove(childCLS.getId());
                        log.info("assignSessionToClassesSummer, course BT " + course + ", i = " + i + "/" + sz + " updateSession Child BT (" + childCLS.getClassCode() + ")" + "C");
                    }
                    log.info("assignSessionToClassesSummer, course LT " + course + ", i = " + i + "/" + sz + " updateSession(" + cls.getClassCode() + ")," + "C");
                }
            }
            */

            // divide LT+BT
            if(CLS_LT_BT.size() > 0) {
                TimeTablingClass aCls = CLS_LT_BT.get(0);
                int duration = aCls.getDuration();
                boolean morningLess = morningCLS.size() < afternoonCLS.size();
                sz = CLS_LT_BT.size();

                int mid = sz / 2;
                if(sz % 2 == 1){
                    if(morningLess) mid = sz - mid;
                }else{// even
                    if (sz % 4 == 2 && duration == 5){// duration 5 -> employ combination 2 + 3
                        mid = mid + 1; // example 6 classes -> 4 morning, 2 afternoon (better than 3+3)
                                    // 10 classes -> 6 morning, 4 afternoon (better than 5+5)

                        if(!morningLess) mid = sz - mid;
                    }
                }



                        for (int i = 0; i < mid; i++) {
                            TimeTablingClass cls = CLS_LT_BT.get(i);
                            //cls.setCrew("S");
                            //updateSession(cls, "S");
                            //classIds.remove(cls.getId());
                            morningCLS.add(cls);
                            log.info("assignSessionToClassesSummer, course LT+BT " + course + ", i = " + i + "/" + sz + " updateSession(" + cls.getClassCode() + "," + "S");

                        }
                        for (int i = mid; i < sz; i++) {
                            TimeTablingClass cls = CLS_LT_BT.get(i);
                            //updateSession(cls, "C");
                            //classIds.remove(cls.getId());
                            afternoonCLS.add(cls);
                            log.info("assignSessionToClassesSummer, course LT+BT " + course + ", i = " + i + "/" + sz + " updateSession(" + cls.getClassCode() + "," + "C");

                            //cls.setCrew("C");
                        }

            }
        }
        for(TimeTablingClass cls: morningCLS){
            updateSession(cls, "S");
            classIds.remove(cls.getId());
        }
        for(TimeTablingClass cls: afternoonCLS){
            updateSession(cls, "C");
            classIds.remove(cls.getId());
        }
        log.info("assignSessionToClassesSummer morning classes = " + morningCLS.size() + " afternoon classes = " + afternoonCLS.size());
        log.info("assignSessionToClassesSummer, REMAINS " + classIds.size() + " NOT ASSIGNED");
        for(Long id: classIds){
            TimeTablingClass cls = mID2Class.get(id);
            log.info("assignSessionToClassesSummer, REMAINS class not assigned id = " + cls.getId() + " code " + cls.getClassCode()+ " course " + cls.getModuleCode() + " type " + cls.getClassType() + " children " + mClassID2ChildrenIds.get(cls.getId()).size() + " parentId = " + cls.getParentClassId());
        }
        return res ;
    }

    @Override
    public List<TimeTablingClassSegment> createClassSegmentForSummerSemester(CreateClassSegmentRequest I) {
        TimeTablingTimeTableVersion version = timeTablingVersionRepo.findById(I.getVersionId()).orElse(null);
        if(version == null) return new ArrayList<>(); // do nothing

        // remove all existing class segments of the current version
        List<TimeTablingClassSegment> CS = timeTablingClassSegmentRepo.findAllByVersionId(version.getId());
        timeTablingClassSegmentRepo.deleteAll(CS);

        // load original timetabling classes of the batch version.getBatchId()

        ClassSegmentPartitionConfigForSummerSemester P = new ClassSegmentPartitionConfigForSummerSemester();
        List<TimeTablingCourse> courses = timeTablingCourseRepo.findAll();
        //List<TimeTablingClass> cls = timeTablingClassRepo.findAll();
        //List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(I.getSemester());
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllByBatchId(version.getBatchId());

        log.info("createClassSegmentForSummerSemester, semester = " + I.getSemester() + " number classes = " + cls.size()
                + " number courses = " + courses.size());

        Map<String, List<TimeTablingClass>> mCourseCode2Class = new HashMap<>();
        for (TimeTablingClass gc : cls) {
            String courseCode = gc.getModuleCode();
            if (mCourseCode2Class.get(courseCode) == null)
                mCourseCode2Class.put(courseCode, new ArrayList<>());
            mCourseCode2Class.get(courseCode).add(gc);
            log.info("createClassSegmentForSummerSemester -> mCourseCode2Class.get(" + courseCode + ").add(gc.id " + gc.getId() + " gc.code " + gc.getClassCode() + ")");
        }
        Map<String, List<List<Integer>>> mCourseCodeLT2Partitions = new HashMap<>();
        Map<String, List<List<Integer>>> mCourseCodeBT2Partitions = new HashMap<>();
        Map<String, List<List<Integer>>> mCourseCodeLTBT2Partitions = new HashMap<>();
        for (TimeTablingCourse c : courses) {
            if (c.getPartitionLtForSummerSemester() != null) {
                List<List<Integer>> L1 = Util.extractPartition(c.getPartitionLtForSummerSemester());
                mCourseCodeLT2Partitions.put(c.getId(), L1);
            }
            if (c.getPartitionBtForSummerSemester() != null) {
                List<List<Integer>> L1 = Util.extractPartition(c.getPartitionBtForSummerSemester());
                mCourseCodeBT2Partitions.put(c.getId(), L1);
            }
            if (c.getPartitionLtBtForSummerSemester() != null) {
                List<List<Integer>> L1 = Util.extractPartition(c.getPartitionLtBtForSummerSemester());
                mCourseCodeLTBT2Partitions.put(c.getId(), L1);
                log.info("createClassSegmentForSummerSemester, mCourse2LTBTPartition.put(" + c.getId() + ", L1.sz = " + L1.size() + " partitionConfigLTBTSummer = " + c.getPartitionLtBtForSummerSemester());
            }
        }

        for (String courseCode : mCourseCode2Class.keySet()) {
            List<TimeTablingClass> L = mCourseCode2Class.get(courseCode);
            List<TimeTablingClass> LS = new ArrayList<>();
            List<TimeTablingClass> LC = new ArrayList<>();
            for (TimeTablingClass gc : L) {
                if (gc.getCrew().equals("S")) LS.add(gc);
                else if (gc.getCrew().equals("C")) LC.add(gc);
            }
            // consider morning classes first
            List<TimeTablingClass> LLT = new ArrayList<>();
            List<TimeTablingClass> LBT = new ArrayList<>();
            List<TimeTablingClass> LLTBT = new ArrayList<>();
            for (TimeTablingClass gc : L) {
                if (gc.getClassType().equals("LT")) LLT.add(gc);
                else if (gc.getClassType().equals("BT")) LBT.add(gc);
                else if (gc.getClassType().equals("LT+BT")) LLTBT.add(gc);
            }

            List<List<Integer>> PLT = mCourseCodeLT2Partitions.get(courseCode);
            List<List<Integer>> PBT = mCourseCodeBT2Partitions.get(courseCode);
            List<List<Integer>> PLTBT = mCourseCodeLTBT2Partitions.get(courseCode);
            // if not configured in DataBase, then use hardcode config
            if (PLT == null) {
                if (LLT != null && LLT.size() >= 1) {
                    TimeTablingClass gc = LLT.get(0);
                    if (P.partitions.get(gc.getDuration()) != null)
                        PLT = P.partitions.get(gc.getDuration());
                }
            }
            if (PBT == null) {
                if (LBT != null && LBT.size() >= 1) {
                    TimeTablingClass gc = LBT.get(0);
                    if (P.partitions.get(gc.getDuration()) != null)
                        PBT = P.partitions.get(gc.getDuration());
                }
            }
            if (PLTBT == null) {
                if (LLTBT != null && LLTBT.size() >= 1) {
                    TimeTablingClass gc = LLTBT.get(0);
                    if (P.partitions.get(gc.getDuration()) != null) {
                        PLTBT = P.partitions.get(gc.getDuration());
                        log.info("createClassSegmentForSummerSemester, course " + courseCode+ " getPartition of duration " + gc.getDuration() + " GOT PLTBT.sz = " + PLTBT.size());
                    }
                }
            }
            log.info("createClassSegmentForSummerSemester, for course " + courseCode + " morning sz = " + LS.size());
            createClassSegmentsOfASession(LS, PLT, PBT, PLTBT,I.getVersionId());
            log.info("createClassSegmentForSummerSemester, for course " + courseCode + " afternoon sz = " + LC.size());
            createClassSegmentsOfASession(LC, PLT, PBT, PLTBT,I.getVersionId());

        }
        return new ArrayList<>();
    }

    @Override
    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(String semester, Long groupId, Long versionId) {
        List<TimeTablingClass> cls = null;
        Map<Long, List<Long>> mClassId2GroupIds = new HashMap<>();
        if(groupId == null){
            cls = timeTablingClassRepo.findAllBySemester(semester);
            log.info("getTimeTablingClassDtos, group NULL, version_id = " + versionId + " -> cls.sz = " + cls.size());
        }else{
            List<ClassGroup> CG = classGroupRepo.findAllByGroupId(groupId);
            List<Long> classIds = CG.stream().map(c -> c.getClassId()).toList();
            List<TimeTablingClass> classInGroup = timeTablingClassRepo.findAllByIdIn(classIds);
            cls = new ArrayList<>();
            for(TimeTablingClass c: classInGroup){
                if(c.getSemester() != null && c.getSemester().equals(semester)){
                    cls.add(c);
                }
            }
            log.info("getTimeTablingClassDtos, group NOT NULL, version_id = " + versionId + " -> cls.sz = " + cls.size());
        }
        List<Long> classIds = cls.stream().map(c -> c.getId()).toList();
        List<ClassGroup> CG = classGroupRepo.findAllByClassIdIn(classIds);
        for(ClassGroup cg: CG){
            Long classId = cg.getClassId();
            if(mClassId2GroupIds.get(classId)==null)
                mClassId2GroupIds.put(classId, new ArrayList<>());
            mClassId2GroupIds.get(classId).add(cg.getGroupId());
        }

        Map<Long, List<String>> mClassId2GroupNames = new HashMap<>();
        List<Group> groups = groupRepo.findAll();
        Map<Long, Group> mId2Group = new HashMap<>();
        for(Group g: groups) mId2Group.put(g.getId(),g);
        for(TimeTablingClass c: cls){
            List<Long> gids = mClassId2GroupIds.get(c.getId());
            mClassId2GroupNames.put(c.getId(), new ArrayList<>());
            if(gids != null) for(Long gid: gids){
                String gname = mId2Group.get(gid).getGroupName();
                mClassId2GroupNames.get(c.getId()).add(gname);
            }
        }
        
        List<TimeTablingClassSegment> classSegments;
        if (versionId != null) {
            classSegments = timeTablingClassSegmentRepo.findAllByClassIdInAndVersionId(classIds, versionId);
            log.info("getTimeTablingClassDtos, filtered by version_id = " + versionId + ", found " + classSegments.size() + " segments");
        } else {
            classSegments = timeTablingClassSegmentRepo.findAllByClassIdInAndVersionIdIsNull(classIds);
            log.info("getTimeTablingClassDtos: Lọc class segments với version_id = NULL, tìm thấy " + classSegments.size() + " segments");
        }
        
        Map<Long, List<TimeTablingClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(TimeTablingClassSegment cs: classSegments){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }

        // sort class such that parent-child classes go consecutive each other
        List<TimeTablingClass> s_cls = new ArrayList<>();
        Map<TimeTablingClass, List<TimeTablingClass>> mClass2Children = new HashMap<>();
        Map<Long, TimeTablingClass> mID2Class = new HashMap<>();
        for(TimeTablingClass c: cls){
            mID2Class.put(c.getId(),c);
        }
        for(TimeTablingClass c: cls){
            Long pid = c.getParentClassId();
            if(pid != null){
                TimeTablingClass pCls = mID2Class.get(pid);
                if(pCls != null){
                    if(mClass2Children.get(pCls)==null) mClass2Children.put(pCls,new ArrayList<>());
                    mClass2Children.get(pCls).add(c);
                }
            }
        }
        Map<TimeTablingClass,Boolean> appeared = new HashMap<>();
        for(int i = 0; i < cls.size(); i++) {
            TimeTablingClass c = cls.get(i);
            appeared.put(c,false);
        }
        for(int i = 0; i < cls.size(); i++){
            TimeTablingClass c = cls.get(i);
            if(appeared.get(c) == false) {
                s_cls.add(c); appeared.put(c,true);
                //log.info("getTimeTablingClassDtos, process sorting, add parent class " + c.getId() + "," + c.getClassType() + "," + c.getModuleCode());
                if (mClass2Children.get(c) != null) {
                    for (TimeTablingClass cc : mClass2Children.get(c)) {
                        s_cls.add(cc);
                        appeared.put(cc, true);
                        //log.info("getTimeTablingClassDtos, process sorting, add child class " + cc.getId() + "," + cc.getClassType() + "," + cc.getModuleCode() + ", parent " + cc.getParentClassId());
                    }
                }
            }
        }
        //return cls.stream()
        return s_cls.stream() // use sorted list
                .map(c -> ModelResponseTimeTablingClass.builder()
                        .id(c.getId())
                        .quantity(c.getQuantity())
                        .quantityMax(c.getQuantityMax())
                        .moduleCode(c.getModuleCode())
                        .moduleName(c.getModuleName())
                        .classType(c.getClassType())
                        .classCode(c.getClassCode())
                        .semester(c.getSemester())
                        .studyClass(c.getStudyClass())
                        .mass(c.getMass())
                        .state(c.getState())
                        .crew(c.getCrew())
                        .openBatch(c.getOpenBatch())
                        .course(c.getCourse())
                        .refClassId(c.getRefClassId())
                        .parentClassId(c.getParentClassId())
                        .duration(c.getDuration())
                        .groupName(c.getGroupName())
                        .listGroupName(mClassId2GroupNames.get(c.getId()))
                        .timeSlots(mClassId2ClassSegments.get(c.getId()))
                        .learningWeeks(c.getLearningWeeks())
                        .foreignLecturer(c.getForeignLecturer())
                        .build()
                ).toList();
    }

    @Override
    public List<ModelResponseClassSegment> getClasssegmentsOfVersion(String userId, Long versionId) {
        List<ModelResponseClassSegment> res = new ArrayList<>();
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByVersionId(versionId);
        Set<Long> classIds = new HashSet<>();
        for(TimeTablingClassSegment cs: classSegments) classIds.add(cs.getClassId());
        List<Long> listClassIds = new ArrayList<>();
        for(Long id: classIds) listClassIds.add(id);
        List<TimeTablingClass> classes = timeTablingClassRepo.findAllByIdIn(listClassIds);
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(listClassIds);
        Set<Long> groupIds = new HashSet<>();
        for(ClassGroup cg: classGroups) groupIds.add(cg.getGroupId());
        List<Long> listGroupIds = new ArrayList<>();
        for(Long gId: groupIds) listGroupIds.add(gId);
        List<Group> groups = groupRepo.findAllByIdIn(listGroupIds);
        Map<Long, Group> mId2Group = new HashMap<>();
        for(Group g: groups) mId2Group.put(g.getId(),g);
        Map<Long, List<Group>> mClassId2Groups = new HashMap<>();
        for(ClassGroup cg: classGroups){
            Long classId = cg.getClassId();
            Long groupId = cg.getGroupId(); Group g = mId2Group.get(groupId);
            if(mClassId2Groups.get(classId)==null) mClassId2Groups.put(classId, new ArrayList<>());
            mClassId2Groups.get(classId).add(g);
        }
        Map<Long, TimeTablingClass> mId2Class = new HashMap<>();
        for(TimeTablingClass cls: classes) mId2Class.put(cls.getId(),cls);
        Set<Long> refClassIds = new HashSet<>();
        for(TimeTablingClass cls: classes) refClassIds.add(cls.getRefClassId());
        List<PlanGeneralClass> planClasses = planGeneralClassRepository.findAllByIdIn(refClassIds);
        Map<Long, PlanGeneralClass> mId2PlanClass = new HashMap<>();
        for(PlanGeneralClass pcls: planClasses) mId2PlanClass.put(pcls.getId(),pcls);

        for(TimeTablingClassSegment cs: classSegments){
            ModelResponseClassSegment mrcs = new ModelResponseClassSegment();
            TimeTablingClass cls = mId2Class.get(cs.getClassId());
            PlanGeneralClass pcls = mId2PlanClass.get(cls.getRefClassId());
            mrcs.setId(cs.getId());
            mrcs.setClassId(cs.getClassId());
            mrcs.setClassCode(cls.getClassCode());
            mrcs.setCourseCode(cls.getModuleCode());
            mrcs.setCourseName("");
            mrcs.setClassType(cls.getClassType());
            mrcs.setMaxNbStudents(cls.getQuantityMax());
            String groupNames = "";
            if(mClassId2Groups.get(cs.getClassId())!=null){
                for(int i = 0; i < mClassId2Groups.get(cs.getClassId()).size(); i++) {
                    Group g = mClassId2Groups.get(cs.getClassId()).get(i);
                    groupNames = groupNames + g.getGroupName();
                    if(i < mClassId2Groups.get(cs.getClassId()).size()-1) groupNames = groupNames + ",";
                }
            }
            mrcs.setGroupNames(groupNames);
            mrcs.setDuration(cs.getDuration());
            mrcs.setLearningWeeks(cls.getLearningWeeks());
            if(pcls != null) mrcs.setPromotion(pcls.getPromotion());
            if(pcls != null) mrcs.setVolumn(pcls.getMass());
            mrcs.setDay(cs.getWeekday());
            mrcs.setSession(cs.getCrew());
            mrcs.setStartTime(cs.getStartTime());
            mrcs.setEndTime(cs.getEndTime());
            mrcs.setRoomCode(cs.getRoom());
            mrcs.setGroups(mClassId2Groups.get(cs.getClassId()));

            res.add(mrcs);
        }
        return res;
    }

    @Override
    public List<ModelResponseClassSegment> getClasssegmentsOfVersionFiltered(String userId, Long versionId, String searchCourseCode, String searchCourseName, String searchClassCode, String searchGroupName) {
        List<ModelResponseClassSegment> L = getClasssegmentsOfVersion(userId, versionId);
        List<ModelResponseClassSegment> res = new ArrayList<>();
        for(ModelResponseClassSegment cs: L){
            boolean ok = true;
            boolean ok1 = true;
            if(searchClassCode != null && !searchClassCode.equals("")&& !searchClassCode.equals("null")){
                if(!cs.getClassCode().contains(searchClassCode)) ok1 = false;
                else log.info("getClasssegmentsOfVersionFiltered classCode " + cs.getClassCode() + " contains " + searchClassCode + " ok1  " + ok1);
            }
            boolean ok2 = true;
            if(searchCourseCode != null && !searchCourseCode.equals("")&& !searchCourseCode.equals("null")){
                if(!cs.getCourseCode().contains(searchCourseCode)) ok2 = false;
                else log.info("getClasssegmentsOfVersionFiltered, courseCode " + cs.getCourseCode() + " contains " + searchCourseCode + " ok2 = " + ok2);
            }
            boolean ok3 = true;
            if(searchCourseName != null && !searchCourseName.equals("")&& !searchCourseName.equals("null")){
                if(!cs.getCourseName().contains(searchCourseName)) ok3 = false;
                else log.info("getClasssegmentsOfVersionFiltered, courseName " + cs.getCourseName() + " contains " + searchCourseName + " ok3 = " + ok3);
            }
            boolean ok4 = true;
            if(searchGroupName != null && !searchGroupName.equals("")&& !searchGroupName.equals("null")){
                boolean okok = false;
                if(cs.getGroups() != null){
                    for(Group g: cs.getGroups()){
                        if(g.getGroupName().contains(searchGroupName)){
                            okok = true; break;
                        }
                    }
                }
                if(!okok) ok4 = false;
                else log.info("getClasssegmentsOfVersionFiltered groupName contains " + searchGroupName + " ok4 = " + ok4);
            }
            ok = ok1 || ok2 || ok3 || ok4 ;

            if(ok) res.add(cs);
        }
        res.sort(new Comparator<ModelResponseClassSegment>() {
            @Override
            public int compare(ModelResponseClassSegment o1, ModelResponseClassSegment o2) {
                if(o1.getCourseCode().compareTo(o2.getCourseCode()) != 0) return o1.getCourseCode().compareTo(o2.getCourseCode());
                if(o1.getClassCode().compareTo(o2.getClassCode()) != 0) return o1.getClassCode().compareTo(o2.getClassCode());
                return o1.getId().compareTo(o2.getId());
            }
        });
        return res;
    }

    @Override
    public List<ModelResponseClassSegment> getUnscheduledClasssegmentsOfVersionFiltered(String userId, Long versionId, String searchCourseCode, String searchCourseName, String searchClassCode, String searchGroupName) {
        List<ModelResponseClassSegment> L = getClasssegmentsOfVersionFiltered(userId,versionId,searchCourseCode,searchCourseName,searchClassCode,searchGroupName);
        List<ModelResponseClassSegment> res = new ArrayList<>();
        for(ModelResponseClassSegment cs: L){
            if(cs.getStartTime()==null || cs.getDay()==null||cs.getSession()==null){
                res.add(cs);
            }
        }
        return res;
    }

    @Override
    public List<ModelResponseClassWithClassSegmentList> getClassesWithClasssegmentsOfVersionFiltered(String userId, Long versionId, String searchCourseCode, String searchCourseName, String searchClassCode, String searchGroupName) {
        log.info("getClassesWithClasssegmentsOfVersionFiltered, version = " + versionId);
        TimeTablingTimeTableVersion ver = timeTablingVersionRepo.findById(versionId).orElse(null);
        if(ver == null) return null;

        List<ModelResponseClassSegment> L = getClasssegmentsOfVersionFiltered(userId, versionId, searchCourseCode, searchCourseName, searchClassCode, searchGroupName);
        log.info("getClassesWithClasssegmentsOfVersionFiltered, version = " + versionId + " GOT list L = " + L.size());

        List<ModelResponseClassWithClassSegmentList> res = new ArrayList<>();
        Map<Long, ModelResponseClassWithClassSegmentList> mId2Cls = new HashMap<>();
        for(ModelResponseClassSegment cs: L){
            ModelResponseClassWithClassSegmentList cls = mId2Cls.get(cs.getClassId());
            if(cls == null){
                cls = new ModelResponseClassWithClassSegmentList();
                cls.setClassCode(cs.getClassCode());
                cls.setClassId(cs.getClassId());
                cls.setCourseCode(cs.getCourseCode());
                cls.setCourseName(cs.getCourseName());
                cls.setClassSegments(new ArrayList<>());
                cls.setGroupNames(cs.getGroupNames());
                cls.setMaxNbStudents(cs.getMaxNbStudents());
                mId2Cls.put(cs.getClassId(),cls);
                res.add(cls);
            }else{
                //ModelResponseClassWithClassSegmentList cls = mId2Cls.get(cs.getClassId());
            }
            cls.getClassSegments().add(cs);
        }
        for(ModelResponseClassWithClassSegmentList cls: res){
            cls.getClassSegments().sort(new Comparator<ModelResponseClassSegment>() {
                @Override
                public int compare(ModelResponseClassSegment o1, ModelResponseClassSegment o2) {
                    if(o1.getDay()==null) return -1;
                    if(o2.getDay()==null) return 1;
                    if(o1.getDay() > o2.getDay()) return 1;
                    else if(o1.getDay() < o2.getDay()) return -1;
                    if(o1.getSession()==null) return -1;
                    if(o2.getSession()==null) return 1;
                    if(o1.getSession().equals("S") && o2.getSession().equals("C")) return -1;
                    else if(o1.getSession().equals("C") && o2.getSession().equals("S")) return 1;
                    if(o1.getStartTime()==null) return -1;
                    if(o2.getStartTime()==null) return 1;
                    if(o1.getStartTime() > o2.getStartTime()) return 1;
                    else if(o1.getStartTime() < o2.getStartTime()) return -1;
                    return 0;
                }
            });

            // add empty slots to class-segments (used for rendering on grid)
            List<ModelResponseClassSegment> tmp = new ArrayList<>();
            List<Integer> slots = new ArrayList<>();
            for(ModelResponseClassSegment cs: cls.getClassSegments()){
                if(cs.getDay()==null || cs.getRoomCode()==null||cs.getSession()==null) continue;
                int session = 0; if(cs.getSession().equals("C")) session = 1;
                DaySessionSlot dss = new DaySessionSlot(cs.getDay(),session,cs.getStartTime());
                slots.add(dss.hash(ver.getNumberSlotsPerSession()));
            }
            int end = 7*ver.getNumberSlotsPerSession()*2;
            if(slots.size() > 0)  end = slots.get(0) - 1;
                for(int sl = 1; sl <= end; sl++){
                    DaySessionSlot dss = new DaySessionSlot(sl,ver.getNumberSlotsPerSession());
                    ModelResponseClassSegment cs = new ModelResponseClassSegment(dss.day,
                            (dss.session == 0 ? "S" : "C"), dss.slot,"white");
                    tmp.add(cs);
                    //log.info("INIT -> add " + cs.getDay() + "-" + cs.getSession() + "-" + cs.getStartTime());
                }


            for(int i = 0; i  < slots.size(); i++){
                //log.info("cls " + cls.getClassCode() + " slots[" + i + "] = " + slots.get(i));
                ModelResponseClassSegment csr = cls.getClassSegments().get(i);
                csr.setColor("yellow");
                tmp.add(csr);
                //log.info("cls " + cls.getClassCode() + " slots[" + i + "] = " + slots.get(i) + " add real cs " + csr.getDay() + "-" + csr.getSession()+"-" + csr.getStartTime() + " duration " + csr.getDuration());
                int st = slots.get(i) + csr.getDuration();
                int fn = 7*ver.getNumberSlotsPerSession()*2;
                if(i < slots.size()-1) fn = slots.get(i+1)-1;
                //log.info("cls " + cls.getClassCode() + " slots[" + i + "] = " + slots.get(i) + " st = " + st + " fn = " + fn);
                for(int sl = st; sl <= fn; sl++){
                    DaySessionSlot dss = new DaySessionSlot(sl,ver.getNumberSlotsPerSession());
                    ModelResponseClassSegment cs = new ModelResponseClassSegment(dss.day,
                            (dss.session == 0 ? "S" : "C"), dss.slot,"white");
                    tmp.add(cs);
                    //log.info("cls " + cls.getClassCode() + " slots[" + i + "] = " + slots.get(i) + " st = " + st + " fn = " + fn + " -> add " + cs.getDay() + "-" + cs.getSession() + "-" + cs.getStartTime());

                }
            }
            cls.setClassSegments(tmp);
            //log.info("getClassesWithClasssegmentsOfVersionFiltered, class-segments length = " + cls.getClassSegments().size());

            for(ModelResponseClassSegment cs: cls.getClassSegments()){
                if(cs.getSession().equals("C")){
                    cs.setStartTime(cs.getStartTime()+ ver.getNumberSlotsPerSession());
                }
            }
        }
        //log.info("getClassesWithClasssegmentsOfVersionFiltered, L = " + L.size() + " res = "  + res.size());



        //return res.subList(0,50);
        return res;
    }

    @Override
    public List<ModelResponseClassWithClassSegmentList> getClassesWithClasssegmentsApprovedOfSemesterFiltered(String userId, String semester, String searchCourseCode, String searchCourseName, String searchClassCode, String searchGroupName) {
        List<ModelResponseClassWithClassSegmentList> res= new ArrayList<>();
        List<TimeTablingBatch> batches = timeTablingBatchRepo.findAllBySemester(semester);
        if(batches != null){
            log.info("getClassesWithClasssegmentsApprovedOfSemesterFiltered batches = " + batches.size());
            for(TimeTablingBatch b: batches){
                List<TimeTablingTimeTableVersion> vers = timeTablingVersionRepo.findAllByStatusAndBatchId(TimeTablingTimeTableVersion.STATUS_PUBLISHED,b.getId());
                log.info("getClassesWithClasssegmentsApprovedOfSemesterFiltered batch " + b.getId() + " vers = " + vers.size());
                for(TimeTablingTimeTableVersion ver: vers) {
                    List<ModelResponseClassWithClassSegmentList> L = getClassesWithClasssegmentsOfVersionFiltered(userId, ver.getId(), searchCourseCode, searchCourseName, searchClassCode, searchGroupName);
                    log.info("getClassesWithClasssegmentsApprovedOfSemesterFiltered batch " + b.getId() + " ver = " + ver.getId() + " L = " + L.size());
                    for(ModelResponseClassWithClassSegmentList cls: L){
                        res.add(cls);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public List<ModelResponseRoomBasedTimetable> getRoomBasedTimetable(String userId, Long versionId, String searchRoomCode) {
        TimeTablingTimeTableVersion ver = timeTablingVersionRepo.findById(versionId).orElse(null);
        if(ver == null) return null;
        List<Classroom> allrooms = classroomRepo.findAllByStatus(Classroom.STATUS_ACTIVE);
        Map<String, Classroom> mCode2Room= new HashMap<>();
        for(Classroom r: allrooms) mCode2Room.put(r.getClassroom(),r);

        List<ModelResponseClassSegment> L = getClasssegmentsOfVersionFiltered(userId, versionId, null, null, null, null);
        List<ModelResponseRoomBasedTimetable> res = new ArrayList<>();
        Map<String, List<ModelResponseClassSegment>> mRoom2ClassSegments = new HashMap<>();
        for(ModelResponseClassSegment cs:L){
            String roomCode= cs.getRoomCode();
            if(roomCode == null || roomCode.equals("null")) continue;
            
            if(mRoom2ClassSegments.get(roomCode)==null)
                mRoom2ClassSegments.put(roomCode, new ArrayList<>());
            mRoom2ClassSegments.get(roomCode).add(cs);
        }

        for(String roomCode:mRoom2ClassSegments.keySet()){
            if(searchRoomCode!=null&&!searchRoomCode.equals("")){
                if(!roomCode.contains(searchRoomCode)){
                    log.info("getRoomBasedTimetable, filtering room " + roomCode + " searchRoomCode = " + searchRoomCode + " not contain ->continue");
                    continue;
                }
            }
            List<ModelResponseClassSegment> CS = mRoom2ClassSegments.get(roomCode);
            CS.sort(new Comparator<>() {
                @Override
                public int compare(ModelResponseClassSegment o1, ModelResponseClassSegment o2) {

                    if (o1.getDay() == null) return -1;
                    if (o2.getDay() == null) return 1;
                    if (o1.getDay() > o2.getDay()) return 1;
                    else if (o1.getDay() < o2.getDay()) return -1;
                    if (o1.getSession() == null) return -1;
                    if (o2.getSession() == null) return 1;
                    if (o1.getSession().equals("S") && o2.getSession().equals("C")) return -1;
                    else if (o1.getSession().equals("C") && o2.getSession().equals("S")) return 1;
                    if (o1.getStartTime() == null) return -1;
                    if (o2.getStartTime() == null) return 1;
                    if (o1.getStartTime() > o2.getStartTime()) return 1;
                    else if (o1.getStartTime() < o2.getStartTime()) return -1;
                    return 0;
                }
            });
            // merge overlap time-periods
            List<ModelResponseTimetableClass> newL= new ArrayList<>();
            int i = 0;
            while(i < CS.size()) {
                ModelResponseClassSegment csi = CS.get(i);
                String classCodes= csi.getClassCode();
                int endTime = -1;
                if(csi.getStartTime()!=null)
                    endTime = csi.getStartTime() + csi.getDuration()-1;
                int j = i + 1;
                while (j < CS.size()) {
                    ModelResponseClassSegment csj = CS.get(j);
                    if(csi.getDay()==null||csj.getDay()==null) break;
                    if(!csi.getDay().equals(csj.getDay())) break;
                    if(csi.getSession()==null|| csj.getSession()==null) break;
                    if(!csj.getSession().equals(csi.getSession())) break;
                    if(csj.getStartTime()==null|| csi.getStartTime()==null) break;
                    if(csj.getStartTime()>= csi.getStartTime()+csi.getDuration()) break;// not overlap
                    classCodes = classCodes + ","+ csj.getClassCode();
                    endTime = csj.getStartTime() + csj.getDuration()-1;
                    j = j + 1;
                }
                ModelResponseTimetableClass ncs = new ModelResponseTimetableClass();
                ncs.setDay(csi.getDay());
                ncs.setSession(csi.getSession());
                if(csi.getStartTime()!=null) {
                    ncs.setStartTime(csi.getStartTime());
                    ncs.setEndTime(endTime);
                    ncs.setDuration(endTime - csi.getStartTime() + 1);
                    ncs.setDay(csi.getDay());
                    ncs.setSession(csi.getSession());
                    ncs.setClassCodes(classCodes);
                }
                newL.add(ncs);
                i = j;
            }

            // add empty slot for rendering grid timetable
            List<ModelResponseTimetableClass> tmp = new ArrayList<>();
            List<Integer> slots = new ArrayList<>();
            for(ModelResponseTimetableClass ttc: newL){
                if(ttc.getDay()==null || ttc.getSession()==null|| ttc.getStartTime()==null) continue;

                int session = 0;
                if(ttc.getSession().equals("C")) session = 1;
                DaySessionSlot dss = new DaySessionSlot(ttc.getDay(),session,ttc.getStartTime());
                slots.add(dss.hash(ver.getNumberSlotsPerSession()));
            }
            int end = 7*ver.getNumberSlotsPerSession()*2;
            if(slots.size() > 0)  end = slots.get(0) - 1;
            for(int sl = 1; sl <= end; sl++){
                DaySessionSlot dss = new DaySessionSlot(sl,ver.getNumberSlotsPerSession());
                ModelResponseTimetableClass ttc = new ModelResponseTimetableClass(dss.day,
                        (dss.session == 0 ? "S" : "C"), dss.slot,dss.slot,1,"","white");
                tmp.add(ttc);
                //log.info("INIT -> add " + ttc.getDay() + "-" + ttc.getSession() + "-" + ttc.getStartTime());
            }


            for(int j = 0; j  < slots.size(); j++){
                //log.info("room " + roomCode + " slots[" + j + "] = " + slots.get(j));
                ModelResponseTimetableClass ttc = newL.get(j);
                ttc.setColor("yellow");
                tmp.add(ttc);
                //log.info("room " + roomCode + " slots[" + j + "] = " + slots.get(j) + " add real cs " + ttc.getDay() + "-" + ttc.getSession()+"-" + ttc.getStartTime() + " duration " + ttc.getDuration());
                int st = slots.get(j) + ttc.getDuration();
                int fn = 7*ver.getNumberSlotsPerSession()*2;
                if(j < slots.size()-1) fn = slots.get(j+1)-1;
                //log.info("room " + roomCode + " slots[" + j + "] = " + slots.get(j) + " st = " + st + " fn = " + fn);
                for(int sl = st; sl <= fn; sl++){
                    DaySessionSlot dss = new DaySessionSlot(sl,ver.getNumberSlotsPerSession());
                    ModelResponseTimetableClass ttci = new ModelResponseTimetableClass(dss.day,
                            (dss.session == 0 ? "S" : "C"), dss.slot,dss.slot,1,"","white");
                    tmp.add(ttci);
                    //log.info("room " + roomCode + " slots[" + j + "] = " + slots.get(j) + " st = " + st + " fn = " + fn + " -> add " + ttci.getDay() + "-" + ttci.getSession() + "-" + ttci.getStartTime()  + "-" + ttci.getDuration());

                }
            }


            ModelResponseRoomBasedTimetable resItem = new ModelResponseRoomBasedTimetable();
            resItem.setRoomCode(roomCode);
            //resItem.setClasses(newL);
            resItem.setClasses(tmp);

            res.add(resItem);

            //if(res.size() > 1) break;
        }

        // serialize startTime on th whole day: if session = C then startTime= startTime + ver.getNumberSlotsPerSession()
        Set<String> roomUsed = new HashSet();
        for(ModelResponseRoomBasedTimetable i: res){
            Classroom cr = mCode2Room.get(i.getRoomCode());
            if(cr != null){
                long c = cr.getQuantityMax();
                i.setCapacity((int)c);
                roomUsed.add(cr.getClassroom());
            }
            for(ModelResponseTimetableClass ttc:i.getClasses()){
                if(ttc.getSession().equals("C")){
                    ttc.setStartTime(ttc.getStartTime() + ver.getNumberSlotsPerSession());
                }
            }
        }
        return res;
    }

    @Override
    public List<ModelResponseRoomBasedTimetable> getRoomBasedTimetableApprovedOfSemester(String userId, String semester, String searchRoomCode) {
        List<ModelResponseRoomBasedTimetable> res = new ArrayList<>();
        List<TimeTablingBatch> batches = timeTablingBatchRepo.findAllBySemester(semester);
        if(batches != null){
            log.info("getRoomBasedTimetableApprovedOfSemester batches = " + batches.size());
            for(TimeTablingBatch b: batches){
                List<TimeTablingTimeTableVersion> vers = timeTablingVersionRepo.findAllByStatusAndBatchId(TimeTablingTimeTableVersion.STATUS_PUBLISHED,b.getId());
                log.info("getRoomBasedTimetableApprovedOfSemester batch " + b.getId() + " vers = " + vers.size());
                for(TimeTablingTimeTableVersion ver: vers) {
                    List<ModelResponseRoomBasedTimetable> L = getRoomBasedTimetable(userId, ver.getId(), searchRoomCode);
                    log.info("getRoomBasedTimetableApprovedOfSemester batch " + b.getId() + " ver = " + ver.getId() + " L = " + L.size());
                    for(ModelResponseRoomBasedTimetable cls: L){
                        res.add(cls);
                    }
                }
            }
        }
        Set<String> roomUsed = new HashSet();
        for(ModelResponseRoomBasedTimetable i: res){
            roomUsed.add(i.getRoomCode());
        }
        List<Classroom> allrooms = classroomRepo.findAllByStatus(Classroom.STATUS_ACTIVE);
        Map<String, Classroom> mCode2Room= new HashMap<>();
        for(Classroom r: allrooms) mCode2Room.put(r.getClassroom(),r);

        for(Classroom r: allrooms){
            if(!roomUsed.contains(r.getClassroom())){
                ModelResponseRoomBasedTimetable e = new ModelResponseRoomBasedTimetable();
                e.setRoomCode(r.getClassroom());
                long c =r.getQuantityMax();
                e.setCapacity((int)c);
                e.setClasses(new ArrayList<>());
                res.add(e);
            }
        }

        return res;
    }

    @Override
    public List<ModelResponseTimeTablingClass> getTimeTablingClassOfBatch(String userId, Long batchId) {
        List<TimeTablingClass> CLS = timeTablingClassRepo.findAllByBatchId(batchId);


        return CLS.stream() // use sorted list
                .map(c -> ModelResponseTimeTablingClass.builder()
                        .id(c.getId())
                        .quantity(c.getQuantity())
                        .quantityMax(c.getQuantityMax())
                        .moduleCode(c.getModuleCode())
                        .moduleName(c.getModuleName())
                        .classType(c.getClassType())
                        .classCode(c.getClassCode())
                        .semester(c.getSemester())
                        .studyClass(c.getStudyClass())
                        .mass(c.getMass())
                        .state(c.getState())
                        .crew(c.getCrew())
                        .openBatch(c.getOpenBatch())
                        .course(c.getCourse())
                        .refClassId(c.getRefClassId())
                        .parentClassId(c.getParentClassId())
                        .duration(c.getDuration())
                        .groupName(c.getGroupName())
                        .listGroupName(null)
                        .timeSlots(null)
                        .learningWeeks(c.getLearningWeeks())
                        .foreignLecturer(c.getForeignLecturer())
                        .build()
                ).toList();
    }

    @Override
    public List<ModelResponseTimeTablingClass> advancedFilter(ModelInputAdvancedFilter I) {
        TimeTablingTimeTableVersion ver = timeTablingVersionRepo.findById(I.getVersionId()).orElse(null);
        if(ver == null) return null;
        String semester = ver.getSemester();
        List<ModelResponseTimeTablingClass> L = getTimeTablingClassDtos(semester,I.getGroupId(),I.getVersionId());
        log.info("advancedFilter, minQty = " + I.getFilterMinQty() + " maxQty = " +
                I.getFilterMaxQty() + " courseCodes = " + I.getFilterCourseCodes() +
                " Class Types = " + I.getFilterClassTypes() + " version = " +
                I.getVersionId() + " L.sz = " + L.size());
        String[] courseCodes = null;
        if(I.getFilterCourseCodes()!=null && !I.getFilterCourseCodes().equals(""))
            courseCodes = I.getFilterCourseCodes().split(";");
        String[] classTypes = null;
        if(I.getFilterClassTypes()!=null && !I.getFilterClassTypes().equals(""))
            classTypes = I.getFilterClassTypes().split(";");

        List<ModelResponseTimeTablingClass> res = new ArrayList<>();
        int minQty = 0;
        try{
            minQty = Integer.valueOf(I.getFilterMinQty());
        }catch (Exception e){ e.printStackTrace();}
        int maxQty = 100000000;
        try{
            maxQty = Integer.valueOf(I.getFilterMaxQty());
        }catch (Exception e){ e.printStackTrace();}
        for(ModelResponseTimeTablingClass cls: L){
            boolean ok = true;
            // Check quantityMax null safety
            Integer classQuantityMax = cls.getQuantityMax();
            if(classQuantityMax == null) {
                // Skip classes with null quantityMax, or treat as 0
                log.info("advancedFilter, skipping class " + cls.getClassCode() + " with null quantityMax");
                continue;
            }
            
            if(classQuantityMax < minQty || classQuantityMax > maxQty) ok = false;
            if(!ok) continue;
            if(courseCodes != null){
                ok = false;
                log.info("advancedFilter, consider cls course " + cls.getModuleCode());
                for(String c: courseCodes){
                    log.info("advancedFilter, consider cls course " + cls.getModuleCode() + " c.trim = "+ c.trim());
                    if(cls.getModuleCode().contains(c.trim())){
                        ok = true; break;
                    }
                }
            }
            if(!ok) continue;
            if(classTypes!=null){
                ok = false;
                for(String ct: classTypes){
                    log.info("advancedFilter, consider cls course " + cls.getModuleCode() + " class type trim = " + ct.trim());
                    if(cls.getClassType().contains(ct.trim())){
                        ok = true; break;
                    }
                }
            }
            if(!ok) continue;
            if(ok){
                res.add(cls);
                log.info("advancedFilter, qty = " + classQuantityMax + " minQty = " +
                        minQty + " maxQty = " + maxQty + " add " + cls.str() + " res.size = " + res.size());
            }
        }
        return res;

    }

    @Override
    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(List<Long> classIds, Long versionId) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllByIdIn(classIds);
        log.info("getTimeTablingClassDtos(List<Long>), classIds.size = " + classIds.size() + " found classes = " + cls.size());
        return getDetailTimeTablingClassesFrom(cls,versionId);
    }

    private List<ModelResponseTimeTablingClass> getDetailTimeTablingClassesFrom(List<TimeTablingClass> cls, Long versionId){
        Map<Long, List<Long>> mClassId2GroupIds = new HashMap<>();
        List<Long> classIds = cls.stream().map(c -> c.getId()).toList();
        List<ClassGroup> CG = classGroupRepo.findAllByClassIdIn(classIds);
        for(ClassGroup cg: CG){
            Long classId = cg.getClassId();
            if(mClassId2GroupIds.get(classId)==null)
                mClassId2GroupIds.put(classId, new ArrayList<>());
            mClassId2GroupIds.get(classId).add(cg.getGroupId());
        }

        Map<Long, List<String>> mClassId2GroupNames = new HashMap<>();
        List<Group> groups = groupRepo.findAll();
        Map<Long, Group> mId2Group = new HashMap<>();
        for(Group g: groups) mId2Group.put(g.getId(),g);
        for(TimeTablingClass c: cls){
            List<Long> gids = mClassId2GroupIds.get(c.getId());
            mClassId2GroupNames.put(c.getId(), new ArrayList<>());
            if(gids != null) for(Long gid: gids){
                String gname = mId2Group.get(gid).getGroupName();
                mClassId2GroupNames.get(c.getId()).add(gname);
            }
        }
        //List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdIn(classIds);
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByVersionIdAndClassIdIn(versionId, classIds);

        Map<Long, List<TimeTablingClassSegment>> mClassId2ClassSegments = new HashMap<>();
        for(TimeTablingClassSegment cs: classSegments){
            if(mClassId2ClassSegments.get(cs.getClassId())==null)
                mClassId2ClassSegments.put(cs.getClassId(),new ArrayList<>());
            mClassId2ClassSegments.get(cs.getClassId()).add(cs);
        }
        return cls.stream()
                .map(c -> ModelResponseTimeTablingClass.builder()
                        .id(c.getId())
                        .quantity(c.getQuantity())
                        .quantityMax(c.getQuantityMax())
                        .moduleCode(c.getModuleCode())
                        .moduleName(c.getModuleName())
                        .classType(c.getClassType())
                        .classCode(c.getClassCode())
                        .semester(c.getSemester())
                        .studyClass(c.getStudyClass())
                        .mass(c.getMass())
                        .state(c.getState())
                        .crew(c.getCrew())
                        .openBatch(c.getOpenBatch())
                        .course(c.getCourse())
                        .refClassId(c.getRefClassId())
                        .parentClassId(c.getParentClassId())
                        .duration(c.getDuration())
                        .groupName(c.getGroupName())
                        .listGroupName(mClassId2GroupNames.get(c.getId()))
                        .timeSlots(mClassId2ClassSegments.get(c.getId()))
                        .learningWeeks(c.getLearningWeeks())
                        .foreignLecturer(c.getForeignLecturer())
                        .build()
                ).toList();

    }
    @Override
    public List<ModelResponseTimeTablingClass> findAll() {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAll();
        return getDetailTimeTablingClassesFrom(cls,null);
    }

    @Override
    public List<ModelResponseTimeTablingClass> findAllBySemester(String semester) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(semester);
        return getDetailTimeTablingClassesFrom(cls,null);
    }

    @Override
    public List<ModelResponseTimeTablingClass> findAllByClassIdIn(List<Long> classIds) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllByIdIn(classIds);
        List<ModelResponseTimeTablingClass> res = getDetailTimeTablingClassesFrom(cls,null);
        return res;
    }

    @Override
    public int removeClassSegment(CreateClassSegmentRequest I) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(I.getSemester());
        List<Long> classIds = cls.stream().map(c -> c.getId()).toList();
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdInAndVersionId(classIds, I.getVersionId());
        int cnt = 0;
        for(TimeTablingClassSegment cs: classSegments){
            timeTablingClassSegmentRepo.delete(cs);
            cnt ++;
            log.info("removeClassSegment, removed " + cnt + "th class-segment " + cs.getId());

        }
        return cnt;
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        // FIRST delete class-segment
        List<TimeTablingClassSegment> CS = timeTablingClassSegmentRepo.findAllByClassIdIn(ids);
        List<Long> classSegmentIds = CS.stream().map(cs -> cs.getId()).toList();
        timeTablingClassSegmentRepo.deleteAllById(classSegmentIds);
        List<Long> childrenIds = new ArrayList<>();
        for(Long id: ids) {
            List<TimeTablingClass> childrenClass = timeTablingClassRepo.findAllByParentClassId(id);
            for(TimeTablingClass c: childrenClass) childrenIds.add(c.getId());
        }

        // SECOND delete children classes (e.g., class BT)
        timeTablingClassRepo.deleteAllById(childrenIds);

        // FINAL delete classes by ids
        timeTablingClassRepo.deleteAllById(ids);
        return 0;
    }

    @Transactional
    @Override
    public TimeTablingClass updateClass(UpdateGeneralClassRequest r) {
        log.info("updateClass, classId = " + r.getGeneralClass().getId());
        TimeTablingClass cls = timeTablingClassRepo
                .findById(r.getGeneralClass().getId()).orElse(null);
        if(cls == null) return null;

        GeneralClass gc = r.getGeneralClass();
        if(!gc.getCrew().equals(cls.getCrew())){
            // update class-segments corresponding to the current cls
            List<TimeTablingClassSegment> CS = timeTablingClassSegmentRepo.findAllByClassId(cls.getId());
            for(TimeTablingClassSegment cs: CS){
                cs.setCrew(gc.getCrew());
            }
            timeTablingClassSegmentRepo.saveAll(CS);
        }

        cls.setCrew(gc.getCrew());
        cls.setDuration(gc.getDuration());
        cls.setClassCode(gc.getClassCode());
        cls.setCourse(gc.getCourse());
        cls.setClassType(gc.getClassType());
        cls.setForeignLecturer(gc.getForeignLecturer());
        cls.setGroupName(gc.getGroupName());
        cls.setLearningWeeks(gc.getLearningWeeks());
        cls.setMass(gc.getMass());
        cls.setModuleCode(gc.getModuleCode());

        cls.setModuleName(gc.getModuleName());
        cls.setOpenBatch(gc.getOpenBatch());
        cls.setParentClassId(gc.getParentClassId());
        cls.setQuantity(gc.getQuantity());
        cls.setQuantityMax(gc.getQuantityMax());
        cls.setRefClassId(gc.getRefClassId());
        cls.setSemester(gc.getSemester());
        cls.setState(gc.getState());
        cls.setStudyClass(gc.getStudyClass());

        cls = timeTablingClassRepo.save(cls);
        log.info("updateClass, saved TimeTablingClass, new learning weeks = " + cls.getLearningWeeks());
        return cls;
    }

    @Override
    public List<ModelResponseTimeTablingClass> getSubClass(Long id) {
        List<TimeTablingClass> L = timeTablingClassRepo.findAllByParentClassId(id);
        List<ModelResponseTimeTablingClass> res = getDetailTimeTablingClassesFrom(L,null);
        return res;
    }

    @Override
    public String clearTimeTable(List<Long> ids) {
        try {
            // Find segments directly by their IDs (these are roomReservationId values)
            List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllById(ids);
            log.info("clearTimeTable, clearing schedule information for " + ids.size() + " segment IDs, found " + classSegments.size() + " segments");
            
            if (classSegments.isEmpty()) {
                return "Không tìm thấy phân đoạn lớp học với các ID đã cung cấp";
            }
            
            for(TimeTablingClassSegment cs: classSegments){
                cs.setRoom(null);
                cs.setEndTime(null);
                cs.setStartTime(null);
                cs.setWeekday(null);
                timeTablingClassSegmentRepo.save(cs);
                log.info("clearTimeTable: Reset segment " + cs.getId() + " of class " + cs.getClassId() + " with versionId=" + cs.getVersionId());
            }
            
            return "ok";
        } catch (Exception e) {
            log.error("Error in clearTimeTable: ", e);
            return "Lỗi khi xóa thời khóa biểu: " + e.getMessage();
        }
    }

    @Override
    public List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndex(String semester, int weekIndex) {
        List<RoomOccupationWithModuleCode> res = new ArrayList<>();
        List<ModelResponseTimeTablingClass> classes = findAllBySemester(semester);
        log.info("getRoomOccupationsBySemesterAndWeekIndex, semester = " + semester + " weekIndex = " + weekIndex + " classes.sz = " + classes.size());

        for(ModelResponseTimeTablingClass c: classes){

            //System.out.println(c);
            List<Integer> learningWeeks = TimeTablingClass.extractLearningWeeks(c.getLearningWeeks());
            //log.info("getRoomOccupationsBySemesterAndWeekIndex, learningWeeks = " + learningWeeks);
            if(!learningWeeks.contains(weekIndex)) continue;
            for(TimeTablingClassSegment cs: c.getTimeSlots()){
                RoomOccupationWithModuleCode ro = new RoomOccupationWithModuleCode();
                ro.setSemester(semester);
                ro.setClassRoom(cs.getRoom());
                ro.setWeekIndex(weekIndex);
                ro.setDayIndex(cs.getWeekday());
                ro.setClassCode(c.getClassCode());
                ro.setEndPeriod(cs.getEndTime());
                ro.setStartPeriod(cs.getStartTime());
                ro.setModuleCode(c.getModuleCode());
                ro.setCrew(c.getCrew());

                //log.info("getRoomOccupationsBySemesterAndWeekIndex, add a ro room " + ro.getClassRoom() + ", start " + ro.getStartPeriod() + " end " + ro.getEndPeriod());

                res.add(ro);
            }
        }
        return res;

    }

    @Override 
    public List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndexAndVersionId(String semester, int weekIndex, Long versionId) {
        List<RoomOccupationWithModuleCode> res = new ArrayList<>();
        log.info("getRoomOccupationsBySemesterAndWeekIndexAndVersionId, semester = {}, weekIndex = {}, versionId = {}", 
                semester, weekIndex, versionId);
        try {
            List<TimeTablingClass> classes = timeTablingClassRepo.findAllBySemester(semester);
            if (classes.isEmpty()) {
                log.info("No classes found for semester {}", semester);
                return res;
            }
            log.info("Found {} classes for semester {}", classes.size(), semester);
            List<TimeTablingClass> classesInWeek = classes.stream()
                    .filter(c -> {
                        List<Integer> learningWeeks = TimeTablingClass.extractLearningWeeks(c.getLearningWeeks());
                        return learningWeeks.contains(weekIndex);
                    })
                    .collect(Collectors.toList());
            if (classesInWeek.isEmpty()) {
                log.info("No classes found for week {} in semester {}", weekIndex, semester);
                return res;
            }
            log.info("Found {} classes for week {} in semester {}", classesInWeek.size(), weekIndex, semester);
            List<Long> classIds = classesInWeek.stream()
                    .map(TimeTablingClass::getId)
                    .collect(Collectors.toList());
            List<TimeTablingClassSegment> segments;
            if (versionId != null) {
                segments = timeTablingClassSegmentRepo.findAllByClassIdInAndVersionId(classIds, versionId);
                log.info("Found {} segments with versionId={} for classes in week {}", segments.size(), versionId, weekIndex);
            } else {
                segments = timeTablingClassSegmentRepo.findAllByClassIdInAndVersionIdIsNull(classIds);
                log.info("Found {} segments with NULL versionId for classes in week {}", segments.size(), weekIndex);
            }
            List<TimeTablingClassSegment> assignedSegments = segments.stream()
                    .filter(s -> s.getRoom() != null && 
                                s.getWeekday() != null && 
                                s.getStartTime() != null && 
                                s.getEndTime() != null)
                    .collect(Collectors.toList());
            log.info("Found {} assigned segments", assignedSegments.size());
            Map<Long, TimeTablingClass> classIdToClass = classesInWeek.stream()
                    .collect(Collectors.toMap(TimeTablingClass::getId, Function.identity()));
            for (TimeTablingClassSegment segment : assignedSegments) {
                TimeTablingClass cls = classIdToClass.get(segment.getClassId());
                if (cls == null) continue; 
                
                RoomOccupationWithModuleCode occupation = new RoomOccupationWithModuleCode();
                occupation.setSemester(semester);
                occupation.setClassRoom(segment.getRoom());
                occupation.setWeekIndex(weekIndex);
                occupation.setDayIndex(segment.getWeekday());
                occupation.setClassCode(cls.getClassCode());
                occupation.setEndPeriod(segment.getEndTime());
                occupation.setStartPeriod(segment.getStartTime());
                occupation.setModuleCode(cls.getModuleCode());
                occupation.setCrew(cls.getCrew());
                
                res.add(occupation);
            }
            
            return res;
        } catch (Exception e) {
            log.error("Error getting room occupations: ", e);
            return res;
        }
    }


    @Override
    public ModelResponseTimeTablingClass splitNewClassSegment(Long classId, Long parentClassSegmentId, Integer duration, Long versionId) {
        List<Long> ids = new ArrayList<>();
        ids.add(classId);
        List<ModelResponseTimeTablingClass> L = findAllByClassIdIn(ids);
        if(L == null || L.size() == 0){
            throw new NotFoundException("Không tìm thấy lớp id " + classId);
            //return null;
        }
        ModelResponseTimeTablingClass res = L.get(0);
        TimeTablingClassSegment cs = timeTablingClassSegmentRepo.findById(parentClassSegmentId).orElse(null);
        if(cs == null){
            throw new NotFoundException("Không tìm thấy class segment " + parentClassSegmentId);
        }
        if(cs.getDuration() <= duration){
            throw new NotFoundException("Số tiết ca được tạo mới (" + duration + ") phải nhỏ hơn số tiết ca cha (" + cs.getDuration()+ ") !");

        }
        cs.setDuration(cs.getDuration() - duration);
        TimeTablingClassSegment ncs = new TimeTablingClassSegment();
        ncs.setDuration(duration);
        ncs.setCrew(cs.getCrew());
        ncs.setClassId(cs.getClassId());
        ncs.setParentId(parentClassSegmentId);
        ncs.setVersionId(cs.getVersionId());
        Long nextId = timeTablingClassSegmentRepo.getNextReferenceValue();
        ncs.setId(nextId);

        cs = timeTablingClassSegmentRepo.save(cs);
        ncs = timeTablingClassSegmentRepo.save(ncs);
        res.getTimeSlots().add(ncs);
        return res;
    }

    @Override
    public int computeClassCluster(ModelInputComputeClassCluster I) {
        //List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(I.getSemester());
        TimeTablingBatch batch = timeTablingBatchRepo.findById(I.getBatchId()).orElse(null);
        if(batch==null) return 0;
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllByBatchId(I.getBatchId());
        List<Long> classIds = new ArrayList<>();
        for(TimeTablingClass c: cls) classIds.add(c.getId());
        List<ClassGroup> classGroups = classGroupRepo.findAllByClassIdIn(classIds);
        List<Group> groups = groupRepo.findAll();
        Map<Long, Group> mId2Group = new HashMap<>();
        for(Group g: groups) mId2Group.put(g.getId(),g);
        Map<Long, List<Long>> mClassId2GroupIds = new HashMap<>();
        for(ClassGroup cg: classGroups){
            if(mClassId2GroupIds.get(cg.getClassId())==null)
                mClassId2GroupIds.put(cg.getClassId(),new ArrayList<>());
            mClassId2GroupIds.get(cg.getClassId()).add(cg.getGroupId());
        }
        ConnectedComponentClassSolver solver = new ConnectedComponentClassSolver();
        List<List<TimeTablingClass>> clusters = solver.computeConnectedComponents(cls,classGroups);
        log.info("computeClassCluster, number of clusters = " + clusters.size());

        //List<Cluster> oldClusters = clusterRepo.findAllBySemester(I.getSemester());
        List<Cluster> oldClusters = clusterRepo.findAllByBatchId(I.getBatchId());

        log.info("computeClassCluster, oldClusters.sz = " + oldClusters.size());
        for(Cluster c: oldClusters){
            List<ClusterClass> clusterClasses = clusterClassRepo.findAllByClusterId(c.getId());
            for(ClusterClass cc: clusterClasses){
                clusterClassRepo.delete(cc);
            }
            clusterRepo.delete(c);
            log.info("computeClassCluster, delete OK cluster " + c.getId() + " name = " + c.getName());
        }

        for(List<TimeTablingClass> cluster : clusters){
            // create a new cluster
            String clusterName = "";
            Set<String> names = new HashSet<>();
            for(TimeTablingClass gc: cluster){
                Long classId = gc.getId();
                List<Long> gids = mClassId2GroupIds.get(classId);
                for(Long gId: gids){
                    Group g = mId2Group.get(gId);
                    names.add(g.getGroupName());
                }
            }
            int cnt = 0;
            for(String n: names){
                cnt++;
                clusterName = clusterName + n;
                if(cnt < names.size()) clusterName = clusterName + ", ";
            }
            Long nextId = planGeneralClassRepository.getNextReferenceValue();
            Cluster newCluster = new Cluster();
            newCluster.setId(nextId);
            newCluster.setName(clusterName);
            newCluster.setSemester(batch.getSemester());
            newCluster.setBatchId(I.getBatchId());
            newCluster = clusterRepo.save(newCluster);
            log.info("computeClassCluster, saved cluster " + newCluster.getId() + " name = " + newCluster.getName());

            for(TimeTablingClass gc: cluster){
                ClusterClass clusterClass = new ClusterClass();
                clusterClass.setClusterId(nextId);
                clusterClass.setClassId(gc.getId());

                clusterClass = clusterClassRepo.save(clusterClass);
                log.info("computeClassCluster, saved clusterClass " + clusterClass.getClusterId() + " classId " + clusterClass.getClassId());

            }
        }

        return clusters.size();
    }

    @Override
    public List<ModelResponseTimeTablingClass> getClassByCluster(Long clusterId) {
        List<ClusterClass> clusterClasses = clusterClassRepo.findAllByClusterId(clusterId);

        if (clusterClasses.isEmpty()) {
            return new ArrayList<>();
        }
        log.info("getClassByCluster(" + clusterId + "), clusterClasses.sz = " + clusterClasses.size());


        // Extract class IDs from the relationships
        List<Long> classIds = clusterClasses.stream()
                .map(ClusterClass::getClassId)
                .collect(Collectors.toList());

        List<ModelResponseTimeTablingClass> cls = findAllByClassIdIn(classIds);
        log.info("getClassByCluster(" + clusterId + "), res.sz = " + cls.size());
        return cls;
    }
    @Override
    public List<ModelResponseTimeTablingClass> getClassByCluster(Long clusterId, Long versionId) {
        List<ClusterClass> clusterClasses = clusterClassRepo.findAllByClusterId(clusterId);

        if (clusterClasses.isEmpty()) {
            log.info("getClassByCluster(" + clusterId + ", " + versionId + "): No classes found in cluster");
            return new ArrayList<>();
        }
        log.info("getClassByCluster(" + clusterId + ", " + versionId + "), clusterClasses.sz = " + clusterClasses.size());

        // Extract class IDs from the relationships
        List<Long> classIds = clusterClasses.stream()
                .map(ClusterClass::getClassId)
                .collect(Collectors.toList());

        // Use the existing method that supports versionId parameter
        List<ModelResponseTimeTablingClass> cls = getTimeTablingClassDtos(classIds, versionId);
        log.info("getClassByCluster(" + clusterId + ", " + versionId + "), res.sz = " + cls.size());
        return cls;
    }

    @Override
    public boolean updateTimeTableClassSegment(String semester, List<V2UpdateClassScheduleRequest> saveRequests) {
        for(V2UpdateClassScheduleRequest r: saveRequests){
            TimeTablingClassSegment ttcs = timeTablingClassSegmentRepo.findById(r.getRoomReservationId()).orElse(null);
            if(ttcs == null){
                log.info("updateTimeTableClassSegment, do not find class segment id " + r.getRoomReservationId());
                return false;
            }
            log.info("updateTimeTableClassSegment consider class segment id = " + r.getRoomReservationId() + " start = " + r.getStartTime() + " end = " + r.getEndTime() + " room = " + r.getRoom());
            TimeTablingClass cls = timeTablingClassRepo.findById(ttcs.getClassId()).orElse(null);
            if(cls == null) return false;
            List<Integer> weeks = cls.extractLearningWeeks();
            List<ModelResponseTimeTablingClass> allClasses = findAllBySemester(semester);
            Long versionId = ttcs.getVersionId();
            //List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo
            //        .findAllBySemesterAndRoomNotNull(semester);

            List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo
                    .findAllByVersionIdAndSemesterAndRoomNotNull(versionId,semester);

            log.info("updateTimeTableClassSegment, get scheduled class segments size = " + classSegments.size());
            for(TimeTablingClassSegment cs: classSegments){
                log.info("updateTimeTableClassSegment, check CONFLICT with class segment " + cs.getId() + " room = " + cs.getRoom() + " day " + cs.getWeekday() + " start = " + cs.getStartTime() + " end = " + cs.getEndTime());
                if(cs.getRoom()==null) continue;
                if(!cs.getCrew().equals(ttcs.getCrew())) continue;
                if(cs.getWeekday() != r.getWeekday()) continue;
                boolean overLap = Util.overLap(r.getStartTime(),r.getEndTime()-r.getStartTime()+1,cs.getStartTime(),cs.getEndTime()-cs.getStartTime()+1);
                log.info("updateTimeTableClassSegment, check CONFLICT with class segment " + cs.getId() +
                        " room = " + cs.getRoom() + " day " + cs.getWeekday() + " start = " + cs.getStartTime() +
                        " end = " + cs.getEndTime() + " overLap = " + overLap);

                if(overLap){
                    if(cs.getRoom().equals(r.getRoom())){
                        String msg = "Conflict room " + cs.getRoom() + " scheduled for class " + cs.getClassId();
                        log.info("updateTimeTableClassSegment, DETECT Conflict room, msg = " + msg);
                        throw new ConflictScheduleException(msg);
                    }
                }
            }
            /*
            for(ModelResponseTimeTablingClass c: allClasses){
                List<Integer> LW = TimeTablingClass.extractLearningWeeks(c.getLearningWeeks());
                int cnt = Util.intersect(weeks,LW);
                log.info("updateTimeTableClassSegment, check CONFLICT, consider class " + c.getClassCode() + " cnt = " + cnt);
                if(cnt >= 1){
                    if (c.getTimeSlots() == null) {
                        log.warn("updateTimeTableClassSegment, timeSlots is null for class " + c.getClassCode());
                        continue;
                    }
                    
                    for(TimeTablingClassSegment cs: c.getTimeSlots()){
                        log.info("updateTimeTableClassSegment, check CONFLICT with class segment " + cs.getId() + " room = " + cs.getRoom() + " day " + cs.getWeekday() + " start = " + cs.getStartTime() + " end = " + cs.getEndTime());
                        if(cs.getRoom()==null) continue;
                        if(!cs.getCrew().equals(ttcs.getCrew())) continue;
                        if(cs.getWeekday() != r.getWeekday()) continue;
                        boolean overLap = Util.overLap(r.getStartTime(),r.getEndTime()-r.getStartTime()+1,cs.getStartTime(),cs.getEndTime()-cs.getStartTime()+1);
                        log.info("updateTimeTableClassSegment, check CONFLICT with class segment " + cs.getId() +
                                " room = " + cs.getRoom() + " day " + cs.getWeekday() + " start = " + cs.getStartTime() +
                                " end = " + cs.getEndTime() + " overLap = " + overLap);

                        if(overLap){
                            if(cs.getRoom().equals(r.getRoom())){
                                String msg = "Conflict room " + cs.getRoom() + " scheduled for class " + c.getClassCode();
                                log.info("updateTimeTableClassSegment, DETECT Conflict room, msg = " + msg);
                                throw new ConflictScheduleException(msg);
                            }
                        }
                    }
                }
            }
            */
            ttcs.setStartTime(r.getStartTime());
            ttcs.setEndTime(r.getEndTime());
            ttcs.setWeekday(r.getWeekday());
            ttcs.setRoom(r.getRoom());
            ttcs = timeTablingClassSegmentRepo.save(ttcs);
        }
        return true;
    }

    @Override
    public ModelResponseManualAssignTimeTable manualAssignTimetable2Classsegment(String userId, ModelInputManualAssignTimeTable I) {

        log.info("manualAssignTimetable2Classsegment, Input = " + I.getClassSegmentId() + " version " + I.getVersionId());
        ModelResponseManualAssignTimeTable res = new ModelResponseManualAssignTimeTable();
        TimeTablingTimeTableVersion ver = timeTablingVersionRepo.findById(I.getVersionId()).orElse(null);
        if(ver == null){
            log.info("manualAssignTimetable2Classsegment, cannot find version, return");
            res.setStatus(ModelResponseManualAssignTimeTable.STATUS_NOT_FOUND); return res;
        }
        TimeTablingClassSegment classSegment = timeTablingClassSegmentRepo.findById(I.getClassSegmentId()).orElse(null);
        if(classSegment == null){
            log.info("manualAssignTimetable2Classsegment, cannot find classSegment, return");
            res.setStatus(ModelResponseManualAssignTimeTable.STATUS_NOT_FOUND); return res;
        }
        if(classSegment.getDuration() + I.getStartTime() - 1  > ver.getNumberSlotsPerSession()){
            res.setStatus(ModelResponseManualAssignTimeTable.STATUS_OUT_OF_RANGE);
            int endTime = I.getStartTime() + classSegment.getDuration() - 1;
            res.setMessage("startTime = " + I.getStartTime() + " duration = " + classSegment.getDuration() +
                    " -> endTime = " + endTime + " : out-of-range slot (" + ver.getNumberSlotsPerSession() + ")");
            return res;
        }

        List<TimeTablingBatch> batches = timeTablingBatchRepo.findAllBySemester(ver.getSemester());
        if(batches == null){
            log.info("manualAssignTimetable2Classsegment, batches NULL???");
            res.setStatus(ModelResponseManualAssignTimeTable.STATUS_NOT_FOUND);
            return res;
        }
        log.info("manualAssignTimetable2Classsegment, ver.semester = " + ver.getSemester() + " -> batches.sz = " + batches.size());
        List<Long> batchIds = batches.stream().map(b -> b.getId()).toList();
        List<TimeTablingTimeTableVersion> versions = timeTablingVersionRepo
                .findAllByStatusAndBatchIdIn(TimeTablingTimeTableVersion.STATUS_PUBLISHED,batchIds);
        List<Long> versionIds = new ArrayList<>();
        versionIds.add(classSegment.getVersionId());
        if(versions != null){
            List<Long> tmp = versions.stream().map(v -> v.getId()).toList();
            for(Long id: tmp) versionIds.add(id);
        }

        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByVersionIdIn(versionIds);
        log.info("manualAssignTimetable2Classsegment classSegments to CHECK = " + classSegments.size() + " versionIds = " + versionIds.size());
        List<TimeTablingClass> classes = timeTablingClassRepo.findAllByBatchIdIn(batchIds);
        log.info("manualAssignTimetable2Classsegment, classes.sz = " + classes.size() + " in batchIds,sz " + batchIds.size());
        Map<Long, TimeTablingClass> mId2Class = new HashMap<>();
        for(TimeTablingClass cls: classes) mId2Class.put(cls.getId(),cls);
        for(TimeTablingClassSegment cs: classSegments){
            log.info("manualAssignTimetable2Classsegment, check CONFLICT with class segment " + cs.getId() + " room = " + cs.getRoom() + " day " + cs.getWeekday() + " start = " + cs.getStartTime() + " end = " + cs.getEndTime());
            if(cs.getRoom()==null) continue;
            if(!cs.getCrew().equals(I.getSession())) continue;
            if(cs.getWeekday() != I.getDay()) continue;
            boolean overLap = Util.overLap(I.getStartTime(),I.getDuration(),cs.getStartTime(),cs.getDuration());//cs.getEndTime()-cs.getStartTime()+1);
            log.info("manualAssignTimetable2Classsegment, check CONFLICT with class segment " + cs.getId() +
                    " room = " + cs.getRoom() + " day " + cs.getWeekday() + " start = " + cs.getStartTime() +
                    " end = " + cs.getEndTime() + " overLap = " + overLap);

            if(overLap){
                if(cs.getRoom().equals(I.getRoomCode())){
                    String classCode = "NULL";
                    if(mId2Class.get(cs.getClassId())!=null) classCode = mId2Class.get(cs.getClassId()).getClassCode();


                    String msg = "Conflict room " + cs.getRoom() + " scheduled for class " + classCode + " classSegmentId = " + cs.getId() + " in version " + cs.getVersionId();
                    log.info("manualAssignTimetable2Classsegment, DETECT Conflict room, msg = " + msg);
                    //throw new ConflictScheduleException(msg);
                    res.setStatus(ModelResponseManualAssignTimeTable.STATUS_CONFLICT);
                    res.setMessage(msg);
                    return res;
                }
            }
        }
        classSegment.setRoom(I.getRoomCode());
        classSegment.setStartTime(I.getStartTime());
        classSegment.setEndTime(I.getStartTime() + I.getDuration() - 1);
        classSegment.setWeekday(I.getDay());
        classSegment= timeTablingClassSegmentRepo.save(classSegment);
        log.info("manualAssignTimetable2Classsegment, SAVE successfully timetable (" + classSegment.getWeekday() + "," + classSegment.getStartTime() + "," + classSegment.getRoom() + ")");

        res.setStatus(ModelResponseManualAssignTimeTable.STATUS_SUCCESS);
        res.setMessage("Assign Timetable successfully");
        return res;
    }

    @Override
    public TimeTablingClassSegment createClassSegment(Long classId, String crew, Integer duration, Long versionId) {
        TimeTablingClassSegment cs = new TimeTablingClassSegment();
        Long csId = timeTablingClassSegmentRepo.getNextReferenceValue();
        cs.setId(csId);
        cs.setClassId(classId);
        cs.setCrew(crew);
        cs.setDuration(duration);
        cs.setVersionId(versionId);
        cs = timeTablingClassSegmentRepo.save(cs);
        return cs;
    }

    @Override
    public List<TimeTablingClass> createClassFromPlan(PlanGeneralClass p) {
        /*
            log.info("savePlanClasses, start to create class for plan " + p.getModuleCode() + " nbClasses = " + p.getNumberOfClasses());
            for(int i = 1;i <= p.getNumberOfClasses();i++) {
                MakeGeneralClassRequest req = new MakeGeneralClassRequest();
                req.setId(p.getId());
                req.setNbClasses(p.getNumberOfClasses());
                req.setClassType(p.getClassType());
                req.setDuration(p.getDuration());
                req.setCrew(p.getCrew());
                req.setMass(p.getMass());
                req.setLearningWeeks(p.getLearningWeeks());
                req.setModuleCode(p.getModuleCode());
                req.setSemester(p.getSemester());
                req.setModuleName(p.getModuleName());
                req.setExerciseMaxQuantity(p.getExerciseMaxQuantity());
                req.setLectureMaxQuantity(p.getLectureMaxQuantity());
                req.setLectureExerciseMaxQuantity(p.getLectureExerciseMaxQuantity());
                req.setProgramName(p.getProgramName());
                req.setWeekType(p.getWeekType());
                //planGeneralClassService.makeClass(req, groupName);
                planGeneralClassService.makeClass(req, groupId);

                // create ONE class-segment for the new class
                //TimeTablingClassSegment cs = new TimeTablingClassSegment();

            }
        */

        return null;
    }

    private boolean overlap(String crew, int day, int startSlot, int endSlot,
                            List<String> crews, List<Integer> days, List<Integer> startSlots, List<Integer> durations){
        for(int i = 0; i < crews.size(); i++){
            if(crew.equals(crews.get(i))&& day == days.get(i) && Util.overLap(startSlot, endSlot-startSlot+1,startSlots.get(i),durations.get(i))){
                return true;
            }
        }
        return false;
    }
    private List<Classroom> searchRoomAND(ModelInputSearchRoom I,
                                          List<Classroom> rooms,
                                          List<TimeTablingClassSegment> classSegments,
                                          List<String> sessions, List<Integer> days,
                                          List<Integer> startSlots, List<Integer> durations){
        List<Classroom> res = new ArrayList<>();
        for(Classroom r: rooms)if(r.getQuantityMax() >= I.getSearchRoomCapacity()){
            boolean ok = true;
            for(TimeTablingClassSegment cs: classSegments) {

                if(cs.getRoom() != null && cs.getRoom().equals(r.getId())){
                    if(overlap(cs.getCrew(),cs.getWeekday(),cs.getStartTime(),cs.getEndTime(),sessions,days,startSlots,durations)){
                        ok = false; break;
                    }
                }

            }
            if(ok) res.add(r);
        }
        return res;
    }
    private List<Classroom> searchRoomOR(ModelInputSearchRoom I,
                                          List<Classroom> rooms,
                                          List<TimeTablingClassSegment> classSegments,
                                          List<String> sessions, List<Integer> days,
                                          List<Integer> startSlots, List<Integer> durations) {
        List<Classroom> res = new ArrayList<>();
        for(Classroom r: rooms)if(r.getQuantityMax() >= I.getSearchRoomCapacity()){
            boolean ok = false;
            for(int i = 0; i < sessions.size(); i++){
                String session = sessions.get(i);
                int day = days.get(i);
                int startSlot = startSlots.get(i);
                int duration = durations.get(i);
                boolean overlap = false;
                for(TimeTablingClassSegment cs: classSegments) {
                    if(cs.getRoom() != null && cs.getRoom().equals(r.getId())){
                        if(session.equals(cs.getCrew())&&day==cs.getWeekday()&&
                                Util.overLap(startSlot,duration,cs.getStartTime(),cs.getEndTime()-cs.getStartTime()+1)){
                            overlap = true; break;
                        }
                    }
                }
                if(!overlap){
                    ok = true; break;
                }
            }
            if(ok) res.add(r);
        }
        return res;
    }
        @Override
    public List<Classroom> searchRoom(ModelInputSearchRoom I) {
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo
                .findAllByVersionId(Long.valueOf(I.getVersionId()));
        List<Classroom> rooms = classroomRepo.findAll();
        List<Classroom> res = new ArrayList<>();
        List<String> sessions = new ArrayList<>();
        List<Integer> days = new ArrayList<>();
        List<Integer> startSlots = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();
        String key = I.getTimeSlots();
        if(key.contains(";")){// search OR condition
            String[] s = key.split(";");
            for(String es: s){
                String[] a = es.split("-");
                sessions.add(a[0]);
                days.add(Integer.valueOf(a[1]));
                startSlots.add(Integer.valueOf(a[2]));
                durations.add(Integer.valueOf(a[3]));
            }
            res = searchRoomOR(I,rooms,classSegments,sessions,days,startSlots,durations);

        }else if(key.contains(":")){ // search AND condition
            String[] s = key.split(":");
            for(String es: s){
                String[] a = es.split("-");
                sessions.add(a[0]);
                days.add(Integer.valueOf(a[1]));
                startSlots.add(Integer.valueOf(a[2]));
                durations.add(Integer.valueOf(a[3]));
            }

            res = searchRoomAND(I,rooms,classSegments,sessions,days,startSlots,durations);

        }else{// only one element
            String[] a = key.split("-");
            sessions.add(a[0]);
            days.add(Integer.valueOf(a[1]));
            startSlots.add(Integer.valueOf(a[2]));
            durations.add(Integer.valueOf(a[3]));

            res = searchRoomOR(I,rooms,classSegments,sessions,days,startSlots,durations);
        }
        return res;
    }

    @Transactional
    @Override
    public void mergeAndDeleteClassSegments(Long timeTablingClassId, Long timeTablingClassSegmentIdToDelete, Long versionId) {
        TimeTablingClass targetClass = timeTablingClassRepo.findById(timeTablingClassId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy lớp với ID: " + timeTablingClassId));

        TimeTablingClassSegment segmentToDelete = timeTablingClassSegmentRepo.findById(timeTablingClassSegmentIdToDelete)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ca học với ID: " + timeTablingClassSegmentIdToDelete));

        if (!segmentToDelete.getClassId().equals(targetClass.getId())) {
            throw new NotFoundException("Ca học ID " + timeTablingClassSegmentIdToDelete + " không thuộc lớp ID " + timeTablingClassId);
        }

       
        if (versionId != null && !Objects.equals(segmentToDelete.getVersionId(), versionId)) {
            log.error("Phiên bản của ca học cần xóa (ID {}, versionId {}) không khớp với phiên bản yêu cầu ({}).",
                segmentToDelete.getId(), segmentToDelete.getVersionId(), versionId);
            throw new IllegalStateException("Thao tác không thể thực hiện do xung đột phiên bản của ca học cần xóa.");
        }


        Long parentSegmentId = segmentToDelete.getParentId();
        if (parentSegmentId == null) {
           
            log.warn("Ca học (ID: {}) không có ca học cha. Không thể thực hiện logic gộp.", segmentToDelete.getId());
            throw new NotFoundException("Ca học (ID: " + segmentToDelete.getId() + ") không có ca học cha. Không thể tự động gộp và xóa theo logic này.");
        }

        TimeTablingClassSegment parentSegment = timeTablingClassSegmentRepo.findById(parentSegmentId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ca học cha (ID: " + parentSegmentId + ") để gộp vào."));

        if (!parentSegment.getClassId().equals(targetClass.getId())) {
            log.error("Ca học cha ID {} (classId={}) không thuộc cùng lớp với ca học con ID {} (classId={}). Lớp mục tiêu ID {}",
                parentSegment.getId(), parentSegment.getClassId(),
                segmentToDelete.getId(), segmentToDelete.getClassId(),
                targetClass.getId());
            throw new IllegalStateException("Thông tin ca học cha không hợp lệ: không cùng lớp với ca học con.");
        }
        if (!Objects.equals(parentSegment.getVersionId(), segmentToDelete.getVersionId())) {
            log.error("Ca học cha ID {} (versionId={}) không cùng phiên bản với ca học con ID {} (versionId={}).",
                parentSegment.getId(), parentSegment.getVersionId(),
                segmentToDelete.getId(), segmentToDelete.getVersionId());
            throw new IllegalStateException("Thông tin ca học cha không hợp lệ: không cùng phiên bản với ca học con.");
        }
        if (versionId != null && !Objects.equals(parentSegment.getVersionId(), versionId)) {
            log.error("Phiên bản của ca học cha (ID {}, versionId {}) không khớp với phiên bản yêu cầu ({}).",
                parentSegment.getId(), parentSegment.getVersionId(), versionId);
            throw new IllegalStateException("Thao tác không thể thực hiện do xung đột phiên bản của ca học cha.");
        }

        parentSegment.setDuration(parentSegment.getDuration() + segmentToDelete.getDuration());
        timeTablingClassSegmentRepo.save(parentSegment);
        log.info("Đã gộp thời lượng từ ca học ID {} (duration={}) vào ca học cha ID {}. Ca học cha duration mới: {}",
                segmentToDelete.getId(), segmentToDelete.getDuration(), parentSegment.getId(), parentSegment.getDuration());

        
        log.info("Ca học ID {} sẽ được xóa.", segmentToDelete.getId());

        timeTablingClassSegmentRepo.delete(segmentToDelete);
        log.info("Đã xóa thành công ca học ID {}", timeTablingClassSegmentIdToDelete);
    }


}