/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.portal.workflow.kaleo.service;

import com.liferay.portal.kernel.service.ServiceWrapper;

/**
 * Provides a wrapper for {@link KaleoDefinitionVersionService}.
 *
 * @author Brian Wing Shun Chan
 * @see KaleoDefinitionVersionService
 * @generated
 */
public class KaleoDefinitionVersionServiceWrapper
	implements KaleoDefinitionVersionService,
			   ServiceWrapper<KaleoDefinitionVersionService> {

	public KaleoDefinitionVersionServiceWrapper() {
		this(null);
	}

	public KaleoDefinitionVersionServiceWrapper(
		KaleoDefinitionVersionService kaleoDefinitionVersionService) {

		_kaleoDefinitionVersionService = kaleoDefinitionVersionService;
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return _kaleoDefinitionVersionService.getOSGiServiceIdentifier();
	}

	@Override
	public KaleoDefinitionVersionService getWrappedService() {
		return _kaleoDefinitionVersionService;
	}

	@Override
	public void setWrappedService(
		KaleoDefinitionVersionService kaleoDefinitionVersionService) {

		_kaleoDefinitionVersionService = kaleoDefinitionVersionService;
	}

	private KaleoDefinitionVersionService _kaleoDefinitionVersionService;

}