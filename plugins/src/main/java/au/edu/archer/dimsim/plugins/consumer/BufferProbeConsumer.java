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
package au.edu.archer.dimsim.plugins.consumer;

import org.instrumentmiddleware.cima.core.ICIMADirector;
import org.instrumentmiddleware.cima.parcel.ParcelDocument.Parcel;
import org.instrumentmiddleware.cima.plugin.PluginException;
import org.instrumentmiddleware.cima.plugin.producer.IParcelCreator;
import org.instrumentmiddleware.cima.plugin.producer.poller.RegularIntervalSinglePollerParcelProducer;
import org.w3c.dom.Node;

import au.edu.archer.dimsim.buffer.plugin.IBufferNot;


public class BufferProbeConsumer extends
		RegularIntervalSinglePollerParcelProducer implements IBufferNot {

	public BufferProbeConsumer(String id, long dataInterval, ICIMADirector director
			, IParcelCreator creator)
			throws Exception {
		super(id, creator, director, dataInterval, "single");
		// TODO Auto-generated constructor stub
	}
	
	/*public BufferProbeConsumer(String id, IPlugin probePlugin,
				long dataInterval, ICIMADirector director)
			throws Exception {
	 
		super(id, new BufferProbeParcelCreator(), director, dataInterval, "single");
		// TODO Auto-generated constructor stub
	
	}*/	
	

	public Parcel[] produceParcels() throws PluginException {
		
		return new Parcel[] {this.parcelCreator.createParcel(null)} ;
		
	}

	public Node getInformation() {
		// TODO Auto-generated method stub
		return null;
	}

}
