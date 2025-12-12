package openerp.openerpresourceserver.generaltimetabling.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekExtractor;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekValidator;
import openerp.openerpresourceserver.generaltimetabling.model.dto.*;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.CreateSingleClassOpenDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.*;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.*;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
import openerp.openerpresourceserver.generaltimetabling.service.PlanGeneralClassService;
import openerp.openerpresourceserver.generaltimetabling.service.TimeTablingClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Log4j2
public class PlanGeneralClassServiceImpl implements PlanGeneralClassService {
    @Autowired
    private PlanGeneralClassRepo planGeneralClassRepo;
    @Autowired
    private AcademicWeekRepo academicWeekRepo;
    @Autowired
    private ClassGroupRepo classGroupRepo;
    @Autowired
    private TimeTablingCourseRepo timeTablingCourseRepo;
    @Autowired
    TimeTablingClassService timeTablingClassService;
    @Autowired
    private TimeTablingVersionRepo timeTablingVersionRepo;
    @Autowired
    private TimeTablingClassRepo timeTablingClassRepo;
    @Autowired
    private TimeTablingClassSegmentRepo timeTablingClassSegmentRepo;
    @Autowired
    private GroupRepo groupRepo;
    @Autowired
    private TimeTablingBatchRepo timeTablingBatchRepo;
    
    @Override
    @Transactional
    public int clearPlanClass(String semester){
        planGeneralClassRepo.deleteAllBySemester(semester);
        return 0;
    }    
    
    @Override
    @Transactional
    //public void makeClass(MakeGeneralClassRequest request, String groupName) {
    public TimeTablingClass makeClass(CreateTimeTablingClassDto request, Long groupId) {
        log.info("makeClass, groupId = " + groupId);
        Group gr = groupRepo.findById(groupId).orElse(null);
        if(gr == null){
            throw new NotFoundException("Không tìm thấy nhóm lớp " + groupId);
        }
        String groupName = gr.getGroupName();
        TimeTablingClass cls = new TimeTablingClass();

        cls.setRefClassId(request.getId());
        cls.setSemester(request.getSemester());
        cls.setModuleCode(request.getModuleCode());
        cls.setModuleName(request.getModuleName());
        cls.setMass(request.getMass());
        cls.setGroupName(groupName);
        cls.setCrew(request.getCrew());
        cls.setBatchId(request.getBatchId());
        cls.setCreatedByUserId(request.getCreatedByUserId());
        //newClass.setQuantityMax(request.getQuantityMax());
        //if(request.getLectureExerciseMaxQuantity()!=null)
        //    cls.setQuantityMax(request.getLectureExerciseMaxQuantity());
        //if(request.getLectureMaxQuantity()!=null)
        //    cls.setQuantityMax(request.getLectureMaxQuantity());
        //if(request.getExerciseMaxQuantity()!=null)
        //    cls.setQuantityMax(request.getExerciseMaxQuantity());
        //cls.setQuantityMax(request.getQuantityMax());
        if(request.getClassType().equals("LT+BT")){
            cls.setQuantityMax(request.getLectureExerciseMaxQuantity());
        }else if(request.getClassType().equals("LT")){
            cls.setQuantityMax(request.getLectureMaxQuantity());
        }else if(request.getClassType().equals("BT")){
            cls.setQuantityMax(request.getExerciseMaxQuantity());
        }else{
            cls.setQuantityMax(request.getQuantityMax());
        }
        cls.setLearningWeeks(request.getLearningWeeks());
        cls.setDuration(request.getDuration());
        //if (request.getClassType() != null && !request.getClassType().isEmpty()) {
        //    cls.setClassType(request.getClassType());
        //} else {
        //    cls.setClassType("LT+BT");
        //}
        cls.setClassType(request.getClassType());


        if(request.getWeekType().equals("Chẵn")) {
            List<Integer> weekIntList = LearningWeekExtractor.extractArray(request.getLearningWeeks()).stream().filter(num->num%2==0).toList();
            String weekListString = weekIntList.stream().map(num -> num + "-" + num)
                    .collect(Collectors.joining(","));
            cls.setLearningWeeks(weekListString);
        } else if(request.getWeekType().equals("Lẻ")) {
            List<Integer> weekIntList = LearningWeekExtractor.extractArray(request.getLearningWeeks()).stream().filter(num->num%2!=0).toList();
            String weekListString = weekIntList.stream().map(num -> num + "-" + num)
                    .collect(Collectors.joining(","));
            cls.setLearningWeeks(weekListString);
        } else if(request.getWeekType().equals("Chẵn+Lẻ")) {
            cls.setLearningWeeks(request.getLearningWeeks());
        }

        Long clsId = timeTablingClassRepo.getNextReferenceValue();//planGeneralClassRepository.getNextReferenceValue();
        //newClass.setParentClassId(nextId);
        cls.setClassCode(clsId.toString());
        cls.setId(clsId);
        cls.setParentClassId(request.getParentClassId());

        cls = timeTablingClassRepo.save(cls);
        ClassGroup clsGroup = new ClassGroup(cls.getId(),groupId);
        classGroupRepo.save(clsGroup);
        log.info("makeClass -> SAVE classId " + cls.getId() + " groupId " + groupId);

        // create corresponding class-segment
        //List<TimeTablingTimeTableVersion> versions = timeTablingVersionRepo.findAll();
        List<TimeTablingTimeTableVersion> versions = timeTablingVersionRepo.findAllBySemester(request.getSemester());

        for(TimeTablingTimeTableVersion v: versions) {
            TimeTablingClassSegment cs = timeTablingClassService.createClassSegment(cls.getId(),cls.getCrew(),cls.getDuration(),v.getId());
        }
        return cls;
    }    
    
