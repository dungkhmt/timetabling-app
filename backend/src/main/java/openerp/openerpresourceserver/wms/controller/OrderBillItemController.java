package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.filter.OrderItemBillingGetListFilter;
import openerp.openerpresourceserver.wms.dto.orderBillItem.OrderItemBillingGetListRes;
import openerp.openerpresourceserver.wms.service.OrderBillItemService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/order-bill-item")
@RestController
public class OrderBillItemController {
    private final OrderBillItemService orderBillItemService;

    @PostMapping("/get-all")
    public ApiResponse<Pagination<OrderItemBillingGetListRes>> getAll(
            @RequestParam int page,@RequestParam  int limit,
            @RequestBody OrderItemBillingGetListFilter filters) {
        return orderBillItemService.getAll(page, limit, filters);
    }
}
