package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateClassSegmentRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputAssignSessionToClassesSummer;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputComputeClassCluster;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.V2UpdateClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.Classroom;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputAdvancedFilter;
import openerp.openerpresourceserver.generaltimetabling.model.input.ModelInputSearchRoom;

import java.util.List;

public interface TimeTablingClassService {
    List<TimeTablingClassSegment> createClassSegment(CreateClassSegmentRequest I);

    TimeTablingClass updateSession(TimeTablingClass cls, String crew);
    List<TimeTablingClass> assignSessionToClassesSummer(ModelInputAssignSessionToClassesSummer I);

    List<TimeTablingClassSegment> createClassSegmentForSummerSemester(CreateClassSegmentRequest I);

    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(String semester, Long groupId, Long versionId);
    public List<ModelResponseTimeTablingClass> advancedFilter(ModelInputAdvancedFilter I);

    List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(List<Long> classIds, Long versionId);

    List<ModelResponseTimeTablingClass> findAll();

    List<ModelResponseTimeTablingClass> findAllBySemester(String semester);

    List<ModelResponseTimeTablingClass> findAllByClassIdIn(List<Long> classIds);

    int removeClassSegment(CreateClassSegmentRequest I);

    int deleteByIds(List<Long> ids);

    TimeTablingClass updateClass(UpdateGeneralClassRequest request);

    public List<ModelResponseTimeTablingClass> getSubClass(Long id);

    public String clearTimeTable(List<Long> ids);

    public List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndex(String semester, int weekIndex);
    
    public List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndexAndVersionId(String semester, int weekIndex, Long versionId);

    public ModelResponseTimeTablingClass splitNewClassSegment(Long classId, Long parentClassSegmentId, Integer duration, Long versionId);

    public int computeClassCluster(ModelInputComputeClassCluster I);

    public List<ModelResponseTimeTablingClass> getClassByCluster(Long clusterId);
    
    public List<ModelResponseTimeTablingClass> getClassByCluster(Long clusterId, Long versionId);

    public boolean updateTimeTableClassSegment(String semester, List<V2UpdateClassScheduleRequest> saveRequests);

    TimeTablingClassSegment createClassSegment(Long classId, String crew, Integer duration, Long versionId);

    List<TimeTablingClass> createClassFromPlan(PlanGeneralClass p);

    public void mergeAndDeleteClassSegments(Long timeTablingClassId, Long timeTablingClassSegmentIdToDelete, Long versionId);

    List<Classroom> searchRoom(ModelInputSearchRoom I);
}