    @Override
    @Transactional
    public List<TimeTablingClass> makeSubClass(CreateSubClassDto request) {
        TimeTablingClass parentClass = timeTablingClassRepo.findById(request.getFromParentClassId()).orElse(null);
        if (parentClass == null) return Collections.emptyList();
        List<ClassGroup> classGroup = classGroupRepo.findByClassId(request.getFromParentClassId());
        log.info("makeSubClassNew, related classGroup.sz = " + classGroup.size());
        List<TimeTablingClass> newClasses = new ArrayList<>();

        for (int i = 0; i < request.getNumberClasses(); i++) {
            TimeTablingClass newClass = new TimeTablingClass();
            newClass.setParentClassId(parentClass.getId());
            newClass.setRefClassId(parentClass.getRefClassId());
            newClass.setSemester(parentClass.getSemester());
            newClass.setModuleCode(parentClass.getModuleCode());
            Long nextCode = timeTablingClassRepo.getNextReferenceValue();
            String classCode = nextCode + "";
            newClass.setClassCode(classCode);
            newClass.setModuleName(parentClass.getModuleName());
            newClass.setMass(parentClass.getMass());
            newClass.setCrew(parentClass.getCrew());
            newClass.setQuantityMax(request.getNumberStudents());
            newClass.setLearningWeeks(parentClass.getLearningWeeks());
            newClass.setDuration(request.getDuration());
            newClass.setGroupName(parentClass.getGroupName());

            if (request.getClassType() != null && !request.getClassType().isEmpty()) {
                newClass.setClassType(request.getClassType());
            } else {
                newClass.setClassType("LT+BT");
            }

            Long nextId = timeTablingClassRepo.getNextReferenceValue();
            log.info("makeSubClassNew, generate nextId = " + nextId);
            newClass.setId(nextId);
            newClass.setGroupName(parentClass.getGroupName());
            newClass = timeTablingClassRepo.save(newClass);
            log.info("makeSubClassNew, after save, newClass.id = " + newClass.getId());

            for (ClassGroup group : classGroup) {
                ClassGroup newClassGroup = new ClassGroup(newClass.getId(), group.getGroupId());
                classGroupRepo.save(newClassGroup);
                log.info("makeSubClassNew, save newClassGroup " + newClassGroup.getClassId() + "," + newClassGroup.getGroupId());

            }
            // make 1 corresponding class segment

            TimeTablingClassSegment cs = new TimeTablingClassSegment();
            Long csId = timeTablingClassSegmentRepo.getNextReferenceValue();
            cs.setId(csId);
            cs.setClassId(newClass.getId());
            cs.setCrew(newClass.getCrew());
            cs.setDuration(newClass.getDuration());
            cs = timeTablingClassSegmentRepo.save(cs);


            newClasses.add(newClass);
        }

        return newClasses;
    }

