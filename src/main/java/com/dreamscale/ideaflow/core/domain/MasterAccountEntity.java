package com.dreamscale.ideaflow.core.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "master_account")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MasterAccountEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Column(name = "master_email")
    private String masterEmail;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "activation_code")
    private String activationCode;

    @Column(name = "activation_date")
    private LocalDateTime activationDate;

    @Column(name = "api_key")
    private String apiKey;

}
