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

package au.edu.archer.dimsim.buffer.util;

//import static org.junit.Assert.*;

import java.util.ArrayList;


import org.instrumentmiddleware.cima.ConsoleMain;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;

import org.instrumentmiddleware.cima.plugin.producer.IProducer;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.session.impl.MapExtenderSession;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.edu.archer.dimsim.buffer.util.DeliverParcels;

/**
 * @author mrafi
 *
 */
public class DeliverParcelsTest {
	private static Mockery context;
	private static ISession session;
	private static Parcel normalParcel;
	private DeliverParcels<Parcel> deliverer;
	private static IProducer producer;	 
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		context = new JUnit4Mockery();
		producer = context.mock(IProducer.class);		
		normalParcel = ConsoleMain.newBasicParcel();
        normalParcel.setType(ParcelTypeEnum.PLUGIN);        
		
		session = context.mock(ISession.class);
		
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
		ArrayList<Parcel>  arrList = new ArrayList<Parcel> ();
		arrList.add(normalParcel);
		deliverer = new DeliverParcels<Parcel>(producer,"TestDeliverParcels", arrList, session);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.util.DeliverParcels#run()}.
	 */
	@Test
	public final void testRun() {		
		deliverer.run(); //cannot test anything other than clean run with mock plugin and subscriber
	}

}
