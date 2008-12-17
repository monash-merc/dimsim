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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.extension.operation.ICommandEnable;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.CommandType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.ResponseType;
import org.instrumentmiddleware.cima.parcel.VariableType;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.producer.impl.AbstractCreator;
import org.instrumentmiddleware.cima.plugin.producer.impl.AbstractProducer;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.w3c.dom.Node;

import au.edu.archer.dimsim.buffer.IDeliveryBuffer;
import au.edu.archer.dimsim.buffer.plugin.IBufferNot;
import au.edu.archer.dimsim.buffer.plugin.parcelcreator.impl.EternalWaitParcelCreator;
import au.edu.archer.dimsim.buffer.util.DeliverParcels;
import au.edu.archer.dimsim.security.IEndPointSecurity;

/**
 * Plug-in to query Dimsim Buffer. 
 * <p>Consumers query Dimsim buffer by sending Command Parcels to this plugin.   
 * Buffer Commands recognized by this plug-in are defined as Enums in  
 * {@link IDeliverybuffer} interface. 
 * 
 * <p>Sample methods to query buffer parcels are defined in {@link DimsimConsumerPlugin} class. 
 * Consumers can use any direct subclass of DimsimConsumerPlugin to query Dimsim buffer.
 *  
 * <p>CIMA will not pass parcels to this plug-in without a valid subscription. 
 * Hence it is important for consumers querying the buffer to establish a
 * subscription to this buffer plugin before sending command parcels.   
 * <p>  
 * For getParcel queries, a separate process is created to deliver the results of the query. @see DeliverParcels.
 * The session id of the parcel is set to current buffer-plugin/consumer session id before delivery.
 * <p>
 * The result of command operation can be determined from the following two fields in the response object.
 * 1. status : set to either ResponseStatusEnum.FAILURE or ResponseStatusEnum.SUCCESS
 * 2. result : set to number of parcels matching command query sent in. As stated above, result parcels 
 *    are delivered using a separate process. Due to the asynchronous nature of the delivery process, 
 *    consumers may find result parcels arriving before response to command request is received.
 *    Hence it is important for consumers to de-link parcel processing logic from query response.
 *          
 * <p>
 * If the bufferSecurity variable is set to a valid IEndpointSecurity object, then the same
 * will be passed to DeliverParcels object. DeliverParcel object enforces endpoint security as 
 * follows :
 * <ul><li>A parcel is delivered to the consumer querying the buffer only if the buffered parcel 
 * has a session whose consumer id and end-point URL match the current consumer id 
 * and its end-point URL.
 * <li>For more information on endpoint security refer to documentation in 
 * {@link EndpointSubscriptionSecurityModel}
 * </ul>
 * <p>
 * @see DimsimConsumerPlugin
 * @see IDeliveryBuffer.bufferMethodEnum
 * @see IEndpointSecurity
 * <p> 
 * @author Rafi M Feroze
 *
 */
