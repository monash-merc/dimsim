/**
*
* Copyright (C) 2007-2008  eResearch Centre, James Cook University
*(eresearch.jcu.edu.au)
* This program was developed as part of the ARCHER project
* (Australian Research Enabling Environment) funded by a   
* Systemic Infrastructure Initiative (SII) grant and supported by the Australian
* Department of Innovation, Industry, Science and Research
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public  License as published by the
* Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.instrumentmiddleware.cima.core.impl;

import org.instrumentmiddleware.cima.core.ICIMAClientUtil;
import org.instrumentmiddleware.cima.core.ProcessException;
import org.instrumentmiddleware.cima.parcel.DescriptionResponseType;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.PluginStatusTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseBodyType;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.SessionType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum.Enum;
import org.instrumentmiddleware.cima.plugin.ICIMAPlugin;
import org.instrumentmiddleware.cima.plugin.manager.IPluginManager;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.session.ISessionManager;
import org.instrumentmiddleware.cima.util.CIMAUtil;

/**
 * @author Rafi M Feroze
 *
 * Controller to handle validate Session IDs using Ping Request.
 * Parcel requests without session IDs are sent to CIMA DefaultController
 * The first session id is validated and the rest are ignored.    
 * Hence it is necessary for clients to validate each session individually
 */
public class DimsimPingController extends DefaultController {

	public DimsimPingController(IPluginManager pluginManager,
			ISessionManager sessionManager) throws Exception {
		super(pluginManager, sessionManager);
	}

	public ResponseBodyType handleParcel(Parcel request,
		    ResponseBodyType responseBody)
		        throws ProcessException {
			Enum parcelType = request.getType();

			switch (parcelType.intValue()) {
				case ParcelTypeEnum.INT_PING:
					return handlePingSession(request, responseBody);

				default:
					return super.handleParcel(request,responseBody);
			}
		}

	private ResponseBodyType handlePingSession(Parcel request,
			ResponseBodyType responseBody) throws ProcessException {
		
		String sessionID = getFirstSessionID(request);
		if (sessionID == null) {
			return super.handleParcel(request, responseBody);
		}
		
		DescriptionResponseType response = (DescriptionResponseType) responseBody.addNewResponse()
		.changeType(DescriptionResponseType.type);
		response.setStatus(ResponseStatusEnum.FAILURE);
		DescriptionResponseType.Plugins plugins = response.addNewPlugins();

		
		ICIMAClientUtil util;
		try {
			util = CIMAUtil.getDirector().getClientUtil();
		} catch (Exception e) {
			e.printStackTrace();
			response.setMessage("Fatal Error : Unable to fetch Cima Director");
			return responseBody;
		}
		ISessionManager sessionManager = util.getSessionManager();

		ISession pingSession = null;
		ICIMAPlugin pingPlugin = null;
		pingSession = sessionManager.getSession(sessionID);
		if (pingSession != null) {
			pingPlugin = pingSession.getPlugin();
			response.setStatus(ResponseStatusEnum.SUCCESS);
			DescriptionResponseType.Plugins.Plugin plugin = plugins.addNewPlugin();
			plugin.setName(pingPlugin.getId());
			plugin.setStatus(PluginStatusTypeEnum.RUNNING);
		} else {
			response.setMessage("Session ID " + sessionID + " not valid");
		}
	
		return responseBody;
		
	}
	
	private String getFirstSessionID(Parcel request) {
		String sessionID = null;
		
		if (request.getSessions() != null) {
			SessionType[] sessions = request.getSessions().getSessionArray();

			if (sessions.length != 0) {
				sessionID = sessions[0].getSessionId();
			}
		}
		
		return sessionID;
	}
	
}
