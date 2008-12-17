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

package au.edu.archer.dimsim.buffer.util;

import java.util.Comparator;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;

/**
 * @author mrafi
 *
 */
public class ParcelCreateTimeComparator implements Comparator<Parcel> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 * 
	 * Methods of this class take two parameters and compares them.
	 * The return value is as follows 
	 * 1. If parameter 1 compares to less than parameter 2, then return -1
	 * 2. If parameter 1 equals parameter 2, then return 0
	 * 3. If parameter 1 greater than parameter 2, return +1
	 *  
	 */
	public int compare(Parcel p1, Parcel p2) {
		
		//obvious and null scenarios.
		if (p1 == p2) return 0;		
		if (p1 == null) return -1;		
		if (p2 == null) return 1;
				
		long p1T = p1.getCreationTime().getTimeInMillis();		
		long p2T = p2.getCreationTime().getTimeInMillis();
		
		if (p1T == p2T) 	return 0;
		else if (p2T > p1T)	return -1;
		
		return 1;
	}
	
   public int compare(Parcel p1, long p2T) {
		
		//obvious and null scenarios.				
		if (p1 == null) return -1;		
				
		long p1T = p1.getCreationTime().getTimeInMillis();		
		
		if (p1T == p2T) 	return 0;
		else if (p2T > p1T)	return -1;
		
		return 1;
	}
}
