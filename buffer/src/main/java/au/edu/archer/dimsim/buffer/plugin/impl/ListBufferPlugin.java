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


import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.CommandType;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.VariableType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.util.ParcelUtil;

import au.edu.archer.dimsim.buffer.IDeliveryBuffer;
import au.edu.archer.dimsim.buffer.IListDeliveryBuffer;
import au.edu.archer.dimsim.buffer.IListDeliveryBuffer.BufferListFields;
import au.edu.archer.dimsim.buffer.parceltype.BufferAccessEnum;
import au.edu.archer.dimsim.buffer.parceltype.ListBufferRow;
import au.edu.archer.dimsim.buffer.parceltype.ListBufferType;
import au.edu.archer.dimsim.security.impl.EndpointSubscriptionSecurityModel;

/**
 * @author mrafi
 *
 */
public class ListBufferPlugin extends BufferPlugin {
	private static Logger log = Logger.getLogger(ListBufferPlugin.class);
	
	IListDeliveryBuffer listBuffer;	
	
	/**
	 * @param director
	 * @param id
	 * @param deliveryBuffer
	 * @throws Exception
	 */	
	public ListBufferPlugin(ICIMADirector director, String id,
			IListDeliveryBuffer deliveryBuffer) throws Exception {
		super(director, id, deliveryBuffer);	
		this.listBuffer = deliveryBuffer;
	}

	/**
	 * @param id
	 * @param deliveryBuffer
	 * @throws Exception
	 */	
	public ListBufferPlugin(String id, IListDeliveryBuffer deliveryBuffer)
			throws Exception {
		super(id, deliveryBuffer);
		this.listBuffer = deliveryBuffer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CommandOperationResponseType processCommandOperation(
		        CommandType operation, Calendar calendar, ISession session,
		        CommandOperationResponseType response) throws PluginException {			       
			
		log.debug("Recevied Command " + operation.getCommandName());
		//process List command only
	    String command = operation.getCommandName();
	    if (!command.equalsIgnoreCase(IListDeliveryBuffer.listBufferCommandEnum.GetList.getName())) {
	       return super.processCommandOperation(operation, calendar, session, response);
	    }
	       
	    response.setStatus(ResponseStatusEnum.FAILURE);  //reset later to success  
	    if (listBuffer == null) {
	    	throw new PluginException("Delivery Buffer is null");
	    }
	    
	    String pluginId = null;
	    VariableType[] params = operation.getParameterArray();	    
	    String paramName;
	    for (int i = 0; i < params.length; i++) {
	    	paramName = params[i].getName();
	        if (paramName.equalsIgnoreCase(IDeliveryBuffer.bufferCommandParamTypeEnum.PLUGINID.getName())) {
	            pluginId = params[i].getValue();	            
	        }
        }	    
	    
	    Set<Map<BufferListFields, Object>> resultRecs = null;
	    if (pluginId != null) {
	    	resultRecs = listBuffer.getList(pluginId);	    	
	    } else {
	    	resultRecs = listBuffer.getList();	    	
	    }	
	    
	    
	    if (resultRecs != null) {	    		    	
	    	this.releaseParcel(getListParcel(resultRecs, session));
	    	response.setStatus(ResponseStatusEnum.SUCCESS);
	    	response.setResult(resultRecs.size() + "");
	    }
	    
	    return response;
	}
	
	private Parcel getListParcel(
			Set<Map<BufferListFields, Object>> resultRecs, ISession session) {
			
		Parcel parcel = ParcelUtil.newParcel(ParcelTypeEnum.PLUGIN);

		BodyType body = parcel.addNewBody();
		//@SuppressWarnings("unused")
		ListBufferType bodyR = (ListBufferType)body.changeType(ListBufferType.type);    
		for(Map<BufferListFields, Object> m : resultRecs) {
			if (m != null) {
				ListBufferRow row = bodyR.addNewRow();
				
				Object pluginId = m.get(BufferListFields.PLUGINID);
				if (pluginId != null) {
					row.setPluginid(pluginId.toString());
				}
				
				Object consumerIds = m.get(BufferListFields.CONSUMERID);
				if (consumerIds != null) {
					if (consumerIds instanceof Set) {
						Set<String> consumerSet = (Set)consumerIds;
						for (String consumerId:consumerSet) {
							row.setConsumerid(consumerId);
						}
					}
				}				
				
				Object createTime = m.get(BufferListFields.CREATETIME);
				if (createTime != null) {
					Calendar cal = (Calendar) createTime;									
					row.setCreatetime(cal);
				}
				
				Object size = m.get(BufferListFields.SIZE);
				if (size != null) {
					row.setSize(size.toString());
				}
				
				Object type = m.get(BufferListFields.TYPE);
				if (type != null) {
					row.setType(type.toString());
				}
				
				if (this.bufferSecurity != null) {
					Set<String> tuple = EndpointSubscriptionSecurityModel.getConsumerEndPoint(session);
					tuple.add(pluginId.toString());
					if (this.bufferSecurity.isValidConsumer(tuple)) {						
						row.setAccess(BufferAccessEnum.READ);
					} else {
						row.setAccess(BufferAccessEnum.NONE);
					}
				}				
			}
		}
       
        return parcel;
	}
	
}
