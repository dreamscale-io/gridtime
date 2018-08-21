package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewIFMBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.FLOW_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class FlowResource {

    @PostMapping(ResourcePaths.BATCH_PATH)
    public void addIFMBatch(@RequestBody NewIFMBatch batch) {
        log.info("addIfmBatch, batch={}", batch);
    }

}
