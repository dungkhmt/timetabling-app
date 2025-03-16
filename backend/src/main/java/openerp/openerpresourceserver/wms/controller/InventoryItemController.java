package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.saleOrder.InventoryItemForOrderRes;
import openerp.openerpresourceserver.wms.service.InventoryItemService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/inventory-item")
@RestController
public class InventoryItemController {
    private final InventoryItemService inventoryItemService;

    @GetMapping("/for-order/{orderId}")
    public ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItemsForOrder(@RequestParam int page,
                                                                                       @RequestParam int limit,
                                                                                       @PathVariable String orderId) {
        return inventoryItemService.getInventoryItems(page, limit, orderId);
    }


}
