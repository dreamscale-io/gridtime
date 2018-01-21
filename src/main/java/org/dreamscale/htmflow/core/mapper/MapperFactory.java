package org.dreamscale.htmflow.core.mapper;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MapperFactory {

    private List<String> mappingConfigFileList = Collections.singletonList("dozerConfig.xml");

    public Mapper createMapper() {
        DozerBeanMapper mapper = new DozerBeanMapper();
        mapper.setMappingFiles(mappingConfigFileList);
        return mapper;
    }

    public <A, E> DtoEntityMapper<A, E> createDtoEntityMapper(Class<A> dtoType, Class<E> entityType) {
        Mapper mapper = createMapper();
        return new DtoEntityMapper<>(mapper, dtoType, entityType);
    }

}