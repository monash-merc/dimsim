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

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.impl.RegisteredParcel;

import au.edu.archer.dimsim.buffer.pool.impl.DimsimRingBuffer;
import au.edu.archer.dimsim.buffer.pool.store.impl.MemoryStore;

import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author mrafi
 * to test run mvn -Dtest=DimsimRingBufferTest test 
 */
public class DimsimRingBufferTest extends AbstractBufferTests {	
	int testSize = 679;
	
	static Logger log = Logger.getLogger(DimsimRingBufferTest.class);
	/**
	 * @throws java.lang.Exception
	 */	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
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
		testBuffer = new DimsimRingBuffer(new MemoryStore<IRegisteredParcel>(testSize));
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
		
		((DimsimRingBuffer)testBuffer).getRingBuffer().clear();

		
		assertEquals("Size is not Zero on clear() ", 0, testBuffer.getSize() );				
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.impl.preGenerics.DimsimRingBuffer#add(org.instrumentmiddleware.cima.parcel.IRegisteredParcel)}.
	 */
	@Test
	public final void testAdd() {
		log.trace("Testing buffer Add");
		((DimsimRingBuffer)testBuffer).getRingBuffer().clear();		
		
		IRegisteredParcel irParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel(ParcelTypeEnum.RESPONSE));				
		testBuffer.add(irParcel);
		
		assertEquals("Add failed to insert Element", 1, testBuffer.getSize());		
	}


	/**
	 * Test method to test Head and Tail on buffer loopAround.
	 */
	private interface LoopAroundTest { public void testNewRegParcelAdd(int seqNo);}
	@Test	
	public final void testLoopAround() {				
		log.trace("Testing buffer LoopAround");
		
		final DimsimRingBuffer mrb = new DimsimRingBuffer(new MemoryStore<IRegisteredParcel>(11));
		final IRegisteredParcel[] regP = new IRegisteredParcel[mrb.getRingBuffer().getCapacity() * 7];		
			
		assertSame("Empty buffer must return a null value for Head", mrb.getRingBuffer().getHead(),null);
		
		LoopAroundTest aMethod = new LoopAroundTest() {
			public void testNewRegParcelAdd(int seqNo) {
				Parcel p = ParcelUtil.newParcel(ParcelTypeEnum.RESPONSE);
				p.setSequenceId(seqNo);
				regP[seqNo] = RegisteredParcel.createRegisteredParcel(p);				
				mrb.add(regP[seqNo]);
				
				if (seqNo < mrb.getRingBuffer().getCapacity()) {
					assertSame("Head parcel must remain same until buffer is full", mrb.getRingBuffer().getHead(),regP[0]);
				} else {
					int oldHead = seqNo - mrb.getRingBuffer().getCapacity();
					//log.info("oldHead, seqNo, cap : " + oldHead + " , " + seqNo + " , " + mrb.getRingBuffer().getCapacity());
					assertNotSame("Head parcel must change when buffer is full", regP[oldHead],mrb.getRingBuffer().getHead());					
					assertSame("Head parcel is not correctly positioned after buffer turns full", 
							regP[oldHead + 1], mrb.getRingBuffer().getHead());
					//Test for getSize(int,int,int) and getSize() methods
					assertEquals("Size does not match capacity on full buffer", mrb.getSize(),mrb.getRingBuffer().getCapacity());
				}	
				/* log.trace("Testing buffer loopAround, seqNo = " + seqNo  
						+ "    HeadParcelSeqNo = " + mrb.getRingBuffer().getHead().getSequenceId()
						+ "    BufferCapacity = " + mrb.getRingBuffer().getCapacity());
				*/	
				//Tail cannot be tested directly. Test indirectly by checking head value on loop around.
			}			
		};
		
		for(int i = 0; i < regP.length; i++) {
			aMethod.testNewRegParcelAdd(i);
		}
		
	}
	

	@Override
	void testGetFromBufferPositiveCallBack(ArrayList<Long> parcelTimes, int count) {

		if (parcelTimes.size() < 3) return;  // need a minimum of three parcels in buffer to test
		
		int parcelStart, parcelEnd, startCountValidate, endCountValidate, midCountValidate;
		long parcelStartTime, parcelEndTime;
		Long[] validateAgainst;		
		
		parcelEnd = random.nextInt(count - 1);
		while (parcelEnd == 0) { parcelEnd = random.nextInt(count - 1); } 
		parcelStart = random.nextInt(parcelEnd);							
		parcelStartTime = parcelTimes.get(parcelStart);							  
		parcelEndTime = parcelTimes.get(parcelEnd);							
		
		int capacity = ((DimsimRingBuffer)testBuffer).getRingBuffer().getCapacity();
		startCountValidate = Math.min((count - parcelStart),capacity);
		endCountValidate = Math.min((count - parcelEnd),capacity);							
		midCountValidate = startCountValidate - endCountValidate;
		if ((endCountValidate != capacity) 
			|| ((count-parcelEnd) == capacity)) {
			midCountValidate = midCountValidate + 1;
		} 
		log.trace("count, pStart, pend, scountval, eval, mval are: " 
				+ count + "," + parcelStart+ "," + parcelEnd +"," 
				+ startCountValidate +"," + endCountValidate + "," + midCountValidate);
		/* */
		testGetSizeLongPositive(parcelStartTime, startCountValidate);
		testGetSizeLongLongPositive(parcelStartTime, parcelEndTime, midCountValidate);
		
		validateAgainst = getSubArray(parcelTimes,Math.min(count,capacity),parcelTimes.size());
		testGetParcelsPositive(validateAgainst);							
		
		validateAgainst =  getSubArray(parcelTimes,startCountValidate,parcelTimes.size());
		testGetParcelsLongPositive(validateAgainst,parcelStartTime);
		
		validateAgainst = getSubArray(parcelTimes,midCountValidate
				,(parcelTimes.size() - startCountValidate + midCountValidate));
		testGetParcelsLongLongPositive(validateAgainst, parcelStartTime,parcelEndTime);		
		
		validateAgainst = getSubArray(parcelTimes,((startCountValidate < capacity)?1:0)
				,(parcelTimes.size() - startCountValidate + 1));
		testGetParcelLongPositive(validateAgainst, parcelStartTime);	
		
		testGetParcelLongNegative(parcelTimes.get(0) - 10); //get a parcel not in the buffer
		
	}

	@Override
	void testGetSize() {
		// Tested elsewhere add() and getFromBufferMethods;
		
	}

	@Override
	void testGetFromBufferNegativeCallBack(ArrayList<Long> parcelTimes, int count) {
				
	}
	
}
