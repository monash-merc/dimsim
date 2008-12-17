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
import org.apache.log4j.Logger;

import au.edu.archer.dimsim.buffer.pool.store.IStore;
import au.edu.archer.dimsim.buffer.pool.store.impl.MemoryStore;

/**
 * @author mrafi
 *
 */
public abstract class AbstractRingBuffer<T> implements Cloneable{
	static Logger log = Logger.getLogger(AbstractRingBuffer.class);
	
	class RingBufferFields  {
		public int head, currTail = -1; //nextTail, size, currTail = -1;
		
		RingBufferFields() {
			this(0,-1);			
		}
		/*public RingBufferFields(int h, int nT, int s, int cT) {
			head = h; nextTail = nT; size = s; currTail = cT;
		}*/
		public RingBufferFields(int h, int cT) {
			head = h; currTail = cT;
		}
		public RingBufferFields clone() {
			return new RingBufferFields(head,currTail);
		}
		
		public String getFields() {
			return ("headIndex = " + head				   
				   +"\tcurrTailIndex = " + currTail
				   +"\tnBufferSize = " + getSize());
		}
	}
	
	protected IStore<T> buffer;	
	protected RingBufferFields fields;
	protected boolean bufferModified;
	private AbstractRingBuffer<T> rClone;
	
	abstract public T add(T obj);
	abstract protected AbstractRingBuffer<T> getNewInstance(IStore<T> store);
	
	
	public AbstractRingBuffer() {
		this (null);
	}
	
	public AbstractRingBuffer(IStore<T> bufStore) {
		
		buffer = bufStore;	
		if (buffer == null) {
			buffer = new MemoryStore<T>(IStore.default_capacity);
		}
		fields = new RingBufferFields();
		bufferModified = true;	
	}
	
	synchronized public void clear() {		
		synchronized (this.fields) {
			buffer.clear();	
			bufferModified = true;		
			fields = new RingBufferFields();
		}
	}
	
	protected void setFields(RingBufferFields fields) {
		if (fields != null) {
			this.fields = fields;
		}
	}	
	
	protected IStore<T> getStore() {
		return this.buffer;
	}
	
	protected RingBufferFields getFields() {
		return this.fields;
	}
	
	public int getSize() {
		return getSize(getHeadIndex(),getCurrTailIndex(),getCapacity());		
	}
	
	public int getCapacity() {				
		return buffer.getCapacity();		
	}		

	protected int getCurrTailIndex() {
		return fields.currTail;
	}	

	protected int getHeadIndex() {
		return fields.head;
	}	
	
	public T getHead() {		
		if (getSize() > 0) {			
			return buffer.get(fields.head);			
		}		
		return null;		
	}
	
	// Note : Could not implement getTail(), as this could change more rapidly that head,
	//        use getRingElement method next instead;
	
	public T getRingElement(int index) {		
		if (index < getSize()) {						
			int pos = (getHeadIndex() + index) % getCapacity();
			return buffer.get(pos);
		}
		return null;
	}
	
	
	
	//Static methods 
	 
	public static int getSize(int startIndex, int endIndex, int maxSize) {
		int retSize = 0;
		
		if (endIndex > -1) { //Negative endIndex implies Buffer is emptyy

			retSize = endIndex - startIndex + 1;
		
			if (retSize <= 0) { //when endIndex < startIndex
				retSize += maxSize;
			} 
	
			//log.debug("getSize : start,end,max,return: " + startIndex + " , " + endIndex + " , " + maxSize + " , " + retSize);	
		}
		return retSize;
	}
	
	private interface subArray { public void fillSubArray(int first, int last); }
	protected static<T> ArrayList<T> getElements(final AbstractRingBuffer<T> bufCopy, int start, int end) {		
		//log.trace("getBuffer() start,end, this.size() :"+ start + "," + end + "," + this.size);		
		
		final ArrayList<T> retArr = new ArrayList<T>(); 
		
		if ((start < 0) || (start >= bufCopy.getSize()) || (end < 0) || (end >= bufCopy.getSize())) {
			return retArr;
		}		
	
		final subArray innerClass = new subArray() {
			public void fillSubArray(int first,int last) {				
				for (int i = first; i <= last; i++) {
					//log.trace("adding " + i + ", time = " + bufCopy[i].getCreationTime().getTimeInMillis());
					retArr.add(bufCopy.buffer.get(i));
				}			
		  }
		};		
		
		if (start > end) {
			innerClass.fillSubArray(start, bufCopy.getCapacity() -1);
			innerClass.fillSubArray(0,end);
		}
		else {			
			innerClass.fillSubArray(start,end);
		}
		
		return retArr;		
	}

	public static int getNextElementPos(int currPos, int maxCapacity) {
		return ((currPos + 1) % maxCapacity);		
	}	

	
	public static int getPrevElementPos(int currPos, int maxCapacity) {
		int retVal = currPos - 1;
		
		if (retVal < 0) {
			retVal = maxCapacity -1 ; 
		}
		return retVal;
	}	
	
	protected AbstractRingBuffer<T> clone() {
		if (this.bufferModified) {	
			synchronized(this.fields) {
				rClone = getNewInstance(this.buffer.clone());
				rClone.setFields(this.fields.clone());
				this.bufferModified = false;
			}
		}
		
		return rClone;
	}
	
	public ArrayList<T> getElements() {
		ArrayList<T> retElements = new ArrayList<T> ();
		AbstractRingBuffer<T> copy = clone();		
		
		int size = copy.getSize();
		if (size > 0) {
			for(int i = 0; i < size; i++) {
				retElements.add(copy.getRingElement(i));
			}
		}
		
		return retElements;
	}
 }
