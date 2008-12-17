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

package au.edu.archer.dimsim.CrystalClear;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import au.edu.archer.dimsim.plugins.IMultiUserStoreListener;


/**
 * @author mrafi
 *
 */
public class AdminDBMonitor extends Thread {	        
	
	static Logger log = Logger.getLogger(AdminDBMonitor.class);
	private final Object syncFlag = new Object();
            
	protected boolean stop;
	protected File adminDBFile;
	protected AdminDBExtract ccExtract;
	protected Set<IMultiUserStoreListener> listenerset; //Set by IOC
	
	public static final int ONE_MILLISECOND = 1;  //default value used in sleep is milli second
	public static final int ONE_SECOND = ONE_MILLISECOND * 1000;
    public final int sleepTime = ONE_SECOND * 10;  //Ten Seconds    
    
    protected long prevModifiedTime = 0;
	private boolean waitingForParseCompletion;
    
    public AdminDBMonitor(String fileName) {
    	adminDBFile = new File(fileName);    	
    	this.start();
    }    
    
    public void setListenerset(Set<IMultiUserStoreListener> lSet) {
    	this.listenerset = lSet;
    }
    
    public void run() {
    	log.debug("Monitoring AdminDBFile " + this.adminDBFile.getPath());        
        
        long currModifiedTime;    
        
        while (!stop) {
            currModifiedTime = this.adminDBFile.lastModified();            
            if (currModifiedTime > this.prevModifiedTime) {
            	log.debug("AdminDB file changed, modify time is " + currModifiedTime);
                this.prevModifiedTime = currModifiedTime;                
            	ccExtract = null;
        		try {
        			ccExtract = new AdminDBExtract(this.adminDBFile);
        			notifyListeners();
        			log.debug("AdminDB Parsed");
        		} catch (XmlException e) {			 
        			e.printStackTrace();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}       
            }      
            
            //The right place to notifyAll is inside the block above. But for some unknown reason
            //Spring Bean initialization results in wait() after notify().
            //hmm..what happens in the XMLBeans parse above, is xmlparsing done in a new thread?
            
            synchronized(syncFlag) {
            	if (waitingForParseCompletion) syncFlag.notifyAll();
            }
            
	        try {
	            Thread.sleep(this.sleepTime);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
        }
    }
	
	private AdminDBExtract getDocRoot() {
		
		if (ccExtract != null) return ccExtract;
		
		log.debug("waiting for AdminDB parse to complete" );
		synchronized(syncFlag) {
			try {				
				this.waitingForParseCompletion = true;
				syncFlag.wait();
			} catch (InterruptedException e) {
				String message = "Wait for adminDB parsing interrupted, parse status " + 
									((ccExtract == null)?" ":"not ") + "completed";
				log.debug(message);
			}
			
			this.waitingForParseCompletion = false;
			
		}
		
		return getDocRoot(); //Ensure Parse is complete by this recursive call.
		
	}

	
	public void addListener(IMultiUserStoreListener listener) {
		this.listenerset.add(listener);		
	}

	public void removeListener(IMultiUserStoreListener listener) {
		this.listenerset.remove(listener);
	}
	
	private void notifyListeners() {
		
		for(IMultiUserStoreListener l:this.listenerset) {
			l.userStoreChanged(getDocRoot());
		}		
	}	

	
    
}
