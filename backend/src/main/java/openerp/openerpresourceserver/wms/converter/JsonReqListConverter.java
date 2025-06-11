package openerp.openerpresourceserver.wms.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;
import openerp.openerpresourceserver.wms.dto.JsonReq;

import java.util.List;

@Converter(autoApply = false)
public class JsonReqListConverter extends GenericJsonListConverter<JsonReq> {
    public JsonReqListConverter() {
        super(new TypeReference<List<JsonReq>>() {});
    }
}
