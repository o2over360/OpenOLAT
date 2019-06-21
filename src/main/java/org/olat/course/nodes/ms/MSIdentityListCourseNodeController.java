/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.ms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanNullCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CSSIconFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.bulk.BulkAssessmentToolController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MSIdentityListCourseNodeController extends IdentityListCourseNodeController {
	
	private Boolean hasEvaluationForm;
	
	@Autowired
	private MSService msService;

	public MSIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, MSCourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, stackPanel, courseEntry, group, courseNode, coachCourseEnv, toolContainer, assessmentCallback);

		flc.contextPut("showTitle", Boolean.valueOf(showTitle));
		flc.setDirty(true);
	}

	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		if(!coachCourseEnv.isCourseReadOnly()) {
			BulkAssessmentToolController bulkAssessmentToolCtrl = new BulkAssessmentToolController(ureq, getWindowControl(),
					coachCourseEnv.getCourseEnvironment(), (MSCourseNode)courseNode);
			listenTo(bulkAssessmentToolCtrl);
			formLayout.put("bulk.assessment", bulkAssessmentToolCtrl.getInitialComponent());
		}
		super.initMultiSelectionTools(ureq, formLayout);
	}
	
	@Override
	protected void initScoreColumns(FlexiTableColumnModel columnsModel) {
		if (hasEvaluationForm()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.details.ms",
					IdentityCourseElementCols.details.ordinal(),
					new BooleanNullCellRenderer(
							new CSSIconFlexiCellRenderer("o_icon_lg o_icon_ms_done"),
							new CSSIconFlexiCellRenderer("o_icon_lg o_icon_ms_pending"),
							null)));
		}
		super.initScoreColumns(columnsModel);
	}
	
	@Override
	protected void loadModel(UserRequest ureq) {
		super.loadModel(ureq);
		
		if (hasEvaluationForm()) {
			List<EvaluationFormSession> sessions = msService.getSessions(getCourseRepositoryEntry(), courseNode.getIdent());
			Map<String, EvaluationFormSession> identToSesssion = sessions.stream()
					.collect(Collectors.toMap(
							s -> s.getSurvey().getIdentifier().getSubident2(),
							Function.identity()));
			
			for (AssessedIdentityElementRow row : usersTableModel.getObjects()) {
				String ident = row.getIdentityKey().toString();
				EvaluationFormSession session = identToSesssion.get(ident);
				Boolean sessionDone = session != null
						? EvaluationFormSessionStatus.done.equals(session.getEvaluationFormSessionStatus())
						: null;
				row.setDetails(sessionDone);
			}
		}
	}

	private boolean hasEvaluationForm() {
		if (hasEvaluationForm == null) {
			ModuleConfiguration config = courseNode.getModuleConfiguration();
			String scoreConfig = config.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
			hasEvaluationForm = MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreConfig)
					|| MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreConfig);
		}
		return hasEvaluationForm.booleanValue();
	}
}
