package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity(name = "ticket_tombstone")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketTombstoneEntity {

    public static final String EMAIL_PROP = "email";
    public static final String ORGANIZATION_ID_PROP = "organizationId";
    public static final String TEAM_ID_PROP = "teamId";

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID ticketId;

    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    private TicketType ticketType;

    private String ticketCode;

    private String jsonProps;

    private LocalDateTime issueDate;

    private LocalDateTime expirationDate;

    private UUID usedBy;

    private LocalDateTime ripDate;

    @Enumerated(EnumType.STRING)
    private RipType ripType;

    @Transient
    private Map<String, String> propsMap;

    public Map<String, String> getProps() {
        if (propsMap != null) {
            return propsMap;
        }

        if (jsonProps != null) {
            propsMap = (HashMap<String, String>) JSONTransformer.fromJson(jsonProps, HashMap.class);
        }

        return propsMap;
    }

    public String getEmailProp() {
        Map<String, String> props = getProps();

        if (props != null) {
            return props.get(EMAIL_PROP);
        }

        return null;
    }

    public UUID getOrganizationIdProp() {
        Map<String, String> props = getProps();

        if (props != null) {
            String prop = props.get(ORGANIZATION_ID_PROP);
            if (prop != null) {
                return UUID.fromString(prop);
            }
        }

        return null;
    }

    public UUID getTeamIdProp() {
        Map<String, String> props = getProps();

        if (props != null) {
            String prop = props.get(TEAM_ID_PROP);
            if (prop != null) {
                return UUID.fromString(prop);
            }
        }

        return null;
    }

}
