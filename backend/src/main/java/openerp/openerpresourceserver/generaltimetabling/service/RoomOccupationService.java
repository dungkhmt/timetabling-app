package openerp.openerpresourceserver.generaltimetabling.service;

import java.io.ByteArrayInputStream;
import java.util.List;

import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.model.entity.occupation.RoomOccupation;


public interface RoomOccupationService {
     List<RoomOccupation> getRoomOccupationsBySemester(String semester);
    
     void saveRoomOccupation(RoomOccupation r);

     void saveAll(List<RoomOccupation> roomOccupations);

     ByteArrayInputStream exportExcel(String semester, int week, Long versionId, int numberSlotsPerSession);

    List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndex(String semester, int weekIndex);

    List<Classroom> getRoomsNotOccupiedBySemesterAndWeekDayCrewStartAndEndSLot(String semester, String crew, int week, int day, int startSlot, int endSlot);
}
