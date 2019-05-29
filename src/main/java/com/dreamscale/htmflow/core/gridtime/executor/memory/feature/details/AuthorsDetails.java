package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.AuthorsType;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.SearchKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AuthorsDetails implements FeatureDetails {

    private List<Member> authors;

    public String toSearchKey() {
       return SearchKeyGenerator.createAuthorsSearchKey(authors);
    }


    public String toString() {
        return toSearchKey();
    }

}
