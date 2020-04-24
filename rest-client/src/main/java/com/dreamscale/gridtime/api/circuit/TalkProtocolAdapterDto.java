package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalkProtocolAdapterDto {

    //what are my enums?

    //talk protocol configurator.

    //changing the specification of the config.

    //enumerate the verbs and classes you support.

    //Takes A and transforms it to B.

    //Configure your socket, with a version of this protocol.

    private List<String> messageTypes;
}
