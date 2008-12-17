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


import java.io.File;
import java.util.Map;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.util.ParcelUtil;


import au.edu.archer.dimsim.plugins.IRigakuConstants;
import au.edu.archer.dimsim.plugins.IRigakuFileEventListener;
import au.edu.archer.dimsim.plugins.RigakuPlugin;
import au.edu.archer.cima.xtal.BeginXtalExperimentType;
import au.edu.archer.cima.xtal.EndXtalExperimentType;

/**
 * @author mrafi
 *
 */
public abstract class AbstractRigakuParcelCreator extends PluginParcelCreator 
	implements IRigakuFileEventListener, IRigakuConstants{	
	 
	private static Logger log = Logger.getLogger(AbstractRigakuParcelCreator.class);	
	
	public AbstractRigakuParcelCreator() {
		super();
	}
	
	public abstract Parcel processRigakuFile(File f );
	public void newImageFileFound(File file) throws PluginException {
		Parcel p = processRigakuFile(file);
		if (p!= null) {
			super.setNewParcel(p);
		}		
	}
	
	
	protected static boolean isValidRigakuFile(File file) {
		
		if((file.getName().endsWith(rigakuFileTypes.OSC.getName())) 
		| (file.getName().endsWith(rigakuFileTypes.IMG.getName()))) {
			return true;
		}
		
		return false;

	}

	public Parcel createParcel(Map<String, Object> map) throws PluginException {

        if (map != null )  {
		String type = map.get(RigakuPlugin.EXPERIMENT_PARCEL_TYPE).toString();;
		if (type.equals(RigakuPlugin.PARCEL_TYPE_END_EXPERIMENT))  { 
		    return getExperimentEndParcel(map);
		 } 
	  	else if (type.equals(RigakuPlugin.PARCEL_TYPE_START_EXPERIMENT)) {
		    return getExperimentStartParcel(map);
	  	}
	 }
		
	 return super.createParcel(map);

	}

	public static Parcel getExperimentEndParcel(Map<String, Object> map) {
		
		Parcel parcel = ParcelUtil.newParcel(ParcelTypeEnum.PLUGIN);

		BodyType body = parcel.addNewBody();
		@SuppressWarnings("unused")
		EndXtalExperimentType bodyR = (EndXtalExperimentType)body.changeType(EndXtalExperimentType.type);         
       
        return parcel;
	}

	public static Parcel getExperimentStartParcel(Map<String, Object> map) {

		Parcel parcel = ParcelUtil.newParcel(ParcelTypeEnum.PLUGIN);

		BodyType body = parcel.addNewBody();
		
		BeginXtalExperimentType bodyR = 
			(BeginXtalExperimentType)body.changeType(BeginXtalExperimentType.type);  
        
        bodyR.setTaskName(extract(map.get(RigakuPlugin.RIGAKU_TASKNAME)));
        bodyR.setImageDirectory(extract(map.get(RigakuPlugin.RIGAKU_IMAGEDIRECTORY)));
        bodyR.setSampleName(extract(map.get(RigakuPlugin.RIGAKU_SAMPLENAME)));
        bodyR.setProjectName(extract(map.get(RIGAKU_PROJECTNAME)));        
        
        return parcel;


	}
	
	private static String extract(Object object) {
		
		if (object == null)  return "null";
		
		return object.toString();
		
	}  
	
}
