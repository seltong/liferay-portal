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

package com.liferay.portal.workflow.kaleo.internal.model.listener;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.info.field.InfoField;
import com.liferay.info.field.type.BooleanInfoFieldType;
import com.liferay.info.field.type.DateInfoFieldType;
import com.liferay.info.field.type.ImageInfoFieldType;
import com.liferay.info.field.type.InfoFieldType;
import com.liferay.info.field.type.NumberInfoFieldType;
import com.liferay.info.field.type.TextInfoFieldType;
import com.liferay.info.localized.InfoLocalizedValue;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.model.ObjectField;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTask;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.portal.workflow.kaleo.service.persistence.KaleoInstanceUtil;
import com.liferay.portal.workflow.kaleo.service.persistence.KaleoTaskInstanceTokenUtil;
import com.liferay.portal.workflow.kaleo.service.persistence.KaleoTaskUtil;
import com.liferay.portal.workflow.metrics.model.Assignment;
import com.liferay.portal.workflow.metrics.model.RoleAssignment;
import com.liferay.portal.workflow.metrics.model.UpdateTaskRequest;
import com.liferay.portal.workflow.metrics.model.UserAssignment;
import com.liferay.portal.workflow.metrics.search.index.InstanceWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.search.index.TaskWorkflowMetricsIndexer;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Selton Guedes
 */
