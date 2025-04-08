package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.ModelInputComputeClassCluster;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.RoomOccupationWithModuleCode;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.UpdateGeneralClassRequest;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.GeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;


import java.util.List;

public interface TimeTablingClassService {
    List<TimeTablingClassSegment> createClassSegment(ModelInputCreateClassSegment I);

    List<TimeTablingClassSegment> createClassSegmentForSummerSemester(ModelInputCreateClassSegment I);

    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(String semester, Long groupId);

    List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(List<Long> classIds);

    List<ModelResponseTimeTablingClass> findAll();

    List<ModelResponseTimeTablingClass> findAllBySemester(String semester);

    List<ModelResponseTimeTablingClass> findAllByClassIdIn(List<Long> classIds);

    int removeClassSegment(ModelInputCreateClassSegment I);

    int deleteByIds(List<Long> ids);

    TimeTablingClass updateClass(UpdateGeneralClassRequest request);

    public List<ModelResponseTimeTablingClass> getSubClass(Long id);

    public List<ModelResponseTimeTablingClass> clearTimeTable(List<String> ids);

    public List<RoomOccupationWithModuleCode> getRoomOccupationsBySemesterAndWeekIndex(String semester, int weekIndex);

    public ModelResponseTimeTablingClass splitNewClassSegment(Long classId, Long parentClassSegmentId, Integer duration);

    public int computeClassCluster(ModelInputComputeClassCluster I);

    public List<ModelResponseTimeTablingClass> getClassByCluster(Long clusterId);
}