package openerp.openerpresourceserver.wms.mapper;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ObjectMapper {
    private final ModelMapper  modelMapper;

    public <D, T> D convertToDto(T entity, Class<D> outClass) {
        return modelMapper.map(entity, outClass);
    }

    public <D, T> T convertToEntity(D dto, Class<T> outClass) {
        return modelMapper.map(dto, outClass);
    }
}
