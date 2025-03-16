package openerp.openerpresourceserver.wms.repository;

import openerp.openerpresourceserver.wms.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceItemRepo extends JpaRepository<InvoiceItem, String> {
}
