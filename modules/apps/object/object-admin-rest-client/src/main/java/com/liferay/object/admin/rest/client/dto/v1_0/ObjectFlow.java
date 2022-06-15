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

package com.liferay.object.admin.rest.client.dto.v1_0;

import com.liferay.object.admin.rest.client.function.UnsafeSupplier;
import com.liferay.object.admin.rest.client.serdes.v1_0.ObjectFlowSerDes;

import java.io.Serializable;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class ObjectFlow implements Cloneable, Serializable {

	public static ObjectFlow toDTO(String json) {
		return ObjectFlowSerDes.toDTO(json);
	}

	public Object getCurrentState() {
		return currentState;
	}

	public void setCurrentState(Object currentState) {
		this.currentState = currentState;
	}

	public void setCurrentState(
		UnsafeSupplier<Object, Exception> currentStateUnsafeSupplier) {

		try {
			currentState = currentStateUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Object currentState;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(UnsafeSupplier<Long, Exception> idUnsafeSupplier) {
		try {
			id = idUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long id;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setName(UnsafeSupplier<String, Exception> nameUnsafeSupplier) {
		try {
			name = nameUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String name;

	public Long getObjectDefinitionId() {
		return objectDefinitionId;
	}

	public void setObjectDefinitionId(Long objectDefinitionId) {
		this.objectDefinitionId = objectDefinitionId;
	}

	public void setObjectDefinitionId(
		UnsafeSupplier<Long, Exception> objectDefinitionIdUnsafeSupplier) {

		try {
			objectDefinitionId = objectDefinitionIdUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Long objectDefinitionId;

	public Map[] getStates() {
		return states;
	}

	public void setStates(Map[] states) {
		this.states = states;
	}

	public void setStates(
		UnsafeSupplier<Map[], Exception> statesUnsafeSupplier) {

		try {
			states = statesUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Map[] states;

	@Override
	public ObjectFlow clone() throws CloneNotSupportedException {
		return (ObjectFlow)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof ObjectFlow)) {
			return false;
		}

		ObjectFlow objectFlow = (ObjectFlow)object;

		return Objects.equals(toString(), objectFlow.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return ObjectFlowSerDes.toJSON(this);
	}

}