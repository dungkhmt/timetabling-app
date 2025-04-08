package openerp.openerpresourceserver.generaltimetabling.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import openerp.openerpresourceserver.generaltimetabling.algorithms.ClassSegmentPartitionConfigForSummerSemester;
import openerp.openerpresourceserver.generaltimetabling.algorithms.Util;
import openerp.openerpresourceserver.generaltimetabling.algorithms.mapdata.ConnectedComponentClassSolver;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.GeneralClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputComputeClassCluster;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.ClassGroup;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Group;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingCourse;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.*;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private PlanGeneralClassRepository planGeneralClassRepository;

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
    @Transactional
    private void createClassSegment(TimeTablingClass gc, List<Integer> seqSlots) {
        log.info("createClassSegments, classId " + gc.getId() + ", crew " + gc.getCrew() + "  course "
                + gc.getModuleCode() + " type " + gc.getClassType() + " slots = " + seqSlots);

        for (int s : seqSlots) {
            TimeTablingClassSegment cs = new TimeTablingClassSegment();
            Long csId = timeTablingClassSegmentRepo.getNextReferenceValue();
            cs.setId(csId);
            cs.setClassId(gc.getId());
            cs.setCrew(gc.getCrew());
            cs.setDuration(s);
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
                                               List<List<Integer>> PLTBT) {
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
                    createClassSegment(gc, seqSlots);
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
                    createClassSegment(gc, seqSlots);
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
                    createClassSegment(gc, seqSlots);
                }
                idx += 1;
                if (idx >= PLTBT.size()) idx = 0;
            }
        }
    }

    @Override
    public List<TimeTablingClassSegment> createClassSegment(ModelInputCreateClassSegment I) {
        List<TimeTablingClassSegment> res = new ArrayList<>();
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(I.getSemester());
        log.info("createClassSegment, semester = " + I.getSemester() + " classes.sz = " + cls.size());
        int cnt = 0;
        for(TimeTablingClass c: cls){
            cnt++;
            TimeTablingClassSegment cs = new TimeTablingClassSegment();
            cs.setCrew(c.getCrew());
            cs.setDuration(c.getDuration());
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
    public List<TimeTablingClassSegment> createClassSegmentForSummerSemester(ModelInputCreateClassSegment I) {
        ClassSegmentPartitionConfigForSummerSemester P = new ClassSegmentPartitionConfigForSummerSemester();
        List<TimeTablingCourse> courses = timeTablingCourseRepo.findAll();
        List<TimeTablingClass> cls = timeTablingClassRepo.findAll();

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
                    if (P.partitions.get(gc.getDuration()) != null)
                        PLTBT = P.partitions.get(gc.getDuration());
                }
            }
            log.info("createClassSegmentForSummerSemester, for course " + courseCode + " morning sz = " + LS.size());
            createClassSegmentsOfASession(LS, PLT, PBT, PLTBT);
            log.info("createClassSegmentForSummerSemester, for course " + courseCode + " afternoon sz = " + LC.size());
            createClassSegmentsOfASession(LC, PLT, PBT, PLTBT);

        }
        return new ArrayList<>();
    }

    @Override
    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(String semester, Long groupId) {
        List<TimeTablingClass> cls = null;
        Map<Long, List<Long>> mClassId2GroupIds = new HashMap<>();
        if(groupId == null){
            cls = timeTablingClassRepo.findAllBySemester(semester);
            log.info("getTimeTablingClassDtos, group NULL -> cls.sz = " + cls.size());
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
            log.info("getTimeTablingClassDtos, group NOT NULL -> cls.sz = " + cls.size());
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
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdIn(classIds);
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
    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(List<Long> classIds) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllByIdIn(classIds);
        Map<Long, List<Long>> mClassId2GroupIds = new HashMap<>();

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
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdIn(classIds);
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

    private List<ModelResponseTimeTablingClass> getDetailTimeTablingClassesFrom(List<TimeTablingClass> cls){
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
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdIn(classIds);
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
        return getDetailTimeTablingClassesFrom(cls);
    }

    @Override
    public List<ModelResponseTimeTablingClass> findAllBySemester(String semester) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(semester);
        return getDetailTimeTablingClassesFrom(cls);
    }

    @Override
    public List<ModelResponseTimeTablingClass> findAllByClassIdIn(List<Long> classIds) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllByIdIn(classIds);
        List<ModelResponseTimeTablingClass> res = getDetailTimeTablingClassesFrom(cls);
        return res;
    }

    @Override
    public int removeClassSegment(ModelInputCreateClassSegment I) {
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(I.getSemester());
        List<Long> classIds = cls.stream().map(c -> c.getId()).toList();
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdIn(classIds);
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
        timeTablingClassRepo.deleteAllById(ids);
        return 0;
    }

    @Override
    public TimeTablingClass updateClass(UpdateGeneralClassRequest r) {
        TimeTablingClass cls = timeTablingClassRepo
                .findById(r.getGeneralClass().getId()).orElse(null);
        if(cls == null) return null;
        GeneralClass gc = r.getGeneralClass();
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
        return cls;
    }

    @Override
    public List<ModelResponseTimeTablingClass> getSubClass(Long id) {
        List<TimeTablingClass> L = timeTablingClassRepo.findAllByParentClassId(id);
        List<ModelResponseTimeTablingClass> res = getDetailTimeTablingClassesFrom(L);
        return res;
    }

    @Override
    public List<ModelResponseTimeTablingClass> clearTimeTable(List<String> ids) {
        List<Long> classIds = new ArrayList<>();
        for (String idString : ids) {
            log.info("clearTimeTable, idString = " + idString);
            long gId = 0;
            int timeSlotIndex = 0;
            if (idString.contains("-")) {
                gId = Integer.parseInt(idString.split("-")[0]);
                timeSlotIndex = Integer.parseInt(idString.split("-")[1]) - 1;
            } else {
                gId = Integer.parseInt(idString);
                timeSlotIndex = 0;
            }
            classIds.add(gId);
        }
        //List<TimeTablingClass> cls = timeTablingClassRepo.findAllByIdIn(classIds);
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdIn(classIds);
        log.info("clearTimeTable, class-segments to be cleared sz = " + classSegments.size());
        for(TimeTablingClassSegment cs: classSegments){
            cs.setEndTime(null); cs.setStartTime(null); cs.setRoom(null);
            cs = timeTablingClassSegmentRepo.save(cs);
            log.info("clearTimeTable, clear class-segment " + cs.getId() + " classId = " + cs.getClassId());
        }
        List<ModelResponseTimeTablingClass> res = findAllByClassIdIn(classIds);
        return res;
    }

    @Override
    public List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndex(String semester, int weekIndex) {
        List<RoomOccupationWithModuleCode> res = new ArrayList<>();
        List<ModelResponseTimeTablingClass> classes = findAllBySemester(semester);
        log.info("getRoomOccupationsBySemesterAndWeekIndex, semester = " + semester + " weekIndex = " + weekIndex + " classes.sz = " + classes.size());

        for(ModelResponseTimeTablingClass c: classes){

            System.out.println(c);
            List<Integer> learningWeeks = TimeTablingClass.extractLearningWeeks(c.getLearningWeeks());
            log.info("getRoomOccupationsBySemesterAndWeekIndex, learningWeeks = " + learningWeeks);
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

                log.info("getRoomOccupationsBySemesterAndWeekIndex, add a ro room " + ro.getClassRoom() + ", start " + ro.getStartPeriod() + " end " + ro.getEndPeriod());

                res.add(ro);
            }
        }
        return res;

    }

    @Override
    public ModelResponseTimeTablingClass splitNewClassSegment(Long classId, Long parentClassSegmentId, Integer duration) {
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
        List<TimeTablingClass> cls = timeTablingClassRepo.findAllBySemester(I.getSemester());
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

        List<Cluster> oldClusters = clusterRepo.findAllBySemester(I.getSemester());
        for(Cluster c: oldClusters){
            clusterRepo.delete(c);
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
            newCluster.setSemester(I.getSemester());
            newCluster = clusterRepo.save(newCluster);

            for(TimeTablingClass gc: cluster){
                ClusterClass clusterClass = new ClusterClass();
                clusterClass.setClusterId(nextId);
                clusterClass.setClassId(gc.getId());

                clusterClass = clusterClassRepo.save(clusterClass);
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


}
