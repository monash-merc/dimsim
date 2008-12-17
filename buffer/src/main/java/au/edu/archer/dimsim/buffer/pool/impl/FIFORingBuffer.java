/**
 * 
 */
package au.edu.archer.dimsim.buffer.pool.impl;

import au.edu.archer.dimsim.buffer.pool.store.IStore;

/**
 * @author mrafi
 *
 */
public class FIFORingBuffer<T> extends AbstractRingBuffer<T> {
	/**
	 * 
	 */
	public FIFORingBuffer() {
		this(null);
	}

	/**
	 * @param bufStore
	 */
	public FIFORingBuffer(IStore<T> bufStore) {
		super(bufStore);
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.pool.impl.AbstractRingBuffer#add(java.lang.Object)
	 */
	@Override
	synchronized public T add(T obj) {		
		T oldParcel = null;		
		
		synchronized(this.fields) {
			//Move fields.head if buffer full			
			if (getSize() == getCapacity()) {	
				oldParcel = buffer.get(getHeadIndex());
				fields.head = getNextElementPos(getHeadIndex(), getCapacity());				
			} 
			
			fields.currTail = getNextElementPos(getCurrTailIndex(), getCapacity());
			buffer.put(fields.currTail,obj);
			this.bufferModified = true;
			
		}				
		
		return oldParcel;		
	}

	@Override
	protected AbstractRingBuffer<T> getNewInstance(IStore<T> store) {
		return new FIFORingBuffer<T>(store);
	}
	
	
}
