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

package au.edu.archer.dimsim.security.impl;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EntityType;
import org.instrumentmiddleware.cima.plugin.IPlugin;
import org.instrumentmiddleware.cima.session.ISession;

import au.edu.archer.dimsim.buffer.pool.impl.AbstractRingBuffer;
import au.edu.archer.dimsim.buffer.pool.impl.SortedRingBufferUtils;
import au.edu.archer.dimsim.buffer.pool.impl.FIFORingBuffer;
import au.edu.archer.dimsim.buffer.pool.store.impl.MemoryStore;
import au.edu.archer.dimsim.security.IEndPointSecurity;

/**
 * @author mrafi
 *
 * This class implements a security model based on tuple <customer_id,endpointURL,source_plugin id>
 *  
 * Customer whose input session matches stored tuple values are considered valid for ISecurity interface
 *  
 * To use this security model for restricting user access, preset consumerProperties map with keys 
 * corresponding to the relevant <customer,endpoint,source> tuple.
 * Note: values can be set to null.
 *  
 */
public class EndpointSubscriptionSecurityModel implements IEndPointSecurity {	
	
	protected Map<Set<String>, AbstractRingBuffer<Properties>>  consumerProperties;
	
	private static final int DEFAULT_SUBSCRIPTION_MEMORY_SIZE = 10;
	//Remember connection details for last ten attempts.Can be overridden using IOC	
	private int subscriptionMemorySize = 10;	

	public EndpointSubscriptionSecurityModel() {		
		this(DEFAULT_SUBSCRIPTION_MEMORY_SIZE);
	}
	
	public EndpointSubscriptionSecurityModel(int subscriptionMemorySize) {		
		this.subscriptionMemorySize = subscriptionMemorySize;	
		this.consumerProperties = new HashMap<Set<String>,  AbstractRingBuffer<Properties>> ();
	}
	
	public EndpointSubscriptionSecurityModel(int subscriptionMemorySize, Set<Set<String>> consumerEndpointProducer) {
		this(subscriptionMemorySize);	
		if (consumerEndpointProducer != null) {		
			for(Set<String> s : consumerEndpointProducer) {
				if (s != null) {
					this.consumerProperties.put(s,getNewConsumerProps());
				}
			}			
		}
	}

	/**
	 * @param subscriptionMemorySize
	 */
	private AbstractRingBuffer<Properties> getNewConsumerProps() {
		return new FIFORingBuffer<Properties>(new MemoryStore<Properties> (this.subscriptionMemorySize)) { };
	}	
	
	@SuppressWarnings("unused")
	private void setConsumerProperties(Map<Set<String>, AbstractRingBuffer<Properties>> consumerProps) {
		this.consumerProperties = consumerProps;		
	}	 
		
	public Properties add(Set<String> consumerEndPointPluginTuple) {		
		
		Properties props  = null;	
		
		if (consumerEndPointPluginTuple != null) {					
			props = new Properties();			
			AbstractRingBuffer<Properties> consumerHistory =
				consumerProperties.get(consumerEndPointPluginTuple);
			if (consumerHistory == null) {
				consumerHistory = this.getNewConsumerProps();
				consumerProperties.put(consumerEndPointPluginTuple,consumerHistory);
			}
			consumerHistory.add(props);
		}		
		
		return props; 
	}
	

	protected void setSubscriptionMemorySize(int i) {
		this.subscriptionMemorySize = i;  //if i <1, memorystore default will kick in.
	}
	
	public static Set<String> getConsumerTuple(ISession session) {
		Set<String> tuple = null;
		
		if (session != null)  {			
			IPlugin plugin = session.getPlugin();
			
			if (plugin != null) {			
				tuple = getConsumerEndPoint(session);
				if (tuple != null) {									
					tuple.add(plugin.getId());
				}
			}
		}
		
		return tuple;  
	}
	
	public static Set<String> getConsumerEndPoint(ISession session) {
		Set<String> tuple = null;
		
		if (session != null)  {		
			
			EntityType subscriber = session.getSubscriber();		
			
			if (subscriber != null) {			
				String customer = subscriber.getId();				
				EndpointType endpoint = (EndpointType) session.get("endpoint");
				tuple = new HashSet<String>();				
				tuple.add(customer);
				tuple.add(endpoint.getUrl());				
			}
		}
		
		return tuple;  
	}

	/**
	 * @param consumerID
	 * @param consumerEndPointURL
	 * @param pluginID
	 * @return
	 */
	public static Set<String> createTuple(String consumerID, String consumerEndPointURL,
			String pluginID) {
		Set<String> tuple = new HashSet<String>();
		tuple.add(consumerID);
		tuple.add(consumerEndPointURL);
		tuple.add(pluginID);
		return tuple;
	}	
	
	/*
	 * Currently, validity is defined as  
	 * 1. Consumer (id) had initiated a session from same end point &
	 * 2. The session must have been for the requested plug-in
	 * 
	 */
	public boolean isValidConsumer(ISession session) {
		
		Set<String> tuple = getConsumerTuple(session);
		
		return consumerProperties.containsKey(tuple);		
	}	
	
	public boolean isValidConsumer(String consumerID, 
				String consumerEndPointURL, String pluginID) {

		Set<String> tuple = createTuple(consumerID, consumerEndPointURL, pluginID);
		
		return isValidConsumer(tuple);
	}
	
	public boolean isValidConsumer(Set<String> tuple) {	
		return consumerProperties.containsKey(tuple);
	}

	public Properties add(String consumerID, String consumerEndPointURL,
			String pluginId) {
 
		return add(createTuple(consumerID, consumerEndPointURL, pluginId));
	}
	
	public Properties add(ISession session) {
		
		Set<String> tuple = getConsumerTuple(session);
		return add(tuple);					
	}

	public  Set<Set<String>> getConsumerEndPoints(String pluginId) {
		Set<Set<String>> retSet = new HashSet<Set<String>> ();
		
		for (Set<String> tuple : consumerProperties.keySet()) {
			if (tuple.contains(pluginId)) {
				retSet.add(tuple);
			}
		}
		return retSet;
	}

	public void remove(String consumerID, String consumerEndPointURL,
			String pluginId) {
		Set<String> inTuple = createTuple(consumerID, consumerEndPointURL, pluginId);
		for (Set<String> tuple : consumerProperties.keySet()) {
			if (tuple.equals(inTuple)) {
				consumerProperties.remove(tuple);
			}
		}		
	}

	public void remove(String pluginId) {
		for (Set<String> tuple : consumerProperties.keySet()) {
			if (tuple.contains(pluginId)) {
				consumerProperties.remove(tuple);
			}
		}				
	}
}
