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


import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.extension.operation.ICommandEnable;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.producer.poller.OnDataSinglePollerParcelProducer;
import org.instrumentmiddleware.cima.plugin.producer.poller.ParcelProducerPoller;
import org.w3c.dom.Node;

import au.edu.archer.dimsim.plugins.parcelcreators.AbstractRigakuParcelCreator;

/**
 * Author: M. Rafi 
 */
public abstract class RigakuPlugin extends OnDataSinglePollerParcelProducer 
	implements ImageDirEventListener, ICommandEnable, IRigakuConstants {
    
    Logger log = Logger.getLogger(RigakuPlugin.class);   
    
    private SingleDirectoryPoller imagesDirPoller;
    private IRigakuFileEventListener imgListener;
    private List<Parcel> waitingParcels;

    public RigakuPlugin(String id, AbstractRigakuParcelCreator rpCreator,
            ICIMADirector director) throws Exception {
    	
    	super(id,rpCreator,director, ParcelProducerPoller.SINGLE);
    	this.imgListener = rpCreator;
    	
    }
    
    public void stop() throws PluginException {
        
		super.stop();
		
        if (imagesDirPoller != null) {
            imagesDirPoller.kill();
        }
        
    }

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
  
    // ImageDirEventListener ----------------------------------------------------
    public void newImageDirectory(File directory, Map<String,String> currParams) {
    	
        if (directory.getName().equals("Images")) {
        	waitingParcels = (List<Parcel>) Collections.synchronizedList(new ArrayList<Parcel>());

            if (imagesDirPoller != null) {
            	log.info("Stop data input from Previous Image Directory");     
            	Hashtable<String, Object> map = new Hashtable<String, Object>();
            	map.put(EXPERIMENT_PARCEL_TYPE,PARCEL_TYPE_END_EXPERIMENT); 	
            	if (currParams != null) {
            		map.putAll(currParams);
            	}
            	try {
            		waitingParcels.add(this.parcelCreator.createParcel(map));
            	} catch (PluginException e) {				
            		e.printStackTrace();
            	}            	
        
            	imagesDirPoller.removeListener(this.imgListener);
        
            }

            newRun(directory);
            
            //set the image directory poller
            imagesDirPoller = new SingleDirectoryPoller(directory.getAbsolutePath());
            imagesDirPoller.addListener(this.imgListener);
	    
            log.info("Start the new Image Dir Poller Thread");            
            //do not start until the Start Image Dir Polling until Experiment Start Parcel is created

            Hashtable<String,Object> map = new Hashtable<String,Object>();
            map.put(EXPERIMENT_PARCEL_TYPE,PARCEL_TYPE_START_EXPERIMENT);
            map.put(RIGAKU_SAMPLENAME,currParams.get(RIGAKU_SAMPLENAME));
            map.put(RIGAKU_PROJECTNAME,currParams.get(RIGAKU_PROJECTNAME));
            map.put(RIGAKU_TASKNAME,currParams.get(RIGAKU_TASKNAME));
            map.put(RIGAKU_IMAGEDIRECTORY,currParams.get(RIGAKU_IMAGEDIRECTORY));
            
            try {
            	waitingParcels.add(this.parcelCreator.createParcel(map));
            	imagesDirPoller.start();  //Start polling Image Dir now.
            	
            } catch (PluginException e) {
            	e.printStackTrace();
            }         
            
        }
    }

	private void newRun(File directory) {
		// TODO Auto-generated method stub
		
	}

	public Node getInformation() {
		// TODO Auto-generated method stub
		return null;
	}

	public Parcel[] produceParcels() throws PluginException {
		Parcel[] retParcels = null;		
		
		retParcels = new Parcel[] {this.parcelCreator.createParcel(null)};
		
		if (waitingParcels != null) {		
			synchronized (waitingParcels) { //to avoid waitingParcels = null, when new experiments are created in rapid succession
				//waitingParcels.add(this.parcelCreator.createParcel(null));
				for(Parcel p:retParcels) {
					waitingParcels.add(p);
				}
				retParcels = waitingParcels.toArray(retParcels);
				waitingParcels = null;
			}
		}
		
		return retParcels;
	}
	
}
