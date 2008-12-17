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

package au.edu.archer.dimsim.buffer.plugin;

import static org.junit.Assert.*;

import org.instrumentmiddleware.cima.CIMAConstants;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EndpointTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.impl.CommandOperationTypeImpl;
import org.instrumentmiddleware.cima.plugin.producer.IProducer;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.instrumentmiddleware.cima.util.SubscribeInfo;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import au.edu.archer.dimsim.buffer.plugin.impl.DimsimBufferedConsumerPlugin;

/**
 * @author mrafi
 * 
 * to test run mvn -Dtest=AbstractDimsimConsumerPluginTest test 
 *
 */
public class DimsimBufferedConsumerPluginTest 
	extends DimsimBufferedConsumerPlugin {

	private static SubscribeInfo bufferSubscribeInfo;	

	public DimsimBufferedConsumerPluginTest() throws Exception {
		super("AbstractDimsimConsumerPluginTest");
	}

	DimsimBufferedConsumerPluginTest testConsumer;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		EndpointType eP = EndpointType.Factory.newInstance();	
		eP.setUrl(""); 
		eP.setType(EndpointTypeEnum.MEMORY);  //MemoryTransport
		
		String bufferPluginID = "Cima_Buffer_Plugin";  //must match value in resource.xsd
		bufferSubscribeInfo = new SubscribeInfo(eP,bufferPluginID);
		
		System.setProperty(CIMAConstants.PLUGIN_CONFIG_PROPERTY, "buffers.xml");
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
		super.subscribeRemoteBufferPlugin(bufferSubscribeInfo);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}


	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.plugin.impl.DimsimBufferedConsumerPlugin#setBufferPluginID()}.
	 */
	@Test
	public final void testsetBufferPluginID() {
		assertTrue("Subscription to Buffer Failed", super.subscribeRemoteBufferPlugin(this.bufferSubscribeInfo));
	}
	
	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.plugin.impl.DimsimBufferedConsumerPlugin#createBufferCommandParcel(java.lang.String, java.util.Map)}.
	 * @throws Exception 
	 */
	@Test
	public final void testCreateBufferCommandParcel() throws Exception {
		Parcel p = super.createBufferCommandParcel("dummyCommand", null);
		assertEquals("Parcel Type invalid", ParcelTypeEnum.PLUGIN, p.getType());
		assertEquals("Command Parcel create failed ", CommandOperationTypeImpl.class,p.getBody().getClass());
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.plugin.impl.DimsimBufferedConsumerPlugin#buffer(org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel)}.
	 * @throws Exception 
	 */
	@Test
	public final void testBuffer() throws Exception {
		resetLocalBuffer();
		Parcel p = super.createBufferCommandParcel("dummyCommand", null);
		buffer(p);
		assertFalse("Parcel not buffered",isLocalBufferEmpty());		
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.plugin.impl.DimsimBufferedConsumerPlugin#resetLocalBuffer()}.
	 * @throws Exception 
	 */
	@Test
	public final void testResetLocalBuffer() throws Exception {
		Parcel p = super.createBufferCommandParcel("dummyCommand", null);
		buffer(p);
		resetLocalBuffer();
		assertTrue("Buffer reset failed", isLocalBufferEmpty());		
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.plugin.impl.DimsimBufferedConsumerPlugin#isLocalBufferEmpty()}.
	 */
	@Test
	public final void testIsLocalBufferEmpty() {
		//tested in testBuffer() and testResetLocalBuffer() methods above
	}

}
