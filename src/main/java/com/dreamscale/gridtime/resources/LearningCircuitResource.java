package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.operator.LearningCircuitOperator;
import com.dreamscale.gridtime.core.capability.directory.OrganizationDirectoryCapability;
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
public class LearningCircuitResource {

    @Autowired
    OrganizationDirectoryCapability organizationDirectoryCapability;

    @Autowired
    LearningCircuitOperator learningCircuitOperator;

    /**
     * Pulls the "Global Andon Cord" to generate a new "Learning Circuit" that can be joined by
     * anyone in the organization that wants to collaborate.  Learning Circuits help the team navigate through
     * the learning flow of triaging the WTF, then reflecting with a Retro to distill the experience into patterns.
     *
     * Circuits can be put on hold, and resumed.
     * Each member can have only one WTF circuit active at any given time.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH)
    public LearningCircuitDto startWTF() {
        RequestContext context = RequestContext.get();
        log.info("startLearningCircuitForWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.createNewLearningCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Pulls the "Global Andon Cord" to generate a new "Learning Circuit" with a specified custom name, that can be joined by
     * anyone in the organization that wants to collaborate.  Learning Circuits help the team navigate through
     * the learning flow of triaging the WTF, then reflecting with a Retro to distill the experience into patterns.
     *
     * If the circuit name already exists, the circuit will add a random extension onto the requested name
     * to make it unique.
     *
     * Circuits can be put on hold, and resumed.
     * Each member can have only one WTF circuit active at any given time.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}")
    public LearningCircuitDto startWTFWithCustomCircuitName(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("startWTFWithCustomCircuitName, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.createNewLearningCircuitWithCustomName(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Overwrites the description property of the Learning Circuit
     *
     * Only editable by owner
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.DESCRIPTION_PATH)
    public LearningCircuitDto saveDescriptionForLearningCircuit(@PathVariable("name") String circuitName, @RequestBody DescriptionInputDto descriptionInputDto ) {
        RequestContext context = RequestContext.get();
        log.info("saveDescriptionForLearningCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.saveDescriptionForLearningCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName, descriptionInputDto);
    }

    /**
     * Overwrites the tags property of the Learning Circuit
     *
     * Only editable by owner
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.TAGS_PATH)
    public LearningCircuitDto saveTagsForLearningCircuit(@PathVariable("name") String circuitName, @RequestBody TagsInputDto tagsInputDto ) {
        RequestContext context = RequestContext.get();
        log.info("saveTagsForLearningCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.saveTagsForLearningCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName, tagsInputDto);
    }

    /**
     * Starts the retrospective for the specified Learning Circuit
     *
     * All members that participated in the WTF, will automatically be added as members of the retro session.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RETRO_PATH)
    public LearningCircuitDto startRetroForWTF(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("startRetroForWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.startRetroForCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * To retrieve the full set of details for a Learning Circuit, including a list of all participating members,
     * and their statuses, use this API.
     *
     * @return LearningCircuitWithMembersDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.WTF_PATH + "/{name}" )
    LearningCircuitWithMembersDto getCircuitWithAllDetails(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("getCircuitWithAllDetails, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.getCircuitWithAllDetails(invokingMember.getOrganizationId(), circuitName);
    }

    /**
     * Joins an existing Learning Circuit created by another team member.
     *
     * Will join the WTF and/or Retro rooms, depending on the active state of the Circuit.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.JOIN_PATH)
    public LearningCircuitDto joinExistingCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("joinExistingCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.joinExistingCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Leaves an existing Learning Circuit created by another team member.
     *
     * The member's status will be changed to Inactive, and will leave the Talk Room.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.LEAVE_PATH)
    public LearningCircuitDto leaveExistingCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("leaveExistingCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.leaveExistingCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Closes an existing Learning Circuit owned by the invoking team member.
     *
     * This causes circuits to disappear from the "My Circuits" list, effectively archiving them as complete.
     *
     * Circuits can only be closed by the owner.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CLOSE_PATH)
    public LearningCircuitDto closeExistingCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("closeExistingCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.closeExistingCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * Aborts an existing Learning Circuit owned by the invoking team member.
     *
     * This causes circuits to disappear from the "My Circuits" list, without having to finish the WTF
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.ABORT_PATH)
    public LearningCircuitDto abortExistingCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("abortExistingCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.abortExistingCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * Changes the status of an active Learning Circuit to the "Do It Later" status.
     *
     * Circuits in this state will still appear on the "My Circuits" list, and also the "Do It Later" list.
     *
     * Only circuits owned by the invoking member can be changed to a different status.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.DO_IT_LATER_PATH)
    public LearningCircuitDto putCircuitOnHoldWithDoItLater(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("shelveCircleWithDoItLater, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.putCircuitOnHoldWithDoItLater(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Changes the status of an inactive Learning Circuit back to the "Active" status.
     *
     * Circuits in this state will appear on the "My Circuits" list, and will no longer show on the "Do It Later" list.
     *
     * Only circuits owned by the invoking member can be changed to a different status.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RESUME_PATH)
    public LearningCircuitDto resumeCircuit(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("resumeCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.resumeCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * Retrieves the active Learning Circuit of the invoking member.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.ACTIVE_PATH)
    public LearningCircuitDto getMyActiveCircuit() {
        RequestContext context = RequestContext.get();
        log.info("getMyActiveCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.getMyActiveWTFCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the list of Learning Circuits in the "Do It Later" status for the invoking member.
     *
     * @return List<LearningCircuitDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.DO_IT_LATER_PATH)
    public List<LearningCircuitDto> getMyDoItLaterCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getMyDoItLaterCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.getMyDoItLaterCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the list of Learning Circuits that are "ACTIVE" that the invoking member has also joined.
     *
     * When joining and leaving another team member's Learning Circuits, the invoking member is still considered
     * to be "participating" even if they are inactive, as long as the circuit itself is in an "ACTIVE" state.
     *
     * @return List<LearningCircuitDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    public List<LearningCircuitDto> getAllMyParticipatingCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getAllMyParticipatingCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        return learningCircuitOperator.getAllParticipatingCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the list of Learning Circuits that are "ACTIVE" that the specified member has also joined.
     *
     * When joining and leaving another team member's Learning Circuits, the specified member is still considered
     * to be "participating" even if they are inactive, as long as the circuit itself is in an "ACTIVE" state.
     *
     * @return List<LearningCircuitDto>
     */
    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    @GetMapping(ResourcePaths.MEMBER_PATH + "/{id}" )
    public List<LearningCircuitDto> getAllParticipatingCircuitsForMember(@PathVariable("memberId") String memberId) {
        RequestContext context = RequestContext.get();
        log.info("getAllParticipatingCircuitsForMember, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationDirectoryCapability.getDefaultMembership(context.getRootAccountId());

        UUID otherMemberId = UUID.fromString(memberId);

        return learningCircuitOperator.getAllParticipatingCircuitsForOtherMember(invokingMember.getOrganizationId(), invokingMember.getId(), otherMemberId);
    }

}
