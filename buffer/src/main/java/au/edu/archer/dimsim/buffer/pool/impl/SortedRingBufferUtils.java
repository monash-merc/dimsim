/**
 * 
 */
package au.edu.archer.dimsim.buffer.pool.impl;

import java.util.Comparator;

import org.apache.log4j.Logger;

import au.edu.archer.dimsim.buffer.pool.store.IStore;

/**
 * @author mrafi
 *
 */
public class SortedRingBufferUtils<T> {
	private static final Logger log = Logger.getLogger(SortedRingBufferUtils.class);
	
	//On occasions, when multiple elements in the buffer have the same comparison value, 
	//binarySearch method does not guarantee the return for first such element.
	//This method verifies previous elements and returns the index of first match in the Ring Buffer
	
	protected static <T> int getFirstLocation(
			final AbstractRingBuffer<T> bufCopy,
			Comparator<? super T> compClass, T inT, int retValFromBinarySearch) {

		if (!(retValFromBinarySearch < 0)) {
			IStore<T> buffer = bufCopy.buffer;
			int prevPos;
			boolean checkPrevious = true;		
			
			if (retValFromBinarySearch == bufCopy.getHeadIndex()) { 
				checkPrevious = false;
			}
			
			while (checkPrevious) {
				checkPrevious = false;
				prevPos = AbstractRingBuffer.getPrevElementPos(retValFromBinarySearch, buffer.getCapacity());
				T prevT = buffer.get(prevPos);			
				int startCompare = compClass.compare(prevT, inT);
				if (startCompare == 0) {	
					checkPrevious = true;
					retValFromBinarySearch = prevPos;
				}
				if (prevPos == bufCopy.getHeadIndex()) { 
					checkPrevious = false;
				}
	        }
		}
		
		return retValFromBinarySearch;
	}

	//On occasions, when multiple elements in the buffer have the same comparison value, 
	//binarySearch method does not guarantee the return of last such element.
	//This method verifies succeeding elements and returns the index of the last match in the Ring Buffer
	protected static <T> int getLastLocation(
			final AbstractRingBuffer<T> bufCopy,
			Comparator<? super T> compClass, T inT, int retValFromBinarySearch) {
		
		if (!(retValFromBinarySearch < 0)) {
			
			IStore<T> buffer = bufCopy.buffer;
			int nextPos;
			boolean checkNext = true;		
			
			if (retValFromBinarySearch  == bufCopy.getCurrTailIndex()) { 
				checkNext = false;
			}
						
			while (checkNext) {
				checkNext = false;
				nextPos = AbstractRingBuffer.getNextElementPos(retValFromBinarySearch, buffer.getCapacity());
				T prevT = buffer.get(nextPos);			
				int startCompare = compClass.compare(prevT, inT);
				if (startCompare == 0) {	
					checkNext = true;
					retValFromBinarySearch = nextPos;
				}
				if (nextPos == bufCopy.getCurrTailIndex()) { 
					checkNext = false;
				}
	        }
		
		}
		
		return retValFromBinarySearch;
	}
	
	//Adapted version of mirko's BinarySearch from URL : http://blog.raner.ws/wordpress/?p=52
	//This search returns the index of an element equal or greater than the search value.
	public static<T> int binarySearch( final AbstractRingBuffer<T> bufCopy, Comparator<? super T> compClass, T inT, int start, int end) {
			      
		IStore<T> buffer = bufCopy.buffer;
		
        if (start > end) {
            return -1; // item not found
        }
        
        //int middle = (start&end)+(start^end)>>1;  //from comment by Schubsi @http://blog.raner.ws/wordpress/?p=52
        int middle = (start + end) / 2;
        
        //log.debug("BinaySearch  Start,Middle,End  = " + start+" , "+ middle +" , " + end);
        
        T startT =  buffer.get(start);
        int startCompare = compClass.compare(startT, inT);
        if (startCompare == 0) {
        	//log.trace("startT equal inT");
        	return start;
        }
        
        T endT =  buffer.get(end);
        int endCompare = compClass.compare(endT, inT);
        if (endCompare == 0) {
        	//log.trace("endT equals inT");
        	return end;
        }
        
        T middleT = buffer.get(middle);
        //log.trace("BinaySearch  searching for, startTime, middleTime = " + timeInMillis + " , " + startTime + "," + middleTime);
        int midCompare = compClass.compare(middleT, inT);
        if (midCompare == 0) {
        	//log.trace("middle equals inT");
            return middle;
        }
        
        int startMid =  compClass.compare(startT, middleT);
        if (startMid  < 0) {
        // => There is no wrap-around in left half of the search area:

            if (startCompare < 0 && midCompare > 0) {
            // => The sought element must be in between the start and the middle:
            	//log.trace("start less than middle which is grater than input ");
                return(Math.max(binarySearch(bufCopy, compClass, inT, start+1, middle),start + 1));
                
            }

            //log.trace("start less than middle, input Not between start and middle ");
            // => search the second half:
            int retVal = binarySearch(bufCopy, compClass, inT, middle+1, end - 1);
            
            //If the binarySearch returns -1, it means that the value is not in buffer.
            //Since startTime is less than MiddleTime, this means that the searched value is 
            //	- either less than both start and middle, in which case return index of the fields.head element of the buffer
            //  - or the element middle+1 is the first element greater than search value. 
            if (retVal == -1 ) {
            	//log.trace("input greater than middle(>start), item not found till end");
            	if (startCompare > 0) {	
            		retVal = bufCopy.fields.head;    
            		//log.trace("returning head ");
            	} else {
            		retVal = middle + 1;
            		//log.trace("returning middle + 1");
            	}
            }
            return retVal;                    
        }

        // => The wrap around is in the left half of the search area (array[start]>array[middle]        
        if (midCompare < 0 && endCompare > 0) {
        	//log.trace("start > middle and item between middle and end ");
            // => the number must be to the right of the middle see [1]
            return (Math.max(binarySearch(bufCopy, compClass, inT, middle+1, end - 1),middle + 1));
        }

        //log.trace("start > middle and item Not Inbetween middle and end, search start to middle ");
        // => the number must be to the left of the middle:
        int retVal = binarySearch(bufCopy, compClass, inT, start+1, middle -1);
        if (retVal == -1) {
        	//log.trace("item not found");
        	if (midCompare > 0) {	
        		//log.trace("returning head");
        		retVal = bufCopy.fields.head;         		
        	} else {
        		//log.trace("returning middle");
        		retVal = middle;
        	}	
        }
        return retVal;
    }
	
	//This search returns the index of the first element equal or greater than(if equal not found) the search value.
	public static<T> int binarySearchFirst( final AbstractRingBuffer<T> bufCopy, Comparator<? super T> compClass, T inT, int start, int end) {
		
		int retVal = binarySearch(bufCopy,compClass, inT, start, end);
		
		if (retVal != -1) {
			return getFirstLocation(bufCopy, compClass, inT, retVal);
		}
		
		return retVal;
	}

	//This search returns the index of the Last element equal or if not found, the first element greater than the search value.
	public static<T> int binarySearchLast( final AbstractRingBuffer<T> bufCopy, Comparator<? super T> compClass, T inT, int start, int end) {
		
		int retVal = binarySearch(bufCopy,compClass, inT, start, end);
		
		if (retVal != -1) {
			return getLastLocation(bufCopy, compClass, inT, retVal);
		}
		
		return retVal;
	}
		

}
