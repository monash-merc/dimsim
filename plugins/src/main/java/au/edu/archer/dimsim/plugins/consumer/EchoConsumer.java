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

package au.edu.archer.dimsim.plugins.consumer;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.ResponseType;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.impl.AbstractPlugin;
import org.instrumentmiddleware.cima.session.ISession;
import org.w3c.dom.Node;

import au.edu.archer.dimsim.buffer.plugin.IBufferNot;
/**
 * @author mrafi
 *
 */
public class EchoConsumer extends AbstractPlugin implements IBufferNot {

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(EchoConsumer.class);
	
	public EchoConsumer(String id) throws Exception {
		super(id);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.instrumentmiddleware.cima.plugin.IPlugin#getInformation()
	 */
	public Node getInformation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 /**
     * Process a parcel of type "plugin".
     *
     * @param body the request body
     * @param session the session the parcel belongs to
     * @param response the partly formed response
     * @return a response
     * @throws PluginException if there is a problem while processing the parcel
     */
    public ResponseType processParcel(BodyType body, ISession session,
        ResponseType response) throws PluginException {
    	/*
    	try {
	    	if (body.getClass().isInstance(RigakuType.class)) {
	    		System.out.println("RigakuType Received from " + session.getSubscriber().getId());
	    		RigakuType r = (RigakuType) body;
	    		System.out.println("Parcel CreateTime : " + r.getTime().toString());
	    		System.out.println("Parcel from File : " + r.getFilename()); 
	    		System.out.println("Parcel BData Size : " + r.getFilecontent().length);
	    	} else if (body.getClass().isInstance(DeviceDataType.class)) {
	    		System.out.println("RigakuType Received from " + session.getSubscriber().getId());
	    		DeviceDataType d = (DeviceDataType) body;
	    		for(VariableType v :d.getDeviceVariableArray()) {
	    			System.out.println(v.getName() + "\t" + v.getValue() + "\t" + v.getUnit().toString());
	    		}    		   
	    	}*
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}*/
    	
    	//RigakuType has been removed , use XTAL parcels instead.

        return response;
    }


}
