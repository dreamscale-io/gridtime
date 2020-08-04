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

    //create and update and retrieve circuits

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

    //Retrieve members for a circuit

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.MEMBER_PATH)
    LearningCircuitMembersDto getCircuitMembers(@Param("name") String circuitName);

    //circuit workflow transitions

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

    //join at the circuit level (sends talk message join events)

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.JOIN_PATH)
    LearningCircuitDto joinWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.LEAVE_PATH)
    LearningCircuitDto leaveWTF(@Param("name") String circuitName);

    //get all participating circuits by state, or get all participating circuits -- all APIs are participating APIs

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.DO_IT_LATER_PATH)
    List<LearningCircuitDto> getMyDoItLaterCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.TROUBLESHOOT_PATH)
    List<LearningCircuitDto> getMyTroubleshootCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.SOLVE_PATH)
    List<LearningCircuitDto> getMySolvedCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.RETRO_PATH)
    List<LearningCircuitDto> getMyRetroCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    List<LearningCircuitDto> getMyParticipatingCircuits();

    //get all participating circuits for some other member

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MEMBER_PATH + "/{id}" + ResourcePaths.PARTICIPATING_PATH )
    List<LearningCircuitDto> getParticipatingCircuitsForMember(@Param("id") String memberId);


}


