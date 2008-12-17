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

import java.io.File;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.CommandType;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.VariableType;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.session.ISession;

import au.edu.archer.dimsim.plugins.parcelcreators.AbstractRigakuParcelCreator;

/**
 * @author mrafi
 *
 */
public class RigakuSCPPlugin extends RigakuPlugin  {

	private SCPFilePoller scpFilePoller;
	private Map<String,String> dirmap;
	private Map<String,String> replacemap;	

	/**
	 * @param id
	 * @param rpCreator
	 * @param director
	 * @throws Exception
	 */
	
	public RigakuSCPPlugin(ICIMADirector director, AbstractRigakuParcelCreator rpCreator,
			String id ) throws Exception {
		super(id, rpCreator, director);				
	}
	
	public void setDirmap(Map<String, String> dirmap) {
		System.out.println("Setting dirMap , size = " + dirmap.size());
		this.dirmap = dirmap;
	}
	
	public void setReplacemap(Map<String, String> replacemap) {
		this.replacemap = replacemap;
	}	
	
	public void setScpFileName(String scpFileName) {
		this.setNewSCPFile(scpFileName);
	}
	
	public void stop() {
		
		if (scpFilePoller != null) {
		   scpFilePoller.terminate();
		}
			
	    try {
	    	super.stop();
		} catch (PluginException e) {			
			e.printStackTrace();
		}       
        
    }
	
	public void newImageDirectory(String imageDir, Map<String,String> currParams) {	
				
		if (imageDir == null) return;  //do nothing		
		
		if (this.dirmap != null) {
			Set<String> keySet = dirmap.keySet();
			for (String modifier :keySet) {
				System.out.println("Checking with " + modifier);
				if (imageDir.startsWith(modifier)) {
					imageDir = imageDir.substring(modifier.length());
					imageDir = dirmap.get(modifier) + imageDir;
				}
			}
		}		 
		
		if (this.replacemap != null) {
			Set<String> keySet = replacemap.keySet();			
			for (String modifier :keySet) {
				System.out.println("Replace :checking with " + modifier);
				imageDir = imageDir.replace(modifier,(String)replacemap.get(modifier));
			}
		}		
		 
		System.out.println("New ImageDir is " + imageDir);
		super.newImageDirectory(new File(imageDir), currParams);
	}
	
	private void setNewSCPFile(String scpFileName) {
		
    	if (scpFileName != null) {
    		//log.info("Set new SCPFile to " + scpFileName);
    		if (this.scpFilePoller == null) {    			
    			scpFilePoller = new SCPFilePoller(scpFileName);
    			scpFilePoller.setEventListener(this);
    			scpFilePoller.start();
    		} else {
    			scpFilePoller.setSCPFile(scpFileName);
    		}
    	}
    	
    }
	
	public CommandOperationResponseType processCommandOperation(
	    	   CommandType operation, Calendar whatCal, ISession session,
	    	   CommandOperationResponseType response) throws PluginException {
	    	
	    	   response.setStatus(ResponseStatusEnum.FAILURE); //set by default, changed later if deliveryBuffer request is successful
				
		       String command = operation.getCommandName();
		       if   (!command.equalsIgnoreCase(IRigakuFileEventListener.SET_NEW_SCPFILE)) {
		               response.setMessage(command + " Command not available");
		               log.info("Illegal command request : command is  " + command);
		               return response;
		       }
		
		       log.debug("processing command " + command);	
		       
		       VariableType[] params = operation.getParameterArray();	       
		       String paramName;
		       for (int i = 0; i < params.length; i++) {
		    	   	   paramName = params[i].getName();
		               if (paramName.equalsIgnoreCase(IRigakuFileEventListener.SCP_FILENAME)) {
		            	   String scpFileName = params[i].getValue();
		            	   if (scpFileName != null) {
		            		   this.setNewSCPFile(scpFileName);
		            		   response.setMessage("SCP file changed, monitoring " + scpFileName);
		            		   response.setStatus(ResponseStatusEnum.SUCCESS);
		            		   return response;
		            	   }
		               } 
		       }
		       
		       response.setMessage("Command " + command  + " failed : Missing parameter " 
		    		   + IRigakuFileEventListener.SCP_FILENAME);
		       return response;
		}

}
