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
package au.edu.archer.dimsim.buffer.plugin.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.SubscriptionResponseType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.impl.AbstractPlugin;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.instrumentmiddleware.cima.util.SubscribeInfo;

import au.edu.archer.dimsim.buffer.IDeliveryBuffer;
import au.edu.archer.dimsim.buffer.exception.NoSubscriptionToRemoteBufferPlugin;
import au.edu.archer.dimsim.buffer.util.DimsimUtils;
import au.edu.archer.dimsim.buffer.util.ParcelUtils;


/**
 * @author mrafi
 *
 * The purpose of this class is to provide consumer plug-ins access to Remote Buffer.
 * 
 */
public abstract class DimsimConsumerPlugin extends AbstractPlugin {

	private static Logger log = Logger.getLogger(DimsimConsumerPlugin.class);
	
	String remoteBufferSessionID;
	protected String remoteBufferPluginID;	

	
	protected DimsimConsumerPlugin(String id) throws Exception {
		super(id);				
	}
	
	public void setRemoteBufferPluginID(String bufferPluginID) {
		this.remoteBufferPluginID = bufferPluginID;
	}	
	
	public boolean subscribeRemoteBufferPlugin(SubscribeInfo remoteBufferInfo) {		
		
		SubscriptionResponseType response = this.doSubscribe(remoteBufferInfo);		
		if (response != null) {
			this.remoteBufferSessionID = response.getNewSessionId();
			return true;
		}
		
		log.debug(response.getMessage());
		return false;
		
	}

	private Map<String,String> createParamMap(String pluginID) throws Exception {
		HashMap<String,String> paramMap = new HashMap<String,String> ();		
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.PLUGINID.getName(), pluginID);		
		return paramMap;
	}	

	// Buffer Command implementation Begin 
	protected CommandOperationResponseType getRemoteBufferParcels(String sourcePluginID) 
	throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {
		return  getRemoteBufferCommandResponse(createBufferCommandParcel(
					IDeliveryBuffer.bufferMethodEnum.GetParcels_String.getName(),createParamMap(sourcePluginID)));
	}

	protected CommandOperationResponseType getRemoteBufferParcels(String sourcePluginID, long startTimeInMillis) 
	throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception	{
		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS.getName(), Long.toString(startTimeInMillis));
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLong.getName(),paramMap));
	}

	protected CommandOperationResponseType getRemoteBufferParcel(String sourcePluginID,long startTimeInMillis) 
	throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception	{
		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS.getName(), Long.toString(startTimeInMillis));
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetParcel_StringLong.getName(),paramMap));
	}	
	
	protected CommandOperationResponseType getRemoteBufferParcels(String sourcePluginID,long startTimeInMillis,
			long endTimeInMillis) 
			throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {
		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS.getName(), Long.toString(startTimeInMillis));
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.ENDTIMEINMILLIS.getName(), Long.toString(endTimeInMillis));
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLongLong.getName(),paramMap));
	}

	protected CommandOperationResponseType getRemoteBufferSize(String sourcePluginID) 
		throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {
		return  getRemoteBufferCommandResponse(createBufferCommandParcel(
				IDeliveryBuffer.bufferMethodEnum.GetSize_String.getName(),createParamMap(sourcePluginID)));
	}

	protected CommandOperationResponseType getRemoteBufferSize(String sourcePluginID,long startTimeInMillis)
		throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {
		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS.getName(), Long.toString(startTimeInMillis));
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetSize_StringLong.getName(),paramMap));
	}

	protected CommandOperationResponseType getRemoteBufferSize(String sourcePluginID,long startTimeInMillis, long endTimeInMillis) 
		throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {
		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS.getName(), Long.toString(startTimeInMillis));
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.ENDTIMEINMILLIS.getName(), Long.toString(endTimeInMillis));
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetSize_StringLongLong.getName(),paramMap));
	}	

	protected  CommandOperationResponseType getRemoteBufferParcels(String sourcePluginID,
			Class<? extends BodyType> bodyType) 
			throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {

		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.BODYTYPE.getName(), bodyType.getName());
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringClass.getName(),paramMap));		
	}
	
	protected  CommandOperationResponseType getLastRemoteBufferParcel(String sourcePluginID,
			Class<? extends BodyType> bodyType) 
			throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {

		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.BODYTYPE.getName(), bodyType.getName());
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetLastParcel_StringClass.getName(),paramMap));		
	}

	
	protected CommandOperationResponseType getRemoteBufferParcels(String sourcePluginID,
			long startTimeInMillis, Class<? extends BodyType> bodyType) 
			throws NoSubscriptionToRemoteBufferPlugin, XmlException, Exception {

		Map<String,String> paramMap = createParamMap(sourcePluginID);
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.BODYTYPE.getName(), bodyType.getName());
		paramMap.put(IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS.getName(), Long.toString(startTimeInMillis));
		
		return  getRemoteBufferCommandResponse(
					createBufferCommandParcel(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLongClass.getName(),paramMap));
	}	
	
	//Buffer Command Implementation End
	
	protected Parcel createBufferCommandParcel(String commandName, Map<String,String> paramMap) 
		throws NoSubscriptionToRemoteBufferPlugin, PluginException  {
		Parcel retParcel = ParcelUtils.createCommandParcel(commandName, paramMap);
		retParcel.addNewSessions().addNewSession().setSessionId(getRemoteBufferPluginSessionID());			
		
		return retParcel;
	}
	
	protected ISession getRemoteBufferPluginSession() 
	throws NoSubscriptionToRemoteBufferPlugin, PluginException {
		if (this.remoteBufferPluginID == null) {
			throw new PluginException("Remote BufferPlugin id not set, Unable to query Buffer");
		}
		try {
			for(ISession s : DimsimUtils.getSessionManager().getSessions(this)) {
				if (s.getSubscriber().getId().equals(remoteBufferPluginID)) {
					return s;				
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		throw new NoSubscriptionToRemoteBufferPlugin ();
	}
	
	protected String getRemoteBufferPluginSessionID() 
		throws PluginException {
		if (remoteBufferSessionID == null) {
			ISession s = getRemoteBufferPluginSession();
			this.remoteBufferSessionID = s.getId();					
		}
		
		return remoteBufferSessionID;		
	}	
	
	private CommandOperationResponseType getRemoteBufferCommandResponse(
			Parcel parcel) throws XmlException, Exception {
		ISession session = getRemoteBufferPluginSession();
		if (session != null) {
			EndpointType endpoint = (EndpointType) session.get("endpoint");
			return getRemoteCommandResponse(parcel,endpoint);
		}
		return null;
	}

	protected CommandOperationResponseType getRemoteCommandResponse(Parcel parcel, EndpointType endpoint) 
	throws XmlException, Exception {
		log.debug("Sending Endpoint url, type : " + endpoint.getUrl() + " ," + endpoint.getType());
		Parcel response = CIMAUtil.getDirector().handleOutgoingParcel(this, parcel, endpoint);
		CommandOperationResponseType commandResponse = null;
		if (response != null) {
			commandResponse = (CommandOperationResponseType) ParcelUtil.getResponse(response)
				.getResponseArray(0).changeType(CommandOperationResponseType.type);
		}
	
		return commandResponse;
	}
	
	/*
	 * Need this to ensure local buffer is called if Buffer Endpoint is same as the current plugin
	 * public synchronized ResponseType processParcel(BodyType body,
		    Calendar creationTime, ISession session, ResponseType response)
		        throws PluginException {
		
	}*/	
	
}
