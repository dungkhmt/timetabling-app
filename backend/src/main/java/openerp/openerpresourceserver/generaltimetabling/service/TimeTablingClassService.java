package openerp.openerpresourceserver.generaltimetabling.service;

import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelInputCreateClassSegment;
import openerp.openerpresourceserver.generaltimetabling.model.dto.ModelResponseTimeTablingClass;
import openerp.openerpresourceserver.generaltimetabling.model.entity.general.TimeTablingClassSegment;


import java.util.List;

public interface TimeTablingClassService {
    List<TimeTablingClassSegment> createClassSegment(ModelInputCreateClassSegment I);

    public List<ModelResponseTimeTablingClass> getTimeTablingClassDtos(String semester, Long groupId);

    int removeClassSegment(ModelInputCreateClassSegment I);
}
