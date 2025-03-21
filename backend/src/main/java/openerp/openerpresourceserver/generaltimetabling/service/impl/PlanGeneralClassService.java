package openerp.openerpresourceserver.generaltimetabling.service.impl;


import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekExtractor;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekValidator;
import openerp.openerpresourceserver.generaltimetabling.model.dto.MakeGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateSubClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputGenerateClassesFromPlan;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputGenerateClassSegmentFromClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.BulkMakeGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.CreateSingleClassOpenRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.AcademicWeek;
import openerp.openerpresourceserver.generaltimetabling.model.entity.ClassGroup;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Group;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;
import openerp.openerpresourceserver.generaltimetabling.repo.*;
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
    private PlanGeneralClassRepository planGeneralClassRepository;
    private AcademicWeekRepo academicWeekRepo;
    private ClassGroupRepo classGroupRepo;

    @Autowired
    private RoomReservationRepo roomReservationRepo;

    @Autowired
    private GroupRepo groupRepo;

    @Transactional
    public int clearPlanClass(String semesterId){
        planGeneralClassRepository.deleteAllBySemester(semesterId);
        return 0;
    }
    //public void makeClass(MakeGeneralClassRequest request, String groupName) {
    public void makeClass(MakeGeneralClassRequest request, Long groupId) {
        log.info("makeClass, groupId = " + groupId);
        //List<Group> groups = groupRepo.getAllByGroupName(groupName);
        //if (groups == null || groups.isEmpty()) {
        //    throw new NotFoundException("Không tìm thấy nhóm lớp " + groupName);
        //}
        Group gr = groupRepo.findById(groupId).orElse(null);
        if(gr == null){
            throw new NotFoundException("Không tìm thấy nhóm lớp " + groupId);
        }
        String groupName = gr.getGroupName();
        //Long groupId = groups.get(0).getId();
        //Long groupId = gr.getId();

        GeneralClass newClass = new GeneralClass();

        newClass.setRefClassId(request.getId());
        newClass.setSemester(request.getSemester());
        newClass.setModuleCode(request.getModuleCode());
        newClass.setModuleName(request.getModuleName());
        newClass.setMass(request.getMass());
        newClass.setGroupName(groupName);
        newClass.setCrew(request.getCrew());
        //newClass.setQuantityMax(request.getQuantityMax());
        if(request.getLectureExerciseMaxQuantity()!=null) newClass.setQuantityMax(request.getLectureExerciseMaxQuantity());
        if(request.getLectureMaxQuantity()!=null)newClass.setQuantityMax(request.getLectureMaxQuantity());
        if(request.getExerciseMaxQuantity()!=null)newClass.setQuantityMax(request.getExerciseMaxQuantity());
        newClass.setLearningWeeks(request.getLearningWeeks());
        newClass.setDuration(request.getDuration());
        if (request.getClassType() != null && !request.getClassType().isEmpty()) {
            newClass.setClassType(request.getClassType());
        } else {
            newClass.setClassType("LT+BT");
        }


        if(request.getWeekType().equals("Chẵn")) {
            List<Integer> weekIntList = LearningWeekExtractor.extractArray(request.getLearningWeeks()).stream().filter(num->num%2==0).toList();
            String weekListString = weekIntList.stream().map(num -> num + "-" + num)
                    .collect(Collectors.joining(","));
            newClass.setLearningWeeks(weekListString);
        } else if(request.getWeekType().equals("Lẻ")) {
            List<Integer> weekIntList = LearningWeekExtractor.extractArray(request.getLearningWeeks()).stream().filter(num->num%2!=0).toList();
            String weekListString = weekIntList.stream().map(num -> num + "-" + num)
                    .collect(Collectors.joining(","));
            newClass.setLearningWeeks(weekListString);
        } else if(request.getWeekType().equals("Chẵn+Lẻ")) {
            newClass.setLearningWeeks(request.getLearningWeeks());
        }

        Long nextId = planGeneralClassRepository.getNextReferenceValue();
        //newClass.setParentClassId(nextId);
        newClass.setClassCode(nextId.toString());

        List<RoomReservation> roomReservations = new ArrayList<>();
        RoomReservation roomReservation =  new RoomReservation();
        roomReservation.setGeneralClass(newClass);
        roomReservations.add(roomReservation);
        roomReservation.setDuration(request.getDuration());
        roomReservation.setCrew(newClass.getCrew());
        newClass.setTimeSlots(roomReservations);
        generalClassRepository.save(newClass);
        ClassGroup classGroup = new ClassGroup(newClass.getId(),groupId);
        classGroupRepo.save(classGroup);
    }

    @Transactional
    public List<GeneralClass> makeSubClass(ModelInputCreateSubClass request) {
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

            Long nextId = planGeneralClassRepository.getNextReferenceValue();
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

    public List<PlanGeneralClass> getAllClasses(String semester) {
        return planGeneralClassRepository.findAllBySemester(semester);
    }

    @Transactional
    public void deleteAllClasses(String semester) {
        planGeneralClassRepository.deleteAllBySemester(semester);
    }

    public List<GeneralClass> getPlanClassById(String semester, Long planClassId) {
        return generalClassRepository.findClassesByRefClassIdAndSemester(planClassId, semester);
    }

    @Transactional
    public GeneralClass updateGeneralClass(GeneralClass generalClass) {
        GeneralClass updateGeneralClass = generalClassRepository.findById(generalClass.getId()).orElse(null);
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
        return generalClassRepository.save(updateGeneralClass);
    }
    public int generateClassSegmentFromClass(ModelInputGenerateClassSegmentFromClass I){
        List<GeneralClass> classes = generalClassRepository.findAllBySemester(I.getSemester());
        return classes.size();
    }
    public List<GeneralClass> generateClassesFromPlan(ModelInputGenerateClassesFromPlan I){
        List<GeneralClass> classes = generalClassRepository.findAllBySemester(I.getSemester());
        for(GeneralClass gc: classes){
            log.info("generateClassesFromPlan, consider class " + gc.getId() + "/" + classes.size());
            List<RoomReservation> rrs = new ArrayList<>();
            RoomReservation rr = new RoomReservation();
            rr.setCrew(gc.getCrew());
            rr.setDuration(gc.getDuration());
            rr.setGeneralClass(gc);
            rr = roomReservationRepo.save(rr);

            rrs.add(rr);
            //gc.setTimeSlots(rrs);
            //gc = generalClassRepository.save(gc);
        }
        /*
        List<PlanGeneralClass> plan = planGeneralClassRepository.findAllBySemester(I.getSemester());
        List<GeneralClass> classes = new ArrayList<>();
        for(PlanGeneralClass pl: plan){
            for(int i = 1; i <= pl.getNumberOfClasses();i++) {
                MakeGeneralClassRequest r = new MakeGeneralClassRequest();
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

            }

        }

         */
        return classes;
    }

    public List<GeneralClass> makeMultipleClasses(BulkMakeGeneralClassRequest request) {
        List<GeneralClass> createdClasses = new ArrayList<>();

        for (int i = 0; i < request.getQuantity(); i++) {
            GeneralClass newClass = new GeneralClass();
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

            Long nextId = planGeneralClassRepository.getNextReferenceValue();
            newClass.setClassCode(nextId.toString());
            newClass.setCourse(request.getClassRequest().getModuleCode());


            createdClasses.add(generalClassRepository.save(newClass));
        }

        return createdClasses;
    }

    @Transactional
    public PlanGeneralClass updatePlanClass(PlanGeneralClass planClass) {
        PlanGeneralClass planGeneralClass = planGeneralClassRepository.findById(planClass.getId()).orElse(null);
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
        planGeneralClass.setLectureMaxQuantity(planClass.getLectureMaxQuantity());
        planGeneralClass.setExerciseMaxQuantity(planClass.getExerciseMaxQuantity());
        planGeneralClass.setLectureExerciseMaxQuantity(planClass.getLectureExerciseMaxQuantity());
        planGeneralClass.setQuantityMax(planClass.getQuantityMax());
        planGeneralClass.setClassType(planClass.getClassType());
        return planGeneralClassRepository.save(planGeneralClass);
    }

    @Transactional
    public List<PlanGeneralClass> deleteClassesByIds(List<Long> planClassIds){
        List<PlanGeneralClass> foundClasses = planGeneralClassRepository.findAllById(planClassIds);
        if(foundClasses == null){
            throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        }
        planGeneralClassRepository.deleteAllById(planClassIds);
        return foundClasses;
    }

    @Transactional
    public PlanGeneralClass deleteClassById(Long planClassId) {
        PlanGeneralClass foundClass = planGeneralClassRepository.findById(planClassId).orElse(null);
        if (foundClass == null) throw new NotFoundException("Không tìm thấy lớp kế hoạch!");
        planGeneralClassRepository.deleteById(planClassId);
        return foundClass;
    }

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
        savedClass.setProgramName(planClass.getProgramName());
        savedClass = planGeneralClassRepository.save(savedClass);

        log.info("PlanGeneralClass successfully saved: {}", savedClass);

        // Process MakeGeneralClassRequest for each class
        for (int i = 1; i <= planClass.getNumberOfClasses(); i++) {
            MakeGeneralClassRequest req = new MakeGeneralClassRequest();
            req.setId(savedClass.getId());
            req.setNbClasses(planClass.getNumberOfClasses());
            req.setClassType(planClass.getClassType());
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
        }

        return savedClass;
    }
}
