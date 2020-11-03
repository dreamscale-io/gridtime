package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.core.domain.member.json.Member;
import com.dreamscale.gridtime.core.machine.memory.cache.SearchKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorsDetails implements FeatureDetails {

    private List<Member> authors;

    public String toSearchKey() {
       return SearchKeyGenerator.createAuthorsSearchKey(authors);
    }


    public String toString() {
        return toSearchKey();
    }

}
