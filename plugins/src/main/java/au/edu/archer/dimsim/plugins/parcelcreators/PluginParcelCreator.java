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

package au.edu.archer.dimsim.plugins.parcelcreators;

import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.producer.IParcelCreator;

/**
 * @author mrafi
 *
 */
public class PluginParcelCreator implements IParcelCreator{	
	
	Logger log = Logger.getLogger(PluginParcelCreator.class);
	protected Vector<Parcel> waitingParcels;	
	protected Object mySync = new Object();	
	
	/**
	 * 
	 */
	protected PluginParcelCreator() {	
		this.waitingParcels = new Vector<Parcel>();		
	
	}
	
	public boolean parcelsWaiting() {
	
		return (!waitingParcels.isEmpty());
	
	}	
		
	public void start() throws PluginException {
		// Do Nothing leave it to subclass		
	}

	public void stop() throws PluginException {
		// Do Nothing, leave it to subclass
		
	}
	
	
	/* (non-Javadoc)
	 * @see org.instrumentmiddleware.cima.plugin.producer.IParcelCreator#produceParcel(java.util.Hashtable)
	 */
	public Parcel createParcel(Map<String, Object> args)
			throws PluginException {

		//log.debug(getClass().getName() + " : in createParcel");
		Parcel parcel = getNextParcel();

        if (parcel == null) {
            //log.debug("No parcel have been produced");
            throw new PluginException(getClass().getName() + " :Unable to produce the parcel");
        }

        
        return parcel;
	}

	private Parcel getNextParcel() {
		//log.debug(getClass().getName() + "in getNextParcel");
		if (waitingParcels.isEmpty()) {
	    // if no message, wait
			try {
				synchronized (mySync) {
					mySync.wait();					
				}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }		
		//log.debug(getClass().getName() + "wait over: getNextParcel, waitingParcels empty is " + waitingParcels.isEmpty());
		
		Parcel parcel = null;
	    if (!waitingParcels.isEmpty()) {
	    	// check again if not empty, as notify method might have been called
	    	// by stop. In that case the vector is still empty, null is returned
	    	waitingParcels.trimToSize();

	        synchronized (waitingParcels) {
	        	parcel = waitingParcels.elementAt(0);
	        	waitingParcels.remove(0);	            
	        }
	    }	        
		 
		return parcel;
	}	

	//This methods deadlocks with createParcel()/getNextParcel() methods
	//Hence must be invoked by a thread different from the above  
	public void setNewParcel(Parcel p) {
		if (p != null) {
			waitingParcels.add(p);            
		} else {
			log.debug("waitingParcels is null : Could not add Parcel; seq id = " + p.getSequenceId());
		}
	
		if (!waitingParcels.isEmpty()) {
			//log.info("Waking up threads waiting for Rigaku parcel " );
			synchronized (mySync) {
				mySync.notifyAll();					
			}			
		}
	}	
}
