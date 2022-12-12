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

package com.liferay.object.service.impl;

import com.liferay.dynamic.data.mapping.expression.CreateExpressionRequest;
import com.liferay.dynamic.data.mapping.expression.DDMExpressionFactory;
import com.liferay.object.action.executor.ObjectActionExecutorRegistry;
import com.liferay.object.constants.ObjectActionConstants;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.constants.ObjectActionTriggerConstants;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.exception.NoSuchObjectFieldException;
import com.liferay.object.exception.ObjectActionConditionExpressionException;
import com.liferay.object.exception.ObjectActionErrorMessageException;
import com.liferay.object.exception.ObjectActionLabelException;
import com.liferay.object.exception.ObjectActionNameException;
import com.liferay.object.exception.ObjectActionParametersException;
import com.liferay.object.exception.ObjectActionTriggerKeyException;
import com.liferay.object.exception.ObjectFieldRelationshipTypeException;
import com.liferay.object.internal.action.trigger.util.ObjectActionTriggerUtil;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntryTable;
import com.liferay.object.model.ObjectField;
import com.liferay.object.scripting.exception.ObjectScriptingException;
import com.liferay.object.scripting.validator.ObjectScriptingValidator;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.base.ObjectActionLocalServiceBaseImpl;
import com.liferay.object.service.persistence.ObjectDefinitionPersistence;
import com.liferay.object.service.persistence.ObjectFieldPersistence;
import com.liferay.object.util.LocalizedMapUtil;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.messaging.MessageBus;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marco Leo
 */
