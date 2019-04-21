package com.dreamscale.htmflow.core.domain.journal;

import com.dreamscale.htmflow.core.domain.member.json.Member;
import com.dreamscale.htmflow.core.domain.member.json.PairingMemberList;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "journal_entry_view")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class JournalEntryEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private LocalDateTime position;
    private String description;

    private String taskName;
    private String projectName;
    private String taskSummary;
    private Integer flameRating;

    private Boolean linked;
    private String finishStatus;
    private LocalDateTime finishTime;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID projectId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID taskId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID memberId;

    String journalEntryType;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circleId;

    private String linkedMembers;

    public List<Member> getLinkedMembers() {
        List<Member> members = null;

        if (linkedMembers != null) {
            ObjectMapper objectMapper = new ObjectMapper();

            PairingMemberList memberList = null;
            try {
                memberList = objectMapper.readValue(linkedMembers, PairingMemberList.class);
                if (memberList != null) {
                    members = memberList.getMemberList();
                }
            } catch (IOException e) {
                log.error("Unable to parse JSON For linkedMembers: "+linkedMembers, e);
                throw new RuntimeException("Unable to parse JSON", e);
            }

        }

        return members;
    }

}