    @Override
    public List<PlanGeneralClass> getAllPlanClasses(String semester) {
        return planGeneralClassRepo.findAllBySemester(semester);
    }

    @Override
    public List<PlanGeneralClass> getOpenedClassPlans(Long batchId) {
        return planGeneralClassRepo.findAllByBatchId(batchId);
    }

    @Override
    @Transactional
    public List<TimeTablingClass> getClassOfPlan(Long planClassId){
        List<TimeTablingClass> timeTablingClasses = timeTablingClassRepo.findAllByRefClassId(planClassId);
        return timeTablingClasses;
    }   
    
     @Override
    @Transactional
    public TimeTablingClass updateTimeTablingClass(TimeTablingClass generalClass) {
        TimeTablingClass updateGeneralClass = timeTablingClassRepo.findById(generalClass.getId()).orElse(null);
        if (updateGeneralClass == null) throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        List<AcademicWeek> foundWeeks = academicWeekRepo.findAllBySemester(updateGeneralClass.getSemester());
        if (foundWeeks.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tuần học trong học kỳ");
        }
        if (updateGeneralClass.getLearningWeeks() != null && !LearningWeekValidator.validate(updateGeneralClass.getLearningWeeks(), foundWeeks)){
            throw new InvalidFieldException("Tuần học không phù hợp với danh sách tuần học");
        }
        updateGeneralClass.setParentClassId(generalClass.getParentClassId());
        updateGeneralClass.setQuantityMax(generalClass.getQuantityMax());
        updateGeneralClass.setClassType(generalClass.getClassType());
        updateGeneralClass.setCrew(generalClass.getCrew());
        updateGeneralClass.setDuration(generalClass.getDuration());
        updateGeneralClass.setLearningWeeks(generalClass.getLearningWeeks());
        updateGeneralClass.setClassCode(generalClass.getClassCode());
        return timeTablingClassRepo.save(updateGeneralClass);
    }    
    
    @Override
    @Transactional
    public List<TimeTablingClass> generateTimeTablingClassFromPlan(UpdateTimeTablingClassFromPlanDto I){
        List<TimeTablingClass> res = new ArrayList<>();
        List<PlanGeneralClass> plan = planGeneralClassRepo.findAllBySemester(I.getSemester());
        log.info("generateTimeTablingClassFromPlan, number of plan courses = " + plan.size());
       // List<GeneralClass> classes = new ArrayList<>();
        for(PlanGeneralClass pl: plan){
            if(pl.getGroupId()==null){
                //throw new NotFoundException("Không tìm thấy nhóm của kế hoạch mở lớp !");
                return null;
            }
        }
        for(PlanGeneralClass pl: plan){

            log.info("generateTimeTablingClassFromPlan, plan course " + pl.getModuleCode() + " number classes = " + pl.getNumberOfClasses());
                CreateTimeTablingClassDto r = new CreateTimeTablingClassDto();
                //r.setNbClasses(pl.getNumberOfClasses());
                r.setWeekType(pl.getWeekType());
                r.setId(pl.getId());
                r.setProgramName(pl.getProgramName());
                r.setCrew(pl.getCrew());
                r.setWeekType(pl.getWeekType());
                r.setLectureExerciseMaxQuantity(pl.getLectureExerciseMaxQuantity());
                r.setLectureMaxQuantity(pl.getLectureMaxQuantity());
                r.setExerciseMaxQuantity(pl.getExerciseMaxQuantity());
                r.setModuleName(pl.getModuleName());
                r.setSemester(pl.getSemester());
                r.setLearningWeeks(pl.getLearningWeeks());
                r.setModuleCode(pl.getModuleCode());
                r.setModuleName(pl.getModuleName());
                r.setDuration(pl.getDuration());
                r.setQuantityMax(pl.getQuantityMax());
                r.setMass(pl.getMass());

                TimeTablingClass cls = makeClass(r, pl.getGroupId());
                res.add(cls);
            // Increment the numberOfClasses field for this plan and save it
            pl.setNumberOfClasses(pl.getNumberOfClasses() + 1);
            planGeneralClassRepo.save(pl);
            log.info("Incremented numberOfClasses for plan " + pl.getModuleCode() + " to " + pl.getNumberOfClasses());
        }

        return res;
    }    
    
