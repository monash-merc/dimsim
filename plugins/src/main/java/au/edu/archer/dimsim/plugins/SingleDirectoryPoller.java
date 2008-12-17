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
import org.instrumentmiddleware.cima.plugin.PluginException;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

/**
 * Author: Mathew Wyatt
 * Organisation: James Cook University
 * Date: 22/12/2006
 * Time: 10:02:24
 *
 * SingleDirectoryPoller is responsible for polling a given directory
 * on disk. When new files are found in the directory, the registered Listeners are notified. * 
 */

public class SingleDirectoryPoller extends Thread {
    Logger logger = Logger.getLogger(SingleDirectoryPoller.class);

    File root;
    HashMap<Long, File> processed;
    boolean alive = true;
    Vector<IRigakuFileEventListener> eventListeners;

    /**
     * @param fileName - file name of the directory to be polled
     * @param eventListener - the event listener to handle the processign of new Files
     */
    public SingleDirectoryPoller(String fileName){
        root = new File(fileName);
        processed = new HashMap<Long, File>();
        eventListeners = new Vector<IRigakuFileEventListener> ();
    }
    
    synchronized public void addListener(IRigakuFileEventListener eventListener) {    	
    	if (eventListener != null) {
    		synchronized(this.eventListeners) {
    			eventListeners.add(eventListener);
    		}
    	}
    }
    
    synchronized public void removeListener(IRigakuFileEventListener eventListener) {
    	if (eventListener != null) {
    		synchronized(this.eventListeners) {
    			eventListeners.remove(eventListener);
    		}
    	}
    }

    public void kill() {
		this.alive = false;		
	}

	public void run() {
        while(alive) {
            File[] files = root.listFiles();
            if(files != null)
                for(File file : files) {
                    //logger.debug("Checking if " + file.getName() + " last modified at " + file.lastModified() + " has been procssed.");
                    if(!processed.containsKey(file.lastModified()) && file.isFile())  {
                    	
                    	if(file.getName().contains(".osc")) {                            

                            //Check the file is still not writing
                            this.check(file);

                            //logger.trace("Notify      " + file.getName() + " last modified at " + file.lastModified() + " to event listener.");
                            //Notify the file listener
                            try {
								for (IRigakuFileEventListener eventListener :eventListeners) {
									eventListener.newImageFileFound(file);
								}
							} catch (PluginException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

                            //logger.debug("Mark        " + file.getName() + " last modified at " + file.lastModified() + " as processed.");
                            //set the file to "been processed"
                            processed.put(file.lastModified(), file);
                        }
                    }
                }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO: determine a resonable sleep time
    private synchronized void check(File file) {
        long tmp = file.length();
        //logger.debug("Checking    " + file.getName() + " last modified at " + file.lastModified() + " writing status.");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //logger.debug("Length of   " + file.getName() + " last modified at " + file.lastModified() + " is " + file.length() + ".");

        while (file.length() > tmp) {
            tmp = file.length();

            //logger.debug("Length of   " + file.getName() + " last modified at " + file.lastModified() + " is " + tmp + ".");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //logger.debug("Writing to  " + file.getName() + " last modified at " + file.lastModified() + " finished. Final Size: " + file.length());
    }
    
}
