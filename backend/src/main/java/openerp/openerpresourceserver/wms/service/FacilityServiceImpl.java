package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.algorithm.SnowFlakeIdGenerator;
import openerp.openerpresourceserver.wms.constant.enumrator.EntityType;
import openerp.openerpresourceserver.wms.constant.enumrator.FacilityStatus;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.facility.CreateFacilityReq;
import openerp.openerpresourceserver.wms.dto.facility.FacilityGetListRes;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static openerp.openerpresourceserver.wms.constant.Constants.ADDRESS_ID_PREFIX;
import static openerp.openerpresourceserver.wms.constant.Constants.FACILITY_ID_PREFIX;

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
            newFacility.setId(SnowFlakeIdGenerator.getInstance().nextId(FACILITY_ID_PREFIX));
        }
        newAddress.setId(SnowFlakeIdGenerator.getInstance().nextId(ADDRESS_ID_PREFIX));

        newFacility.setStatusId(FacilityStatus.ACTIVE.name());
        newFacility.setCurrentAddressId(newAddress.getId());

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
    public ApiResponse<Pagination<FacilityGetListRes>> getFacilities(Integer page, Integer limit, FacilityGetListFilter filters) {
        var pageRequest = CommonUtil.getPageRequest(page, limit);
        var facilitySpec = new FacilitySpecification(filters);
        var facilityPage = facilityRepo.findAll(facilitySpec, pageRequest);

        var facilityIds = facilityPage.getContent().stream()
                .map(Facility::getId)
                .toList();

        var addresses = addressRepo.findAllByEntityIdInAndEntityType(facilityIds, EntityType.FACILITY.name());

        var addressMap = addresses.stream()
                .collect(
                        Collectors.toMap(Address::getEntityId, Function.identity())
                );

        var facilityGetListRes = facilityPage.<FacilityGetListRes>getContent().
                stream().map(
                        facility ->{
                            var facilityRes =  generalMapper.convertToDto(facility, FacilityGetListRes.class);
                            var address = addressMap.get(facility.getId());
                            facilityRes.setLatitude(address.getLatitude());
                            facilityRes.setLongitude(address.getLongitude());
                            facilityRes.setFullAddress(address.getFullAddress());
                            return facilityRes;
                        }
                ).toList();

        var pagination = Pagination.<FacilityGetListRes>builder()
                .data(facilityGetListRes)
                .page(page)
                .size(limit)
                .totalElements(facilityPage.getTotalElements())
                .totalPages(facilityPage.getTotalPages())
                .build();

        return ApiResponse.<Pagination<FacilityGetListRes>>builder()
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
