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
import org.instrumentmiddleware.cima.event.CIMAEvent;
/*
 * Abstract Base Class representing Dimsim Buffer Events represented by @see BufferEventType. 
 * <p>
 * @author Rafi M Feroze 
 */

public abstract class BufferEvent extends CIMAEvent {
	private static final long serialVersionUID = 1L;

	/**
	 * Dimsim Buffer Events of Interest. 
	 * For each event, a relevant Event and EventHandler class must be coded. 
	 * 
	 * 
	 * @see au.edu.archer.dimsim.buffer.event.handler.ParcelEventHandler
	 * 
	 * @author Rafi M Feroze
	 *
	 */
	public static enum BufferEventType { ParcelBuffered, ParcelRemoved };
	
	String producerID;
	BufferEventType eventType;
	
	/**
	 * Construct a Parcel related CIMAEvent for given ProducerID and Buffer EventType. 
	 * The parcel that generated this event is available for all event receivers using 
	 * Spring's EventObject method getSource(). 
	 * 
	 * @param regParcel		Parcel generating this event
	 * @param producerID	Parcel Producer ID
	 * @param eventType		Buffer Event Type. @see BufferEventType
	 */
	protected BufferEvent(IRegisteredParcel regParcel, String producerID, BufferEventType eventType) {
	    super(regParcel);
	    this.producerID = producerID;
	    this.eventType = eventType;
    }
	
	/** 
	 * @return	Producer ID that generated the parcel responsible for this event
	 */
	public String getProducerID() {
		return this.producerID;
	}
	
	/** 
	 * @return Event Type   @see BufferEventType.
	 */
	public BufferEventType getEventType()
	{
		return eventType;
	}
}
