package com.dreamscale.gridtime.api.dictionary;

import com.dreamscale.gridtime.api.circuit.MessageDetailsBody;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerPatternDto implements MessageDetailsBody {

    UUID id;
    String hashTag;
    String definition;
    UUID glyphId;

    List<DictionaryLinkDto> dictionaryLinks;

}
