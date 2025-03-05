package openerp.openerpresourceserver.wms.service;

import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.entity.Customer;

import java.util.List;

public interface CustomerService {
    ApiResponse<Pagination<Customer>> getCustomers(Integer page, Integer limit);
}
