package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity(name = "one_time_ticket")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OneTimeTicketEntity {

    public static final String EMAIL_PROP = "email";

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    private TicketType ticketType;

    private String ticketCode;

    private String jsonProps;

    private LocalDateTime issueDate;

    private LocalDateTime expirationDate;

    @Transient
    private Map<String, Object> propsMap;

    public Map<String, Object> getProps() {
        if (propsMap != null) {
            return propsMap;
        }

        if (jsonProps != null) {
            propsMap = (HashMap<String, Object>) JSONTransformer.fromJson(jsonProps, HashMap.class);
        }

        return propsMap;
    }

    public String getEmailProp() {
        Map<String, Object> props = getProps();

        if (props != null) {
            return (String) props.get(EMAIL_PROP);
        }

        return null;
    }

}
