package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.entity.OpenClassPlan;

import java.util.List;

public interface OpenClassPlanService {

    List<OpenClassPlan> getAllBySemester(String semester);

    List<String> getAllSemesters();
}
