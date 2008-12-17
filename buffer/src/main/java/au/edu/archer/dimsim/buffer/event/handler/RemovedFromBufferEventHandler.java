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
package au.edu.archer.dimsim.buffer.event.handler;

import org.apache.log4j.Logger;

import au.edu.archer.dimsim.buffer.event.RemovedFromBufferEvent;
/*
 * EventHandler Class for Parcel Removed from Buffer Event. 
 * One event handler per JVM is sufficient for Dimsim. Each object that is interested 
 * in Parcel removed event notifies this object of their interest using the addListener() 
 * method of super class ParcelEventHandler. 
 * <P>
 * To Stop receiving events invoke removeListener() method.  
 * <P>
 * @author Rafi M Feroze 
 * @see ParcelEventHandler
 */

public class RemovedFromBufferEventHandler extends ParcelEventHandler<RemovedFromBufferEvent> {
    @SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(RemovedFromBufferEventHandler.class);
    
    @Override
	protected Class<RemovedFromBufferEvent> getInterestedEventClass() {
		return RemovedFromBufferEvent.class;
	} 
}
