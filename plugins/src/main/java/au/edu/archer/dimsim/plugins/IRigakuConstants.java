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

public interface IRigakuConstants {
	public enum rigakuFileTypes {
		OSC("osc"),
		IMG("img"),
		UNKNOWN("unknown");
		
		private String name;
		
		rigakuFileTypes(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}		
	};
	
	public enum imageFileTypes {
		JPEG("jpg"),
		PNG("png"),
		UNKNOWN("unknown");
		
		private String name;
		
		imageFileTypes(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}	
		
		public String getFileExtension() {
			return "." + getName();
		}
	};
	
	 public static String FILE_URL_PROTOCOL = "file://";
	
	 public static String PARCEL_TYPE_START_EXPERIMENT = "START_EXPERIMENT";
	 public static String PARCEL_TYPE_END_EXPERIMENT = "END_EXPERIMENT";
	 public static String EXPERIMENT_PARCEL_TYPE = "EXPERIMENT_PARCEL_TYPE";
	    
	 public static String RIGAKU_SAMPLENAME = "SampleName";
	 public static String RIGAKU_PROJECTNAME = "ProjectName";
	 public static String RIGAKU_TASKNAME = "TaskName";
	 public static String RIGAKU_IMAGEDIRECTORY = "ImageDirectory";
	 
	 public static int maxEncodeSizeInKB = 90;
}