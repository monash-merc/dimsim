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
package au.edu.archer.dimsim.buffer.eventListener;

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;

import au.edu.archer.dimsim.buffer.event.BufferEvent;
/**
 * Objects that require notification of Buffer Events must pass an implementation of this Interface 
 * to {@link ParcelEventHandler}'s addListener() method.
 * <p>
 * @author Rafi M Feroze
 */
public interface IBufferEventListener {
	/**
	 * This method will be invoked by Parcel Event Handler when the registered event occurs
	 * @param regParcel Parcel that gave rise to the event
	 * @param pluginID  Producer of the parcel 
	 * @param eventType	@see BufferEvent.BufferEventType
	 */
   public void eventParcel(IRegisteredParcel regParcel, String pluginID, BufferEvent.BufferEventType eventType);
   
   /** 
    * @return Id of the Producer whose parcel/buffer event is of interest to the implementation  
    */
   public String getProducerOfInterest();
}
