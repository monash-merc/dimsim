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
package au.edu.archer.dimsim.buffer.impl;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;

import au.edu.archer.dimsim.buffer.IDeliveryBuffer;
import au.edu.archer.dimsim.buffer.util.ParcelUtils;

import org.instrumentmiddleware.cima.transport.delivery.IDeliveryStrategy;

/**
 * To be deleted.
 * @author Rafi M Feroze
 *
 */
public class delete_BufferedDeliveryStrategyDecorator implements IDeliveryStrategy {
	protected static Logger log = Logger.getLogger(delete_BufferedDeliveryStrategyDecorator.class);	
	
	IDeliveryStrategy decoratedDeliveryStrategy; 
	
	//variable set by Spring Inversion of Control logic
	IDeliveryBuffer deliveryBuffer;
	
	/**
	 * 
	 */
	public delete_BufferedDeliveryStrategyDecorator(IDeliveryStrategy strategy) {
		this.decoratedDeliveryStrategy = strategy;		
	}	
	
	private delete_BufferedDeliveryStrategyDecorator() {  
		//default constructor,remember to set decoratedDeliveryStrategy using IOC parameter 
		//Cima Producers require Class<?> as deliveryStrategy
		this(null);		
	}
	
	public void setDecoratedDeliveryStrategy(IDeliveryStrategy strategy) {
		this.decoratedDeliveryStrategy = strategy;
	}
	
	/* 
	 * 
	 */	
	public void setDeliveryBuffer(IDeliveryBuffer deliveryBuffer) {
		this.deliveryBuffer = deliveryBuffer;
	}
	
	public IDeliveryBuffer getDeliveryBuffer() {
		return deliveryBuffer;
	}	
	

	public Parcel deliver() { 
		return this.decoratedDeliveryStrategy.deliver();
	}

	public void setRegisteredParcel(IRegisteredParcel parcel) {
		
		if (deliveryBuffer != null) deliveryBuffer.buffer(ParcelUtils.getSender(parcel), parcel);
		
		this.decoratedDeliveryStrategy.setRegisteredParcel(parcel);	
		
	}

	public Parcel call() throws Exception {
		return this.decoratedDeliveryStrategy.call();
	}

}


