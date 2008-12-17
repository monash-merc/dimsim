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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;

import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.springframework.context.ApplicationEventPublisherAware;

import au.edu.archer.dimsim.buffer.util.DimsimUtils;

/**
 * Lists basic query commands that can be sent to the Dimsim buffer. 
 * <p>This includes Buffer size and parcel list
 * <p>To help client/customer plug-ins Enums for Command, Method and Parameter names 
 * are also available
 * 
 * @author Rafi M Feroze
 *
 */
public interface IDeliveryBuffer extends ApplicationEventPublisherAware {
	
	/**
	 * Buffer input RegisteredParcel for the given plugin
	 * @param pluginId 			Source Plugin ID
	 * @param registeredParcel 	Parcel to Buffer
	 * @return					Number of Parcels available 
	 */
	public void buffer(String pluginId, IRegisteredParcel registeredParcel);
	

	/**
	 * Get the number of parcels available for the given plugin
	 * @param	pluginId 	Source Plugin ID 
	 * @return	Number of Parcels available
	 */	
	public int getSize(String pluginId);
	
	/**
	 * Get the number of parcels available for the given plugin with 
	 * time-stamp greater than or equal to the input time-stamp 
	 * @param	pluginId 			Source Plugin ID
	 * @param	startTimeInMillis	Start time-stamp in milliseconds
	 * @return	Number of Parcels available 
	 */
	public int getSize(String pluginId, long startTimeInMillis);
	
	/**
	 * Get the number of parcels available for the given plugin with 
	 * time-stamp in the input time-stamp range  
	 * @param	pluginId 			Source Plugin ID
	 * @param	startTimeInMillis	Start time-stamp in milliseconds
	 * @param	endTimeInMillis		End time-stamp in milliseconds
	 * @return	Number of Parcels available 
	 */
	public int getSize(String pluginId, long startTimeInMillis, long endTimeInMillis);

	/**
	 * Return the first parcel matching the input time-stamp for the given Plug-in	 
	 *   
	 * @param	pluginId 		Source Plugin ID
	 * @param	timeInMillis	time-stamp in milliseconds 
	 * @return	first parcel matching input time-stamp 
	 */
	public IRegisteredParcel getParcel(String pluginId, long timeInMillis);	

	/**
	 * Return a list of all available parcels for the given plug-in.	 
	 *   
	 * @param	pluginId 		Source Plugin ID 
	 * @return	ArrayList of parcels 
	 */	
	public ArrayList<IRegisteredParcel> getParcels(String pluginId);
	
	/**
	 * Return a list of all available parcels for the given plug-in and	 
	 * time-stamp greater than or equal to input time-stamp
	 * @param	pluginId 			Source Plugin ID 
	 * @param	startTimeInMillis	Start time-stamp in milliseconds
	 * @return	ArrayList of parcels
	 */
	
	public ArrayList<IRegisteredParcel> getParcels(String pluginId, long startTimeInMillis);
	
	/**
	 * Return a list of all available parcels for the given plug-in and
	 * time-stamp in the input time-stamp range  
	 * @param	pluginId 			Source Plugin ID
	 * @param	startTimeInMillis	Start time-stamp in milliseconds
	 * @param	endTimeInMillis		End time-stamp in milliseconds
	 * @return	ArrayList of parcels
	 */	
	public ArrayList<IRegisteredParcel> getParcels(String pluginId, long startTimeInMillis, long endTimeInMillis);
	
	/**
	 * Return the last parcel with a matching input BodyType for the given plug-in	 
	 *   
	 * @param	pluginId 		Source Plugin ID
	 * @param	bodyType		Body Type Implementation class required 
	 * @return	Last buffered parcel matching input criteria 
	 */
	public IRegisteredParcel getLastParcel(String pluginId, Class<? extends BodyType> bodyType);
	
	/**
	 * Return a list of parcels with a matching input BodyType for the given plug-in	 
	 *   
	 * @param	pluginId 		Source Plugin ID
	 * @param	bodyType		Body Type Implementation class required 
	 * @return	ArrayList of parcels matching input criteria 
	 */
	public ArrayList<IRegisteredParcel> getParcels(String pluginId, Class<? extends BodyType> bodyType);
	
