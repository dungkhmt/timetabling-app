package openerp.openerpresourceserver.wms.service;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Customer;
import openerp.openerpresourceserver.wms.repository.CustomerRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepo customerRepo;
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
                        .total(customers.getTotalElements())
                        .totalPages(customers.getTotalPages())
                        .build())
                .build();
    }
}
