package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.customer.CreateCustomerReq;
import openerp.openerpresourceserver.wms.entity.Customer;

public interface CustomerService {
    ApiResponse<Pagination<Customer>> getCustomers(Integer page, Integer limit);

    ApiResponse<Void> createCustomer(CreateCustomerReq customer);
}
