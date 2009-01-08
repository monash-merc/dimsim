/**
*
* * Copyright (C) 2007-2009, Monash University
* *
* * This program was developed as part of the ARCHER project
* * (Australian Research Enabling Environment) funded by a   
* * Systemic Infrastructure Initiative (SII) grant and supported by the Australian
* * Department of Innovation, Industry, Science and Research
* *
* * This program is free software: you can redistribute it and/or modify
* * it under the terms of the GNU General Public  License as published by the
* * Free Software Foundation, either version 3 of the License, or
* * (at your option) any later version.
* *
* * This program is distributed in the hope that it will be useful,
* * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
* * or FITNESS FOR A PARTICULAR PURPOSE.  
* * See the GNU General Public License for more details.
* *
* * You should have received a copy of the GNU General Public License
* * along with this program.  If not, see <http://www.gnu.org/licenses/>.
* */

package au.edu.archer.dimsim.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Rafi M Feroze
 * Polls labjack and stores sensor data. When queried this controller returns agoogle chart api url
 * Default data returned is for minutes. 
 * To get a graph by hour, use query parameter "type=hour" in the url
 * To get a current sensor data as html, use query parameter "type=current" in the url
 */
public class sensorDisplayController implements Controller {
	private String sensorBaseURL;
	private int maxSize = 200;
	private float[][][] data;
	private long[][][] time;
	private int nextPos,hourPos;
	private boolean full, hourFull;
	public static final long ONE_HOUR_IN_MILLI = 1000 * 60 * 60;
	protected long lastHour = 0; 
	private float currTemp = 0.0f;
	private float currHumid = 0.0f;
	
	public sensorDisplayController() {
		this.sensorBaseURL = "http://localhost/labjack";
		setMaxSize(maxSize);
	}
	
	public void setMaxSize(int maxSize) {
		if (maxSize > 0) {
			this.maxSize = maxSize;
			//first index 0-temp/1-humid, last index  0-hour/1-min
			this.data = new float[2][maxSize][2]; 
			this.time = new long[2][maxSize][2];
			this.nextPos = 0;
			this.hourPos = 0;
			this.full = false;
			this.hourFull = false;
		}
	}
	
	public void setSensorBaseURL(String sensorBaseURL) throws MalformedURLException {
		@SuppressWarnings("unused")
		URL test = new URL(sensorBaseURL);
		
		this.sensorBaseURL = sensorBaseURL;
	}
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
	
		String type = arg0.getParameter("type");
		if (type == null) {
			type = "minute";
		} else if (type.equals("current")) {
			return displayCurrentData();
		}

		int p = 0;	
		float scale = 60 * 6 *1000f;	
		int count = this.hourPos;
		int startPos = 0;
		if (this.hourFull) { 
			count = this.maxSize;
			startPos = this.hourPos;
		}
		if (!type.equals("hour")) {
			p = 1;
		 	count = this.nextPos;
			scale = 60 * 1000f;
			if (this.full) { 
					count = this.maxSize;
					startPos = this.nextPos;
					}
		}
		
		long currTime = System.currentTimeMillis();
		String[] dout = {"","","",""};
		int readPos = 0;
		for(int i = 0; i < count; i++) {
			readPos = (startPos + i) % this.maxSize;
			float t1 = (currTime - this.time[0][readPos][p])/scale;
			dout[0] += String.format(",%05.2f", t1);
			dout[1] += String.format(",%06.2f", this.data[0][readPos][p]);
			float t2 = (currTime - this.time[1][readPos][p])/scale;
			dout[2] += String.format(",%05.2f", t2);
			dout[3] += String.format(",%06.2f", this.data[1][readPos][p]);
		}
		
		String chd = "chd=t:";
		for(int i = 0; i < 4; i++) {
			//Remove starting ',' from the string
			if (dout[i].startsWith(",")) { 
				dout[i] = dout[i].substring(1);
			}
			chd += dout[i] + "|";
		}
		if (chd.endsWith("|")) {
			int l = chd.length();
			if (l > 1) {
				chd = chd.substring(0,(l - 2));
			} else { 
				chd = "";
			}
		}	

		Map<String,Object> model = new HashMap<String,Object>();
    		try {
    			model.put("gurl", getChartUrl(currTime, chd, scale) );
    		}catch (Exception ex) {
    		}
    	
    		return new ModelAndView("sensorData","model",model);    
	}

	public ModelAndView displayCurrentData() {
		Map<String,Object> model = new HashMap<String,Object>();
    		try {
    			model.put("currtemp", this.currTemp );
    			model.put("currhumid", this.currHumid );
    		}catch (Exception ex) {
    		}
    	
    		return new ModelAndView("sensorTable","model",model);    
	}
	public void pollSensor() {
		float temp  = getCurrSensorData("temperature");
		float humid  = getCurrSensorData("humidity");
		long c = System.currentTimeMillis();
	
		if (temp > 0) {
			this.currTemp = temp;
			if (humid > 0) {
				this.currHumid = humid;
			}
			this.data[0][nextPos][1] = this.currTemp;
			this.data[1][nextPos][1] = this.currHumid;;
			this.time[0][nextPos][1] = c;
			this.time[1][nextPos][1] = c;
	
			
			//check for hourly data
			if ( c > (lastHour + ONE_HOUR_IN_MILLI)) {
			 	this.data[0][hourPos][0] = this.currTemp; 
			 	this.data[1][hourPos][0] = this.currHumid;
			 	this.time[0][hourPos][0] = c;
			 	this.time[1][hourPos][0] = c;

				lastHour = c;
				this.hourPos++;
				if (this.hourPos >= this.maxSize) {
					this.hourPos = 0;
					if (!this.hourFull) this.hourFull = true;
				}
			}
		
			this.nextPos++;
			if (this.nextPos >= this.maxSize) {
				this.nextPos = 0;
				if (!this.full) this.full = true;
			}
		}
	}

	private float getCurrSensorData(String urlParam) {
		float data = 0.0f;
		//System.out.println("in with "+ urlParam);
		try {			
			URL sensorURL = new URL(this.sensorBaseURL + "/" + urlParam);
			URLConnection uc = sensorURL.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(uc.getInputStream()));
			
			String dataStr = in.readLine();
			in.close();
			data = Float.valueOf(dataStr).floatValue();
		} catch (IOException e) {
			// do nothing
		}
		return data;
	}
	

	private Object getChartUrl(long currTime, String chd, float scale) {
		String retStr = "http://chart.apis.google.com/chart?cht=lxy&chs=400x240&chdlp=t&"
				+ chd 
				+ "&chdl=Temperature|Humidity&chco=2345FE,DCBA09&chxt=x,y,x&chg=-1,10&"
				+ getChxl(currTime, scale)
				+ "&chxs=2,0000DD,13&chf=bg,s,DFE8F6|c,s,FFFFFF";
			
		return retStr;
	}

	private String getChxl(long currTime, float scale) {
		String chxl = "";
		String chxp = "";
		SimpleDateFormat df = new SimpleDateFormat("H:mm");
		Date testDate;
		long lscale = new Float(scale).longValue();
		for (int i = 0; i < 6; i++) {
			testDate = new Date(currTime - (i *20 * lscale));
			chxl += "|" + df.format(testDate);
			chxp += "," + (i * 20);
		}
		if (chxl.startsWith("|")) {
			chxl = chxl.substring(1);
			chxp = chxp.substring(1);
		}
		chxl = "chxl=2:|" + chxl;
		chxp = "chxp=2," + chxp;
		return chxl + "&" + chxp;
	}

}
