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
package au.edu.archer.dimsim.buffer.pool.impl.preGenerics;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import au.edu.archer.dimsim.buffer.impl.DeliveryBuffer;
import au.edu.archer.dimsim.buffer.pool.IBuffer;

/**
 * @author mrafi
 *
 */
public class MemoryRingBuffer implements IBuffer {
	static Logger log = Logger.getLogger(MemoryRingBuffer.class);
	
	private int head, nextTail, size, capacity, currTail = -1;	 
	private IRegisteredParcel[] buffer;	
	
	/**
	 * 
	 */
	public MemoryRingBuffer() {
		this(DeliveryBuffer.default_capacity);
	}
	
	public MemoryRingBuffer(int capacity) {
		 
		if (capacity > 0) this.capacity = capacity;
		else this.capacity = DeliveryBuffer.default_capacity;
		
		buffer = new IRegisteredParcel[this.capacity];			
		head = 0; nextTail = 0; size=0; currTail = -1;
	}
	 
	synchronized public void clear() {
		synchronized(buffer) {
			buffer = new IRegisteredParcel[this.capacity];			
			head = 0; nextTail = 0; size = 0; currTail = -1;
		}
	}
	
	public int getCapacity() {				
		return this.capacity;		
	}	
	

	public int getCurrTail() {
		return currTail;
	}	
	
	private long getHeadTS() {
		if (currTail != -1) {
			return buffer[this.head].getCreationTime().getTimeInMillis(); 
		}
		return 0;
	}
	
	public IRegisteredParcel getHead() {		
		if (size > 0) return buffer[head];		
		else return null;		
	}	
	// Note : Could not implement getTail();
	
	/**
	 * @return
	 */
	protected IRegisteredParcel[] getBuffer() {
		synchronized(buffer) {
 			return buffer.clone();
 		}
	}
	
	private interface subArray { public void fillSubArray(int first, int last); }
	protected ArrayList<IRegisteredParcel> getBuffer(int start, int end) {
		
		//log.trace("getBuffer() start,end, this.size() :"+ start + "," + end + "," + this.size);
		
		final ArrayList<IRegisteredParcel> retArr = new ArrayList<IRegisteredParcel>(); 
		
		if ((start < 0) || (start >= this.size) || (end < 0) || (end >= this.size)) {
			return retArr;
		}
		
		final IRegisteredParcel[] bufCopy = getBuffer();		
	
		final subArray innerClass = new subArray() {
			public void fillSubArray(int first,int last) {				
				for (int i = first; i <= last; i++) {
					//log.trace("adding " + i + ", time = " + bufCopy[i].getCreationTime().getTimeInMillis());
					retArr.add(bufCopy[i]);
				}			
		  }
		};		
		
		if (start > end) {
			innerClass.fillSubArray(start, this.capacity -1);
			innerClass.fillSubArray(0,end);
		}
		else {			
			innerClass.fillSubArray(start,end);
		}
		
		return retArr;		
	}
	
	public int getSize() { return size; }
	
	public int getSize(long startTimeInMillis) {		
	
		if (getHeadTS() >= startTimeInMillis) {  //
			return this.size;
		} 
		
		int start = binarySearchInRingBuffer(buffer, startTimeInMillis, 0, this.size - 1);		
		if (start == -1 ) { //Buffer Empty
			return 0; 
		}		

		int retVal = MemoryRingBuffer.getSize(start,this.nextTail,this.capacity);

		return retVal;
	}
	
	public int getSize(long startTimeInMillis, long endTimeInMillis) {		
		
		if (startTimeInMillis > endTimeInMillis)  return getSize(startTimeInMillis);
		
		if (getHeadTS() > endTimeInMillis)  {
			return 0;
		} else if (getHeadTS() == endTimeInMillis) {
			return 1;
		}
		
		int end = binarySearchInRingBuffer(buffer, endTimeInMillis, 0, this.size - 1);
		if (end == -1) { return 0; }   //Buffer Empty
		
		int start = binarySearchInRingBuffer(buffer, startTimeInMillis, 0, this.size - 1);
		if (start == -1) { return 0; } //Buffer Empty. Could happen if buffer cleared after search for end.
						
		if (start == end) {
			if (endTimeInMillis == buffer[end].getCreationTime().getTimeInMillis()) { return 1; }
			else return 0; 
		}
		else {
			//log.trace("getSize(long,long) : start,end = " + start + "," + end);
			//log.trace("startTime,endTime = " + startTimeInMillis + "," + endTimeInMillis);
			int retVal = MemoryRingBuffer.getSize(start,end,this.capacity);
			if (buffer[end].getCreationTime().getTimeInMillis() == endTimeInMillis) {
				//The end returned by BinarySearch is not equivalent to 'nextTail' used in getSize(int,int,int) method above.
				retVal = retVal + 1;  //increment retVal to compensate for the element in 'end' index of the buffer.
			}
			return retVal;
		}
	}
	
	public static int getSize(int startIndex, int nextTailIndex, int maxSize) {
		int retSize;
		
		retSize = nextTailIndex - startIndex ;
		
		if (retSize <= 0) {	//this will fail when buffer is empty
			retSize += maxSize;
		} 
		
		return retSize;
	}

