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
package au.edu.archer.dimsim.plugins.srb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.DeviceDataType;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.ResponseType;
import org.instrumentmiddleware.cima.parcel.VariableType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.session.ISession;

import au.edu.archer.cima.xtal.BeginXtalExperimentType;
import au.edu.archer.cima.xtal.EndXtalExperimentType;
import au.edu.archer.cima.xtal.XtalImageType;
import au.edu.archer.cima.xtal.impl.BeginXtalExperimentTypeImpl;
import au.edu.archer.dimsim.buffer.plugin.impl.DimsimBufferedPersistentConsumerPlugin;
import au.edu.archer.dimsim.buffer.plugin.impl.DimsimPersistenceMonitor;
import au.edu.jcu.hpc.srb.SRBExtendedFile;
import au.edu.jcu.hpc.srb.beans.MetaDataEntry;
import edu.sdsc.grid.io.local.LocalFile;
import edu.sdsc.grid.io.srb.SRBException;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBRandomAccessFile;


/**
 * @author 
 *  Andrew Sharpe	original version	Feb 2008
 *  Rafi M Feroze	Dimsim Port 		May 2008 
 *					Add LocalBuffer
 */
public class CrystalSRBConsumer
        extends DimsimBufferedPersistentConsumerPlugin {
	private static final String LastImagePersistPath = "lastimageprocesstime";

	private static Logger log = Logger.getLogger(CrystalSRBConsumer.class);

	// TODO: make this not protected - ie, create a newExperiment function
	protected SRBFile experimentRoot, errorRoot;
	private SRBHelper srbHelperValidRecords;
	private SRBHelper srbHelperErrorRecords;
	private SRBConfig errorConfig;
	private boolean beginExperiementParcelQuerySent = false;	
	private long lastStoredImageTimeStamp = 0L;


	public CrystalSRBConsumer(String id, SRBConfig config)
	        throws Exception {
		super(id);
		srbHelperValidRecords = new SRBHelper();
		srbHelperValidRecords.setSRBConfig(config);		
		setErrorConfig(config);  //defaults to current valid config initially		
	}	
	
	public void setErrorConfig(SRBConfig config) throws Exception {
		this.errorConfig = config;
		if (this.errorConfig == null) {
			srbHelperErrorRecords = new SRBHelper();		
			srbHelperErrorRecords.setSRBConfig(this.errorConfig);			
		} else {
			srbHelperErrorRecords = srbHelperValidRecords;
		}
		errorRoot = srbHelperErrorRecords.getSRBFile(srbHelperErrorRecords.getSRBHome() + "/" +"ErrorRecords");
	}
	
	public synchronized ResponseType processParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.info("received body of type: " + body.getClass().getName());
		//log.debug("parcel: " + body);

		// TODO: use spring to load all the SRB parcel handlers
		if (body instanceof BeginXtalExperimentType) {
			return processBeginXtalExperiment((BeginXtalExperimentType) body.changeType(
			        BeginXtalExperimentType.type), creationTime, session,
			    response);
		}
		else if (body instanceof EndXtalExperimentType) {
			return processEndXtalExperiment((EndXtalExperimentType) body.changeType(
			        EndXtalExperimentType.type), creationTime, session, response);
		}
		else if (body instanceof XtalImageType) {
			return processXtalImage((XtalImageType) body.changeType(
			        XtalImageType.type), creationTime, session, response);
		}
		else if (body instanceof DeviceDataType) {
			return processDeviceData((DeviceDataType) body.changeType(
			        DeviceDataType.type), creationTime, session, response);
		} 

		log.info("unknown parcel type " + body.getClass().getName() + " - ignored");

		return response;
	}


	protected ResponseType processBeginXtalExperiment(
	    BeginXtalExperimentType data, Calendar creationTime, ISession session,
	    ResponseType response)
	        throws PluginException {
		String lab = data.getProjectName();
		String instrument = "bruker";
		String sampleName = data.getSampleName();
		String timeStamp = creationTime.toString();

		try {
			SRBFile newExperimentRoot = srbHelperValidRecords.getSRBFile(srbHelperValidRecords.getSRBHome() + "/" + lab + "/" + instrument +
			    "/" + sampleName + "_" + timeStamp);
	
			log.debug("experiment root calculated is " + experimentRoot);
		
			if ((experimentRoot == null) ||
				(experimentRoot.compareTo(newExperimentRoot) != 0)){
				
				experimentRoot = newExperimentRoot;				
				
				ArrayList<Parcel> localBufferedParcels = this.resetLocalBuffer();				
				 
				String sourcePluginID = session.getSubscriber().getId();
				long exprStartTime = creationTime.getTimeInMillis();

				//Request parcels between lastImageProcessed and earliest of BufferParcelTimeStamp
				requestMissedParcels(exprStartTime, localBufferedParcels,sourcePluginID); 
				
				
				//Problematic if more than one Experiment Parcel is received.
				//Every one of the buffered Parcel will go with the first experiment.				
				processBufferedParcels(exprStartTime, localBufferedParcels);
			}
	
			log.info("experiment root set to " + experimentRoot);
			
		} catch (IOException e) {
			throw new PluginException(
			    "error creating experiment directory: " + e);
		}		

		return response;
	}

	private void requestMissedParcels(long experimentStartTime, 
									  ArrayList<Parcel> parcelList
									  ,String sourcePluginID) {
		
		if ((parcelList != null) &&
			(parcelList.size() > 0)) {
			long firstAvailableParcelTS = parcelList.get(0).getCreationTime().getTimeInMillis();
			if (firstAvailableParcelTS > this.lastStoredImageTimeStamp) {
				//send a request to get Parcels missed, adjust time interval to exclude processed parcels
				long startTimeInMillis = Math.max(experimentStartTime, lastStoredImageTimeStamp ) + 1;
				long endTimeInMillis = firstAvailableParcelTS - 1;
				if (startTimeInMillis <= endTimeInMillis) { 
					CommandOperationResponseType getResponse;
					try {
						getResponse = this.getRemoteBufferParcels(sourcePluginID, startTimeInMillis, endTimeInMillis);
						if (!getResponse.getStatus().equals(ResponseStatusEnum.SUCCESS)) {
							String message = "Error requesting missing parcels for time " 
											+ startTimeInMillis + " to " + endTimeInMillis ;
							log.error(message + " : " + getResponse.getMessage());							
						}
					} catch (Exception e) {
						e.printStackTrace();
						String message = "Error obtaining missed Parcels from BufferPlugin: " 
									+	e.getMessage();
						log.error(message);						
					}
				}
			}
		}
	}
	/*
	 * 
	 */
	private void processBufferedParcels(long exprStartTime, ArrayList<Parcel> parcelList) {
		log.debug("Processing buffered Parcels " );
		
		if (parcelList != null) {			
			byte[] fileContent = null;
			for(Parcel p : parcelList) {
				BodyType body = p.getBody();
				if (body instanceof XtalImageType) {
					XtalImageType image = (XtalImageType) body.changeType(XtalImageType.type);
					Calendar imageParcelCreateTime = p.getCreationTime();

					try {
						fileContent = getFileContent(image);
					} catch (PluginException e) {
						e.printStackTrace();
					}
					boolean isErrorRecord = false;
					if (imageParcelCreateTime.getTimeInMillis() < exprStartTime) {
						isErrorRecord = true;
					}
					try {
						storeImageinSRB(image, imageParcelCreateTime, fileContent,isErrorRecord);
					} catch (PluginException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}


	protected ResponseType processEndXtalExperiment(
	    EndXtalExperimentType data, Calendar creationTime, ISession session,
	    ResponseType response)
	        throws PluginException {
		experimentRoot = null;

		return response;
	}


	protected ResponseType processXtalImage(XtalImageType image,
	    Calendar timestamp, ISession session, ResponseType response)
	        throws PluginException {
		log.trace("processing crystallography image");

		byte[] fileContent = null;

		if (!image.isSetFileContent() && !image.isSetFileLocation()) {
			throw new PluginException(
			    "neither fileContent nor fileLocation were set");
		}

		fileContent = getFileContent(image);
		
		if (experimentRoot == null) {
			String message = "Experiment start parcel not recieved yet, buffering parcel locally";
			log.debug(message);
			//throw new PluginException(message);
			XtalImageType bufferImage = XtalImageType.Factory.newInstance();
			bufferImage.setFileLocation(image.getFileLocation());
			bufferImage.setFileContent(fileContent);
			bufferImage.setFileName(image.getFileName());
			bufferImage.setTime(image.getTime());
			bufferImage.setDirectory(image.getDirectory());						
			buffer(generateParcelForLocalBuffer(bufferImage,timestamp,ParcelTypeEnum.PLUGIN,session));
			if (!this.beginExperiementParcelQuerySent) {
				String sourcePluginID = session.getSubscriber().getId();
				CommandOperationResponseType getResponse;
				try {
					getResponse = getLastRemoteBufferParcel(sourcePluginID,BeginXtalExperimentTypeImpl.class);
				} catch (Exception e) {
					e.printStackTrace();
					message = "Error obtaining start Experiment Parcel from BufferPlugin: " 
								+	e.getMessage();
					log.error(message);
					throw new PluginException(message);
				}				
				if ((!getResponse.getStatus().equals(ResponseStatusEnum.SUCCESS)) 
					| (getResponse.getResult().equals("0"))) {
					message = "Image Parcel has no corresponding Experiment start parcel";
					log.error(message);
					throw new PluginException(message);
				}
				this.beginExperiementParcelQuerySent = true;
				response.setMessage("Image Parcel out of Sync, parcel buffered locally. Update to SRB on hold until Experiment Start parcel is recieved.");
			}
		} else {
			//##issue : should an error be raised if the received parcel time stamp 
			//        is less than experiment start time stamp.
			storeImageinSRB(image, timestamp, fileContent,false);
		}

		return response;
	}

	/**
	 * @param image
	 * @param fileContent
	 * @return
	 * @throws PluginException
	 */
	private byte[] getFileContent(XtalImageType image)
			throws PluginException {
		byte[] fileContent = null;
		
	/*	if (image.isSetFileContent()) {			
			log.debug("got inline image data");
			fileContent = image.getFileContent();

			log.trace("fileContent length: " + fileContent.length);

			if (fileContent.length == 0) {
				log.warn("0 length image data");
			}
		} else {
	*/		try {
				URL fileLocation = new URL(image.getFileLocation());
				log.debug("retrieving image from remote location = " + fileLocation);
				URLConnection connection = fileLocation.openConnection();
				InputStream input = fileLocation.openStream();
	
				int fileSize = connection.getContentLength();
				int read = 0;
	
				fileContent = new byte[fileSize];
	
				while (read < fileSize) {
					read += input.read(fileContent, read, fileSize - read);
				}
			}
			catch (MalformedURLException e) {
				throw new PluginException(e.getMessage());
			}
			catch (IOException e) {
				throw new PluginException(e.getMessage());
			}			
	//	}
		return fileContent;
	}

	private void storeImageinSRB(XtalImageType image, Calendar timestamp,
			byte[] fileContent, boolean isErrorRecord) throws PluginException {
		SRBFile srbFile = null;
		String filename = null;
		SRBHelper currSrbHelper = null;
		
		if (!isErrorRecord) {
			filename = experimentRoot.getAbsolutePath();
			currSrbHelper = this.srbHelperValidRecords;
		} else {
			filename = errorRoot.getAbsolutePath();
			currSrbHelper = this.srbHelperErrorRecords;
		}
		filename = filename + SRBFile.separator + image.getFileName();
		
		try {
			srbFile = currSrbHelper.getSRBFile(filename);

			if (srbFile.exists()) {
				String message = srbFile.getAbsolutePath() + " already exists";
				log.error(message);
				throw new PluginException(message);
			}
			else {
				if (!srbFile.createNewFile()) {
					String message = "couldn't create image file: " +
						srbFile.getAbsolutePath();
					log.error(message);
					throw new PluginException(message);
				}
			}
		}
		catch (IOException e) {
			String message = "error obtaining SRBFile: " + filename;
			log.error(message);
			throw new PluginException(message);
		}

		// TODO: make this a configuration option
		String path = System.getProperty("java.io.tmpdir") + File.separator +
			image.getFileName();
		File file = new File(path);
		log.trace("temporary file path: " + file);

		try {
			FileOutputStream out = new FileOutputStream(file);			
			out.write(fileContent);
			//			out.write(Base64.decode(fileContent));
		}
		catch (FileNotFoundException e) {
			String message = "Failed.  File not found: " + e;
			log.error(message);
			throw new PluginException(message);
		}
		catch (IOException e) {
			String message = "Failed.  IO error decoding parcel: " + e;
			log.error(message);
			throw new PluginException(message);
		}

		LocalFile local = new LocalFile(file);
		log.debug("local file: " + local);

		try {
			srbFile.copyFrom(local);
		}
		catch (IOException e) {
			String message = "Failed.  IO error creating remote file: " + e;
			log.error(message);
			throw new PluginException(message);
		}

		log.trace("image stored at " + srbFile);
		file.delete();

		// add metadata
		SRBExtendedFile srbxf = new SRBExtendedFile(srbFile);

		try {
			srbxf.addUserMetaData(new MetaDataEntry("FileName",
			        image.getFileName()));
			srbxf.addUserMetaData(new MetaDataEntry("FileLocation", "unknown"));
			srbxf.addUserMetaData(new MetaDataEntry("FileSubDirectory",
			        image.getDirectory()));
			srbxf.addUserMetaData(new MetaDataEntry("FileCategory", "unknown"));
			// TODO: externalise			
			srbxf.addUserMetaData(new MetaDataEntry("FileTimeStamp",
			        image.getTime().toString()));
			srbxf.addUserMetaData(new MetaDataEntry("FileSentTimeStamp",
			        timestamp.toString()));
			if (image.getFileName().endsWith("osc")){
				srbxf.addUserMetaData(new MetaDataEntry("FileType", "OSC"));
			}
			else  if (image.getFileName().endsWith("img")) {
				srbxf.addUserMetaData(new MetaDataEntry("FileType", "IMG"));
			} else  if (image.getFileName().endsWith("jpg")) {
				srbxf.addUserMetaData(new MetaDataEntry("FileType", "image/jpeg"));
			} else {
				srbxf.addUserMetaData(new MetaDataEntry("FileType", "UNKNOWN"));
			}			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.debug("metadata added to " + srbFile);
		long imageTime = image.getTime().getTimeInMillis();
		
		if (this.lastStoredImageTimeStamp < imageTime) {
			this.lastStoredImageTimeStamp = imageTime;
			persistTimeStampImmediate();
		}

	}

	synchronized private void persistTimeStampImmediate() {
		if (persistMonitor != null) {
			String fileName = getSerializedPath(LastImagePersistPath);
			File persistFile = new File(fileName);			
			if (persistFile.canWrite() 
					|| ((!persistFile.exists()) && persistFile.getParentFile().canWrite())) {
				DimsimPersistenceMonitor.serialize(persistFile, new Long(this.lastStoredImageTimeStamp));
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public void start()	throws PluginException {
		//Read Last Image Processed Timestamp				
		super.start();
		loadLastIamgeTimeStamp();
	}
	
	
	private void loadLastIamgeTimeStamp() {
		File imageTSFile = new File(
				getSerializedPath(LastImagePersistPath));
		if (imageTSFile.exists()) {								
			try  {
			     FileInputStream fis = new FileInputStream(imageTSFile);
			     ObjectInputStream in = new ObjectInputStream(fis);
			     Long imageTSLong = (Long)in.readObject();
			     in.close();
			     if (imageTSLong != null) {					
						this.lastStoredImageTimeStamp = imageTSLong.longValue();
			     }
			} catch(IOException ex)   {
			     ex.printStackTrace();
			} catch(ClassNotFoundException ex) {
			   ex.printStackTrace();
			}
		}		
	}

	protected ResponseType processDeviceData(DeviceDataType data,
	    Calendar timestamp, ISession session, ResponseType response)
	        throws PluginException {
		log.trace("processing device data");

		VariableType[] variables = data.getDeviceVariableArray();

		if (experimentRoot == null) {
			// TODO: queue the parcel for a while waiting for a new experiment
			String message = "no experiment started yet";
			log.error(message);

			throw new PluginException(message);
		}

		for (int i = 0; i < variables.length; i++) {
			String name = variables[i].getName();
			String value = variables[i].getValue();

			log.debug("variable name: " + name);
			log.debug("variable value: " + value);

			if ((name == null) || (value == null) || name.equals("") ||
				    value.equals("")) {
				log.error("invalid name or value supplied");

				continue;
			}

			SRBFile dataFile = null;
			String filename = experimentRoot.getAbsolutePath() +
				SRBFile.separator + name;

			try {
				dataFile = srbHelperValidRecords.getSRBFile(filename);

				if (dataFile.exists()) {
					log.trace(dataFile.getAbsolutePath() +
					    " already exists, appending");
				}
				else {
					log.info(dataFile.getAbsolutePath() +
					    " doesn't exist, creating");

					if (!dataFile.createNewFile()) {
						String message = "couldn't create file for sensor data: " +
							data;
						log.error(message);
						throw new PluginException(message);
					}
				}
			}
			catch (IOException e) {
				log.error("error obtaining SRBFile: " + filename);

				continue;
			}

			SRBRandomAccessFile sraf = null;

			try {
				sraf = srbHelperValidRecords.getSRBRandomAccessFile(dataFile, "rw");
				log.trace("opened " + sraf);
			}
			catch (IllegalArgumentException e) {
				log.debug(e, e);
				throw new PluginException(e.getMessage());
			}
			catch (SecurityException e) {
				log.debug(e, e);
				throw new PluginException(e.getMessage());
			}
			catch (IOException e) {
				log.debug(e, e);
				throw new PluginException(e.getMessage());
			}

			try {
				String output = timestamp + ";" + value + "\n";

				synchronized (data) {
					log.trace("seeking to " + dataFile.length());
					sraf.seek(dataFile.length());
					log.trace("writing bytes: " + output);
					sraf.write(output.getBytes());
				}
			}
			catch (SRBException e) {
				log.debug(e, e);
				throw new PluginException("Writing data failed: " +
				    e.getMessage());
			}
			catch (IOException e) {
				log.debug(e, e);
				throw new PluginException("Writing data failed: " +
				    e.getMessage());
			}
			finally {
				try {
					log.trace("closing " + sraf);
					sraf.close();
				}
				catch (IOException e) {
					log.error(
					    "error closing SRB file - nothing we can do about that :(" +
					    e);
				}
			}

			log.trace("data added to " + dataFile);
		}

		return response;
	}


	public ResponseType processSubscribeParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a subscribe: " + body);

		return response;
	}


	public ResponseType processUnsubscribeParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a unsubscribe: " + body);

		return response;
	}


	public ResponseType processStartmoduleParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a start: " + body);

		return response;
	}


	public ResponseType processStopmoduleParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a stop: " + body);

		return response;
	}


	public ResponseType processDescribeParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a describe: " + body);

		return response;
	}


	public ResponseType processRegisterParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a register: " + body);

		return response;
	}


	public ResponseType processUnregisterParcel(BodyType body,
	    Calendar creationTime, ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a unregister: " + body);

		return response;
	}


	public ResponseType processPingParcel(BodyType body, Calendar creationTime,
	    ISession session, ResponseType response)
	        throws PluginException {
		log.debug("received a ping: " + body);

		return response;
	}

	/*
	 * // this is oh so very dirty, and is only here to test the unsubscribe process protected void
	 * processSubscribeResponse(Parcel response) { ResponseType[] responses = ParcelUtil.getResponse(response)
	 * .getResponseArray(); String sessionId = null;
	 *
	 * for (int i = 0; i < responses.length; i++) { SubscriptionResponseType srt = (SubscriptionResponseType) responses
	 * [i].changeType(SubscriptionResponseType.type);
	 *
	 * log.info("received status: " + srt.getStatus()); if (srt.getStatus() == ResponseStatusEnum.SUCCESS) sessionId =
	 * srt.getNewSessionId(); }
	 *  // unsubscribe if (sessionId != null) { Parcel parcel = ParcelUtil.newParcel("unsubscribe");
	 * parcel.setSequenceId(2);
	 *
	 * Sessions ss = parcel.addNewSessions(); SessionType s = ss.addNewSession(); s.setSessionId(sessionId);
	 *
	 * try { ICIMADirector director = CIMAUtil.getDirector(); Parcel res = director.handleOutgoingParcel(this, parcel,
	 * subscribeInfo.endpoint); } catch (Exception e) { log.error("error subscribing: " + e); e.printStackTrace(); } } }
	 */

	/*
	 * protected String processParcel(Parcel parcel) throws IOException { log.debug("internalProcessParcel");
	 *
	 * if (!started) { log.error("plugin not started");
	 *
	 * return "FAILED. Plugin not started."; }
	 *
	 * log.trace("parcel: " + parcel);
	 *
	 * if (parcel.getType().equals(Parcel.CIMA_NEWRUN_DATA)) { return processNewRunParcel(parcel); } else { if
	 * (experimentRoot == null) { // TODO: queue the parcel for a while waiting for a new experiment log.error("received
	 * binary ready parcel with no experiment");
	 *
	 * return "FAILED. No experiment started."; }
	 *
	 * if (parcel.getType().equals(Parcel.CIMA_BINARY_READY)) { return processBinaryDataReadyParcel(parcel); } else if
	 * (parcel.getType().equals(Parcel.CIMA_BINARY_DATA)) { return processBinaryDataParcel(parcel); } else if
	 * (parcel.getType().equals(Parcel.CIMA_DOUBLE_DATA)) { return processDoubleDataParcel(parcel); } }
	 *
	 * log.debug("don't know how to deal with parcel of type " + parcel.getType());
	 *
	 * return null; }
	 *
	 *
	 * private synchronized String processNewRunParcel(Parcel parcel) throws IOException {
	 * log.debug("processNewRunParcel");
	 *
	 * String lab = parcel.getTextValue(CIMAConstants.PARCEL_BODY_XPATH + "/Lab").replaceAll("[/ ]", ""); String
	 * instrument = parcel.getTextValue(CIMAConstants.PARCEL_BODY_XPATH + "/Instrument").replaceAll("[/ ]", ""); String
	 * sample = parcel.getTextValue(CIMAConstants.PARCEL_BODY_XPATH + "/SampleNo").replaceAll("[/ ]", ""); String
	 * timestamp = parcel.getTextValue(CIMAConstants.PARCEL_BODY_XPATH + "/TimeStamp").replaceAll("[/ ]", "");
	 *
	 * if (experimentRoot != null) { log.warn("starting a new experiment (old experiment was at " +
	 * experimentRoot.getAbsolutePath() + ")"); }
	 *
	 * experimentRoot = srbHelper.getSRBFile(lab + "/" + instrument + SRBFile.separator + sample + "_" + timestamp);
	 *
	 * SRBFile binaryDir = srbHelper.getSRBFile(experimentRoot.getAbsolutePath() + SRBFile.separator + "BinaryData");
	 *
	 * log.debug("new experiment root: " + experimentRoot);
	 *
	 * if (experimentRoot.exists()) { return "Failed. Directory for experiment already exists"; }
	 *
	 * if (!binaryDir.mkdirs()) { return "Failed. Error creating directory(s) for experiment"; }
	 *
	 * return "OK"; }
	 *
	 *
	 * private synchronized String processBinaryDataReadyParcel(Parcel parcel) {
	 * log.debug("processBinaryDataReadyParcel");
	 *
	 * ChannelSource source = getSourceChannel( "http://localhost:8001/ChannelSource");
	 *
	 * Parcel getParcel = Parcel.buildGetParcel(parcel.getTextValue( CIMAConstants.PARCEL_SOURCE_XPATH),
	 * parcel.getTextValue(CIMAConstants.PARCEL_CCDFILELOCATION_XPATH));
	 *
	 * try { log.trace("sending parcel: " + parcel);
	 *
	 * String ret = source.receiveParcel(localEndpoint, getParcel.toString()); log.trace("received parcel: " + ret); }
	 * catch (Exception e) { e.printStackTrace();
	 *
	 * return "FAILED"; }
	 *
	 * return "OK"; }
	 *
	 *
	 * private synchronized String processBinaryDataParcel(Parcel parcel) throws IOException {
	 * log.debug("processBinaryDataParcel");
	 *
	 * SRBFile data = srbHelper.getSRBFile(experimentRoot.getAbsolutePath() + SRBFile.separator + "BinaryData" +
	 * SRBFile.separator + parcel.getTextValue(CIMAConstants.PARCEL_CCDFILENAME_XPATH));
	 *
	 * if (data.exists()) { log.error(data.getAbsolutePath() + " already exists. Not overwriting.");
	 *
	 * return "Failed. " + data.getAbsolutePath() + " already exists"; }
	 *  // TODO: make this a configuration option String path = System.getProperty("java.io.tmpdir") + File.separator +
	 * data.getName(); File file = new File(path); log.debug("temporary file path: " + file);
	 *
	 * try { Base64.decode(parcel.getTextValue( CIMAConstants.PARCEL_CCDENCODED_XPATH), new FileOutputStream(file)); }
	 * catch (FileNotFoundException e) { e.printStackTrace();
	 *
	 * return "Failed. File not found: " + e; } catch (IOException e) { e.printStackTrace();
	 *
	 * return "Failed. IO error decoding parcel: " + e; }
	 *
	 * LocalFile local = new LocalFile(file); log.debug("local file: " + local);
	 *
	 * try { data.copyFrom(local); } catch (IOException e) { e.printStackTrace();
	 *
	 * return "Failed. IO error creating remote file: " + e; }
	 *
	 * log.debug(local + " added to SRB as " + data);
	 *  // add metadata SRBExtendedFile srbxf = new SRBExtendedFile(data); String fileInfoXPath =
	 * CIMAConstants.PARCEL_BODY_XPATH + "/FileInfo";
	 *
	 * try { srbxf.addUserMetaData(new MetaDataEntry("FileName",
	 * parcel.getTextValue(CIMAConstants.PARCEL_CCDFILENAME_XPATH))); srbxf.addUserMetaData(new
	 * MetaDataEntry("FileLocation", parcel.getTextValue( CIMAConstants.PARCEL_CCDFILELOCATION_XPATH)));
	 * srbxf.addUserMetaData(new MetaDataEntry("FileSubDirectory", parcel.getTextValue(fileInfoXPath +
	 * "/FileSubDirectory"))); srbxf.addUserMetaData(new MetaDataEntry("FileCategory", parcel.getTextValue(fileInfoXPath +
	 * "/FileCategory"))); srbxf.addUserMetaData(new MetaDataEntry("FileType", parcel.getTextValue(fileInfoXPath +
	 * "/FileType"))); srbxf.addUserMetaData(new MetaDataEntry("FileTimeStamp", parcel.getTextValue(fileInfoXPath +
	 * "/FileTimeStamp"))); srbxf.addUserMetaData(new MetaDataEntry("FileSentTimeStamp",
	 * parcel.getTextValue(fileInfoXPath + "/FileSentTimeStamp"))); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); }
	 *
	 * log.debug("metadata added to " + data);
	 *
	 * return "OK"; }
	 */

	/*
	 * private synchronized String processDoubleDataParcel(Parcel parcel) throws IOException {
	 * log.debug("processDoubleDataParcel");
	 *
	 * String sensorName = parcel.getTextValue(CIMAConstants.PARCEL_BODY_XPATH + "/SensorName"); String doubleData =
	 * parcel.getTextValue(CIMAConstants.PARCEL_BODY_XPATH + "/DoubleData");
	 *
	 * SRBFile data = srbHelper.getSRBFile(experimentRoot.getAbsolutePath() + SRBFile.separator + sensorName);
	 *
	 * if (data.exists()) { log.debug(data.getAbsolutePath() + " already exists, appending"); } else {
	 * log.debug(data.getAbsolutePath() + " doesn't exist, creating");
	 *
	 * if (!data.createNewFile()) { log.error("couldn't create file for sensor data: " + parcel);
	 *
	 * return "Failed. Couldn't create file"; } }
	 *
	 * SRBRandomAccessFile sraf = null;
	 *
	 * try { sraf = srbHelper.getSRBRandomAccessFile(data, "rw"); log.trace("opened " + sraf); } catch
	 * (IllegalArgumentException e) { log.debug(e, e); throw new IOException(e.getMessage()); } catch (SecurityException
	 * e) { log.debug(e, e); throw new IOException(e.getMessage()); } catch (IOException e) { log.debug(e, e); throw new
	 * IOException(e.getMessage()); }
	 *
	 * try { String output = parcel.getTextValue(CIMAConstants.PARCEL_BODY_XPATH + "/TimeStamp") + ";" + doubleData +
	 * "\n";
	 *
	 * synchronized (data) { log.trace("seeking to " + data.length()); sraf.seek(data.length()); log.trace("writing
	 * bytes: " + output); sraf.write(output.getBytes()); log.trace("closing " + sraf); sraf.close(); } } catch
	 * (SRBException e) { log.debug(e, e);
	 *
	 * return "Failed. " + e.getMessage(); } catch (IOException e) { log.debug(e, e);
	 *
	 * return "Failed. " + e.getMessage(); }
	 *
	 * log.debug("data added to " + data);
	 *
	 * return "OK"; }
	 */

	/*
	 * public void setSRBHelper(SRBHelper helper) { srbHelper = helper; }
	 */

	/**
	 * This method is here to allow the injection of an endpoint to facilitate testing
	 *
	 * @param endpoint
	 */

	/*
	 * public void setLocalEndpoint(String endpoint) { this.localEndpoint = endpoint; }
	 */
}
