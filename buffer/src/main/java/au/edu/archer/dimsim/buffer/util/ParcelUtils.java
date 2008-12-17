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

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.instrumentmiddleware.cima.ConsoleMain;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.CommandOperationType;
import org.instrumentmiddleware.cima.parcel.CommandType;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EndpointTypeEnum;
import org.instrumentmiddleware.cima.parcel.EntityType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.SubscriptionRequestType;
import org.instrumentmiddleware.cima.parcel.SubscriptionResponseType;
import org.instrumentmiddleware.cima.parcel.ValueVariableType;
import org.instrumentmiddleware.cima.parcel.VariableType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;

public class ParcelUtils {

	static int sequenceNum = 0;
	
	public static Parcel buildCommandParcel(String commandName, ValueVariableType[] params) {
        Parcel parcel = ConsoleMain.newBasicParcel();
        parcel.setType(ParcelTypeEnum.PLUGIN);

        CommandOperationType commandRequest = (CommandOperationType) parcel.addNewBody()
        					.changeType(CommandOperationType.type);
        CommandType operation = commandRequest.addNewCommandOperation();
        operation.setCommandName(commandName);
        operation.setParameterArray(params);		
        
        return parcel;
    }
	
	public static ValueVariableType[] buildParamArray(String[] name, String[] value) {		
		
		if (name == null)  return new ValueVariableType[0];
		
		ValueVariableType[] retArr = new ValueVariableType[name.length];		
		for(int i = 0; i<retArr.length;i++) {
			retArr[i] = ValueVariableType.Factory.newInstance();
			retArr[i].setName(name[i]);
			if (value != null) {
				retArr[i].setValue(value[i]);
			}
		}
		
		return retArr;
	}
	
	public static String getSender(IRegisteredParcel registeredParcel) {
		String senderID = null;
		if (registeredParcel != null) {
			EntityType sender = registeredParcel.getSender();
			if (sender != null) {
				senderID = registeredParcel.getSender().getId();
			} else {
				List<ISession> sessions = registeredParcel.getRegisteredSessions();
				if ((sessions != null) 
					& (sessions.size() > 0)) 	{
					ISession session = sessions.get(0);
					if (session != null) {
						senderID = session.getPlugin().getId();
					}
				}
			}
		}
		return senderID;
	}	

	public static Parcel createCommandParcel(String commandName,
			Map<String, String> paramMap) throws PluginException {

		if (commandName == null) {
			throw new PluginException("CommandName is null : Cannot create Command Parcel ");
		}
		
		Parcel parcel = ConsoleMain.newBasicParcel();
		parcel.setType(ParcelTypeEnum.PLUGIN);
		
		CommandOperationType commandRequest = (CommandOperationType) parcel.addNewBody()
			.changeType(CommandOperationType.type);
		CommandType operation = commandRequest.addNewCommandOperation();
		operation.setCommandName(commandName);
					
		if (paramMap != null) {
			for (String name :paramMap.keySet()) {			
				VariableType param = operation.addNewParameter();
				param.setName(name);
				param.setValue(paramMap.get(name));
			}
		}

		return parcel; 
		
	}
	
	public static Parcel createSubscribeParcel(String senderID, String recipientID, EndpointType endpointCanBeNullForMemoryTransport) {
	
		Parcel parcel = ParcelUtil.newParcel(ParcelTypeEnum.SUBSCRIBE);
		parcel.setSequenceId(sequenceNum++);
		parcel.setCreationTime(Calendar.getInstance());

		EntityType sender = parcel.addNewSender();
		sender.setId(senderID);

		EntityType recipient = parcel.addNewRecipient();
		recipient.setId(recipientID);

		SubscriptionRequestType request = (SubscriptionRequestType) parcel.addNewBody()
			.changeType(SubscriptionRequestType.type);

		if (endpointCanBeNullForMemoryTransport == null) {
			EndpointType endpoint = request.addNewEndpoint();
			endpoint.setUrl("irrelevant");
			endpoint.setType(EndpointTypeEnum.MEMORY);
		} else {
			request.setEndpoint(endpointCanBeNullForMemoryTransport);
		}
		
		return parcel;
	}
	
}
