
package com.dreamscale.gridtime.core.domain.flow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;

@Slf4j
public class MetadataFields {

	private HashMap fieldsMap = new HashMap();
	private ObjectMapper mapper = new ObjectMapper();

	public void set(String key, Object value) {
		fieldsMap.put(key, value);
	}

	public String get(String key) {
		Object value = fieldsMap.get(key);
		return value != null ? value.toString() : null;
	}

	public void fromJson(String metadata) {
		try {
			fieldsMap = toMetadataFieldsMap(metadata);
		} catch (IOException ex) {
			log.error("Failed to convert metadata into map, json=" + metadata, ex);
		}
	}

	public String toJson() {
		try {
			return mapper.writeValueAsString(fieldsMap);
		} catch (JsonProcessingException ex) {
			log.error("Failed to convert metadata into json, map=" + fieldsMap, ex);
			return "";
		}
	}

	private HashMap toMetadataFieldsMap(String metadata) throws IOException {
		if (metadata == null) {
			return new HashMap();
		} else {
			return mapper.readValue(metadata, HashMap.class);
		}
	}

}