	/**
	 * Return a list of all available parcels for the given plug-in with input BodyType and	 
	 * time-stamp greater than or equal to input time-stamp
	 * @param	pluginId 			Source Plugin ID 
	 * @param	startTimeInMillis	Start time-stamp in milliseconds
	 * @param	bodyType			Body Type Implementation class required
	 * @return	ArrayList of parcels matching input criteria
	 */
	public ArrayList<IRegisteredParcel> getParcels(String pluginId, long startTimeInMillis, Class<? extends BodyType> bodyType);	

	
	/**
	 * Enum definitions for Buffer Commands. 
	 * <p>This represents the name part of the Buffer Commands.
	 * <p>Consumers querying {@link BufferPlugin} can use definitions and methods defined in this Enum to ensure correct syntax
	 * <p>For enum definitions of overload parameters refer to {@link bufferMethodEnum}
	 * @author Rafi M Feroze
	 */
	public enum bufferCommandEnum {
		/**
		 * Enum Definition for getSize method name. 
		 *    
		 */
		GetSize("getSize") {public bufferMethodEnum getBufferMethodEnum(EnumMap<bufferCommandParamTypeEnum, String> inParams) {
								bufferMethodEnum retVal = null;
								if (inParams != null) {
									if (inParams.containsKey(bufferCommandParamTypeEnum.PLUGINID)) {
										retVal = bufferMethodEnum.GetSize_String;
										if (inParams.containsKey(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)) {
											retVal = bufferMethodEnum.GetSize_StringLong;
											if (inParams.containsKey(bufferCommandParamTypeEnum.ENDTIMEINMILLIS)) {
												retVal = bufferMethodEnum.GetSize_StringLongLong;
											}
										}
									}
								}
								return retVal;
							}
							public String getMinimumValidParams() {
							return DimsimUtils.SetToString(bufferMethodEnum.GetSize_String.getParamMap().values());  
						}},
		/**
		 * Enum definition for getParcel method name
		 */
		GetParcel("getParcel"){public bufferMethodEnum getBufferMethodEnum(EnumMap<bufferCommandParamTypeEnum, String> inParams) {
									bufferMethodEnum retVal = null;
									if (inParams != null) {
										if (inParams.containsKey(bufferCommandParamTypeEnum.PLUGINID)) {											
											if (inParams.containsKey(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)) {
												retVal = bufferMethodEnum.GetParcel_StringLong;												
											}
										}
									}
									return retVal;
									}
									public String getMinimumValidParams() {
									return DimsimUtils.SetToString(bufferMethodEnum.GetParcel_StringLong.getParamMap().values());  
							}},
		/**
		 * Enum definition for getParcels method name
		 */
		GetParcels("getParcels"){public bufferMethodEnum getBufferMethodEnum(EnumMap<bufferCommandParamTypeEnum, String> inParams) {
									bufferMethodEnum retVal = null;
									if (inParams != null) {
										if (inParams.containsKey(bufferCommandParamTypeEnum.PLUGINID)) {
											retVal = bufferMethodEnum.GetParcels_String;
											if (inParams.containsKey(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)) {
												retVal = bufferMethodEnum.GetParcels_StringLong;
												if (inParams.containsKey(bufferCommandParamTypeEnum.BODYTYPE)) {
													retVal = bufferMethodEnum.GetParcels_StringLongClass;													
												} else if (inParams.containsKey(bufferCommandParamTypeEnum.ENDTIMEINMILLIS)) {
													retVal = bufferMethodEnum.GetParcels_StringLongLong;
												}
											} else if (inParams.containsKey(bufferCommandParamTypeEnum.BODYTYPE)) {
												retVal = bufferMethodEnum.GetParcels_StringClass;													
											}
										}
									}
									return retVal;
								}
								public String getMinimumValidParams() {
								return DimsimUtils.SetToString(bufferMethodEnum.GetParcels_String.getParamMap().values());  
							}},
		/**
		 * Enum definition for getLastParcel method name
		 */
		GetLastParcel("getLastParcel"){public bufferMethodEnum getBufferMethodEnum(EnumMap<bufferCommandParamTypeEnum, String> inParams) {
										bufferMethodEnum retVal = null;
										if (inParams != null) {
											if (inParams.containsKey(bufferCommandParamTypeEnum.PLUGINID)) {												
												if (inParams.containsKey(bufferCommandParamTypeEnum.BODYTYPE)) {
													retVal = bufferMethodEnum.GetLastParcel_StringClass;													
												}
											}
										}
										return retVal;							
									}
									public String getMinimumValidParams() {
										return DimsimUtils.SetToString(bufferMethodEnum.GetLastParcel_StringClass.getParamMap().values());  								
							}};
		/**
		 * String representation of method name represented by this Enum 
		 */
		private String name;
		
