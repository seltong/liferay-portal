/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.workflow.metrics.model;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.workflow.metrics.sla.processor.WorkflowMetricsSLAStatus;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.animation.Transition;

/**
 * @author Selton Guedes
 */
public class AddInstanceRequest {

	public Map<Locale, String> getAssetTitleMap() {
		return _assetTitleMap;
	}

	public Map<Locale, String> getAssetTypeMap() {
		return _assetTypeMap;
	}

	public List<Assignment> getAssignments() {
		return _assignments;
	}

	public String getClassName() {
		return _className;
	}

	public long getClassPK() {
		return _classPK;
	}

	public Date getCompletionDate() {
		return _completionDate;
	}

	public Date getCreateDate() {
		return _createDate;
	}

	public Integer getDuration() {
		return _duration;
	}

	public long getInstanceId() {
		return _instanceId;
	}

	public Date getModifiedDate() {
		return _modifiedDate;
	}

	public long getProcessId() {
		return _processId;
	}

	public String getProcessVersion() {
		return _processVersion;
	}

	public List<Object> getSlaResults() {
		return _slaResults;
	}

	public WorkflowMetricsSLAStatus getSlaStatus() {
		return _slaStatus;
	}

	public List<String> getTaskNames() {
		return _taskNames;
	}

	public List<Transition> getTransisions() {
		return _transitions;
	}

	public boolean isCompleted() {
		return _completed;
	}

	public static class Builder {

		public AddInstanceRequest build() {
			return _addInstanceRequest;
		}

		public Builder setAssetTitleMap(Map<Locale, String> assetTitleMap) {
			_addInstanceRequest._assetTitleMap = assetTitleMap;

			return this;
		}

		public Builder setAssetTypeMap(Map<Locale, String> assetTitleMap) {
			_addInstanceRequest._assetTypeMap = assetTitleMap;

			return this;
		}

		public Builder setAssignments(List<Assignment> assignments) {
			_addInstanceRequest._assignments = assignments;

			return this;
		}

		public Builder setAssignments(
			UnsafeSupplier<List<Assignment>, Exception>
				assignmentsUnsafeSupplier) {

			try {
				_addInstanceRequest._assignments =
					assignmentsUnsafeSupplier.get();
			}
			catch (Exception exception) {
				if (_log.isDebugEnabled()) {
					_log.debug(exception, exception);
				}
			}

			return this;
		}

		public Builder setClassName(String className) {
			_addInstanceRequest._className = className;

			return this;
		}

		public Builder setClassPK(long classPK) {
			_addInstanceRequest._classPK = classPK;

			return this;
		}

		public Builder setCompleted(boolean completed) {
			_addInstanceRequest._completed = completed;

			return this;
		}

		public Builder setCompletionDate(Date completionDate) {
			_addInstanceRequest._completionDate = completionDate;

			return this;
		}

		public Builder setCreateDate(Date createDate) {
			_addInstanceRequest._createDate = createDate;

			return this;
		}

		public Builder setDuration(Integer duration) {
			_addInstanceRequest._duration = duration;

			return this;
		}

		public Builder setInstanceId(long instanceId) {
			_addInstanceRequest._instanceId = instanceId;

			return this;
		}

		public Builder setModifiedDate(Date modifiedDate) {
			_addInstanceRequest._modifiedDate = modifiedDate;

			return this;
		}

		public Builder setProcessId(long processId) {
			_addInstanceRequest._processId = processId;

			return this;
		}

		public Builder setProcessVersion(String processVersion) {
			_addInstanceRequest._processVersion = processVersion;

			return this;
		}

		public Builder setSLAStatus(WorkflowMetricsSLAStatus slaStatus) {
			_addInstanceRequest._slaStatus = slaStatus;

			return this;
		}

		public Builder setTaskName(List<String> taskNames) {
			_addInstanceRequest._taskNames = taskNames;

			return this;
		}

		public Builder setTransitions(List<Transition> transitions) {
			_addInstanceRequest._transitions = transitions;

			return this;
		}

		private final AddInstanceRequest _addInstanceRequest =
			new AddInstanceRequest();

	}

	private static final Log _log = LogFactoryUtil.getLog(
		AddInstanceRequest.class);

	private Map<Locale, String> _assetTitleMap;
	private Map<Locale, String> _assetTypeMap;
	private List<Assignment> _assignments;
	private String _className;
	private long _classPK;
	private boolean _completed;
	private Date _completionDate;
	private Date _createDate;
	private Integer _duration;
	private long _instanceId;
	private Date _modifiedDate;
	private long _processId;
	private String _processVersion;
	private WorkflowMetricsSLAStatus _slaStatus;
	private List<String> _taskNames;
	private List<Transition> _transitions;

}