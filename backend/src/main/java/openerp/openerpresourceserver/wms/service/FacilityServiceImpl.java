package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.EntityType;
import openerp.openerpresourceserver.wms.constant.enumrator.FacilityStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.facility.CreateFacilityReq;
import openerp.openerpresourceserver.wms.dto.filter.FacilityGetListFilter;
import openerp.openerpresourceserver.wms.entity.Address;
import openerp.openerpresourceserver.wms.entity.Facility;
import openerp.openerpresourceserver.wms.exception.DataNotFoundException;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.AddressRepo;
import openerp.openerpresourceserver.wms.repository.FacilityRepo;
import openerp.openerpresourceserver.wms.repository.specification.FacilitySpecification;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class FacilityServiceImpl implements FacilityService{
    private final FacilityRepo facilityRepo;
    private final GeneralMapper generalMapper;
    private final AddressRepo addressRepo;
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
                        .totalElements(facilities.getTotalElements())
                        .totalPages(facilities.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public ApiResponse<Void> createFacility(CreateFacilityReq req) {
        var newFacility = generalMapper.convertToEntity(req, Facility.class);
        var newAddress = generalMapper.convertToEntity(req.getAddress(), Address.class);

        if(Objects.isNull(req.getId())) {
            newFacility.setId(CommonUtil.getUUID());
        }

        newFacility.setStatusId(FacilityStatus.ACTIVE.name());
        newFacility.setAddress(newAddress.getFullAddress());

        newAddress.setId(CommonUtil.getUUID());
        newAddress.setEntityId(newFacility.getId());
        newAddress.setEntityType(EntityType.FACILITY.name());

        facilityRepo.save(newFacility);
        addressRepo.save(newAddress);

        return ApiResponse.<Void>builder()
                .code(201)
                .message("Facility created successfully")
                .build();
    }

    @Override
    public ApiResponse<Pagination<Facility>> getFacilities(Integer page, Integer limit, FacilityGetListFilter filters) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var facilitySpec = new FacilitySpecification(filters);
        var facilityPage = facilityRepo.findAll(facilitySpec, pageRequest);

        var pagination = Pagination.<Facility>builder()
                .data(facilityPage.getContent())
                .page(page)
                .size(limit)
                .totalElements(facilityPage.getTotalElements())
                .totalPages(facilityPage.getTotalPages())
                .build();

        return ApiResponse.<Pagination<Facility>>builder()
                .code(200)
                .message("Get facilities successfully")
                .data(pagination)
                .build();
    }

    @Override
    public ApiResponse<Facility> getFacilityById(String id) {
        return ApiResponse.<Facility>builder()
                .code(200)
                .message("Get facility successfully")
                .data(facilityRepo.findById(id).orElseThrow(
                        () -> new DataNotFoundException("Facility not found with id: " + id)
                ))
                .build();
    }
}
