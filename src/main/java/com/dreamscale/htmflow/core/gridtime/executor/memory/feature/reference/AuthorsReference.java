package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.AuthorsType;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.IdeaFlowStateType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.AuthorsDetails;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import lombok.Getter;
import lombok.Setter;

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
