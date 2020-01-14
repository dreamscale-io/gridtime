package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.LearningCircuitService;
import com.dreamscale.gridtime.core.service.OrganizationService;
import feign.RequestLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.CIRCUIT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class CircuitResource {

    @Autowired
    OrganizationService organizationService;

    @Autowired
    LearningCircuitService learningCircuitService;

    /**
     * Pulls the "Global Andon Cord" to generate a new "Twilight Learning Circuit" that can be joined by
     * anyone in the organization that wants to collaborate, provided permission to join is granted by the owner
     * of the Circuit.  Existance of this new Circuit will be broadcast to listeners.
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH)
    public LearningCircuitDto createLearningCircuitForWTF() {
        RequestContext context = RequestContext.get();
        log.info("createLearningCircuitForWTF, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.createNewLearningCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Pulls the "Global Andon Cord" to generate a new "Twilight Learning Circuit" with the specified name,
     * (or throws an error if name already exists.
     * This new Circuit can be joined by anyone in the organization that wants to collaborate, provided permission to join
     * is granted by the owner of the Circuit.  Existance of this new Circuit will be broadcast to listeners.
     *
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}")
    public LearningCircuitDto createLearningCircuitForWTFWithCustomName(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("createLearningCircuitForWTFWithCustomName, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.createNewLearningCircuitWithCustomName(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Starts the retrospective feed for the provided WTF_ROOM Circuit
     *
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RETRO_PATH)
    public LearningCircuitDto startRetroForWTF(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("startRetroForWTF, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.startRetroForCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.WTF_PATH + "/{name}" )
    LearningCircuitWithMembersDto getCircuitWithAllDetails(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("getCircuitWithAllDetails, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.getCircuitWithAllDetails(invokingMember.getOrganizationId(), circuitName);
    }

    /**
     * Joins an existing WTF_ROOM circuit
     *
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.JOIN_PATH)
    public LearningCircuitDto joinExistingCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("joinExistingCircuit, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.joinExistingCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Leaves an existing WTF_ROOM circuit (member will still be joined, but inactive)
     *
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.LEAVE_PATH)
    public LearningCircuitDto leaveExistingCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("leaveExistingCircuit, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.leaveExistingCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CLOSE_PATH)
    public LearningCircuitDto closeExistingCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("closeExistingCircuit, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.closeExistingCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.DO_IT_LATER_PATH)
    public LearningCircuitDto putCircuitOnHoldWithDoItLater(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("shelveCircleWithDoItLater, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.putCircuitOnHoldWithDoItLater(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RESUME_PATH)
    public LearningCircuitDto resumeCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("resumeCircuit, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.resumeCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * Retrieves the active circuit for the user
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.ACTIVE_PATH)
    public LearningCircuitDto getMyActiveCircuit() {
        RequestContext context = RequestContext.get();
        log.info("getMyActiveCircuit, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.getMyActiveWTFCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the do it later circuits for the logged in user
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.DO_IT_LATER_PATH)
    public List<LearningCircuitDto> getMyDoItLaterCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getMyDoItLaterCircuits, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.getMyDoItLaterCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves all participating circuits for the logged in user
     * @return TwilightLearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    public List<LearningCircuitDto> getAllMyParticipatingCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getAllMyParticipatingCircuits, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return learningCircuitService.getAllParticipatingCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)


    @GetMapping(ResourcePaths.MEMBER_PATH + "/{id}" )
    public List<LearningCircuitDto> getAllParticipatingCircuitsForMember(@PathVariable("memberId") String memberId) {
        RequestContext context = RequestContext.get();
        log.info("getAllParticipatingCircuitsForMember, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        UUID otherMemberId = UUID.fromString(memberId);

        return learningCircuitService.getAllParticipatingCircuitsForOtherMember(invokingMember.getOrganizationId(), invokingMember.getId(), otherMemberId);
    }

}
