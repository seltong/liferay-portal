/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.object.internal.item.selector;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.item.selector.ItemSelector;
import com.liferay.item.selector.ItemSelectorView;
import com.liferay.item.selector.ItemSelectorViewDescriptor;
import com.liferay.item.selector.ItemSelectorViewDescriptorRenderer;
import com.liferay.item.selector.criteria.info.item.criterion.InfoItemItemSelectorCriterion;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectField;
import com.liferay.object.related.models.ObjectRelatedModelsProviderRegistry;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectFieldLocalService;
import com.liferay.object.system.SystemObjectDefinitionManager;
import com.liferay.object.system.SystemObjectDefinitionManagerRegistry;
import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderRequest;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletRenderResponse;
import com.liferay.portal.kernel.test.portlet.MockLiferayPortletURL;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.JavaConstants;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.vulcan.dto.converter.DTOConverterRegistry;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import javax.servlet.ServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Selton Guedes
 */
@RunWith(Arquillian.class)
public class SystemObjectEntryItemSelectorViewTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUpClass() throws Exception {
		Bundle bundle = FrameworkUtil.getBundle(
			SystemObjectEntryItemSelectorViewTest.class);

		_bundleContext = bundle.getBundleContext();

		_serviceReferences = _bundleContext.getServiceReferences(
			ItemSelectorView.class, "(item.selector.view.order=500)");

