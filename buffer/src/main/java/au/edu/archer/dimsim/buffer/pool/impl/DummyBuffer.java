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

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;

import au.edu.archer.dimsim.buffer.pool.IBuffer;

/**
 * @author mrafi
 * A dummy implementation that does nothing to store data. 
 * Useful in scenarios where no buffering is required. 
 */
public class DummyBuffer implements IBuffer {

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#add(org.instrumentmiddleware.cima.parcel.IRegisteredParcel)
	 */
	public IRegisteredParcel add(IRegisteredParcel registerdParcel) {
		return null;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getParcels()
	 */
	public ArrayList<IRegisteredParcel> getParcels() {
		return null;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getParcels(long)
	 */
	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis) {
		return null;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getParcels(long, long)
	 */
	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis,
			long endTimeInMillis) {
		return null;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getSize()
	 */
	public int getSize() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getSize(long)
	 */
	public int getSize(long startTimeInMillis) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getSize(long, long)
	 */
	public int getSize(long startTimeInMillis, long endTimeInMillis) {
		return 0;
	}

	public IRegisteredParcel getParcel(long timeInMillis) {		
		return null;
	}

}
