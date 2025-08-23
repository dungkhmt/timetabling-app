package openerp.openerpresourceserver.teacherassignment.service.impl;

import openerp.openerpresourceserver.teacherassignment.model.dto.OpenedClassDto;
import openerp.openerpresourceserver.teacherassignment.model.entity.OpenedClass;
import openerp.openerpresourceserver.teacherassignment.repo.OpenedClassRepo;
import openerp.openerpresourceserver.teacherassignment.service.OpenedClassService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpenedClassImpl implements OpenedClassService {

    @Autowired
    private OpenedClassRepo openedClassRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<OpenedClassDto> findAllBySemester(String semester) {
        List<OpenedClass> openedClasses = openedClassRepo.findAllBySemester(semester);
        return openedClasses.stream()
                .map(openedClass -> modelMapper.map(openedClass, OpenedClassDto.class))
                .collect(Collectors.toList());

    }

    @Override
    public List<String> getAllDistinctSemester() {
        return openedClassRepo.getDistinctInSemester();
    }
}
