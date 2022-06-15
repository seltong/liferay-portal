/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.object.admin.rest.client.serdes.v1_0;

import com.liferay.object.admin.rest.client.dto.v1_0.ObjectFlow;
import com.liferay.object.admin.rest.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class ObjectFlowSerDes {

	public static ObjectFlow toDTO(String json) {
		ObjectFlowJSONParser objectFlowJSONParser = new ObjectFlowJSONParser();

		return objectFlowJSONParser.parseToDTO(json);
	}

	public static ObjectFlow[] toDTOs(String json) {
		ObjectFlowJSONParser objectFlowJSONParser = new ObjectFlowJSONParser();

		return objectFlowJSONParser.parseToDTOs(json);
	}

	public static String toJSON(ObjectFlow objectFlow) {
		if (objectFlow == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (objectFlow.getCurrentState() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"currentState\": ");

			if (objectFlow.getCurrentState() instanceof String) {
				sb.append("\"");
				sb.append((String)objectFlow.getCurrentState());
				sb.append("\"");
			}
			else {
				sb.append(objectFlow.getCurrentState());
			}
		}

		if (objectFlow.getId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"id\": ");

			sb.append(objectFlow.getId());
		}

		if (objectFlow.getName() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"name\": ");

			sb.append("\"");

			sb.append(_escape(objectFlow.getName()));

			sb.append("\"");
		}

		if (objectFlow.getObjectDefinitionId() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"objectDefinitionId\": ");

			sb.append(objectFlow.getObjectDefinitionId());
		}

		if (objectFlow.getStates() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"states\": ");

			sb.append("[");

			for (int i = 0; i < objectFlow.getStates().length; i++) {
				sb.append(objectFlow.getStates()[i]);

				if ((i + 1) < objectFlow.getStates().length) {
					sb.append(", ");
				}
			}

			sb.append("]");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		ObjectFlowJSONParser objectFlowJSONParser = new ObjectFlowJSONParser();

		return objectFlowJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(ObjectFlow objectFlow) {
		if (objectFlow == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (objectFlow.getCurrentState() == null) {
			map.put("currentState", null);
		}
		else {
			map.put(
				"currentState", String.valueOf(objectFlow.getCurrentState()));
		}

		if (objectFlow.getId() == null) {
			map.put("id", null);
		}
		else {
			map.put("id", String.valueOf(objectFlow.getId()));
		}

		if (objectFlow.getName() == null) {
			map.put("name", null);
		}
		else {
			map.put("name", String.valueOf(objectFlow.getName()));
		}

		if (objectFlow.getObjectDefinitionId() == null) {
			map.put("objectDefinitionId", null);
		}
		else {
			map.put(
				"objectDefinitionId",
				String.valueOf(objectFlow.getObjectDefinitionId()));
		}

		if (objectFlow.getStates() == null) {
			map.put("states", null);
		}
		else {
			map.put("states", String.valueOf(objectFlow.getStates()));
		}

		return map;
	}

	public static class ObjectFlowJSONParser
		extends BaseJSONParser<ObjectFlow> {

		@Override
		protected ObjectFlow createDTO() {
			return new ObjectFlow();
		}

		@Override
		protected ObjectFlow[] createDTOArray(int size) {
			return new ObjectFlow[size];
		}

		@Override
		protected void setField(
			ObjectFlow objectFlow, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "currentState")) {
				if (jsonParserFieldValue != null) {
					objectFlow.setCurrentState((Object)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(jsonParserFieldName, "id")) {
				if (jsonParserFieldValue != null) {
					objectFlow.setId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "name")) {
				if (jsonParserFieldValue != null) {
					objectFlow.setName((String)jsonParserFieldValue);
				}
			}
			else if (Objects.equals(
						jsonParserFieldName, "objectDefinitionId")) {

				if (jsonParserFieldValue != null) {
					objectFlow.setObjectDefinitionId(
						Long.valueOf((String)jsonParserFieldValue));
				}
			}
			else if (Objects.equals(jsonParserFieldName, "states")) {
				if (jsonParserFieldValue != null) {
					objectFlow.setStates((Map[])jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			Class<?> valueClass = value.getClass();

			if (value instanceof Map) {
				sb.append(_toJSON((Map)value));
			}
			else if (valueClass.isArray()) {
				Object[] values = (Object[])value;

				sb.append("[");

				for (int i = 0; i < values.length; i++) {
					sb.append("\"");
					sb.append(_escape(values[i]));
					sb.append("\"");

					if ((i + 1) < values.length) {
						sb.append(", ");
					}
				}

				sb.append("]");
			}
			else if (value instanceof String) {
				sb.append("\"");
				sb.append(_escape(entry.getValue()));
				sb.append("\"");
			}
			else {
				sb.append(String.valueOf(entry.getValue()));
			}

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

}