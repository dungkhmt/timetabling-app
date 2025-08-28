package openerp.openerpresourceserver.teacherassignment.service.impl;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OpenedClassServiceImpl implements OpenedClassService {

    @Autowired
    private OpenedClassRepo openedClassRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<OpenedClassDto> findAllBySemester(String semester) {
//        List<OpenedClass> openedClasses = openedClassRepo.findAllBySemester(semester);
        List<OpenedClass> openedClasses = openedClassRepo.findAllBySemesterWithDto(semester);

//        log.info(" --- Retrieved OpenedClasses for semester: {} ---", semester);
//
//        openedClasses.forEach(openedClass -> {
//            // Kiểm tra nếu batchClass khác null
//            if (openedClass.getBatchClass() != null) {
//                log.info("OpenedClass ID: {}, Course Name: {}, Batch Class: {}",
//                        openedClass.getClassId(),
//                        openedClass.getCourseId() ,
//                        openedClass.getBatchClass() // hoặc getId() tùy thuộc vào trường bạn muốn log
//                );
//            }
//        });

        return openedClasses.stream()
                .map(openedClass -> modelMapper.map(openedClass, OpenedClassDto.class))
                .collect(Collectors.toList());

    }

    @Override
    public List<String> getAllDistinctSemester() {
        return openedClassRepo.getDistinctInSemester();
    }

    @Override
    public List<OpenedClassDto> findAllByBatchId(Long batchId) {
        List<OpenedClass> openedClasses = openedClassRepo.findAllByBatchIdWithDto(batchId);

        return openedClasses.stream()
                .map(openedClass -> modelMapper.map(openedClass, OpenedClassDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OpenedClass> findAllBySemesterAndCourseId(String semester, String courseId) {
        return openedClassRepo.findAllBySemesterAndCourseId(semester, courseId);
    }
}
