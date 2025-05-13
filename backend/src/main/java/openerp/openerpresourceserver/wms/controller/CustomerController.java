package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.customer.CreateCustomerReq;
import openerp.openerpresourceserver.wms.dto.filter.CustomerGetListFilter;
import openerp.openerpresourceserver.wms.entity.Customer;
import openerp.openerpresourceserver.wms.service.CustomerService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/customer")
public class CustomerController {
    private final CustomerService customerService;
    // this function to fetch more customer
    @GetMapping("/get-more")
    public ApiResponse<Pagination<Customer>> getCustomers(@RequestParam Integer page, @RequestParam Integer limit) {
        return customerService.getCustomers(page, limit);
    }

    @PostMapping("/create")
    public ApiResponse<Void> createCustomer(@RequestBody CreateCustomerReq customer) {
        return customerService.createCustomer(customer);
    }

    @PostMapping("/get-all")
    public ApiResponse<Pagination<Customer>> getCustomers(@RequestParam Integer page, @RequestParam Integer limit, @RequestBody
    CustomerGetListFilter filters) {
        return customerService.getCustomers(page, limit, filters);
    }

    @GetMapping("/details/{id}")
    public ApiResponse<Customer> getCustomerDetails(@PathVariable String id) {
        return customerService.getCustomerById(id);
    }
}