@Component(
	property = "model.class.name=com.liferay.object.model.ObjectAction",
	service = AopService.class
)
public class ObjectActionLocalServiceImpl
	extends ObjectActionLocalServiceBaseImpl {

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public ObjectAction addObjectAction(
			long userId, long objectDefinitionId, boolean active,
			String conditionExpression, String description,
			Map<Locale, String> errorMessageMap, Map<Locale, String> labelMap,
			String name, String objectActionExecutorKey,
			String objectActionTriggerKey,
			UnicodeProperties parametersUnicodeProperties)
		throws PortalException {

		_validateErrorMessage(errorMessageMap, objectActionTriggerKey);
		_validateLabel(labelMap);
		_validateName(0, objectDefinitionId, name);
		_validateObjectActionExecutorKey(objectActionExecutorKey);
		_validateObjectActionTriggerKey(
			conditionExpression, objectActionTriggerKey);
		_validateParametersUnicodeProperties(
			conditionExpression, objectActionExecutorKey,
			objectActionTriggerKey, parametersUnicodeProperties);

		ObjectDefinition objectDefinition =
			_objectDefinitionPersistence.findByPrimaryKey(objectDefinitionId);

		ObjectAction objectAction = objectActionPersistence.create(
			counterLocalService.increment());

		User user = _userLocalService.getUser(userId);

		objectAction.setCompanyId(user.getCompanyId());
		objectAction.setUserId(user.getUserId());
		objectAction.setUserName(user.getFullName());

		objectAction.setObjectDefinitionId(
			objectDefinition.getObjectDefinitionId());
		objectAction.setActive(active);
		objectAction.setConditionExpression(conditionExpression);
		objectAction.setDescription(description);
		objectAction.setErrorMessageMap(
			errorMessageMap, LocaleUtil.getSiteDefault());
		objectAction.setLabelMap(labelMap, LocaleUtil.getSiteDefault());
		objectAction.setName(name);
		objectAction.setObjectActionExecutorKey(objectActionExecutorKey);
		objectAction.setObjectActionTriggerKey(objectActionTriggerKey);
		objectAction.setParameters(parametersUnicodeProperties.toString());
		objectAction.setStatus(ObjectActionConstants.STATUS_NEVER_RAN);

		return objectActionPersistence.update(objectAction);
	}

	@Indexable(type = IndexableType.DELETE)
	@Override
	public ObjectAction deleteObjectAction(long objectActionId)
		throws PortalException {

		ObjectAction objectAction = objectActionPersistence.findByPrimaryKey(
			objectActionId);

		return deleteObjectAction(objectAction);
	}

	@Indexable(type = IndexableType.DELETE)
	@Override
	@SystemEvent(type = SystemEventConstants.TYPE_DELETE)
	public ObjectAction deleteObjectAction(ObjectAction objectAction) {
		return objectActionPersistence.remove(objectAction);
	}

	@Override
	public void deleteObjectActions(long objectDefinitionId)
		throws PortalException {

		for (ObjectAction objectAction :
				objectActionPersistence.findByObjectDefinitionId(
					objectDefinitionId)) {

			objectActionLocalService.deleteObjectAction(objectAction);
		}
	}

	@Override
	public ObjectAction getObjectAction(
			long objectDefinitionId, String name, String objectActionTriggerKey)
		throws PortalException {

		return objectActionPersistence.findByODI_A_N_OATK(
			objectDefinitionId, true, name, objectActionTriggerKey);
	}

	@Override
	public List<ObjectAction> getObjectActions(long objectDefinitionId) {
		return objectActionPersistence.findByObjectDefinitionId(
			objectDefinitionId);
	}

	@Override
	public List<ObjectAction> getObjectActions(
		long objectDefinitionId, String objectActionTriggerKey) {

		return objectActionPersistence.findByO_A_OATK(
			objectDefinitionId, true, objectActionTriggerKey);
	}

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public ObjectAction updateObjectAction(
			long objectActionId, boolean active, String conditionExpression,
			String description, Map<Locale, String> errorMessageMap,
			Map<Locale, String> labelMap, String name,
			String objectActionExecutorKey, String objectActionTriggerKey,
			UnicodeProperties parametersUnicodeProperties)
		throws PortalException {

		_validateErrorMessage(errorMessageMap, objectActionTriggerKey);
		_validateLabel(labelMap);
		_validateObjectActionExecutorKey(objectActionExecutorKey);
		_validateParametersUnicodeProperties(
			conditionExpression, objectActionExecutorKey,
			objectActionTriggerKey, parametersUnicodeProperties);

		ObjectAction objectAction = objectActionPersistence.findByPrimaryKey(
			objectActionId);

		objectAction.setActive(active);
		objectAction.setConditionExpression(conditionExpression);
		objectAction.setDescription(description);
		objectAction.setErrorMessageMap(
			errorMessageMap, LocaleUtil.getSiteDefault());
		objectAction.setLabelMap(labelMap, LocaleUtil.getSiteDefault());
		objectAction.setObjectActionExecutorKey(objectActionExecutorKey);
		objectAction.setParameters(parametersUnicodeProperties.toString());
		objectAction.setStatus(ObjectActionConstants.STATUS_NEVER_RAN);

		ObjectDefinition objectDefinition =
			_objectDefinitionPersistence.findByPrimaryKey(
				objectAction.getObjectDefinitionId());

		if (GetterUtil.getBoolean(PropsUtil.get("feature.flag.LPS-166918")) &&
			objectDefinition.isApproved()) {

			return objectActionPersistence.update(objectAction);
		}

		_validateName(
			objectActionId, objectDefinition.getObjectDefinitionId(), name);
		_validateObjectActionTriggerKey(
			conditionExpression, objectActionTriggerKey);

		objectAction.setName(name);
		objectAction.setObjectActionTriggerKey(objectActionTriggerKey);

		return objectActionPersistence.update(objectAction);
	}

	@Indexable(type = IndexableType.REINDEX)
	@Override
	public ObjectAction updateStatus(long objectActionId, int status)
		throws PortalException {

		ObjectAction objectAction = objectActionPersistence.findByPrimaryKey(
			objectActionId);

		objectAction.setStatus(status);

		return objectActionPersistence.update(objectAction);
	}

	private void _addSystemObjectFields(
			String dbTableName, ObjectDefinition objectDefinition,
			String pkObjectFieldName, boolean system, long userId)
		throws PortalException {

		_objectFieldLocalService.addSystemObjectField(
			userId, objectDefinition.getObjectDefinitionId(),
			ObjectFieldConstants.BUSINESS_TYPE_TEXT,
			ObjectEntryTable.INSTANCE.userName.getName(), dbTableName,
			ObjectFieldConstants.DB_TYPE_STRING, null, false, false, null,
			LocalizedMapUtil.getLocalizedMap(
				_language.get(LocaleUtil.getDefault(), "author")),
			"creator", false, false);

		_objectFieldLocalService.addSystemObjectField(
			userId, objectDefinition.getObjectDefinitionId(),
			ObjectFieldConstants.BUSINESS_TYPE_DATE,
			ObjectEntryTable.INSTANCE.createDate.getName(), dbTableName,
			ObjectFieldConstants.DB_TYPE_DATE, null, false, false, null,
			LocalizedMapUtil.getLocalizedMap(
				_language.get(LocaleUtil.getDefault(), "create-date")),
			"createDate", false, false);

		_objectFieldLocalService.addSystemObjectField(
			userId, objectDefinition.getObjectDefinitionId(),
			ObjectFieldConstants.BUSINESS_TYPE_TEXT,
			ObjectEntryTable.INSTANCE.externalReferenceCode.getName(),
			dbTableName, ObjectFieldConstants.DB_TYPE_STRING, null, false,
			false, null,
			LocalizedMapUtil.getLocalizedMap(
				_language.get(
					LocaleUtil.getDefault(), "external-reference-code")),
			"externalReferenceCode", false, false);

		String dbColumnName = ObjectEntryTable.INSTANCE.objectEntryId.getName();

		if (system) {
			dbColumnName = pkObjectFieldName;
		}

		_objectFieldLocalService.addSystemObjectField(
			userId, objectDefinition.getObjectDefinitionId(),
			ObjectFieldConstants.BUSINESS_TYPE_LONG_INTEGER, dbColumnName,
			dbTableName, ObjectFieldConstants.DB_TYPE_LONG, null, true, true,
			null,
			LocalizedMapUtil.getLocalizedMap(
				_language.get(LocaleUtil.getDefault(), "id")),
			"id", false, false);

		_objectFieldLocalService.addSystemObjectField(
			userId, objectDefinition.getObjectDefinitionId(),
			ObjectFieldConstants.BUSINESS_TYPE_DATE,
			ObjectEntryTable.INSTANCE.modifiedDate.getName(), dbTableName,
			ObjectFieldConstants.DB_TYPE_DATE, null, false, false, null,
			LocalizedMapUtil.getLocalizedMap(
				_language.get(LocaleUtil.getDefault(), "modified-date")),
			"modifiedDate", false, false);

		_objectFieldLocalService.addSystemObjectField(
			userId, objectDefinition.getObjectDefinitionId(),
			ObjectFieldConstants.BUSINESS_TYPE_TEXT,
			ObjectEntryTable.INSTANCE.status.getName(), dbTableName,
			ObjectFieldConstants.DB_TYPE_INTEGER, null, false, false, null,
			LocalizedMapUtil.getLocalizedMap(
				_language.get(LocaleUtil.getDefault(), "status")),
			"status", false, false);
	}

	private ObjectDefinition _updateTitleObjectFieldId(
			ObjectDefinition objectDefinition, String titleObjectFieldName)
		throws PortalException {

		if (Validator.isNull(titleObjectFieldName)) {
			titleObjectFieldName = "id";
		}

		ObjectField objectField = _objectFieldPersistence.findByODI_N(
			objectDefinition.getObjectDefinitionId(), titleObjectFieldName);

		_validateObjectFieldId(
			objectDefinition, objectField.getObjectFieldId());

		objectDefinition.setTitleObjectFieldId(objectField.getObjectFieldId());

		return _objectDefinitionPersistence.update(objectDefinition);
	}

	private void _validateErrorMessage(
			Map<Locale, String> errorMessageMap, String objectActionTriggerKey)
		throws PortalException {

		Locale locale = LocaleUtil.getSiteDefault();

		if (GetterUtil.getBoolean(PropsUtil.get("feature.flag.LPS-166918")) &&
			Objects.equals(
				objectActionTriggerKey,
				ObjectActionTriggerConstants.KEY_STANDALONE) &&
			((errorMessageMap == null) ||
			 Validator.isNull(errorMessageMap.get(locale)))) {

			throw new ObjectActionErrorMessageException(
				"Error message is null for locale " + locale.getDisplayName());
		}
	}

	private void _validateLabel(Map<Locale, String> labelMap)
		throws PortalException {

		Locale locale = LocaleUtil.getSiteDefault();

		if (GetterUtil.getBoolean(PropsUtil.get("feature.flag.LPS-166918")) &&
			((labelMap == null) || Validator.isNull(labelMap.get(locale)))) {

			throw new ObjectActionLabelException(
				"Label is null for locale " + locale.getDisplayName());
		}
	}

	private void _validateName(
			long objectActionId, long objectDefinitionId, String name)
		throws PortalException {

		if (Validator.isNull(name)) {
			throw new ObjectActionNameException.MustNotBeNull();
		}

		if (!GetterUtil.getBoolean(PropsUtil.get("feature.flag.LPS-166918"))) {
			return;
		}

		char[] nameCharArray = name.toCharArray();

		for (char c : nameCharArray) {
			if (!Validator.isChar(c) && !Validator.isDigit(c)) {
				throw new ObjectActionNameException.
					MustOnlyContainLettersAndDigits();
			}
		}

		if (nameCharArray.length > 41) {
			throw new ObjectActionNameException.MustBeLessThan41Characters();
		}

		ObjectAction objectAction = objectActionPersistence.fetchByODI_N(
			objectDefinitionId, name);

		if ((objectAction != null) &&
			(objectAction.getObjectActionId() != objectActionId)) {

			throw new ObjectActionNameException.MustNotBeDuplicate(name);
		}
	}

	private void _validateObjectActionExecutorKey(
			String objectActionExecutorKey)
		throws PortalException {

		if (!_objectActionExecutorRegistry.hasObjectActionExecutor(
				objectActionExecutorKey)) {

			if (_log.isDebugEnabled()) {
				_log.debug(
					"No object action executor is registered with " +
						objectActionExecutorKey);
			}
		}
	}

	private void _validateObjectActionTriggerKey(
			String conditionExpression, String objectActionTriggerKey)
		throws PortalException {

		if (Objects.equals(
				objectActionTriggerKey,
				ObjectActionTriggerConstants.KEY_STANDALONE) &&
			Validator.isNotNull(conditionExpression)) {

			throw new ObjectActionTriggerKeyException(
				StringBundler.concat(
					"The object action trigger key ",
					ObjectActionTriggerConstants.KEY_STANDALONE,
					" cannot have a condition expression"));
		}

		if (!ListUtil.exists(
				ObjectActionTriggerUtil.getDefaultObjectActionTriggers(),
				objectActionTrigger -> StringUtil.equals(
					objectActionTrigger.getKey(), objectActionTriggerKey))) {

			if (!_messageBus.hasDestination(objectActionTriggerKey)) {
				throw new ObjectActionTriggerKeyException();
			}

			if (Validator.isNotNull(conditionExpression)) {
				throw new ObjectActionConditionExpressionException();
			}
		}
	}

	private void _validateObjectFieldId(
			ObjectDefinition objectDefinition, long objectFieldId)
		throws PortalException {

		if (objectFieldId <= 0) {
			return;
		}

		ObjectField objectField = _objectFieldLocalService.fetchObjectField(
			objectFieldId);

		if ((objectField == null) ||
			(objectField.getObjectDefinitionId() !=
				objectDefinition.getObjectDefinitionId())) {

			throw new NoSuchObjectFieldException();
		}

		if (Validator.isNotNull(objectField.getRelationshipType())) {
			throw new ObjectFieldRelationshipTypeException(
				"Description and title object fields cannot have a " +
					"relationship type");
		}
	}

	private void _validateParametersUnicodeProperties(
			String conditionExpression, String objectActionExecutorKey,
			String objectActionTriggerKey,
			UnicodeProperties parametersUnicodeProperties)
		throws PortalException {

		Map<String, Object> errorMessageKeys = new HashMap<>();

		if (Validator.isNotNull(conditionExpression)) {
			try {
				_ddmExpressionFactory.createExpression(
					CreateExpressionRequest.Builder.newBuilder(
						conditionExpression
					).build());
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}

				errorMessageKeys.put("conditionExpression", "syntax-error");
			}
		}

		if (Objects.equals(
				objectActionExecutorKey,
				ObjectActionExecutorConstants.KEY_UPDATE_OBJECT_ENTRY) &&
			Objects.equals(
				objectActionTriggerKey,
				ObjectActionTriggerConstants.KEY_ON_AFTER_DELETE)) {

			throw new ObjectActionTriggerKeyException(
				StringBundler.concat(
					"The object action executor key ",
					ObjectActionExecutorConstants.KEY_UPDATE_OBJECT_ENTRY,
					" cannot be associated with the object action trigger key",
					ObjectActionTriggerConstants.KEY_ON_AFTER_DELETE));
		}

		if (Objects.equals(
				objectActionExecutorKey,
				ObjectActionExecutorConstants.KEY_ADD_OBJECT_ENTRY) ||
			Objects.equals(
				objectActionExecutorKey,
				ObjectActionExecutorConstants.KEY_UPDATE_OBJECT_ENTRY)) {

			ObjectDefinition objectDefinition = null;

			String externalReferenceCode = GetterUtil.getString(
				parametersUnicodeProperties.get(
					"objectDefinitionExternalReferenceCode"));

			if (Validator.isNotNull(externalReferenceCode)) {
				objectDefinition = _objectDefinitionPersistence.fetchByERC_C(
					externalReferenceCode, CompanyThreadLocal.getCompanyId());

				if (objectDefinition == null) {
					objectDefinition = _objectDefinitionPersistence.create(
						counterLocalService.increment());

					objectDefinition.setExternalReferenceCode(
						externalReferenceCode);

					User user = _userLocalService.getUser(
						PrincipalThreadLocal.getUserId());

					objectDefinition.setCompanyId(user.getCompanyId());
					objectDefinition.setUserId(user.getUserId());
					objectDefinition.setUserName(user.getFullName());

					objectDefinition.setActive(true);
					objectDefinition.setLabel(externalReferenceCode);
					objectDefinition.setName(externalReferenceCode);
					objectDefinition.setPluralLabel(externalReferenceCode);
					objectDefinition.setStorageType(
						ObjectDefinitionConstants.STORAGE_TYPE_DEFAULT);
					objectDefinition.setSystem(false);
					objectDefinition.setStatus(WorkflowConstants.STATUS_DRAFT);

					objectDefinition = _objectDefinitionPersistence.update(
						objectDefinition);

					_resourceLocalService.addResources(
						objectDefinition.getCompanyId(), 0,
						objectDefinition.getUserId(),
						ObjectDefinition.class.getName(),
						objectDefinition.getObjectDefinitionId(), false, true,
						true);

					_addSystemObjectFields(
						ObjectEntryTable.INSTANCE.getTableName(),
						objectDefinition,
						ObjectEntryTable.INSTANCE.objectEntryId.getName(),
						objectDefinition.isSystem(), user.getUserId());

					objectDefinition = _updateTitleObjectFieldId(
						objectDefinition, null);
				}
			}
			else {
				long objectDefinitionId = GetterUtil.getLong(
					parametersUnicodeProperties.get("objectDefinitionId"));

				objectDefinition =
					_objectDefinitionPersistence.fetchByPrimaryKey(
						objectDefinitionId);
			}

			if ((objectDefinition == null) || !objectDefinition.isActive() ||
				!objectDefinition.isApproved() || objectDefinition.isSystem()) {

				errorMessageKeys.put(
					"objectDefinitionExternalReferenceCode", "invalid");
				errorMessageKeys.put("objectDefinitionId", "invalid");
			}
			else {
				_validatePredefinedValues(
					errorMessageKeys, objectActionExecutorKey,
					objectDefinition.getObjectDefinitionId(),
					_jsonFactory.createJSONArray(
						parametersUnicodeProperties.get("predefinedValues")));
			}
		}
		else if (Objects.equals(
					objectActionExecutorKey,
					ObjectActionExecutorConstants.KEY_GROOVY)) {

			String script = parametersUnicodeProperties.get("script");

			if (Validator.isNotNull(script)) {
				try {
					_objectScriptingValidator.validate("groovy", script);
				}
				catch (ObjectScriptingException objectScriptingException) {
					errorMessageKeys.put(
						"script", objectScriptingException.getMessageKey());
				}
			}
		}
		else if (Objects.equals(
					objectActionExecutorKey,
					ObjectActionExecutorConstants.KEY_WEBHOOK)) {

			if (Validator.isNull(parametersUnicodeProperties.get("url"))) {
				errorMessageKeys.put("url", "required");
			}
		}

		if (MapUtil.isNotEmpty(errorMessageKeys)) {
			throw new ObjectActionParametersException(errorMessageKeys);
		}
	}

	private void _validatePredefinedValues(
		Map<String, Object> errorMessageKeys, String objectActionExecutorKey,
		long objectDefinitionId, JSONArray predefinedValuesJSONArray) {

		Map<String, String> predefinedValuesErrorMessageKeys = new HashMap<>();

		Map<String, String> predefinedValuesMap = new HashMap<>();

		for (int i = 0; i < predefinedValuesJSONArray.length(); i++) {
			JSONObject predefinedValueJSONObject =
				predefinedValuesJSONArray.getJSONObject(i);

			String name = predefinedValueJSONObject.getString("name");

			ObjectField objectField = _objectFieldLocalService.fetchObjectField(
				objectDefinitionId, name);

			if (objectField == null) {
				predefinedValuesErrorMessageKeys.put(name, "invalid");

				continue;
			}

			String value = predefinedValueJSONObject.getString("value");

			predefinedValuesMap.put(name, value);

			if (Objects.equals(
					objectActionExecutorKey,
					ObjectActionExecutorConstants.KEY_UPDATE_OBJECT_ENTRY) &&
				objectField.isRequired() && Validator.isNull(value)) {

				predefinedValuesErrorMessageKeys.put(
					objectField.getName(), "required");
			}

			if (Validator.isNull(value) ||
				predefinedValueJSONObject.getBoolean("inputAsValue")) {

				continue;
			}

			try {
				_ddmExpressionFactory.createExpression(
					CreateExpressionRequest.Builder.newBuilder(
						value
					).build());
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception);
				}

				predefinedValuesErrorMessageKeys.put(name, "syntax-error");
			}
		}

		if (Objects.equals(
				objectActionExecutorKey,
				ObjectActionExecutorConstants.KEY_UPDATE_OBJECT_ENTRY)) {

			return;
		}

		for (ObjectField objectField :
				_objectFieldLocalService.getObjectFields(objectDefinitionId)) {

			if (!objectField.isRequired() ||
				Validator.isNotNull(
					predefinedValuesMap.get(objectField.getName())) ||
				StringUtil.equals(
					objectField.getBusinessType(),
					ObjectFieldConstants.BUSINESS_TYPE_RELATIONSHIP)) {

				continue;
			}

			predefinedValuesErrorMessageKeys.put(
				objectField.getName(), "required");
		}

		if (MapUtil.isNotEmpty(predefinedValuesErrorMessageKeys)) {
			errorMessageKeys.put(
				"predefinedValues", predefinedValuesErrorMessageKeys);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectActionLocalServiceImpl.class);

	@Reference
	private DDMExpressionFactory _ddmExpressionFactory;

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private Language _language;

	@Reference
	private MessageBus _messageBus;

	@Reference
	private ObjectActionExecutorRegistry _objectActionExecutorRegistry;

	@Reference
	private ObjectDefinitionPersistence _objectDefinitionPersistence;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private ObjectFieldPersistence _objectFieldPersistence;

	@Reference
	private ObjectScriptingValidator _objectScriptingValidator;

	@Reference
	private ResourceLocalService _resourceLocalService;

	@Reference
	private UserLocalService _userLocalService;

}