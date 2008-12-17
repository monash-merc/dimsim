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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.util.Base64;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.util.FileUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;

import au.edu.archer.cima.xtal.XtalImageType;
import au.edu.archer.dimsim.plugins.IRigakuConstants;

/**
 * @author mrafi
 *
 */
public class RigakuCCDParcelCreator extends AbstractRigakuParcelCreator {
	private static Logger log = Logger.getLogger(RigakuCCDParcelCreator.class);

	private boolean inlineImage = false; //if true, image is encoded within parcel, else just filename is sent	
	
	public void setInlineImage(boolean flag) {
		this.inlineImage = flag;
	}	
	
	@Override
	public Parcel processRigakuFile(File file) {
		Parcel parcel = null;		
		
		try {
			if (isValidRigakuFile(file)) {
				log.info("RigakuParcelCreator: New osc file found " + file.getAbsolutePath());			
				parcel = ParcelUtil.newParcel(ParcelTypeEnum.PLUGIN);            
				byte[] encodedBytes = null;

				BodyType body = parcel.addNewBody();
				XtalImageType bodyR = (XtalImageType)body.changeType(XtalImageType.type);
            
				if ((this.inlineImage) &&
            		(file.length() < IRigakuConstants.maxEncodeSizeInKB * 1000))  {
            	    encodedBytes = Base64.encode(FileUtil.getBytesFromFile(file));
            	}
            	bodyR.setFileContent(encodedBytes);
    
            	bodyR.setDirectory(file.getParent());
            	bodyR.setFileName(file.getName());
            	bodyR.setFileLocation(FILE_URL_PROTOCOL + file.getPath());
            
            	Calendar createTime = Calendar.getInstance();
            	createTime.setTimeInMillis(file.lastModified());
            	bodyR.setTime(createTime);
			}			
		} catch (IOException ioe) {
			log.debug(ioe.getMessage());
			ioe.printStackTrace();
		}
	
		return parcel;		
	}

}
