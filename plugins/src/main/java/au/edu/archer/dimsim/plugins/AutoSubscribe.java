/**
*
* Copyright (C) 2007-2008, Monash University
*
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
package au.edu.archer.dimsim.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.ICIMAClientUtil;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EndpointTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseBodyType;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.ResponseType;
import org.instrumentmiddleware.cima.parcel.SessionType;
import org.instrumentmiddleware.cima.parcel.SubscriptionResponseType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel.Sessions;
import org.instrumentmiddleware.cima.plugin.ICIMAPlugin;
import org.instrumentmiddleware.cima.plugin.manager.IPluginManager;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.instrumentmiddleware.cima.util.SubscribeInfo;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
/**
 * @author Rafi M Feroze
 *
 */
public class AutoSubscribe implements Controller{
	Map<String,Set<String>> subscribeMap;
	private static Logger log = Logger.getLogger(AutoSubscribe.class);
	
	String remoteEndpointURL = "http://localhost:8080/ws/cima";
	Map<String, Map<String,String>> failures = new HashMap<String,Map<String,String>>();
	Map<String, Map<String,String>> sessions = 	new HashMap<String, Map<String,String>> ();
	
	
	public AutoSubscribe(Map<String,Set<String>> subscribeMap) {
		this.subscribeMap = subscribeMap;
		if (this.subscribeMap != null) {
			for(String k:this.subscribeMap.keySet()) {
				Map<String,String> rMap = new HashMap<String,String> ();
				failures.put(k, rMap);
				
				Map<String,String> sMap = new HashMap<String,String> ();
				sessions.put(k, sMap);
				
				Set<String> rSet = this.subscribeMap.get(k);
				if (rSet != null) {
					for(String r:rSet) {
						rMap.put(r, "Waiting for initial Subscription ");
					}
				}
			}
		}
	}

	public void setRemoteEndpointURL(String url) {
		this.remoteEndpointURL = url;
	}
	
	public void checkAndSubscribe() throws Exception {
		log.debug(System.currentTimeMillis() + " ms: in checkAndSubscribe()");
		if (this.subscribeMap == null)	return;
		
		ICIMAClientUtil util = CIMAUtil.getDirector().getClientUtil();

		IPluginManager pluginManager = util.getPluginManager();
		//ICIMAPlugin plugin = pluginManager.getPlugin(k);
		
		EndpointType endpoint = getRemoteEndPoint();

		for (String k :this.subscribeMap.keySet()) {
			ICIMAPlugin plugin = pluginManager.getPlugin(k);
			
			Set<String> remotePlugins = this.subscribeMap.get(k);
			Map<String,String> failureMap = this.failures.get(k);
			Map<String,String> sessionMap = this.sessions.get(k);
			
			if (remotePlugins != null) {
				for (String r : remotePlugins) {
					if (!checkSubscribed(sessionMap,plugin,r)) {
						SubscribeInfo info = new SubscribeInfo(endpoint, r);
						
						log.debug(k + " invoking Subscribe to " + r + "@" + this.remoteEndpointURL);
						SubscriptionResponseType response = plugin.doSubscribe(info);
						log.debug(response.getMessage());
						
						if (response.getStatus() == ResponseStatusEnum.SUCCESS) {
							if (failureMap.containsKey(r)) {
								failureMap.remove(r);
							}
							sessionMap.put(r, response.getNewSessionId());
						}
						else
						{
							failureMap.put(r,response.getMessage());
							if (sessionMap.containsKey(r)) {
								sessionMap.remove(r);
							}
						}	
					}
				}
			} else {
				log.debug("No Remote plugins provided for " + k);
			}
		}
	}

	private EndpointType getRemoteEndPoint() {
		EndpointType endpoint = EndpointType.Factory.newInstance();
		endpoint.setUrl(this.remoteEndpointURL);
		endpoint.setType(EndpointTypeEnum.SOAP);
		return endpoint;
	}

	/**
	 * send a dummy message to the remote plugin to see if the subscription is
	 * still active. Return fasle if the message returns 'No subscription'
	 * @param plugin
	 * @param r
	 * @return
	 */
	private boolean checkSubscribed(Map<String,String>sessionMap,ICIMAPlugin plugin, String r) {
		if (!sessionMap.containsKey(r))  return false;
	
		Parcel parcel = ParcelUtil.newParcel(ParcelTypeEnum.PING);
		parcel.setSequenceId(1234);
		EndpointType endpoint = getRemoteEndPoint();
		
		Sessions ss = parcel.addNewSessions();
		SessionType session = ss.addNewSession();
		session.setSessionId(sessionMap.get(r));
		
		try {
			Parcel responseParcel = CIMAUtil.getDirector().handleOutgoingParcel(plugin,
				    parcel, endpoint);

			ResponseType[] responses = ((ResponseBodyType) responseParcel.getBody()
				.changeType(ResponseBodyType.type)).getResponseArray();

			for (ResponseType response : responses) {
				if (response.getStatus() == ResponseStatusEnum.SUCCESS) {
					return true;
				} else {
					log.debug("checkSubscribe Failed : " + response.getMessage());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("error unsubscribing: " + e);
		}
		
		return false; 
	}

	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
    	Map<String,Object> model = new HashMap<String,Object>();
    	
    	String message = "";
    	
    	for (String k:this.failures.keySet()) {
    		Map<String,String> f = this.failures.get(k);
    		for (String r : f.keySet()){
    			message +=  k + " , " + r + " : " + f.get(r) + '\n';
    		}
    	}
    	model.put("message",message);
    	return new ModelAndView("autoSubscribe","model",model);
	}
}
