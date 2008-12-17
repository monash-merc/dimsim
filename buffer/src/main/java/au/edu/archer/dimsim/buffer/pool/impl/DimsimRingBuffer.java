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

package au.edu.archer.dimsim.buffer.pool.impl;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.impl.RegisteredParcel;
import org.instrumentmiddleware.cima.util.ParcelUtil;

import au.edu.archer.dimsim.buffer.pool.IBuffer;
import au.edu.archer.dimsim.buffer.pool.store.IStore;
import au.edu.archer.dimsim.buffer.util.ParcelCreateTimeComparator;

/**
 * @author mrafi
 *
 */
public class DimsimRingBuffer implements IBuffer {
	static Logger log = Logger.getLogger(DimsimRingBuffer.class);
	static ParcelCreateTimeComparator parcelComparator = new ParcelCreateTimeComparator();
	
	private AbstractRingBuffer<IRegisteredParcel> sortedInputRingBuffer;
	
	public DimsimRingBuffer() {
		this((AbstractRingBuffer<IRegisteredParcel>)null);			
	}
		
	public DimsimRingBuffer(IStore<IRegisteredParcel> bufStore) {
		this(new FIFORingBuffer<IRegisteredParcel> (bufStore));	
	}
	
	public DimsimRingBuffer(AbstractRingBuffer<IRegisteredParcel> ringBuffer) {
		this.sortedInputRingBuffer = ringBuffer;
		if (ringBuffer == null) {
			this.sortedInputRingBuffer = new FIFORingBuffer<IRegisteredParcel> (); 
		}	
	}
	
	public AbstractRingBuffer<IRegisteredParcel> getRingBuffer() {
		return this.sortedInputRingBuffer;
	}
	
