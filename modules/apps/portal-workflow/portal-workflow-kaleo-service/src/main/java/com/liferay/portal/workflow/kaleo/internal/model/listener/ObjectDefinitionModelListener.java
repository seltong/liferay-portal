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

import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.persistence.ObjectEntryUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PortalRunMode;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.BulkDocumentRequest;
import com.liferay.portal.search.engine.adapter.document.IndexDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
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
import com.liferay.portal.workflow.metrics.internal.search.index.WorkflowMetricsIndex;
import com.liferay.portal.workflow.metrics.model.Assignment;
import com.liferay.portal.workflow.metrics.model.RoleAssignment;
import com.liferay.portal.workflow.metrics.model.UpdateTaskRequest;
import com.liferay.portal.workflow.metrics.model.UserAssignment;
import com.liferay.portal.workflow.metrics.search.index.InstanceWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.search.index.TaskWorkflowMetricsIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

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

				BulkDocumentRequest bulkDocumentRequest = new BulkDocumentRequest();

				Stream.of(
					ObjectEntryUtil.findByObjectDefinitionId(
						objectDefinition.getObjectDefinitionId())
				).flatMap(
					List::stream
				).map(
					objectEntry -> _instanceWorkflowMetricsIndexer.updateInstance(
						objectDefinition.isActive(),
						_createAssetTitleLocalizationMap(
							objectEntry.getModelClassName(),
							objectEntry.getObjectEntryId()),
						objectDefinition.getLabelMap(),
						objectDefinition.getCompanyId(),
						KaleoInstanceUtil.fetchByCN_CPK_First(
							objectDefinition.getClassName(),
							objectEntry.getObjectEntryId(),
							null).getKaleoInstanceId(),
						objectDefinition.getModifiedDate())
				).map(
					document -> new IndexDocumentRequest(
						_instanceWorkflowMetricsIndex.getIndexName(
							objectDefinition.getCompanyId()), document
					) {

						{
							setType(
								_instanceWorkflowMetricsIndex.
									getIndexType());
						}
					}
				).forEach(
					bulkDocumentRequest::addBulkableDocumentRequest
				);

				if (ListUtil.isNotEmpty(
					bulkDocumentRequest.getBulkableDocumentRequests())) {

					if (PortalRunMode.isTestMode()) {
						bulkDocumentRequest.setRefresh(true);
					}

					searchEngineAdapter.execute(bulkDocumentRequest);
				}

//				for (ObjectEntry objectEntry :
//						ObjectEntryUtil.findByObjectDefinitionId(
//							objectDefinition.getObjectDefinitionId())) {
//
//					Map<Locale, String> titleLocalizationMap =
//						_createAssetTitleLocalizationMap(
//							objectEntry.getModelClassName(),
//							objectEntry.getObjectEntryId());
//
//					KaleoInstance kaleoInstance =
//						KaleoInstanceUtil.fetchByCN_CPK_First(
//							objectDefinition.getClassName(),
//							objectEntry.getObjectEntryId(), null);
//
//					_instanceWorkflowMetricsIndexer.updateInstance(
//						objectDefinition.isActive(), titleLocalizationMap,
//						objectDefinition.getLabelMap(),
//						objectDefinition.getCompanyId(),
//						kaleoInstance.getKaleoInstanceId(),
//						objectDefinition.getModifiedDate());
//
//					KaleoTaskInstanceToken kaleoTaskInstanceToken =
//						KaleoTaskInstanceTokenUtil.fetchByKaleoInstanceId_First(
//							kaleoInstance.getKaleoInstanceId(), null);
//
//					List<KaleoTaskAssignmentInstance>
//						kaleoTaskAssignmentInstances =
//							_kaleoTaskAssignmentInstanceLocalService.
//								getKaleoTaskAssignmentInstances(
//									kaleoTaskInstanceToken.
//										getKaleoTaskInstanceTokenId());
//
//					KaleoTaskAssignmentInstance
//						firstKaleoTaskAssignmentInstance =
//							kaleoTaskAssignmentInstances.get(0);
//
//					List<Assignment> assignments = new ArrayList<>();
//
//					if (Objects.equals(
//							firstKaleoTaskAssignmentInstance.
//								getAssigneeClassName(),
//							User.class.getName())) {
//
//						User user = _userLocalService.fetchUser(
//							firstKaleoTaskAssignmentInstance.
//								getAssigneeClassPK());
//
//						assignments.add(
//							new UserAssignment(
//								firstKaleoTaskAssignmentInstance.
//									getAssigneeClassPK(),
//								user.getFullName()));
//					}
//					else {
//						Stream.of(
//							kaleoTaskAssignmentInstances
//						).flatMap(
//							List::stream
//						).collect(
//							Collectors.groupingBy(
//								KaleoTaskAssignmentInstance::getAssigneeClassPK,
//								Collectors.mapping(
//									KaleoTaskAssignmentInstance::getGroupId,
//									Collectors.toList()))
//						).forEach(
//							(assignmentId, assignmentGroupIds) ->
//								assignments.add(
//									new RoleAssignment(
//										assignmentId, assignmentGroupIds))
//						);
//					}
//
//					KaleoTask kaleoTask =
//						KaleoTaskUtil.fetchByKaleoDefinitionVersionId_First(
//							kaleoInstance.getKaleoDefinitionVersionId(), null);
//
//					UpdateTaskRequest.Builder builder =
//						new UpdateTaskRequest.Builder();
//
//					_taskWorkflowMetricsIndexer.updateTask(
//						builder.assetTitleMap(
//							titleLocalizationMap
//						).taskId(
//							kaleoTask.getKaleoTaskId()
//						).assetTypeMap(
//							objectDefinition.getLabelMap()
//						).assignments(
//							assignments
//						).companyId(
//							kaleoTask.getCompanyId()
//						).modifiedDate(
//							kaleoTask.getModifiedDate()
//						).userId(
//							kaleoTask.getUserId()
//						).build());
//				}
			}
			catch (Exception portalException) {
				_log.error(portalException);
			}
		}
	}

	private Map<Locale, String> _createAssetTitleLocalizationMap(
		String className, long classPK) {

		WorkflowHandler<?> workflowHandler =
			WorkflowHandlerRegistryUtil.getWorkflowHandler(className);

		if (workflowHandler != null) {
			Map<Locale, String> localizationMap = new HashMap<>();

			for (Locale availableLocale :
					LanguageUtil.getAvailableLocales(0L)) {

				localizationMap.put(
					availableLocale,
					workflowHandler.getTitle(classPK, availableLocale));
			}

			return localizationMap;
		}

		return Collections.emptyMap();
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ObjectDefinitionModelListener.class);

	@Reference(
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.DYNAMIC,
		policyOption = ReferencePolicyOption.GREEDY,
		target = "(search.engine.impl=Elasticsearch)"
	)
	protected volatile SearchEngineAdapter searchEngineAdapter;

	@Reference(target = "(workflow.metrics.index.entity.name=instance)")
	private WorkflowMetricsIndex _instanceWorkflowMetricsIndex;

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