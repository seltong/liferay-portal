/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.object.admin.rest.resource.v1_0.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import com.liferay.object.admin.rest.client.dto.v1_0.ObjectFlow;
import com.liferay.object.admin.rest.client.http.HttpInvoker;
import com.liferay.object.admin.rest.client.pagination.Page;
import com.liferay.object.admin.rest.client.pagination.Pagination;
import com.liferay.object.admin.rest.client.resource.v1_0.ObjectFlowResource;
import com.liferay.object.admin.rest.client.serdes.v1_0.ObjectFlowSerDes;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.odata.entity.EntityField;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.search.test.util.SearchTestRule;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.vulcan.resource.EntityModelResource;

import java.lang.reflect.Method;

import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Generated;

import javax.ws.rs.core.MultivaluedHashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public abstract class BaseObjectFlowResourceTestCase {

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() throws Exception {
		_dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	}

	@Before
	public void setUp() throws Exception {
		irrelevantGroup = GroupTestUtil.addGroup();
		testGroup = GroupTestUtil.addGroup();

		testCompany = CompanyLocalServiceUtil.getCompany(
			testGroup.getCompanyId());

		_objectFlowResource.setContextCompany(testCompany);

		ObjectFlowResource.Builder builder = ObjectFlowResource.builder();

		objectFlowResource = builder.authentication(
			"test@liferay.com", "test"
		).locale(
			LocaleUtil.getDefault()
		).build();
	}

	@After
	public void tearDown() throws Exception {
		GroupTestUtil.deleteGroup(irrelevantGroup);
		GroupTestUtil.deleteGroup(testGroup);
	}

	@Test
	public void testClientSerDesToDTO() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				enable(SerializationFeature.INDENT_OUTPUT);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};

		ObjectFlow objectFlow1 = randomObjectFlow();

		String json = objectMapper.writeValueAsString(objectFlow1);

		ObjectFlow objectFlow2 = ObjectFlowSerDes.toDTO(json);

		Assert.assertTrue(equals(objectFlow1, objectFlow2));
	}

	@Test
	public void testClientSerDesToJSON() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper() {
			{
				configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
				configure(
					SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
				setDateFormat(new ISO8601DateFormat());
				setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
				setSerializationInclusion(JsonInclude.Include.NON_NULL);
				setVisibility(
					PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
				setVisibility(
					PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
			}
		};

		ObjectFlow objectFlow = randomObjectFlow();

		String json1 = objectMapper.writeValueAsString(objectFlow);
		String json2 = ObjectFlowSerDes.toJSON(objectFlow);

		Assert.assertEquals(
			objectMapper.readTree(json1), objectMapper.readTree(json2));
	}

	@Test
	public void testEscapeRegexInStringFields() throws Exception {
		String regex = "^[0-9]+(\\.[0-9]{1,2})\"?";

		ObjectFlow objectFlow = randomObjectFlow();

		objectFlow.setName(regex);

		String json = ObjectFlowSerDes.toJSON(objectFlow);

		Assert.assertFalse(json.contains(regex));

		objectFlow = ObjectFlowSerDes.toDTO(json);

		Assert.assertEquals(regex, objectFlow.getName());
	}

	@Test
	public void testGetObjectDefinitionObjectStatesPage() throws Exception {
		Long objectDefinitionId =
			testGetObjectDefinitionObjectStatesPage_getObjectDefinitionId();
		Long irrelevantObjectDefinitionId =
			testGetObjectDefinitionObjectStatesPage_getIrrelevantObjectDefinitionId();

		Page<ObjectFlow> page =
			objectFlowResource.getObjectDefinitionObjectStatesPage(
				objectDefinitionId, null, null, Pagination.of(1, 10));

		Assert.assertEquals(0, page.getTotalCount());

		if (irrelevantObjectDefinitionId != null) {
			ObjectFlow irrelevantObjectFlow =
				testGetObjectDefinitionObjectStatesPage_addObjectFlow(
					irrelevantObjectDefinitionId, randomIrrelevantObjectFlow());

			page = objectFlowResource.getObjectDefinitionObjectStatesPage(
				irrelevantObjectDefinitionId, null, null, Pagination.of(1, 2));

			Assert.assertEquals(1, page.getTotalCount());

			assertEquals(
				Arrays.asList(irrelevantObjectFlow),
				(List<ObjectFlow>)page.getItems());
			assertValid(page);
		}

		ObjectFlow objectFlow1 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		ObjectFlow objectFlow2 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		page = objectFlowResource.getObjectDefinitionObjectStatesPage(
			objectDefinitionId, null, null, Pagination.of(1, 10));

		Assert.assertEquals(2, page.getTotalCount());

		assertEqualsIgnoringOrder(
			Arrays.asList(objectFlow1, objectFlow2),
			(List<ObjectFlow>)page.getItems());
		assertValid(page);
	}

	@Test
	public void testGetObjectDefinitionObjectStatesPageWithFilterDateTimeEquals()
		throws Exception {

		List<EntityField> entityFields = getEntityFields(
			EntityField.Type.DATE_TIME);

		if (entityFields.isEmpty()) {
			return;
		}

		Long objectDefinitionId =
			testGetObjectDefinitionObjectStatesPage_getObjectDefinitionId();

		ObjectFlow objectFlow1 = randomObjectFlow();

		objectFlow1 = testGetObjectDefinitionObjectStatesPage_addObjectFlow(
			objectDefinitionId, objectFlow1);

		for (EntityField entityField : entityFields) {
			Page<ObjectFlow> page =
				objectFlowResource.getObjectDefinitionObjectStatesPage(
					objectDefinitionId, null,
					getFilterString(entityField, "between", objectFlow1),
					Pagination.of(1, 2));

			assertEquals(
				Collections.singletonList(objectFlow1),
				(List<ObjectFlow>)page.getItems());
		}
	}

	@Test
	public void testGetObjectDefinitionObjectStatesPageWithFilterDoubleEquals()
		throws Exception {

		List<EntityField> entityFields = getEntityFields(
			EntityField.Type.DOUBLE);

		if (entityFields.isEmpty()) {
			return;
		}

		Long objectDefinitionId =
			testGetObjectDefinitionObjectStatesPage_getObjectDefinitionId();

		ObjectFlow objectFlow1 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		@SuppressWarnings("PMD.UnusedLocalVariable")
		ObjectFlow objectFlow2 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		for (EntityField entityField : entityFields) {
			Page<ObjectFlow> page =
				objectFlowResource.getObjectDefinitionObjectStatesPage(
					objectDefinitionId, null,
					getFilterString(entityField, "eq", objectFlow1),
					Pagination.of(1, 2));

			assertEquals(
				Collections.singletonList(objectFlow1),
				(List<ObjectFlow>)page.getItems());
		}
	}

	@Test
	public void testGetObjectDefinitionObjectStatesPageWithFilterStringEquals()
		throws Exception {

		List<EntityField> entityFields = getEntityFields(
			EntityField.Type.STRING);

		if (entityFields.isEmpty()) {
			return;
		}

		Long objectDefinitionId =
			testGetObjectDefinitionObjectStatesPage_getObjectDefinitionId();

		ObjectFlow objectFlow1 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		@SuppressWarnings("PMD.UnusedLocalVariable")
		ObjectFlow objectFlow2 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		for (EntityField entityField : entityFields) {
			Page<ObjectFlow> page =
				objectFlowResource.getObjectDefinitionObjectStatesPage(
					objectDefinitionId, null,
					getFilterString(entityField, "eq", objectFlow1),
					Pagination.of(1, 2));

			assertEquals(
				Collections.singletonList(objectFlow1),
				(List<ObjectFlow>)page.getItems());
		}
	}

	@Test
	public void testGetObjectDefinitionObjectStatesPageWithPagination()
		throws Exception {

		Long objectDefinitionId =
			testGetObjectDefinitionObjectStatesPage_getObjectDefinitionId();

		ObjectFlow objectFlow1 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		ObjectFlow objectFlow2 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		ObjectFlow objectFlow3 =
			testGetObjectDefinitionObjectStatesPage_addObjectFlow(
				objectDefinitionId, randomObjectFlow());

		Page<ObjectFlow> page1 =
			objectFlowResource.getObjectDefinitionObjectStatesPage(
				objectDefinitionId, null, null, Pagination.of(1, 2));

		List<ObjectFlow> objectFlows1 = (List<ObjectFlow>)page1.getItems();

		Assert.assertEquals(objectFlows1.toString(), 2, objectFlows1.size());

		Page<ObjectFlow> page2 =
			objectFlowResource.getObjectDefinitionObjectStatesPage(
				objectDefinitionId, null, null, Pagination.of(2, 2));

		Assert.assertEquals(3, page2.getTotalCount());

		List<ObjectFlow> objectFlows2 = (List<ObjectFlow>)page2.getItems();

		Assert.assertEquals(objectFlows2.toString(), 1, objectFlows2.size());

		Page<ObjectFlow> page3 =
			objectFlowResource.getObjectDefinitionObjectStatesPage(
				objectDefinitionId, null, null, Pagination.of(1, 3));

		assertEqualsIgnoringOrder(
			Arrays.asList(objectFlow1, objectFlow2, objectFlow3),
			(List<ObjectFlow>)page3.getItems());
	}

	protected ObjectFlow testGetObjectDefinitionObjectStatesPage_addObjectFlow(
			Long objectDefinitionId, ObjectFlow objectFlow)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long
			testGetObjectDefinitionObjectStatesPage_getObjectDefinitionId()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected Long
			testGetObjectDefinitionObjectStatesPage_getIrrelevantObjectDefinitionId()
		throws Exception {

		return null;
	}

	@Test
	public void testPostObjectDefinitionObjectState() throws Exception {
		ObjectFlow randomObjectFlow = randomObjectFlow();

		ObjectFlow postObjectFlow =
			testPostObjectDefinitionObjectState_addObjectFlow(randomObjectFlow);

		assertEquals(randomObjectFlow, postObjectFlow);
		assertValid(postObjectFlow);
	}

	protected ObjectFlow testPostObjectDefinitionObjectState_addObjectFlow(
			ObjectFlow objectFlow)
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testDeleteObjectStateObjectFlow() throws Exception {
		@SuppressWarnings("PMD.UnusedLocalVariable")
		ObjectFlow objectFlow = testDeleteObjectStateObjectFlow_addObjectFlow();

		assertHttpResponseStatusCode(
			204,
			objectFlowResource.deleteObjectStateObjectFlowHttpResponse(
				objectFlow.getId()));

		assertHttpResponseStatusCode(
			404,
			objectFlowResource.getObjectStateObjectFlowHttpResponse(
				objectFlow.getId()));

		assertHttpResponseStatusCode(
			404, objectFlowResource.getObjectStateObjectFlowHttpResponse(0L));
	}

	protected ObjectFlow testDeleteObjectStateObjectFlow_addObjectFlow()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGetObjectStateObjectFlow() throws Exception {
		ObjectFlow postObjectFlow =
			testGetObjectStateObjectFlow_addObjectFlow();

		ObjectFlow getObjectFlow = objectFlowResource.getObjectStateObjectFlow(
			postObjectFlow.getId());

		assertEquals(postObjectFlow, getObjectFlow);
		assertValid(getObjectFlow);
	}

	protected ObjectFlow testGetObjectStateObjectFlow_addObjectFlow()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testGraphQLGetObjectStateObjectFlow() throws Exception {
		ObjectFlow objectFlow =
			testGraphQLGetObjectStateObjectFlow_addObjectFlow();

		Assert.assertTrue(
			equals(
				objectFlow,
				ObjectFlowSerDes.toDTO(
					JSONUtil.getValueAsString(
						invokeGraphQLQuery(
							new GraphQLField(
								"objectStateObjectFlow",
								new HashMap<String, Object>() {
									{
										put("objectFlowId", objectFlow.getId());
									}
								},
								getGraphQLFields())),
						"JSONObject/data", "Object/objectStateObjectFlow"))));
	}

	@Test
	public void testGraphQLGetObjectStateObjectFlowNotFound() throws Exception {
		Long irrelevantObjectFlowId = RandomTestUtil.randomLong();

		Assert.assertEquals(
			"Not Found",
			JSONUtil.getValueAsString(
				invokeGraphQLQuery(
					new GraphQLField(
						"objectStateObjectFlow",
						new HashMap<String, Object>() {
							{
								put("objectFlowId", irrelevantObjectFlowId);
							}
						},
						getGraphQLFields())),
				"JSONArray/errors", "Object/0", "JSONObject/extensions",
				"Object/code"));
	}

	protected ObjectFlow testGraphQLGetObjectStateObjectFlow_addObjectFlow()
		throws Exception {

		return testGraphQLObjectFlow_addObjectFlow();
	}

	@Test
	public void testPatchObjectStateObjectFlow() throws Exception {
		ObjectFlow postObjectFlow =
			testPatchObjectStateObjectFlow_addObjectFlow();

		ObjectFlow randomPatchObjectFlow = randomPatchObjectFlow();

		@SuppressWarnings("PMD.UnusedLocalVariable")
		ObjectFlow patchObjectFlow =
			objectFlowResource.patchObjectStateObjectFlow(
				postObjectFlow.getId(), randomPatchObjectFlow);

		ObjectFlow expectedPatchObjectFlow = postObjectFlow.clone();

		BeanTestUtil.copyProperties(
			randomPatchObjectFlow, expectedPatchObjectFlow);

		ObjectFlow getObjectFlow = objectFlowResource.getObjectStateObjectFlow(
			patchObjectFlow.getId());

		assertEquals(expectedPatchObjectFlow, getObjectFlow);
		assertValid(getObjectFlow);
	}

	protected ObjectFlow testPatchObjectStateObjectFlow_addObjectFlow()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Test
	public void testPutObjectStateObjectFlow() throws Exception {
		ObjectFlow postObjectFlow =
			testPutObjectStateObjectFlow_addObjectFlow();

		ObjectFlow randomObjectFlow = randomObjectFlow();

		ObjectFlow putObjectFlow = objectFlowResource.putObjectStateObjectFlow(
			postObjectFlow.getId(), randomObjectFlow);

		assertEquals(randomObjectFlow, putObjectFlow);
		assertValid(putObjectFlow);

		ObjectFlow getObjectFlow = objectFlowResource.getObjectStateObjectFlow(
			putObjectFlow.getId());

		assertEquals(randomObjectFlow, getObjectFlow);
		assertValid(getObjectFlow);
	}

	protected ObjectFlow testPutObjectStateObjectFlow_addObjectFlow()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	@Rule
	public SearchTestRule searchTestRule = new SearchTestRule();

	protected ObjectFlow testGraphQLObjectFlow_addObjectFlow()
		throws Exception {

		throw new UnsupportedOperationException(
			"This method needs to be implemented");
	}

	protected void assertContains(
		ObjectFlow objectFlow, List<ObjectFlow> objectFlows) {

		boolean contains = false;

		for (ObjectFlow item : objectFlows) {
			if (equals(objectFlow, item)) {
				contains = true;

				break;
			}
		}

		Assert.assertTrue(
			objectFlows + " does not contain " + objectFlow, contains);
	}

	protected void assertHttpResponseStatusCode(
		int expectedHttpResponseStatusCode,
		HttpInvoker.HttpResponse actualHttpResponse) {

		Assert.assertEquals(
			expectedHttpResponseStatusCode, actualHttpResponse.getStatusCode());
	}

	protected void assertEquals(
		ObjectFlow objectFlow1, ObjectFlow objectFlow2) {

		Assert.assertTrue(
			objectFlow1 + " does not equal " + objectFlow2,
			equals(objectFlow1, objectFlow2));
	}

	protected void assertEquals(
		List<ObjectFlow> objectFlows1, List<ObjectFlow> objectFlows2) {

		Assert.assertEquals(objectFlows1.size(), objectFlows2.size());

		for (int i = 0; i < objectFlows1.size(); i++) {
			ObjectFlow objectFlow1 = objectFlows1.get(i);
			ObjectFlow objectFlow2 = objectFlows2.get(i);

			assertEquals(objectFlow1, objectFlow2);
		}
	}

	protected void assertEqualsIgnoringOrder(
		List<ObjectFlow> objectFlows1, List<ObjectFlow> objectFlows2) {

		Assert.assertEquals(objectFlows1.size(), objectFlows2.size());

		for (ObjectFlow objectFlow1 : objectFlows1) {
			boolean contains = false;

			for (ObjectFlow objectFlow2 : objectFlows2) {
				if (equals(objectFlow1, objectFlow2)) {
					contains = true;

					break;
				}
			}

			Assert.assertTrue(
				objectFlows2 + " does not contain " + objectFlow1, contains);
		}
	}

	protected void assertValid(ObjectFlow objectFlow) throws Exception {
		boolean valid = true;

		if (objectFlow.getId() == null) {
			valid = false;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("currentState", additionalAssertFieldName)) {
				if (objectFlow.getCurrentState() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (objectFlow.getName() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals(
					"objectDefinitionId", additionalAssertFieldName)) {

				if (objectFlow.getObjectDefinitionId() == null) {
					valid = false;
				}

				continue;
			}

			if (Objects.equals("states", additionalAssertFieldName)) {
				if (objectFlow.getStates() == null) {
					valid = false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		Assert.assertTrue(valid);
	}

	protected void assertValid(Page<ObjectFlow> page) {
		boolean valid = false;

		java.util.Collection<ObjectFlow> objectFlows = page.getItems();

		int size = objectFlows.size();

		if ((page.getLastPage() > 0) && (page.getPage() > 0) &&
			(page.getPageSize() > 0) && (page.getTotalCount() > 0) &&
			(size > 0)) {

			valid = true;
		}

		Assert.assertTrue(valid);
	}

	protected String[] getAdditionalAssertFieldNames() {
		return new String[0];
	}

	protected List<GraphQLField> getGraphQLFields() throws Exception {
		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field :
				getDeclaredFields(
					com.liferay.object.admin.rest.dto.v1_0.ObjectFlow.class)) {

			if (!ArrayUtil.contains(
					getAdditionalAssertFieldNames(), field.getName())) {

				continue;
			}

			graphQLFields.addAll(getGraphQLFields(field));
		}

		return graphQLFields;
	}

	protected List<GraphQLField> getGraphQLFields(
			java.lang.reflect.Field... fields)
		throws Exception {

		List<GraphQLField> graphQLFields = new ArrayList<>();

		for (java.lang.reflect.Field field : fields) {
			com.liferay.portal.vulcan.graphql.annotation.GraphQLField
				vulcanGraphQLField = field.getAnnotation(
					com.liferay.portal.vulcan.graphql.annotation.GraphQLField.
						class);

			if (vulcanGraphQLField != null) {
				Class<?> clazz = field.getType();

				if (clazz.isArray()) {
					clazz = clazz.getComponentType();
				}

				List<GraphQLField> childrenGraphQLFields = getGraphQLFields(
					getDeclaredFields(clazz));

				graphQLFields.add(
					new GraphQLField(field.getName(), childrenGraphQLFields));
			}
		}

		return graphQLFields;
	}

	protected String[] getIgnoredEntityFieldNames() {
		return new String[0];
	}

	protected boolean equals(ObjectFlow objectFlow1, ObjectFlow objectFlow2) {
		if (objectFlow1 == objectFlow2) {
			return true;
		}

		for (String additionalAssertFieldName :
				getAdditionalAssertFieldNames()) {

			if (Objects.equals("currentState", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						objectFlow1.getCurrentState(),
						objectFlow2.getCurrentState())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("id", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						objectFlow1.getId(), objectFlow2.getId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("name", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						objectFlow1.getName(), objectFlow2.getName())) {

					return false;
				}

				continue;
			}

			if (Objects.equals(
					"objectDefinitionId", additionalAssertFieldName)) {

				if (!Objects.deepEquals(
						objectFlow1.getObjectDefinitionId(),
						objectFlow2.getObjectDefinitionId())) {

					return false;
				}

				continue;
			}

			if (Objects.equals("states", additionalAssertFieldName)) {
				if (!Objects.deepEquals(
						objectFlow1.getStates(), objectFlow2.getStates())) {

					return false;
				}

				continue;
			}

			throw new IllegalArgumentException(
				"Invalid additional assert field name " +
					additionalAssertFieldName);
		}

		return true;
	}

	protected boolean equals(
		Map<String, Object> map1, Map<String, Object> map2) {

		if (Objects.equals(map1.keySet(), map2.keySet())) {
			for (Map.Entry<String, Object> entry : map1.entrySet()) {
				if (entry.getValue() instanceof Map) {
					if (!equals(
							(Map)entry.getValue(),
							(Map)map2.get(entry.getKey()))) {

						return false;
					}
				}
				else if (!Objects.deepEquals(
							entry.getValue(), map2.get(entry.getKey()))) {

					return false;
				}
			}

			return true;
		}

		return false;
	}

	protected java.lang.reflect.Field[] getDeclaredFields(Class clazz)
		throws Exception {

		Stream<java.lang.reflect.Field> stream = Stream.of(
			ReflectionUtil.getDeclaredFields(clazz));

		return stream.filter(
			field -> !field.isSynthetic()
		).toArray(
			java.lang.reflect.Field[]::new
		);
	}

	protected java.util.Collection<EntityField> getEntityFields()
		throws Exception {

		if (!(_objectFlowResource instanceof EntityModelResource)) {
			throw new UnsupportedOperationException(
				"Resource is not an instance of EntityModelResource");
		}

		EntityModelResource entityModelResource =
			(EntityModelResource)_objectFlowResource;

		EntityModel entityModel = entityModelResource.getEntityModel(
			new MultivaluedHashMap());

		Map<String, EntityField> entityFieldsMap =
			entityModel.getEntityFieldsMap();

		return entityFieldsMap.values();
	}

	protected List<EntityField> getEntityFields(EntityField.Type type)
		throws Exception {

		java.util.Collection<EntityField> entityFields = getEntityFields();

		Stream<EntityField> stream = entityFields.stream();

		return stream.filter(
			entityField ->
				Objects.equals(entityField.getType(), type) &&
				!ArrayUtil.contains(
					getIgnoredEntityFieldNames(), entityField.getName())
		).collect(
			Collectors.toList()
		);
	}

	protected String getFilterString(
		EntityField entityField, String operator, ObjectFlow objectFlow) {

		StringBundler sb = new StringBundler();

		String entityFieldName = entityField.getName();

		sb.append(entityFieldName);

		sb.append(" ");
		sb.append(operator);
		sb.append(" ");

		if (entityFieldName.equals("currentState")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("id")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("name")) {
			sb.append("'");
			sb.append(String.valueOf(objectFlow.getName()));
			sb.append("'");

			return sb.toString();
		}

		if (entityFieldName.equals("objectDefinitionId")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		if (entityFieldName.equals("states")) {
			throw new IllegalArgumentException(
				"Invalid entity field " + entityFieldName);
		}

		throw new IllegalArgumentException(
			"Invalid entity field " + entityFieldName);
	}

	protected String invoke(String query) throws Exception {
		HttpInvoker httpInvoker = HttpInvoker.newHttpInvoker();

		httpInvoker.body(
			JSONUtil.put(
				"query", query
			).toString(),
			"application/json");
		httpInvoker.httpMethod(HttpInvoker.HttpMethod.POST);
		httpInvoker.path("http://localhost:8080/o/graphql");
		httpInvoker.userNameAndPassword("test@liferay.com:test");

		HttpInvoker.HttpResponse httpResponse = httpInvoker.invoke();

		return httpResponse.getContent();
	}

	protected JSONObject invokeGraphQLMutation(GraphQLField graphQLField)
		throws Exception {

		GraphQLField mutationGraphQLField = new GraphQLField(
			"mutation", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(mutationGraphQLField.toString()));
	}

	protected JSONObject invokeGraphQLQuery(GraphQLField graphQLField)
		throws Exception {

		GraphQLField queryGraphQLField = new GraphQLField(
			"query", graphQLField);

		return JSONFactoryUtil.createJSONObject(
			invoke(queryGraphQLField.toString()));
	}

	protected ObjectFlow randomObjectFlow() throws Exception {
		return new ObjectFlow() {
			{
				id = RandomTestUtil.randomLong();
				name = StringUtil.toLowerCase(RandomTestUtil.randomString());
				objectDefinitionId = RandomTestUtil.randomLong();
			}
		};
	}

	protected ObjectFlow randomIrrelevantObjectFlow() throws Exception {
		ObjectFlow randomIrrelevantObjectFlow = randomObjectFlow();

		return randomIrrelevantObjectFlow;
	}

	protected ObjectFlow randomPatchObjectFlow() throws Exception {
		return randomObjectFlow();
	}

	protected ObjectFlowResource objectFlowResource;
	protected Group irrelevantGroup;
	protected Company testCompany;
	protected Group testGroup;

	protected static class BeanTestUtil {

		public static void copyProperties(Object source, Object target)
			throws Exception {

			Class<?> sourceClass = _getSuperClass(source.getClass());

			Class<?> targetClass = target.getClass();

			for (java.lang.reflect.Field field :
					sourceClass.getDeclaredFields()) {

				if (field.isSynthetic()) {
					continue;
				}

				Method getMethod = _getMethod(
					sourceClass, field.getName(), "get");

				Method setMethod = _getMethod(
					targetClass, field.getName(), "set",
					getMethod.getReturnType());

				setMethod.invoke(target, getMethod.invoke(source));
			}
		}

		public static boolean hasProperty(Object bean, String name) {
			Method setMethod = _getMethod(
				bean.getClass(), "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod != null) {
				return true;
			}

			return false;
		}

		public static void setProperty(Object bean, String name, Object value)
			throws Exception {

			Class<?> clazz = bean.getClass();

			Method setMethod = _getMethod(
				clazz, "set" + StringUtil.upperCaseFirstLetter(name));

			if (setMethod == null) {
				throw new NoSuchMethodException();
			}

			Class<?>[] parameterTypes = setMethod.getParameterTypes();

			setMethod.invoke(bean, _translateValue(parameterTypes[0], value));
		}

		private static Method _getMethod(Class<?> clazz, String name) {
			for (Method method : clazz.getMethods()) {
				if (name.equals(method.getName()) &&
					(method.getParameterCount() == 1) &&
					_parameterTypes.contains(method.getParameterTypes()[0])) {

					return method;
				}
			}

			return null;
		}

		private static Method _getMethod(
				Class<?> clazz, String fieldName, String prefix,
				Class<?>... parameterTypes)
			throws Exception {

			return clazz.getMethod(
				prefix + StringUtil.upperCaseFirstLetter(fieldName),
				parameterTypes);
		}

		private static Class<?> _getSuperClass(Class<?> clazz) {
			Class<?> superClass = clazz.getSuperclass();

			if ((superClass == null) || (superClass == Object.class)) {
				return clazz;
			}

			return superClass;
		}

		private static Object _translateValue(
			Class<?> parameterType, Object value) {

			if ((value instanceof Integer) &&
				parameterType.equals(Long.class)) {

				Integer intValue = (Integer)value;

				return intValue.longValue();
			}

			return value;
		}

		private static final Set<Class<?>> _parameterTypes = new HashSet<>(
			Arrays.asList(
				Boolean.class, Date.class, Double.class, Integer.class,
				Long.class, Map.class, String.class));

	}

	protected class GraphQLField {

		public GraphQLField(String key, GraphQLField... graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(String key, List<GraphQLField> graphQLFields) {
			this(key, new HashMap<>(), graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			GraphQLField... graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = Arrays.asList(graphQLFields);
		}

		public GraphQLField(
			String key, Map<String, Object> parameterMap,
			List<GraphQLField> graphQLFields) {

			_key = key;
			_parameterMap = parameterMap;
			_graphQLFields = graphQLFields;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(_key);

			if (!_parameterMap.isEmpty()) {
				sb.append("(");

				for (Map.Entry<String, Object> entry :
						_parameterMap.entrySet()) {

					sb.append(entry.getKey());
					sb.append(": ");
					sb.append(entry.getValue());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append(")");
			}

			if (!_graphQLFields.isEmpty()) {
				sb.append("{");

				for (GraphQLField graphQLField : _graphQLFields) {
					sb.append(graphQLField.toString());
					sb.append(", ");
				}

				sb.setLength(sb.length() - 2);

				sb.append("}");
			}

			return sb.toString();
		}

		private final List<GraphQLField> _graphQLFields;
		private final String _key;
		private final Map<String, Object> _parameterMap;

	}

	private static final com.liferay.portal.kernel.log.Log _log =
		LogFactoryUtil.getLog(BaseObjectFlowResourceTestCase.class);

	private static DateFormat _dateFormat;

	@Inject
	private com.liferay.object.admin.rest.resource.v1_0.ObjectFlowResource
		_objectFlowResource;

}