	public int getSize(long startTimeInMillis) {
		//log.debug("getSize for time = " + startTimeInMillis);
		AbstractRingBuffer<IRegisteredParcel> bufCopy = getRingBuffer().clone();		
		
		if (parcelComparator.compare(bufCopy.getHead(),startTimeInMillis) >= 0) {  
			return bufCopy.getSize();
		} 
		
		final IRegisteredParcel searchParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel());
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTimeInMillis);		
		searchParcel.setCreationTime(cal);
		
		int start = SortedRingBufferUtils.binarySearchFirst(bufCopy, parcelComparator, searchParcel, 0, bufCopy.getSize() - 1);		
		if (start == -1 ) { //Buffer Empty
			return 0; 
		}		

		int retVal = AbstractRingBuffer.getSize(start,bufCopy.getCurrTailIndex(),bufCopy.getCapacity());

		return retVal;
	}
	
	public int getSize(long startTimeInMillis, long endTimeInMillis) {		
		
		if (startTimeInMillis > endTimeInMillis)  return getSize(startTimeInMillis);
		
		AbstractRingBuffer<IRegisteredParcel> bufCopy = getRingBuffer().clone();		
		
		IRegisteredParcel searchParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel());
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(endTimeInMillis);		
		searchParcel.setCreationTime(cal);
		
		int end = SortedRingBufferUtils.binarySearchLast(bufCopy, parcelComparator, searchParcel, 0, bufCopy.getSize() - 1);
		if (end == -1) { return 0; }   //Buffer Empty
		
		cal.setTimeInMillis(startTimeInMillis);		
		searchParcel.setCreationTime(cal);		
		int start = SortedRingBufferUtils.binarySearchFirst(bufCopy, parcelComparator, searchParcel, 0, bufCopy.getSize() - 1);
		if (start == -1) { return 0; } //Buffer Empty. Could happen if buffer cleared after search for end.
						
		if (start == end) { //Element returned by binarySearch is equal-to or greater than input value.
			if (parcelComparator.compare(bufCopy.getStore().get(end), endTimeInMillis) == 0) {
				return 1; 
			}
			else return 0; 
		}
		else {
			//log.debug("getSize(long,long) : start,end = " + start + "," + end);
			//log.trace("startTime,endTime = " + startTimeInMillis + "," + endTimeInMillis);
			int retVal = AbstractRingBuffer.getSize(start,end,bufCopy.getCapacity());
			/*if (parcelComparator.compare(bufCopy.getStore().get(end), endTimeInMillis) == 0) {			
				//The end returned by BinarySearch is not equivalent to 'fields.nextTail' used in getSize(int,int,int) method above.
				retVal = retVal + 1;  //increment retVal to compensate for the element in 'end' index of the buffer.
			}*/
			return retVal;
		}		
		
	}	
	
	public ArrayList<IRegisteredParcel> getParcels() {	
		//AbstractRingBuffer<IRegisteredParcel> bufCopy = getRingBuffer().clone();
 		//return AbstractRingBuffer.getBuffer(bufCopy, bufCopy.getFields().head,bufCopy.getCurrTailIndex());
		return getRingBuffer().getElements();
	}

	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis) {
		AbstractRingBuffer<IRegisteredParcel> bufCopy = getRingBuffer().clone();
		
		if (parcelComparator.compare(bufCopy.getHead(),startTimeInMillis) > 0) {
			//requested start Parcel has  been erased from buffer, return whatever is left in the buffer
			return getParcels();
		}
		
		final IRegisteredParcel searchParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel());
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTimeInMillis);		
		searchParcel.setCreationTime(cal);
		
		int start = SortedRingBufferUtils.binarySearchFirst(bufCopy, parcelComparator, searchParcel, 0, bufCopy.getSize() - 1);
		return AbstractRingBuffer.getElements(bufCopy, start,bufCopy.getCurrTailIndex());
	}

	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis,
			long endTimeInMillis) {
		
		AbstractRingBuffer<IRegisteredParcel> bufCopy = getRingBuffer().clone();
		
		IRegisteredParcel searchParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel());
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(startTimeInMillis);		
		searchParcel.setCreationTime(cal);
		
		if (startTimeInMillis > endTimeInMillis) {
			return getParcels(startTimeInMillis);
		} else {
			int endCompare = parcelComparator.compare(bufCopy.getHead(),endTimeInMillis);
			if (endCompare > 0){
				//Requested end Parcel is no longer in buffer, so will be everything less than it, return null
				return null;
			} else if (endCompare == 0) {
				//head parcel matches endTime, check subsequent parcels to get the end element to copy
				int startCopy = bufCopy.getFields().head;
				int endCopy = SortedRingBufferUtils.getLastLocation(bufCopy, parcelComparator, searchParcel, startCopy);
				return AbstractRingBuffer.getElements(bufCopy, startCopy, endCopy);
			}
		}		
		
		int start = SortedRingBufferUtils.binarySearchFirst(bufCopy, parcelComparator, searchParcel, 0, bufCopy.getSize() - 1);
		
		cal.setTimeInMillis(endTimeInMillis);		
		searchParcel.setCreationTime(cal);		
		int end = SortedRingBufferUtils.binarySearchLast(bufCopy, parcelComparator, searchParcel, 0, bufCopy.getSize() - 1);
		return AbstractRingBuffer.getElements(bufCopy,start,end);
	}

	public IRegisteredParcel getParcel(long timeInMillis) {		
		
		AbstractRingBuffer<IRegisteredParcel> bufCopy = getRingBuffer().clone();
		
		if (parcelComparator.compare(bufCopy.getHead(),timeInMillis) <= 0) {		
			final IRegisteredParcel searchParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel());
			final Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timeInMillis);		
			searchParcel.setCreationTime(cal);
		
			int start = SortedRingBufferUtils.binarySearchFirst(bufCopy, parcelComparator, searchParcel, 0, bufCopy.getSize() - 1);
			ArrayList<IRegisteredParcel> list = AbstractRingBuffer.getElements(bufCopy,start,start);
			if ((list.size() > 0) 
			  && (list.get(0).getCreationTime().getTimeInMillis() == timeInMillis)) {
				return list.get(0);
			}
		}
		
		return null;		
	}

	public int getSize() {
		return getRingBuffer().getSize();
	}

	public IRegisteredParcel add(IRegisteredParcel registeredParcel) {

		return (IRegisteredParcel) getRingBuffer().add(registeredParcel);
	}
}
