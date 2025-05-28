package openerp.openerpresourceserver.generaltimetabling.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekExtractor;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekValidator;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateTimeTablingClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateSubClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.UpdateTimeTablingClassFromPlanRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputGenerateClassSegmentFromClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.BulkMakeGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.CreateSingleClassOpenRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.AcademicWeek;
import openerp.openerpresourceserver.generaltimetabling.model.entity.ClassGroup;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Group;
import openerp.openerpresourceserver.generaltimetabling.model.entity.TimeTablingCourse;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.*;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
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
public class PlanGeneralClassService {
    private GeneralClassRepository generalClassRepository;
    private PlanGeneralClassRepo planGeneralClassRepo;
    private AcademicWeekRepo academicWeekRepo;
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
    private RoomReservationRepo roomReservationRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Transactional
    public int clearPlanClass(String semesterId){
        planGeneralClassRepo.deleteAllBySemester(semesterId);
        return 0;
    }

    @Transactional
    //public void makeClass(MakeGeneralClassRequest request, String groupName) {
    public TimeTablingClass makeClass(CreateTimeTablingClassRequest request, Long groupId) {
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
        //newClass.setQuantityMax(request.getQuantityMax());
        if(request.getLectureExerciseMaxQuantity()!=null)
            cls.setQuantityMax(request.getLectureExerciseMaxQuantity());
        if(request.getLectureMaxQuantity()!=null)
            cls.setQuantityMax(request.getLectureMaxQuantity());
        if(request.getExerciseMaxQuantity()!=null)
            cls.setQuantityMax(request.getExerciseMaxQuantity());
        cls.setLearningWeeks(request.getLearningWeeks());
        cls.setDuration(request.getDuration());
        if (request.getClassType() != null && !request.getClassType().isEmpty()) {
            cls.setClassType(request.getClassType());
        } else {
            cls.setClassType("LT+BT");
        }


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

    @Transactional
    public List<GeneralClass> makeSubClass(CreateSubClassRequest request) {
        GeneralClass parentClass = generalClassRepository.findById(request.getFromParentClassId()).orElse(null);
        if (parentClass == null) return Collections.emptyList();
        List<ClassGroup> classGroup = classGroupRepo.findByClassId(request.getFromParentClassId());

        List<GeneralClass> newClasses = new ArrayList<>();

        for (int i = 0; i < request.getNumberClasses(); i++) {
            GeneralClass newClass = new GeneralClass();
            newClass.setParentClassId(parentClass.getId());
            newClass.setRefClassId(parentClass.getRefClassId());
            newClass.setSemester(parentClass.getSemester());
            newClass.setModuleCode(parentClass.getModuleCode());
            newClass.setClassCode(parentClass.getClassCode());
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

            Long nextId = planGeneralClassRepo.getNextReferenceValue();
            List<RoomReservation> roomReservations = new ArrayList<>();
            RoomReservation roomReservation = new RoomReservation();
            roomReservation.setGeneralClass(newClass);
            roomReservation.setCrew(newClass.getCrew());
            roomReservation.setDuration(newClass.getDuration());

            roomReservations.add(roomReservation);

            newClass.setTimeSlots(roomReservations);

            generalClassRepository.save(newClass);

            for (ClassGroup group : classGroup) {
                ClassGroup newClassGroup = new ClassGroup(newClass.getId(), group.getGroupId());
                classGroupRepo.save(newClassGroup);
            }
            newClasses.add(newClass);
        }

        return newClasses;
    }
    @Transactional
    public List<TimeTablingClass> makeSubClassNew(CreateSubClassRequest request) {
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

    public List<PlanGeneralClass> getAllClasses(String semester) {
        return planGeneralClassRepo.findAllBySemester(semester);
    }

    @Transactional
    public void deleteAllClasses(String semester) {
        planGeneralClassRepo.deleteAllBySemester(semester);
    }

    public List<GeneralClass> getPlanClassById(String semester, Long planClassId) {
        return generalClassRepository.findClassesByRefClassIdAndSemester(planClassId, semester);
    }
    public List<TimeTablingClass> getClassOfPlan(String semester, Long planClassId){
        List<TimeTablingClass> timeTablingClasses = timeTablingClassRepo.findAllByRefClassId(planClassId);
        return timeTablingClasses;
    }

    @Transactional
    public TimeTablingClass updateGeneralClass(TimeTablingClass generalClass) {
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
    public int generateClassSegmentFromClass(ModelInputGenerateClassSegmentFromClass I){
        List<GeneralClass> classes = generalClassRepository.findAllBySemester(I.getSemester());
        for(GeneralClass gc: classes){
            RoomReservation rr = new RoomReservation();
            rr.setDuration(gc.getDuration());
            rr.setGeneralClass(gc);
            rr.setCrew(gc.getCrew());
            rr = roomReservationRepo.save(rr);
            log.info("generateClassSegmentFromClass, save roomReservation " + rr.getId() + " for class " + gc.getId() + " course " + gc.getModuleCode());
        }
        return classes.size();
    }    
    
    @Transactional
    public List<TimeTablingClass> generateTimeTablingClassFromPlan(UpdateTimeTablingClassFromPlanRequest I){
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

            for (int i = 1; i <= pl.getNumberOfClasses(); i++) {
                CreateTimeTablingClassRequest r = new CreateTimeTablingClassRequest();
                r.setNbClasses(pl.getNumberOfClasses());
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
            }
            
            // Increment the numberOfClasses field for this plan and save it
            pl.setNumberOfClasses(pl.getNumberOfClasses() + 1);
            planGeneralClassRepo.save(pl);
            log.info("Incremented numberOfClasses for plan " + pl.getModuleCode() + " to " + pl.getNumberOfClasses());
        }

        return res;
    }
    public List<GeneralClass> generateClassesFromPlan(UpdateTimeTablingClassFromPlanRequest I){
        List<PlanGeneralClass> plan = planGeneralClassRepo.findAllBySemester(I.getSemester());
        log.info("generateClassesFromPlan, number of plan courses = " + plan.size());
        List<GeneralClass> classes = new ArrayList<>();
        for(PlanGeneralClass pl: plan){
            log.info("generateClassesFromPlan, plan course " + pl.getModuleCode() + " number classes = " + pl.getNumberOfClasses());

                pl.setNumberOfClasses(pl.getNumberOfClasses()+1);
                for (int i = 1; i <= pl.getNumberOfClasses(); i++) {
                    CreateTimeTablingClassRequest r = new CreateTimeTablingClassRequest();
                    r.setNbClasses(pl.getNumberOfClasses());
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

                    makeClass(r, pl.getGroupId());
                }
                planGeneralClassRepo.save(pl);
        }


        return classes;
    }    
    
    @Transactional
    public List<TimeTablingClass> makeMultipleClasses(BulkMakeGeneralClassRequest request) {
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

    @Transactional
    public PlanGeneralClass deleteClassById(Long planClassId) {
        PlanGeneralClass foundClass = planGeneralClassRepo.findById(planClassId).orElse(null);
        if (foundClass == null) throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        planGeneralClassRepo.deleteById(planClassId);
        return foundClass;
    }

    @Transactional
    public PlanGeneralClass createSingleClass(CreateSingleClassOpenRequest planClass) {
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
            CreateTimeTablingClassRequest req = new CreateTimeTablingClassRequest();
            req.setId(savedClass.getId());
            req.setNbClasses(planClass.getNumberOfClasses());
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
}
