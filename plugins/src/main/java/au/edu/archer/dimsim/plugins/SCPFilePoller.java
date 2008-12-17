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

/*
 * scpFilePoller.java
 *
 * Created on March 28, 2007, 12:58 PM
 *
 */

package au.edu.archer.dimsim.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author mrafi
 * Organisation : Archer Project
 */
public class SCPFilePoller extends Thread {    
    
	static Logger logger = Logger.getLogger(SCPFilePoller.class);
	
	public static final int ONE_MILLISECOND = 1;  //default value used in sleep is milli second
	public static final int ONE_SECOND = ONE_MILLISECOND * 1000;
    public final int sleepTime = ONE_SECOND * 10;  //Ten Seconds
    
    protected File scpFile;
    protected long prevModifiedTime = 0;
    private boolean stop;
    private ImageDirEventListener imageEventListener;
    private Map<String,String> currParams;
    String imagesDir = null;
    
    final static String OPENSAMPLE_UpperCase = "OPENSAMPLE";
    final static String CLOSESAMPLE_UpperCase = "CLOSESAMPLE";        
    
    private String prevOpen;

    //private Object paramMap;    
    
    public SCPFilePoller(String scpFilename) {
        setSCPFile(scpFilename);
        currParams = null;
    }    
    
    @SuppressWarnings("unused")
	public void setEventListener(ImageDirEventListener scpListen) {
    	
    	this.imageEventListener = scpListen;
    	
    }
    
    public void setSCPFile(String scpFileName) {    	
    	if (scpFileName != null) {
    		this.scpFile = new File(scpFileName);
    		this.prevModifiedTime = 0;  //force check for new ImageDir
    	} 	
    	
    }
    
    public Map<String,String> getCurrParams() {
    	return this.currParams;
    }
    
    public void terminate() {
        this.stop = true;
    }
    
    public void run() {
        //logger.info
    	System.out.println("Monitoring SessionScript file in " + scpFile.getParent() + " directory");        
        
        long currModifiedTime;  
        String newImageDir;
        boolean process;
        
        while (!stop) {
            currModifiedTime = this.scpFile.lastModified();            
            if (currModifiedTime > this.prevModifiedTime) {                
                
                this.prevModifiedTime = currModifiedTime;
                
                process = false;
                newImageDir = checkNewDir();                  
                if (this.imagesDir == null)  { //avoid nullpointerexception
                    if (newImageDir != null) {
                        process = true;
                    }
                } else if (!this.imagesDir.equals(newImageDir)) {
                    process = true;
                }
                
                if (process) {  //notify the listener that the directory was found                	
                    this.imagesDir = newImageDir;                    
                    if (imageEventListener != null) {
                    	imageEventListener.newImageDirectory(this.imagesDir, this.getCurrParams());                    
                    }
                }
            }

            try {
                Thread.sleep(this.sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Each line in SCP file begins with a directive, followed by parameters related to that directive. 
     * Of interest to CIMA are the OpenSample and CloseSample directives.
     * The image Directory is fetched from the 'ImageDirectory' parameter of the OpenSample command.
     * 
     * Two CloseSample commands without an intervening OpenSample command indicates that the user 
     * has stopped image colleciton process when the second CloseSample command was issued.
     */
     private String checkNewDir () {        
        
        String line = null;    
        String lastOpen = null;                        
        boolean openFound = false;
        int closeCount = -2;               
        
        try {
            BufferedReader in   = new BufferedReader(new FileReader(scpFile));            
            
            while ((line = in.readLine()) != null) {                
                if (checkProcessRequired(line))   {                                                            
                    if (line.toUpperCase().startsWith(OPENSAMPLE_UpperCase)) {                     
                            lastOpen = line; 
                            openFound = true;
                            closeCount = 0;
                        
                    } else if (line.toUpperCase().startsWith(CLOSESAMPLE_UpperCase)) {                        
                           if (openFound) closeCount++;
                    }
                    if (closeCount == 2) { //real close command
                        lastOpen = null;
                        openFound = false;
                        closeCount = -1;                        
                    }
                }
            }      
            
            in.close();            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            
        }        
        
        if ((this.prevOpen != null) && this.prevOpen.equals(lastOpen)) {
            return this.imagesDir;
        }
        
        this.prevOpen = lastOpen;
        
        return processAction(lastOpen);        
    }

    private boolean checkProcessRequired(String line) {
       if (line != null)  {       
        if (line.toUpperCase().startsWith(OPENSAMPLE_UpperCase) 
            || line.toUpperCase().startsWith(CLOSESAMPLE_UpperCase))            
             return true; 
       }
      
       return false;
    } 
     
    public Map<String, String> extractParams(byte[] p) {
        Map<String, String> m = new HashMap<String,String>();
        
        //boolean wordStart, braces;
        
        StringBuffer  key = new StringBuffer();
        StringBuffer  value = new StringBuffer();
        
        int i = 0;
        while (i < p.length) {
            if (p[i] != '=') {
                key.append((char)p[i]);
                i++;
            }
            else {                
                while ((++i < p.length) && (p[i] == ' ')) { ; }   //ignore spaces before start of value
                
                if (i < p.length) {
                    if (p[i] == '{' ) {   // value delimited by braces
          //                  braces = true;                            
                            while (((++i) < p.length) && (p[i] != '}')) {
                                value.append((char)p[i]);
                            }                            
                            i++;
                    }
                    else {   // single value, delimited by space                        
                        while ((i < p.length) && (p[i] != ' ')) {
                                value.append((char)p[i++]);
                            }
                    }
                    m.put(key.toString().trim(),value.toString().trim());
                    key = new StringBuffer();
                    value = new StringBuffer();
                }                
            }
        }     
        
        return m;
    }
       
    private String processAction(String commandLine) {       
       
       if (commandLine != null) {
           
           String[] tempArr = commandLine.split(" ", 2);
           String command = tempArr[0];
           String params = (tempArr.length > 1)?tempArr[1]:null;

           if (command.compareToIgnoreCase(OPENSAMPLE_UpperCase) == 0) {                
               //return new File(extractParams(params.getBytes()).get("ImageDirectory").toString());
        	   this.currParams = extractParams(params.getBytes());
        	   return this.getCurrParams().get("ImageDirectory");
           }           
           
       }
       return null;
       
    }    
    
 
    public static void main(String[] args) {    
        
        String scpFilePath = "/home/mrafi/work/SessionScript.scp";
        
        SCPFilePoller scp = new SCPFilePoller(scpFilePath);
        scp.setEventListener(new ImageDirEventListener () {
            public void newImageDirectory(String directory, Map<String,String> currParams) {
                String name;
                if (directory == null) {
                    name = "null";
                } else {
                   name = directory; 
                }
                System.out.println("Out Image Dir  is " + name);
                if (currParams != null) {
                	System.out.println("Parameters are :");
                	for(String s:currParams.keySet()) {
                		System.out.println(s + " = " + currParams.get(s));
                	}
                }
            }
        } );
        scp.start();
        
        try {
            System.in.read();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        scp.terminate();
        
        }        
    
}
