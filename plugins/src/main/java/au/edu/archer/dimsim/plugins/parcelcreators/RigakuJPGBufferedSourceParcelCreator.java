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


import java.net.MalformedURLException;

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.plugin.PluginException;

import au.edu.archer.dimsim.buffer.event.BufferEvent.BufferEventType;
import au.edu.archer.dimsim.buffer.event.handler.ParcelBufferedEventHandler;
import au.edu.archer.dimsim.buffer.event.handler.RemovedFromBufferEventHandler;
import au.edu.archer.dimsim.buffer.eventListener.IBufferEventListener;


/**
 * @author mrafi
 */
public class RigakuJPGBufferedSourceParcelCreator extends
		AbstractRigakuJPGFromUserParcel implements IBufferEventListener {

	ParcelBufferedEventHandler parcelBufferedListener;
	RemovedFromBufferEventHandler parceRemovedListener;
	boolean deleteOnJVMExit;
	
	/**
	 * @param id
	 * @param rigakuPlugin
	 */
	
	public RigakuJPGBufferedSourceParcelCreator(ParcelBufferedEventHandler pListener) {
		this(pListener,null);  // interested in all Parcels
	}
	
	public RigakuJPGBufferedSourceParcelCreator(ParcelBufferedEventHandler pListener,
			String producerOfInterestID) {
		super(producerOfInterestID);
		this.parcelBufferedListener = pListener;
		
	}
	
	public void setParcelRemovedListener(RemovedFromBufferEventHandler rListener) {
		this.parceRemovedListener = rListener;
	}
	
	public void setdeleteOnJVMExit(boolean flag) {
		this.deleteOnJVMExit = flag;
	}
	
	public void start() throws PluginException {
		if (parcelBufferedListener != null) {
			parcelBufferedListener.addListener(this);
		}
		if (parceRemovedListener != null) {
			parceRemovedListener.addListener(this);
		}
		super.start();
	}

	public void stop() throws PluginException {
		if (parcelBufferedListener != null) {
			parcelBufferedListener.removeListener(this);
		}
		//Might want to delete JPEG Files even after the plug-in is stopped, 
		// so comment out the next line
		/*if (rListener != null) {
			rListener.removeListener(this);
		}*/
		super.stop();
	}	
	
	 public String getProducerOfInterest() {		 
		 return this.sourceProducerID;
	 }

	public void eventParcel(IRegisteredParcel regParcel, String pluginID,
			BufferEventType eventType) {
		if (eventType.equals(BufferEventType.ParcelBuffered)) {			
				super.newRigakuParcel(regParcel);			
		} else if (eventType.equals(BufferEventType.ParcelRemoved)) {
			try {
				super.deleteXtalJPEGFile(regParcel, deleteOnJVMExit);
			} catch (MalformedURLException e) {				
				log.debug("Unable to delete JPEG file : " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	protected boolean isParcelSourceValid(IRegisteredParcel regParcel) {
		//Return true by default, since IBufferEventListener takes care of 
		// restricting parcels to sourceProducerID set in super class
		return true;	
	}
	
}

