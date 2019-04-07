package com.dreamscale.htmflow.core.domain.circle;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.flow.MetadataFields;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "circle_feed_message_view")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleFeedMessageEntity {

    public static String MESSAGE_FIELD = "message";

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "circle_id")
    private UUID circleId;

    private String circleName;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;
    private String fullName;

    private LocalDateTime position;

    @Enumerated(EnumType.STRING)
    private CircleMessageType messageType;



    private String metadata;

    @Transient
    private final MetadataFields metadataFields = new MetadataFields();

    public void setMetadataField(String key, Object value) {
        metadataFields.set(key, value);
        metadata = metadataFields.toJson();
    }

    public String getMessage() {
        return getMetadataValue(CircleMessageEntity.MESSAGE_FIELD);
    }

    public String getFileName() {
        return getMetadataValue(CircleMessageEntity.FILE_NAME_FIELD);
    }

    public String getFilePath() {
        return getMetadataValue(CircleMessageEntity.FILEPATH_FIELD);
    }

    public String getSnippet() {
        return getMetadataValue(CircleMessageEntity.SNIPPET_FIELD);
    }

    public String getSnippetSource() {
        return getMetadataValue(CircleMessageEntity.SNIPPET_SOURCE_FIELD);
    }

    public String getMetadataValue(String key) {
        return metadataFields.get(key);
    }

    @PostLoad
    private void postLoad() {
        metadataFields.fromJson(metadata);
    }

}
