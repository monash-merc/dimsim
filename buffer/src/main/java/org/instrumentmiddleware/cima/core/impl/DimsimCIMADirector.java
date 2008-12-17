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


import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.IDescriptionController;
import org.instrumentmiddleware.cima.core.IPluginController;
import org.instrumentmiddleware.cima.core.ISubscriptionController;
import org.instrumentmiddleware.cima.core.ProcessException;
import org.instrumentmiddleware.cima.core.impl.CIMADirector;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EntityType;
import org.instrumentmiddleware.cima.parcel.IParcelHandler;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseBodyType;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.ResponseType;
import org.instrumentmiddleware.cima.parcel.SubscriptionRequestType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum.Enum;
import org.instrumentmiddleware.cima.plugin.manager.IPluginManager;
import org.instrumentmiddleware.cima.plugin.producer.IProducer;
import org.instrumentmiddleware.cima.session.ISessionManager;
import org.instrumentmiddleware.cima.transport.ITransportManager;
import org.instrumentmiddleware.cima.transport.delivery.IDeliveryStrategyFactory;

import au.edu.archer.dimsim.buffer.plugin.IBufferNot;
import au.edu.archer.dimsim.buffer.IDeliveryBuffer;
import au.edu.archer.dimsim.security.IEndPointSecurity;

/**
 * @author mrafi
 *
 */
public class DimsimCIMADirector extends CIMADirector {
	private static Logger log = Logger.getLogger(DimsimCIMADirector.class);
	
	boolean isBuffered = false;
	
	IDeliveryBuffer deliveryBuffer;	
	ISessionManager sessionManager;
	IEndPointSecurity endPointSecurity;
	private IParcelHandler pingHandler;
	
	protected DimsimCIMADirector(IPluginController pController,
		    ISubscriptionController rController,
		    IDescriptionController dController, IPluginManager pManager,
		    ISessionManager sManager, ITransportManager tManager,
		    IDeliveryStrategyFactory dsFactory,
		    IEndPointSecurity endPointSecurity)
		        throws Exception {
		super(pController,rController,dController,pManager,sManager,tManager,dsFactory);
		
		this.sessionManager = sManager;
		this.endPointSecurity = endPointSecurity;
		
	}
	
	/*
	@SuppressWarnings("unused")
	private void setEndPointSecurity(EndpointSubscriptionSecurityModel endPointSecurity) {
		this.endPointSecurity = endPointSecurity;
	}*/
	public void setPingHandler(IParcelHandler pingHandler) {
		this.pingHandler = pingHandler;
	}
	
	public void setDeliveryBuffer(IDeliveryBuffer deliveryBuffer) {
		this.deliveryBuffer = deliveryBuffer;
	}
	
	public IDeliveryBuffer getDeliveryBuffer() {
		return deliveryBuffer;
	}	
	
	
	public void setIsBuffered(boolean b) {
		this.isBuffered = b;
	}
	
	public boolean getIsBuffered() {
		return this.isBuffered;
	}	
	
	
	/*public Parcel handleOutgoingParcel(IPlugin plugin, Parcel parcel,
			EndpointType endpoint) throws ProcessException {		
	
		//Buffer this parcel if needed and if possible
		if (this.getDeliveryBuffer() != null) {
			IRegisteredParcel regParcel = null;
			if (parcel instanceof IRegisteredParcel) {
				regParcel = (IRegisteredParcel)parcel;
			} else if (plugin instanceof IProducer) {
				IProducer producer = (IProducer)plugin;
				regParcel = sessionManager.createRegisteredParcel(producer,parcel);
			}
				    
			if (regParcel != null) {
				deliveryBuffer.buffer(regParcel);
			}
		}
		
		return super.handleOutgoingParcel(plugin, parcel, endpoint);
	}*/
	
	public void handleOutgoingParcel(IProducer producer, Parcel parcel)
    throws ProcessException {
		
		//Buffer this parcel if needed and possible
		if  (!(producer instanceof IBufferNot)) {
			
			if ((this.getDeliveryBuffer() != null) 
				& (this.isBuffered)) {
			
				IRegisteredParcel regParcel = sessionManager.createRegisteredParcel(producer,parcel);
			
				deliveryBuffer.buffer(producer.getId(), regParcel);
				System.out.println("Buffered Parcel from " + producer.getId());
			}
		}
		super.handleOutgoingParcel(producer, parcel);
		
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.instrumentmiddleware.cima.core.impl.CIMADirector#handleIncomingParcel(org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel)
	 */	
	protected ResponseBodyType handleIncomingParcel(Parcel parcel,
		    ResponseBodyType responseBody)
		        throws ProcessException {

		if ((parcel != null) 
			& (endPointSecurity != null)) {
			Enum parcelType = parcel.getType();

			switch (parcelType.intValue()) {
				case ParcelTypeEnum.INT_PING :
					if (this.pingHandler != null) {
						return pingHandler.handleParcel(parcel, responseBody);
					} 
					break;
					
				case ParcelTypeEnum.INT_SUBSCRIBE:{
					
					EntityType consumer = parcel.getSender();
					String consumerID = consumer.getId();
					if (consumerID == null) break;
					
					SubscriptionRequestType regRequest = (SubscriptionRequestType) parcel.getBody()
					.changeType(SubscriptionRequestType.type);
					EndpointType endPoint = regRequest.getEndpoint();
					if (endPoint == null) break;
					
					EntityType[] recipients = parcel.getRecipientArray();
					for (int i = 0; i < recipients.length; i++) {
						String pluginID = recipients[i].getId();
						if (pluginID == null) continue;	//check next plugin for subscription						

						if (!endPointSecurity.isValidConsumer(consumerID, endPoint.getUrl(), pluginID)) {
							//check if this is a generic open plug-in.
							//Such plug-ins have the following tuple's setup by default 
							if (!endPointSecurity.isValidConsumer(consumerID, "*", pluginID))	{
								if (!endPointSecurity.isValidConsumer("*", "*", pluginID)) {
									// setup response
									ResponseType response = responseBody.addNewResponse();
									EntityType sender = response.addNewSender();
									sender.setId(pluginID);
									String message = "Subscription request rejected. " + 
										consumerID + "@" + endPoint.getUrl() 
										+ "is not authorised to subscribe to plug-in "
										+ pluginID;							
									log.warn(message);
									response.setMessage(message);
									response.setStatus(ResponseStatusEnum.FAILURE);
									return responseBody;
								}
							}							
						}						
					}
				}
			}
		}
		
		
		return super.handleIncomingParcel(parcel,responseBody);
		
	}


}
