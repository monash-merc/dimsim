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
package au.edu.archer.dimsim.plugins.srb;

import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFile;
import edu.sdsc.grid.io.srb.SRBFileSystem;
import edu.sdsc.grid.io.srb.SRBRandomAccessFile;

import org.apache.log4j.Logger;

import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import au.edu.archer.dimsim.plugins.srb.SRBConfig;
import au.edu.archer.dimsim.plugins.srb.SRBHelper;

import java.io.IOException;


public class SRBHelper {
	private static Logger log = Logger.getLogger(SRBHelper.class);
	private SRBFileSystem srbFS;
	private SRBAccount srbAccount;
	private SRBConfig srbConfig;

	public void setSRBConfig(SRBConfig config)
	        throws IOException {
		if (config == null) {
			throw new IOException("Null configuration supplied");
		}

		// TODO: make this a bit more helpful
		if (!config.isValid()) {
			throw new IOException("invalid configuration supplied");
		}

		srbConfig = config;

		if (srbConfig.usingX509) {
			GSSCredential cert = null;

			try {
				GlobusCredential gCert = GlobusCredential.getDefaultCredential();
				// might be unnecessary
				cert = new GlobusGSSCredentialImpl(gCert,
					    GSSCredential.INITIATE_AND_ACCEPT);
				log.info("obtained credential: " + cert + " : " +
				    cert.getRemainingLifetime());
			}
			catch (GlobusCredentialException e) {
				log.error("error obtaining globus credential: " + e);
				throw new IOException(e.getMessage());
			}
			catch (GSSException e) {
				log.error("error obtaining globus credential: " + e);
				throw new IOException(e.getMessage());
			}

			srbAccount = new SRBAccount(srbConfig.srbHost, srbConfig.srbPort,
				    cert);
			srbAccount.setDefaultStorageResource(srbConfig.srbDefaultResource);
		}
		else {
			srbAccount = new SRBAccount(srbConfig.srbHost, srbConfig.srbPort,
				    srbConfig.srbUsername, srbConfig.srbPassword,
				    srbConfig.getSrbHomeCollection(), srbConfig.srbMDASDomain,
				    srbConfig.srbDefaultResource);
		}
	}


	/*
	        public SRBHelper setConfig(SRBConfig config)
	                throws IOException {
	                if (config == null) {
	                        throw new IOException("Null configuration supplied");
	                }

	                // TODO: make this a bit more helpful
	                if (!config.isValid()) {
	                        throw new IOException("invalid configuration supplied");
	                }

	                srbConfig = config;
	                srbAccount = new SRBAccount(srbConfig.srbHost, srbConfig.srbPort,
	                            srbConfig.srbUsername, srbConfig.srbPassword,
	                            srbConfig.srbHomeCollection, srbConfig.srbMDASDomainHome,
	                            srbConfig.srbDefaultResource);

	                // this is too eager
	//                srbFS = getSRBFileSystem(srbAccount);
	                return this;
	        }
	*/

	// not sure of the validity of the design, but it helps testing a little
	protected synchronized SRBFileSystem getSRBFileSystem()
	        throws IOException {
		// lazy connection to srb
		if (srbFS == null) {
			log.info("connecting to SRB ...");
			srbFS = new SRBFileSystem(srbAccount);
			log.info("connected to SRB");
		}

		return srbFS;
	}


	public boolean ready() {
		return srbFS != null;
	}


	public SRBFile getSRBFile(String path)
	        throws IOException {
		SRBFileSystem fs = getSRBFileSystem();
		String basePath = path.startsWith(fs.getHomeDirectory()) ? ""
			                                                     : (fs.getHomeDirectory() +
			"/");

		return new SRBFile(fs, basePath + path);
	}


	// again to facilitate testing
	public SRBRandomAccessFile getSRBRandomAccessFile(SRBFile target,
	    String mode)
	        throws IOException {
		return new SRBRandomAccessFile(target, mode);
	}
}