		bufferCommandEnum(String name) {this.name = name;}		
		/** 
		 * Return {@link IDeliveryBuffer} Interface Method Name
		 * @return Method Name
		 */
		public String getName() {return name;}
		
		/**
		 * Check if input String is a valid {@link IDeliveryBuffer} method name
		 * @param name Name to check
		 * @return	true if valid, else false
		 */
		public static boolean isValid(String name) {
			if (name != null) { 
				for(bufferCommandEnum c : EnumSet.allOf(bufferCommandEnum.class)) {
					if (c.getName().equals(name)) {
						return true;
					}
				}
			}
			return false;
		}
		
		/**
		 * Get Enum constant corresponding to input String representation of {@link IDeliveryBuffer} method 
		 * @param name Buffer Method Name
		 * @return	Buffer Command Enum
		 */
		public static bufferCommandEnum getValidCommandEnum(String name) {
			if (name != null) { 
				for(bufferCommandEnum c : EnumSet.allOf(bufferCommandEnum.class)) {
					if (c.getName().equals(name)) {
						return c;
					}
				}
			}
			return null;
		}		
		/**
		 * Returns the correct Enum MethodName for given set of overload parameters.
		 * @param inParams	EnumMap whose Keys are restricted to BufferCommandParamTypeEnum. 
		 * 					The values for Map fields are not required and can be null. 
		 * @return Enum for the method with input parameters
		*/
		public abstract bufferMethodEnum getBufferMethodEnum(
				EnumMap<bufferCommandParamTypeEnum, String> inParams);
		/**
		 * Returns the minimum required parameters for successful execution of this buffer command.
		 * Useful for display/printing error messages.
		 * @return A concatenated String of minimum required parameters
		 */
		public abstract String getMinimumValidParams();		
	}
	
