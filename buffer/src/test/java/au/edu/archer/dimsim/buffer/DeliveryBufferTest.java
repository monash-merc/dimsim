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
package au.edu.archer.dimsim.buffer;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.CIMAConstants;
import org.instrumentmiddleware.cima.ConsoleMain;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.CommandType;
import org.instrumentmiddleware.cima.parcel.EntityType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseBodyType;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.ValueVariableType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.impl.RegisteredParcel;
import org.instrumentmiddleware.cima.parcel.impl.ResponseBodyTypeImpl;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.session.impl.MapExtenderSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.edu.archer.dimsim.buffer.AbstractBufferTests;
import au.edu.archer.dimsim.buffer.IDeliveryBuffer;
import au.edu.archer.dimsim.buffer.impl.DeliveryBuffer;
import au.edu.archer.dimsim.buffer.plugin.impl.BufferPlugin;

/**
 * to test run mvn -Dtest=DeliveryBufferTest test 
 */

/**
 * @author mrafi
 *
 */
public class DeliveryBufferTest {
	private static Logger log = Logger.getLogger(DeliveryBufferTest.class);
	  
	DeliveryBuffer testBuffer;
	ISession session;		
		
	private BodyType body;
	private static EntityType subscriber;
	private static EntityType sender;		
	private static Parcel baseParcel, parcelWithBody;
	private static long startTime,endTime;
	public static final String testPluginName = "DeliveryBufferTest";
	public static final int count = 3;
	private static Calendar cal, nowMinus2Hr, nowPlus10Min;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		subscriber = EntityType.Factory.newInstance();
		subscriber.setId("testSubscriber");
		
		sender = EntityType.Factory.newInstance();
		sender.setId(testPluginName);
        
        //Get time objects for search
        cal = Calendar.getInstance();        
        
        nowMinus2Hr = ((Calendar) cal.clone());
        nowMinus2Hr.add(Calendar.HOUR, -2);
        nowMinus2Hr.add(Calendar.MILLISECOND, 243);//to differentiate numbers on print
        
        nowPlus10Min = ((Calendar) cal.clone());
        nowPlus10Min.add(Calendar.MINUTE, 10);
        nowMinus2Hr.add(Calendar.MILLISECOND, -271);//to differentiate numbers on print
        
