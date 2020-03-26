package com.dreamscale.gridtime.core.domain.member;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "root_account")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RootAccountEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Column(name = "root_email")
    private String rootEmail;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "activation_date")
    private LocalDateTime activationDate;

    @Column(name = "api_key")
    private String apiKey;

}
