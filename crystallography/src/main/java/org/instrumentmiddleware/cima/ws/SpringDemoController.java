package org.instrumentmiddleware.cima.ws;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JSONString;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import org.apache.log4j.Logger;

import org.apache.xmlbeans.XmlException;

import org.instrumentmiddleware.cima.*;
import org.instrumentmiddleware.cima.core.ICIMAClientUtil;
import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.parcel.DescriptionResponseType;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EndpointTypeEnum;
import org.instrumentmiddleware.cima.parcel.EntityType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel.Sessions;
import org.instrumentmiddleware.cima.parcel.ParcelTypeEnum;
import org.instrumentmiddleware.cima.parcel.ResponseBodyType;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.ResponseType;
import org.instrumentmiddleware.cima.parcel.SessionType;
import org.instrumentmiddleware.cima.parcel.SubscriptionResponseType;
import org.instrumentmiddleware.cima.plugin.ICIMAPlugin;
import org.instrumentmiddleware.cima.plugin.IPlugin;
import org.instrumentmiddleware.cima.plugin.manager.IPluginManager;
import org.instrumentmiddleware.cima.session.ISession;
import org.instrumentmiddleware.cima.session.ISessionManager;
import org.instrumentmiddleware.cima.transport.ITransportManager;
import org.instrumentmiddleware.cima.transport.TransportException;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.instrumentmiddleware.cima.util.SubscribeInfo;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


