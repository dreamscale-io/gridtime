package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import com.dreamscale.htmflow.core.domain.TaskEntity;
import com.dreamscale.htmflow.core.domain.TaskRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.JournalService;
import com.dreamscale.htmflow.core.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.JOURNAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JournalResource {

    @Autowired
    private JournalService journalService;

    @PostMapping(ResourcePaths.CHUNK_PATH)
    ChunkEventOutputDto createChunkEvent(@RequestBody ChunkEventInputDto chunkEventInput) {
        RequestContext context = RequestContext.get();
        return journalService.createChunkEvent(context.getMasterAccountId(), chunkEventInput);
    }

    @GetMapping(ResourcePaths.CHUNK_PATH)
    List<ChunkEventOutputDto> getChunks(@RequestParam("from_date") String fromDate, @RequestParam("to_date") String toDate) {
        RequestContext context = RequestContext.get();

        return journalService.getChunksWithinRange(context.getMasterAccountId(), null, null);
    }


    @GetMapping(ResourcePaths.CHUNK_PATH + ResourcePaths.RECENT_PATH)
    List<ChunkEventOutputDto> getRecentChunksForMember(@RequestParam("member") Optional<String> memberId) {
        RequestContext context = RequestContext.get();
        if (memberId.isPresent()) {
            return journalService.getRecentChunksForMember(context.getMasterAccountId(), UUID.fromString(memberId.get()));
        } else {
            return journalService.getRecentChunks(context.getMasterAccountId());
        }
    }

}
