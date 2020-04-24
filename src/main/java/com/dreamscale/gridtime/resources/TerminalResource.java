package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.core.capability.integration.FlowPublisher;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.GridTableResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.TERMINAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TerminalResource {

    @Autowired
    FlowPublisher flowService;

    @Autowired
    OrganizationCapability organizationCapability;


    @PostMapping(ResourcePaths.GRID_PATH + ResourcePaths.START_PATH )
    public void startGrid() {

    }

    @PostMapping(ResourcePaths.GRID_PATH + ResourcePaths.STOP_PATH )
    public void stopGrid() {

    }

    @GetMapping(ResourcePaths.GRID_PATH + ResourcePaths.STATUS_PATH)
    public GridTableResults getGridSummaryStatus() {
        return null;
    }


    @GetMapping(ResourcePaths.GRID_PATH + ResourcePaths.TOP_PATH)
    public GridTableResults topProcesses() {
        return null;
    }

    @GetMapping(ResourcePaths.GRID_PATH + ResourcePaths.TOP_PATH + ResourcePaths.TORCHIE_PATH)
    public GridTableResults topTorchieProcesses() {
        return null;
    }

    @GetMapping(ResourcePaths.GRID_PATH + ResourcePaths.TOP_PATH + ResourcePaths.PLEXER_PATH)
    public GridTableResults topPlexerProcesses() {
        return null;
    }

    @GetMapping(ResourcePaths.GRID_PATH + ResourcePaths.TOP_PATH + ResourcePaths.SYSTEM_PATH)
    public GridTableResults topSystemProcesses() {
        return null;
    }

    @PostMapping(ResourcePaths.GRID_PATH + ResourcePaths.PROCESS_PATH + "/{procId}" + ResourcePaths.HALT_PATH)
    public GridTableResults haltProcess() {
        return null;
    }

    @PostMapping(ResourcePaths.GRID_PATH + ResourcePaths.PROCESS_PATH + "/{procId}" + ResourcePaths.RESUME_PATH)
    public GridTableResults resumeProcess() {
        return null;
    }

    //terminal/grid/top


    //grid start
    //grid stop
    //grid status

    //grid top
    //grid top torchie
    //grid top system
    //grid top plexer

    //grid proc kill {procId}
    //grid proc halt {procId}
    //grid proc resume

//
//    @GetMapping(ResourcePaths.CALENDAR_PATH )
//    public void getCalendarJobStatus() {
//
//       //TODO return how many tiles are generated on what schedule, how far ahead into the future?
//        //how early is our calendar data...?  When does this run?
//
//        //calendar tile generator job configuration
//
//        //how much data you have for your tilesd
//
//        //its a watch dog program...
//
//        //gives you a report of your bogs
//
//        //running / or not
//    }
//
//    @PostMapping(ResourcePaths.CALENDAR_PATH )
//    public void configureCalendarJob() {
//
//        //TODO post configuration state.
//        //how early is our calendar data...?  When does this run?
//
//        //calendar tile generator job configuration
//    }
//
//    @GetMapping(ResourcePaths.WATCH_PATH )
//    public void getWatchDogStatus() {
//
//        //TODO return how many tiles are generated on what schedule, how far ahead into the future?
//        //how early is our calendar data...?  When does this run?
//
//        //calendar tile generator job configuration
//
//        //how much data you have for your tiles
//
//        //its a watch dog program...
//
//        //gives you a report of your bogs
//
//        //running / or not
//    }
//
//    @PostMapping(ResourcePaths.WATCH_PATH )
//    public void configureWatchJob() {
//
//        //TODO post configuration state.
//        //watch which #tags?
//
//        //watch which thresholds?
//
//        //if everyone is all WTF_ROOM all the time, when do you want to know?
//        // How many WTFs/time before you pull the andon cord?
//    }
//
//
//    @PostMapping(ResourcePaths.WATCH_PATH + ResourcePaths.MY_PATH + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.WORD_PATH + "/{tagName}"  )
//    public void watchThisOrganizationTagNow(@PathVariable("tagName") String tagName) {
//
//    }
//
//    @PostMapping(ResourcePaths.WATCH_PATH + ResourcePaths.MY_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.WORD_PATH + "/{tagName}"  )
//    public void watchThisTeamTagNow(@PathVariable("tagName") String tagName) {
//
//    }
//
//    @PostMapping(ResourcePaths.WATCH_PATH + ResourcePaths.MY_PATH + ResourcePaths.WORD_PATH + "/{tagName}"  )
//    public void watchMyTagNow(@PathVariable("tagName") String tagName) {
//
//    }
//
//    @PostMapping(ResourcePaths.CLEAR_PATH + ResourcePaths.MY_PATH + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.WORD_PATH + "/{tagName}"  )
//    public void clearThisOrganizationTagNow(@PathVariable("tagName") String tagName) {
//
//    }
//
//    @PostMapping(ResourcePaths.CLEAR_PATH + ResourcePaths.MY_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.WORD_PATH + "/{tagName}"  )
//    public void clearThisTeamTagNow(@PathVariable("tagName") String tagName) {
//
//    }
//
//    @PostMapping(ResourcePaths.CLEAR_PATH + ResourcePaths.MY_PATH + ResourcePaths.WORD_PATH + "/{tagName}"  )
//    public void clearMyTagNow(@PathVariable("tagName") String tagName) {
//
//    }
//
//    @PostMapping(ResourcePaths.LISTEN_PATH + ResourcePaths.FOR_PATH + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.THRESHOLD_PATH  )
//    public void listenForThisOrganizationThreshold() {
//
//    }
//
//    @PostMapping(ResourcePaths.LISTEN_PATH + ResourcePaths.FOR_PATH + ResourcePaths.TEAM_PATH  + ResourcePaths.THRESHOLD_PATH  )
//    public void listenForTeamThreshold() {
//
//        //once up a listener, it sends you notifications in realtime over talk
//
//    }
//
//    @PostMapping(ResourcePaths.LISTEN_PATH + ResourcePaths.FOR_PATH +  ResourcePaths.THRESHOLD_PATH  )
//    public void listenForThreshold() {
//
//        //once up a listener, it sends you notifications in realtime over talk
//    }
//
//
//    @PostMapping(ResourcePaths.RUN_PATH + ResourcePaths.PROGRAM_PATH + "/{programName}"  )
//    public void runProgram(@PathVariable("programName") String programName) {
//
//    }
//
//    @PostMapping(ResourcePaths.RUN_PATH + ResourcePaths.CMD_PATH + "/{cmdName}"  )
//    public void runCommand(@PathVariable("cmdName") String cmdName) {
//
//    }


    //start torchies for team

    // /torchie/job/team/{id}/transition/start 1:11 11:11

//    @PreAuthorize("hasRole('ROLE_USER')")
//    @PostMapping(ResourcePaths.TEAM_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
//    public TorchieJobStatus startTeamTorchieFeed(@PathVariable("id") String teamIdStr) {
//
//        UUID teamId = UUID.fromString(teamIdStr);
//
//        return torchieExecutorService.startTeamTorchie(teamId);
//    }
//
//    // /torchie/job/member/{id}/transition/start 1:11 11:11
//
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @PostMapping(ResourcePaths.MEMBER_PATH + "/{id}"+ ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
//    public CircuitMonitor startMemberTorchieFeed(@PathVariable("id") String memberIdStr) {
//
//        RequestContext context = RequestContext.get();
//
//        OrganizationMemberEntity memberEntity = organizationService.getActiveMembership(context.getRootAccountId());
//
//        UUID memberId = UUID.fromString(memberIdStr);
//        organizationService.validateMemberWithinOrgByMemberId(memberEntity.getOrganizationId(), memberId);
//
//        return torchieService.startMemberTorchie(memberId);
//
//    }



}
