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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.ICIMADirector;

import au.edu.archer.dimsim.CrystalClear.AdminDBExtract;
import au.edu.archer.dimsim.plugins.parcelcreators.RigakuCCDParcelCreator;

/**
 * @author mrafi
 *
 */
public class RigakuSCPMultiUser extends MultiUserPluginBase<RigakuSCPPlugin> {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(RigakuSCPMultiUser.class);
	private static final String ADD_SCPFILENAME = File.separator + "SessionScript.scp";
	
	private Map<String,String> dirmap;
	private Map<String,String> replacemap;
	
	/**
	 * @param director
	 * @param creator
	 * @param id
	 * @throws Exception
	 */
	public RigakuSCPMultiUser(ICIMADirector director, 
			String id) throws Exception {
		super(director, new RigakuCCDParcelCreator(), id);
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.plugins.MultiUserPluginBase#getNewInstance(java.lang.String)
	 */
	@Override
	protected RigakuSCPPlugin getNewInstance(String userID) {
		RigakuSCPPlugin retProd = null;
		try {
			String id = getUserPluginID(userID);
			retProd = new RigakuSCPPlugin(director,new RigakuCCDParcelCreator(),id);
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return retProd;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.plugins.MultiUserPluginBase#setUserParams(org.instrumentmiddleware.cima.plugin.producer.impl.AbstractProducer, java.util.Map)
	 */
	@Override
	protected void setUserParams(RigakuSCPPlugin rPlugin,
			Map<String, Object> params) {
		if (rPlugin != null)	 {			
			Object scpDirObj = params.get(AdminDBExtract.dirTag.ScriptDir.getName());
			if (scpDirObj != null) {					
				String scpFile = convertFileName(scpDirObj.toString()) + ADD_SCPFILENAME;				
				rPlugin.setScpFileName(scpFile);
			}	
		}

	}
	
	
	public String convertFileName(String dirPath) {	
		
		if (dirPath == null) return null;  //do nothing		
		
		if (this.dirmap != null) {
			Set<String> keySet = dirmap.keySet();
			for (String modifier :keySet) {				
				if (dirPath.startsWith(modifier)) {
					dirPath = dirPath.substring(modifier.length());
					dirPath = dirmap.get(modifier) + dirPath;
				}
			}
		}		 
		
		if (this.replacemap != null) {
			Set<String> keySet = replacemap.keySet();			
			for (String modifier :keySet) {				
				dirPath = dirPath.replace(modifier,(String)replacemap.get(modifier));
			}
		}		
		 
		return dirPath;
	}
	

	public void setDirmap(Map<String, String> dirmap) {
		System.out.println("Setting dirMap , size = " + dirmap.size());
		this.dirmap = dirmap;
	}

	public void setReplacemap(Map<String, String> replacemap) {
		this.replacemap = replacemap;
	}

}