public class BufferPlugin extends AbstractProducer 
	implements ICommandEnable, IBufferNot {
	   
       private static Logger log = Logger.getLogger(BufferPlugin.class);

       private IDeliveryBuffer deliveryBuffer;   

       /**
        * Set this variable if endpoint security is to be enforced on parcel delivery.
        * When endpoint security is enabled, buffered parcels will not be delivered to consumers
        * if they were not originally meant to be delivered to them.
        * <p>This is enforced by matching consumer id and endpoint urls of the original sessions
        * against current consumer subscribed to the buffer plugin. 
        * 
        */
       protected IEndPointSecurity bufferSecurity; 
       
       /**
        * This class is a sub-class of AbstractProducer, which requires a parcel creator to be defined.
        * Currently set to a {@link EternalWaitParcelCreator}.   
        */
       protected static AbstractCreator pCreator = new EternalWaitParcelCreator();
       
	   /**
        * @param id
        * @throws Exception
        */
       public BufferPlugin(ICIMADirector director, String id, IDeliveryBuffer deliveryBuffer) throws Exception {
    	   
    	   super(id,pCreator,director);
    	   this.deliveryBuffer = deliveryBuffer;
       }

       /**
        * Set this variable if endpoint security is to be enforced on parcel delivery.
        * When endpoint security is enabled, buffered parcels will not be delivered to consumers
        * if they were not originally meant to be delivered to them.
        * <p>This is enforced by matching consumer id and endpoint urls of the original sessions
        * against current consumer subscribed to the buffer plugin. 
        * 
        */       
       @SuppressWarnings("unused")       
       public void setBufferSecurity(IEndPointSecurity endPointSecurity) {
    	   this.bufferSecurity = endPointSecurity;
       }
       
       public IEndPointSecurity getBufferSecurity() {
    	   return this.bufferSecurity;
       }
       
       
       public BufferPlugin(String id, IDeliveryBuffer deliveryBuffer) throws Exception {    	   
    	   this(CIMAUtil.getDirector(),id, deliveryBuffer);    	       	   
       }

       public void setDeliveryBuffer(IDeliveryBuffer buffer) {
    	   this.deliveryBuffer = buffer;
       }
       /** 
        * @see org.instrumentmiddleware.cima.extension.operation.ICommandEnable#processCommandOperation(org.instrumentmiddleware.cima.parcel.CommandOperationType, org.instrumentmiddleware.cima.session.ISession, org.instrumentmiddleware.cima.parcel.CommandOperationResponseType)
        */
       @SuppressWarnings("unchecked")
       public CommandOperationResponseType processCommandOperation(
        CommandType operation, Calendar calendar, ISession session,
        CommandOperationResponseType response) throws PluginException {   
    	   
	       response.setStatus(ResponseStatusEnum.FAILURE); //set by default, changed later if deliveryBuffer request is successful
	       response.setResult("0");
	       
	       IDeliveryBuffer.bufferCommandEnum command = 
	    	   IDeliveryBuffer.bufferCommandEnum.getValidCommandEnum(operation.getCommandName());
	       if (command == null) {	    	   	   
	    		   response.setMessage(command + " Command not available");
	               log.info("Illegal command request : command is  " + operation.getCommandName());
	               return response;
	       }
	
	       log.debug("processing command " + command.getName());
	       
	       
	       String paramName;
	       IDeliveryBuffer.bufferCommandParamTypeEnum param;
	       EnumMap<IDeliveryBuffer.bufferCommandParamTypeEnum, String> inParams =
	    	   	new EnumMap<IDeliveryBuffer.bufferCommandParamTypeEnum, String> (IDeliveryBuffer.bufferCommandParamTypeEnum.class);	       
	       
	       VariableType[] params = operation.getParameterArray();
	       log.debug("Got " + params.length + " params for " +  command.getName());
	       for (int i = 0; i < params.length; i++) {
	    	   	   paramName = params[i].getName();	    	   	   
	    	   	   if (IDeliveryBuffer.bufferCommandParamTypeEnum.isValid(paramName)) {
	    	   		   	param = IDeliveryBuffer.bufferCommandParamTypeEnum.getEnum(paramName);
	                    String value = params[i].getValue();
	                    log.debug("Got value: " + value + " for param: " + paramName);
	                    if (param.isValidValue(value)) {
	                    	inParams.put(param, value);
	                    }
	    	   	   }
	       }
	       
	               	            	   
	
			String bodyType = null;
			String pluginId = inParams.get(IDeliveryBuffer.bufferCommandParamTypeEnum.PLUGINID);
			long startTimeInMillis = 0L, endTimeInMillis = 0L;
			IDeliveryBuffer.bufferMethodEnum methodEnum = command.getBufferMethodEnum(inParams);
			if (methodEnum == null) {
				   String message = "Missing Parameters for Command " + command.getName();
				   message += "\n Minimum valid Params are " + command.getMinimumValidParams(); 
				   response.setMessage(message);
	               log.info(message);
	               return response;
			}
	        
	       Object ret = null;
		   try {
	    	   ret = methodEnum.invoke(this.deliveryBuffer, inParams);
		   } catch (Exception ex) {
			   response.setMessage(ex.getMessage());
			   ex.printStackTrace();
			   return response;
		   }	           
	       
           // process results from deliveryBuffer
           
           response.setStatus(ResponseStatusEnum.SUCCESS);
           if (ret == null) {	            	   
               response.setResult("0");
           }
           else if(ret instanceof Integer) {	            	 
               	response.setResult(((Integer)ret).toString());
           } else if (ret instanceof IRegisteredParcel) {
        		response.setResult("1");
                deliverParcel(ret, session, pluginId);            		
           } else if (ret instanceof ArrayList) {
                ArrayList<IRegisteredParcel> retList = (ArrayList<IRegisteredParcel>)ret;
                response.setResult(retList.size() + "");
                deliverParcel(retList, session, pluginId);
           } 
       
	       return response;
       
       }

       /* (non-Javadoc)
        * @see org.instrumentmiddleware.cima.plugin.IPlugin#getInformation()
        */
       public Node getInformation() {
               // TODO Auto-generated method stub
               return null;
       }       
       
    
    @SuppressWarnings("unchecked")
	private void deliverParcel(Object parcelHolder, ISession session, String pluginId) {
    	   DeliverParcels<IRegisteredParcel> parcelDeliverer = null;
    	   if (parcelHolder instanceof IRegisteredParcel) {
    		   IRegisteredParcel retParcel = (IRegisteredParcel)parcelHolder;
    		   parcelDeliverer =  new DeliverParcels<IRegisteredParcel> (this, pluginId,retParcel,session);
           	            		
    	   } else if (parcelHolder instanceof ArrayList) {
    		   ArrayList<IRegisteredParcel> retList = (ArrayList<IRegisteredParcel>)parcelHolder;
    		   parcelDeliverer = new DeliverParcels<IRegisteredParcel> (this, pluginId, retList,session);
    	   }
    	   
    	   if (parcelDeliverer != null) {
    		   parcelDeliverer.setBufferSecurity(getBufferSecurity());
    		   parcelDeliverer.run();
    	   }
       } 
    
    public ResponseType processSubscribeParcel(BodyType body,
    	    Calendar creationTime, ISession session, ResponseType response)
    	        throws PluginException {
    	
    	
    	return super.processSubscribeParcel(body, creationTime, session, response);   	
    }
    
}
