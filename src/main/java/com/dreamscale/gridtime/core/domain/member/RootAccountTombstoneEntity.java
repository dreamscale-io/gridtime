package com.dreamscale.gridtime.core.domain.member;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "root_account_tombstone")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RootAccountTombstoneEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID rootAccountId;

    @Column(name = "root_email")
    private String rootEmail;

    @Column(name = "root_username")
    private String rootUsername;

    @Column(name = "lowercase_root_username")
    private String lowercaseRootUsername;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    @Column(name = "activation_date")
    private LocalDateTime activationDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    private boolean isEmailValidated;

    @Column(name = "api_key")
    private String apiKey;

    private LocalDateTime ripDate;

}
