package com.dreamscale.gridtime.core.mapper;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Component
public class MapperFactory {

    private List<String> mappingConfigFileList = Collections.singletonList("dozerConfig.xml");

    private DozerBeanMapper sharedMapper;


    @PostConstruct
    void initMapper() {
        DozerBeanMapper mapper = new DozerBeanMapper();
        mapper.setMappingFiles(mappingConfigFileList);
        this.sharedMapper = mapper;
    }

    public <A, E> DtoEntityMapper<A, E> createDtoEntityMapper(Class<A> dtoType, Class<E> entityType) {
        return new DtoEntityMapper<>(sharedMapper, dtoType, entityType);
    }

}