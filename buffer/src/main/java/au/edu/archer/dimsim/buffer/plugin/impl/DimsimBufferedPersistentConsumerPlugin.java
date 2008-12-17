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

package au.edu.archer.dimsim.buffer.plugin.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EndpointTypeEnum;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.util.SubscribeInfo;

import au.edu.archer.dimsim.buffer.util.DimsimUtils;

/**
 * @author mrafi
 *
 * The purpose of this class is to persist parameters required for 
 * smooth restart of consumer plug-ins using serialization. 
 */
public class DimsimBufferedPersistentConsumerPlugin extends
		DimsimBufferedConsumerPlugin {

	private String serializePath;	//overrides SerializePath set in DimsimPersistenceMonitor
	protected DimsimPersistenceMonitor persistMonitor;
	private static Logger log = Logger.getLogger(DimsimBufferedPersistentConsumerPlugin.class);
	
	protected DimsimBufferedPersistentConsumerPlugin(String id) throws Exception {
		super(id);		
	}
	
	public void setPersistMonitor(DimsimPersistenceMonitor persistMonitor) {
		if (this.persistMonitor != null) {
			this.persistMonitor.stop(this);
		}
		this.persistMonitor = persistMonitor;	
		log.debug("set PersistMonitor, path is " + persistMonitor.getSerializePath());
		setupPersistSessions();
	}
	
	public void setSerializePath(String serializePath) {
		this.serializePath = serializePath;		
		if (serializePath != null) {
			setupPersistSessions();		
		}	
	}

	protected void buffer(Parcel incomingParcel) {
		super.buffer(incomingParcel);
		persistLocalBufferImmediate();
	}
	
	synchronized protected ArrayList<Parcel> resetLocalBuffer() {
		ArrayList<Parcel> retList = super.resetLocalBuffer();
		persistLocalBufferImmediate();
		return retList;
	}
	
	synchronized protected void persistLocalBufferImmediate() {
		if (persistMonitor != null) {
				String fileName = getSerializedPath(DimsimPersistenceMonitor.LocalBufferPersistPath);
				File persistFile = new File(fileName);
				if (persistFile.canWrite() 
						|| ((!persistFile.exists()) && persistFile.getParentFile().canWrite())) {
					DimsimPersistenceMonitor.serialize(persistFile, getConsumerBufferElements());
				} else {
					log.debug("Persist Buffer failed, no write permission for file: " + persistFile.getPath());
				}
		}
	}

	@SuppressWarnings("unused")
	synchronized protected void persistLocalBuffer() {
		if ((persistMonitor != null)		 
		 && (!isLocalBufferEmpty())) {
			String fileName = getSerializedPath(DimsimPersistenceMonitor.LocalBufferPersistPath);
			File persistFile = new File(fileName);
			if (persistFile.canWrite()) {				
				persistMonitor.add(this, persistFile, 
						new DimsimPersistenceMonitor.ISerializableMethod () {
							public Serializable getSerializableObject() {
								try {
									return getConsumerBufferElements();
								} catch (Exception e) {
									e.printStackTrace();
								}
								return null;
							}
					});
				}				
		}		
	}

	
	private void setupPersistSessions() {
		if (persistMonitor != null) {
			String fileName = getSerializedPath(DimsimPersistenceMonitor.SessionPersistPath);
			File persistFile = new File(fileName);
			if (persistFile.canWrite() 
				|| ((!persistFile.exists()) && persistFile.getParentFile().canWrite())) {							
				persistMonitor.add(this, persistFile, 
					new DimsimPersistenceMonitor.ISerializableMethod () {
						public Serializable getSerializableObject() {
							try {
								return getSerializableSessions();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return null;
						}
				});
			} else {
				log.debug("Persist Session setup failed, no write permission for file: " + persistFile.getPath());
			}
		} else {
			log.debug("Persist Session setup failed, persistMonitor is null");
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	public void start()	throws PluginException {
		//Read Subscription Info from Persistent Store Location to restart
		loadSessionInfo();				
		super.start();  //loadSessionInfo() must precede super.start();
		
		loadLocalBuffer();
		
		if (this.persistMonitor == null) {
			this.setPersistMonitor(new DimsimPersistenceMonitor());			
		}
		
		
	}

	public void stop() {
		if (this.persistMonitor != null) {
			this.persistMonitor.stop(this);
			this.persistMonitor = null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadLocalBuffer() {
		File bufferFile = new File(
				getSerializedPath(DimsimPersistenceMonitor.LocalBufferPersistPath));
		if (bufferFile.exists()) {								
			try  {
			     FileInputStream fis = new FileInputStream(bufferFile);
			     ObjectInputStream in = new ObjectInputStream(fis);
			     ArrayList<Parcel> parcelList = (ArrayList<Parcel>)in.readObject();
			     in.close();
			     if (parcelList != null) {					
						for(Parcel p:parcelList) {
							this.buffer(p);
						}
			     }
			} catch(IOException ex)   {
			     ex.printStackTrace();
			} catch(ClassNotFoundException ex) {
			   ex.printStackTrace();
			}
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void loadSessionInfo() {
		File sessionFile = new File(
					getSerializedPath(DimsimPersistenceMonitor.SessionPersistPath));
		if (sessionFile.exists()) {								
			try  {
			     FileInputStream fis = new FileInputStream(sessionFile);
			     ObjectInputStream in = new ObjectInputStream(fis);
			     Vector<HashMap<String,String>> sessionList
			     			= (Vector<HashMap<String,String>>)in.readObject();
			     in.close();
			     List<SubscribeInfo> sInfoList = new ArrayList<SubscribeInfo> ();			     
			     if (sessionList != null) {
			    	 for(Map<String,String> m : sessionList) {
			    		String sessionId = m.get("SessionID");
						String pluginID = m.get("PluginID");
						String remotePlugin = m.get("RemotePlugin");
						String remoteEndPoint = m.get("RemoteEndPoint");
						String remoteEndPointType = m.get("RemoteEndPointType");
						EndpointType endpoint = EndpointType.Factory.newInstance();
						endpoint.setUrl(remoteEndPoint);
						EndpointTypeEnum.Enum eType = EndpointTypeEnum.Enum.forString(remoteEndPointType);						
						endpoint.setType(eType);
						sInfoList.add(new SubscribeInfo(endpoint,remotePlugin));						
			    	 }
			    	 this.setSubscriptions(sInfoList);			    	 
			     }
			} catch(IOException ex)   {
			     ex.printStackTrace();
			} catch(ClassNotFoundException ex) {
			   ex.printStackTrace();
			}
		}
	}
	
	protected String getSerializedPath(String localPath) {
		String retStr = this.getId() + "." + localPath;		
		String dirPath = null;
		
		if (serializePath != null) {
			dirPath = serializePath;
		} else if (persistMonitor != null) {
			dirPath = persistMonitor.getSerializePath();
		}
	
		if (dirPath != null) {
			retStr = dirPath + File.separator + localPath;
		}  	
		return retStr;
	}	
	
	private final Vector<HashMap<String,String>> getSerializableSessions() {
		Vector<HashMap<String,String>> retMap = new Vector<HashMap<String,String>> ();
		
		try {
			List<ISession> sessions = DimsimUtils.getSessionManager().getSessions(this);
			if (sessions != null) {
				for(ISession s: sessions) {
					HashMap<String,String> sessionMap = new HashMap<String,String> ();
					retMap.add(sessionMap);
					sessionMap.put("SessionID", s.getId());
					sessionMap.put("PluginID", this.getId());
					sessionMap.put("RemotePlugin", s.getSubscriber().getId());
					EndpointType endpoint = (EndpointType) s.get("endpoint");
					if (endpoint != null) {
						sessionMap.put("RemoteEndPoint", endpoint.getUrl());
						sessionMap.put("RemoteEndPointType", endpoint.getType().toString());					
					}					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return retMap;
	}

}
