/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.notification.internal.type.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.notification.constants.NotificationConstants;
import com.liferay.notification.constants.NotificationQueueEntryConstants;
import com.liferay.notification.model.NotificationQueueEntry;
import com.liferay.notification.model.NotificationRecipient;
import com.liferay.notification.model.NotificationTemplate;
import com.liferay.notification.service.test.util.NotificationTemplateUtil;
import com.liferay.notification.util.NotificationRecipientSettingUtil;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.constants.ObjectActionTriggerConstants;
import com.liferay.object.constants.ObjectDefinitionConstants;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.rest.dto.v1_0.ObjectEntry;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodePropertiesBuilder;
import com.liferay.portal.test.mail.MailServiceTestUtil;
import com.liferay.portal.test.rule.FeatureFlags;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.SynchronousMailTestRule;
import com.liferay.portal.vulcan.util.LocalizedMapUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Feliphe Marinho
 */
@FeatureFlags("LPS-187854")
@RunWith(Arquillian.class)
public class EmailNotificationTypeTest extends BaseNotificationTypeTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), SynchronousMailTestRule.INSTANCE);

	@Test
	public void testSendNotification() throws Exception {
		Assert.assertEquals(
			0,
			notificationQueueEntryLocalService.
				getNotificationQueueEntriesCount());

		NotificationTemplate notificationTemplate = _addNotificationTemplate(
			StringPool.FALSE);

		ObjectAction objectAction = objectActionLocalService.addObjectAction(
			RandomTestUtil.randomString(), TestPropsValues.getUserId(),
			childObjectDefinition.getObjectDefinitionId(), true,
			StringPool.BLANK, RandomTestUtil.randomString(),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			RandomTestUtil.randomString(),
			ObjectActionExecutorConstants.KEY_NOTIFICATION,
			ObjectActionTriggerConstants.KEY_ON_AFTER_ADD,
			UnicodePropertiesBuilder.put(
				"notificationTemplateId",
				notificationTemplate.getNotificationTemplateId()
			).build());

		_addObjectEntry();

		_assetNotificationQueueEntry(
			false,
			Collections.singletonList(
				StringBundler.concat(
					user1.getEmailAddress(), StringPool.COMMA,
					user2.getEmailAddress())),
			notificationQueueEntryLocalService.getNotificationEntries(
				NotificationConstants.TYPE_EMAIL,
				NotificationQueueEntryConstants.STATUS_SENT));

		notificationTemplate = _addNotificationTemplate(StringPool.TRUE);

		objectActionLocalService.updateObjectAction(
			RandomTestUtil.randomString(), objectAction.getObjectActionId(),
			true, StringPool.BLANK, RandomTestUtil.randomString(),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			LocalizedMapUtil.getLocalizedMap(RandomTestUtil.randomString()),
			RandomTestUtil.randomString(),
			ObjectActionExecutorConstants.KEY_NOTIFICATION,
			ObjectActionTriggerConstants.KEY_ON_AFTER_ADD,
			UnicodePropertiesBuilder.put(
				"notificationTemplateId",
				notificationTemplate.getNotificationTemplateId()
			).build());

		_addObjectEntry();

		_assetNotificationQueueEntry(
			true,
			ListUtil.sort(
				Arrays.asList(
					user1.getEmailAddress(), user2.getEmailAddress())),
			ListUtil.sort(
				notificationQueueEntryLocalService.getNotificationEntries(
					NotificationConstants.TYPE_EMAIL,
					NotificationQueueEntryConstants.STATUS_SENT),
				Comparator.comparing(
					notification -> {
						NotificationRecipient notificationRecipient =
							notification.getNotificationRecipient();

						Map<String, Object> notificationRecipientSettingsMap =
							NotificationRecipientSettingUtil.toMap(
								notificationRecipient.
									getNotificationRecipientSettings());

						return String.valueOf(
							notificationRecipientSettingsMap.get("to"));
					})));
	}

	private NotificationTemplate _addNotificationTemplate(
			String singleRecipient)
		throws Exception {

		return notificationTemplateLocalService.addNotificationTemplate(
			NotificationTemplateUtil.createNotificationContext(
				TestPropsValues.getUser(),
				ListUtil.toString(getTermNames(), StringPool.BLANK),
				RandomTestUtil.randomString(),
				Arrays.asList(
					createNotificationRecipientSetting(
						"bcc",
						"[%CURRENT_USER_EMAIL_ADDRESS%],bcc@liferay.com"),
					createNotificationRecipientSetting(
						"cc", "[%CURRENT_USER_EMAIL_ADDRESS%],cc@liferay.com"),
					createNotificationRecipientSetting(
						"from", "[%CURRENT_USER_EMAIL_ADDRESS%]"),
					createNotificationRecipientSetting(
						"fromName",
						Collections.singletonMap(
							LocaleUtil.US, "[%CURRENT_USER_FIRST_NAME%]")),
					createNotificationRecipientSetting(
						"singleRecipient", singleRecipient),
					createNotificationRecipientSetting(
						"to",
						Collections.singletonMap(
							LocaleUtil.US,
							StringBundler.concat(
								user1.getEmailAddress(), StringPool.COMMA,
								user2.getEmailAddress())))),
				ListUtil.toString(getTermNames(), StringPool.BLANK),
				NotificationConstants.TYPE_EMAIL));
	}

	private void _addObjectEntry() throws Exception {
		ObjectEntry objectEntry = objectEntryManager.addObjectEntry(
			dtoConverterContext, parentObjectDefinition,
			new ObjectEntry() {
				{
					setProperties(parentObjectEntryValues);
				}
			},
			ObjectDefinitionConstants.SCOPE_COMPANY);

		objectEntryManager.addObjectEntry(
			dtoConverterContext, childObjectDefinition,
			new ObjectEntry() {
				{
					setProperties(
						HashMapBuilder.putAll(
							childObjectEntryValues
						).put(
							getObjectRelationshipObjectField2Name(),
							objectEntry.getId()
						).build());
				}
			},
			ObjectDefinitionConstants.SCOPE_COMPANY);
	}

	private void _assetNotificationQueueEntry(
			boolean expectedSingleRecipient, List<String> expectedToRecipients,
			List<NotificationQueueEntry> notificationQueueEntries)
		throws Exception {

		Assert.assertEquals(
			notificationQueueEntries.toString(), expectedToRecipients.size(),
			notificationQueueEntries.size());

		for (int i = 0; i < notificationQueueEntries.size(); i++) {
			String expectedToRecipient = expectedToRecipients.get(i);

			Assert.assertNotNull(
				MailServiceTestUtil.getMailMessages("To", expectedToRecipient));

			NotificationQueueEntry notificationQueueEntry =
				notificationQueueEntries.get(i);

			assertTermValues(
				getTermValues(),
				ListUtil.fromString(
					notificationQueueEntry.getBody(), StringPool.COMMA));
			assertTermValues(
				getTermValues(),
				ListUtil.fromString(
					notificationQueueEntry.getSubject(), StringPool.COMMA));

			NotificationRecipient notificationRecipient =
				notificationQueueEntry.getNotificationRecipient();

			Map<String, Object> notificationRecipientSettingsMap =
				NotificationRecipientSettingUtil.toMap(
					notificationRecipient.getNotificationRecipientSettings());

			Assert.assertEquals(
				user2.getEmailAddress() + ",bcc@liferay.com",
				notificationRecipientSettingsMap.get("bcc"));
			Assert.assertEquals(
				user2.getEmailAddress() + ",cc@liferay.com",
				notificationRecipientSettingsMap.get("cc"));
			Assert.assertEquals(
				user2.getEmailAddress(),
				notificationRecipientSettingsMap.get("from"));
			Assert.assertEquals(
				user2.getFirstName(),
				notificationRecipientSettingsMap.get("fromName"));
			Assert.assertEquals(
				expectedSingleRecipient,
				notificationRecipientSettingsMap.get("singleRecipient"));

			String[] actualEmailAddresses = StringUtil.split(
				String.valueOf(notificationRecipientSettingsMap.get("to")));

			String[] expectedEmailAddresses = StringUtil.split(
				expectedToRecipient);

			Arrays.sort(actualEmailAddresses);

			Arrays.sort(expectedEmailAddresses);

			Assert.assertArrayEquals(
				expectedEmailAddresses, actualEmailAddresses);

			notificationQueueEntryLocalService.deleteNotificationQueueEntry(
				notificationQueueEntry);
		}
	}

}