package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "learning_circuit")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearningCircuitEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID ownerId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID moderatorId;

    private String circuitName;
    private String description;
    private String jsonTags;

    private UUID wtfRoomId;
    private UUID retroRoomId;

    private LocalDateTime openTime;
    private LocalDateTime solvedTime;

    @Enumerated(EnumType.STRING)
    private LearningCircuitState circuitState;

    private Long totalCircuitElapsedNanoTime;
    private Long totalCircuitPausedNanoTime;

    private Long wtfOpenNanoTime;
    private Long retroOpenNanoTime;
    private Long solvedCircuitNanoTime;
    private Long pauseCircuitNanoTime;
    private Long resumeCircuitNanoTime;
    private Long cancelCircuitNanoTime;
    private Long closeCircuitNanoTime;

    private Integer marksForReview;
    private Integer marksRequiredForReview;

    private Integer marksForClose;
    private Integer marksRequiredForClose;

}


