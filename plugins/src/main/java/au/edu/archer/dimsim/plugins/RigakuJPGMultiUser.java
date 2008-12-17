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

import java.util.Map;

import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.util.CIMAUtil;

import au.edu.archer.dimsim.plugins.parcelcreators.AbstractRigakuJPGFromUserParcel;
import au.edu.archer.dimsim.plugins.parcelcreators.NullParcelCreator;

import org.springframework.beans.factory.ObjectFactory;

/**
 * @author mrafi
 *
 */
public class RigakuJPGMultiUser extends MultiUserPluginBase<RigakuJPGPlugin> {

	IDimsimMultiUserParams sourcePluginParams;
	boolean standAloneMode = false;
	
	private ObjectFactory factory;  //to avoid Spring EventListener problem SPR-4690
									//See http://jira.springframework.org/browse/SPR-4690 

    public void setFactory(ObjectFactory factory) {
        this.factory = factory;
    }
    
    public void setStandAloneMode(boolean standAloneMode) {
		this.standAloneMode = standAloneMode;
	}
	/**
	 * @param director
	 * @param creator
	 * @param id
	 * @throws Exception
	 */
	
	public RigakuJPGMultiUser(ICIMADirector director, IDimsimMultiUserParams sourcePluginParams,
           String id) throws Exception {
    	
		super(director,new NullParcelCreator(),id);
		CIMAUtil.checkDependency(sourcePluginParams, IDimsimMultiUserParams.class);
		this.sourcePluginParams = sourcePluginParams;
				
    }
	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.plugins.MultiUserPluginBase#getNewInstance(org.instrumentmiddleware.cima.core.ICIMADirector, org.instrumentmiddleware.cima.plugin.producer.IParcelCreator, java.lang.String)
	 */
	@Override
	protected RigakuJPGPlugin getNewInstance(String userID) {

		RigakuJPGPlugin retProd = null;
		try {						
			String producerID = this.sourcePluginParams.getUserPluginID(userID);			
			Object rpObject = factory.getObject();
			if (rpObject instanceof AbstractRigakuJPGFromUserParcel) {
				AbstractRigakuJPGFromUserParcel rpCreator = (AbstractRigakuJPGFromUserParcel)rpObject;
				rpCreator.setStandAloneMode(this.standAloneMode);
				rpCreator.setSourceProducerID(producerID);
				retProd = new RigakuJPGPlugin(director,rpCreator,getUserPluginID(userID));					
			}			
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return retProd;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.plugins.MultiUserPluginBase#setUserParams(org.instrumentmiddleware.cima.plugin.producer.impl.AbstractProducer, java.util.Map)
	 */
	@Override
	protected void setUserParams(RigakuJPGPlugin plugin,
			Map<String, Object> params) {
		// do nothing
	}

}