@Component(immediate = true, service = ModelListener.class)
public class ObjectDefinitionModelListener
	extends BaseModelListener<ObjectDefinition> {

	@Override
	public void onAfterUpdate(
			ObjectDefinition originalModel, ObjectDefinition model)
		throws ModelListenerException {

		if (originalModel.getTitleObjectFieldId() !=
				model.getTitleObjectFieldId()) {

			try {
				ObjectDefinition objectDefinition =
					_objectDefinitionLocalService.fetchObjectDefinition(
						model.getObjectDefinitionId());

				Company company = _companyLocalService.getCompany(
					model.getCompanyId());

				long groupId = company.getGroupId();

				int count = _objectEntryLocalService.getObjectEntriesCount(
					0L, model.getObjectDefinitionId());

				List<ObjectEntry> objectEntries =
					_objectEntryLocalService.getObjectEntries(
						0L, objectDefinition.getObjectDefinitionId(), 1, count);

				for (ObjectEntry objectEntry : objectEntries) {
					KaleoInstance kaleoInstance =
						KaleoInstanceUtil.fetchByCN_CPK_First(
							objectDefinition.getClassName(),
							objectEntry.getObjectEntryId(), null);

					Map<Locale, String> titleLocalizationMap =
						_createAssetTitleLocalizationMap(
							objectEntry.getModelClassName(),
							objectEntry.getObjectEntryId(), groupId);

					if (objectDefinition.getTitleObjectFieldId() != 0) {
						ObjectField objectField =
							_objectFieldLocalService.getObjectField(
								model.getTitleObjectFieldId());

						Map<String, Serializable> values =
							objectEntry.getValues();

						titleLocalizationMap = Stream.of(
							titleLocalizationMap.entrySet()
						).flatMap(
							Set::stream
						).peek(
							item -> item.setValue(
								String.valueOf(
									values.get(
										InfoField.builder(
										).infoFieldType(
											_getInfoFieldType(objectField)
										).name(
											objectField.getName()
										).labelInfoLocalizedValue(
											InfoLocalizedValue.<String>builder(
											).values(
												objectField.getLabelMap()
											).build()
										).build(
										).getName())))
						).collect(
							Collectors.toMap(
								Map.Entry::getKey, Map.Entry::getValue)
						);
					}
					else {
						titleLocalizationMap = Stream.of(
							titleLocalizationMap.entrySet()
						).flatMap(
							Set::stream
						).peek(
							item -> item.setValue(
								String.valueOf(objectEntry.getObjectEntryId()))
						).collect(
							Collectors.toMap(
								Map.Entry::getKey, Map.Entry::getValue)
						);
					}

					_instanceWorkflowMetricsIndexer.updateInstance(
						objectDefinition.isActive(), titleLocalizationMap,
						objectDefinition.getLabelMap(),
						objectDefinition.getCompanyId(),
						kaleoInstance.getKaleoInstanceId(),
						objectDefinition.getModifiedDate());

					KaleoTask kaleoTask =
						KaleoTaskUtil.fetchByKaleoDefinitionVersionId_First(
							kaleoInstance.getKaleoDefinitionVersionId(), null);

					UpdateTaskRequest.Builder builder =
						new UpdateTaskRequest.Builder();

					KaleoTaskInstanceToken kaleoTaskInstanceToken =
						KaleoTaskInstanceTokenUtil.fetchByKaleoInstanceId_First(
							kaleoInstance.getKaleoInstanceId(), null);

					List<KaleoTaskAssignmentInstance>
						kaleoTaskAssignmentInstances =
							_kaleoTaskAssignmentInstanceLocalService.
								getKaleoTaskAssignmentInstances(
									kaleoTaskInstanceToken.
										getKaleoTaskInstanceTokenId());

					KaleoTaskAssignmentInstance
						firstKaleoTaskAssignmentInstance =
							kaleoTaskAssignmentInstances.get(0);

					List<Assignment> assignments = new ArrayList<>();

					if (Objects.equals(
							firstKaleoTaskAssignmentInstance.
								getAssigneeClassName(),
							User.class.getName())) {

						User user = _userLocalService.fetchUser(
							firstKaleoTaskAssignmentInstance.
								getAssigneeClassPK());

						assignments.add(
							new UserAssignment(
								firstKaleoTaskAssignmentInstance.
									getAssigneeClassPK(),
								user.getFullName()));
					}
					else {
						Stream.of(
							kaleoTaskAssignmentInstances
						).flatMap(
							List::stream
						).collect(
							Collectors.groupingBy(
								KaleoTaskAssignmentInstance::getAssigneeClassPK,
								Collectors.mapping(
									KaleoTaskAssignmentInstance::getGroupId,
									Collectors.toList()))
						).forEach(
							(assignmentId, assignmentGroupIds) ->
								assignments.add(
									new RoleAssignment(
										assignmentId, assignmentGroupIds))
						);
					}

					_taskWorkflowMetricsIndexer.updateTask(
						builder.assetTitleMap(
							titleLocalizationMap
						).taskId(
							kaleoTask.getKaleoTaskId()
						).assetTypeMap(
							objectDefinition.getLabelMap()
						).assignments(
							assignments
						).companyId(
							kaleoTask.getCompanyId()
						).modifiedDate(
							kaleoTask.getModifiedDate()
						).userId(
							kaleoTask.getUserId()
						).build());
				}
			}
			catch (PortalException portalException) {
				_log.error(portalException);
			}
		}
	}

	private Map<Locale, String> _createAssetTitleLocalizationMap(
		String className, long classPK, long groupId) {

		AssetRenderer<?> assetRenderer = _getAssetRenderer(className, classPK);

		if (assetRenderer != null) {
			AssetEntry assetEntry = _assetEntryLocalService.fetchEntry(
				assetRenderer.getClassName(), assetRenderer.getClassPK());

			if (assetEntry != null) {
				return LocalizationUtil.populateLocalizationMap(
					assetEntry.getTitleMap(), assetEntry.getDefaultLanguageId(),
					assetEntry.getGroupId());
			}
		}

		WorkflowHandler<?> workflowHandler =
			WorkflowHandlerRegistryUtil.getWorkflowHandler(className);

		if (workflowHandler != null) {
			Map<Locale, String> localizationMap = new HashMap<>();

			for (Locale availableLocale :
					LanguageUtil.getAvailableLocales(groupId)) {

				localizationMap.put(
					availableLocale,
					workflowHandler.getTitle(classPK, availableLocale));
			}

			return localizationMap;
		}

		return Collections.emptyMap();
	}

	private AssetRenderer<?> _getAssetRenderer(String className, long classPK) {
		AssetRendererFactory<?> assetRendererFactory =
			AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClassName(
				className);

		if (assetRendererFactory != null) {
			try {
				return assetRendererFactory.getAssetRenderer(classPK);
			}
			catch (PortalException portalException) {
				if (_log.isDebugEnabled()) {
					_log.debug(portalException);
				}
			}
		}

		return null;
	}

	private InfoFieldType _getInfoFieldType(ObjectField objectField) {
		if (Validator.isNotNull(objectField.getRelationshipType())) {
			return TextInfoFieldType.INSTANCE;
		}
		else if (Objects.equals(objectField.getDBType(), "Boolean")) {
			return BooleanInfoFieldType.INSTANCE;
		}
		else if (Objects.equals(objectField.getDBType(), "BigDecimal") ||
				 Objects.equals(objectField.getDBType(), "Double") ||
				 Objects.equals(objectField.getDBType(), "Integer") ||
				 Objects.equals(objectField.getDBType(), "Long")) {

			return NumberInfoFieldType.INSTANCE;
		}
		else if (Objects.equals(objectField.getDBType(), "Blob")) {
			return ImageInfoFieldType.INSTANCE;
		}
		else if (Objects.equals(objectField.getDBType(), "Date")) {
			return DateInfoFieldType.INSTANCE;
		}
		else if (Objects.equals(objectField.getDBType(), "String")) {
			return TextInfoFieldType.INSTANCE;
		}

		return TextInfoFieldType.INSTANCE;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectDefinitionModelListener.class);

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private InstanceWorkflowMetricsIndexer _instanceWorkflowMetricsIndexer;

	@Reference
	private KaleoInstanceLocalService _kaleoInstanceLocalService;

	@Reference
	private KaleoTaskAssignmentInstanceLocalService
		_kaleoTaskAssignmentInstanceLocalService;

	@Reference
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private ObjectFieldLocalService _objectFieldLocalService;

	@Reference
	private TaskWorkflowMetricsIndexer _taskWorkflowMetricsIndexer;

	@Reference
	private UserLocalService _userLocalService;

}