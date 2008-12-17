package org.instrumentmiddleware.cima.transport.impl;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import org.apache.log4j.Logger;

import org.apache.xmlbeans.XmlException;

import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.core.ProcessException;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.EndpointTypeEnum;
import org.instrumentmiddleware.cima.parcel.EndpointTypeEnum.Enum;
import org.instrumentmiddleware.cima.parcel.EntityType;
import org.instrumentmiddleware.cima.parcel.ParcelDocument;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.parcel.SubscriptionRequestType;
import org.instrumentmiddleware.cima.plugin.IPlugin;
import org.instrumentmiddleware.cima.transport.ITransportProvider;
import org.instrumentmiddleware.cima.transport.TransportException;
import org.instrumentmiddleware.cima.transport.impl.AbstractTransportProvider;
import org.instrumentmiddleware.cima.util.CIMAUtil;
import org.instrumentmiddleware.cima.util.ParcelUtil;
import org.instrumentmiddleware.cima.ws.CimaWS;

import java.io.IOException;

import java.net.MalformedURLException;

import java.util.Properties;

import javax.jws.WebService;


@WebService(endpointInterface = "org.instrumentmiddleware.cima.ws.CimaWS")
public class SOAPTransport
        extends AbstractTransportProvider
        implements CimaWS {
	private static Logger log = Logger.getLogger(SOAPTransport.class);
	private static ICIMADirector director;
	private long startupTime = 10000;
	private static String PROPERTIES_FILE = "transport.properties";
	private String localEndpointUrl = "";

	public Enum getType() {
		return EndpointTypeEnum.SOAP;
	}

	private static long loadTime = System.currentTimeMillis();
	public boolean isAvailable() {
		//TODO: Find a better way to know if I'm ready to recieve parcels.
		return super.isAvailable() && System.currentTimeMillis() - loadTime > startupTime;
	}


	public void setLocalEndpointUrl(String localEndpoint) {
		this.localEndpointUrl = localEndpoint;
	}


	public String getLocalEndpointUrl() {
		// TODO: find a really nice way to get this (even if just from a properties file!)
		// preferrably from the webapp configuration...

		// this has completely destroyed my faith in the servlet API and the projects that "enhance" it,
		// WRT the ability to do useful things... (Spring, CXF)
		// The address will come with the port that the servlet container is listening on (need a request to
		// get that), the context path we can get from the servlet context (only in servlet 2.5 spec),
		// the /ws section comes from web.xml where we're mapping to the CFX servlet (arguably we could map
		// a different URI but we shouldn't have to), and the last bit we can get if we mess with spring
		// a bit (see the wsAddressSniffer in the cimaWebService-servlet.xml file)
		// In the end we're better off getting the user to work out what the address is and putting
		// it in a config file... :(
		//log.error("sniffed address: " + AddressSniffer.address);

		
		// this hasn't worked yet either
	/*	Properties properties = new Properties();
		try {
		properties.load(getClass().getResourceAsStream("transport.properties"));
		}
		catch (IOException e) {
		log.warn("error reading properties file (" + PROPERTIES_FILE + "): " + e);
		}
		catch (NullPointerException e) {
		log.warn(PROPERTIES_FILE + " doesn't exist!");
		}

		localEndpointUrl =  properties.getProperty("localEndpoint", "http://localhost:8080/ws/cima");
	*/	
		log.debug("LocalEndPointURL is " + localEndpointUrl);
		return localEndpointUrl;
	}


	public Parcel send()
	        throws TransportException {
		return send(sendingPlugin, remoteEndpoint, outgoingParcel);
	}


	private CimaWS getRemoteCimaPort(String endpointUrl) {
		// prep the WS client
		JaxWsProxyFactoryBean bf = new JaxWsProxyFactoryBean();
		bf.setServiceClass(CimaWS.class);
		bf.setAddress(endpointUrl);
		Object stub = bf.create();
		log.debug("bf: " + bf);

		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setAllowChunking(false);
		log.debug("policy: " + policy);
		
		try {
			log.debug("bf2: " + bf);
			ClientFactoryBean bean = bf.getClientFactoryBean();
			log.debug("bean: " + bean);
			Client client = bean.getClient();
			log.debug("client: " + client);
			HTTPConduit conduit = (HTTPConduit) client.getConduit();
			log.debug("conduit: " + conduit);
			conduit.setClient(policy);
		}
		catch (Throwable e) {
			e.printStackTrace();
		}

		return (CimaWS) bf.create();
	}


	// outgoing requests
	public Parcel send(IPlugin source, EndpointType endpoint, Parcel parcel)
	        throws TransportException {
		log.trace("sending parcel: " + parcel.getSequenceId());


		//		log.trace("sending parcel: " + parcel);
		/*
		                // prep the WS client
		                JaxWsProxyFactoryBean bf = new JaxWsProxyFactoryBean();
		                bf.setServiceClass(CimaWS.class);
		                bf.setAddress(endpoint.getUrl());

		                CimaWS port = (CimaWS) bf.create();
		*/
		CimaWS port = getRemoteCimaPort(endpoint.getUrl());

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
		log.debug("sending to " + endpoint.getUrl());

		// unmarshal the request
		ParcelDocument pd = ParcelDocument.Factory.newInstance();
		pd.setParcel(parcel);

		// process
		String responseParcelString = port.handleParcel(pd.toString());

		//log.trace("received response: " + responseParcelString);

		// marshal the response
		try {
			ParcelDocument doc = ParcelDocument.Factory.parse(responseParcelString);
			parcel = doc.getParcel();

			//log.trace("received parcel: " + parcel);
			return parcel;
		}
		catch (XmlException e) {
			e.printStackTrace();
			throw new TransportException("Error retrieving parcel: " + e);
		}
	}


	// incoming requests
	public String handleParcel(String parcelString) {
		ParcelDocument doc = null;
		Parcel parcel = null;
		Parcel response = null;

		String res = null;

		//		log.trace("received parcel: " + parcelString);
		log.trace("received parcel");

		// this method of unmarshalling the request then processing then marshalling
		// again is HORRIBLE.  It's an interim solution until CXF supports XMLBeans
		// as a native data binding (supposed to be in version 2.1)

		// unmarshal the request
		try {
			doc = ParcelDocument.Factory.parse(parcelString);
			parcel = doc.getParcel();
		}
		catch (XmlException e) {
			e.printStackTrace();
			res = "failure extracting parcel";

			return res;
		}

		// process
		try {
			// can't use ICIMADirectorAware because we might actually be the
			// initial entry point (ie, the call directly below is what gets
			// the spring IOC container going!)
			if (director == null) {
				director = CIMAUtil.getDirector();

				if (director == null) {
					throw new ProcessException(
					    "cannot create director - CIMA at this end is broken!");
				}
			}

			response = director.handleIncomingParcel(parcel);
		}
		catch (ProcessException e) {
			e.printStackTrace();

			return "failure processing parcel: " + e;
		}
		catch (Exception e) {
			e.printStackTrace();
			res = "failure processing parcel";

			return res;
		}

		// marshal the response
		ParcelDocument pd = ParcelDocument.Factory.newInstance();
		pd.setParcel(response);

		return pd.toString();
	}


	/**
	 * @param args
	 * @throws MalformedURLException
	 * @throws MyProxyException
	 */
	public static void main(String[] args)
	        throws MalformedURLException, TransportException {
		// Create the web service proxy
		//		JaxWsProxyFactoryBean bf = new JaxWsProxyFactoryBean();
		//		bf.setServiceClass(Cima.class);
		//		bf.setAddress("http://localhost:8080/ws/cima");
		//		Cima port = (Cima)bf.create();
		Parcel parcel = ParcelUtil.newParcel("subscribe");
		parcel.setSequenceId(1);

		EntityType sender = parcel.addNewSender();
		sender.setId("ExampleConsumer");

		EntityType recipient = parcel.addNewRecipient();
		recipient.setId("ExampleCreator");

		recipient = parcel.addNewRecipient();
		recipient.setId("ExampleProducer");

		SubscriptionRequestType request = (SubscriptionRequestType) parcel.addNewBody()
			.changeType(SubscriptionRequestType.type);
		EndpointType endpoint = request.addNewEndpoint();
		endpoint.setType(EndpointTypeEnum.SOAP);
		System.out.println("" + args.length);

		for (int i = 0; i < args.length; i++) {
			System.out.println(i + ": " + args[i]);
		}

		if (args.length > 0) {
			endpoint.setUrl(args[0]);
		}
		else {
			endpoint.setUrl("http://localhost:8080/ws/cima");
		}

		Parcel response = new SOAPTransport().send(null, endpoint, parcel);
		System.out.println(response.toString());
	}

	public void setStartupTime(long startupTime) {
		this.startupTime = startupTime;
	}
}