    @Override
    @Transactional
    public List<TimeTablingClass> createMultipleClasses(BulkMakeGeneralClassDto request) {
        List<TimeTablingClass> createdClasses = new ArrayList<>();

        // Get the reference plan class and update the number of classes if it exists
        Long planClassId = request.getClassRequest().getId();
        if (planClassId != null) {
            PlanGeneralClass planClass = planGeneralClassRepo.findById(planClassId).orElse(null);
            if (planClass != null) {
                // Increment the numberOfClasses field by the quantity being created
                planClass.setNumberOfClasses(planClass.getNumberOfClasses() + request.getQuantity());
                planGeneralClassRepo.save(planClass);
                log.info("Incremented numberOfClasses for plan " + planClass.getModuleCode() + " to " + planClass.getNumberOfClasses());
            }
        }

        for (int i = 0; i < request.getQuantity(); i++) {
            TimeTablingClass newClass = new TimeTablingClass();
            int maxQty = Math.max(
                    request.getClassRequest().getLectureExerciseMaxQuantity() != null ? request.getClassRequest().getLectureExerciseMaxQuantity() : 0,
                    Math.max(
                            request.getClassRequest().getExerciseMaxQuantity() != null ? request.getClassRequest().getExerciseMaxQuantity() : 0,
                            request.getClassRequest().getLectureMaxQuantity() != null ? request.getClassRequest().getLectureMaxQuantity() : 0
                    )
            );

            newClass.setRefClassId(request.getClassRequest().getId());
            newClass.setSemester(request.getClassRequest().getSemester());
            newClass.setModuleCode(request.getClassRequest().getModuleCode());
            newClass.setModuleName(request.getClassRequest().getModuleName());
            newClass.setMass(request.getClassRequest().getMass());
            newClass.setCrew(request.getClassRequest().getCrew());
            newClass.setQuantityMax(maxQty);
            newClass.setLearningWeeks(request.getClassRequest().getLearningWeeks());
            newClass.setDuration(request.getClassRequest().getDuration());
            newClass.setClassType(request.getClassType());

            Long nextId = planGeneralClassRepo.getNextReferenceValue();
            newClass.setClassCode(nextId.toString());
            newClass.setCourse(request.getClassRequest().getModuleCode());


            createdClasses.add(timeTablingClassRepo.save(newClass));
        }

        return createdClasses;
    }    
    
    @Override
    @Transactional
    public PlanGeneralClass updatePlanClass(PlanGeneralClass planClass) {
        PlanGeneralClass planGeneralClass = planGeneralClassRepo.findById(planClass.getId()).orElse(null);
        if (planGeneralClass == null) throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        List<AcademicWeek> foundWeeks = academicWeekRepo.findAllBySemester(planClass.getSemester());
        if (foundWeeks.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tuần học trong học kỳ");
        }
        if (planGeneralClass.getLearningWeeks() != null && !LearningWeekValidator.validate(planClass.getLearningWeeks(), foundWeeks)){
            throw new InvalidFieldException("Tuần học không phù hợp với danh sách tuần học");
        }
        planGeneralClass.setLearningWeeks(planClass.getLearningWeeks());
        planGeneralClass.setCrew(planClass.getCrew());
        planGeneralClass.setDuration(planClass.getDuration());
        planGeneralClass.setLectureMaxQuantity(planClass.getLectureMaxQuantity());
        planGeneralClass.setExerciseMaxQuantity(planClass.getExerciseMaxQuantity());
        planGeneralClass.setLectureExerciseMaxQuantity(planClass.getLectureExerciseMaxQuantity());
        planGeneralClass.setQuantityMax(planClass.getQuantityMax());
        planGeneralClass.setClassType(planClass.getClassType());
        return planGeneralClassRepo.save(planGeneralClass);
    }    
    
