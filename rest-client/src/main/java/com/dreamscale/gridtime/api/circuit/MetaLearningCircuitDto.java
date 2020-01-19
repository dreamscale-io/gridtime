package com.dreamscale.gridtime.api.circuit;

import com.dreamscale.gridtime.api.dictionary.DictionaryLinkDto;
import com.dreamscale.gridtime.api.dictionary.RetroLinkDto;
import com.dreamscale.gridtime.api.dictionary.Scope;
import com.dreamscale.gridtime.api.dictionary.WTFLinkDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaLearningCircuitDto {

    UUID id;
    String circuitName;
    String circuitPurpose; //what are we here to learn?
    Scope context;

    String metaChatRoomName;
    UUID metaChatRoomId; //humans chatting about whatever this circuit is for

    List<WTFLinkDto> wtfsNeedsReview;
    List<RetroLinkDto> recentRetroXPs;
    List<DictionaryLinkDto> recentWords;

    List<WTFLinkDto> activeWTFs;

    UUID ownerId;
    UUID moderatorId;


}

//    UUID inputWordsFeed; //words referenced across any context
//    UUID inputWTFs; //feed of input WTFs (automated realtime talk input, others publish here)
//    UUID outputYAYs; //feed of output YAYs (humans publish output, for others to consume.
