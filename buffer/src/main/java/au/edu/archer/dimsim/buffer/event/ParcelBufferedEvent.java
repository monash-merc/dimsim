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
package au.edu.archer.dimsim.buffer.event;

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
/**
 * Represents Parcel Buffered Event. 
 * This event must be generated whenever add() method of {@link IBufferInput} interface is successful.
 * <p>
 * @author Rafi M Feroze
 * 
 */

@SuppressWarnings("serial")
public class ParcelBufferedEvent extends BufferEvent {	
	public ParcelBufferedEvent(IRegisteredParcel regParcel, String producerID) {
	    super(regParcel,producerID, BufferEventType.ParcelBuffered);	    
    }	
}
