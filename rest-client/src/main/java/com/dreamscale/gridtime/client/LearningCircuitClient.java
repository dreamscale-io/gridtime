package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface LearningCircuitClient {

    //create circuits

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH)
    LearningCircuitDto startWTF();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH)
    LearningCircuitDto getActiveCircuit();

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}")
    LearningCircuitDto startWTFWithCustomCircuitName(@Param("name") String customCircuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.DESCRIPTION_PATH)
    LearningCircuitDto saveDescriptionForLearningCircuit(@Param("name") String customCircuitName, DescriptionInputDto descriptionInputDto);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.TAGS_PATH)
    LearningCircuitDto saveTagsForLearningCircuit(@Param("name") String customCircuitName, TagsInputDto tagsInputDto);

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" )
    LearningCircuitWithMembersDto getCircuitWithAllDetails(@Param("name") String circuitName);

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.MEMBER_PATH)
    LearningCircuitMembersDto getCircuitMembers(@Param("name") String circuitName);

    //workflow transitions

    //document workflow transitions

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.SOLVE_PATH)
    LearningCircuitDto solveWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CANCEL_PATH)
    LearningCircuitDto cancelWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.DO_IT_LATER_PATH)
    LearningCircuitDto pauseWTFWithDoItLater(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RESUME_PATH)
    LearningCircuitDto resumeWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.MARK_PATH + ResourcePaths.RETRO_PATH)
    SimpleStatusDto markForReview(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.MARK_PATH + ResourcePaths.CLOSE_PATH)
    SimpleStatusDto markForClose(@Param("name") String circuitName);

    //join and leave at the circuit level

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.JOIN_PATH)
    LearningCircuitDto joinWTF(@Param("name") String circuitName);

    //query circuits -- these below APIs feel like they need to move somewhere else...?

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.DO_IT_LATER_PATH)
    List<LearningCircuitDto> getMyDoItLaterCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.TROUBLESHOOT_PATH)
    List<LearningCircuitDto> getMyTroubleshootCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.SOLVE_PATH)
    List<LearningCircuitDto> getMySolvedCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.RETRO_PATH)
    List<LearningCircuitDto> getMyRetroCircuits();


    //deprecating this API since every one of these searches returns participating, use /circuit/my/troubleshoot instead

    @Deprecated
    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    List<LearningCircuitDto> getAllMyParticipatingCircuits();

    //do we need this?  I feel like we don't, or if we do, should follow same pattern as for single user

    @Deprecated
    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MEMBER_PATH + "/{id}" + ResourcePaths.PARTICIPATING_PATH )
    List<LearningCircuitDto> getAllParticipatingCircuitsForMember(@Param("id") String memberId);


}


