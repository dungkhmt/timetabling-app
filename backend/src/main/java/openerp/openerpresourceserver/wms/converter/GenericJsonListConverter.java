package openerp.openerpresourceserver.wms.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;

import java.util.ArrayList;
import java.util.List;

public class GenericJsonListConverter<T> implements AttributeConverter<List<T>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TypeReference<List<T>> typeReference;

    public GenericJsonListConverter(TypeReference<List<T>> typeReference) {
        this.typeReference = typeReference;
    }

    @Override
    public String convertToDatabaseColumn(List<T> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<T> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to list", e);
        }
    }
}
