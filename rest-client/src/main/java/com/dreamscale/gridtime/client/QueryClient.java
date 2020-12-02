package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.api.query.QueryInputDto;
import com.dreamscale.gridtime.api.query.TargetInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface QueryClient {


    @RequestLine("GET " + ResourcePaths.QUERY_PATH + ResourcePaths.TOP_PATH + ResourcePaths.WTF_PATH +  "?scope={scope}")
    GridTableResults getTopWTFsForTimeScope(@Param("scope") String timeScope);

    @RequestLine("GET " + ResourcePaths.QUERY_PATH + ResourcePaths.TOP_PATH + ResourcePaths.WTF_PATH +  "?scope={scope}&target_type={targetType}&target_name={targetName}")
    GridTableResults getTopWTFsForTimeScopeAndTarget(@Param("scope") String timeScope, @Param("targetType") String targetType, @Param("targetName") String targetName);


    @RequestLine("POST " + ResourcePaths.QUERY_PATH + ResourcePaths.TARGET_PATH)
    SimpleStatusDto setQueryTarget(TargetInputDto targetInputDto);

}


