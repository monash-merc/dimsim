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
import java.util.Comparator;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.util.ParcelUtil;

import au.edu.archer.dimsim.buffer.pool.impl.AbstractRingBuffer;
import au.edu.archer.dimsim.buffer.pool.impl.RandomInputSortedOutputRingBuffer;
import au.edu.archer.dimsim.buffer.util.ParcelCreateTimeComparator;


/**
 * @author mrafi
 *
 * The purpose of this class is to provide consumer plug-ins the ability to locally buffer
 * incoming parcels, while they are waiting to be processed. 
 */
public abstract class DimsimBufferedConsumerPlugin extends DimsimConsumerPlugin {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DimsimBufferedConsumerPlugin.class);
	
	private AbstractRingBuffer<Parcel> localBuffer;  //local store for incoming parcels waiting to be processed.

	
	protected DimsimBufferedConsumerPlugin(String id) throws Exception {
		super(id);		
		resetLocalBuffer();		
	}	
	
	protected ArrayList<Parcel> getConsumerBufferElements() {
		return localBuffer.getElements();
	}	
		
	synchronized protected void buffer(Parcel incomingParcel) {		
		if (incomingParcel != null) {			
			if (localBuffer == null) {
				resetLocalBuffer();
			}
			synchronized(localBuffer) {
				this.localBuffer.add(incomingParcel);
			}
		}
	}	
	
	//Use this method to retrieve all saved Parcels. Note this will reset the buffer.
	//To obtain size, or parcels without reset, 
	//	- use the protected variable localBuffer directly.
	//	- be wary of synchronization problems with the localBuffer variable.	
	synchronized protected ArrayList<Parcel> resetLocalBuffer() {
		ArrayList<Parcel> bufParcels = null;		
		
		Comparator<Parcel> comparator = new ParcelCreateTimeComparator();
		if (localBuffer != null) {
			synchronized(localBuffer) {
				bufParcels = getConsumerBufferElements();
				localBuffer = new RandomInputSortedOutputRingBuffer<Parcel> (comparator);
			} 
		} else {
			synchronized(this) {
				localBuffer = new RandomInputSortedOutputRingBuffer<Parcel> (comparator);	
			}
		}		
		
		return bufParcels;
	}
	
	protected boolean isLocalBufferEmpty() {
		
		if (localBuffer == null) return true;	
		
		return ((localBuffer.getSize() > 0)?false:true);
	}
	
	public static Parcel generateParcelForLocalBuffer(BodyType body,
		    Calendar creationTime, ParcelTypeEnum.Enum type, ISession session) {
		
		Parcel parcel = ParcelUtil.newParcel(type);
		parcel.setCreationTime(creationTime);
		parcel.addNewSessions().addNewSession().setSessionId(session.getId());
		parcel.setBody(body);
		
		return parcel;
	}
	
}
