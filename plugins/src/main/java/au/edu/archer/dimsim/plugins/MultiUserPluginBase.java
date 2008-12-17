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

package au.edu.archer.dimsim.plugins;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.parcel.CommandOperationResponseType;
import org.instrumentmiddleware.cima.parcel.CommandType;
import org.instrumentmiddleware.cima.parcel.EndpointType;
import org.instrumentmiddleware.cima.parcel.ResponseStatusEnum;
import org.instrumentmiddleware.cima.parcel.VariableType;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.manager.IPluginManager;
import org.instrumentmiddleware.cima.plugin.producer.IParcelCreator;
import org.instrumentmiddleware.cima.plugin.producer.impl.AbstractProducer;
import org.instrumentmiddleware.cima.session.ISession;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import au.edu.archer.dimsim.security.IEndPointSecurity;
import au.edu.archer.dimsim.security.impl.EndpointSubscriptionSecurityModel;
import org.instrumentmiddleware.cima.extension.operation.ICommandEnable;


/**
 * @author mrafi
 *
 */
public abstract class MultiUserPluginBase<T extends AbstractProducer> extends AbstractProducer 
	implements ICommandEnable, IMultiUserStoreListener, ApplicationContextAware, IDimsimMultiUserParams {

	private static Logger log = Logger.getLogger(MultiUserPluginBase.class);	
	
	protected ApplicationContext context = null;
	
	private boolean addValidUsersToEndPointSecurity;
	private IMultiUserStore userRecord;	
	private Map<String,T> userPlugins = new HashMap<String, T> ();
		
	public static final String VALIDATE_USER_COMMAND = "validateUserCommand";
	public static final String VALIDATE_USER_PARAM_USER = "validateUserID";
	public static final String VALIDATE_USER_PARAM_PASSWORD = "validatePassword";
	public static final String VALIDATE_USER_PARAM_CONSUMERID = "validateForConsumerID";
	
	protected abstract T getNewInstance(String userID);	

	protected abstract void setUserParams(T plugin,
			Map<String, Object> params) ;
	
	private final class waitForContext extends Thread {
		public void run() {
			while(context == null) {
				try {
					sleep(1000 * 10);  //sleep 10 seconds
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
			startUserPlugins();
		}
	}
	
	
	/**
	 * @param id
	 * @param rpCreator
	 * @param director
	 * @throws Exception
	 */
	
	public MultiUserPluginBase(ICIMADirector director, IParcelCreator pCreator,
			String id ) throws Exception {
		super(id,pCreator,director);		
	}
	
	public void setAddValidUsersToEndPointSecurity(boolean b) {
		this.addValidUsersToEndPointSecurity = b;
	}	
	
	private void userSetup() throws PluginException {
		if (this.userRecord != null) {	
			Set<String> userSet = userRecord.getUsers();
			
			//Remove Plug-ins for deleted users
			for(String user : userPlugins.keySet()) {
				if (!userSet.contains(user)) {
					T rPlugin = userPlugins.get(user);
					if (rPlugin != null) {
						rPlugin.stop();			
					}
					userPlugins.remove(user);
				}
			}
			
			//Process new Users/SCPFile location
			for (String user : userSet) {	
				//log.debug("setup(): Processing User " + user);
				T rPlugin = getUserPlugin(user);				
				if (rPlugin == null) {
					try {				
						rPlugin = getNewInstance(user);
						rPlugin.setDeliveryStrategy(this.deliveryStrategy);
						rPlugin.setStartOnLoad(this.startOnLoad);						
					} catch (Exception e) {
						log.debug("Error Creating RigakuSCPPlugin for user : " + user);
					}					
					this.userPlugins.put(user, rPlugin);					
				}
				
				Map<String,Object> params = userRecord.getUserParams(user);
				setUserParams(rPlugin,params);								
			}	
			
			this.startUserPlugins();
		}		
	}	
	
	public CommandOperationResponseType processCommandOperation(
	    	   CommandType operation, Calendar whatCal, ISession session,
	    	   CommandOperationResponseType response) throws PluginException {
	    	
    	   response.setStatus(ResponseStatusEnum.FAILURE); //set by default, changed later if request is successful
			
	       String command = operation.getCommandName();
	       if   (!command.equalsIgnoreCase(VALIDATE_USER_COMMAND)) {
	               response.setMessage(command + " Command not available");
	               log.info("Illegal command request : command is  " + command);
	               return response;
	       }
	
	       log.debug("processing command " + command);	
	       
	       VariableType[] params = operation.getParameterArray();	       
	       String paramName, userID = null;
	       String password = "";  //default password is empty String
	       String consumerID = null;
	       for (int i = 0; i < params.length; i++) {
	    	   	   paramName = params[i].getName();
	               if (paramName.equalsIgnoreCase(VALIDATE_USER_PARAM_USER)) {
	            	   userID = params[i].getValue();
	               } else if (paramName.equalsIgnoreCase(VALIDATE_USER_PARAM_PASSWORD)) {
	            	   password = params[i].getValue();
	               } else if (paramName.equalsIgnoreCase(VALIDATE_USER_PARAM_CONSUMERID)) {
	            	   consumerID = params[i].getValue();
	               } 
	       }		       
	       
	       if (userID != null)  {
	    	   if (this.userRecord.validate(userID,password)) {
	            		   response.setMessage("UserId & Password validated for user " + userID);
	            		   response.setStatus(ResponseStatusEnum.SUCCESS);	
	            		   if (addValidUsersToEndPointSecurity) {
	            			   Set<String> tuple = new HashSet<String>();
	            			   EndpointType endpoint = (EndpointType) session.get("endpoint");
	            			   if (consumerID != null) {
	            				   tuple.add(consumerID);
	            				   tuple.add(endpoint.getUrl());
	            			   } else {
	            				   tuple = EndpointSubscriptionSecurityModel.getConsumerTuple(session);
	            			   }
	            			   tuple.add(getUserPluginID(userID));
	            			   getEndPointSecurity().add(tuple);
	            		   }
	           } else {
	        	   response.setMessage("Password " + password + " is invalid for user " + userID);      
	           }		           
	       }  else {		       
	    	   response.setMessage("Command " + command  + " failed : Missing parameter "  
	    		   + VALIDATE_USER_PARAM_USER);
	       }
	       
	       return response;
	}

	public String getUserPluginID(String userID) {
		
		T rPlugin = getUserPlugin(userID);
		if (rPlugin != null) {
			return rPlugin.getId();
		}
		
		return this.getId() + "_" + userID;		
	}

	public T getUserPlugin(String userID) {
		T rPlugin = this.userPlugins.get(userID);
		return rPlugin;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;		
	}	
	
	public void stop() throws PluginException {
		for(T rPlugin : userPlugins.values()) {
			if (rPlugin != null) rPlugin.stop();
		}
		super.stop();				
	}
	
	private void startUserPlugins() {
		
		if (context == null) {
			//log.debug("startUserPlugins() : Error: Context is null, user Plugins not started");
			new waitForContext().start(); //start a new thread to run this method 
			return;
		}
		
		IPluginManager pluginManager = 
			(IPluginManager) context.getBean("pluginManager");	
		for(String user :userPlugins.keySet()) {
			T rPlugin = userPlugins.get(user);
			if (rPlugin != null) {
				pluginManager.addPlugin(rPlugin);
				//duplicate add will result in a log message, so no problems re-using current method.
				if (rPlugin.startOnLoad()) {
					try {
						pluginManager.startPlugin(rPlugin);
						//restart will result in a log message, so no problems 
					} catch (PluginException e) {
						e.printStackTrace();
					}
				}
			}
		}		
	}
	
	//Interface IMultiUserStoreListener
	@SuppressWarnings("unused")
	public void userStoreChanged(IMultiUserStore u) {
		this.userRecord = u;
		if (u == null) {
			log.debug("Input IMultiUserStore is null");
		} else {
			try {			
				userSetup();			
			} catch (BeansException e) { 
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private IEndPointSecurity getEndPointSecurity() {
		Map securityBeansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, IEndPointSecurity.class);
		if ((securityBeansMap != null) &&
				(securityBeansMap.size() > 0)) {
				for(Object beanName : securityBeansMap.keySet()) {
					//return first available Bean Object
					return (IEndPointSecurity) securityBeansMap.get(beanName);
				}
		}
		return null;
	}
	
}
