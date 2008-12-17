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

package au.edu.archer.dimsim.buffer.pool.manager.impl;

import java.util.Hashtable;

import au.edu.archer.dimsim.buffer.pool.IBuffer;
import au.edu.archer.dimsim.buffer.pool.impl.DummyBuffer;
import au.edu.archer.dimsim.buffer.pool.manager.IBufferPoolManager;

/**
 * @author mrafi
 *
 */
public class AbstractDimsimBufferManager implements IBufferPoolManager {

	private Hashtable<String, Class<? extends IBuffer>> pluginBufferType ;	
	Class<? extends IBuffer> defaultBufferType;
	
	
	public AbstractDimsimBufferManager() {
		pluginBufferType = new Hashtable<String, Class<? extends IBuffer>> ();
		defaultBufferType = DummyBuffer.class;
	}
	
	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBufferManager#getNewBuffer(java.lang.String)
	 */
	public IBuffer getNewBuffer(String pluginId) {
		
		IBuffer retVal = null;
		Class<? extends IBuffer> bufferType = pluginBufferType.get(pluginId);		
		
		try {
			if (bufferType == null) {				
				bufferType = defaultBufferType;
			}
			retVal = bufferType.newInstance();				
		} catch (InstantiationException e) {				
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBufferManager#setBufferType(java.lang.String, java.lang.Class)
	 */
	public boolean setBufferType(String pluginId, Class<? extends IBuffer> bufferType) { 
		boolean retVal = false;		
		
		if (bufferType != null){		   			
			pluginBufferType.put(pluginId, bufferType);
			retVal = true;
		}
		
		return retVal;
	}

	public boolean setDefaultBufferType(Class<? extends IBuffer> defaultBuffer) {
		boolean retVal = false;		
		
		if (defaultBuffer != null){			
			this.defaultBufferType = defaultBuffer;
			retVal = true;
		}
		
		return retVal;	
	}

}
