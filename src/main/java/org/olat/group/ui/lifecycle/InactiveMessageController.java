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
package org.olat.group.ui.lifecycle;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InactiveMessageController extends BasicController {
	
	private Link reactivateLink;
	private final VelocityContainer mainVC;
	
	private BusinessGroup businessGroup;
	
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	
	public InactiveMessageController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup, boolean groupAdmin) {
		super(ureq, wControl);
		this.businessGroup = businessGroup;
		
		mainVC = createVelocityContainer("inactive_warn");
		
		Date softDeleteDate = businessGroupLifecycleManager.getSoftDeleteDate(businessGroup);
		long days = DateUtils.countDays(ureq.getRequestTimestamp(), softDeleteDate);
		String i18nKey = days <= 1 ? "warning.readonly.group.singular" : "warning.readonly.group";
		String message = translate(i18nKey, new String[] { Long.toString(days)} );
		mainVC.contextPut("message", StringHelper.escapeHtml(message));
		
		if(groupAdmin) {
			reactivateLink = LinkFactory.createCustomLink("reactivate.group", "reactivate", "reactivate.group", Link.BUTTON_SMALL, mainVC, this);
		}
		
		putInitialPanel(mainVC);
	}
	
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == reactivateLink) {
			doReactivate(ureq);
		}
	}
	
	private void doReactivate(UserRequest ureq) {
		businessGroup = businessGroupLifecycleManager.reactivateBusinessGroup(businessGroup, getIdentity());
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
