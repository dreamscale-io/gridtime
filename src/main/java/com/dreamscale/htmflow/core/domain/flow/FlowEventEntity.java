
package com.dreamscale.htmflow.core.domain.flow;


import com.dreamscale.htmflow.api.event.EventType;
import lombok.*;

import javax.persistence.*;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "flow_event")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowEventEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "flow_event_seq_gen")
	@SequenceGenerator(name = "flow_event_seq_gen", sequenceName = "flow_event_seq")
	private Long id;

	@org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
	@Column(name = "member_id")
	private UUID memberId;

	private LocalDateTime timePosition;

	@Enumerated(EnumType.STRING)
	private EventType eventType;

	private String metadata;

	@Transient
	private final MetadataFields metadataFields = new MetadataFields();

	public void setMetadataField(FlowEventMetadataField field, Object value) {
		metadataFields.set(field.name(), value);
		metadata = metadataFields.toJson();
	}

	public String getMetadataValue(FlowEventMetadataField field) {
		return metadataFields.get(field.name());
	}

	@PostLoad
	private void postLoad() {
		metadataFields.fromJson(metadata);
	}

}
