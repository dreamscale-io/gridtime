package com.dreamscale.gridtime.api.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetroLinkDto {

    UUID roomId;

    String hashTag;
    String linkTo;

    double importanceWeight;

    double similiarityWeight;

    //color code signal
    

    //aura of similarity

    //what is the link from gridtime...

    //give me the URL of where I can get that definition from...

    //will represent the URL of where that resource lives
}
