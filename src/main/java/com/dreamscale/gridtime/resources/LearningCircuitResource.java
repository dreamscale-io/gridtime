package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.circuit.WTFCircuitOperator;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
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
    OrganizationCapability organizationCapability;

    @Autowired
    WTFCircuitOperator wtfCircuitOperator;

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
        log.info("startWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.startWTF(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the active LearningCircuit of the invoking user, whether created by the user, or
     * a joined session of another member.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.WTF_PATH)
    public LearningCircuitDto getActiveWTF() {
        RequestContext context = RequestContext.get();
        log.info("getActiveWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getMyActiveWTFCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
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

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.startWTFWithCustomName(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
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

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.saveDescriptionForLearningCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName, descriptionInputDto);
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

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.saveTagsForLearningCircuit(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName, tagsInputDto);
    }

    /**
     * Retrieve the full set of details for a Learning Circuit, including a list of all participating members,
     * and their statuses.
     *
     * @return LearningCircuitWithMembersDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.WTF_PATH + "/{name}" )
    LearningCircuitWithMembersDto getCircuitWithAllDetails(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("getCircuitWithAllDetails, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getCircuitWithAllDetails(invokingMember.getOrganizationId(), circuitName);
    }

    /**
     * Retrieve the list of Circuit Members for a Learning Circuit, including all of their statuses
     *
     * @return LearningCircuitWithMembersDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.MEMBER_PATH)
    LearningCircuitMembersDto getCircuitMembers(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("getCircuitMembers, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getCircuitMembers(invokingMember.getOrganizationId(), circuitName);
    }


    /**
     * Joins the specified WTF circuit.
     *
     * If the user currently has a WTF active, this old WTF will be put on hold, and this new one will become active.
     *
     * If the user is currently joined to another members WTF, they will leave and join this new one.
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.JOIN_PATH)
    public LearningCircuitDto joinWTF(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("joinWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.joinWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * Leaves the specified WTF circuit.
     *
     * The users current WTF will be cleared and the member status updated
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.LEAVE_PATH)
    public LearningCircuitDto leaveWTF(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("leaveWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.leaveWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }



    /**
     * Finishes an existing WTF , the circuit must be owned by the invoking member.
     *
     * This causes the WTF to disappear from the "My Circuits" list, effectively archiving them as complete.
     *
     * All members will be automatically removed the WTF talk room.
     *
     * Input States: TROUBLESHOOT
     * Ouput State: SOLVED
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.SOLVE_PATH)
    public LearningCircuitDto solveWTF(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("solveWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.solveWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Cancels an existing WTF, the circuit must be owned by the invoking member.
     *
     * This causes circuits to disappear from the "My Circuits" list, without having to finish the WTF
     *
     * Canceling a WTF is a final state, and does not need to be closed.
     *
     * Input States: TROUBLESHOOT or ONHOLD
     * Ouput State: CANCELED
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CANCEL_PATH)
    public LearningCircuitDto cancelWTF(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("cancelWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.cancelWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Changes the status of a WTF to the "ONHOLD" status, the circuit must be owned by the invoking member.
     *
     * Circuits in this state will still appear on the "My Circuits" list, and also the "Do It Later" list.
     *
     * Input States: TROUBLESHOOT
     * Ouput State: ONHOLD
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.DO_IT_LATER_PATH)
    public LearningCircuitDto putWTFOnHoldWithDoItLater(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("pauseWTFWithDoItLater, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.pauseWTFWithDoItLater(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * When a WTF is in the "ONHOLD" state, can resume the Circuit to make it active again.
     *
     * Circuits in this state will appear on the "My Circuits" list, and will no longer show on the "Do It Later" list.
     *
     * Only circuits owned by the invoking member can be changed to a different status.
     *
     * Input States: ONHOLD
     * Ouput State: TROUBLESHOOT
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RESUME_PATH)
    public LearningCircuitDto resumeWTF(@PathVariable("name") String circuitName) {

        RequestContext context = RequestContext.get();
        log.info("resumeWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.resumeWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Marks the specified WTF for needing review by the team.
     *
     * The WTF can be marked for review only if it is in SOLVED or RETRO state,
     * and only if the user participated in the WTF.
     *
     * The Learning Circuit will show how many marks.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.MARK_PATH + ResourcePaths.RETRO_PATH)
    public SimpleStatusDto markForReview(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("markForReview, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.markForReview(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * Marks the specified WTF for close by the team.
     *
     * The WTF can be marked for close only if it is in RETRO state,
     * and only if the user participated in the WTF.
     *
     * The Learning Circuit will show how many marks, and will automatically close when the majority selects close.
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.MARK_PATH + ResourcePaths.CLOSE_PATH)
    public SimpleStatusDto markForClose(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("markForClose, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.markForClose(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * Retrieves the list of Learning Circuits in the "ONHOLD" status for the invoking member.
     *
     * @return List<LearningCircuitDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.DO_IT_LATER_PATH)
    public List<LearningCircuitDto> getMyDoItLaterCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getMyDoItLaterCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getMyDoItLaterCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the list of Learning Circuits that the member has joined, across all states, as long as they are not closed
     *
     * When joining and leaving another team member's Learning Circuits, the invoking member is still considered
     * to be "participating" unless they explicitly leave the circuit room.
     *
     *
     * @return List<LearningCircuitDto>
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    public List<LearningCircuitDto> getMyParticipatingCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getMyParticipatingCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getMyParticipatingCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Retrieves the list of Learning Circuits that are in "TROUBLESHOOT" state that the invoking member has also joined.
     *
     * When joining and leaving another team member's Learning Circuits, the invoking member is still considered
     * to be "participating" unless they explicitly leave the circuit.
     *
     * All circuits listed will be in "TROUBLESHOOT" state.
     *
     * @return List<LearningCircuitDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.TROUBLESHOOT_PATH)
    public List<LearningCircuitDto> getMyTroubleshootCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getMyTroubleshootCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getMyTroubleshootCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Retrieves the list of Learning Circuits that are in "SOLVED" state
     * that the user participated in, and are ready for the user to mark for review or close.
     *
     * Is this important?  Mark for review.
     *
     * These are sorted descending by amount of time in WTF (totalCircuitElapsedNanoTime)
     *
     * @return List<LearningCircuitDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.SOLVE_PATH)
    public List<LearningCircuitDto> getMySolvedCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getMySolvedCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getMySolvedCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Retrieves the list of Learning Circuits that are in "RETRO" state
     * that the user participated in, and have retros actively open.
     *
     * Done retro-ing?  Mark for close.
     *
     * These are sorted descending by amount of time in WTF (totalCircuitElapsedNanoTime)
     *
     * @return List<LearningCircuitDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.RETRO_PATH)
    public List<LearningCircuitDto> getMyRetroCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getMyRetroCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getMyRetroCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the list of Learning Circuits that are in "TROUBLESHOOT" or "RETRO" state that the specifed member has also joined.
     *
     * When joining and leaving another team member's Learning Circuits, the invoking member is still considered
     * to be "participating" unless they explicitly leave the circuit room.
     *
     * All circuits listed will be in "TROUBLESHOOT" or "RETRO" state.
     *
     * @return List<LearningCircuitDto>
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping( ResourcePaths.MEMBER_PATH + "/{id}" + ResourcePaths.PARTICIPATING_PATH )
    public List<LearningCircuitDto> getAllParticipatingCircuitsForMember(@PathVariable("memberId") String memberId) {
        RequestContext context = RequestContext.get();
        log.info("getAllParticipatingCircuitsForMember, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID otherMemberId = UUID.fromString(memberId);

        return wtfCircuitOperator.getAllParticipatingCircuitsForOtherMember(invokingMember.getOrganizationId(), invokingMember.getId(), otherMemberId);
    }

}
