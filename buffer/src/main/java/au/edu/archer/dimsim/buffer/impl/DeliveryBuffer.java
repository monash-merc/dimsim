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
import java.util.Hashtable;

import org.apache.log4j.Logger;
import au.edu.archer.dimsim.buffer.event.ParcelBufferedEvent;
import au.edu.archer.dimsim.buffer.event.RemovedFromBufferEvent;
import au.edu.archer.dimsim.buffer.pool.IBuffer;
import au.edu.archer.dimsim.buffer.pool.IBufferInput;
import au.edu.archer.dimsim.buffer.pool.impl.DummyBuffer;
import au.edu.archer.dimsim.buffer.pool.manager.IBufferPoolManager;
import au.edu.archer.dimsim.buffer.pool.manager.impl.MemoryBufferManager;

import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;

import au.edu.archer.dimsim.buffer.IDeliveryBuffer;

import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Dimsim implementation of IDeliveryBuffer interface.
 * 
 * @author Rafi M Feroze
 * @see IDeliveryBuffer
 */
public class DeliveryBuffer implements IDeliveryBuffer, ApplicationEventPublisherAware  {
	
	private static Logger log = Logger.getLogger(DeliveryBuffer.class);
	public static final int default_capacity = 100;
	public static final IBuffer dummyBuffer = new DummyBuffer();	
	
	private int capacity;
	private IBufferPoolManager buffMgr;    //can be set by Spring IOC
	protected Hashtable<String, IBuffer> parcelBuffers ; //can be set by Spring IOC
	private ApplicationEventPublisher publisher;
	
	
	public DeliveryBuffer() {
		this(default_capacity);
	}
	
	public DeliveryBuffer(int capacity) {
		this(new MemoryBufferManager(), capacity);
	}
	
	public DeliveryBuffer(IBufferPoolManager bMgr, int capacity) {
		if (capacity > 0) {
			this.capacity = capacity;		
		} else {
			capacity = default_capacity;
		}
		
		if (bMgr != null) {
			this.buffMgr = bMgr;
		} else {
			bMgr = new MemoryBufferManager();
		}
		
		parcelBuffers = new Hashtable<String, IBuffer>();
		
		
	}
	
	public IBufferPoolManager getBufferManager() {
		return this.buffMgr;
	}
	
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
	
	public void buffer(String senderID, IRegisteredParcel registeredParcel) {
		
		if (registeredParcel == null) return;
		
		//String senderID = ParcelUtils.getSender(registeredParcel);
		
		IBufferInput buffer = getPluginBuffer(senderID);		
		IRegisteredParcel removedParcel = buffer.add(registeredParcel);
		if (removedParcel != null) {
			CIMAUtil.publishEvent(log, this.publisher, new RemovedFromBufferEvent(registeredParcel,senderID));
		}
		CIMAUtil.publishEvent(log, this.publisher, new ParcelBufferedEvent(registeredParcel,senderID));
	}


	/**
	 * @param pluginId
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	private IBuffer getPluginBuffer(String pluginId) {
		IBuffer buffer = parcelBuffers.get(pluginId);
		if (buffer == null) {
			buffer = this.buffMgr.getNewBuffer(pluginId);			
			parcelBuffers.put(pluginId, buffer);			
		}
		return ((buffer != null)?buffer:DeliveryBuffer.dummyBuffer);
	}

	
	public int  getCapacity() {
		return this.capacity;
	}

	public int getSize(String pluginId) {
		return getPluginBuffer(pluginId).getSize();
	}

	public int getSize(String pluginId, long startTimeInMillis) {		
		return getPluginBuffer(pluginId).getSize(startTimeInMillis);
	}

	public int getSize(String pluginId, long startTimeInMillis,
			long endTimeInMillis) {
		
		return getPluginBuffer(pluginId).getSize(startTimeInMillis,endTimeInMillis);		
	}

	public ArrayList<IRegisteredParcel> getParcels(String pluginId) {

		return getPluginBuffer(pluginId).getParcels();
	}

	public ArrayList<IRegisteredParcel> getParcels(String pluginId,
			long startTimeInMillis) {
 
		return getPluginBuffer(pluginId).getParcels(startTimeInMillis);
	}

	public ArrayList<IRegisteredParcel> getParcels(String pluginId,
			long startTimeInMillis, long endTimeInMillis) {
		
		return getPluginBuffer(pluginId).getParcels(startTimeInMillis,endTimeInMillis);
	}

	public IRegisteredParcel getParcel(String pluginId, long timeInMillis) {

		return getPluginBuffer(pluginId).getParcel(timeInMillis);
	}

	public ArrayList<IRegisteredParcel> getParcels(String pluginId,
			Class<? extends BodyType> bodyType) {
		return BufferUtility.getParcels(getPluginBuffer(pluginId), bodyType);
	}

	public ArrayList<IRegisteredParcel> getParcels(String pluginId,
			long startTimeInMillis, Class<? extends BodyType> bodyType) {
		return BufferUtility.getParcels(getPluginBuffer(pluginId), startTimeInMillis, bodyType);		
	}

	public IRegisteredParcel getLastParcel(String pluginId,
			Class<? extends BodyType> bodyType) {
		
		return BufferUtility.getLastParcel(getPluginBuffer(pluginId), bodyType);
	}

}
