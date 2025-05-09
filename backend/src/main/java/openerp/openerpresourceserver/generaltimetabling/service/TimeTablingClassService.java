package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputComputeClassCluster;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.V2UpdateClassScheduleRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.SaveScheduleToVersionRequest;

import java.util.List;

public interface TimeTablingClassService {
    List<TimeTablingClassSegment> createClassSegment(ModelInputCreateClassSegment I);

    List<TimeTablingClassSegment> createClassSegmentForSummerSemester(ModelInputCreateClassSegment I);

    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(String semester, Long groupId, Long versionId);

    List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(List<Long> classIds, Long versionId);

    List<ModelResponseTimeTablingClass> findAll();

    List<ModelResponseTimeTablingClass> findAllBySemester(String semester);

    List<ModelResponseTimeTablingClass> findAllByClassIdIn(List<Long> classIds);

    int removeClassSegment(ModelInputCreateClassSegment I);

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
}