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
package org.olat.modules.edusharing.manager;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.UriBuilder;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

import org.edu_sharing.webservices.authbyapp.AuthByApp;
import org.edu_sharing.webservices.authbyapp.AuthByAppService;
import org.edu_sharing.webservices.authbyapp.AuthenticationException;
import org.edu_sharing.webservices.authentication.AuthenticationResult;
import org.edu_sharing.webservices.types.KeyValue;
import org.edu_sharing.webservices.usage2.Usage2;
import org.edu_sharing.webservices.usage2.Usage2Exception_Exception;
import org.edu_sharing.webservices.usage2.Usage2Service;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.edusharing.CreateUsageParameter;
import org.olat.modules.edusharing.DeleteUsageParameter;
import org.olat.modules.edusharing.EdusharingModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class EdusharingSoapClient {

	private static final OLog log = Tracing.createLoggerFor(EdusharingSoapClient.class);
	
	@Autowired
	private EdusharingModule edusharingModule;

	String createTicket(Identity identity) throws AuthenticationException {
		return authenticate(identity).getTicket();
	}

	private AuthenticationResult authenticate(Identity identity) throws AuthenticationException {
		AuthByApp service = getAuthByAppService();
		List<KeyValue> ssoData = getSSOData(identity);
		AuthenticationResult result = service.authenticateByTrustedApp(edusharingModule.getAppId(), ssoData);
		if (log.isDebug()) {
			String logResult = new StringBuilder()
					.append("AuthenticationResult [")
					.append("username: ").append(result.getUsername())
					.append(", email: ").append(result.getEmail())
					.append(", ticket: ").append(result.getTicket())
					.append(", session: ").append(result.getSessionid())
					.append("]")
					.toString();
			log.debug("edu-sharing " + logResult);
		}
		return result;
	}
	
	/**
	 * Key are configured in edu-sharing configuration: edu-sharing-sso-context.xml
	 *
	 * @return
	 */
	private List<KeyValue> getSSOData(Identity identity) {
		List<KeyValue> ssoData = new ArrayList<>();
		
		// mandatory
		KeyValue userId = new KeyValue();
		userId.setKey(edusharingModule.getAuthKeyUseriId());
		userId.setValue(identity.getName());
		ssoData.add(userId);
		
		// optional
		User user = identity.getUser();
		KeyValue lastname = new KeyValue();
		lastname.setKey(edusharingModule.getAuthKeyLastname());
		lastname.setValue(user.getLastName());
		ssoData.add(lastname);
		
		KeyValue firstname = new KeyValue();
		firstname.setKey(edusharingModule.getAuthKeyFirstname());
		firstname.setValue(user.getFirstName());
		ssoData.add(firstname);

		KeyValue email = new KeyValue();
		email.setKey(edusharingModule.getAuthKeyEmail());
		email.setValue(user.getEmail());
		ssoData.add(email);

		KeyValue affiliationId = new KeyValue();
		affiliationId.setKey("affiliation");
		affiliationId.setValue(edusharingModule.getAuthAffiliationId());
		ssoData.add(affiliationId);

		KeyValue affiliationName = new KeyValue();
		affiliationName.setKey("affiliationname");
		affiliationName.setValue(edusharingModule.getAuthAffiliationName());
		ssoData.add(affiliationName);
		
		return ssoData;
	}
	
	boolean valdateTicket(String ticket) throws AuthenticationException {
		AuthByApp service = getAuthByAppService();
		return service.checkTicket(ticket);
	}
	
	void createUsage(CreateUsageParameter parameter) throws Usage2Exception_Exception {
		Usage2 service = getUsage2Service();
		service.setUsage(parameter.getEduRef(),
				parameter.getUser(),
				edusharingModule.getAppId(),
				parameter.getCourseId(),
				parameter.getUserMail(),
				parameter.getFromUsed(),
				parameter.getToUsed(),
				parameter.getDistinctPersons(),
				parameter.getVersion(),
				parameter.getResourceId(),
				parameter.getXmlParams());
	}

	void deleteUsage(DeleteUsageParameter parameter) throws Usage2Exception_Exception {
		Usage2 service = getUsage2Service();
		service.deleteUsage(
				parameter.getEduRef(),
				parameter.getUser(),
				edusharingModule.getAppId(),
				parameter.getCourseId(),
				parameter.getResourceId());
	}

	//Factories for service stubs
	private final AuthByApp getAuthByAppService() {
		AuthByAppService service = new AuthByAppService();
		service.setHandlerResolver(new EdusharingSecurityHandlerResolver());
		AuthByApp port = service.getAuthbyapp();
		String endPoint = getEdusharingEndPoint("authbyapp");
		((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
		return port;
	}
	
	private final Usage2 getUsage2Service() {
		Usage2Service service = new Usage2Service();
		service.setHandlerResolver(new EdusharingSecurityHandlerResolver());
		Usage2 port = service.getUsage2();
		String endPoint = getEdusharingEndPoint("usage2");
		((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPoint);
		return port;
	}
	
	private final String getEdusharingEndPoint(String service) {
		UriBuilder builder = UriBuilder.fromUri(edusharingModule.getBaseUrl());
		builder.path("services").path(service);
		return builder.build().toString();
	}
	
	private final class EdusharingSecurityHandlerResolver implements HandlerResolver {
		
		@SuppressWarnings("rawtypes")
		@Override
		public List<Handler> getHandlerChain(PortInfo portInfo) {
			List<Handler> handlerList = new ArrayList<>();
			handlerList.add(new EdusharingSecurityHandler());
			return handlerList;
		}
	}

}
