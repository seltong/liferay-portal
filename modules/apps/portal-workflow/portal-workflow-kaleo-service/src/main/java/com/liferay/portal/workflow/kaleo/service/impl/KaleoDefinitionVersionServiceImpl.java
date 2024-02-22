/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.service.impl;

import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionVersionLocalService;
import com.liferay.portal.workflow.kaleo.service.base.KaleoDefinitionVersionServiceBaseImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Brian Wing Shun Chan
 */
@Component(
	property = {
		"json.web.service.context.name=kaleo",
		"json.web.service.context.path=KaleoDefinitionVersion"
	},
	service = AopService.class
)
public class KaleoDefinitionVersionServiceImpl
	extends KaleoDefinitionVersionServiceBaseImpl {

	public KaleoDefinitionVersion getKaleoDefinitionVersion(
			long companyId, String name, String version)
		throws PortalException {

		KaleoDefinitionVersion kaleoDefinitionVersion =
			_kaleoDefinitionVersionLocalService.getKaleoDefinitionVersion(
				companyId, name, version);

		_kaleoDefinitionVersionModelResourcePermission.check(
			getPermissionChecker(),
			kaleoDefinitionVersion.getKaleoDefinitionVersionId(),
			ActionKeys.VIEW);

		return kaleoDefinitionVersion;
	}

	private KaleoDefinitionVersionLocalService
		_kaleoDefinitionVersionLocalService;

	@Reference(
		target = "(model.class.name=com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion)"
	)
	private ModelResourcePermission<KaleoDefinitionVersion>
		_kaleoDefinitionVersionModelResourcePermission;

}