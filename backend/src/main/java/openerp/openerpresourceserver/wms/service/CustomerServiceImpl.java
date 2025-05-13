package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.constant.enumrator.CustomerStatus;
import openerp.openerpresourceserver.wms.constant.enumrator.EntityType;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.customer.CreateCustomerReq;
import openerp.openerpresourceserver.wms.entity.Address;
import openerp.openerpresourceserver.wms.entity.Customer;
import openerp.openerpresourceserver.wms.mapper.GeneralMapper;
import openerp.openerpresourceserver.wms.repository.AddressRepo;
import openerp.openerpresourceserver.wms.repository.CustomerRepo;
import openerp.openerpresourceserver.wms.util.CommonUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepo customerRepo;
    private final AddressRepo addressRepo;
    private final GeneralMapper generalMapper;
    @Override
    public ApiResponse<Pagination<Customer>> getCustomers(Integer page, Integer limit) {
        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<Customer> customers = customerRepo.findAll(pageRequest);

        return ApiResponse.<Pagination<Customer>>builder()
                .code(200)
                .message("Success")
                .data(Pagination.<Customer>builder()
                        .data(customers.getContent())
                        .page(customers.getNumber())
                        .size(customers.getSize())
                        .totalElements(customers.getTotalElements())
                        .totalPages(customers.getTotalPages())
                        .build())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<Void> createCustomer(CreateCustomerReq req) {
        var newCustomer = generalMapper.convertToEntity(req, Customer.class);
        var newAddress = generalMapper.convertToEntity(req.getAddress(), Address.class);

        if(Objects.isNull(req.getId())) {
            newCustomer.setId(CommonUtil.getUUID());
        }

        newCustomer.setStatusId(CustomerStatus.ACTIVE.name());
        newCustomer.setAddress(newAddress.getFullAddress());

        newAddress.setId(CommonUtil.getUUID());
        newAddress.setEntityId(newCustomer.getId());
        newAddress.setEntityType(EntityType.CUSTOMER.name());

        customerRepo.save(newCustomer);
        addressRepo.save(newAddress);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("Created customer successfully")
                .build();
    }
}
