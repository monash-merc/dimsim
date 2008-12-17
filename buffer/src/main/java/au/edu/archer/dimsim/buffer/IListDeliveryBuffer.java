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
package au.edu.archer.dimsim.buffer;

import java.util.Calendar;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.instrumentmiddleware.cima.plugin.PluginException;
/**
 * Extends IDeliveryBuffer to define commands for Listing Buffered Parcels.
 * <p>
 * Clients/Consumers using a class implementing this interface can expect to receive a 
 * File Attribute like structure for each Plugin/Parcel managed by the Buffer. 
 * <p>
 * The attributes available for listing include, 
 * 		PluginID Name, 
 * 		Consumer to which the parcel Belongs, 
 * 		Parcel Type, 
 * 		Size (number of parcels, or actual size of parcel), 
 * 		Access Rights and 
 * 		Parcel creation time stamp.
 * <p> 
 * @author Rafi M Feroze
 *
 */
public interface IListDeliveryBuffer extends IDeliveryBuffer {

	/**
	 * Attributes returned by buffer in response to {@link getList} query.    
	 * @author Rafi M Feroze
	 *
	 */
	public enum BufferListFields { 
		/**
		 * When representing a parcel row, this field represents the producer.
		 * Otherwise, this field represents the Plugin whose details the row details.
		 */
		PLUGINID 	{public Class getParamClass() { return String.class;}}, 
		/**
		 * Available only for rows representing a parcel. Specifies, who the original consumer
		 * of the parcel was.
		 */
		CONSUMERID	{public Class getParamClass() { return Set.class;}},
		/**
		 * Specifies the body type of the parcel.
		 * Defaults to value 'Plugin' for rows representing plugin
		 */
		TYPE		{public Class getParamClass() { return String.class;}},
		/**
		 * Not available for rows representing parcels. Provides the number of parcels
		 * available in the buffer for the plugin
		 */
		SIZE		{public Class getParamClass() { return String.class;}},
		/**
		 * Valid only for rows representing a parcel, set to Name of the parcel's security class
		 */
		ACCESS		{public Class getParamClass() { return String.class;}}, 
		/**
		 * Valid only for rows representing a parcel. Specifies parcel creation time.
		 */
		CREATETIME	{public Class getParamClass() { return Calendar.class;}};
		
		/** 
		 * @return Class representing this Attribute
		 */
		public abstract Class getParamClass();
		};	
		
	/**
	 * Parcel Access Rights, currently unused.
	 * @author Rafi M Feroze
	 *
	 */	
	public enum BufferAccessFlags { NONE, READ, PERSIST, LOAD };
	
	/**
	 * Return a Set of Maps. Each Map represents a plugin managed by the Buffer.
	 * Map Attributes include PluginID,Size,Access  
	 * @return Details for each Plugin managed by Buffer
	 */
	public Set<Map<BufferListFields, Object>> getList();
	
	/**
	 * Return a Set of Maps. Each Map represents the details of Parcels availabe for a given Plugin  
	 * @param pluginId	Plugin ID 
	 * @return	Set of Map Attributes, attributes include PluginID, ConsumerID,ParcelType, 
	 * 							ParcelSize, Access Rights and Create Time
	 */
	public Set<Map<BufferListFields, Object>> getList(String pluginId);
	
	/**
	 * Enum Definitions for List Commands. This represents the name part of the command.
	 * For enum definitions of overloaded methods, refer to @see listBufferMethodEnum 
	 * @author Rafi M Feroze
	 *
	 */	
	public enum listBufferCommandEnum {
		/**
		 * Enum definition for getList command
		 */
		GetList("getList") {
			public listBufferMethodEnum getListBufferMethodEnum(EnumMap<bufferCommandParamTypeEnum, String> inParams) 
			{
			listBufferMethodEnum retVal = listBufferMethodEnum.GetList;
			if (inParams != null) {				
				if (inParams.containsKey(bufferCommandParamTypeEnum.PLUGINID)) {
					retVal = listBufferMethodEnum.GetList_String;					
				}
			}
			return retVal;
			}
		
			public String getMinimumValidParams() {
				return "<No params required for " + getName() + ">";  
			}};
		
		private String name;		
		listBufferCommandEnum(String name) {this.name = name;}
		/**		  
		 * @return String representation of command name
		 */
		public String getName() {return name;}	
		/**		  
		 * @param String representation of command to be checked
		 * @return true if input string represents a valid enum defined in this class, false otherwise
		 */
		public static boolean isValid(String name) {
			if (name != null) { 
				for(listBufferCommandEnum c : EnumSet.allOf(listBufferCommandEnum.class)) {
					if (c.getName().equals(name)) {
						return true;
					}
				}
			}
			return false;
		}
		/** 
		 * @param inParams overload parameters
		 * @return	List method enum represented by input over load parameters
		 */
		public abstract listBufferMethodEnum getListBufferMethodEnum(EnumMap<bufferCommandParamTypeEnum, String> inParams);
		
		/**
		 * Useful for Debug/Error messages 
		 * @return	Concatenated names of minimum required parameters for successful execution of List Command 
		 */
		public abstract String getMinimumValidParams();
	}

	/**
	 * Enum definitions for Overloaded List Methods
	 * @author Rafi M Feroze
	 *
	 */
	public enum listBufferMethodEnum {
		/**
		 * String representation of getList() method
		 */
		GetList(listBufferCommandEnum.GetList)
		{ public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
			return null;
		}
		public Object invoke(IListDeliveryBuffer dBuffer, 
				EnumMap<bufferCommandParamTypeEnum, String> inParams) {
				if (dBuffer != null) {
					return dBuffer.getList();
				}
			  return null;
		  }
		},
		/**
		 * String representation of getList(String pluginId) method
		 */
		GetList_String(listBufferCommandEnum.GetList)
			{ public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.PLUGINID.getParamMap(null);
			}
			public Object invoke(IListDeliveryBuffer dBuffer, 
					EnumMap<bufferCommandParamTypeEnum, String> inParams) {
					if ((dBuffer != null) && (inParams != null)) {
						return dBuffer.getList(inParams.get(bufferCommandParamTypeEnum.PLUGINID));
					}
				  return null;
			  }
			};
		
		private listBufferCommandEnum method;
		
		listBufferMethodEnum(listBufferCommandEnum method) {this.method = method;}
		
		/**		  
		 * @return String representation of method represented by this Enum
		 */
		public String getName() {return method.getName();}
		
		/**   
		 * @return EnumMap representing Parameters for this Enum.
		 */
		public abstract EnumMap<bufferCommandParamTypeEnum, Class> getParamMap();
		/**
		 * Dynamic invocation of method represented by this Enum. Using this method
		 * simplifies validation of input parameters and method name
		 * @param dBuffer	Buffer to invoke method from
		 * @param inParams	Parameters for method to be invoked.
		 * @return	Return value from method invocation
		 * @throws PluginException
		 */
		public abstract Object invoke(IListDeliveryBuffer dBuffer, 
				EnumMap<bufferCommandParamTypeEnum, String> inParams) throws PluginException;
	}									 
}
