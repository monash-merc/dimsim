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
package au.edu.archer.dimsim.buffer.impl;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;

import au.edu.archer.dimsim.buffer.pool.IBuffer;

/**
 * Static Utlity functions used in Dimsim.
 * <p> 
 * @author Rafi M Feroze
 *
 */
public class BufferUtility {
	private static Logger log = Logger.getLogger(BufferUtility.class);
	
	/**
	 * Return buffered parcels of given BodyType only.
	 * @param buffer to query parcels 
	 * @param bodyType interested in
	 * @return a list of parcels from the buffer that match input body type class.
	 */
	public static ArrayList<IRegisteredParcel> getParcels(IBuffer buffer, Class<? extends BodyType> bodyType) {
		if (buffer != null) { 
			return filter(bodyType, buffer.getParcels());
		}
		
		return getEmptyParcelList();
	}
	
	/**
	 * Return Buffered parcels of given BodyType that has a create timestamp greater than
	 * or equal to input value
	 * <p>
	 * @param buffer	buffer to query parcels
	 * @param startTimeInMillis	parcel creation start time
	 * @param bodyType	bodytype interested in
	 * @return a list of parcels from the buffer that match input body type and has a creation time 
	 * 			greater than or equal input start time.
	 */
	public static ArrayList<IRegisteredParcel> getParcels(IBuffer buffer, long startTimeInMillis,
			Class<? extends BodyType> bodyType) {
		
		if (buffer != null) { 
			return filter(bodyType, buffer.getParcels(startTimeInMillis));
		}
		
		return getEmptyParcelList();	
	}
	

	private static ArrayList<IRegisteredParcel> filter(Class<? extends BodyType> bodyType,
			ArrayList<IRegisteredParcel> allVal) {
		
		ArrayList<IRegisteredParcel> retVal = getEmptyParcelList(); 
			
		log.debug("Checking for Class " + bodyType.getName());
		log.debug("   Buffer Size =  " + allVal.size());
		
		if (allVal != null) {
			//String bodyTypeClassName = bodyType.getClass().getName();
			for (IRegisteredParcel p : allVal) {
				BodyType b = p.getBody();				
				if (b != null) {	
					log.debug("got Class " + b.getClass().getName());
					Class<? extends BodyType> pBodyClass = b.getClass();
					if (pBodyClass.equals(bodyType)) {
						retVal.add(p);
					}/* else {
						String pBodyClassName = pBodyClass.getName();
						if (pBodyClassName.endsWith("Impl")) {
							if (pBodyClassName.equals(bodyTypeClassName + "Impl")) {
								retVal.add(p);
							}
						}					
					}*/
				}
			}
		}
		
		return retVal;
	}

	private static ArrayList<IRegisteredParcel> getEmptyParcelList() {
	
		ArrayList<IRegisteredParcel> retVal = new ArrayList<IRegisteredParcel> ();
	
		return retVal;
	
	}

	/**
	 * 
	 * @param pluginBuffer	Buffer to get parcels from
	 * @param bodyType	of interest
	 * @return Last Buffered Parcel that matches input body type. 
	 */
	public static IRegisteredParcel getLastParcel(IBuffer pluginBuffer,
			Class<? extends BodyType> bodyType) {
		ArrayList<IRegisteredParcel> allParcels = getParcels(pluginBuffer,bodyType);
		int size = allParcels.size();
		if (size > 0) {
			return allParcels.get(size -1);
		}
		
		return null;
	}
}
