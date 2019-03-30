package com.dreamscale.ideaflow.core.mapper;

import org.dozer.Mapper;

import java.util.List;

public class DtoEntityMapper<DTO_TYPE, ENTITY_TYPE> {

    private Class<DTO_TYPE> dtoType;
    private Class<ENTITY_TYPE> entityType;
    private ValueObjectMapper mapper;

    public DtoEntityMapper(Mapper mapper, Class<DTO_TYPE> dtoType, Class<ENTITY_TYPE> entityType) {
        this.dtoType = dtoType;
        this.entityType = entityType;
        this.mapper = new ValueObjectMapper(mapper);
    }

    public ENTITY_TYPE toEntity(DTO_TYPE api) {
        ENTITY_TYPE entity = mapper.mapIfNotNull(api, entityType);
        if (entity != null) {
            onEntityConversion(api, entity);
        }
        return entity;
    }

    protected void onEntityConversion(DTO_TYPE source, ENTITY_TYPE target) {
        // subclass to override if custom mapping is required
    }

    public List<ENTITY_TYPE> toEntityList(Iterable<DTO_TYPE> apiList) {
        return mapper.mapList(apiList, this::toEntity);
    }

    public DTO_TYPE toApi(ENTITY_TYPE entity) {
        DTO_TYPE api = mapper.mapIfNotNull(entity, dtoType);
        if (api != null) {
            onApiConversion(entity, api);
        }
        return api;
    }

    protected void onApiConversion(ENTITY_TYPE source, DTO_TYPE target) {
        // subclass to override if custom mapping is required
    }

    public List<DTO_TYPE> toApiList(Iterable<ENTITY_TYPE> entityList) {
        return mapper.mapList(entityList, this::toApi);
    }

}