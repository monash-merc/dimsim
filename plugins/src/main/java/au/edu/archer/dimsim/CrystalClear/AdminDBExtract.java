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

package au.edu.archer.dimsim.CrystalClear;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import noNamespace.LUsersDocument.LUsers;
import noNamespace.ODIRECTORIESDocument.ODIRECTORIES;
import noNamespace.OUSERSDocument.OUSERS;
import noNamespace.SPathDocument.SPath;
import noNamespace.SUserPasswordDocument.SUserPassword;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import au.edu.archer.dimsim.plugins.IMultiUserStore;

import com.rigakumsc.hobbit.OADMINDBDocument;



/**
 * @author MRafi
 *
 */
public class AdminDBExtract implements IMultiUserStore {	 
	
	private static Logger log = Logger.getLogger(AdminDBExtract.class);	
	
	public enum SIMPLE_HO_TYPE {		
		//To understand java ENUMs read http://www.ajaxonomy.com/2007/java/making-the-most-of-java-50-enum-tricks

		STRING("HO_S_REF") {String getElementValue (String xmlContent) {return xmlContent.replaceAll("<[^>]*>", "");}},
		INT("HO_INT")   {String getElementValue(String xmlContent) {return xmlContent.replaceAll("<[^>]*>", "");}},
		OTHER("HO_?")    {String getElementValue(String xmlContent) {return "";}};
		
		private String code;
		SIMPLE_HO_TYPE(String s) {
			this.code = s;
		}
		 
		public String getCode() { return code; }

		public static SIMPLE_HO_TYPE get(String code) { 
			 if (code.equals(STRING.code)) return STRING;
			 else if (code.equals(INT.code)) return INT;
			 return OTHER;
		 }
		 abstract String getElementValue(String xmlContent);
	};
	
	public enum dirTag {
		ScriptDir("ScriptDir"),
		UserDir("User Data Directory"),
		Other("");
		
		private String name;
		
		dirTag(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public static boolean isScriptDir(String code) { 
			 if (code.equals(ScriptDir.name)) return true;			 
			 return false;
		 }
	};
	
	protected OADMINDBDocument adminDBDoc;
	
	public AdminDBExtract(File adminDBXMLFile) throws XmlException, IOException {
		this(new BufferedReader(new FileReader(adminDBXMLFile)));
	}
	
	public AdminDBExtract(InputStream adminDBXMLStream) throws XmlException, IOException {
		this(new InputStreamReader(adminDBXMLStream));		
	}
	
	public AdminDBExtract(Reader adminDBReader) throws XmlException, IOException {		 
		adminDBDoc = OADMINDBDocument.Factory.parse(adminDBReader);			
	}
	
		
	protected LUsers getLUsers() {		
		return adminDBDoc.getOADMINDB().getLUsers();
	}
	
	public Set<String> getUsers() {
		Set<String> users = new HashSet<String> ();

		for(OUSERS u:getLUsers().getOUSERSArray()) {
			users.add(u.getName());
			//log.debug("Added user : " + u.getName());
		}
		
		return users;
	}
	
	public Map<String, String> getUserCredentials()
	{
		HashMap<String,String> userCreds = new HashMap<String, String > ();
		
		for(OUSERS u:getLUsers().getOUSERSArray()) {
			String user = u.getName();
			SUserPassword passwd = u.getSUserPassword();
			userCreds.put(user, SIMPLE_HO_TYPE.get(passwd.getHoType()).getElementValue(passwd.xmlText()));
		}
		return userCreds;
	}
	
	public Map<String, String> getUserScriptDirs()
	{
		HashMap<String,String> scriptDirs = new HashMap<String, String > ();
		
		for(OUSERS u:getLUsers().getOUSERSArray()) {
			String user = u.getName();			
			for (ODIRECTORIES d :u.getLDirectories().getODIRECTORIESArray()) {				
				SPath spath = d.getSPath();
				if (dirTag.isScriptDir(d.getName().getStringValue())) {
					scriptDirs.put(user, SIMPLE_HO_TYPE.get(spath.getHoType()).getElementValue(spath.xmlText()));
				}
			}
		}
		return scriptDirs;
	}	

	public Map<String, Object> getUserParams(String userId) {
		
		Map<String, Object> userParams = new HashMap<String, Object>();

		Map<String,String> scriptDirs = getUserScriptDirs();
		userParams.put(AdminDBExtract.dirTag.ScriptDir.getName(), scriptDirs.get(userId));		

		return userParams;
	}

	public boolean validate(String userId, Object credential) {
		
		Map<String,String> userCreds = getUserCredentials();
		if (userCreds != null) {
			return userCreds.get(userId).equals(credential);
		}
		
		return false;
	}

}
