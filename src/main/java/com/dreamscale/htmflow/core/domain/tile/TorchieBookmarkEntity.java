package com.dreamscale.htmflow.core.domain.tile;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "torchie_bookmark")
@Data
@EqualsAndHashCode(of = "torchieId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TorchieBookmarkEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;
    private LocalDateTime metronomeCursor;

}
