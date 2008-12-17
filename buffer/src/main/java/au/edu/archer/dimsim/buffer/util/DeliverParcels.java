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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.IPlugin;
import org.instrumentmiddleware.cima.plugin.producer.IProducer;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.util.ParcelUtil;

import au.edu.archer.dimsim.security.IEndPointSecurity;
import au.edu.archer.dimsim.security.impl.EndpointSubscriptionSecurityModel;




/**
 * @author mrafi
 * Used by Plugin that handles commands sent by consumers to the buffer.
 */
@SuppressWarnings("hiding")
public class DeliverParcels<T extends Parcel> implements Runnable {
	private static Logger log = Logger.getLogger(DeliverParcels.class);
	ArrayList<T> parcelsToDeliver;
	String pluginId;
	IProducer producer;
	ISession redeliverSession; 
	IEndPointSecurity endPointSecurity;
	
	/**
	 * @param args
	 */
	
	private DeliverParcels(IProducer producer, 
				String pluginId,		
				ISession redeliverSession) {	
		this.pluginId = pluginId;
		this.producer = producer;
		this.redeliverSession = redeliverSession;		
	}
	
	public DeliverParcels (IProducer producer, 
				String pluginId, 
				ArrayList<T> parcelsToDeliver,
				ISession redeliverSession) {		
		this(producer,pluginId,redeliverSession);
		this.parcelsToDeliver = parcelsToDeliver;
	}

	public DeliverParcels (IProducer producer, 
			String pluginId, 
			T parcelToDeliver,
			ISession redeliverSession) {		
		this(producer,pluginId,redeliverSession);
		this.parcelsToDeliver = new ArrayList<T> ();
		this.parcelsToDeliver.add(parcelToDeliver);
	}

	public void run() {
		log.debug("inside Run");
		if	((parcelsToDeliver != null)
			&&  (producer != null))  {
			reDeliver();
		}		
	}
	
	public void setBufferSecurity(IEndPointSecurity endPointSecurity) {
		this.endPointSecurity = endPointSecurity;
	}
	
	private void reDeliver() {		
		log.debug("Try to deliver");
		Parcel p;		
		
		try {					
			for (int i=0; i < parcelsToDeliver.size(); i++) {
				
				p = parcelsToDeliver.get(i);				
				
				if (p instanceof IRegisteredParcel ) {
					IRegisteredParcel regP = (IRegisteredParcel) p;
					
					if (endPointSecurity != null) {
						if (!subscriberEndPointMatch(redeliverSession,regP.getRegisteredSessions())) {
							log.debug("Session does not previous consumer Endpoint, checiking against current subscriptions to plugin id: " + this.pluginId);
							//check if the consumer/endpoint has a current subscription with Source Plug-in
							if (!validateAgainstCurrentSubscriptions()) {
								log.debug("Session does not match consumer Endpoint, Parcel not sent, seq_id " + regP.getSequenceId());
								continue;  //do not send this parcel, check next;
							}
						}
					}
				}				
				
				Parcel parcel = ParcelUtil.newParcel(p.getType());									        
				parcel.setCreationTime(p.getCreationTime());
				//parcel.setSecurity(p.getSecurity());  //SecurtiyType is currently AbstractType and cannot be used. 
				parcel.setVersion(p.getVersion());
				parcel.setSequenceId(p.getSequenceId());
				parcel.setAsyn(p.getAsyn());
				
				log.debug("Resend Parcel created@ " + p.getCreationTime().getTimeInMillis());
				
				//The following is a mess, for now assume resend is for a new session always
				/*if (redeliverSession != null) {
					//Note: Original sender info is lost after this stage, 
					//but this ensures that the parcel is sent to the original recipient		
					parcel.addNewSessions().addNewSession().setSessionId(redeliverSession.getId());
				} else {
					Sessions prevSessions = p.getSessions();
					if (prevSessions != null) {
						parcel.setSessions(prevSessions);
					} else {
					//If this is executed, the parcel will be sent to all Sessions registered with the Producer.
					parcel.addNewSender().setId(this.pluginId);
					}
				}*/
				
				parcel.addNewSessions().addNewSession().setSessionId(redeliverSession.getId());
				
				BodyType b = p.getBody();
				if (b != null) {
					log.debug("releasing parcel");
					parcel.setBody(b);					
					producer.releaseParcel(parcel);					
				}				
			}	
		} catch (Exception e1) {			
			log.info(e1.toString());
			e1.printStackTrace();
		}	
		
	}

	private boolean validateAgainstCurrentSubscriptions() {
		List<ISession> currSessions = null;
		if (this.pluginId != null) {
			try {
				IPlugin plugin = DimsimUtils.getPluginManager().getPlugin(pluginId);
				currSessions = DimsimUtils.getSessionManager().getSessions(plugin);			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return subscriberEndPointMatch(redeliverSession, currSessions);
	}

	/*
	 * Return true if the current session's <customerID,endpointURL> tuple matches any input sessions.
	 */
	public static boolean subscriberEndPointMatch(ISession validateSession, List<ISession> sessions) {	
		Set<String> currEndPointTuple = 
			EndpointSubscriptionSecurityModel.getConsumerEndPoint(validateSession);
		
		log.debug("Validating consumerEndPoint " + toString(currEndPointTuple));
		
		if (sessions != null) {
			for (ISession r : sessions) {				
				log.debug("checking against tuple " + toString(EndpointSubscriptionSecurityModel.getConsumerEndPoint(r)));
				if (EndpointSubscriptionSecurityModel.getConsumerEndPoint(r).equals(currEndPointTuple)) {
					return true;
				}				 
			}
		}	
		
		return false;
	}

	private static String toString(Set<String> inSet) {
		String retStr = "";
		if (inSet != null) {
			for(String s:inSet) {
				retStr += s + "  ";
			}
		}
		return retStr;
	}

}
