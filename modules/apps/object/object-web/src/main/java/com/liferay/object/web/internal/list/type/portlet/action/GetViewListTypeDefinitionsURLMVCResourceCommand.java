/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.web.internal.list.type.portlet.action;

import com.liferay.object.constants.ObjectPortletKeys;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.portlet.JSONPortletResponseUtil;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactory;
import com.liferay.portal.kernel.portlet.RequestBackedPortletURLFactoryUtil;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.portlet.url.builder.PortletURLBuilder;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author Selton Guedes
 */
@Component(
	property = {
		"javax.portlet.name=" + ObjectPortletKeys.LIST_TYPE_DEFINITIONS,
		"mvc.command.name=/list_type_definitions/get_view_list_type_definitions_url"
	},
	service = MVCResourceCommand.class
)
public class GetViewListTypeDefinitionsURLMVCResourceCommand
	extends BaseMVCResourceCommand {

	@Override
	protected void doServeResource(
			ResourceRequest resourceRequest, ResourceResponse resourceResponse)
		throws Exception {

		RequestBackedPortletURLFactory requestBackedPortletURLFactory =
			RequestBackedPortletURLFactoryUtil.create(resourceRequest);

		JSONPortletResponseUtil.writeJSON(
			resourceRequest, resourceResponse,
			JSONUtil.put(
				"url",
				PortletURLBuilder.create(
					requestBackedPortletURLFactory.createControlPanelRenderURL(
						ObjectPortletKeys.LIST_TYPE_DEFINITIONS, null, 0, 0)
				).setMVCRenderCommandName(
					"/list_type_definitions/view_list_type_definitions"
				).buildString()));
	}

}