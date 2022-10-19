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

package com.liferay.object.util;

import com.liferay.dynamic.data.mapping.expression.CreateExpressionRequest;
import com.liferay.dynamic.data.mapping.expression.DDMExpression;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.model.ObjectField;
import com.liferay.object.model.ObjectFieldSetting;
import com.liferay.object.service.ObjectFieldLocalServiceUtil;
import com.liferay.object.service.ObjectFieldSettingLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.Serializable;

import java.text.DateFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Selton Guedes
 */
public class ObjectFieldFormulaUtil {

	public static Map<String, Serializable> evaluate(
			DDMExpressionFactory ddmExpressionFactory, long objectDefinitionId,
			Map<String, Serializable> values)
		throws PortalException {

		List<ObjectField> objectFields =
			ObjectFieldLocalServiceUtil.getObjectFields(objectDefinitionId);

		for (ObjectField objectField : objectFields) {
			if (!objectField.compareBusinessType(
					ObjectFieldConstants.BUSINESS_TYPE_FORMULA)) {

				continue;
			}

			List<ObjectFieldSetting> objectFieldSettings =
				ObjectFieldSettingLocalServiceUtil.
					getObjectFieldObjectFieldSettings(
						objectField.getObjectFieldId());

			Map<String, Object> objectFieldSettingMap = new HashMap<>();

			for (ObjectFieldSetting objectFieldSetting : objectFieldSettings) {
				objectFieldSettingMap.put(
					objectFieldSetting.getName(),
					objectFieldSetting.getValue());
			}

			Object script = objectFieldSettingMap.get("script");

			if (script == null) {
				break;
			}

			DDMExpression<Serializable> ddmExpression =
				ddmExpressionFactory.createExpression(
					CreateExpressionRequest.Builder.newBuilder(
						String.valueOf(script)
					).build());

			ddmExpression.setVariables(new HashMap<>(values));

			try {
				values.put(
					objectField.getName(),
					_getOutputValue(
						String.valueOf(objectFieldSettingMap.get("output")),
						ddmExpression.evaluate()));
			}
			catch (PortalException portalException) {
				_log.error(portalException);
			}
		}

		return values;
	}

	private static Serializable _getOutputValue(
		String outputType, Object value) {

		if (StringUtil.equals(
				outputType, ObjectFieldConstants.BUSINESS_TYPE_TEXT)) {

			return value.toString();
		}

		if (StringUtil.equals(
				outputType, ObjectFieldConstants.BUSINESS_TYPE_INTEGER)) {

			return GetterUtil.getInteger(value);
		}

		if (StringUtil.equals(
				outputType, ObjectFieldConstants.BUSINESS_TYPE_DATE)) {

			DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
				"dd/MM/yyyy");

			return dateFormat.format(value);
		}

		if (StringUtil.equals(
				outputType, ObjectFieldConstants.BUSINESS_TYPE_BOOLEAN)) {

			return GetterUtil.getBoolean(value);
		}

		if (StringUtil.equals(
				outputType, ObjectFieldConstants.BUSINESS_TYPE_DECIMAL)) {

			return GetterUtil.getDouble(value);
		}

		return null;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectFieldFormulaUtil.class);

}