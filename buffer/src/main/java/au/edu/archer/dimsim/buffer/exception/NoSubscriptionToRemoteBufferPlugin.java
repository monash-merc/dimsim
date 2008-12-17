/**
 * 
 */
package au.edu.archer.dimsim.buffer.exception;

import org.instrumentmiddleware.cima.plugin.PluginException;

/**
 * Used by consumer plug-ins to throw an exception when a subscription to remote buffer
 * does not exist. On receipt of this exception, the user is expected to initiate a new
 * subscription to the remote buffer for successful continuation of the buffer request.
 * <p>
 * @author Rafi M Feroze
 */
public class NoSubscriptionToRemoteBufferPlugin extends PluginException {
	private static final long serialVersionUID = 5304544764960120674L;	
}
