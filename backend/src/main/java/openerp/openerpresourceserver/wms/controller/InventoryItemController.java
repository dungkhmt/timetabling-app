package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.Pagination;
import openerp.openerpresourceserver.wms.dto.inventoryItem.InventoryItemForOrderRes;
import openerp.openerpresourceserver.wms.dto.inventoryItem.InventoryProductRes;
import openerp.openerpresourceserver.wms.service.InventoryItemService;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/inventory-item")
@RestController
public class InventoryItemController {
    private final InventoryItemService inventoryItemService;

    @GetMapping("/for-outbound/{orderId}")
    public ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItemsForOutBound(@RequestParam int page,
                                                                                       @RequestParam int limit,
                                                                                       @PathVariable String orderId) {
        return inventoryItemService.getInventoryItemsForOutBound(page, limit, orderId);
    }

    @GetMapping("/for-inbound/{orderId}")
    public ApiResponse<Pagination<InventoryItemForOrderRes>> getInventoryItemsForInBound(@RequestParam int page,
                                                                                          @RequestParam int limit,
                                                                                          @PathVariable String orderId) {
        return inventoryItemService.getInventoryItemsForInBound(page, limit, orderId);
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<Pagination<InventoryProductRes>> getInventoryItemByProductId(@RequestParam int page,
                                                                                    @RequestParam int limit,
                                                                                    @PathVariable String productId) {
        return inventoryItemService.getInventoryItemByProductId(page, limit, productId);
    }


}
