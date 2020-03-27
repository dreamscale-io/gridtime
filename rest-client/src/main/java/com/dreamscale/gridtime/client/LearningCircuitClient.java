package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
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

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}")
    LearningCircuitDto startWTFWithCustomCircuitName(@Param("name") String customCircuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.DESCRIPTION_PATH)
    LearningCircuitDto saveDescriptionForLearningCircuit(@Param("name") String customCircuitName, DescriptionInputDto descriptionInputDto);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.PROPERTY_PATH + ResourcePaths.TAGS_PATH)
    LearningCircuitDto saveTagsForLearningCircuit(@Param("name") String customCircuitName, TagsInputDto tagsInputDto);

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" )
    LearningCircuitWithMembersDto getCircuitWithAllDetails(@Param("name") String circuitName);

    //workflow transitions

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.SOLVE_PATH)
    LearningCircuitDto solveWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CANCEL_PATH)
    LearningCircuitDto abortWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.DO_IT_LATER_PATH)
    LearningCircuitDto putWTFOnHoldWithDoItLater(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RESUME_PATH)
    LearningCircuitDto resumeWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RETRO_PATH)
    LearningCircuitDto startRetroForWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CLOSE_PATH)
    LearningCircuitDto closeWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.REOPEN_PATH)
    LearningCircuitDto reopenWTF(@Param("name") String circuitName);

    //query circuits

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.ACTIVE_PATH )
    LearningCircuitDto getActiveCircuit();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.DO_IT_LATER_PATH)
    List<LearningCircuitDto> getAllMyDoItLaterCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    List<LearningCircuitDto> getAllMyParticipatingCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MEMBER_PATH + "/{id}" + ResourcePaths.PARTICIPATING_PATH )
    List<LearningCircuitDto> getAllParticipatingCircuitsForMember(@Param("id") String memberId);



}


