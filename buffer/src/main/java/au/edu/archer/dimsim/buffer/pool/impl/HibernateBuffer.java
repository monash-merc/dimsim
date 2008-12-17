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
package au.edu.archer.dimsim.buffer.pool.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelDocument;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.impl.RegisteredParcel;

import org.springframework.stereotype.Repository;
import org.apache.xmlbeans.XmlException;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;

import au.edu.archer.dimsim.buffer.pool.IBuffer;
import au.edu.archer.dimsim.buffer.pool.store.impl.DimsimORMStore;

/**
 * @author mrafi
 *
 */
@Repository
public class HibernateBuffer implements IBuffer {

	SessionFactory sessionFactory;   // Set by Spring IOC
	/**
	 * 
	 */
	public HibernateBuffer(SessionFactory factory) {
		this.sessionFactory = factory;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#add(org.instrumentmiddleware.cima.parcel.IRegisteredParcel)
	 */
	public IRegisteredParcel add(IRegisteredParcel registeredParcel) {		
		IRegisteredParcel retVal = null;
		Long idTS;
		
		String parcelXML = registeredParcel.xmlText();
		
		Calendar cal = registeredParcel.getCreationTime();
		if (cal == null) idTS = System.currentTimeMillis();
		else idTS = cal.getTimeInMillis();
		
		if (this.sessionFactory != null) {			
			DimsimORMStore buff = new DimsimORMStore();
			buff.setParcelXML(parcelXML);
			buff.setTimeStamp(idTS);		
			this.sessionFactory.getCurrentSession().save(buff);        
		}
		
		return retVal;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getParcels()
	 */
	public ArrayList<IRegisteredParcel> getParcels() {		
		
		if (this.sessionFactory != null) {					
			List<DimsimORMStore> recList = this.sessionFactory.getCurrentSession().createQuery("from DimsimBufferORM").list();
			return db2IRegParcel(recList);
		}		
		
		return null;
	}

	/**
	 * @param recList
	 * @return
	 */
	private ArrayList<IRegisteredParcel> db2IRegParcel(List<DimsimORMStore> recList) {
		
		ArrayList<IRegisteredParcel>  retArr = new ArrayList<IRegisteredParcel> ();
		
		IRegisteredParcel regP;
		
		for (int i = 0;i<recList.size();i++) {
			DimsimORMStore r = recList.get(i);   
			Parcel p = null;
			try {
				p = ParcelDocument.Factory.parse(r.getParcelXML()).getParcel();
			} catch (XmlException e) {					
				e.printStackTrace();
			}
			regP = RegisteredParcel.createRegisteredParcel(p);
			retArr.add(regP);
		}
		
		return retArr;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getParcels(long)
	 */
	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis) {
		
		if (this.sessionFactory != null) {
			List<DimsimORMStore> recList = this.sessionFactory.getCurrentSession()
									.createQuery("from DimsimBuffer where TimeStamp >= :ts")
									.setParameter("ts", startTimeInMillis).list();
			return db2IRegParcel(recList);
		}
			
		return null;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getParcels(long, long)
	 */
	public ArrayList<IRegisteredParcel> getParcels(long startTimeInMillis,
			long endTimeInMillis) {

		if (startTimeInMillis > endTimeInMillis) return getParcels(startTimeInMillis);
		
		if (this.sessionFactory != null) {
			List<DimsimORMStore> recList = this.sessionFactory.getCurrentSession()
									.createQuery("from DimsimBuffer where TimeStamp >= :ts and TimeStamp <= :te")
									.setParameter("ts", startTimeInMillis)
									.setParameter("te", endTimeInMillis)
									.list();
			return db2IRegParcel(recList);
		}
			
		return null;
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getSize()
	 */
	public int getSize() {
		
		if (this.sessionFactory == null) return -1;
		
		Criteria criteria = this.sessionFactory.getCurrentSession().createCriteria(DimsimORMStore.class);
		criteria.setProjection(Projections.rowCount());
		
		return ((Integer)criteria.list().get(0)).intValue();		
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getSize(long)
	 */
	public int getSize(long startTimeInMillis) {
		
		if (this.sessionFactory == null) return -1;
		
		List<Integer> recList = this.sessionFactory.getCurrentSession()
		.createQuery("select count(*) from DimsimBuffer where TimeStamp >= :ts and TimeStamp <= :te")
		.setParameter("ts", startTimeInMillis)		
		.list();
		
		return ((Integer)recList.get(0)).intValue();
	}

	/* (non-Javadoc)
	 * @see au.edu.archer.dimsim.buffer.IBuffer#getSize(long, long)
	 */
	public int getSize(long startTimeInMillis, long endTimeInMillis) {

		if (this.sessionFactory == null) return -1;
		
		List<Integer> recList = this.sessionFactory.getCurrentSession()
		.createQuery("select count(*) from DimsimBuffer where TimeStamp >= :ts and TimeStamp <= :te")
		.setParameter("ts", startTimeInMillis)
		.setParameter("te", endTimeInMillis)
		.list();
		
		return ((Integer)recList.get(0)).intValue();
	}

	public int clear() {
		if (this.sessionFactory == null) return -1;
		
		int deletedEntities = this.sessionFactory.getCurrentSession()
		.createQuery("delete DimsimBuffer ").executeUpdate();
		
		return deletedEntities;
		
	}

	public IRegisteredParcel getParcel(long timeInMillis) {		 
		ArrayList<IRegisteredParcel> list = getParcels(timeInMillis, timeInMillis);
		
		if ((list.size() > 0) 
				  && (list.get(0).getCreationTime().getTimeInMillis() == timeInMillis)) {
			return list.get(0);
		}
		
		return null;
	}
}
