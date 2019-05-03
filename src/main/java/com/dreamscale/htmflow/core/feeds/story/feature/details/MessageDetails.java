package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MessageDetails extends Details {

    private UUID messageFromTorchieId;
    private String messageFromName;

    private UUID messageId;
    private CircleMessageType messageType;
    private String message;

    private UUID circleId;
    private String circleName;
    private String snippetSource;
    private String filePath;
    private String fileName;

    public MessageDetails() {
        super(DetailsType.MESSAGE);
    }

}
