/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.additionalconditions.AdditionalCondition;
import org.olat.course.condition.additionalconditions.AdditionalConditionManager;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * Initial Date: May 28, 2004
 * 
 * @author gnaegi<br>
 *         Comment: Use this abstract course node if you implement a node that
 *         has only one accessability condition: access the node. See the
 *         CPCourse node for an example implementation.
 */
public abstract class AbstractAccessableCourseNode extends GenericCourseNode {

	private static final long serialVersionUID = 8769187818935593237L;

	private Condition preConditionAccess;

	public static final String BLOCKED_BY_ORIGINAL_ACCESS_RULES = "blockedByOriginalAccessRules";

	/**
	 * Constructor, only used by implementing course nodes
	 * 
	 * @param type The course node type
	 */
	protected AbstractAccessableCourseNode(String type) {
		super(type);
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		course.getCourseEnvironment()
				.getAssessmentManager().deleteIndividualAssessmentDocuments(this);
	}

	/**
	 * Returns the generic access precondition
	 * 
	 * @return Condition
	 */
	@Override
	public Condition getPreConditionAccess() {
		if (preConditionAccess == null) {
			preConditionAccess = new Condition();
		}
		preConditionAccess.setConditionId("accessability");
		return preConditionAccess;
	}

	/**
	 * Sets the generic access precondition.
	 * 
	 * @param precondition_accessor The precondition_accessor to set
	 */
	public void setPreConditionAccess(Condition precondition_accessor) {
		if (precondition_accessor == null) {
			precondition_accessor = getPreConditionAccess();
		}
		precondition_accessor.setConditionId("accessability");
		this.preConditionAccess = precondition_accessor;
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionAccess, envMapper);
	}
	
	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionAccess, envMapper, backwardsCompatible);
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCrourse, context);
		
		if (context != null) {
			ModuleConfiguration config = getModuleConfiguration();
			checkAndUpdateDate(IQEditController.CONFIG_KEY_RESULTS_START_DATE, config, context);
			checkAndUpdateDate(IQEditController.CONFIG_KEY_RESULTS_END_DATE, config, context);
			checkAndUpdateDate(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE, config, context);
			checkAndUpdateDate(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE, config, context);
			checkAndUpdateDate(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE, config, context);
			checkAndUpdateDate(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE, config, context);
			
			checkAndUpdateDate(IQEditController.CONFIG_KEY_START_TEST_DATE, config, context);
			checkAndUpdateDate(IQEditController.CONFIG_KEY_END_TEST_DATE, config, context);
		}
	}
	
	private void checkAndUpdateDate(String configKey, ModuleConfiguration config, CopyCourseContext context) {
		Date dateToCheck = config.getDateValue(configKey);
		
		if (dateToCheck != null) {
			dateToCheck.setTime(dateToCheck.getTime() + context.getDateDifference(getIdent()));
			config.setDateValue(configKey, dateToCheck);
		}
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		// </OLATCE-91>
		// for this node: only one role: accessing the node
		boolean accessible = getPreConditionAccess().getConditionExpression() == null || ci.evaluateCondition(getPreConditionAccess());
		// <OLATCE-91>
		if(accessible){
			Long courseId = ci.getUserCourseEnvironment().getCourseEnvironment().getCourseResourceableId();
			IdentityEnvironment identityEnv = ci.getUserCourseEnvironment().getIdentityEnvironment();
			nodeEval.putAccessStatus(BLOCKED_BY_ORIGINAL_ACCESS_RULES, false);
			AdditionalConditionManager addMan = new AdditionalConditionManager(this, courseId, identityEnv);
			accessible = addMan.evaluateConditions();
		}
		// </OLATCE-91>
		nodeEval.putAccessStatus("access", accessible);
		boolean visible = getPreConditionVisibility().getConditionExpression() == null
				|| ci.evaluateCondition(getPreConditionVisibility());
		nodeEval.setVisible(visible);
	}

	@Override
	public void copyConfigurationTo(CourseNode courseNode, ICourse course, Identity savedBy) {
		super.copyConfigurationTo(courseNode, course, savedBy);
		if(courseNode instanceof AbstractAccessableCourseNode) {
			AbstractAccessableCourseNode accessableNode = (AbstractAccessableCourseNode)courseNode;
			if(preConditionAccess != null) {
				accessableNode.setPreConditionAccess(preConditionAccess.clone());
			}
		}
	}

	@Override
	public List<ConditionExpression> getConditionExpressions() {
		ArrayList<ConditionExpression> retVal;
		List<ConditionExpression> parentsConditions = super.getConditionExpressions();
		if (parentsConditions.size() > 0) {
			retVal = new ArrayList<>(parentsConditions);
		} else {
			retVal = new ArrayList<>();
		}
		//
		String coS = getPreConditionAccess().getConditionExpression();
		if (coS != null && !coS.equals("")) {
			// an active condition is defined
			ConditionExpression ce = new ConditionExpression(getPreConditionAccess().getConditionId());
			ce.setExpressionString(getPreConditionAccess().getConditionExpression());
			retVal.add(ce);
		}
		//
		return retVal;
	}

	public List<AdditionalCondition> getAdditionalConditions(){
		if(additionalConditions==null)
			additionalConditions = new ArrayList<>();
		return additionalConditions;
	}
	
	/**
	 * Defines whether the course node has still a custom access condition
	 * controller or if the standard controller is used.
	 * 
	 * As of today is the goal to eliminate all custom condition controllers. If
	 * this goal is achieved, this method should be deleted and the code from
	 * {@link org.olat.course.editor.NodeEditController}.
	 * 
	 * @return
	 */
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.custom();
	}
	
	@Override
	public List<Map.Entry<String, Date>> getNodeSpecificDatesWithLabel() {
		List<Map.Entry<String, Date>> datesMap = new ArrayList<>();
		ModuleConfiguration config = getModuleConfiguration();
		
		Date resultsStartDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_START_DATE);
		Date resultsEndDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_END_DATE);
		
		Date resultsFailedStartDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_START_DATE);
		Date resultsFailedEndDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_FAILED_END_DATE);
		
		Date resultsPassedStartDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_START_DATE);
		Date resultsPassedEndDate = config.getDateValue(IQEditController.CONFIG_KEY_RESULTS_PASSED_END_DATE);
		
		Date testStartDate = config.getDateValue(IQEditController.CONFIG_KEY_START_TEST_DATE);
		Date testEndDate = config.getDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE);
		
		if (testStartDate != null) {
			datesMap.add(Map.entry("test.start", testStartDate));
		}
		if (testEndDate != null) {
			datesMap.add(Map.entry("test.end", testEndDate));
		}
		
		if (resultsStartDate != null) {
			datesMap.add(Map.entry("test.results.start", resultsStartDate));
		}
		if (resultsEndDate != null) {
			datesMap.add(Map.entry("test.results.end", resultsEndDate));
		}
		
		if (resultsPassedStartDate != null) {
			datesMap.add(Map.entry("test.results.passed.start", resultsPassedStartDate));
		}
		if (resultsPassedEndDate != null) {
			datesMap.add(Map.entry("test.results.passed.end", resultsPassedEndDate));
		}
		
		if (resultsFailedStartDate != null) {
			datesMap.add(Map.entry("test.results.failed.start", resultsFailedStartDate));
		}
		if (resultsFailedEndDate != null) {
			datesMap.add(Map.entry("test.results.failed.end", resultsFailedEndDate));
		}
		
		return datesMap;
	}
	
}