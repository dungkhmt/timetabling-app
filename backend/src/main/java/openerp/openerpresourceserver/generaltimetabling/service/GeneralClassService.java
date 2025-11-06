package openerp.openerpresourceserver.generaltimetabling.service;

import java.util.List;

import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateClassSegmentRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.GeneralClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputComputeClassCluster;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.V2UpdateClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.RoomReservation;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputAdvancedFilter;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputAutoScheduleTimeSlotRoom;
import openerp.openerpresourceserver.generaltimetabling.model.response.ModelResponseGeneralClass;


public interface GeneralClassService {

    ModelResponseGeneralClass getClassDetailWithSubClasses(Long classId);

    void deleteAllGeneralClasses();

    public GeneralClass updateGeneralClassSchedule(String semester, UpdateGeneralClassScheduleRequest request);

    public GeneralClass updateGeneralClass(UpdateGeneralClassRequest request);

    List<GeneralClass> addClassesToGroup(List<Long> ids, String groupName) throws Exception;

    List<GeneralClassDto> getSubClasses(Long parentClassId);

    List<GeneralClassDto> getGeneralClassDtos(String semester, Long groupId);



    public void deleteClassesBySemester(String semester);

    void deleteClassesByIds(List<Long> ids);

    List<GeneralClass> resetSchedule(List<String> ids, String semester);

    List<GeneralClass> autoScheduleGroup(String semester, String groupName, int timeLimit);

    List<ModelResponseTimeTablingClass> autoScheduleTimeSlotRoom(ModelInputAutoScheduleTimeSlotRoom I);

    List<ModelResponseTimeTablingClass> autoScheduleTimeSlotRoom4ClassSegments(ModelInputAutoScheduleTimeSlotRoom I);

                                                                               //List<ModelResponseTimeTablingClass> advancedFilter(ModelInputAdvancedFilter I);

    List<GeneralClass> autoSchedule(String semester, int timeLimit);

    List<GeneralClass> autoScheduleRoom(String semester, String groupName, int timeLimit);

    List<GeneralClass> v2UpdateClassSchedule(String semester, List<V2UpdateClassScheduleRequest> request);

    GeneralClass deleteClassById(Long generalClassId);

    GeneralClass addRoomReservation(Long generalClassId, Long parentId, Integer duration);

    void deleteRoomReservation(Long generalClassId, Long roomReservationId);

    int computeClassCluster(ModelInputComputeClassCluster I);

    List<GeneralClassDto> getGeneralClassByCluster(Long clusterId);

    List<RoomReservation> createClassSegment(CreateClassSegmentRequest I);

    int removeClassSegment(CreateClassSegmentRequest I);

}
