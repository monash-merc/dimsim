package au.edu.archer.dimsim.plugins;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.instrumentmiddleware.cima.parcel.BodyType;
import org.instrumentmiddleware.cima.parcel.IRegisteredParcel;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import au.edu.archer.cima.xtal.BeginXtalExperimentType;
import au.edu.archer.cima.xtal.EndXtalExperimentType;
import au.edu.archer.cima.xtal.XtalImageType;
import au.edu.archer.dimsim.buffer.event.BufferEvent;
import au.edu.archer.dimsim.buffer.event.BufferEvent.BufferEventType;
import au.edu.archer.dimsim.buffer.event.handler.ParcelBufferedEventHandler;
import au.edu.archer.dimsim.buffer.eventListener.IBufferEventListener;

public class projInfoController implements Controller, IBufferEventListener {
	public ParcelBufferedEventHandler eventListener;
	public String producerOfInterest = null;
	String project = "UnKnown";
	String sample = "None";
	String lastupdate = "";
	String imagedir = "";
	int count = 0;
	
	public void setEventListener(ParcelBufferedEventHandler eventListener) {
		this.eventListener = eventListener;
		if (this.eventListener != null) {
			this.eventListener.addListener(this);
		}
	}
	
	public void setProducerOfInterest(String id) {
		this.producerOfInterest = id;
	}
	
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) {
    	
    	Map<String,Object> model = new HashMap<String,Object>();
    	try {
			model.put("project",project);
			model.put("sample",sample);
			model.put("imagedir", imagedir);
			model.put("imagecount",count);
			model.put("lastparceltime", lastupdate);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return new ModelAndView("projInfo","model",model);
    }

	public String getProducerOfInterest() {
		return this.producerOfInterest;
	}
	
	public void eventParcel(IRegisteredParcel regParcel, String pluginID, BufferEvent.BufferEventType eventType)
	{
		if (eventType.equals(BufferEventType.ParcelBuffered)) {			
			BodyType b = regParcel.getBody();	
			Calendar c = regParcel.getCreationTime();
	
			if (b instanceof EndXtalExperimentType) {
				 project = "Unknown";
				 sample = "None";
				 imagedir = "";
				 count = 0;
			} else if (b instanceof XtalImageType) {
				count = count + 1;
			} else if (b instanceof BeginXtalExperimentType) {
				BeginXtalExperimentType bp = (BeginXtalExperimentType) b;
				project = bp.getProjectName();
				sample = bp.getSampleName();
				imagedir = bp.getImageDirectory();
				count = 0;
			} else return;
			
			try {
				lastupdate = new Date(c.getTimeInMillis()).toString();
			} catch (Exception ex) {
				lastupdate = new Date(System.currentTimeMillis()).toString();
			}
		}
	}
}
