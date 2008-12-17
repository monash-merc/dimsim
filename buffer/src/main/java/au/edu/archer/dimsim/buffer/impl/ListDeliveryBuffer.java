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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.SessionType;
import org.instrumentmiddleware.cima.session.ISessionManager;

import au.edu.archer.dimsim.buffer.IListDeliveryBuffer;
import au.edu.archer.dimsim.buffer.pool.manager.IBufferPoolManager;
import au.edu.archer.dimsim.buffer.util.DimsimUtils;

/**
 * Dimsim implementation of IListDeliveryBuffer interface.
 * <p>
 * @author Rafi M Feroze
 * @see IListDeliveryBuffer
 */
public class ListDeliveryBuffer extends DeliveryBuffer implements
		IListDeliveryBuffer {	
	private static Logger log = Logger.getLogger(ListDeliveryBuffer.class);
	public ListDeliveryBuffer() {
		super();
	}
	
	public ListDeliveryBuffer(int capacity) {
		super(capacity);
	}
	
	public ListDeliveryBuffer(IBufferPoolManager bMgr, int capacity) {
		super(bMgr,capacity);
	}
	
	/* (non-Javadoc)	 * 
	 * @see au.edu.archer.dimsim.buffer.IListDeliveryBuffer#getList()
	 */
	public Set<Map<BufferListFields, Object>> getList() {
		Set<Map<BufferListFields, Object>> retValue = new HashSet<Map<BufferListFields, Object>> ();
		
		for(String pluginId : this.parcelBuffers.keySet()) {
			HashMap<BufferListFields, Object> buffRec = new HashMap<BufferListFields, Object> ();
			buffRec.put(BufferListFields.PLUGINID, pluginId);
			buffRec.put(BufferListFields.TYPE, "Plugin");
			buffRec.put(BufferListFields.SIZE, getSize(pluginId) + "");	
			retValue.add(buffRec);
		}
		return retValue;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IListDeliveryBuffer#getList(java.lang.String)
	 */
	public Set<Map<BufferListFields, Object>> getList(String pluginId) {
		Set<Map<BufferListFields, Object>> retValue = new HashSet<Map<BufferListFields, Object>> ();
		
		for(IRegisteredParcel parcel : getParcels(pluginId)) {
			HashMap<BufferListFields, Object> buffRec = new HashMap<BufferListFields, Object> ();
			buffRec.put(BufferListFields.PLUGINID, pluginId);
			buffRec.put(BufferListFields.TYPE, parcel.getType().toString());
			buffRec.put(BufferListFields.CREATETIME, parcel.getCreationTime());			
			if (parcel.isSetSecurity()) {
				buffRec.put(BufferListFields.ACCESS, parcel.getSecurity().getClass().getSimpleName());
			}			
			if ( parcel.isSetBody()) {
				buffRec.put(BufferListFields.TYPE, parcel.getBody().getClass().getSimpleName());
			}
			
			if (parcel.isSetSessions()) {				
				Set<String> consumerIDs = new HashSet<String>();
				buffRec.put(BufferListFields.CONSUMERID, consumerIDs);
				try {					
					ISessionManager sMgr = DimsimUtils.getSessionManager();
					SessionType[] sTypeArr = parcel.getSessions().getSessionArray();
					for(SessionType sType : sTypeArr) {						
						consumerIDs.add(DimsimUtils.getConsumerID(sMgr.getSession(sType.getSessionId())));
					}		
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			retValue.add(buffRec);
		}
		return retValue;
	}

}
