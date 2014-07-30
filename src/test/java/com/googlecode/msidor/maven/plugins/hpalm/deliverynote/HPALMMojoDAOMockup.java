/*
sCopyright 2014 Maciej SIDOR [maciejsidor@gmail.com]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.	
 */
package com.googlecode.msidor.maven.plugins.hpalm.deliverynote;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.plexus.logging.Logger;

/**
 * 
 * Implementation of HPALMMavenPluginDAOI interface that allows to tests the HPALMMavenPlugin MOJO.
 * 
 * @author Maciej SIDOR
 *
 */
public class HPALMMojoDAOMockup implements HPALMMojoDAOI
{
	
	private Hashtable<String, String> 					fromHPALMResultFiles 	= null;
	private Hashtable<String, HashMap<Object, Object>> 	confluencePages 		= null;
	private HashMap<Object, Object> 					updatedConfluencePage	= null;	
	private String 										confleunceUser 			= null;
	private String 										confluencePassword 		= null;
	private String 										username 				= null;
	private String 										password 				= null;	
	private Logger										logger 					= null;

	@Override
	
	/**
	 * Update Confluence page with given content.
	 * This implmentation sets updatedConfluencePage with given page.
	 * @param cookie Confluence session authentication object
	 * @param page Confluence page content
	 * @param pageUpdateOptions Update options
	 * @throws XmlRpcException
	 */	
	public void updateConfluencePage(Object cookie, HashMap<Object, Object> page, HashMap<Object, Object> pageUpdateOptions) throws XmlRpcException
	{
		if(logger!=null) 
			logger.info("HPALMMojoDAOMockup.updateConfluencePage: "+cookie+page+pageUpdateOptions);

		updatedConfluencePage = page;
	}

	@Override
	/**
	 * Retrieves Confluence page content.
	 * This iplmeentation returns confluence page that is set in confluencePages field under cookie+confluencePageID key.
	 * @param cookie Confluence session authentication object
	 * @param confluencePageID CID of Confluence page to retrieve
	 * @return Confluence page content
	 * @throws XmlRpcException
	 */	
	public HashMap<Object, Object> getConfluencePage(Object cookie, String confluencePageID) throws XmlRpcException
	{	
		if(logger!=null) 
			logger.info("HPALMMojoDAOMockup.getConfluencePage: "+cookie+confluencePageID);
		
		if(confluencePages!=null && confluencePages.containsKey(cookie+confluencePageID))
		{
			return confluencePages.get(cookie+confluencePageID);
		}		
		
		return null; 
	}

	@Override
	/**
	 * Authenticates to Confluence.
	 * This implementation sets confleunceUser and confluencePassword fields.
	 * @param confleunceUser Confluence user
	 * @param confluencePassword Confluence password
	 * @return Confluence session authentication object
	 * @throws XmlRpcException
	 */	
	public Object authenticateToConfluence(String confleunceUser, String confluencePassword) throws XmlRpcException
	{
		this.confleunceUser=confleunceUser;
		this.confluencePassword=confluencePassword;
		
		return "479a88d7bb";
	}

	@Override
	/**
	 * Initialize XML RPC Client.
	 * This implementation does nothing.
	 * @param confluenceServer Confluence address
	 * @throws MalformedURLException
	 */	
	public void initializeXmlRpcClient(String confluenceServer) throws MalformedURLException
	{
		// nothing to do here
	}

	@Override
	/**
	 * Send content to target URL via PUT method.
	 * This implementation practically does nothing.
	 * @param targetURL
	 *            - target to send the content to
	 * @param urlParameters
	 *            - the content to be sent
	 * @param cookie
	 *            - cookie to be set in Cookie header file
	 * @return response from the target URL
	 * @throws MojoFailureException
	 *             if server responded with code different than 200
	 */	
	public String putToHPALM(String targetURL, String urlParameters, String cookie) throws MojoFailureException
	{
		if(logger!=null) 
			logger.info("HPALMMojoDAOMockup.putToHPALM: "+targetURL+urlParameters+cookie);
		
		return "content";
	}

