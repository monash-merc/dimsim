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
package au.edu.archer.dimsim.buffer.plugin.parcelcreator.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.producer.impl.AbstractCreator;


/**
 * @author mrafi
 *
 */
public class EternalWaitParcelCreator extends AbstractCreator {

	private static Logger log = Logger.getLogger(EternalWaitParcelCreator.class);
	private Object waitObj;
	/* (non-Javadoc)
	 * @see org.instrumentmiddleware.cima.plugin.producer.IParcelCreator#createParcel(java.util.Map)
	 */
	public Parcel createParcel(Map<String, Object> args) throws PluginException {
		try {
			this.stop();
			waitObj.wait(); //eternal wait. This plug-in does not produce parcel
		} catch (InterruptedException e) {
			log.debug("Received Interrupt on waitObj.wait() ");
		} 			
		
		return null;
	}

}
