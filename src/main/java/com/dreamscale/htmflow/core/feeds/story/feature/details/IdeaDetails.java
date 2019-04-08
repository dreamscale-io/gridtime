package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import lombok.Getter;

import java.util.UUID;

@Getter
public class IdeaDetails extends Details {

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


    public void setMessageFromTorchieId(UUID torchieId) {
        this.messageFromTorchieId = torchieId;
    }

    public void setMessageFromName(String fullName) {
        this.messageFromName = fullName;
    }

    public void setCircleId(UUID circleId) {
        this.circleId = circleId;
    }

    public void setCircleName(String circleName) {
        this.circleName = circleName;
    }

    public void setSnippetSource(String snippetSource) {
        this.snippetSource = snippetSource;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageType(CircleMessageType messageType) {
        this.messageType = messageType;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }
}
