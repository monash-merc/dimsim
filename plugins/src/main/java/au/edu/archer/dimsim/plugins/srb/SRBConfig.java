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

import edu.sdsc.grid.io.srb.SRBFile;


public class SRBConfig {
	public String srbHost = null;
	public int srbPort = 5544;
	public String srbUsername = null;
	public String srbPassword = null;
	public String srbMDASDomain = null;
	public String srbMDASZone = null;
	public String srbDefaultResource = null;
	public boolean usingX509 = false;

	public boolean isValid() {
		return (srbHost != null) && (srbPort != -1) && (srbPassword != null) &&
		(srbMDASZone != null) && (srbMDASDomain != null) &&
		(srbDefaultResource != null) &&
		((srbUsername != null) || (usingX509 == true));
	}


	public String getSrbHost() {
		return srbHost;
	}


	public void setSrbHost(String srbHost) {
		this.srbHost = srbHost;
	}


	public int getSrbPort() {
		return srbPort;
	}


	public void setSrbPort(int srbPort) {
		this.srbPort = srbPort;
	}


	public String getSrbUsername() {
		return srbUsername;
	}


	public void setSrbUsername(String srbUsername) {
		this.srbUsername = srbUsername;
	}


	public String getSrbPassword() {
		return srbPassword;
	}


	public void setSrbPassword(String srbPassword) {
		this.srbPassword = srbPassword;
	}


	public String getSrbHomeCollection() {
		return SRBFile.separator + srbMDASZone + SRBFile.separator + "home" + SRBFile.separator +
		srbUsername + "." + srbMDASDomain;
	}


	public String getSrbDefaultResource() {
		return srbDefaultResource;
	}


	public void setSrbDefaultResource(String srbDefaultResource) {
		this.srbDefaultResource = srbDefaultResource;
	}


	public boolean isUsingX509() {
		return usingX509;
	}


	public void setUsingX509(boolean usingX509) {
		this.usingX509 = usingX509;
	}


	public String getSrbMDASDomain() {
		return srbMDASDomain;
	}


	public void setSrbMDASDomain(String srbMDASDomain) {
		this.srbMDASDomain = srbMDASDomain;
	}


	public String getSrbMDASZone() {
		return srbMDASZone;
	}


	public void setSrbMDASZone(String srbMDASZone) {
		this.srbMDASZone = srbMDASZone;
	}
}
