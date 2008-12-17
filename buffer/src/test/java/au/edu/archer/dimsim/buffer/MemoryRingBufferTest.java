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
import java.util.Random;

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.impl.RegisteredParcel;
import au.edu.archer.dimsim.buffer.impl.DeliveryBuffer;
import au.edu.archer.dimsim.buffer.pool.impl.preGenerics.MemoryRingBuffer;

import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.log4j.Logger;

/**
 * @author mrafi
 * to test run mvn -Dtest=MemoryRingBufferTest test 
 */
public class MemoryRingBufferTest {	
	static Logger log = Logger.getLogger(MemoryRingBufferTest.class);
	/**
	 * @throws java.lang.Exception
	 */	
	
	final int SECOND_IN_MILLIS = 1000;
	final int MINUTE_IN_MILLIS = 60 * SECOND_IN_MILLIS;
	final int HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
	final int DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
	final int WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
	final int MONTH_IN_MILLIS = DAY_IN_MILLIS * 30;
	final int YEAR_IN_MILLIS = DAY_IN_MILLIS * 365;		
	
	MemoryRingBuffer mrbTest;
	private final int testSize = 999;
	private final Parcel testParcel = ParcelUtil.newParcel();
	private final IRegisteredParcel regParcel = RegisteredParcel.createRegisteredParcel(testParcel);
	
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
		mrbTest = new MemoryRingBuffer(this.testSize);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		mrbTest.clear();
		mrbTest = null;
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.impl.preGenerics.MemoryRingBuffer#MemoryRingBuffer(int)}.
	 */
	@Test
	public final void testMemoryRingBuffer() {
		
		log.trace("Testing buffer Init and Capacity");
		
		MemoryRingBuffer mrb;
		//Test failsafe constructor for size
		mrb = new MemoryRingBuffer();
		assertEquals("MemoryRingBuffer Size must match default size", mrb.getCapacity(),DeliveryBuffer.default_capacity);
		
		mrb = new MemoryRingBuffer(-4);
		assertEquals("MemoryRingBuffer Size must match default size", mrb.getCapacity(),DeliveryBuffer.default_capacity);
		
		mrb = new MemoryRingBuffer(this.testSize);
		assertEquals("MemoryRingBuffer Size does not match input size " + this.testSize, mrb.getCapacity(),testSize );
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.impl.preGenerics.MemoryRingBuffer#clear()}.
	 */
	@Test
	public final void testClear() {
		log.trace("Testing buffer Clear");
		mrbTest.add(this.regParcel);
		mrbTest.add(this.regParcel);
		mrbTest.add(this.regParcel);
		mrbTest.clear();
		assertEquals("Size is not Zero on clear() ", mrbTest.getSize(),0 );				
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.impl.preGenerics.MemoryRingBuffer#add(org.instrumentmiddleware.cima.parcel.IRegisteredParcel)}.
	 */
	@Test
	public final void testAdd() {
		log.trace("Testing buffer Add");
		mrbTest.clear();		
		
		IRegisteredParcel irParcel = RegisteredParcel.createRegisteredParcel(ParcelUtil.newParcel(ParcelTypeEnum.RESPONSE));				
		mrbTest.add(irParcel);
		
		assertEquals("Add failed to insert Element", 1, mrbTest.getSize());
		assertSame("Add failed insert element is different from input element", irParcel, mrbTest.getHead());
	}


	/**
	 * Test method to test Head and Tail on buffer loopAround.
	 */
	private interface LoopAroundTest { public void testNewRegParcelAdd(int seqNo);}
	@Test	
	public final void testLoopAround() {				
		log.trace("Testing buffer LoopAround");
		
		final MemoryRingBuffer mrb = new MemoryRingBuffer(11);
		final IRegisteredParcel[] regP = new IRegisteredParcel[mrb.getCapacity() * 7];		
			
		assertSame("Empty buffer must return a null value for Head", mrb.getHead(),null);
		
		LoopAroundTest aMethod = new LoopAroundTest() {
			public void testNewRegParcelAdd(int seqNo) {
				Parcel p = ParcelUtil.newParcel(ParcelTypeEnum.RESPONSE);
				p.setSequenceId(seqNo);
				regP[seqNo] = RegisteredParcel.createRegisteredParcel(p);				
				mrb.add(regP[seqNo]);
				
				if (seqNo < mrb.getCapacity()) {
					assertSame("Head parcel must remain same until buffer is full", mrb.getHead(),regP[0]);
				} else {
					int oldHead = seqNo - mrb.getCapacity();					
					assertNotSame("Head parcel must change on buffer is full", mrb.getHead(),regP[oldHead]);					
					assertSame("Head parcel must is not correctly positioned after buffer turns full", 
							mrb.getHead(),regP[oldHead + 1]);
					//Test for getSize(int,int,int) and getSize() methods
					assertEquals("Size does not match capacity on full buffer", mrb.getSize(),mrb.getCapacity());
				}	
				/*log.trace("Testing buffer loopAround, seqNo = " + seqNo  
						+ "    HeadParcelSeqNo = " + mrb.getHead().getSequenceId()
						+ "    BufferCapacity = " + mrb.getCapacity());
						*/
				//Tail cannot be tested directly. Test indirectly by checking head value on loop around.
			}			
		};
		
		for(int i = 0; i < regP.length; i++) {
			aMethod.testNewRegParcelAdd(i);
		}
		
	}
	/**
	 * Test method for all getParcel and getSize methods.
	 */	
	@Test
	public final void testGetFromBufferMethods() {
		log.trace("Testing buffer getSize(startTime)");
		log.trace("Testing buffer getSize(startTime, endTime)");
		log.trace("Testing buffer getBuffer()");
		log.trace("Testing buffer getBuffer(startTime)");
		log.trace("Testing buffer getBuffer(startTime, endTime)");
		
		final int max_num = 5;
		final int START_YEAR = 1997;
		final Random random = new Random(System.currentTimeMillis());
		ArrayList<Long> parcelTimes = new ArrayList<Long> ();
		int parcelStart, parcelEnd, startCountValidate, endCountValidate, midCountValidate, count = 0;
		long parcelStartTime, parcelEndTime;
		Long[] validateAgainst;
		IRegisteredParcel tmpParcel;
				
		for(int year= START_YEAR; year < START_YEAR + max_num; year+=random.nextInt(3) + 1) {			
			for(int month=1;month<max_num;month+=random.nextInt(13) + 1){
				for(int day=1;day<29;day+=random.nextInt(11) + 1) {						
					for(int hour=0;hour<24;hour+=random.nextInt(7) + 1){							
						for(int min=0;min<60;min+=random.nextInt(17) + 1){																
							for(int sec=0;sec<60;sec+=random.nextInt(19) + 1){
								tmpParcel = getDatedParcel(year,month,day,hour,min,sec);								
								mrbTest.add(tmpParcel);
								parcelTimes.add(tmpParcel.getCreationTime().getTimeInMillis());								
								count++;								
							}  //end of seconds loop
						}//end of minute loop
						//Test after each hour
							parcelEnd = random.nextInt(count - 1);
							while (parcelEnd == 0) { parcelEnd = random.nextInt(count - 1); } 
							parcelStart = random.nextInt(parcelEnd);							
							parcelStartTime = parcelTimes.get(parcelStart);							  
							parcelEndTime = parcelTimes.get(parcelEnd);							
							
							startCountValidate = Math.min((count - parcelStart),mrbTest.getCapacity());
							endCountValidate = Math.min((count - parcelEnd),mrbTest.getCapacity());							
							midCountValidate = startCountValidate - endCountValidate;
							if ((endCountValidate != mrbTest.getCapacity()) 
								|| ((count-parcelEnd) == mrbTest.getCapacity())) {
								midCountValidate = midCountValidate + 1;
							} 
							/*log.debug("count, start, end, sval, eval, mval are: " 
									+ count + "," + parcelStart+ "," + parcelEnd +"," 
									+ startCountValidate +"," + endCountValidate + "," + midCountValidate);
									*/
							testGetSizeLong(parcelStartTime, startCountValidate);
							testGetSizeLongLong(parcelStartTime, parcelEndTime, midCountValidate);
							
							validateAgainst = getSubArray(parcelTimes,Math.min(count,mrbTest.getCapacity()),parcelTimes.size());
							testGetParcels(validateAgainst);							
							
							validateAgainst =  getSubArray(parcelTimes,startCountValidate,parcelTimes.size());
							testGetParcelsLong(validateAgainst,parcelStartTime);
							
							validateAgainst = getSubArray(parcelTimes,midCountValidate
									,(parcelTimes.size() - startCountValidate + midCountValidate));
							testGetParcelsLongLong(validateAgainst, parcelStartTime,parcelEndTime);												
					}	//End of Hour Loop					
				}	//End of Day Loop									
			} //End of Month Loop
		}	//End of Year Loop	
		
		log.debug("count = " + count );
		
	}

	public final void testGetSizeLong(long timeMilli, int countAfter) {	
		//log.trace("getSizeLong");
		assertEquals("GetSize failed for startTime " + timeMilli,mrbTest.getSize(timeMilli),countAfter);		
	}
	
	public final void testGetSizeLongLong(long timeMilliStart, long timeMilliEnd, int countInBetween) {
		//log.trace("getSizeLongLong");
		assertEquals("GetSize failed for <startTime,endTime>  <" + timeMilliStart + "," + timeMilliEnd + ">"
				,mrbTest.getSize(timeMilliStart,timeMilliEnd),countInBetween);		
	}	
	
	public final void testGetParcels(Long[] validateAgainst) {
		//log.trace("getParcel");
		ArrayList<IRegisteredParcel> regArrList = mrbTest.getParcels();		
		checkParcels(validateAgainst,regArrList);
	}
	
	public final void testGetParcelsLong(Long[] validateAgainst, long startTime) {		
		//log.trace("getParcelLong");
		ArrayList<IRegisteredParcel> regArrList = mrbTest.getParcels(startTime);		
		checkParcels(validateAgainst, regArrList);
		
	}

	
	public final void testGetParcelsLongLong(Long[] validateAgainst, long startTime, long endTime) {
		//log.trace("getParcelLongLong");
		ArrayList<IRegisteredParcel> regArrList = mrbTest.getParcels(startTime,endTime);		
		checkParcels(validateAgainst, regArrList);		
	}
	
	/**
	 * @param validateAgainst
	 * @param regList
	 */
	private void checkParcels(Long[] validateAgainst, ArrayList<IRegisteredParcel> regArrList) {
		
		if (regArrList != null) {			
			assertTrue("getParcel(): Parcels returned when none expected", (validateAgainst != null));
			assertTrue("getParcel(): Parcel return count " + regArrList.size() + " does not match expected value" + validateAgainst.length
					,(validateAgainst.length == regArrList.size()));
			
			for(int i=0;i<regArrList.size();i++) {
				assertEquals("getParcel() :parcel " + i + " does not match expected value", 
						validateAgainst[i].longValue(), regArrList.get(i).getCreationTime().getTimeInMillis());
			}
		} else {
			assertTrue("getParcel(): Expected parcels not returned", (validateAgainst == null));
		}	 
	}

	
	private Long[] getSubArray(ArrayList<Long> parcelList, int size, int lastIndexPlusOne) {
		//log.trace("SubArray size, lastIndexPlusOne is " + size + "," + lastIndexPlusOne);
		
		Long[] retArr = null;
		
		int pSize = parcelList.size();		
		if ((size == 0) 
			|| (parcelList == null)
			|| (pSize < size)
			|| (lastIndexPlusOne > pSize))
		{
			return retArr;
		}
		
		retArr = new Long[size];		 
		for(int i = size; i > 0; i-- ) {
			retArr[size - i] = parcelList.get(lastIndexPlusOne - i);
		}
		return retArr;
	}
	
	@Test
	public final void testGetSizeIntIntInt() {
		log.trace("Testing complete elsewhere, getSize(int,int,int)"); //tested in testLoopAround() method.
	}	

	@Test
	public final void testBinarySearchInRingBuffer() {
		log.trace("Test complete elsewhere, BinarySearchInRingBuffer");	//tested As part of GetSize and GetBuffer
	}

	
	private static final IRegisteredParcel getDatedParcel(int year
											, 	int month
											,	int day
											,	int hour
											,	int minute
											,	int	second ) {
		final Parcel parcel = ParcelUtil.newParcel();		
		
		final Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, hour, minute, second);		
		parcel.setCreationTime(cal);
		
		return RegisteredParcel.createRegisteredParcel(parcel);			
	}
	
}