		_group = GroupTestUtil.addGroup();
	}

	@Test
	public void testGetTitle() throws Exception {
		Object[] iterator =
			_serviceReferences.toArray();

		ServiceReference<ItemSelectorView> serviceReference =
			(ServiceReference<ItemSelectorView>) iterator[3];

		ItemSelectorView itemSelectorView = _bundleContext.getService(
			serviceReference);

		Queue<ItemSelectorViewDescriptor> itemSelectorViewDescriptors =
			new LinkedList<>();

		ItemSelectorViewDescriptorRenderer itemSelectorViewDescriptorRenderer =
			(ItemSelectorViewDescriptorRenderer)
				ReflectionTestUtil.getAndSetFieldValue(
					itemSelectorView, "_itemSelectorViewDescriptorRenderer",
					ProxyUtil.newProxyInstance(
						ItemSelectorViewDescriptorRenderer.class.
							getClassLoader(),
						new Class<?>[] {
							ItemSelectorViewDescriptorRenderer.class
						},
						(proxy, method, arguments) -> {
							if (StringUtil.equals(
									method.getName(), "renderHTML")) {

								itemSelectorViewDescriptors.add(
									(ItemSelectorViewDescriptor)arguments[6]);
							}

							return null;
						}));

		itemSelectorView.renderHTML(
			_mockHttpServletRequest(), new MockHttpServletResponse(),
			new InfoItemItemSelectorCriterion(), new MockLiferayPortletURL(),
			RandomTestUtil.randomString(), true);

		ItemSelectorViewDescriptor itemSelectorViewDescriptor =
			itemSelectorViewDescriptors.poll();

		ObjectDefinition objectDefinition =
			_objectDefinitionLocalService.
				getObjectDefinitionByExternalReferenceCode(
					_systemObjectDefinitionsERCs.get(
						itemSelectorView.getTitle(Locale.getDefault())),
					_group.getCompanyId());

		SystemObjectDefinitionManager systemObjectDefinitionManager =
			_systemObjectDefinitionManagerRegistry.
				getSystemObjectDefinitionManager(objectDefinition.getName());

		User user = UserTestUtil.addUser();

		ItemSelectorViewDescriptor.ItemDescriptor itemDescriptor =
			itemSelectorViewDescriptor.getItemDescriptor(
				systemObjectDefinitionManager.getBaseModelByExternalReferenceCode(
					systemObjectDefinitionManager.getBaseModelExternalReferenceCode(
						user.getPrimaryKey()), _group.getCompanyId()));

		long originalTitleObjectFieldId =
			objectDefinition.getTitleObjectFieldId();

		Assert.assertEquals(
			user.getFirstName(), itemDescriptor.getTitle(Locale.getDefault()));

		ObjectField objectField = _objectFieldLocalService.getObjectField(
			objectDefinition.getObjectDefinitionId(), "emailAddress");

		_objectDefinitionLocalService.updateTitleObjectFieldId(
			objectDefinition.getObjectDefinitionId(),
			objectField.getObjectFieldId());

		Assert.assertEquals(
			user.getEmailAddress(),
			itemDescriptor.getTitle(Locale.getDefault()));

		_objectDefinitionLocalService.updateTitleObjectFieldId(
			objectDefinition.getObjectDefinitionId(),
			originalTitleObjectFieldId);

		ReflectionTestUtil.setFieldValue(
			itemSelectorView, "_itemSelectorViewDescriptorRenderer",
			itemSelectorViewDescriptorRenderer);
	}

	private ThemeDisplay _getThemeDisplay(Group group) throws Exception {
		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(
			CompanyLocalServiceUtil.fetchCompany(group.getCompanyId()));
		themeDisplay.setLocale(LocaleUtil.getDefault());
		themeDisplay.setPermissionChecker(
			PermissionThreadLocal.getPermissionChecker());
		themeDisplay.setScopeGroupId(group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());

		return themeDisplay;
	}

	private ObjectDefinition _getUserSystemObjectDefinition() {
		return _objectDefinitionLocalService.fetchObjectDefinition(
			_company.getCompanyId(), "L_USER");
	}

	private ServletRequest _mockHttpServletRequest() throws Exception {
		MockHttpServletRequest mockHttpServletRequest =
			new MockHttpServletRequest();

		MockLiferayPortletRenderRequest mockLiferayPortletRenderRequest =
			new MockLiferayPortletRenderRequest();

		mockLiferayPortletRenderRequest.setAttribute(
			"null-" + WebKeys.CURRENT_PORTLET_URL, new MockLiferayPortletURL());

		mockHttpServletRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_REQUEST,
			mockLiferayPortletRenderRequest);

		mockHttpServletRequest.setAttribute(
			JavaConstants.JAVAX_PORTLET_RESPONSE,
			new MockLiferayPortletRenderResponse());

		ThemeDisplay themeDisplay = _getThemeDisplay(_group);

		themeDisplay.setRequest(mockHttpServletRequest);
		themeDisplay.setUser(TestPropsValues.getUser());

		mockHttpServletRequest.setAttribute(
			WebKeys.THEME_DISPLAY, themeDisplay);

		return mockHttpServletRequest;
	}

	private static BundleContext _bundleContext;
	private static Company _company;
	private static String _originalName;

	@Inject
	private static Portal _portal;

	private static Collection<ServiceReference<ItemSelectorView>>
		_serviceReferences;

	@Inject
	private static SystemObjectDefinitionManagerRegistry
		_systemObjectDefinitionManagerRegistry;

	@Inject
	private static UserLocalService _userLocalService;

	@Inject
	private CompanyLocalService _companyLocalService;

	@Inject
	private DTOConverterRegistry _dtoConverterRegistry;

	@DeleteAfterTestRun
	private Group _group;

	@Inject
	private ItemSelector _itemSelector;

	@Inject
	private ItemSelectorViewDescriptorRenderer<InfoItemItemSelectorCriterion>
		_itemSelectorViewDescriptorRenderer;

	@Inject
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Inject
	private ObjectFieldLocalService _objectFieldLocalService;

	@Inject
	private ObjectRelatedModelsProviderRegistry
		_objectRelatedModelsProviderRegistry;

	private final Map<String, String> _systemObjectDefinitionsERCs =
		HashMapBuilder.put(
			"Account", "L_ACCOUNT"
		).put(
			"Commerce Order", "L_COMMERCE_ORDER"
		).put(
			"Commerce Product", "L_COMMERCE_PRODUCT_DEFINITION"
		).put(
			"Commerce Product Group", "L_COMMERCE_PRODUCT_GROUP"
		).put(
			"Organization", "L_ORGANIZATION"
		).put(
			"Postal Address", "L_POSTAL_ADDRESS"
		).put(
			"Users", "L_USER"
		).build();

}