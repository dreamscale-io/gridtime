
package com.dreamscale.htmflow.core.domain.flow;

import lombok.*;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "flow_activity")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlowActivityEntity implements Cloneable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "flow_activity_seq_gen")
	@SequenceGenerator(name = "flow_activity_seq_gen", sequenceName = "flow_activity_seq")
	private Long id;

	@org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
	@Column(name = "member_id")
	private UUID memberId;

	@Column(name = "start_time")
	private LocalDateTime start;
	@Column(name = "end_time")
	private LocalDateTime end;

	public Duration getDuration() {
		return Duration.between(start, end);
	}

	@Enumerated(EnumType.STRING)
	private FlowActivityType activityType;
	private String metadata;

	@Transient
	private final MetadataFields metadataFields = new MetadataFields();

	public void setMetadataField(FlowActivityMetadataField field, Object value) {
		metadataFields.set(field.name(), value);
		metadata = metadataFields.toJson();
	}

	public String getMetadataValue(FlowActivityMetadataField field) {
		return metadataFields.get(field.name());
	}

	@PostLoad
	private void postLoad() {
		metadataFields.fromJson(metadata);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
