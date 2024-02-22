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
import com.liferay.portal.kernel.workflow.WorkflowDefinition;
import com.liferay.portal.workflow.kaleo.KaleoWorkflowModelConverter;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionLocalService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

		check(permissionChecker, _getWorkflowDefinition(primaryKey), actionId);
	}

	@Override
	public void check(
			PermissionChecker permissionChecker,
			WorkflowDefinition workflowDefinition, String actionId)
		throws PortalException {

		if (!contains(permissionChecker, workflowDefinition, actionId)) {
			throw new PrincipalException.MustHavePermission(
				permissionChecker, getModelName(),
				workflowDefinition.getWorkflowDefinitionId(), actionId);
		}
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker, long primaryKey,
			String actionId)
		throws PortalException {

		return contains(
			permissionChecker, _getWorkflowDefinition(primaryKey), actionId);
	}

	@Override
	public boolean contains(
			PermissionChecker permissionChecker,
			WorkflowDefinition workflowDefinition, String actionId)
		throws PortalException {

		if (permissionChecker.hasOwnerPermission(
				permissionChecker.getCompanyId(),
				WorkflowDefinition.class.getName(),
				workflowDefinition.getCompanyId(),
				workflowDefinition.getUserId(), actionId) ||
			permissionChecker.hasPermission(
				null, getModelName(),
				workflowDefinition.getWorkflowDefinitionId(), actionId)) {

			return true;
		}

		return false;
	}

	@Override
	public String getModelName() {
		return WorkflowDefinition.class.getName();
	}

	@Override
	public PortletResourcePermission getPortletResourcePermission() {
		return _portletResourcePermission;
	}

	private WorkflowDefinition _getWorkflowDefinition(long primaryKey)
		throws PortalException {

		return _kaleoWorkflowModelConverter.toWorkflowDefinition(
			_kaleoDefinitionLocalService.getKaleoDefinition(primaryKey));
	}

	@Reference
	private KaleoDefinitionLocalService _kaleoDefinitionLocalService;

	@Reference
	private KaleoWorkflowModelConverter _kaleoWorkflowModelConverter;

	@Reference(
		target = "(resource.name=" + WorkflowConstants.RESOURCE_NAME + ")"
	)
	private PortletResourcePermission _portletResourcePermission;

}