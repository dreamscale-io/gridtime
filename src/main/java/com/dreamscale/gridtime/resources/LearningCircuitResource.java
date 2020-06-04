package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
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

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getCircuitWithAllDetails(invokingMember.getOrganizationId(), circuitName);
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
     * Starts the retrospective for the specified WTF.
     *
     * The WTF can be solved, and waiting for a retro, or I might do a retro immediately.
     *
     * If the WTF has not been finished, it will automatically be finished.
     *
     * All members in the WTF room, will be moved to the Retro room.
     *
     * Input States: TROUBLESHOOT or SOLVED
     * Ouput State: RETRO
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RETRO_PATH)
    public LearningCircuitDto startRetroForWTF(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("startRetroForWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.startRetroForWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }


    /**
     * For a WTF that's been solved, with a retro potentially already started,
     * the owner can choose to re-open the WTF, moving all the people in the retro, back to the WTF room.
     *
     * Input States: SOLVED or RETRO
     * Ouput State: TROUBLESHOOT
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.REOPEN_PATH)
    public LearningCircuitDto reopenSolvedWTF(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("reopenSolvedWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.reopenSolvedWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }

    /**
     * For a WTF that's been solved, with an optional retro thats now finished up,
     * the owner can now close the WTF, which closes all the rooms.  Once the WTF is closed,
     * its locked in read-only mode for metrics calculations and analytics.
     *
     * Input States: SOLVED or RETRO
     * Ouput State: CLOSED
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CLOSE_PATH)
    public LearningCircuitDto closeWTF(@PathVariable("name") String circuitName) {
        RequestContext context = RequestContext.get();
        log.info("closeWTF, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.closeWTF(invokingMember.getOrganizationId(), invokingMember.getId(), circuitName);
    }



    /**
     * Retrieves the active LearningCircuit owned by the invoking member.
     *
     * Once a WTF is solved (even if no Retro has been run yet), it will no longer be the active circuit
     *
     * All circuits return will be in "TROUBLESHOOT" state.
     *
     *
     * @return LearningCircuitDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.ACTIVE_PATH)
    public LearningCircuitDto getMyActiveCircuit() {
        RequestContext context = RequestContext.get();
        log.info("getMyActiveCircuit, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getMyActiveWTFCircuit(invokingMember.getOrganizationId(), invokingMember.getId());
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
     * Retrieves the list of Learning Circuits that are in "TROUBLESHOOT" or "RETRO" state that the invoking member has also joined.
     *
     * When joining and leaving another team member's Learning Circuits, the invoking member is still considered
     * to be "participating" unless they explicitly leave the circuit room.
     *
     * All circuits listed will be in "TROUBLESHOOT" or "RETRO" state.
     *
     * @return List<LearningCircuitDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    public List<LearningCircuitDto> getAllMyParticipatingCircuits() {
        RequestContext context = RequestContext.get();
        log.info("getAllMyParticipatingCircuits, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return wtfCircuitOperator.getAllParticipatingCircuits(invokingMember.getOrganizationId(), invokingMember.getId());
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
