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

package au.edu.archer.dimsim.buffer.pool.store.impl;

import org.apache.log4j.Logger;
import au.edu.archer.dimsim.buffer.pool.store.IStore;

/**
 * @author mrafi
 *
 */
public class MemoryStore<T> implements IStore<T>, Cloneable {
	private int capacity;
	private T[] store;	
	
	static Logger log = Logger.getLogger(MemoryStore.class);
	
	private MemoryStore() { //dummy constructor used internally for clone operation.		
	}
	
	@SuppressWarnings("unchecked")
	public MemoryStore(int capacity) {
		this.capacity = capacity;
		if (this.capacity < 1) {
			this.capacity = IStore.default_capacity;
		}
		
		this.store = (T[]) (new Object[this.capacity]); 
	}
	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.store.IStore#put(int, Object)
	 */
	synchronized public T put(int index, T obj) {		
		
		T retVal = this.get(index);		
		if ((index >=0) && (index < getSize())) {
			synchronized(store) {
				store[index] = obj;				
			}			
		}		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.store.IStore#clear()
	 */
	@SuppressWarnings("unchecked")
	synchronized public void clear() {		
		synchronized (store) {			
			store = (T[]) (new Object[this.capacity]); 
		}
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.store.IStore#get(int)
	 */
	public T get(int index) {
		T retVal = null;
		
		if ((index >= 0) && (index < getSize())) {
			retVal = store[index];		
		}
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.store.IStore#getCapacity()
	 */
	public int getCapacity() {
		return this.capacity;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.store.IStore#getSize()
	 */
	public int getSize() {		
		return store.length; //same a capacity because this is a memory Array.
	}
		
	public IStore<T> clone() {
		MemoryStore<T> retStore = new MemoryStore<T>();
		retStore.store = this.store.clone();		
		retStore.capacity = this.capacity;
		return retStore;
	}
	
}