	/**
	 * Enum definitions for overloaded Buffer Commands. 
	 * @author Rafi M Feroze
	 */	
	public enum bufferMethodEnum {
		/**
		 * Enum definition for method getSize(String pluginId)
		 */
		GetSize_String(bufferCommandEnum.GetSize)
			{ public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringParamMap();
			}
			public Object invoke(IDeliveryBuffer dBuffer, 
					EnumMap<bufferCommandParamTypeEnum, String> inParams) {
					if ((dBuffer != null) && (inParams != null)) {
						return dBuffer.getSize(inParams.get(bufferCommandParamTypeEnum.PLUGINID));
					}
				  return null;
			  }
			},
		/**
		 * Enum definition for method getSize(String pluginId, long startTimeInMillis)
		 */
		GetSize_StringLong(bufferCommandEnum.GetSize)
		{ 	public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringLongParamMap();
			}
			public Object invoke(IDeliveryBuffer dBuffer, 
					EnumMap<bufferCommandParamTypeEnum, String> inParams) {
				if ((dBuffer != null) && (inParams != null)) {
					long start = new Long(inParams.get(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)).longValue();
					return dBuffer.getSize(inParams.get(bufferCommandParamTypeEnum.PLUGINID), start);
				}  
				return null;
			  }
			},
			
		/**
		 * Enum definition for method getSize(String pluginId, 
		 * 						long startTimeInMillis, long endTimeInMillis)
		 */		
		GetSize_StringLongLong(bufferCommandEnum.GetSize)
		{ 	public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringLongLongParamMap();	}
			public Object invoke(IDeliveryBuffer dBuffer, 
					EnumMap<bufferCommandParamTypeEnum, String> inParams) {
					if ((dBuffer != null) && (inParams != null)) {
						long start = new Long(inParams.get(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)).longValue();
						long end = new Long(inParams.get(bufferCommandParamTypeEnum.ENDTIMEINMILLIS)).longValue();
						return dBuffer.getSize(inParams.get(bufferCommandParamTypeEnum.PLUGINID), start, end);
					}
				  return null;
			  }
			},
			/**
			 * Enum definition for method getParcel(String pluginId, long startTimeInMillis)
			 */
		GetParcel_StringLong(bufferCommandEnum.GetParcel)
		{ 	public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringLongParamMap();	}
			public Object invoke(IDeliveryBuffer dBuffer, 
				EnumMap<bufferCommandParamTypeEnum, String> inParams) {
				if ((dBuffer != null) && (inParams != null)) {
					long start = new Long(inParams.get(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)).longValue();
					return dBuffer.getParcel(inParams.get(bufferCommandParamTypeEnum.PLUGINID), start);
				}
			  return null;
		  }
		},
		/**
		 * Enum definition for method getLastParcel(String pluginId, <? extends bodyType> bodyClass);
		 */
		GetLastParcel_StringClass(bufferCommandEnum.GetLastParcel)
		{ 	public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringClassParamMap(); }
			public Object invoke(IDeliveryBuffer dBuffer, 
				EnumMap<bufferCommandParamTypeEnum, String> inParams) throws PluginException {
				if ((dBuffer != null) && (inParams != null)) {
					Class<? extends BodyType> classVal = bufferCommandParamTypeEnum.getBodyClassInstance(inParams.get(bufferCommandParamTypeEnum.BODYTYPE));
					return dBuffer.getLastParcel(inParams.get(bufferCommandParamTypeEnum.PLUGINID), classVal);
				}
			  return null;
		  }},
		  /**
		   * Enum definition for method getParcel(String pluginId,
		   * 						<? extends bodyType> bodyType)
		   */
		GetParcels_StringClass(bufferCommandEnum.GetParcels)
		{ 	public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringClassParamMap();
			}
			public Object invoke(IDeliveryBuffer dBuffer, 
				EnumMap<bufferCommandParamTypeEnum, String> inParams) throws PluginException {
				if ((dBuffer != null) && (inParams != null)) {
					Class<? extends BodyType> classVal = bufferCommandParamTypeEnum.getBodyClassInstance(inParams.get(bufferCommandParamTypeEnum.BODYTYPE));
					return dBuffer.getParcels(inParams.get(bufferCommandParamTypeEnum.PLUGINID), classVal);
				}
			  return null;
		  }}     ,
		  /**
		   * Enum definition for method getParcels(String pluginId, long startTimeInMillis
		   * 						<? extends bodyType> bodyType)
		   */
		GetParcels_StringLongClass(bufferCommandEnum.GetParcels)
		{	public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringLongClassParamMap();			}
			public Object invoke(IDeliveryBuffer dBuffer, 
					EnumMap<bufferCommandParamTypeEnum, String> inParams) throws PluginException {
					if ((dBuffer != null) && (inParams != null)) {
						long start = new Long(inParams.get(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)).longValue();
						Class<? extends BodyType> classVal = bufferCommandParamTypeEnum.getBodyClassInstance(inParams.get(bufferCommandParamTypeEnum.BODYTYPE));
						return dBuffer.getParcels(inParams.get(bufferCommandParamTypeEnum.PLUGINID)
												 ,start, classVal);
					}
				  return null;
			  }},
		/**
		 * Enum definition for method getParcels(String pluginId)
		 */
		GetParcels_String(bufferCommandEnum.GetParcels)
			{ public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringParamMap(); }
			  public Object invoke(IDeliveryBuffer dBuffer, 
					EnumMap<bufferCommandParamTypeEnum, String> inParams) {
					if ((dBuffer != null) && (inParams != null)) {
						return dBuffer.getParcels(inParams.get(bufferCommandParamTypeEnum.PLUGINID));
					}
				  return null;
			  }
			},
			
			/**
			 * Enum defintion for method getParcels(String pluginId, long startTimeInMillis)
			 */
		GetParcels_StringLong(bufferCommandEnum.GetParcels)
			{ public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringLongParamMap(); }
			  public Object invoke(IDeliveryBuffer dBuffer, 
						EnumMap<bufferCommandParamTypeEnum, String> inParams) {
					long start = new Long(inParams.get(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)).longValue();
					if ((dBuffer != null) && (inParams != null)) {
						return dBuffer.getParcels(inParams.get(bufferCommandParamTypeEnum.PLUGINID),start);
					}
				    return null;
			  }
			},
			/**
			 * Enum definition for method getParcels(String pluginId, long startTimeInMillis
			 * 								long endTimeInMillis)
			 */
		GetParcels_StringLongLong(bufferCommandEnum.GetParcels)
			{  public EnumMap<bufferCommandParamTypeEnum, Class> getParamMap() {
				return bufferCommandParamTypeEnum.getStringLongLongParamMap(); }
			  public Object invoke(IDeliveryBuffer dBuffer, 
						EnumMap<bufferCommandParamTypeEnum, String> inParams) {
					long start = new Long(inParams.get(bufferCommandParamTypeEnum.STARTTIMEINMILLIS)).longValue();
					long end = new Long(inParams.get(bufferCommandParamTypeEnum.ENDTIMEINMILLIS)).longValue();
					if ((dBuffer != null) && (inParams != null)) {
						return dBuffer.getParcels(inParams.get(bufferCommandParamTypeEnum.PLUGINID),start,end);
					}
				    return null;
			  }};
				
		/**
		 * The Buffer Command being overloaded
		 */
		private bufferCommandEnum method;		
		
		bufferMethodEnum(bufferCommandEnum method) {this.method = method;}
		
		/** 
		 * @return String representation of the overloaded Buffer Command 
		 */
		public String getName() {return method.getName();}	
		
		/**
		 * Return a Map representing the required Parameters for the overloaded method
		 * represented by this Enum and their corresponding Class Type
		 * 
		 * @return EnumMap<bufferCommandParamTypeEnum, Class>
		 * 
		 */
		public abstract EnumMap<bufferCommandParamTypeEnum, Class> getParamMap();
		
		
		/**
		 * Dynamic invocation of the overloaded method represented by this enum. 
		 * This ensures that the parameters and method name are validated before invocation. 
		 * 
		 * @param dBuffer Buffer to invoke method from
		 * @param EnumMap representing the parameters and value.
		 * 
		 * @return Return value from invoked method
		 *
		 */		
		public abstract Object invoke(IDeliveryBuffer dBuffer, 
				EnumMap<bufferCommandParamTypeEnum, String> inParams) throws PluginException;
	}
	
	/**
	 * Enum definitions for Buffer Command Parameters
	 * <p>Consumers querying {@link BufferPlugin} to use this enum to ensure correct syntax for query parameters
	 *    
	 * @author Rafi M Feroze
	 *
	 */
	public enum bufferCommandParamTypeEnum {
		/**
		 * String representation of parameter pluginID
		 */
		PLUGINID("pluginId")   { public Class getParamClass() { return String.class; } 
								 public boolean isValidValue(String p) { return (p != null)?true:false;}
								 public EnumMap<bufferCommandParamTypeEnum,Class> getParamMap(EnumMap<bufferCommandParamTypeEnum,Class> pMap){
									 pMap = validate(pMap);
									 pMap.put(PLUGINID, PLUGINID.getClass());
									 return pMap;
								 }},
		 /**
		  * String representation of parameter start Time Stamp in milliseconds
		  */
		STARTTIMEINMILLIS("startTimeInMillis")  { public Class getParamClass() { return long.class; } 
								 public boolean isValidValue(String p) {return (p != null)?true:false;} 
								 public EnumMap<bufferCommandParamTypeEnum,Class> getParamMap(EnumMap<bufferCommandParamTypeEnum,Class> pMap){
									 pMap = validate(pMap);
									 pMap.put(STARTTIMEINMILLIS, STARTTIMEINMILLIS.getClass());
									 return pMap;
								 }},
		 /**
		  * String representation of parameter end Time Stamp in milliseconds
		  */
		ENDTIMEINMILLIS("endTimeInMillis")  { public Class getParamClass() { return long.class; } 
								 public boolean isValidValue(String p) {return (p != null)?true:false;}
								 public EnumMap<bufferCommandParamTypeEnum,Class> getParamMap(EnumMap<bufferCommandParamTypeEnum,Class> pMap){
									 pMap = validate(pMap);
									 pMap.put(ENDTIMEINMILLIS, ENDTIMEINMILLIS.getClass());
									 return pMap;
								 }},
		 /**
		  * String representation of parameter body type
		  */
		BODYTYPE("bodyType")  { public Class getParamClass() { return Class.class; }
								public EnumMap<bufferCommandParamTypeEnum,Class> getParamMap(EnumMap<bufferCommandParamTypeEnum,Class> pMap){
									 pMap = validate(pMap);
									 pMap.put(BODYTYPE, BODYTYPE.getClass());
									 return pMap;
								 }
								public boolean isValidValue(String p) throws PluginException {
									getBodyClassInstance(p);
									return true;
								}};
		/**
		 * parameter name
		 */
		private String name;
		
		bufferCommandParamTypeEnum(String name) {this.name = name;}		
		
		/** 
		 * @return String representation of the Parameter 
		 */
		public String getName() {return name;}
		
		/**
		 * Return the parameter corresponding to the given string
		 * @param name	Parameter Name
		 * @return	Enum representing input Parameter Name
		 */
		public static bufferCommandParamTypeEnum getEnum(String name) {
			if (name != null) { 
				for(bufferCommandParamTypeEnum p : EnumSet.allOf(bufferCommandParamTypeEnum.class)) {
					if (p.getName().equals(name)) {
						return p;
					}
				}
			}
			return null;
		}
		
		/**
		 * Return the Class represented by this Parameter Enum
		 * @return Class represented by the Parameter
		 */
		public abstract Class getParamClass();
		
		/**
		 * Append Parameter Class to input Map, return a new Map if null
		 * @param pMap can be null
		 * @return Map with new entry for current Enum
		 */
		public abstract EnumMap<bufferCommandParamTypeEnum,Class> getParamMap(EnumMap<bufferCommandParamTypeEnum,Class> pMap);
		
		/**
		 * Check if the input String represents the valid Parameter value for the Enum. 
		 * In case of Java Objects extending bodyType, the input class name is checked for syntax 
		 * @param paramValue
		 * @return true if valid, else false
		 * @throws PluginException	in case of java objects, if the input Class Name cannot be instantiated
		 */
		public abstract boolean isValidValue(String paramValue) throws PluginException;
		
		/**
		 * Verify that the input String represents a valid parameter.
		 * @param name String representation of the parameter.
		 * @return true if valid, else false
		 */
		public static boolean isValid(String name) {
			if (name != null) { 
				for(bufferCommandParamTypeEnum p : EnumSet.allOf(bufferCommandParamTypeEnum.class)) {
					if (p.getName().equals(name)) {
						return true;
					}
				}
			}
			return false;
		}
		
		
		EnumMap<bufferCommandParamTypeEnum,Class> validate(EnumMap<bufferCommandParamTypeEnum,Class> pMap) {
			if (pMap == null) {
				 pMap = new EnumMap<bufferCommandParamTypeEnum,Class> (bufferCommandParamTypeEnum.class);		
			 }
			return pMap;
		}
		/**
		 * Return BodyType Class represented by the input String  
		 * @param className Name of a BodyType Class
		 * @return Class represented by the input String
		 * @throws PluginException
		 */
		public static Class<? extends BodyType> getBodyClassInstance(String className) throws PluginException {
			Class objClass;
			try {
				objClass = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new PluginException("Invalid ClassName : " + className);
			}
			Class<BodyType> retClass = null;
			try {
				retClass = objClass.asSubclass(BodyType.class);
			} catch (ClassCastException e) {
				throw new PluginException("Class " + className + " not subclass of BodyType: ");
			}
			return retClass;
		}
		
		/** 
		 * @return	Map containing PluginID Enum and its Class
		 */
		public static EnumMap<bufferCommandParamTypeEnum, Class> getStringParamMap() {
			return bufferCommandParamTypeEnum.PLUGINID.getParamMap(null);
		}
		
		/** 
		 * @return	Map containing Enums (PluginID, BodyType} and their respective Class
		 */
		public static EnumMap<bufferCommandParamTypeEnum, Class> getStringClassParamMap() {
			return bufferCommandParamTypeEnum.BODYTYPE.getParamMap(getStringParamMap());
		}
		
		/** 
		 * @return	Map containing Enums (PluginID, StartTime} and their respective Class
		 */
		public static EnumMap<bufferCommandParamTypeEnum, Class> getStringLongParamMap() {
			return bufferCommandParamTypeEnum.STARTTIMEINMILLIS.getParamMap(getStringParamMap());
		}
		
		/** 
		 * @return	Map containing Enums (PluginID, StartTime, EndTime} and their respective Class
		 */
		public static EnumMap<bufferCommandParamTypeEnum, Class> getStringLongLongParamMap() {
			return bufferCommandParamTypeEnum.ENDTIMEINMILLIS.getParamMap(getStringLongParamMap());
		}
		
		/** 
		 * @return	Map containing Enums (PluginID, BodyType, StartTime} and their respective Class
		 */
		public static EnumMap<bufferCommandParamTypeEnum, Class> getStringLongClassParamMap() {
			return bufferCommandParamTypeEnum.BODYTYPE.getParamMap(getStringLongParamMap());
		}
	}	
}
