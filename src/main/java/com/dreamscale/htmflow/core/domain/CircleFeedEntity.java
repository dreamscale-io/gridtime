package com.dreamscale.htmflow.core.domain;

import com.dreamscale.htmflow.api.circle.MessageType;
import com.dreamscale.htmflow.api.circle.SnippetType;
import com.dreamscale.htmflow.api.event.EventType;
import com.dreamscale.htmflow.core.domain.flow.MetadataFields;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "circle_feed")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleFeedEntity {

    public static String MESSAGE_FIELD = "message";

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "circle_id")
    private UUID circleId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "member_id")
    private UUID memberId;

    private LocalDateTime timePosition;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private String metadata;

    @Transient
    private final MetadataFields metadataFields = new MetadataFields();

    public void setMetadataField(String key, Object value) {
        metadataFields.set(key, value);
        metadata = metadataFields.toJson();
    }

    public String getMetadataValue(String key) {
        return metadataFields.get(key);
    }

    @PostLoad
    private void postLoad() {
        metadataFields.fromJson(metadata);
    }

}
