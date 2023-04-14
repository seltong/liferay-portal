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

package com.liferay.object.internal.model.listener;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.asset.kernel.service.AssetLinkLocalService;
import com.liferay.object.constants.ObjectFieldConstants;
import com.liferay.object.constants.ObjectRelationshipConstants;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryLocalService;
import com.liferay.object.service.persistence.ObjectEntryPersistence;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.dao.orm.common.SQLTransformer;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouter;
import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.dao.jdbc.AutoBatchPreparedStatementUtil;
import com.liferay.portal.kernel.dao.jdbc.CurrentConnection;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBus;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.model.WorkflowInstanceLink;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.service.ClassNameLocalService;
import com.liferay.portal.kernel.service.WorkflowInstanceLinkLocalService;
import com.liferay.portal.kernel.transaction.TransactionCommitCallbackUtil;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.audit.event.generators.constants.EventTypes;
import com.liferay.portal.security.audit.event.generators.util.Attribute;
import com.liferay.portal.security.audit.event.generators.util.AttributesBuilder;
import com.liferay.portal.security.audit.event.generators.util.AuditMessageBuilder;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalService;
import com.liferay.portal.workflow.metrics.search.index.InstanceWorkflowMetricsIndexer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import com.liferay.portal.workflow.metrics.search.index.reindexer.WorkflowMetricsReindexer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Marcela Cunha
 */
@Component(service = ModelListener.class)
public class ObjectDefinitionModelListener
	extends BaseModelListener<ObjectDefinition> {

	@Override
	public void onAfterUpdate(
			ObjectDefinition originalObjectDefinition,
			ObjectDefinition objectDefinition)
		throws ModelListenerException {

		if (originalObjectDefinition.getTitleObjectFieldId() ==
				objectDefinition.getTitleObjectFieldId()) {

			return;
		}

		List<AssetEntry> assetEntries = new ArrayList<>();
		List<KaleoInstance> kaleoInstances = new ArrayList<>();

		TransactionCommitCallbackUtil.registerCallback(
			() -> {
				for (ObjectEntry objectEntry :
					_objectEntryLocalService.getObjectEntries(
						objectDefinition.getObjectDefinitionId())) {

					try {
						AssetEntry assetEntry =
							_assetEntryLocalService.getEntry(
								objectDefinition.getClassName(),
								objectEntry.getObjectEntryId());

						List<AssetLink> assetLinks =
							_assetLinkLocalService.getDirectLinks(
								assetEntry.getEntryId());

						long[] assetLinkEntryIds = new long[assetLinks.size()];

						for (int i = 0; i < assetLinks.size(); i++) {
							AssetLink assetLink = assetLinks.get(i);

							assetLinkEntryIds[i] = assetLink.getLinkId();
						}

						_objectEntryLocalService.updateAsset(
							assetEntry.getUserId(), objectEntry,
							assetEntry.getCategoryIds(),
							assetEntry.getTagNames(), assetLinkEntryIds,
							assetEntry.getPriority());

						WorkflowInstanceLink workflowInstanceLink =
							_workflowInstanceLinkLocalService.
								fetchWorkflowInstanceLink(
									objectEntry.getCompanyId(),
									objectEntry.getNonzeroGroupId(),
									objectDefinition.getClassName(),
									objectEntry.getObjectEntryId());

						assetEntries.add(_assetEntryLocalService.getEntry(
							objectDefinition.getClassName(),
							objectEntry.getObjectEntryId()));

						kaleoInstances.add(
							_kaleoInstanceLocalService.getKaleoInstance(
								workflowInstanceLink.getWorkflowInstanceId()));
					}
					catch (Exception exception) {
						throw new ModelListenerException(exception);
					}

					return null;
				}

				Message message = new Message();

				message.put("assetEntries", assetEntries);
				message.put("className", objectDefinition.getClassName());
				message.put("kaleoInstances", kaleoInstances);

				_messageBus.sendMessage("liferay/kaleo_definition", message);

				return null;
			});
	}

	@Reference
	private CurrentConnection _currentConnection;

	@Override
	public void onBeforeCreate(ObjectDefinition objectDefinition)
		throws ModelListenerException {

		_route(EventTypes.ADD, objectDefinition);
	}

	@Override
	public void onBeforeRemove(ObjectDefinition objectDefinition)
		throws ModelListenerException {

		_route(EventTypes.DELETE, objectDefinition);
	}

	@Override
	public void onBeforeUpdate(
			ObjectDefinition originalObjectDefinition,
			ObjectDefinition objectDefinition)
		throws ModelListenerException {

		try {
			_auditRouter.route(
				AuditMessageBuilder.buildAuditMessage(
					EventTypes.UPDATE, ObjectDefinition.class.getName(),
					objectDefinition.getObjectDefinitionId(),
					_getModifiedAttributes(
						originalObjectDefinition, objectDefinition)));
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	private Map<Locale, String> _createAssetTypeLocalizationMap(
		String className, long groupId) {

		Map<Locale, String> localizationMap = new HashMap<>();

		for (Locale availableLocale : _language.getAvailableLocales(groupId)) {
			localizationMap.put(
				availableLocale,
				ResourceActionsUtil.getModelResource(
					availableLocale, className));
		}

		return localizationMap;
	}

	private List<Attribute> _getModifiedAttributes(
		ObjectDefinition originalObjectDefinition,
		ObjectDefinition objectDefinition) {

		AttributesBuilder attributesBuilder = new AttributesBuilder(
			objectDefinition, originalObjectDefinition);

		attributesBuilder.add("active");
		attributesBuilder.add("descriptionObjectFieldId");
		attributesBuilder.add("labelMap");
		attributesBuilder.add("name");
		attributesBuilder.add("panelAppOrder");
		attributesBuilder.add("panelCategoryKey");
		attributesBuilder.add("pluralLabelMap");
		attributesBuilder.add("portlet");
		attributesBuilder.add("scope");
		attributesBuilder.add("titleObjectFieldId");

		return attributesBuilder.getAttributes();
	}

	private void _route(String eventType, ObjectDefinition objectDefinition)
		throws ModelListenerException {

		try {
			AuditMessage auditMessage = AuditMessageBuilder.buildAuditMessage(
				eventType, ObjectDefinition.class.getName(),
				objectDefinition.getObjectDefinitionId(), null);

			JSONObject additionalInfoJSONObject =
				auditMessage.getAdditionalInfo();

			additionalInfoJSONObject.put(
				"active", objectDefinition.isActive()
			).put(
				"labelMap", objectDefinition.getLabelMap()
			).put(
				"name", objectDefinition.getName()
			).put(
				"scope", objectDefinition.getScope()
			);

			_auditRouter.route(auditMessage);
		}
		catch (Exception exception) {
			throw new ModelListenerException(exception);
		}
	}

	@Reference
	private ObjectEntryPersistence _objectEntryPersistence;

	@Reference
	private ClassNameLocalService _classNameLocalService;

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private AssetLinkLocalService _assetLinkLocalService;

	@Reference
	private AuditRouter _auditRouter;

	@Reference
	private InstanceWorkflowMetricsIndexer _instanceWorkflowMetricsIndexer;

	@Reference
	private WorkflowMetricsReindexer _instanceWorkflowMetricsReindexer;

	@Reference
	private KaleoInstanceLocalService _kaleoInstanceLocalService;

	@Reference
	private Language _language;

	@Reference
	private Localization _localization;

	@Reference
	private MessageBus _messageBus;

	@Reference
	private ObjectEntryLocalService _objectEntryLocalService;

	@Reference
	private WorkflowInstanceLinkLocalService _workflowInstanceLinkLocalService;

}