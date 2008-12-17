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
 * to test run mvn -Dtest=BufferPluginTest test 
 */

/**
 * @author mrafi
 *
 */
public class BufferPluginTest {
	private static Logger log = Logger.getLogger(BufferPluginTest.class);
	  
	DeliveryBuffer testBuffer;
	ISession session;
	BufferPlugin bufferPlugin;	
			
	private static EntityType subscriber;
	private static EntityType sender;		
	private static Parcel dummyParcel, dummyBodyParcel;
	private static long startTime,endTime;
	public static final String testPluginName = "BufferPluginTest";
	public static final int count = 8;
	
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {		
		subscriber = EntityType.Factory.newInstance();
		subscriber.setId("testSubscriber");
		
		sender = EntityType.Factory.newInstance();
		sender.setId(testPluginName);
		
		dummyParcel = ConsoleMain.newBasicParcel();
        dummyParcel.setType(ParcelTypeEnum.PLUGIN);
        dummyParcel.setSender(sender);
        
        dummyBodyParcel = ConsoleMain.newBasicParcel();
        dummyBodyParcel.setType(ParcelTypeEnum.PLUGIN);
        dummyBodyParcel.setSender(sender);
        BodyType body = dummyBodyParcel.addNewBody();
        ResponseBodyType bodyR = (ResponseBodyType)body.changeType(ResponseBodyType.type);
        
        startTime = dummyParcel.getCreationTime().getTimeInMillis() - AbstractBufferTests.HOUR_IN_MILLIS; 
        endTime = startTime + 2 * AbstractBufferTests.HOUR_IN_MILLIS;        
        
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
		int max_plugins = 99;
		testBuffer = new DeliveryBuffer(max_plugins); //sets to default Memory Ring Buffer 
		bufferPlugin = new BufferPlugin(testPluginName,testBuffer);		
		session = MapExtenderSession.createNewSession("BufferPluginTest", bufferPlugin,
				subscriber);
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
		IRegisteredParcel parcel = RegisteredParcel.createRegisteredParcel(dummyParcel);
		IRegisteredParcel bodyParcel = RegisteredParcel.createRegisteredParcel(dummyBodyParcel);
		parcel.addSession(session);
		bodyParcel.addSession(session);
		boolean addBody = true;
		for(int i=0;i<count;i++) { 
			testBuffer.buffer(this.testPluginName, parcel); 
			if (addBody) {
				testBuffer.buffer(this.testPluginName, bodyParcel);
				addBody = !addBody;
			}
		}
		
		//setup Response Object        
        CommandOperationResponseType responseBody = CommandOperationResponseType.Factory.newInstance();
        //Test Commands
        
		testGet(IDeliveryBuffer.bufferMethodEnum.GetSize_String, responseBody);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_String, responseBody);
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetSize_StringLong,responseBody);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLong,responseBody);
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetSize_StringLongLong, responseBody);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLongLong, responseBody);		
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcel_StringLong, responseBody);
		
		testGet(IDeliveryBuffer.bufferMethodEnum.GetLastParcel_StringClass, responseBody);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringClass, responseBody);
		testGet(IDeliveryBuffer.bufferMethodEnum.GetParcels_StringLongClass, responseBody);
		
		log.trace("finished TestAll");
	}

	/**
	 * @param responseBody
	 * 
	 * The tests here are to verify BufferPlugin processing of IDeliveryBuffer's get methods.
	 * The correctness of the buffer results returned are checked by test classes for package  
	 * 		au.edu.archer.dimsim.buffer :- Classes DeliveryBufferTest and DimsimRingBufferTest
	 * @throws PluginException 
	 * 
	 */
	private void testGet(IDeliveryBuffer.bufferMethodEnum methodEnum, CommandOperationResponseType responseBody) throws PluginException {
		int retCount = this.count;
		
		log.debug("Start test for  command " + methodEnum.toString());
		CommandType cmdType = getCommand(methodEnum.getName());
		
		switch (methodEnum) {
		case GetLastParcel_StringClass:
		case GetParcels_StringClass:
			addParam(cmdType,IDeliveryBuffer.bufferCommandParamTypeEnum.BODYTYPE);
			break;
		case GetParcels_StringLongClass:
			addParam(cmdType,IDeliveryBuffer.bufferCommandParamTypeEnum.BODYTYPE);
			//continue without break to get start time
		case GetSize_StringLong:
		case GetParcel_StringLong:
		case GetParcels_StringLong:
			addParam(cmdType,IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS);
			break;
		case GetSize_StringLongLong:
		case GetParcels_StringLongLong:
			addParam(cmdType,IDeliveryBuffer.bufferCommandParamTypeEnum.STARTTIMEINMILLIS);
			addParam(cmdType,IDeliveryBuffer.bufferCommandParamTypeEnum.ENDTIMEINMILLIS);
		case GetSize_String:
		case GetParcels_String:
			break;
		default: //never reached.
			throw new PluginException("Invalid command : " + methodEnum.getName());
		}
				
		try {
			addParam(cmdType,IDeliveryBuffer.bufferCommandParamTypeEnum.PLUGINID);
			responseBody = bufferPlugin.processCommandOperation(cmdType,null, session, responseBody);
		} catch (PluginException e) {
			fail(e.getMessage());
		}
		 
		assertEquals("Expected Success, got Error Message " + responseBody.getMessage(), ResponseStatusEnum.SUCCESS, responseBody.getStatus());
		assertEquals("Result returned from BufferPlugin should not be null", responseBody.isSetResult(),true);
		log.debug("Result returned for command " + methodEnum.toString() + " is " + responseBody.getResult());
		//assertEquals("Return Count not set ",new Integer(retCount).toString(), responseBody.getResult());
	}

	/**
	 * @return
	 */
	private void addParam(CommandType cmdType, IDeliveryBuffer.bufferCommandParamTypeEnum paramType) {
		
		ValueVariableType param1 = cmdType.addNewParameter();
		param1.setName(paramType.getName());

		switch (paramType) {
		case PLUGINID :			
			param1.setValue(testPluginName);
			break;
		case STARTTIMEINMILLIS:
			param1.setValue(Long.toString(startTime));
			break;
		case ENDTIMEINMILLIS:
			param1.setValue(Long.toString(endTime));
			break;
		case BODYTYPE:
			param1.setValue(ResponseBodyType.class.getName());
			break;
		}
	}
	
	private CommandType getCommand(String command) {
		CommandType cmdType = CommandType.Factory.newInstance();
		cmdType.setCommandName(command);
		return cmdType;
	}
	
	public static void main(String[] args) {
		BufferPluginTest t = new BufferPluginTest();
		try {
			BufferPluginTest.setUpBeforeClass();
			t.setUp();
			t.testAll();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
