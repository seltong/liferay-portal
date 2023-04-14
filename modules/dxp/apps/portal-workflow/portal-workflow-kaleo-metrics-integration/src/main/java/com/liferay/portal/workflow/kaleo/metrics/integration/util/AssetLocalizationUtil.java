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

package com.liferay.portal.workflow.kaleo.metrics.integration.util;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Selton Guedes
 */
public class AssetLocalizationUtil {

	public static Map<Locale, String> createAssetTitleLocalizationMap(
		AssetEntryLocalService assetEntryLocalService, String className,
		long classPK, long groupId, Language language,
		Localization localization) {

		AssetRenderer<?> assetRenderer = _getAssetRenderer(className, classPK);

		if (assetRenderer != null) {
			AssetEntry assetEntry = assetEntryLocalService.fetchEntry(
				assetRenderer.getClassName(), assetRenderer.getClassPK());

			if (assetEntry != null) {
				return localization.populateLocalizationMap(
					assetEntry.getTitleMap(), assetEntry.getDefaultLanguageId(),
					assetEntry.getGroupId());
			}
		}

		WorkflowHandler<?> workflowHandler =
			WorkflowHandlerRegistryUtil.getWorkflowHandler(className);

		if (workflowHandler != null) {
			Map<Locale, String> localizationMap = new HashMap<>();

			for (Locale availableLocale :
					language.getAvailableLocales(groupId)) {

				localizationMap.put(
					availableLocale,
					workflowHandler.getTitle(classPK, availableLocale));
			}

			return localizationMap;
		}

		return Collections.emptyMap();
	}

	public static Map<Locale, String> createAssetTypeLocalizationMap(
		String className, long groupId, Language language) {

		Map<Locale, String> localizationMap = new HashMap<>();

		for (Locale availableLocale : language.getAvailableLocales(groupId)) {
			localizationMap.put(
				availableLocale,
				ResourceActionsUtil.getModelResource(
					availableLocale, className));
		}

		return localizationMap;
	}

	private static AssetRenderer<?> _getAssetRenderer(
		String className, long classPK) {

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

	private static final Log _log = LogFactoryUtil.getLog(
		AssetLocalizationUtil.class);

}