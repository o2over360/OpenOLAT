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
* <p>
*/ 
package org.olat.core.gui.components.form.flexible.impl.components;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Initial Date: 06.12.2006 <br>
 * 
 * @author patrickb
 */
public class SimpleFormErrorText extends FormBaseComponentImpl {
	private static final ComponentRenderer ERROR_RENDERER = new DefaultComponentRenderer() {
		@Override
		public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
				RenderResult renderResult, String[] args) {
			SimpleFormErrorText stc = (SimpleFormErrorText) source;
			if(StringHelper.containsNonWhitespace(stc.getText())) {
			// error component only
				sb.append("<div class='o_error' id='o_c").append(source.getDispatchID()).append("'>")
				  .append(stc.getText())
				  .append("</div>");
			}
		}
	};
	
	private static final ComponentRenderer WARNING_RENDERER = new DefaultComponentRenderer() {
		@Override
		public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
				RenderResult renderResult, String[] args) {
			SimpleFormErrorText stc = (SimpleFormErrorText) source;
			if(StringHelper.containsNonWhitespace(stc.getText())) {
			// error component only
				sb.append("<div class='o_warning' id='o_c").append(source.getDispatchID()).append("'>")
				  .append(stc.getText())
				  .append("</div>");
			}
		}
	};

	private final String text;
	private final boolean isWarning;

	public SimpleFormErrorText(String name, String text, boolean isWarning) {
		super(name);
		this.text = text;
		this.setDomReplacementWrapperRequired(false);
		this.isWarning = isWarning;
		setDirty(true);
	}
	
	public SimpleFormErrorText(String name, String text) { 
		this(name, text, false);
	}
	
	public String getText() {
		return text;
	}

	/**
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return isWarning ? WARNING_RENDERER : ERROR_RENDERER;
	}
}