package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.service.InvoiceService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RequiredArgsConstructor
@RequestMapping("/invoice")
@RestController
public class InvoiceController {
    private final InvoiceService invoiceService;
    @PutMapping("/export/{shipmentId}")
    public ApiResponse<Void> exportShipment(@PathVariable String shipmentId, Principal principal) {
        return invoiceService.exportShipment(shipmentId, principal.getName());
    }
}
