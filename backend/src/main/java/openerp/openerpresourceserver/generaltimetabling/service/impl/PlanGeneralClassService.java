package openerp.openerpresourceserver.generaltimetabling.service.impl;


import lombok.AllArgsConstructor;
import openerp.openerpresourceserver.generaltimetabling.exception.InvalidFieldException;
import openerp.openerpresourceserver.generaltimetabling.exception.NotFoundException;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekExtractor;
import openerp.openerpresourceserver.generaltimetabling.helper.LearningWeekValidator;
import openerp.openerpresourceserver.generaltimetabling.model.dto.MakeGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateSubClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.AcademicWeek;
import openerp.openerpresourceserver.generaltimetabling.model.entity.ClassGroup;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;
import openerp.openerpresourceserver.generaltimetabling.repo.AcademicWeekRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.ClassGroupRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.GeneralClassRepository;
import openerp.openerpresourceserver.generaltimetabling.repo.PlanGeneralClassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class PlanGeneralClassService {
    private GeneralClassRepository generalClassRepository;
    private PlanGeneralClassRepository planGeneralClassRepository;
    private AcademicWeekRepo academicWeekRepo;
    private ClassGroupRepo classGroupRepo;

    @Transactional
    public int clearPlanClass(String semesterId){
        planGeneralClassRepository.deleteAllBySemester(semesterId);
        return 0;
    }
    public GeneralClass makeClass(MakeGeneralClassRequest request) {
        GeneralClass newClass = new GeneralClass();

        newClass.setRefClassId(request.getId());
        newClass.setSemester(request.getSemester());
        newClass.setModuleCode(request.getModuleCode());
        newClass.setModuleName(request.getModuleName());
        newClass.setMass(request.getMass());
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

        return generalClassRepository.save(newClass);
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
}
