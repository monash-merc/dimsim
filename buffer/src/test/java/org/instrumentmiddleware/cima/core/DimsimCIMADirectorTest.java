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

package org.instrumentmiddleware.cima.core;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.instrumentmiddleware.cima.CIMAConstants;
import org.instrumentmiddleware.cima.core.impl.DimsimCIMADirector;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.SubscriptionResponseType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.ICIMAPlugin;
import org.instrumentmiddleware.cima.plugin.producer.IProducer;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.edu.archer.dimsim.buffer.util.ParcelUtils;

/**
 * @author mrafi
 * to test run mvn -Dtest=DimsimCIMADirectorTest test
 */
public class DimsimCIMADirectorTest {
     
	private static DimsimCIMADirector dDir;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty(CIMAConstants.PLUGIN_CONFIG_PROPERTY, "buffers.xml");
		
		ICIMADirector dir = CIMAUtil.getDirector();		
		assertEquals("Director not instanceof DimsimCIMADirector", DimsimCIMADirector.class, dir.getClass());
		dDir = (DimsimCIMADirector) dir;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.instrumentmiddleware.cima.core.impl.DimsimCIMADirector#handleOutgoingParcel(org.instrumentmiddleware.cima.plugin.producer.IProducer, org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel)}.
	 * @throws ProcessException 
	 */
	@Test
	public final void testHandleOutgoingParcelIProducerParcel() throws ProcessException {
		
		String validRecipientID = "Cima_Buffer_Plugin";
		String stubRecipientID = "DimsimStubProducer";
		
		Parcel parcel = ParcelUtil.newParcel(ParcelTypeEnum.SUBSCRIBE);
		parcel.setSequenceId(1);
		parcel.setCreationTime(Calendar.getInstance());
		
		//Test Parcel Not Buffered
		dDir.setIsBuffered(false);
		ICIMAPlugin stub = dDir.getClientUtil().getPluginManager().getPlugin(stubRecipientID);
		IProducer stubProd = (IProducer) stub;
		dDir.handleOutgoingParcel(stubProd, parcel);
		assertEquals("Parcel Buffered when not expected to for "+ stubProd.getId(), 0, dDir.getDeliveryBuffer().getSize(stubProd.getId()));
		
		//Test Parcel from Producers set to BufferNot
		dDir.setIsBuffered(true);
		ICIMAPlugin bufferP = dDir.getClientUtil().getPluginManager().getPlugin(validRecipientID);
		IProducer bufferProd = (IProducer) bufferP;				
		dDir.handleOutgoingParcel(bufferProd,parcel);
		assertEquals("Parcel Buffered when not expected to. Buffer Size for "+bufferProd.getId(), 0, dDir.getDeliveryBuffer().getSize(bufferProd.getId()));
		
		
		//Test Parcels from Producers not set to BufferNot
		dDir.setIsBuffered(true);
		dDir.handleOutgoingParcel(stubProd, parcel);		
		assertEquals("Parcel not Buffered for "+ stubProd.getId(), 1, dDir.getDeliveryBuffer().getSize(stubProd.getId()));		
	}

	/**
	 * Test method for {@link org.instrumentmiddleware.cima.core.impl.DimsimCIMADirector#handleIncomingParcel(org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel, org.instrumentmiddleware.cima.parcel.ResponseBodyType)}.
	 * @throws Exception 
	 */
	@Test
	public final void testHandleIncomingParcelParcelResponseBodyType() throws Exception {
		
		String senderID = "DimsimCIMADirectorTest";

		//Test Endpoint Security Subscription success
		String validRecipientID = "Cima_Buffer_Plugin";  //must match value in resources/buffers.xml for BufferPlugin
		Parcel response = dDir.handleIncomingParcel(ParcelUtils.createSubscribeParcel(senderID, validRecipientID, null));				
		SubscriptionResponseType regResponse = (SubscriptionResponseType) ParcelUtil.getResponse(response)
					.getResponseArray(0).changeType(SubscriptionResponseType.type);		
		assertEquals("Subscription Request for BufferPlugin failed",ResponseStatusEnum.SUCCESS,regResponse.getStatus()); 
		
		//Test Endpoint Security Subscription failure
		String invalidRecipientID = "DimsimStubProducer";
		response = dDir.handleIncomingParcel(ParcelUtils.createSubscribeParcel(senderID, invalidRecipientID, null));				
		regResponse = (SubscriptionResponseType) ParcelUtil.getResponse(response)
					.getResponseArray(0).changeType(SubscriptionResponseType.type);		
		assertEquals("Subscription Request not rejected by security for StubPlugin",ResponseStatusEnum.FAILURE,regResponse.getStatus());
			
	}

}