	synchronized public IRegisteredParcel add(IRegisteredParcel registeredParcel) {		
		IRegisteredParcel oldParcel = null;		
		
		synchronized(buffer) {
			
			this.currTail = nextTail;
			buffer[currTail] = registeredParcel;
			
			nextTail = (nextTail + 1) % capacity;
			//Move head if buffer full			
			if (this.size == this.getCapacity()) {	
				oldParcel = buffer[head];
				head = (head + 1) % capacity;				
			} else {
				size = size + 1;
			}
			
		}				
		
		return oldParcel;		
	}

	//Adapted version of mirko's BinarySearch from URL : http://blog.raner.ws/wordpress/?p=52
	//This search returns the index of a element equal or greater than the search value.
	public int binarySearchInRingBuffer( IRegisteredParcel[] array, long timeInMillis, int start, int end) {		
			       
        if (start > end) {
            return -1; // item not found
        }
        
        //int middle = (start&end)+(start^end)>>1;  //from comment by Schubsi @http://blog.raner.ws/wordpress/?p=52
        int middle = (start + end) / 2;
        
        //log.trace("BinaySearch  Start,Middle,End  = " + start+" , "+ middle +" , " + end);
        
        long startTime =  array[start].getCreationTime().getTimeInMillis();        
        if ( startTime == timeInMillis) {
        	//log.trace("Returning = " + start);
        	return start;
        }
        
        long endTime =  array[end].getCreationTime().getTimeInMillis();        
        if ( endTime == timeInMillis) {
        	//log.trace("Returning end = " + end);
        	return end;
        }
        
        long middleTime = array[middle].getCreationTime().getTimeInMillis();
        //log.trace("BinaySearch  searching for, startTime, middleTime = " + timeInMillis + " , " + startTime + "," + middleTime);
        
        if (middleTime == timeInMillis) {
        	//log.trace("Returning middle = " + middle);
            return middle;
        }
        
        if (startTime  < middleTime) {
        // => There is no wrap-around in left half of the search area:

            if (startTime < timeInMillis && timeInMillis < middleTime) {
            // => The sought element must be in between the start and the middle:
            	//log.trace("Time inBetween start and middle ");
                return(Math.max(binarySearchInRingBuffer(array, timeInMillis, start+1, middle),start + 1));
                
            }

            //log.trace("time Not Inbetween start and middle ");
            // => search the second half:
            int retVal = binarySearchInRingBuffer(array, timeInMillis, middle+1, end - 1);
            
            //If the binarySearch returns -1, it means that the value is not in buffer.
            //Since startTime is less than MiddleTime, this means that the searched value is 
            //	- either less than both start and middle, in which case return index of the head element of the buffer
            //  - or the element middle+1 is the first element greater than search value. 
            if (retVal == -1 ) {
            	if (timeInMillis < startTime) {	
            		retVal = this.head;         		
            	} else {
            		retVal = middle + 1;
            	}
            }
            return retVal;                    
        }

        // => The wrap around is in the left half of the search area (array[start]>array[middle]        
        if (middleTime < timeInMillis && timeInMillis < endTime) {
        	//log.trace("time Inbetween middle and end ");
            // => the number must be to the right of the middle see [1]
            return (Math.max(binarySearchInRingBuffer(array, timeInMillis, middle+1, end - 1),middle + 1));
        }

        //log.trace("time Not Inbetween middle and end ");
        // => the number must be to the left of the middle:
        int retVal = binarySearchInRingBuffer(array, timeInMillis, start+1, middle -1);
        if (retVal == -1) {
        	if (timeInMillis < middleTime) {	
        		retVal = this.head;         		
        	} else {
        		retVal = middle;
        	}	
        }
        return retVal;
    }
	
	public ArrayList<IRegisteredParcel> getParcels() {		 
 		return getBuffer(this.head,this.getCurrTail());
	}

	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis) {
		if (getHeadTS() >= startTimeInMillis) {
			return getParcels();
		}
		
		int start = binarySearchInRingBuffer(buffer, startTimeInMillis, 0, this.size - 1);
		return getBuffer(start,this.getCurrTail());
	}

	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis,
			long endTimeInMillis) {
		
		if (startTimeInMillis > endTimeInMillis) {
			return getParcels(startTimeInMillis);
		} else if (getHeadTS() > endTimeInMillis)  {
			return null;
		} else if (getHeadTS() == endTimeInMillis) {
			return getBuffer(this.head,this.head);
		}
		
		int start = binarySearchInRingBuffer(buffer, startTimeInMillis, 0, this.size - 1);
		int end = binarySearchInRingBuffer(buffer, endTimeInMillis, 0, this.size - 1);
		//log.trace("getParcelsLongLong : start,end =" + start + "," + end);
		return getBuffer(start,end);
	}

	public IRegisteredParcel getParcel(long timeInMillis) {
		if (getHeadTS() <= timeInMillis) {		
			int start = binarySearchInRingBuffer(buffer, timeInMillis, 0, this.size - 1);
			ArrayList<IRegisteredParcel> list = getBuffer(start,start);
			if ((list.size() > 0) 
			  && (list.get(0).getCreationTime().getTimeInMillis() == timeInMillis)) {
				return list.get(0);
			}
		}
		
		return null;
	}
	
}
