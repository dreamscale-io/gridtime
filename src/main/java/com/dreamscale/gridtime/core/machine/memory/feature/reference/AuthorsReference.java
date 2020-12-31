package com.dreamscale.gridtime.core.machine.memory.feature.reference;

import com.dreamscale.gridtime.core.domain.member.json.Member;
import com.dreamscale.gridtime.core.machine.memory.type.AuthorsType;
import com.dreamscale.gridtime.core.machine.memory.feature.details.AuthorsDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AuthorsReference extends FeatureReference {

    public AuthorsReference(AuthorsType authorsType, String searchKey) {
        super(UUID.randomUUID(), authorsType, searchKey, null, false);
    }

    public AuthorsReference(AuthorsType authorsType, String searchKey, AuthorsDetails authorsDetails) {
        super(UUID.randomUUID(), authorsType, searchKey, authorsDetails, false);
    }

    public AuthorsType getAuthorsType() {
        return (AuthorsType) getFeatureType();
    }

    public List<Member> getAuthors() {
        return ((AuthorsDetails)getDetails()).getAuthors();
    }

    @Override
    public String toDisplayString() {
        List<Member> authors = getAuthors();
        String allInitials = "";

        for (Member author : authors) {
            String initials = toInitials(author.getMemberName());
            allInitials += initials + " ";
        }

        return allInitials.trim();
    }

    @Override
    public String getDescription() {
        String description = "";

        Iterator<Member> authorIter = getAuthors().iterator();

        while (authorIter.hasNext()) {
            Member author = authorIter.next();
            description += author.getMemberName();

            if (authorIter.hasNext()) {
                description += ", ";
            }

        }
        return description;
    }

    private String toInitials(String memberName) {
        String [] nameParts = memberName.split(" ");
        String initials = "";
        for (String namePart : nameParts) {
            if (namePart.length() > 1) {
                initials += namePart.substring(0, 1).toUpperCase();
            }
        }

        return initials;
    }
}
