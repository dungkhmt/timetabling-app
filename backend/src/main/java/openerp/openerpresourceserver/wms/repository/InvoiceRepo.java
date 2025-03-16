package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepo extends JpaRepository<Invoice, String> {
}
