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
package au.edu.archer.dimsim.buffer.event.handler;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import au.edu.archer.dimsim.buffer.event.BufferEvent;
import au.edu.archer.dimsim.buffer.eventListener.IBufferEventListener;

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;

import org.instrumentmiddleware.cima.event.CIMAEvent;
import org.instrumentmiddleware.cima.event.listener.AbstractEventListener;

/*
 * Abstract Base EventHandler Class for Parcel Events. One handler per Event in a JVM is sufficient
 * for Dimsim. Each object that is interested in Parcel Buffered Event can then notify this  
 * object of their interest using the addListener() method of super class ParcelEventHandler. 
 * <p>
 * To Stop receiving events invoke removeListener() method.  
 * <p>
 * To implement a new Buffer event, 
 * <ul>
 * <li> add a new {@link BufferEventType}, 
 * <li> create a new BufferEvent @see au.edu.archer.dimsim.buffer.event.BufferEvent  &
 * <li> extend this class and implement abstract method getInterestedEventClass() 
 * 		to return the new BufferEvent class implemented above.
 * </ul>
 * <p>
 * @author Rafi M Feroze 
 */
public abstract class ParcelEventHandler<T extends BufferEvent> extends AbstractEventListener {
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ParcelEventHandler.class);    
        
    private Set<IBufferEventListener> listeners;    
    
    public ParcelEventHandler() {
    	this(new HashSet<IBufferEventListener> ());    
    }
    
    public ParcelEventHandler(Set<IBufferEventListener> listeners) {
    	this.listeners = listeners;    	
    }    
    
    public void onCIMAEvent(CIMAEvent event) {
        if ((event.getClass().equals(getInterestedEventClass()))  
        		&& (this.listeners != null)) {
        	BufferEvent bEvent = (BufferEvent) event;        	 
        	IRegisteredParcel p = (IRegisteredParcel)event.getSource();
        	for (IBufferEventListener listener : this.listeners) {
        		try {	        		
	        		String parcelProducerID = bEvent.getProducerID();
	        		String interestedProducerID = listener.getProducerOfInterest();
	        		if ((interestedProducerID == null) |        				
	        			(interestedProducerID.equals(parcelProducerID))) {        		
	        			//ParcelProducerID and EventType are redundantly passed, because 
	        			//the interface IBufferEventListener is defined for a generic case.
	        			listener.eventParcel(p, parcelProducerID,bEvent.getEventType());	        			
	        		}
        		} catch (NullPointerException ex) {
        			ex.printStackTrace();
        		}
        	}
        }
    }
    
    /** 
     * @return a subclass of au.edu.archer.dimsim.buffer.event.BufferEvent
     */
    protected abstract Class<T> getInterestedEventClass();
    
    /** 
     * Whenever a new event is received that matches getInterestedEventClass(), listener
     * object is queried for Producer of Interest. If the query matches the event's Producer, 
     * then the listener is notified of the event.
     * <p>
     * @param listener implementing IBufferEventListener interface. 
     */
	public void addListener(IBufferEventListener listener) {
    	//log.debug("Adding " + getClass().getSimpleName() + " for Parcels from " + listener.getProducerOfInterest());
    	this.listeners.add(listener);
    }
    
	/**
	 * Stop receiving Buffer Notifications.
	 * @param listener to be removed
	 */
    public void removeListener(IBufferEventListener listener) {    	
    	this.listeners.remove(listener);
    	//log.debug("Removed " + getClass().getSimpleName() + " for Parcels from " + listener.getProducerOfInterest());
    }
}
