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
package au.edu.archer.dimsim.buffer.pool.store;
/*
 * Store provides and manages the underlying physical resource in which buffer data will be stored.
 * Example of stores include Memory Arrays, Memory Databases, FileSystems, Relational Datastores etc.
 */
public interface IStore<T> {	
	
	public final static int default_capacity = 999;	
	
	public int getSize();  //Return the number of objects stored
	//public void setSizer(IStoreSizer storeSizer); //Allows for size to be determined by an external party. 
	public T get(int index);  // Return the object stored at the corresponding index value
	public int getCapacity(); //Return the maximum number of IRegisteredParcel objects that can be stored by this Store
	public T put(int index, T t); //Place input Object at the specified index and return existing object
	public void clear(); //return oldStore and set new . used for Clearing the store.
	public IStore<T> clone(); //Return a clone of the current store that is an image frozen at invocation time.
}
