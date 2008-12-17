
package au.edu.archer.dimsim.buffer.pool.impl;

import java.util.Comparator;

import au.edu.archer.dimsim.buffer.pool.store.IStore;
import au.edu.archer.dimsim.buffer.pool.store.impl.MemoryVectorStore;

/**
 * @author mrafi
 *
 */

public class RandomInputSortedOutputRingBuffer<T> extends AbstractRingBuffer<T> {
	
	Comparator<T> comparator;
	
	public RandomInputSortedOutputRingBuffer() {
		this(null);
	}

	public RandomInputSortedOutputRingBuffer(Comparator<T> comparator) {
		super(new MemoryVectorStore<T>(-1));
		this.comparator = comparator;
		if (comparator == null) {
			comparator = new Comparator<T> () {
				public int compare(T o1, T o2) {return (o1.hashCode() - o2.hashCode());}		
			}; 
		}		
	}

	synchronized public T add(T obj) {
		int retValFromBinarySearch = SortedRingBufferUtils.binarySearch(this,comparator,obj, 0,this.getSize() - 1);		
		int insPos = SortedRingBufferUtils.getLastLocation(this, comparator, obj, retValFromBinarySearch);
		if (insPos < 0) {
			insPos = 0;
		}
		
		T retVal = this.buffer.put(insPos, obj);		
		fields.currTail = this.buffer.getSize() - 1;
		//Note: Head never changes index, always at '0' 
		//  because the underlying store is a Vector of fixed size
		
		return retVal;
	}

	@Override
	protected AbstractRingBuffer<T> getNewInstance(final IStore<T> store) {
		
		RandomInputSortedOutputRingBuffer<T> retVal = 
			new RandomInputSortedOutputRingBuffer<T>(comparator);		
		
		if (store != null) {
			if (!(store instanceof MemoryVectorStore)) {					
				MemoryVectorStore<T> newStore = new MemoryVectorStore<T> (store.getCapacity());
				for(int i = 0; i < store.getCapacity(); i++) {
					newStore.put(i, store.get(i));
				}			
				retVal.buffer = newStore;
			} else {
				retVal.buffer = store;
			}
		}
		
		return retVal;		
	}
}
