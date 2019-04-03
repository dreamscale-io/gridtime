package com.dreamscale.htmflow.api.spirit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ActiveLinksNetworkDto {

    //list of spirit members actively linked

    private UUID networkId;

    private UUID myId;
    private String myName;

    private List<SpiritLinkDto> spiritLinks;

    public ActiveLinksNetworkDto() {
        spiritLinks = new ArrayList<>();
    }

    public void addSpiritLink(SpiritLinkDto spiritLink) {
        spiritLinks.add(spiritLink);
    }

    public boolean containsOneLink() {
        return (spiritLinks.size() == 1);
    }

    public boolean isEmpty() {
        return spiritLinks.isEmpty();
    }
}
