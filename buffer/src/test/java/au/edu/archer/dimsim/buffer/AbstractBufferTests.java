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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.impl.RegisteredParcel;
import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.junit.Test;

import au.edu.archer.dimsim.buffer.pool.IBuffer;

/**
 * @author mrafi
 *
 */
public abstract class AbstractBufferTests {

	IBuffer testBuffer;
	public enum TEST_PERIOD { YEAR,MONTH,DAY,HOUR,MIN,SEC };
	TEST_PERIOD testPeriod = TEST_PERIOD.HOUR;
	
	abstract void testAdd();
	abstract void testGetFromBufferPositiveCallBack(ArrayList<Long> p,int c);
	abstract void testGetFromBufferNegativeCallBack(ArrayList<Long> p,int c);
	abstract void testGetSize();
	
	public static final int SECOND_IN_MILLIS = 1000;
	public static final int MINUTE_IN_MILLIS = 60 * SECOND_IN_MILLIS;
	public static final int HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
	public static final int DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
	public static final int WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;
	public static final int MONTH_IN_MILLIS = DAY_IN_MILLIS * 30;
	public static final int YEAR_IN_MILLIS = DAY_IN_MILLIS * 365;	
	final Random random = new Random(System.currentTimeMillis());
	
	final Parcel testParcel = ParcelUtil.newParcel();
	final IRegisteredParcel regParcel = RegisteredParcel.createRegisteredParcel(testParcel);
		
	static Logger log = Logger.getLogger(AbstractBufferTests.class);	
	
	@Test
	public final void testGetFromBufferMethods() {
		log.trace("Testing buffer getSize(startTime)");
		log.trace("Testing buffer getSize(startTime, endTime)");
		log.trace("Testing buffer getBuffer()");
		log.trace("Testing buffer getBuffer(startTime)");
		log.trace("Testing buffer getBuffer(startTime, endTime)");
		
		final int max_num = 5;
		final int START_YEAR = 1997;		
		ArrayList<Long> parcelTimes = new ArrayList<Long> ();		
		IRegisteredParcel tmpParcel;
		int count = 0;
				
		for(int year= START_YEAR; year < START_YEAR + max_num; year+=random.nextInt(3) + 1) {			
			for(int month=1;month<max_num;month+=random.nextInt(13) + 1){
				for(int day=1;day<29;day+=random.nextInt(11) + 1) {						
					for(int hour=0;hour<24;hour+=random.nextInt(7) + 1){							
						for(int min=0;min<60;min+=random.nextInt(17) + 1){																
							for(int sec=0;sec<60;sec+=random.nextInt(19) + 1){
								tmpParcel = getDatedParcel(year,month,day,hour,min,sec);								
								testBuffer.add(tmpParcel);
								parcelTimes.add(tmpParcel.getCreationTime().getTimeInMillis());								
								count++;								
							}  //end of seconds loop
							if (testPeriod == TEST_PERIOD.SEC)  testGetFromBufferPositiveCallBack(parcelTimes,count);
						}//end of minute loop
						if (testPeriod == TEST_PERIOD.MIN)  testGetFromBufferPositiveCallBack(parcelTimes,count);
					}	//End of Hour Loop				
					if (testPeriod == TEST_PERIOD.HOUR)  testGetFromBufferPositiveCallBack(parcelTimes,count);
				}	//End of Day Loop					
				if (testPeriod == TEST_PERIOD.DAY)  testGetFromBufferPositiveCallBack(parcelTimes,count);
			} //End of Month Loop
			if (testPeriod == TEST_PERIOD.MONTH)  testGetFromBufferPositiveCallBack(parcelTimes,count);
		}	//End of Year Loop	
		if (testPeriod == TEST_PERIOD.YEAR)  testGetFromBufferPositiveCallBack(parcelTimes,count);
		
		log.debug("count = " + count );
		
	}
	
	
	public final void testGetSizeLongPositive(long timeMilli, int countAfter) {	
		//log.trace("getSizeLong");
		assertEquals("GetSize failed for startTime " + timeMilli,testBuffer.getSize(timeMilli),countAfter);		
	}
	
	public final void testGetSizeLongLongPositive(long timeMilliStart, long timeMilliEnd, int countInBetween) {
		//log.trace("getSizeLongLong");
		assertEquals("GetSize failed for <startTime,endTime>  <" + timeMilliStart + "," + timeMilliEnd + ">"
				,testBuffer.getSize(timeMilliStart,timeMilliEnd),countInBetween);		
	}	
	
	public final void testGetParcelsPositive(Long[] validateAgainst) {
		//log.trace("getParcel");
		ArrayList<IRegisteredParcel> regArrList = testBuffer.getParcels();		
		checkParcels(validateAgainst,regArrList);
	}
	
	public final void testGetParcelsLongPositive(Long[] validateAgainst, long startTime) {		
		//log.trace("getParcelLong");
		ArrayList<IRegisteredParcel> regArrList = testBuffer.getParcels(startTime);		
		checkParcels(validateAgainst, regArrList);
		
	}

	
	public final void testGetParcelsLongLongPositive(Long[] validateAgainst, long startTime, long endTime) {
		//log.trace("getParcelLongLong");
		ArrayList<IRegisteredParcel> regArrList = testBuffer.getParcels(startTime,endTime);		
		checkParcels(validateAgainst, regArrList);		
	}
	
	public void testGetParcelLongPositive(Long[] validateAgainst, long timeInMillis) {
		IRegisteredParcel regP = testBuffer.getParcel(timeInMillis);
		if (regP != null) {
			assertTrue("getParcel(timeInMillis): Expected parcel not returned", (validateAgainst != null));
			assertEquals("getParcel(timeInMillis) :parcel does not match expected value", 
					validateAgainst[0].longValue(), regP.getCreationTime().getTimeInMillis());
			
		} else {
			assertTrue("getParcel(timeInMillis): Expected parcel not returned", (validateAgainst == null));
		}
		
	}

	public void testGetParcelLongNegative(long invalidTimeInMillis) {
		IRegisteredParcel regP = testBuffer.getParcel(invalidTimeInMillis);
		assertTrue("getParcel(timeInMillis): unexpected Parcel returned", (regP == null));		
	}
	
	/**
	 * @param validateAgainst
	 * @param regList
	 */
	public static void checkParcels(Long[] validateAgainst, ArrayList<IRegisteredParcel> regArrList) {
		
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

	
	public static Long[] getSubArray(ArrayList<Long> parcelList, int size, int lastIndexPlusOne) {
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
	
	public static final IRegisteredParcel getDatedParcel(int year
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
