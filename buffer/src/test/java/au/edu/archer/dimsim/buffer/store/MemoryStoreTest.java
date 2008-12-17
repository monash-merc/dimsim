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

package au.edu.archer.dimsim.buffer.store;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.edu.archer.dimsim.buffer.pool.store.impl.MemoryStore;

/**
 * @author mrafi
 *
 */
public class MemoryStoreTest {	
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link au.edu.archer.dimsim.buffer.pool.store.impl.MemoryStore#put(int, java.lang.Object)}.
	 */
	@Test
	public final void testAll() {
		final int CAPACITY = 23;
		MemoryStore<Integer> m = new MemoryStore<Integer>(CAPACITY);
		
		assertEquals("MemoryStore Capacity incorrect ", m.getCapacity(), CAPACITY );
		assertEquals("MemoryStore Size must be fixed to capacity ", m.getCapacity(), m.getSize() );
		
		for(int i = 0; i < m.getSize(); i++) {
			m.put(i, new Integer(i));
			assertTrue("MemoryStore Size must be fixed to capacity " , (i != m.getSize()) );			
		}
		
		int j = m.getSize();
		m.put(j,new Integer(j));
		assertEquals("MemoryStore get(index) return value is incorrect", null, m.get(j));
		assertEquals("MemoryStore get(index) return value is incorrect", null, m.get(-1));
		
		for(int i = 0; i < m.getSize(); i++) {			
			assertEquals("MemoryStore get(index) return value is incorrect", i, m.get(i).intValue());
		}
		
		m.clear();
		for(int i = 0; i < m.getSize(); i++) {			
			assertEquals("MemoryStore get(index) return value is incorrect", null, m.get(i));
		}
		
	}

}
