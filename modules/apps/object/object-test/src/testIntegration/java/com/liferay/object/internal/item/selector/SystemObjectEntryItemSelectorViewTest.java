/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.item.selector;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorCriterion;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.ItemSelectorViewDescriptorRenderer;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.related.models.ObjectRelatedModelsProviderRegistrarHelper;
import com.liferay.object.related.models.ObjectRelatedModelsProviderRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.service.ObjectFolderLocalService;
import com.liferay.object.system.SystemObjectDefinitionManager;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.portal.kernel.concurrent.test.TestUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Release;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapDictionaryBuilder;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.util.PortalInstances;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Selton Guedes
 */
@RunWith(Arquillian.class)
public class SystemObjectEntryItemSelectorViewTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
//		_company = CompanyTestUtil.addCompany(true);
//
//		_originalName = PrincipalThreadLocal.getName();
//
//		PrincipalThreadLocal.setName(TestPropsValues.getUserId());

		Bundle bundle = FrameworkUtil.getBundle(
			SystemObjectEntryItemSelectorViewTest.class);

		_bundleContext = bundle.getBundleContext();

		_bundleContext.registerService(
			ItemSelectorView.class,
			new SystemObjectEntryItemSelectorView(
				_dtoConverterRegistry, _itemSelector,
				_itemSelectorViewDescriptorRenderer,
				_getUserSystemObjectDefinition(), _objectDefinitionLocalService,
				_objectFieldLocalService, _objectRelatedModelsProviderRegistry,
				_portal, _systemObjectDefinitionManagerRegistry,
				_userLocalService),
			HashMapDictionaryBuilder.<String, Object>put(
				"item.selector.view.order", 500
			).build());

	}

	@AfterClass
	public static void tearDownClass() throws Exception {
//		_companyLocalService.deleteCompany(_company);
//
//		PrincipalThreadLocal.setName(_originalName);
	}

	@Test
	public void testGetTitle() throws PortalException {
		_bundleContext.getServiceReference(ItemSelectorView.class);

		ObjectDefinition objectDefinition = _getUserSystemObjectDefinition();

		//		_systemObjectEntryItemSelectorView.getTitle(_company.getLocale());

		//		Assert.assertEquals(
		//			"User ",
		//			_systemObjectEntryItemSelectorView.getTitle(_company.getLocale()));
	}

	private static ObjectDefinition _getUserSystemObjectDefinition() {
		return _objectDefinitionLocalService.fetchObjectDefinition(
			_company.getCompanyId(), "L_USER");
	}

	private static BundleContext _bundleContext;
	private static Company _company;

	@Inject
	private static CompanyLocalService _companyLocalService;

	@Inject
	private static DTOConverterRegistry _dtoConverterRegistry;

	@Inject
	private static ItemSelector _itemSelector;

	@Inject
	private static ItemSelectorViewDescriptorRenderer
		<InfoItemItemSelectorCriterion> _itemSelectorViewDescriptorRenderer;

	@Inject
	private static ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private static ObjectFieldLocalService _objectFieldLocalService;

	@Inject
	private static ObjectRelatedModelsProviderRegistry
		_objectRelatedModelsProviderRegistry;

	private static String _originalName;

	@Inject
	private static Portal _portal;

	@Inject
	private static SystemObjectDefinitionManagerRegistry
		_systemObjectDefinitionManagerRegistry;

	@Inject
	private static UserLocalService _userLocalService;
}