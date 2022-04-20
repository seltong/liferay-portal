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

package com.liferay.object.web.internal.object.definitions.display.context;

import com.liferay.dynamic.data.mapping.expression.DDMExpressionFunctionTracker;
import com.liferay.frontend.data.set.model.FDSActionDropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectValidationRule;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.object.validation.rule.ObjectValidationRuleEngineServicesTracker;
import com.liferay.petra.function.UnsafeConsumer;
import com.liferay.petra.portlet.url.builder.PortletURLBuilder;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.HashMapBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Selton Guedes
 */
public class ObjectDefinitionsValidationsDisplayContext
	extends BaseObjectDefinitionsDisplayContext {

	public ObjectDefinitionsValidationsDisplayContext(
		HttpServletRequest httpServletRequest,
		ModelResourcePermission<ObjectDefinition>
			objectDefinitionModelResourcePermission,
		DDMExpressionFunctionTracker ddmExpressionFunctionTracker,
		ObjectValidationRuleEngineServicesTracker
			objectValidationRuleEngineServicesTracker) {

		super(httpServletRequest, objectDefinitionModelResourcePermission);

		_ddmExpressionFunctionTracker = ddmExpressionFunctionTracker;
		_objectValidationRuleEngineServicesTracker =
			objectValidationRuleEngineServicesTracker;
	}

	public List<FDSActionDropdownItem> getFDSActionDropdownItems()
		throws Exception {

		return Arrays.asList(
			new FDSActionDropdownItem(
				PortletURLBuilder.create(
					getPortletURL()
				).setMVCRenderCommandName(
					"/object_definitions/edit_object_validation_rule"
				).setParameter(
					"objectValidationRuleId", "{id}"
				).setWindowState(
					LiferayWindowState.POP_UP
				).buildString(),
				"view", "view",
				LanguageUtil.get(objectRequestHelper.getRequest(), "view"),
				"get", null, "sidePanel"),
			new FDSActionDropdownItem(
				"/o/object-admin/v1.0/object-validation-rules/{id}", "trash",
				"delete",
				LanguageUtil.get(objectRequestHelper.getRequest(), "delete"),
				"delete", "delete", "async"));
	}

	public List<HashMap<String, Object>> getObjectValidationElements(
		String engine) {

		List<HashMap<String, Object>> elements = new ArrayList<>();

		Collections.addAll(
			elements,
			HashMapBuilder.<String, Object>put(
				"items",
				Stream.of(
					ObjectFieldLocalServiceUtil.getObjectFields(
						getObjectDefinitionId())
				).flatMap(
					List::stream
				).map(
					field -> _createObjectValidationElementItem(
						field.getName(),
						field.getLabel(objectRequestHelper.getLocale()))
				).collect(
					Collectors.toList()
				)
			).put(
				"label", "Fields"
			).build());

		if (engine.equals("ddm")) {
			Collections.addAll(
				elements,
				HashMapBuilder.<String, Object>put(
					"items",
					Arrays.asList(
						_createObjectValidationElementItem(
							"AND",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "and")),
						_createObjectValidationElementItem(
							"OR",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "or")),
						_createObjectValidationElementItem(
							"field_reference + field_reference2",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "plus")),
						_createObjectValidationElementItem(
							"field_reference - field_reference2",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "minus")),
						_createObjectValidationElementItem(
							"field_reference / field_reference2",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "divided-by")),
						_createObjectValidationElementItem(
							"field_reference * field_reference2",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "times")),
						_createObjectValidationElementItem(
							"field_reference = field_reference - " +
								"field_reference2",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"equals-or-atribute")))
				).put(
					"label", "Operators"
				).build(),
				HashMapBuilder.<String, Object>put(
					"items",
					Arrays.asList(
						_createObjectValidationElementItem(
							"contains(field_reference, \"parameter\")",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "contains")),
						_createObjectValidationElementItem(
							"NOT(contains(field_reference, \"parameter\"))",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"does-not-contain")),
						_createObjectValidationElementItem(
							"isURL(field_reference)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "is-a-url")),
						_createObjectValidationElementItem(
							"isEmailAddress(field_reference)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-an-email")),
						_createObjectValidationElementItem(
							"match(field_reference, \"parameter\")",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "matches")),
						_createObjectValidationElementItem(
							"field_reference == parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-equal-to")),
						_createObjectValidationElementItem(
							"field_reference != parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-not-equal-to")),
						_createObjectValidationElementItem(
							"isEmpty(parameter)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "is-empty")),
						_createObjectValidationElementItem(
							"concat(parameters)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "concat")),
						_createObjectValidationElementItem(
							"field_reference == parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-equal-to")),
						_createObjectValidationElementItem(
							"field_reference != parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-not-equal-to")),
						_createObjectValidationElementItem(
							"field_reference > parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-greater-than")),
						_createObjectValidationElementItem(
							"field_reference >= parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-greater-than-or-equal-to")),
						_createObjectValidationElementItem(
							"field_reference < parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-less-than")),
						_createObjectValidationElementItem(
							"field_reference <= parameter",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"is-less-than-or-equal-to")),
						_createObjectValidationElementItem(
							"isDecimal(parameter)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "is-decimal")),
						_createObjectValidationElementItem(
							"isInteger(parameter)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "is-integer")),
						_createObjectValidationElementItem(
							"sum(parameter)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "sum")),
						_createObjectValidationElementItem(
							"futureDates(field_reference, parameter)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(),
								"future-dates")),
						_createObjectValidationElementItem(
							"pastDates(field_reference, parameter)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "past-dates")),
						_createObjectValidationElementItem(
							"futureDates(name, startsFrom, date, unit, " +
								"quantity, endsOn, date, unit, quantity)",
							LanguageUtil.get(
								objectRequestHelper.getLocale(), "range")))
				).put(
					"label", "Functions"
				).build());
		}

		return elements;
	}

	public List<Map<String, String>> getObjectValidationRuleEngines() {
		return Stream.of(
			_objectValidationRuleEngineServicesTracker.
				getObjectValidationRuleEngines()
		).flatMap(
			List::stream
		).map(
			objectValidationRuleEngine -> HashMapBuilder.put(
				"label",
				LanguageUtil.get(
					objectRequestHelper.getLocale(),
					objectValidationRuleEngine.getName())
			).put(
				"name", objectValidationRuleEngine.getName()
			).build()
		).sorted(
			Comparator.comparing(p -> p.get("label"))
		).collect(
			Collectors.toList()
		);
	}

	public HashMap<String, Object> getProps(
			ObjectValidationRule objectValidationRule)
		throws PortalException {

		return HashMapBuilder.<String, Object>put(
			"objectValidationRule",
			HashMapBuilder.<String, Object>put(
				"active", objectValidationRule.isActive()
			).put(
				"engine", objectValidationRule.getEngine()
			).put(
				"engineLabel",
				LanguageUtil.get(
					objectRequestHelper.getLocale(),
					objectValidationRule.getEngine())
			).put(
				"errorLabel", objectValidationRule.getErrorLabel()
			).put(
				"id", objectValidationRule.getObjectValidationRuleId()
			).put(
				"name", objectValidationRule.getName()
			).put(
				"script", objectValidationRule.getScript()
			).build()
		).put(
			"objectValidationRuleElements",
			getObjectValidationElements(objectValidationRule.getEngine())
		).put(
			"objectValidationRuleEngines", getObjectValidationRuleEngines()
		).put(
			"readOnly", !hasUpdateObjectDefinitionPermission()
		).build();
	}

	@Override
	protected String getAPIURI() {
		return "/object-validation-rules";
	}

	@Override
	protected UnsafeConsumer<DropdownItem, Exception>
		getCreationMenuDropdownItemUnsafeConsumer() {

		return dropdownItem -> {
			dropdownItem.setHref("addObjectValidation");
			dropdownItem.setLabel(
				LanguageUtil.get(
					objectRequestHelper.getRequest(), "add-object-validation"));
			dropdownItem.setTarget("event");
		};
	}

	private HashMap<String, Object> _createObjectValidationElementItem(
		String content, String label) {

		return HashMapBuilder.<String, Object>put(
			"content", content
		).put(
			"label", label
		).put(
			"tooltip", "placeholder"
		).build();
	}

	private final DDMExpressionFunctionTracker _ddmExpressionFunctionTracker;
	private final ObjectValidationRuleEngineServicesTracker
		_objectValidationRuleEngineServicesTracker;

}