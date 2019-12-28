package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.router.TalkConnectionStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.ROUTER_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class RouterResource {


    //TODO this needs to be an internal only API, moved to it's own deploy as part of GridTime Router

    @PostMapping(ResourcePaths.CONFIRM_PATH + ResourcePaths.CONNECTION_PATH + "/{id}")
    public void confirmConnectionStatus(@PathVariable("id") String connectionId, TalkConnectionStatusDto connectionStatusDto) {

        //needs to trigger resync updates of all the Rooms for this particular person,
        // and resync (join) room connections

        //Cypher your stuff.  Neo4J.
    }
}

