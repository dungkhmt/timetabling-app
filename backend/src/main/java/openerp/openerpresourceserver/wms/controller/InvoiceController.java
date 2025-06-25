package openerp.openerpresourceserver.wms.controller;

import lombok.RequiredArgsConstructor;
import openerp.openerpresourceserver.wms.dto.ApiResponse;
import openerp.openerpresourceserver.wms.dto.invoice.InvoiceDTO;
import openerp.openerpresourceserver.wms.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/import-inbound/{shipmentId}")
    public ApiResponse<Void> importInBound(@PathVariable String shipmentId, Principal principal) {
        return invoiceService.importInBound(shipmentId, principal.getName());
    }

    @GetMapping("/shipment/{shipmentId}")
    public ApiResponse<InvoiceDTO> getInvoiceByShipmentId(@PathVariable String shipmentId) {
        return invoiceService.getInvoiceByShipmentId(shipmentId);
    }

    @GetMapping("/{invoiceId}")
    public ApiResponse<InvoiceDTO> getInvoiceById(@PathVariable String invoiceId) {
        return invoiceService.getInvoiceById(invoiceId);
    }
}
