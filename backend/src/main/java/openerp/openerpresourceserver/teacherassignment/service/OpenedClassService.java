package openerp.openerpresourceserver.teacherassignment.service;

import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;

import java.util.List;

public interface OpenedClassService {
    List<OpenedClassDto> findAllBySemester(String semester);

    List<String> getAllDistinctSemester();

    List<OpenedClassDto> findAllByBatchId(Long batchId);
}
