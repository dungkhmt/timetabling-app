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
    @PutMapping("/export-outbound/{shipmentId}")
    public ApiResponse<Void> exportOutBound(@PathVariable String shipmentId, Principal principal) {
        return invoiceService.exportOutBound(shipmentId, principal.getName());
    }

    @PutMapping("/export-inbound/{shipmentId}")
    public ApiResponse<Void> importInBound(@PathVariable String shipmentId, Principal principal) {
        return invoiceService.importInBound(shipmentId, principal.getName());
    }
}
