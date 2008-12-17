package org.instrumentmiddleware.cima.ws;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FormData {
	protected static Logger logger = Logger.getLogger(FormData.class);
	private String serverURL;

//	private List<String> localPlugins;
//    private List<String> remotePlugins;
	private String action;
	private String sessionId;
	private String localPlugin;
	private String remotePlugin;
	private Map<String, Object> model = new HashMap<String, Object>();

	public void setAction(String action) {
		this.action = action;
	}


	public String getAction() {
		return action;
	}


/*
    public void setLocalPlugins(List<String> plugins) {
        this.localPlugins = plugins;
    }


    public List<String> getLocalPlugins() {
        return localPlugins;
    }
*/
	public void setServerURL(String url) {
		serverURL = url;
	}


	public String getServerURL() {
		return serverURL;
	}


	public void setModel(Map<String, Object> model) {
		this.model = model;
	}


	public Map<String, Object> getModel() {
		return model;
	}


	public String getSessionId() {
		return sessionId;
	}


	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}


	public String getLocalPlugin() {
		return localPlugin;
	}


	public void setLocalPlugin(String localPlugin) {
		this.localPlugin = localPlugin;
	}


	public String getRemotePlugin() {
		return remotePlugin;
	}


	public void setRemotePlugin(String remotePlugin) {
		this.remotePlugin = remotePlugin;
	}

/*
    public List<String> getRemotePlugins() {
        return remotePlugins;
    }


    public void setRemotePlugins(List<String> remotePlugins) {
        this.remotePlugins = remotePlugins;
    }
*/
}
