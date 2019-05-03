package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AuthorDetails extends Details {

    private List<Member> authors;

    public AuthorDetails(Member author) {
        this();
        this.authors = new ArrayList<>();
        this.authors.add(author);
    }

    public AuthorDetails(List<Member> authors) {
        this();
        this.authors = authors;
    }

    public AuthorDetails() {
        super(DetailsType.AUTHOR);
    }
}
