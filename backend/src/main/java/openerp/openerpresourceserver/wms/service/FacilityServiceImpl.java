package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Facility;
import openerp.openerpresourceserver.wms.repository.FacilityRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FacilityServiceImpl implements FacilityService{
    private final FacilityRepo facilityRepo;
    @Override
    public ApiResponse<Pagination<Facility>> getFacilities(Integer page, Integer limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<Facility> facilities = facilityRepo.findAll(pageRequest);
        return ApiResponse.<Pagination<Facility>>builder()
                .code(200)
                .message("Success")
                .data(Pagination.<Facility>builder()
                        .data(facilities.getContent())
                        .page(facilities.getNumber())
                        .size(facilities.getSize())
                        .total(facilities.getTotalElements())
                        .totalPages(facilities.getTotalPages())
                        .build())
                .build();
    }
}
