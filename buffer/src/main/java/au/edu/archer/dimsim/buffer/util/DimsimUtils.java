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
package au.edu.archer.dimsim.buffer.util;

import java.util.Collection;

import org.instrumentmiddleware.cima.core.ICIMAClientUtil;
import org.instrumentmiddleware.cima.parcel.EntityType;
import org.instrumentmiddleware.cima.plugin.manager.IPluginManager;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.session.ISessionManager;
import org.instrumentmiddleware.cima.util.CIMAUtil;

public class DimsimUtils {

	@SuppressWarnings("unchecked")
	public static String classToString(Class[] cArr) {
		String retVal = "";
		if (cArr != null) {
			for(Class c : cArr) {
				retVal += "\t" + c.getName() + "\n";  
			}
		}
		return retVal;
	}


	@SuppressWarnings("unchecked")
	public static String SetToString(Collection<Class> values) {
		String retStr = "{";
		
		if (values != null) {
			for(Class c:values) {
				if (c != null) {
					retStr += c.getName();
				}
			}
		}
		
		return retStr + "}";
	}
	
	public static ISessionManager getSessionManager() throws Exception {
		ICIMAClientUtil clientUtil = CIMAUtil.getDirector().getClientUtil();
		ISessionManager sessionManager = clientUtil.getSessionManager();
		return sessionManager;
	}
	
	public static IPluginManager getPluginManager() throws Exception {
		ICIMAClientUtil clientUtil = CIMAUtil.getDirector().getClientUtil();
		IPluginManager pluginManager = clientUtil.getPluginManager();
		return pluginManager;
	}
	
	
	public static String getConsumerID(ISession session) {
		if (session != null)  {			
			EntityType subscriber = session.getSubscriber();			
			if (subscriber != null) {			
				return subscriber.getId();
			}
			return "SessionID:" + session.getId();
		}
		return null;
	}
}
