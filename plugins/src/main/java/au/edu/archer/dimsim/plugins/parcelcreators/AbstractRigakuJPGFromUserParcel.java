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

package au.edu.archer.dimsim.plugins.parcelcreators;

import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;

/**
 * @author mrafi
 * 
 * This abstract class is used to restrict JPG creation to specified SourceProducerID.
 * There is no way for the newRigakuParcel() method to check against the input SourceProducerID. 
 * It is the responsibility of the extending class to ensure that the input parcel is from the relevant SourceProducer
 * 
 */
public abstract class AbstractRigakuJPGFromUserParcel extends
		RigakuJPGCreatorFromParcel {  

	protected String sourceProducerID;
	public void setSourceProducerID(String sourceProducerID) {		
		this.sourceProducerID = sourceProducerID;			
	}
	
	public AbstractRigakuJPGFromUserParcel(String producerOfInterestID) {
		super();
		this.setSourceProducerID(producerOfInterestID);
	}
	
	public String getSourcePluginID() {		 
		 return this.sourceProducerID;
	 }

    protected abstract boolean isParcelSourceValid(IRegisteredParcel parcel);

	public void newRigakuParcel(IRegisteredParcel rParcel) {
		if (isParcelSourceValid(rParcel)) {
			super.newRigakuParcel(rParcel);
		}
	}
}