public class SpringDemoController
        extends SimpleFormController {
	private static Logger log = Logger.getLogger(SpringDemoController.class);
	private static final String DEFAULT_URL = "http://localhost:8081/ws/cima";
	public static final String SUBSCRIPTIONS_KEY = "subscriptions";
	public static final String FAILED_SUBSCRIPTIONS_KEY = "failed";
	private IPlugin plugin;

	public void setPlugin(IPlugin plugin) {
		this.plugin = plugin;
		log.info("got plugin: " + plugin);
	}


	// this is being overridden so that we can stay on the same page after submitting the form, even successfully!
	protected Object formBackingObject(HttpServletRequest request)
	        throws Exception {
		String formAttrName = getFormSessionAttributeName(request);
		Object sessionFormObject = request.getSession(true)
			.getAttribute(formAttrName + ".mine");

		if (sessionFormObject == null) {
			sessionFormObject = createCommand();

			// keep a copy for later
			//			logger.debug("Setting form session attribute [" + formAttrName + ".mine] to [" + sessionFormObject + "]");
			request.getSession(true)
			.setAttribute(formAttrName + ".mine", sessionFormObject);

			FormData form = (FormData) sessionFormObject;
			form.setServerURL(DEFAULT_URL);

			Map<String, Object> model = form.getModel();
			ICIMADirector director = CIMAUtil.getDirector();
			model.put("localPlugins", director.listPluginIds());
			model.put(SUBSCRIPTIONS_KEY, new ArrayList<Subscription>());
			model.put("JSON", (JSONObject) JSONSerializer.toJSON(model));
		}

		return sessionFormObject;
	}


	// this is being overridden so that we can stay on the same page after submitting the form, even successfully!
	public ModelAndView onSubmit(Object command)
	        throws ServletException {
		FormData form = null;

		if (command instanceof FormData) {
			form = (FormData) command;
		}

		if (form != null) {
			Map<String, Object> model = form.getModel();

			try {
				// initialise CIMA
				// TODO: check if this is necessary
				ICIMADirector director = CIMAUtil.getDirector();
				String action = form.getAction();
				Map<String, Object> newModel = null;
				System.out.println("action: " + action);

				List<SubscriptionResponseType> subscriptions = (List) model.get(SUBSCRIPTIONS_KEY);

				if (action.equals("subscribe")) {
					newModel = doSubscribe(form);
				}
				else if (action.equals("updateRemote")) {
					newModel = doRemoteUpdate(form);
				}
				else if (action.equals("unsubscribe")) {
					newModel = doUnSubscribe(form);
				}
				// this happens when the user presses enter for the form
				else {
					newModel = new HashMap<String, Object>();
					newModel.put(SUBSCRIPTIONS_KEY, new ArrayList<SubscriptionResponseType>());
				}

				List<SubscriptionResponseType> newSubscriptions = (List) newModel.get(SUBSCRIPTIONS_KEY);

				if (newSubscriptions == null) {
					newModel.put(SUBSCRIPTIONS_KEY, subscriptions);
				}

				// TODO: make sure we combine anything else that needs it
				// we should also zero some things like errors...
				model.put("remotePlugin", form.getRemotePlugin());

				// TODO: add remote plugins
				model.remove("sessions");
				model.putAll(newModel);
			}
			catch (Exception e) {
				model.put("error", e);
				e.printStackTrace();
			}

			System.out.println("model: " + model);

//			model.remove(model.get("JSON"));
//			model.remove("JSON");
//			model.put("JSON", (JSONObject) JSONSerializer.toJSON(model));
		}
		else {
			logger.error("didn't get the form object");
		}

		return new ModelAndView(new RedirectView(getSuccessView()));

		// this doesn't work!  infinite loop as spring tries to reprocess the form
		//		return new ModelAndView(getSuccessView(), "subscribeCommand", form);
	}


	public Map<String, Object> doRemoteUpdate(FormData form)
	        throws ServletException, IOException, Exception {
		ICIMADirector director = CIMAUtil.getDirector();
		ICIMAClientUtil util = director.getClientUtil();

		IPluginManager pluginManager = util.getPluginManager();
		ITransportManager transportManager = util.getTransportManager();
		ISessionManager sessionManager = util.getSessionManager();

		System.out.println("plugin man: " + pluginManager);
		System.out.println("transport man: " + transportManager);
		System.out.println("session man: " + sessionManager);

		Map<String, Object> model = new HashMap<String, Object>();

		Parcel describe = ParcelUtil.newParcel(ParcelTypeEnum.DESCRIBE);

		// TODO: proper sequence handling
		describe.setSequenceId(11);

		EntityType recipient = describe.addNewRecipient();
		recipient.setId(ICIMADirector.CIMA);

		EndpointType endpoint = EndpointType.Factory.newInstance();
		endpoint.setUrl(form.getServerURL());
		endpoint.setType(EndpointTypeEnum.SOAP);
		log.debug("recipient: " + recipient);
		log.debug("endpoint: " + endpoint);

		try {
			Parcel responseParcel = director.handleOutgoingParcel(null,
				    describe, endpoint);

			ResponseType[] responses = ((ResponseBodyType) responseParcel.getBody()
				.changeType(ResponseBodyType.type)).getResponseArray();

			// there should only be one
			for (ResponseType response : responses) {
				if (response.getStatus() == ResponseStatusEnum.SUCCESS) {
					List<String> remotePlugins = new ArrayList<String>();
					DescriptionResponseType drt = (DescriptionResponseType) response.changeType(DescriptionResponseType.type);
					DescriptionResponseType.Plugins plugins = drt.getPlugins();

					for (DescriptionResponseType.Plugins.Plugin plugin : plugins.getPluginArray()) {
						remotePlugins.add(plugin.getName());
					}

					model.put("remotePlugins", remotePlugins);
				}
				else
				{
					model.put("error",
					    "plugin list failed: " + response.getMessage());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("error unsubscribing: " + e);
		}

		return model;
	}


	public Map<String, Object> doUnSubscribe(FormData form)
	        throws ServletException, IOException, Exception {
		ICIMADirector director = CIMAUtil.getDirector();
		ICIMAClientUtil util = director.getClientUtil();

		IPluginManager pluginManager = util.getPluginManager();
		ITransportManager transportManager = util.getTransportManager();
		ISessionManager sessionManager = util.getSessionManager();

		System.out.println("plugin man: " + pluginManager);
		System.out.println("transport man: " + transportManager);
		System.out.println("session man: " + sessionManager);

		Map<String, Object> model = new HashMap<String, Object>();

		Map<ICIMAPlugin, List<ISession>> sessions = new HashMap<ICIMAPlugin, List<ISession>>();

		for (String pluginId : pluginManager.listPluginIds()) {
			ICIMAPlugin plugin = pluginManager.getPlugin(pluginId);
			sessions.put(plugin, sessionManager.getSessions(plugin));
		}

		log.debug("loaded " + sessions.size() + " sessions");
		model.put("sessions", sessions);

		ICIMAPlugin plugin = pluginManager.getPlugin(form.getLocalPlugin());

		// TODO: this is wrong - we should be getting subRemotePlugin
		log.debug("remote plugin: " + form.getRemotePlugin());

		List<ISession> pluginSessions = sessionManager.getSessions(plugin);
		ISession selectedSession = null;

		for (ISession session : pluginSessions) {
			log.debug("checking session " + session);

			if (session.getSubscriber().getId().equals(form.getRemotePlugin())) {
				selectedSession = session;

				break;
			}
		}

		log.debug("session: " + selectedSession);

		if (selectedSession == null) {
			model.put("error", "couldn't find session");

			return model;
		}

		List<Subscription> subscriptions = (List) form.getModel()
			.get(SUBSCRIPTIONS_KEY);
		Subscription subscription = null;

		for (Subscription s : subscriptions) {
			if (s.plugin == plugin) {
				subscription = s;
			}
		}

		model.put(SUBSCRIPTIONS_KEY, subscriptions);

/*
        //cont
        EndpointType endpoint = EndpointType.Factory.newInstance();
        endpoint.setUrl(form.getServerURL());
        endpoint.setType(EndpointTypeEnum.SOAP);

        SubscribeInfo info = new SubscribeInfo(endpoint, form.getRemotePlugin());
*/
		Parcel unsubscribe = ParcelUtil.newParcel(ParcelTypeEnum.UNSUBSCRIBE);

		// TODO: proper sequence handling
		unsubscribe.setSequenceId(10);

		Sessions ss = unsubscribe.addNewSessions();
		SessionType session = ss.addNewSession();
		session.setSessionId(selectedSession.getId());

		EndpointType endpoint = (EndpointType) selectedSession.get("endpoint");
		log.debug("endpoint: " + endpoint);

		try {
			Parcel responseParcel = director.handleOutgoingParcel(plugin,
				    unsubscribe, endpoint);

			ResponseType[] responses = ((ResponseBodyType) responseParcel.getBody()
				.changeType(ResponseBodyType.type)).getResponseArray();

			// there should only be one
			for (ResponseType response : responses) {
				if (response.getStatus() == ResponseStatusEnum.SUCCESS) {
					subscriptions.remove(subscription);
				}
				else
				{
					model.put("error",
					    "unsubscribe failed: " + response.getMessage());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error("error unsubscribing: " + e);
		}

		return model;
	}


	public Map<String, Object> doSubscribe(FormData form)
	        throws ServletException, IOException, Exception {
		ICIMAClientUtil util = CIMAUtil.getDirector().getClientUtil();
		System.out.println("util: " + util);

		IPluginManager pluginManager = util.getPluginManager();
		ITransportManager transportManager = util.getTransportManager();
		ISessionManager sessionManager = util.getSessionManager();

		System.out.println("plugin man: " + pluginManager);
		System.out.println("transport man: " + transportManager);
		System.out.println("session man: " + sessionManager);

		Map<String, Object> model = new HashMap<String, Object>();

		ICIMAPlugin plugin = pluginManager.getPlugin(form.getLocalPlugin());

		// this is a bit wierd, we're only doing one subscription
		ArrayList<Subscription> subscription = new ArrayList<Subscription>();
		ArrayList<Subscription> failures = new ArrayList<Subscription>();
		model.put(SUBSCRIPTIONS_KEY, subscription);
		model.put(FAILED_SUBSCRIPTIONS_KEY, failures);

		EndpointType endpoint = EndpointType.Factory.newInstance();
		endpoint.setUrl(form.getServerURL());
		endpoint.setType(EndpointTypeEnum.SOAP);

		SubscribeInfo info = new SubscribeInfo(endpoint, form.getRemotePlugin());

		SubscriptionResponseType response = plugin.doSubscribe(info);

		if (response.getStatus() == ResponseStatusEnum.SUCCESS) {
			subscription.add(new Subscription(plugin, response));
		}
		else
		{
			failures.add(new Subscription(plugin, response));
		}

/*
        Parcel parcel = ParcelUtil.newParcel("subscribe");
        parcel.setSequenceId(1);
*/

/*
        EntityType sender = parcel.addNewSender();
        sender.setId("ExampleConsumer");

        EntityType recipient = parcel.addNewRecipient();
        recipient.setId("ExampleCreator");

        recipient = parcel.addNewRecipient();
        recipient.setId("ExampleProducer");
*/

/*
        SubscriptionRequestType req = (SubscriptionRequestType) parcel.addNewBody()
            .changeType(SubscriptionRequestType.type);
        EndpointType endpoint = req.addNewEndpoint();
        endpoint.setType(EndpointTypeEnum.SOAP);
        endpoint.setUrl(url);

        List failed_subscriptions = new ArrayList();
        List subscriptions = new ArrayList();

        model.put("subscriptions", subscriptions);
        model.put("failed", failed_subscriptions);

        try {
            Parcel res = send(null, endpoint, parcel);

            try {
                ResponseType[] responses = ParcelUtil.getResponse(res)
                    .getResponseArray();

                for (int i = 0; i < responses.length; i++) {
                    SubscriptionResponseType srt = (SubscriptionResponseType) responses [i].changeType(SubscriptionResponseType.type);

                    if (srt.getStatus() == ResponseStatusEnum.SUCCESS) {
                        subscriptions.add(srt);
                    }
                    else {
                        failed_subscriptions.add(srt);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                model.put("error", e.getMessage());
            }
        }
        catch (TransportException e) {
            e.printStackTrace();
            model.put("error", e.getMessage());
        }
*/
		return model;
	}


	// outgoing requests
	public Parcel send(IPlugin source, EndpointType endpoint, Parcel parcel)
	        throws TransportException {
		parcel.setCreationTime(Calendar.getInstance());

		log.trace("sending parcel: " + parcel);

		// prep the WS client
		JaxWsProxyFactoryBean bf = new JaxWsProxyFactoryBean();
		bf.setServiceClass(CimaWS.class);
		bf.setAddress(endpoint.getUrl());

		CimaWS port = (CimaWS) bf.create();

		// this is the stuff for WSSec
		//		Client client = ClientProxy.getClient(port);
		//		Endpoint cxfEndpoint = client.getEndpoint();

		//		Map outProps = new HashMap();

		//		GlobusCredentialOutInterceptor wssOut = new GlobusCredentialOutInterceptor(outProps);
		//		cxfEndpoint.getOutInterceptors().add(wssOut);
		//		cxfEndpoint.getOutInterceptors().add(new SAAJOutInterceptor());

		//		outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE);
		//		outProps.put(WSHandlerConstants.USER, "myAlias");		
		//		outProps.put("cert", newCred);
		//		outProps.put(WSHandlerConstants.SIG_KEY_ID, "DirectReference");

		// this method of unmarshalling the request then processing then marshalling
		// again is HORRIBLE.  It's an interim solution until CXF supports XMLBeans
		// as a native data binding (supposed to be in version 2.1)

		// unmarshal the request
		ParcelDocument pd = ParcelDocument.Factory.newInstance();
		pd.setParcel(parcel);

		// process
		String responseParcelString = port.handleParcel(pd.toString());
		log.trace("received response: " + responseParcelString);

		// marshal the response
		try {
			ParcelDocument doc = ParcelDocument.Factory.parse(responseParcelString);
			parcel = doc.getParcel();
			log.trace("received parcel: " + parcel);

			return parcel;
		}
		catch (XmlException e) {
			e.printStackTrace();
			throw new TransportException("Error retrieving parcel: " + e);
		}
	}

	// TODO: move the JSON stuff out of here
	public class Subscription
	        implements JSONString {
		public SubscriptionResponseType responseInfo;
		public ICIMAPlugin plugin;

		public Subscription(ICIMAPlugin plugin, SubscriptionResponseType info) {
			this.responseInfo = info;
			this.plugin = plugin;
		}

		public String toString() {
			return getClass().getName() + ": plugin=" + plugin + ", response=" +
			responseInfo;
		}


		public SubscriptionResponseType getResponseInfo() {
			return responseInfo;
		}


		public void setResponseInfo(SubscriptionResponseType responseInfo) {
			this.responseInfo = responseInfo;
		}


		public ICIMAPlugin getPlugin() {
			return plugin;
		}


		public void setPlugin(ICIMAPlugin plugin) {
			this.plugin = plugin;
		}


/*
            public String toString() {
                return toJSONString();
            }
*/
		public String toJSONString() {
			return "{ \"plugin\": \"" + plugin.getId() + "\"" +
			", \"response\": { \"status\": \"" + responseInfo.getStatus() +
			"\"" + ", \"message\": \"" +
			responseInfo.getMessage().replace("\"", "'") + "\"" +
			", \"newSessionId\": \"" + responseInfo.getNewSessionId() + "\"" +
			", \"sender\": \"" + responseInfo.getSender().getId() + "\"" + "}" +
			"}";
		}
	}
}