    @Override
    @Transactional
    public List<PlanGeneralClass> deleteClassesByIds(List<Long> planClassIds){
        List<PlanGeneralClass> foundClasses = planGeneralClassRepo.findAllById(planClassIds);
        if(foundClasses == null){
            throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        }
        planGeneralClassRepo.deleteAllById(planClassIds);
        // delete generated classes and class-segments
        List<TimeTablingClass> CLS = timeTablingClassRepo.findAllByRefClassIdIn(planClassIds);
        List<Long> classIds = CLS.stream().map(cls -> cls.getId()).toList();
        List<TimeTablingClassSegment> classSegments = timeTablingClassSegmentRepo.findAllByClassIdIn(classIds);
        List<Long> classSegmentIds = classSegments.stream().map(cs -> cs.getId()).toList();

        timeTablingClassSegmentRepo.deleteAllById(classSegmentIds);
        timeTablingClassRepo.deleteAllById(classIds);

        return foundClasses;
    }    
    
    @Override
    @Transactional
    public PlanGeneralClass deleteClassById(Long planClassId) {
        PlanGeneralClass foundClass = planGeneralClassRepo.findById(planClassId).orElse(null);
        if (foundClass == null) throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        planGeneralClassRepo.deleteById(planClassId);
        return foundClass;
    }    
    
    @Override
    @Transactional
    public PlanGeneralClass createSingleClass(CreateSingleClassOpenDto planClass) {
        log.info("Validating and creating single class: {}", planClass);

        // Validate the input
        if (planClass.getModuleCode() == null || planClass.getModuleCode().isEmpty()) {
            log.error("Validation failed: Module code is empty");
            throw new InvalidFieldException("Mã học phần không được để trống!");
        }
        if (planClass.getModuleName() == null || planClass.getModuleName().isEmpty()) {
            log.error("Validation failed: Module name is empty");
            throw new InvalidFieldException("Tên học phần không được để trống!");
        }
        if (planClass.getMass() == null || planClass.getMass().isEmpty()) {
            log.error("Validation failed: Mass is empty");
            throw new InvalidFieldException("Mã lớp không được để trống!");
        }
        if (planClass.getSemester() == null || planClass.getSemester().isEmpty()) {
            log.error("Validation failed: Semester is empty");
            throw new InvalidFieldException("Kỳ học không được để trống!");
        }

        // Set default values if they're null
        if (planClass.getNumberOfClasses() == null) {
            planClass.setNumberOfClasses(1);
        }
        TimeTablingCourse course = timeTablingCourseRepo.findById(planClass.getModuleCode()).orElse(null);
        if(course == null){// course code does not exist -> create new
            course = new TimeTablingCourse();
            course.setId(planClass.getModuleCode());
            course.setName(planClass.getModuleName());
            course= timeTablingCourseRepo.save(course);
        }

        // Save the PlanGeneralClass entity first
        PlanGeneralClass savedClass = new PlanGeneralClass();
        savedClass.setModuleCode(planClass.getModuleCode());
        savedClass.setModuleName(planClass.getModuleName());
        savedClass.setMass(planClass.getMass());
        savedClass.setSemester(planClass.getSemester());
        savedClass.setNumberOfClasses(planClass.getNumberOfClasses());
        savedClass.setCreatedStamp(planClass.getCreatedStamp());
        savedClass.setCrew(planClass.getCrew());
        savedClass.setLearningWeeks(planClass.getLearningWeeks());
        savedClass.setDuration(planClass.getDuration());
        savedClass.setClassType(planClass.getClassType());
        savedClass.setPromotion(planClass.getPromotion());
        savedClass.setQuantityMax(planClass.getQuantityMax());
        savedClass.setLectureMaxQuantity(planClass.getLectureMaxQuantity());
        savedClass.setExerciseMaxQuantity(planClass.getExerciseMaxQuantity());
        savedClass.setLectureExerciseMaxQuantity(planClass.getLectureExerciseMaxQuantity());
        savedClass.setWeekType(planClass.getWeekType());
        savedClass.setGroupId(planClass.getGroupId());
        savedClass.setProgramName(planClass.getProgramName());
        savedClass = planGeneralClassRepo.save(savedClass);

        log.info("PlanGeneralClass successfully saved: {}", savedClass);

        // Process MakeGeneralClassRequest for each class
        for (int i = 1; i <= planClass.getNumberOfClasses(); i++) {
            CreateTimeTablingClassDto req = new CreateTimeTablingClassDto();
            req.setId(savedClass.getId());
            //req.setNbClasses(planClass.getNumberOfClasses());
            req.setClassType(planClass.getClassType());
            if(course.getMaxStudentLT()!=null && course.getMaxStudentLT() > 0){
                req.setClassType("LT");
            }
            req.setDuration(planClass.getDuration());
            req.setCrew(planClass.getCrew());
            req.setMass(planClass.getMass());
            req.setLearningWeeks(planClass.getLearningWeeks());
            req.setModuleCode(planClass.getModuleCode());
            req.setSemester(planClass.getSemester());
            req.setModuleName(planClass.getModuleName());
            req.setExerciseMaxQuantity(planClass.getExerciseMaxQuantity());
            req.setLectureMaxQuantity(planClass.getLectureMaxQuantity());
            req.setLectureExerciseMaxQuantity(planClass.getLectureExerciseMaxQuantity());
            req.setProgramName(planClass.getProgramName());
            req.setWeekType(planClass.getWeekType());
            makeClass(req, planClass.getGroupId());

            // make sub-class (class BT of the current class LT
            if(course.getMaxStudentBT()!=null && course.getMaxStudentLT()!=null &&
            course.getMaxStudentLT() > 0 && course.getMaxStudentBT() > 0){
                int nbSubClass = course.getMaxStudentLT()/course.getMaxStudentBT();
                for(int k = 1; k <= nbSubClass; k++){
                    // not finish yet
                    //ModelInputCreateSubClass I = new ModelInputCreateSubClass();
                    //I.setClassType("BT");
                    //I.setDuration();
                    //makeSubClassNew(I);
                }
            }
        }

        return savedClass;
    }

