/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.runtime.integration.internal.security.permission.resource;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.auth.PrincipalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionVersionLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Selton Guedes
 */
@Component(
	property = "model.class.name=com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion",
	service = ModelResourcePermission.class
)
public class KaleoDefinitionVersionModelResourcePermission
	implements ModelResourcePermission<KaleoDefinitionVersion> {

	@Override
	public void check(
			PermissionChecker permissionChecker,
			KaleoDefinitionVersion kaleoDefinitionVersion, String actionId)
		throws PortalException {

		if (!contains(permissionChecker, kaleoDefinitionVersion, actionId)) {
			throw new PrincipalException.MustBeCompanyAdmin(
				permissionChecker.getUserId());
		}
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, long primaryKey,
			String actionId)
		throws PortalException {

		check(
			permissionChecker,
			_kaleoDefinitionVersionLocalService.getKaleoDefinitionVersion(
				primaryKey),
			actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker,
			KaleoDefinitionVersion kaleoDefinitionVersion, String actionId)
		throws PortalException {

		return contains(
			permissionChecker,
			kaleoDefinitionVersion.getKaleoDefinitionVersionId(), actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long primaryKey,
			String actionId)
		throws PortalException {

		return contains(
			permissionChecker,
			_kaleoDefinitionVersionLocalService.getKaleoDefinitionVersion(
				primaryKey),
			actionId);
	}

	@Override
	public String getModelName() {
		return KaleoDefinitionVersion.class.getName();
	}

	@Override
	public PortletResourcePermission getPortletResourcePermission() {
		return _portletResourcePermission;
	}

	private KaleoDefinitionVersionLocalService
		_kaleoDefinitionVersionLocalService;

	@Reference(
		target = "(resource.name=" + WorkflowConstants.RESOURCE_NAME + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

}