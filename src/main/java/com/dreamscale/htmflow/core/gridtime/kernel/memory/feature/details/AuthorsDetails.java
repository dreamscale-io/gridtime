package com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.details;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.SearchKeyGenerator;
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
