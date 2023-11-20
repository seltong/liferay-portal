/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.dynamic.data.mapping.util.comparator;

import com.liferay.dynamic.data.mapping.model.DDMFormInstanceRecord;
import com.liferay.portal.kernel.util.DateUtil;
import com.liferay.portal.kernel.util.OrderByComparator;

/**
 * @author Selton Guedes
 */
public class DDMFormInstanceRecordCreateDateComparator
	extends OrderByComparator<DDMFormInstanceRecord> {

	public static final String ORDER_BY_ASC =
		"DDMFormInstanceRecord.createDate ASC";

	public static final String ORDER_BY_DESC =
		"DDMFormInstanceRecord.createDate DESC";

	public static final String[] ORDER_BY_FIELDS = {"createDate"};

	public DDMFormInstanceRecordCreateDateComparator() {
		this(false);
	}

	public DDMFormInstanceRecordCreateDateComparator(boolean ascending) {
		_ascending = ascending;
	}

	@Override
	public int compare(
		DDMFormInstanceRecord ddmFormInstanceRecord1,
		DDMFormInstanceRecord ddmFormInstanceRecord2) {

		int value = DateUtil.compareTo(
			ddmFormInstanceRecord1.getCreateDate(),
			ddmFormInstanceRecord2.getCreateDate());

		if (_ascending) {
			return value;
		}

		return -value;
	}

	@Override
	public String getOrderBy() {
		if (_ascending) {
			return ORDER_BY_ASC;
		}

		return ORDER_BY_DESC;
	}

	@Override
	public String[] getOrderByFields() {
		return ORDER_BY_FIELDS;
	}

	@Override
	public boolean isAscending() {
		return _ascending;
	}

	private final boolean _ascending;

}