/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.runtime.integration.internal.security.permission.resource;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.security.permission.resource.PortletResourcePermission;
import com.liferay.portal.kernel.workflow.WorkflowDefinition;

import org.osgi.service.component.annotations.Component;

/**
 * @author Selton Guedes
 */
@Component(
	property = "model.class.name=com.liferay.portal.kernel.workflow.WorkflowDefinition",
	service = ModelResourcePermission.class
)
public class WorkflowDefinitionModelResourcePermission
	implements ModelResourcePermission<WorkflowDefinition> {

	@Override
	public void check(
			PermissionChecker permissionChecker, long primaryKey,
			String actionId)
		throws PortalException {
	}

	@Override
	public void check(
			PermissionChecker permissionChecker, WorkflowDefinition model,
			String actionId)
		throws PortalException {
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long primaryKey,
			String actionId)
		throws PortalException {

		return false;
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, WorkflowDefinition model,
			String actionId)
		throws PortalException {

		return false;
	}

	@Override
	public String getModelName() {
		return null;
	}

	@Override
	public PortletResourcePermission getPortletResourcePermission() {
		return null;
	}

}