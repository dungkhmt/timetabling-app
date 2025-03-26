package openerp.openerpresourceserver.generaltimetabling.service.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lombok.AllArgsConstructor;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.RoomOccupation;
import openerp.openerpresourceserver.generaltimetabling.repo.AcademicWeekRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.ClassroomRepo;
import openerp.openerpresourceserver.generaltimetabling.repo.GeneralClassRepository;
import openerp.openerpresourceserver.generaltimetabling.repo.RoomOccupationRepo;
import openerp.openerpresourceserver.generaltimetabling.service.RoomOccupationService;
import openerp.openerpresourceserver.generaltimetabling.helper.GeneralExcelHelper;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class RoomOccupationServiceImp implements RoomOccupationService {

    private RoomOccupationRepo roomOccupationRepo;
    private AcademicWeekRepo academicWeekRepo;
    private GeneralExcelHelper excelHelper;
    private ClassroomRepo classroomRepo;
    private GeneralClassRepository gRepo;
    @Override
    public List<RoomOccupation> getRoomOccupationsBySemester(String semester) {
        return roomOccupationRepo.findAllBySemester(semester);
    }

    @Override
    public void saveRoomOccupation(RoomOccupation room) {
        roomOccupationRepo.save(room);
    }

    @Override
    public void saveAll(List<RoomOccupation> roomOccupations) {
        roomOccupationRepo.saveAll(roomOccupations);
    }

    @Override
    public List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndex(String semester, int weekIndex) {
        List<RoomOccupation> roomOccupations = roomOccupationRepo.findAllBySemesterAndWeekIndex(semester,weekIndex);
        List<RoomOccupationWithModuleCode> roomOccupationsWithModuleCode = new ArrayList<>();
        for(RoomOccupation roomOccupation: roomOccupations){
            String classCode = roomOccupation.getClassCode();
            List<GeneralClass> generalClasses = gRepo.findByClassCode(classCode);
            
            String moduleCode = null;
            if (!generalClasses.isEmpty()) {
                GeneralClass firstGeneralClass = generalClasses.get(0);
                moduleCode = firstGeneralClass.getModuleCode();
            }

            RoomOccupationWithModuleCode roomOcc = new RoomOccupationWithModuleCode(
                roomOccupation.getClassRoom(),
                roomOccupation.getClassCode(),
                roomOccupation.getStartPeriod(),
                roomOccupation.getEndPeriod(),
                roomOccupation.getCrew(),
                roomOccupation.getDayIndex(),
                roomOccupation.getWeekIndex(),
                roomOccupation.getStatus(),
                roomOccupation.getSemester(),
                moduleCode
            );
            roomOcc.setId(roomOccupation.getId());
            roomOccupationsWithModuleCode.add(roomOcc);
        }
        return roomOccupationsWithModuleCode;
    }

    @Override
    public ByteArrayInputStream exportExcel(String semester, int week) {
        List<RoomOccupationWithModuleCode> roomOccupations = getRoomOccupationsBySemesterAndWeekIndex(semester, week);
        return excelHelper.convertRoomOccupationToExcel(roomOccupations);
    }


    @Override
    public List<Classroom> getRoomsNotOccupiedBySemesterAndWeekDayCrewStartAndEndSLot(String semester, String crew, int week, int day, int startSlot, int endSlot) {
        // collect all rooms occupied in the given semester, crew (morning (S)/afternoon(C)), week, day
        List<RoomOccupation> lst = roomOccupationRepo.
                findAllBySemesterAndCrewAndWeekIndexAndDayIndex(semester,crew,week,day);
        List<Classroom> rooms = classroomRepo.findAll();
        HashSet<String> occupiedRooms = new HashSet();
        for(RoomOccupation ro: lst){
            boolean notOverlap = ro.getStartPeriod() > endSlot ||
                    startSlot > ro.getEndPeriod();
            if(notOverlap == false){
                occupiedRooms.add(ro.getClassRoom());
            }
        }
        List<Classroom> res = new ArrayList<>();
        for(Classroom r: rooms){
            if(!occupiedRooms.contains(r.getClassroom()))
                res.add(r);
        }
        return res;

    }

    @Override
    public List<RoomOccupation> getRoomOccupationsBySemesterAndWeekIndexOriginal(String semester, int weekIndex) {
        return roomOccupationRepo.findAllBySemesterAndWeekIndex(semester, weekIndex);
    }
}

