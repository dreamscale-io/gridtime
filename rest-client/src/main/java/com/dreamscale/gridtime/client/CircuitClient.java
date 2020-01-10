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
public interface CircuitClient {


    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH)
    LearningCircuitDto createLearningCircuitForWTF();

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}")
    LearningCircuitDto createLearningCircuitWithCustomName(@Param("name") String customCircuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RETRO_PATH)
    LearningCircuitDto startRetroForWTF(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.JOIN_PATH)
    LearningCircuitDto joinExistingCircuit(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.LEAVE_PATH)
    LearningCircuitDto leaveExistingCircuit(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.CLOSE_PATH)
    LearningCircuitDto closeExistingCircuit(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.DO_IT_LATER_PATH)
    LearningCircuitDto putCircuitOnHoldWithDoItLater(@Param("name") String circuitName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" + ResourcePaths.RESUME_PATH)
    LearningCircuitDto resumeCircuit(@Param("name") String circuitName);

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.WTF_PATH + "/{name}" )
    LearningCircuitWithMembersDto getCircuitWithAllDetails(@Param("name") String circuitName);

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.ACTIVE_PATH )
    LearningCircuitDto getActiveCircuit();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.DO_IT_LATER_PATH)
    List<LearningCircuitDto> getAllMyDoItLaterCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    List<LearningCircuitDto> getAllMyParticipatingCircuits();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.MEMBER_PATH + "/{id}" )
    List<LearningCircuitDto> getAllParticipatingCircuitsForMember(@Param("id") String memberId);
}
