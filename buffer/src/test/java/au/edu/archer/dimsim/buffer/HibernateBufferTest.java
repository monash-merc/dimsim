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

import org.hibernate.SessionFactory;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.impl.RegisteredParcel;

import au.edu.archer.dimsim.buffer.pool.impl.HibernateBuffer;

import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author mrafi
 * to test run mvn -Dtest=DimsimRingBufferTest test 
 */
public class HibernateBufferTest extends AbstractBufferTests {	
	
	static ApplicationContext context;
	static BeanFactory factory;
	static SessionFactory sFactory;
	static Logger log = Logger.getLogger(HibernateBufferTest.class);
	/**
	 * @throws java.lang.Exception
	 */	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		context = new ClassPathXmlApplicationContext(new String[] {"hibernateTest.xml"});
		factory = (BeanFactory) context;
		sFactory = (SessionFactory) factory.getBean("dimsimSessionFactory", SessionFactory.class);	
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
		testBuffer = new HibernateBuffer(sFactory);
		//testBuffer = (HibernateBuffer) factory.getBean("dimsimStoreManager", HibernateBuffer.class);
		log.setLevel(Level.TRACE);		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {	
		testBuffer = null;
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.impl.preGenerics.DimsimRingBuffer#clear()}.
	 */
	@Test
	public final void testClear() {
		log.trace("Testing buffer Clear");
		testBuffer.add(this.regParcel);
		testBuffer.add(this.regParcel);
		testBuffer.add(this.regParcel);
		
		((HibernateBuffer)testBuffer).clear();
		
		assertEquals("Size is not Zero on clear() ", testBuffer.getSize(),0 );				
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.impl.preGenerics.HibernateBuffer#add(org.instrumentmiddleware.cima.parcel.IRegisteredParcel)}.
	 */
	@Test
	public final void testAdd() {
		log.trace("Testing buffer Add");
		((HibernateBuffer)testBuffer).clear();		
		
		IRegisteredParcel irParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel(ParcelTypeEnum.RESPONSE));				
		testBuffer.add(irParcel);
		
		assertEquals("Add failed to insert Element", 1, testBuffer.getSize());		
	}


	@Override
	void testGetFromBufferPositiveCallBack(ArrayList<Long> parcelTimes, int count) {

		int parcelStart, parcelEnd, startCountValidate, endCountValidate, midCountValidate;
		long parcelStartTime, parcelEndTime;
		Long[] validateAgainst = null;		
		
		parcelEnd = random.nextInt(count - 1);
		while (parcelEnd == 0) { parcelEnd = random.nextInt(count - 1); } 
		parcelStart = random.nextInt(parcelEnd);							
		parcelStartTime = parcelTimes.get(parcelStart);							  
		parcelEndTime = parcelTimes.get(parcelEnd);

		startCountValidate = count - parcelStart;
		endCountValidate = count - parcelEnd;							
		midCountValidate = startCountValidate - endCountValidate;
		 
		/*log.trace("count, pStart, pend, scountval, eval, mval are: " 
				+ count + "," + parcelStart+ "," + parcelEnd +"," 
				+ startCountValidate +"," + endCountValidate + "," + midCountValidate);
		*/
		testGetSizeLongPositive(parcelStartTime, startCountValidate);
		testGetSizeLongLongPositive(parcelStartTime, parcelEndTime, midCountValidate);
		
		validateAgainst = parcelTimes.toArray(validateAgainst);
		testGetParcelsPositive(validateAgainst);							
		
		validateAgainst =  getSubArray(parcelTimes,startCountValidate,parcelTimes.size());
		testGetParcelsLongPositive(validateAgainst,parcelStartTime);
		
		validateAgainst = getSubArray(parcelTimes,midCountValidate
				,(parcelTimes.size() - startCountValidate + midCountValidate));
		testGetParcelsLongLongPositive(validateAgainst, parcelStartTime,parcelEndTime);	
		
		validateAgainst = getSubArray(parcelTimes,1
				,(parcelTimes.size() - startCountValidate + 1));
		testGetParcelLongPositive(validateAgainst, parcelStartTime);	
		
		testGetParcelLongNegative(parcelTimes.get(0) - 10); //get a parcel not in the buffer
	}

	@Override
	void testGetSize() {
		// Tested earlier in add().
		
	}

	@Override
	void testGetFromBufferNegativeCallBack(ArrayList<Long> p, int c) {
		// TODO Auto-generated method stub
		
	}
	
}
