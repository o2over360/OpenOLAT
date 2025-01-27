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
package org.olat.repository.ui.author.copy.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.settings.RepositoryEntryLifecycleController;
import org.olat.repository.ui.settings.RepositoryEntryMetadataController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 07.06.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseGeneralStep extends BasicStep {

	private final CopyCourseContext context;
	private final CopyCourseSteps steps;
	
	public CopyCourseGeneralStep(UserRequest ureq, CopyCourseSteps steps, CopyCourseContext context) {
		super(ureq);
		
		this.steps = steps;
		this.context = context;
		
		setI18nTitleAndDescr("general.title", null);
		
		steps.setShowNodesOverview(context.hasDateDependantNodes() || context.hasNodeSpecificSettings());
		setNextStep(CopyCourseStepsStep.create(ureq, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		stepsRunContext.put(CopyCourseSteps.CONTEXT_KEY, steps);
		stepsRunContext.put(CopyCourseContext.CONTEXT_KEY, context);
		
		return new GeneralStepController(ureq, windowControl, form, stepsRunContext);
	}

	private class GeneralStepController extends StepFormBasicController {

		private static final String CUSTOM_MODE = "wizard.mode.custom";
		private static final String AUTOMATIC_MODE = "wizard.mode.automatic";
		
		private TextElement externalRefEl;
		private TextElement displayNameEl;
		
		private RepositoryEntryLifecycleController lifecycleController;
		private RepositoryEntryMetadataController metadataController;
		
		private SingleSelection copyModeEl;
		
		@Autowired
		private RepositoryService repositoryService;
		@Autowired
		private CopyCourseWizardModule wizardModule;
		
		public GeneralStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			setTranslator(Util.createPackageTranslator(CopyCourseStepsStep.class, getLocale(), getTranslator()));
			setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
			
			metadataController = new RepositoryEntryMetadataController(ureq, wControl, context.getSourceRepositoryEntry(), rootForm);
			listenTo(metadataController);
			
			lifecycleController = new RepositoryEntryLifecycleController(ureq, wControl, context.getSourceRepositoryEntry(), rootForm);
			listenTo(lifecycleController);
			
			initForm(ureq);
			
			checkCourseAvailability(ureq, displayNameEl);
			checkCourseAvailability(ureq, externalRefEl);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}
		
		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			boolean allOk = super.validateFormLogic(ureq);
			
			displayNameEl.clearError();
			if (!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
				displayNameEl.setErrorKey("input.mandatory", null);
			}
			
			return allOk;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			context.setDisplayName(displayNameEl.getValue());
			context.setExternalRef(externalRefEl.getValue());
			
			if (lifecycleController.saveToContext(ureq, context) && metadataController.saveToContext(ureq, context)) {
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FormItemContainer referenceAndTitleLayout = FormLayoutContainer.createDefaultFormLayout_2_10("refernceAndTitleLayout", getTranslator());
			referenceAndTitleLayout.setRootForm(mainForm);
			formLayout.add(referenceAndTitleLayout);
			
			// Course name
			displayNameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, context.getSourceRepositoryEntry().getDisplayname() + " " + translate("copy.suffix"), referenceAndTitleLayout);
			displayNameEl.setDisplaySize(30);
			displayNameEl.setMandatory(true);
			displayNameEl.addActionListener(FormEvent.ONCHANGE);
			
			// Course reference
			externalRefEl = uifactory.addTextElement("cif.externalref", "cif.externalref", 255, context.getSourceRepositoryEntry().getExternalRef(), referenceAndTitleLayout);
			externalRefEl.setHelpText(translate("cif.externalref.hover"));
			externalRefEl.setHelpUrlForManualPage("Set up info page");
			externalRefEl.addActionListener(FormEvent.ONCHANGE);
			
			// Spacer
			uifactory.addSpacerElement("space_1", formLayout, false);
			
			// Execution
			formLayout.add("lifeCycle", lifecycleController.getInitialFormItem());
			lifecycleController.loadFromContext(context);
			
			// Spacer
			uifactory.addSpacerElement("space_2", formLayout, false);
			
			// Metadata
			formLayout.add("metaData", metadataController.getInitialFormItem());
			metadataController.loadFromContext(context);
			
			// Spacer
			uifactory.addSpacerElement("space_3", formLayout, false);
			
			FormItemContainer copyModeLayout = FormLayoutContainer.createDefaultFormLayout_2_10("copyModeLayout", getTranslator());
			copyModeLayout.setRootForm(mainForm);
			formLayout.add(copyModeLayout);
			
			// Copy mode
			SelectionValue custom = new SelectionValue(CUSTOM_MODE, translate(CUSTOM_MODE), translate(CUSTOM_MODE + ".desc"));
			SelectionValue automatic = new SelectionValue(AUTOMATIC_MODE, translate(AUTOMATIC_MODE), translate(AUTOMATIC_MODE + ".desc"));
			SelectionValues modes = new SelectionValues(automatic, custom);
			
			copyModeEl = uifactory.addCardSingleSelectHorizontal("wizard.mode", copyModeLayout, modes.keys(), modes.values(), modes.descriptions(), null);
			copyModeEl.select(steps.isAdvancedMode() ? custom.getKey() : automatic.getKey(), true);
			copyModeEl.addActionListener(FormEvent.ONCHANGE);
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == copyModeEl) {
				if (copyModeEl.isKeySelected(AUTOMATIC_MODE)) {
					steps.setAdvancedMode(false);
					steps.loadFromWizardConfig(wizardModule);
					context.loadFromWizardConfig(wizardModule);
				} else if (copyModeEl.isKeySelected(CUSTOM_MODE)) {
					steps.setAdvancedMode(true);
				}
				
				setNextStep(CopyCourseStepsStep.create(ureq, steps));
				fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			} else if (source == displayNameEl) {
				checkCourseAvailability(ureq, displayNameEl);
				checkCourseAvailability(ureq, externalRefEl);
			} else if (source == externalRefEl) {
				checkCourseAvailability(ureq, externalRefEl);
				checkCourseAvailability(ureq, displayNameEl);
			}
		};
		
		private void checkCourseAvailability(UserRequest ureq, TextElement textElement) {
			if (StringHelper.containsNonWhitespace(textElement.getValue())) {
				SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
				params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
				params.setExactSearch(true);
				
				if (textElement == displayNameEl) {
					String displayName = textElement.getValue();
					if (displayName != null) {
						displayName = displayName.toLowerCase();
					}
					params.setDisplayname(displayName);
				} else if (textElement == externalRefEl) {
					String reference = textElement.getValue();
					if (reference != null) {
						reference = reference.toLowerCase();
					}
					params.setReference(reference);
				}
				
				textElement.clearError();
				if (repositoryService.countAuthorView(params) > 0) {
					String errorKey = "input.existing";
					
					if (textElement == displayNameEl) {
						errorKey += ".name";
					} else if (textElement == externalRefEl) {
						errorKey += ".reference";
					}
					textElement.setErrorKey(errorKey, true);
				}
			} else if (textElement.isMandatory()) {
				textElement.setErrorKey("input.mandatory", null);
			} else {
				textElement.clearError();
			}
		}
	}
 }
