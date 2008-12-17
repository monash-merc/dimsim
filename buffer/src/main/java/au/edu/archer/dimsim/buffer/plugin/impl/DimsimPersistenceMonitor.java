/**
 * 
 */
package au.edu.archer.dimsim.buffer.plugin.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.plugin.IPlugin;

/**
 * @author mrafi
 *
 * Provides persistence methods. Persistence can be periodic or immediate.
 * For periodic persistence, use 'add' methods. 
 * For immediate persistence, use the static serialize(File,Serializable) method.
 *  
 */
public class DimsimPersistenceMonitor implements Runnable {

	public interface ISerializableMethod {
		public Serializable getSerializableObject();}
	
	Map<File,Serializable> persistMap = 
				Collections.synchronizedMap(new HashMap<File,Serializable> ());
	Map<File,ISerializableMethod> persistMethodMap = 
				Collections.synchronizedMap(new HashMap<File,ISerializableMethod> ());
	Map<Object,Set<File>> objectList = 
				Collections.synchronizedMap(new HashMap<Object,Set<File>> ());
	
	IPlugin plugin;
	boolean stopped = false;
	long sleepTime = 30 * 1000; //defaultSleepTime;	
	private String serializePath;
	
	public static final long defaultSleepTime = 5 * 1000 * 60;  //Five Minutes default;
	public static final String SessionPersistPath = "sessionInfo.dimsim";
	public static final String LocalBufferPersistPath = "localbuffer.dimsim";
	
	private static Logger log = Logger.getLogger(DimsimPersistenceMonitor.class);
	
	public DimsimPersistenceMonitor() {
		super();
		(new Thread(this)).start();	//Start Monitoring.	
	}
	
	public void add(Object requestingObject, File persistFile, Serializable objectReference) {
		if (persistFile != null) {
			persistMap.put(persistFile,objectReference);
			updateObjectList(requestingObject, persistFile);
		}
	}

	/*
	 * Periodic persistence of dynamic objects is not possible using object Reference.	  
	 * To identify dynamic objects, use 'add' method with ISerializableMethod interface. 
	 */
	public void add(Object requestingObject, File persistFile, ISerializableMethod methodReference) {
		if (persistFile != null) {
			persistMethodMap.put(persistFile,methodReference);
			updateObjectList(requestingObject, persistFile);
		}
	}
	
	/**
	 * @param requestingObject
	 * @param persistFile
	 */
	synchronized private void updateObjectList(Object requestingObject, File persistFile) {
		Set<File> fileset = objectList.get(requestingObject);
		if (fileset == null) {
			fileset = Collections.synchronizedSet(new HashSet<File> ());
			objectList.put(requestingObject, fileset);
		}
		fileset.add(persistFile);
	}

	
	public void setSerializePath(String serializePath) {
		this.serializePath = serializePath;
	}
	
	public String getSerializePath() {
		return serializePath;
	}
	
	public void setSleepTime(long timeInSeconds) {
		this.sleepTime = timeInSeconds * 1000;
		if (this.sleepTime == 0) {
			this.sleepTime = defaultSleepTime;
		}
	}
	
	public void run() {
		log.debug("Starting Persistent Monitor run, sleep time in millis is  " + sleepTime);
		while(!stopped) {	
			//log.debug("Running Persistent Monitor, map,method size are "
			//			+ persistMap.size() + "," + persistMethodMap.size());
			for(File f : persistMap.keySet()) {				 
				 serialize(f, persistMap.get(f));
			}
			for(File f : persistMethodMap.keySet()) {				 
				 serialize(f, persistMethodMap.get(f).getSerializableObject());
			}						
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {	} 
		}
		log.debug("Stopping Persistent Monitor run");
	}

	synchronized public void stop(Object requestingObject) {		
		Set<File> fileset = objectList.get(requestingObject);
		if (fileset != null) {
			for (File f:fileset) {
				persistMap.remove(f);
				persistMethodMap.remove(f);
			}
		}
		objectList.remove(requestingObject);
	}
	
	
	/**
	 * @param f
	 * @param obj
	 */
	public static void serialize(File f, Serializable obj) {
		FileOutputStream fos;
		ObjectOutputStream out;
		try {
		      fos = new FileOutputStream(f);
		      out = new ObjectOutputStream(fos);
		      out.writeObject(obj);
		      out.close();
		 } catch(IOException ex) {
		     ex.printStackTrace();
		 }
	}	
}