	@Override
	/**
	 * Get the content from target URL via GET method.
	 * This implementation returns the content of XML file that was registered in fromHPALMResultFiles filed under targetURL+cookie key.  
	 * @param targetURL
	 *            - target to get the content from
	 * @param cookie
	 *            - cookie to be set in Cookie header file
	 * @return response from the target URL
	 * @throws MojoFailureException
	 *             if server responded with code different than 200
	 */	
	public String getFromHPALM(String targetURL, String cookie) throws MojoFailureException
	{
		if(logger!=null) 
			logger.info("HPALMMojoDAOMockup.getFromHPALM: "+targetURL+cookie);
		
		if(fromHPALMResultFiles!=null && fromHPALMResultFiles.containsKey(targetURL+cookie))
		{
			return readFromFile(fromHPALMResultFiles.get(targetURL+cookie));
		}
		
		return null;
	}

	@Override
	/**
	 * Authenticate to HP ALM.
	 * This implementation sets username and password fields.
	 * 
	 * @param targetURL
	 *            - target to authenticate to
	 * @param username
	 *            - HP ALM user
	 * @param password
	 *            - HP ALM password
	 * @return authentication cookie
	 * @throws MojoFailureException
	 *             if server responded with code different than 200
	 */	
	public String authenticateToHPALM(String targetURL, String username, String password) throws MojoFailureException
	{
		this.username=username;
		this.password=password;
		return "LWSSO_COOKIE_KEY=yv5W1bsYKsX-zzlWWq4jdL9qICd-6wYEeq69dwww67lfg1tLMVFXPdXDOZgbUWTUNUKRVPkdlz2Fjfo58B62Mp-dDLpfCPETEXWNFx4wT5Ic1lKtz8YSgz8C3DujgbsbETjVBe_ryFZHxFqmJev2w0mqmo2LIR8aTw9f4tQ2BJjY1xkNLQDt4p8_4g_25OP9wQZ-emAvkBc1lAjuXsXOZpNw7jXRvnaSjkaXt0uIcoU.; Path=/";
	}
	
	/**
	 * Reads the content of the file indicated by filePath
	 * @param filePath
	 * @return file content
	 */
	private String readFromFile(String filePath)
	{
		StringBuilder sb = new StringBuilder();
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line=reader.readLine())!=null)
			{
				sb.append(line);
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
		return sb.toString();
		
	}
	
	/**
	 * Add fake HP ALM result to fromHPALMResultFiles filed
	 * @param key
	 * @param filePath
	 */
	public void addToFromHPALMResultFiles(String key, String filePath)
	{
		if(fromHPALMResultFiles==null)
		{
			fromHPALMResultFiles = new Hashtable<String, String>();
		}
		
		fromHPALMResultFiles.put(key, filePath);
	}
	
	/**
	 * Add fake confluence page
	 * @param key
	 * @param content
	 */
	public void addConfluencePage(String key, HashMap<Object, Object> content)
	{
		if(confluencePages==null)
		{
			confluencePages = new Hashtable<String, HashMap<Object, Object>>();
		}
		
		confluencePages.put(key, content);
		
	}

	/*-------------------------Getters and setters------------------------------*/
	
	public String getConfluencePassword()
	{
		return confluencePassword;
	}

	public void setConfluencePassword(String confluencePassword)
	{
		this.confluencePassword = confluencePassword;
	}

	public String getConfleunceUser()
	{
		return confleunceUser;
	}

	public void setConfleunceUser(String confleunceUser)
	{
		this.confleunceUser = confleunceUser;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}	
	
	public Logger getLogger()
	{
		return logger;
	}

	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	public HashMap<Object, Object> getUpdatedConfluencePage()
	{
		return updatedConfluencePage;
	}

	public void setUpdatedConfluencePage(HashMap<Object, Object> updatedConfluencePage)
	{
		this.updatedConfluencePage = updatedConfluencePage;
	}
	

}
