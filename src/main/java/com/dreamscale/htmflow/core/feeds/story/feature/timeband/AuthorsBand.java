package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
public class AuthorsBand extends Timeband {

    public AuthorsBand(LocalDateTime start, LocalDateTime end, AuthorDetails authorDetails) {
        super(start, end, authorDetails);
    }

    @JsonIgnore
    public int getAuthorCount() {
        return ((AuthorDetails)getDetails()).getAuthors().size();
    }

    @JsonIgnore
    public List<Member> getAuthors() {
        return ((AuthorDetails)getDetails()).getAuthors();
    }
}
