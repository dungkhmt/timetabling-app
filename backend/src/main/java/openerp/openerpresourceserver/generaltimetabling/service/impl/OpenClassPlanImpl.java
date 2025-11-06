package openerp.openerpresourceserver.generaltimetabling.service.impl;

import openerp.openerpresourceserver.generaltimetabling.model.entity.OpenClassPlan;
import openerp.openerpresourceserver.generaltimetabling.repo.OpenClassPlanRepo;
import openerp.openerpresourceserver.generaltimetabling.service.OpenClassPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpenClassPlanImpl implements OpenClassPlanService {

    @Autowired
    private OpenClassPlanRepo openClassPlanRepo;

    @Override
    public List<OpenClassPlan> getAllBySemester(String semester) {
        return openClassPlanRepo.findAllBySemester(semester);
    }

    @Override
    public List<String> getAllSemesters() {
        return openClassPlanRepo.getAllSemesters();
    }
}