    @Transactional
    @Override
    public PlanGeneralClass createClassOpenningPlan(String userId, CreateSingleClassOpenDto planClass) {
        TimeTablingBatch batch = timeTablingBatchRepo.findById(planClass.getBatchId()).orElse(null);
        if(batch == null){
            throw new InvalidFieldException("cannot find batch id = " + planClass.getBatchId());
        }
        Group group = groupRepo.findById(planClass.getGroupId()).orElse(null);
        if(group == null){
            throw new InvalidFieldException("cannot find group id = " + planClass.getGroupId());
        }

        TimeTablingCourse course = timeTablingCourseRepo.findById(planClass.getModuleCode()).orElse(null);
        if(course == null){// course code does not exist -> create new
            course = new TimeTablingCourse();
            course.setId(planClass.getModuleCode());
            course.setName(planClass.getModuleName());
            course= timeTablingCourseRepo.save(course);
        }

        PlanGeneralClass aPlan = new PlanGeneralClass();
        aPlan.setGroupId(planClass.getGroupId());
        aPlan.setProgramName(group.getGroupName());
        aPlan.setModuleCode(planClass.getModuleCode());
        aPlan.setModuleName(course.getName());
        aPlan.setClassType(planClass.getClassType());
        aPlan.setSemester(batch.getSemester());
        aPlan.setNumberOfClasses(planClass.getNumberOfClasses());
        aPlan.setCreatedByUserId(userId);
        aPlan.setBatchId(planClass.getBatchId());
        if(planClass.getClassType().equals("LT+BT")){
            aPlan.setLectureExerciseMaxQuantity(planClass.getLectureExerciseMaxQuantity());
            aPlan.setDuration(planClass.getDurationLTBT());
        }else if(planClass.getClassType().equals("LT")){
            aPlan.setLectureMaxQuantity(planClass.getLectureMaxQuantity());
            aPlan.setDuration(planClass.getDurationLT());
        }else if(planClass.getClassType().equals("BT")){
            aPlan.setExerciseMaxQuantity(planClass.getExerciseMaxQuantity());
            aPlan.setDuration(planClass.getDurationBT());
        }
        aPlan.setPromotion(planClass.getPromotion());
        aPlan.setCrew(planClass.getCrew());
        aPlan.setWeekType(planClass.getWeekType());



        aPlan = planGeneralClassRepo.save(aPlan);

        if(planClass.getGenerateClasses() != null && planClass.getGenerateClasses().equals("Y")) {
            // create classes
            for (int i = 1; i <= planClass.getNumberOfClasses(); i++) {
                CreateTimeTablingClassDto req = new CreateTimeTablingClassDto();
                req.setId(aPlan.getId());
                //req.setNbClasses(planClass.getNumberOfClasses());
                if(planClass.getSeparateLTBT() != null && planClass.getSeparateLTBT().equals("Y")){
                    req.setClassType("LT");
                    //req.setDuration(course.getDurationLt());
                    req.setDuration(planClass.getDurationLT());
                }else {
                    req.setClassType(planClass.getClassType());
                    //req.setDuration(course.getDurationLtBt());
                    if(planClass.getClassType().equals("BT"))
                        req.setDuration(planClass.getDurationLTBT());
                    else if(planClass.getClassType().equals("LT+BT"))
                        req.setDuration(planClass.getDurationLTBT());
                }
                //if (course.getMaxStudentLT() != null && course.getMaxStudentLT() > 0) {
                //    req.setClassType("LT");
                //}
                //req.setDuration(planClass.getDuration());
                req.setCrew(planClass.getCrew());
                req.setMass(planClass.getMass());
                req.setLearningWeeks(planClass.getLearningWeeks());
                req.setModuleCode(planClass.getModuleCode());
                req.setSemester(batch.getSemester());
                req.setModuleName(course.getName());
                //req.setQuantityMax(course.getMaxStudentLT());
                req.setQuantityMax(planClass.getQuantityMax());
                req.setExerciseMaxQuantity(planClass.getExerciseMaxQuantity());
                req.setLectureMaxQuantity(planClass.getLectureMaxQuantity());
                req.setLectureExerciseMaxQuantity(planClass.getLectureExerciseMaxQuantity());
                req.setProgramName(planClass.getProgramName());
                req.setWeekType(planClass.getWeekType());
                req.setBatchId(batch.getId());
                req.setMass(course.getVolumn());
                req.setCreatedByUserId(userId);
                TimeTablingClass cls = makeClass(req, planClass.getGroupId());

                if(planClass.getClassType().equals("LT+BT") && planClass.getSeparateLTBT() != null
                        && planClass.getSeparateLTBT().equals("Y")) {
                    // make sub-class (class BT of the current class LT
                    if (course.getMaxStudentBT() != null && course.getMaxStudentLT() != null &&
                            course.getMaxStudentLT() > 0 && course.getMaxStudentBT() > 0) {
                        //int nbSubClass = course.getMaxStudentLT() / course.getMaxStudentBT();
                        int nbSubClass = planClass.getLectureMaxQuantity()/planClass.getExerciseMaxQuantity();
                        if(planClass.getLectureMaxQuantity()%planClass.getExerciseMaxQuantity() != 0){
                            nbSubClass++;
                        }
                        for (int k = 1; k <= nbSubClass; k++) {
                            req.setClassType("BT");
                            req.setParentClassId(cls.getId());
                            req.setDuration(planClass.getDurationBT());

                            //req.setDuration(course.getDurationBt());
                            //req.setQuantityMax(course.getMaxStudentBT());
                            makeClass(req, planClass.getGroupId());
                        }
                    }
                }
            }
        }

        return aPlan;
    }

