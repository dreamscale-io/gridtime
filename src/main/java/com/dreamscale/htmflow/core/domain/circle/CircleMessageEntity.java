package com.dreamscale.htmflow.core.domain.circle;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.flow.MetadataFields;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "circle_message")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleMessageEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "circle_id")
    private UUID circleId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    private LocalDateTime position;

    @Enumerated(EnumType.STRING)
    private CircleMessageType messageType;

    private String metadata;

    @Transient
    private final MetadataFields metadataFields = new MetadataFields();

    public void setMetadataField(CircleMessageMetadataField field, Object value) {
        metadataFields.set(field.name(), value);
        metadata = metadataFields.toJson();
    }

    public String getMetadataValue(CircleMessageMetadataField field) {
        return metadataFields.get(field.name());
    }

    @PostLoad
    private void postLoad() {
        metadataFields.fromJson(metadata);
    }

}
