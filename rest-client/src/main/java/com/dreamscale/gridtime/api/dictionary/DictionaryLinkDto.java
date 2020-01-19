package com.dreamscale.gridtime.api.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryLinkDto {

    String hashTag;
    String linkTo;

    double correlationWeight;

    //color code signal


    //aura of similarity

    //what is the link from gridtime...

    //give me the URL of where I can get that definition from...

    //will represent the URL of where that resource lives
}
