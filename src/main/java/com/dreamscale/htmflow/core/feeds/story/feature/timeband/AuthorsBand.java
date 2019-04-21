package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.feeds.story.feature.details.AuthorDetails;

import java.time.LocalDateTime;
import java.util.List;

public class AuthorsBand extends Timeband {

    private final AuthorDetails details;

    public AuthorsBand(LocalDateTime start, LocalDateTime end, AuthorDetails authorDetails) {
        super(start, end, authorDetails);
        this.details = authorDetails;
    }

    public int getAuthorCount() {
        return details.getAuthors().size();
    }

    public List<Member> getAuthors() {
        return details.getAuthors();
    }
}
