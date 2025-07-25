package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateTimeTablingClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.CreateSubClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.UpdateTimeTablingClassFromPlanDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.BulkMakeGeneralClassDto;
import openerp.openerpresourceserver.generaltimetabling.model.dto.request.general.CreateSingleClassOpenDto;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.PlanGeneralClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClass;

import java.util.List;

public interface PlanGeneralClassService {

    int clearPlanClass(String semester);

    TimeTablingClass makeClass(CreateTimeTablingClassDto request, Long groupId);

    List<TimeTablingClass> makeSubClass(CreateSubClassDto request);

    List<PlanGeneralClass> getAllPlanClasses(String semester);

    List<PlanGeneralClass> getOpenedClassPlans(Long batchId);


    List<TimeTablingClass> getClassOfPlan(Long planClassId);

    TimeTablingClass updateTimeTablingClass(TimeTablingClass generalClass);

    List<TimeTablingClass> generateTimeTablingClassFromPlan(UpdateTimeTablingClassFromPlanDto request);

    List<TimeTablingClass> createMultipleClasses(BulkMakeGeneralClassDto request);

    PlanGeneralClass updatePlanClass(PlanGeneralClass planClass);

    List<PlanGeneralClass> deleteClassesByIds(List<Long> planClassIds);

    PlanGeneralClass deleteClassById(Long planClassId);

    PlanGeneralClass createSingleClass(CreateSingleClassOpenDto planClass);

    PlanGeneralClass createClassOpenningPlan(String userId, CreateSingleClassOpenDto planClass);

    PlanGeneralClass updateClassOpenningPlan(String userId, CreateSingleClassOpenDto planClass);

}
