package com.dreamscale.gridtime.core.domain.circle;

import com.dreamscale.gridtime.api.circle.CircleMessageType;
import com.dreamscale.gridtime.core.domain.flow.MetadataFields;
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
        return getMetadataValue(CircleMessageMetadataField.message);
    }

    public String getFileName() {
        return getMetadataValue(CircleMessageMetadataField.name);
    }

    public String getFilePath() {
        return getMetadataValue(CircleMessageMetadataField.filePath);
    }

    public String getSnippet() {
        return getMetadataValue(CircleMessageMetadataField.snippet);
    }

    public String getSnippetSource() {
        return getMetadataValue(CircleMessageMetadataField.snippetSource);
    }

    public String getMetadataValue(CircleMessageMetadataField field) {
        return metadataFields.get(field.name());
    }

    @PostLoad
    private void postLoad() {
        metadataFields.fromJson(metadata);
    }

}
