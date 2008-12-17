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
package au.edu.archer.dimsim.security;

import java.util.Properties;
import java.util.Set;

import org.instrumentmiddleware.cima.session.ISession;


public interface IEndPointSecurity {
	public enum SecurityProperty {STARTTIME, ENDTIME};
	
	Properties add(String consumerID, String consumerEndPointURL, String pluginId);
	public boolean isValidConsumer(String consumerID, String consumerEndPointURL, String pluginId);
	
	Properties add(ISession session);
	public boolean isValidConsumer(ISession session);
	
	Properties add(Set<String> tuple);
	public boolean isValidConsumer(Set<String> tuple);
	
	public void remove(String pluginId);    //remove all access to given plugin 
	public void remove(String consumerID, String consumerEndPointURL, String pluginId);  //remove access granted to the tuple
	public  Set<Set<String>> getConsumerEndPoints(String pluginId); //get all tuples <consumerId, EndPointURL> that have current access to given plugin
}