    @Transactional
    @Override
    public PlanGeneralClass updateClassOpenningPlan(String userId, CreateSingleClassOpenDto planClass) {
        PlanGeneralClass existingPlan = planGeneralClassRepo.findById(planClass.getId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy kế hoạch mở lớp với ID: " + planClass.getId()));

        // update timetabling classes (try first with update only batchId of this plan
        if(existingPlan.getBatchId()!=planClass.getBatchId()) {
            List<TimeTablingClass> classes = timeTablingClassRepo.findAllByRefClassId(planClass.getId());
            for(TimeTablingClass cls: classes){
                cls.setBatchId(planClass.getBatchId());
                log.info("updateClassOpenningPlan update batch " + planClass.getBatchId() + " for class " + cls.getId());
            }
            timeTablingClassRepo.saveAll(classes);

            //if there are versions of the batch, then add class segments to that version and remove class segments from versions of old batch
            List<TimeTablingTimeTableVersion> vers = timeTablingVersionRepo.findAllByBatchId(planClass.getBatchId());
            for(TimeTablingTimeTableVersion ver: vers){
                for(TimeTablingClass cls: classes) {
                    TimeTablingClassSegment cs = new TimeTablingClassSegment();
                    cs.setClassId(cls.getId());
                    cs.setVersionId(ver.getId());
                    cs.setDuration(cls.getDuration());
                    cs = timeTablingClassSegmentRepo.save(cs);
                }
            }

            // remove class segments of old batch
            List<TimeTablingTimeTableVersion> oldvers = timeTablingVersionRepo.findAllByBatchId(existingPlan.getBatchId());
            for(TimeTablingTimeTableVersion ver: oldvers){
                List<TimeTablingClassSegment> CS = timeTablingClassSegmentRepo.findAllByVersionId(ver.getId());
                timeTablingClassSegmentRepo.deleteAll(CS);
            }

        }


        // Cập nhật thông tin cơ bản
        existingPlan.setModuleCode(planClass.getModuleCode());
        existingPlan.setModuleName(planClass.getModuleName());
        existingPlan.setMass(planClass.getMass());
        existingPlan.setClassType(planClass.getClassType());
        existingPlan.setNumberOfClasses(planClass.getNumberOfClasses());
        existingPlan.setCrew(planClass.getCrew());
        existingPlan.setLearningWeeks(planClass.getLearningWeeks());
        existingPlan.setPromotion(planClass.getPromotion());
        existingPlan.setWeekType(planClass.getWeekType());
        existingPlan.setGroupId(planClass.getGroupId());
        existingPlan.setProgramName(planClass.getProgramName());
        existingPlan.setBatchId(planClass.getBatchId());

        // Cập nhật số lượng và thời lượng theo loại lớp
        switch (planClass.getClassType()) {
            case "LT+BT":
                existingPlan.setLectureExerciseMaxQuantity(planClass.getLectureExerciseMaxQuantity());
                existingPlan.setDuration(planClass.getDurationLTBT());
                break;
            case "LT":
                existingPlan.setLectureMaxQuantity(planClass.getLectureMaxQuantity());
                existingPlan.setDuration(planClass.getDurationLT());
                break;
            case "BT":
                existingPlan.setExerciseMaxQuantity(planClass.getExerciseMaxQuantity());
                existingPlan.setDuration(planClass.getDurationBT());
                break;
        }

        TimeTablingCourse course = timeTablingCourseRepo.findById(planClass.getModuleCode()).orElse(null);
        if(course == null){// course code does not exist -> create new
            course = new TimeTablingCourse();
            course.setId(planClass.getModuleCode());
            course.setName(planClass.getModuleName());
            course= timeTablingCourseRepo.save(course);
        }
        existingPlan.setModuleName(course.getName());

        if(planClass.getGroupId() != null) {
            Group group = groupRepo.findById(planClass.getGroupId()).orElse(null);
            //if (group == null) {
            //    throw new InvalidFieldException("cannot find group id = " + planClass.getGroupId());
            //}
            if(group != null) {
                existingPlan.setProgramName(group.getGroupName());
            }
        }
        log.info("updateClassOpenningPlan prepare to save DB");
        return planGeneralClassRepo.save(existingPlan);
    }

    @Override
    @Transactional
    public TimeTablingClass updateTimeTablingClass(String userId, UpdateClassOpenRequest request) {
        // Find the existing class by ID
        TimeTablingClass existingClass = timeTablingClassRepo.findById(request.getId())
            .orElseThrow(() -> new NotFoundException("Không tìm thấy lớp với ID: " + request.getId()));

        // Validate the semester and learning weeks
        List<AcademicWeek> foundWeeks = academicWeekRepo.findAllBySemester(existingClass.getSemester());
        if (foundWeeks.isEmpty()) {
            throw new NotFoundException("Không tìm thấy tuần học trong học kỳ");
        }
        existingClass.setModuleCode(request.getModuleCode());
        existingClass.setModuleName(request.getModuleName());
        existingClass.setDuration(request.getDuration());
        existingClass.setLearningWeeks(request.getLearningWeeks());
        existingClass.setQuantity(request.getQuantity());
        existingClass.setQuantityMax(request.getQuantityMax());
        existingClass.setClassType(request.getClassType());
        existingClass.setCrew(request.getCrew());

        // Save and return the updated class
        return timeTablingClassRepo.save(existingClass);
    }


}