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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFiltersComponent extends FormBaseComponentImpl implements ComponentCollection {
	
	private static final FlexiFiltersComponentRenderer RENDERER = new FlexiFiltersComponentRenderer();

	private final FlexiFiltersElementImpl element;
	
	public FlexiFiltersComponent(FlexiFiltersElementImpl element, Translator translator) {
		super(element.getName(), translator);
		this.element = element;
	}
	
	public FlexiFiltersElementImpl getFlexiFiltersElement() {
		return element;
	}
	
	@Override
	protected void fireEvent(UserRequest ureq, Event event) {
		super.fireEvent(ureq, event);
	}

	@Override
	public Component getComponent(String name) {
		FormItem item = element.getFormComponent(name);
		return item == null ? null : item.getComponent();
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmpList = new ArrayList<>();
		for(FormItem item:element.getFormItems()) {
			cmpList.add(item.getComponent());
		}
		return cmpList;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
