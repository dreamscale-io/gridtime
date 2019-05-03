package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "details_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ExecutionDetails.class, name = Details.EXECUTION),
        @JsonSubTypes.Type(value = AuthorDetails.class, name = Details.AUTHOR),
        @JsonSubTypes.Type(value = CircleDetails.class, name = Details.CIRCLE),
        @JsonSubTypes.Type(value = FeelsDetails.class, name = Details.FEELS),
        @JsonSubTypes.Type(value = MessageDetails.class, name = Details.MESSAGE),
})

public abstract class Details {

    static final String EXECUTION= "EXECUTION";
    static final String AUTHOR= "AUTHOR";
    static final String CIRCLE= "CIRCLE";
    static final String FEELS= "FEELS";
    static final String MESSAGE= "MESSAGE";

    private DetailsType detailsType;

    public Details(DetailsType detailsType) {
        this.detailsType = detailsType;
    }
}
