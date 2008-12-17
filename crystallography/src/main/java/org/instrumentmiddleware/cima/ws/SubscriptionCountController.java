package org.instrumentmiddleware.cima.ws;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.instrumentmiddleware.cima.core.ICIMAClientUtil;
import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.plugin.IPlugin;
import org.instrumentmiddleware.cima.plugin.manager.IPluginManager;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.session.ISessionManager;
import org.instrumentmiddleware.cima.transport.ITransportManager;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionCountController implements Controller {

    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) {

    	Map<String,Object> model = new HashMap<String,Object>();
    	String pId = "Rigaku_Monash"; //Rigaku_Monash";
    	try {
			model.put("count",getSubs(pId));
			model.put("producer",pId);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return new ModelAndView("subCount","model",model);
    }
    
    private int getSubs(String producerId) throws Exception {
    	ICIMADirector director = CIMAUtil.getDirector();
		ICIMAClientUtil util = director.getClientUtil();

		IPluginManager pluginManager = util.getPluginManager();
		ITransportManager transportManager = util.getTransportManager();
		ISessionManager sessionManager = util.getSessionManager();
		
		IPlugin plugin = pluginManager.getPlugin(producerId);
		
		int size = 0;
		try {
			List<ISession> sessions = sessionManager.getSessions(plugin);
			size = sessions.size();
		} catch (java.lang.NullPointerException ex) {
			size = 0;
		}
		
		return size;
    }

}

