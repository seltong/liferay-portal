/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.workflow.kaleo.metrics.integration.internal.model.listener;

import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectEntry;
import com.liferay.object.service.ObjectEntryService;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.workflow.kaleo.metrics.integration.internal.helper.IndexerHelper;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.service.persistence.KaleoInstanceUtil;
import com.liferay.portal.workflow.metrics.search.index.InstanceWorkflowMetricsIndexer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Pedro Leite
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

		for (ObjectEntry objectEntry :
				_objectEntryService.getObjectEntries(
					objectDefinition.getObjectDefinitionId())) {

			KaleoInstance kaleoInstance = KaleoInstanceUtil.fetchByCN_CPK_First(
				objectDefinition.getClassName(), objectEntry.getObjectEntryId(),
				null);

			if (kaleoInstance == null) {
				continue;
			}

			_instanceWorkflowMetricsIndexer.updateInstance(
				kaleoInstance.isActive(),
				_indexerHelper.createAssetTitleLocalizationMap(
					kaleoInstance.getClassName(), kaleoInstance.getClassPK(),
					kaleoInstance.getGroupId()),
				_indexerHelper.createAssetTypeLocalizationMap(
					kaleoInstance.getClassName(), kaleoInstance.getGroupId()),
				kaleoInstance.getCompanyId(),
				kaleoInstance.getKaleoInstanceId(),
				kaleoInstance.getModifiedDate());
		}
	}

	@Reference
	private IndexerHelper _indexerHelper;

	@Reference
	private InstanceWorkflowMetricsIndexer _instanceWorkflowMetricsIndexer;

	@Reference
	private ObjectEntryService _objectEntryService;

}