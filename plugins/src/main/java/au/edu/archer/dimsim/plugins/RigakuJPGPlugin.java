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
package au.edu.archer.dimsim.plugins;


import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.producer.poller.OnDataSinglePollerParcelProducer;
import org.instrumentmiddleware.cima.plugin.producer.poller.ParcelProducerPoller;

import au.edu.archer.dimsim.plugins.parcelcreators.RigakuJPGCreatorFromParcel;

import org.w3c.dom.Node;


/**
 * Author: M. Rafi 
 */
public class RigakuJPGPlugin extends OnDataSinglePollerParcelProducer  {
    
	Logger log = Logger.getLogger(RigakuJPGPlugin.class);
	 
	public RigakuJPGPlugin(ICIMADirector director, RigakuJPGCreatorFromParcel rpCreator,
            String id) throws Exception {
    	
    	super(id,rpCreator,director, ParcelProducerPoller.SINGLE);
    	
    }
    
	/* RigakuBuffersourceJPGParcelCreator is a spring aware bean. 
	 *  Hence this constructor requires ApplicationContext
	 */	
	/*public RigakuJPGPlugin(ICIMADirector director, RigakuPlugin sourcePlugin,
            String id, ApplicationContext context) throws Exception {
    	
		this(director,RigakuBufferSourceJPGParcelCreator.getNewParcelCreator(context,sourcePlugin),id);
				
    } */   

	public void start() throws PluginException {
    	//Start polling, invoking start() implies plug-in should produce, so poller should start as well 
    	//  without waiting for subscription request. Besides, CIMA issues a start only on two occasions
    	//  1. when a subscription request comes in and the plug-in is not started.
    	//  2. when start on load parameter is set. 
    	if (!this.isThreadStarted) {
			//check if the thread is started, if not start it
			pollerThread = getPollerThread();
			new Thread(pollerThread).start();
			isThreadStarted = true;
		}    	
    	super.start();    	
    }
	
	
	public void stop() throws PluginException {    
		super.stop();
	}

  
	public Node getInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	public Parcel[] produceParcels() throws PluginException {		
		return new Parcel[] {this.parcelCreator.createParcel(null)};
	}
	
	
}