        System.setProperty(CIMAConstants.PLUGINS_CONFIG_CLASSPATH, "buffers.xml");        
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
		int max_plugins = 99;
		testBuffer = new DeliveryBuffer(max_plugins); //sets to default Memory Ring Buffer		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.plugin.impl.BufferPlugin#processCommandOperation(org.instrumentmiddleware.cima.parcel.CommandOperationType, org.instrumentmiddleware.cima.session.ISession, org.instrumentmiddleware.cima.parcel.CommandOperationResponseType)}.
	 * @throws PluginException 
	 * @throws  
	 */
	@Test
	public final void testAll() throws PluginException {
		log.trace("Starting testAll");
		//fill up buffer
		IRegisteredParcel parcel;
		IRegisteredParcel bodyParcel;
		Calendar[] cals = new Calendar[] {nowMinus2Hr, cal, nowPlus10Min};
		
		for(int i=0;i<count;i++) { 
			baseParcel = ConsoleMain.newBasicParcel();
	        baseParcel.setType(ParcelTypeEnum.PLUGIN);
	        baseParcel.setSender(sender);
			baseParcel.setCreationTime(cals[i]);
			parcel = RegisteredParcel.createRegisteredParcel(baseParcel);
			
			testBuffer.buffer(this.testPluginName, parcel);	
			log.debug("inserted reg parcel for time " + parcel.getCreationTime().getTimeInMillis());

			parcelWithBody = ConsoleMain.newBasicParcel();
	        parcelWithBody.setType(ParcelTypeEnum.PLUGIN);
	        body = parcelWithBody.addNewBody();
	        body.changeType(ResponseBodyType.type);
			parcelWithBody.setCreationTime(cals[i]);
			bodyParcel = RegisteredParcel.createRegisteredParcel(parcelWithBody);
			
			testBuffer.buffer(this.testPluginName, bodyParcel);			
			log.debug("inserted body parcel for time " + bodyParcel.getCreationTime().getTimeInMillis());
		}
		
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetSize_String);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_String);
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetSize_StringLong);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLong);
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetSize_StringLongLong);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLongLong);		
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcel_StringLong);
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetLastParcel_StringClass);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringClass);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLongClass);
		
		log.trace("finished TestAll");
	}

	/**
	 * @param responseBody
	 * 
	 * The tests here are to verify BufferPlugin processing of IDeliveryBuffer's get methods.
	 * The correctness of the buffer results returned are not of interest. That is checked by
	 * the test classes for package au.edu.archer.dimsim.buffer (for example: DimsimRingBufferTest)   
	 * @throws PluginException 
	 * 
	 */
	private void testGet(IDeliveryBuffer.bufferMethodEnum methodEnum) throws PluginException {
		int retCount = this.count;
		
		log.debug("Start test for  command " + methodEnum.toString());					
		Class<? extends BodyType> bodyType = ResponseBodyTypeImpl.class;		
		String pluginId = this.testPluginName;
		ArrayList<IRegisteredParcel> ret = null;
		int retCounter = 0; 
		IRegisteredParcel rp;
		
		
		switch (methodEnum) {
		case GetLastParcel_StringClass:
			rp = testBuffer.getLastParcel(pluginId,bodyType);
			assertNotNull(methodEnum.toString() + "Parcels not returned : ",rp);			
			assertEquals(methodEnum.toString() + ": Wrong parcel returned ",this.nowPlus10Min.getTimeInMillis(),rp.getCreationTime().getTimeInMillis());
			break;
		case GetParcels_StringClass:
			ret = testBuffer.getParcels(pluginId,bodyType);			
			assertNotNull(methodEnum.toString() + "Parcels not returned : ",ret);			
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",3,ret.size());
			break;			
		case GetParcels_StringLongClass:			
			ret = testBuffer.getParcels(pluginId,cal.getTimeInMillis(),bodyType);
			assertNotNull(methodEnum.toString() + "Parcels not returned : ",ret);			
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",2,ret.size());
			break;
		case GetSize_StringLong:			
			retCounter = testBuffer.getSize(pluginId,this.nowPlus10Min.getTimeInMillis());						
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",2,retCounter);
			break;
		case GetParcel_StringLong:
			log.debug("for time " +cal.getTimeInMillis());
			rp = testBuffer.getParcel(pluginId,cal.getTimeInMillis());
			assertNotNull(methodEnum.toString() + "Parcels not returned : ",rp);			
			assertEquals(methodEnum.toString() + ": Wrong parcel returned ",this.cal.getTimeInMillis(),rp.getCreationTime().getTimeInMillis());
			break;
		case GetParcels_StringLong:
			log.debug("for time " +cal.getTimeInMillis());
			ret = testBuffer.getParcels(pluginId,cal.getTimeInMillis());
			assertNotNull(methodEnum.toString() + "Parcels not returned : ",ret);			
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",4,ret.size());
			break;			
		case GetSize_StringLongLong:			
			log.debug("for time " +this.nowMinus2Hr.getTimeInMillis() + " / " + this.nowPlus10Min.getTimeInMillis());
			retCounter = testBuffer.getSize(pluginId,this.nowMinus2Hr.getTimeInMillis(),this.nowPlus10Min.getTimeInMillis());									
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",6,retCounter);
			break;
		case GetParcels_StringLongLong:
			log.debug("for time " +this.nowMinus2Hr.getTimeInMillis() + " / " + this.nowPlus10Min.getTimeInMillis());
			ret = testBuffer.getParcels(pluginId,this.nowMinus2Hr.getTimeInMillis(),this.nowPlus10Min.getTimeInMillis());
			assertNotNull(methodEnum.toString() + "Parcels not returned : ",ret);			
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",6,ret.size());
			break;
		case GetSize_String:
			retCounter = testBuffer.getSize(pluginId);
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",6,retCounter);
			break;
		case GetParcels_String:
			ret = testBuffer.getParcels(pluginId);			
			assertNotNull(methodEnum.toString() + "Parcels not returned : ",ret);			
			assertEquals(methodEnum.toString() + ": Wrong number of parcels returned ",6,ret.size());
			break;
		default: //never reached.
			throw new PluginException("Invalid command : " + methodEnum.getName());
		}						
	}

	/**
	 * @return
	 */
	private String getParam(IDeliveryBuffer.bufferCommandParamTypeEnum paramType) {		

		switch (paramType) {
		case PLUGINID :			
			return testPluginName;			
		case STARTTIMEINMILLIS:
			return startTime +"";			
		case ENDTIMEINMILLIS:
			return endTime + "";			
		case BODYTYPE:
			return ResponseBodyType.class.getName();			
		}
		return "";
	}	
	
	public static void main(String[] args) {
		DeliveryBufferTest t = new DeliveryBufferTest();
		try {
			DeliveryBufferTest.setUpBeforeClass();
			t.setUp();
			t.testAll();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
