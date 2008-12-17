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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Vector;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.util.Base64;

import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.util.FileUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;

import au.edu.archer.cima.xtal.BeginXtalExperimentType;
import au.edu.archer.cima.xtal.EndXtalExperimentType;
import au.edu.archer.cima.xtal.XtalImageType;
import au.edu.archer.dimsim.buffer.util.ParcelUtils;
import au.edu.archer.dimsim.plugins.IRigakuConstants;
//import au.edu.archer.dimsim.xraycrystallography.RigakuSaturn944;
import au.edu.usyd.xraycrystallography.rigaku.ImageConverter;
import au.edu.usyd.xraycrystallography.rigaku.ImageRigaku;

/*
 * Creates JPG Images on receiving a new Rigaku parcel.
 * To get this parcel creator to work, invoke newRigakuParcel() method at appropriate time.
 *  
 */
public class RigakuJPGCreatorFromParcel 
	extends PluginParcelCreator	implements IRigakuConstants {

	//if an Experiment Start and End parcel must be created, then set this to true.
	//Use Cases for standAloneMode variable : 
	//		- SRBConsumer stores JPEG only and not OSC or IMG file as is the case with IMB
	//
	boolean standAloneMode = false;
	
	Logger log = Logger.getLogger(RigakuJPGCreatorFromParcel.class);
	
	String  defaultFileName = null;
	
	public void setStandAloneMode(boolean standAloneMode) {
		this.standAloneMode = standAloneMode;
	}	
	
	public void setDefaultFileName(String defaultFileName) {
		this.defaultFileName = defaultFileName;
	}

	//A subclass must invoke newRigakuParcel() method for this class to work correctly.
	public void newRigakuParcel(IRegisteredParcel regParcel) {
		
		BodyType b = regParcel.getBody();						
		if ((b instanceof EndXtalExperimentType) | 
		  	(b instanceof BeginXtalExperimentType)) {
			if (this.standAloneMode) {
				log.debug("Parcel "+ b.getClass().getName() + " created for JPG ");
				Parcel expParcel = ParcelUtil.newParcel(ParcelTypeEnum.PLUGIN);
				expParcel.setBody(b);
				super.setNewParcel(expParcel);				
			} else {
				log.debug("Experiment start/stop parcel not created for JPG: StandAlone mode is " + this.standAloneMode);
			}
		} else {
			try {
				getNewJPGParcel(regParcel);
			} catch (FileNotFoundException e) {
				log.debug(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				log.debug(e.getMessage());
				e.printStackTrace();
			}
		}		
	}

	protected void getNewJPGParcel(IRegisteredParcel p) throws FileNotFoundException, IOException {
		Calendar imageTime = null;
		String imageFileName = null;
		
		if (p !=  null) {
			BodyType b = p.getBody();
			File jpegFileArr[] = null;			
			
			if (b instanceof XtalImageType) {
				XtalImageType baseBody = (XtalImageType)b;
				
				imageFileName = p.getSequenceId() + "";
				if (baseBody.isSetFileName()) {
					imageFileName = new File(baseBody.getFileName()).getName();										
				} else if (p instanceof IRegisteredParcel) {
					imageFileName = ParcelUtils.getSender((IRegisteredParcel)p) + "_" + imageFileName;
				}
								
				@SuppressWarnings("unused")
				rigakuFileTypes inImageType = rigakuFileTypes.UNKNOWN;
				if (baseBody.isSetFileLocation()) {
					URL fileURL = new URL(baseBody.getFileLocation());
					File inFile = new File(fileURL.getFile());
					String fileName = inFile.getName();
					if (fileName.endsWith(rigakuFileTypes.OSC.getName())) {
						//Not needed since convertIMG2JPEG below can handle OSC and IMG files.
						//But if structure studio libraries are not available, 
						//	- fall back to USyd Conversion code for OSC files
						inImageType = rigakuFileTypes.OSC;
						jpegFileArr = USydConversion(inFile);
			//		} else {												
			//			jpegFileArr = RigakuSaturn944.convertIMG2JPEG(inFile);
			//		}
			//	} else {												
			//		if (baseBody.isSetFileContent()) {
			//			ImageInputStream imgInpStream =	new MemoryCacheImageInputStream (new ByteArrayInputStream(baseBody.getFileContent()));
			//			jpegFileArr = RigakuSaturn944.convertIMG2JPEG(imgInpStream, imageFileName);
					}					
				}
				imageTime = baseBody.getTime();								
			}			
			
			if (jpegFileArr != null) {				
				int count = 0;
				for(File jpegFile:jpegFileArr) {
					byte[] encodedBytes = null;					
					
					Parcel retParcel = ParcelUtil.newParcel(ParcelTypeEnum.PLUGIN);					
					BodyType bodyR = retParcel.addNewBody();
					XtalImageType bodyRet = (XtalImageType)bodyR.changeType(XtalImageType.type);
		            
				   if (jpegFile.length() < IRigakuConstants.maxEncodeSizeInKB * 1000)  {
		            	try {
		                    encodedBytes = Base64.encode(FileUtil.getBytesFromFile(jpegFile));
		                    bodyRet.setFileContent(encodedBytes);
		            	}
		            	catch (IOException ex) {
		            		ex.printStackTrace();
		            	}		            	
		            }
					bodyRet.setDirectory(jpegFile.getParent());  	
            		bodyRet.setFileLocation(FILE_URL_PROTOCOL + jpegFile.getPath());

		    		if (imageFileName == null) {
            			imageFileName = jpegFile.getName();	            
            		} else {
            			if (jpegFileArr.length > 1) {
            				imageFileName = imageFileName + (++count);
            			}
            			imageFileName += imageFileTypes.JPEG.getFileExtension(); 
            		}
            		bodyRet.setFileName(imageFileName);
		            	            
		            
		            if (imageTime == null) {
		            	imageTime = Calendar.getInstance();	            	
		            }
					bodyRet.setTime(imageTime);
					
					super.setNewParcel(retParcel);
				}
			}		
		}		
	}
	
	private File[] USydConversion(File fileLoc) {
		File[] retVal = null;
		
		BufferedInputStream bufIn;
		try {
			bufIn = new BufferedInputStream(new FileInputStream(fileLoc));
			ImageConverter imgConv = new ImageConverter(new ImageRigaku (bufIn));		
			try {
				retVal = new File[1];
				if (this.defaultFileName != null) {
					retVal[0] = new File(this.defaultFileName + ".jpg");
				} else {
					retVal[0] = File.createTempFile(fileLoc.getName(), imageFileTypes.JPEG.getName());
				}
				imgConv.writeJPGToFile(retVal[0]);
			} catch (IOException e) {			
				e.printStackTrace();
			}		
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();			
		} 
	
		return retVal;	
	}	
	
	/*
	 * Since this class creates the JPEG parcel and uses a temporary File storage, 
	 * some subclass of this should invoke the methods below to delete the files after usage. 
	 */
		
	protected static void deleteXtalJPEGFile(Vector<Parcel> pVec, boolean deleteOnJVMExit) throws MalformedURLException {
		
		if (pVec != null) {
			for(Parcel p : pVec) {
				deleteXtalJPEGFile(p, deleteOnJVMExit);
			}
		}		
	}

	protected static void deleteXtalJPEGFile(Parcel p, boolean deleteOnJVMExit) throws MalformedURLException {
		BodyType bodyR = p.getBody();				
		if (bodyR instanceof XtalImageType) {
			XtalImageType imageBody = (XtalImageType)bodyR.changeType(XtalImageType.type);
			if (imageBody.isSetFileLocation()) {
				URL fileURL = new URL(imageBody.getFileLocation());
				File file = new File(fileURL.getFile());
				if (deleteOnJVMExit) {
					file.deleteOnExit();
				} else {
					file.delete();						
				}
			}
		}
	}
	
}
