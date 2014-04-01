/*
Copyright 2014 Maciej SIDOR [maciejsidor@gmail.com]

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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import sun.misc.BASE64Encoder;

/**
 * Default implementation of HPALMMavenPluginDAOI. 
 * This Data Access Object provides API that allows to execute remote commands on Confluence and HP ALM servers. 
 * 
 * @author Maciej SIDOR
 *
 */
@SuppressWarnings("restriction")
public class DefaultHPALMMavenPluginDAO implements HPALMMavenPluginDAOI
{
	/**
	 * The RPC client for connecting to Confluence
	 */
	private XmlRpcClient client;

	/**
	 * Default contructor
	 */
	public DefaultHPALMMavenPluginDAO()
	{
	}
	
	/**
	 * Update Confluence page with given content
	 * @param cookie Confluence session authentication object
	 * @param page Confluence page content
	 * @param pageUpdateOptions Update options
	 * @throws XmlRpcException
	 */
	@Override
	public void updateConfluencePage(Object cookie, HashMap<Object, Object> page, HashMap<Object, Object> pageUpdateOptions) throws XmlRpcException
	{
		client.execute("confluence2.updatePage", new Object[] { cookie, page, pageUpdateOptions });
	}

	/**
	 * Retrieves Confluence page content
	 * @param cookie Confluence session authentication object
	 * @param confluencePageID CID of Confluence page to retrieve
	 * @return Confluence page content
	 * @throws XmlRpcException
	 */
	@Override
	public HashMap<Object, Object> getConfluencePage(Object cookie,String confluencePageID) throws XmlRpcException
	{
		@SuppressWarnings("unchecked")
		HashMap<Object, Object> page = (HashMap<Object, Object>) client.execute("confluence2.getPage", new Object[] { cookie, confluencePageID });
		return page;
	}

	/**
	 * Authenticates to Confluence
	 * @param confleunceUser Confluence user
	 * @param confluencePassword Confluence password
	 * @return Confluence session authentication object
	 * @throws XmlRpcException
	 */
	@Override
	public Object authenticateToConfluence(String confleunceUser, String confluencePassword) throws XmlRpcException
	{
		// authenticate with user and password
		Object result = client.execute("confluence2.login", new String[] { confleunceUser, confluencePassword });
		return result;
	}

	/**
	 * Initialize XML RPC Client
	 * @param confluenceServer Confluence address
	 * @throws MalformedURLException
	 */
	@Override
	public void initializeXmlRpcClient(String confluenceServer) throws MalformedURLException
	{
		// get the connection string
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(confluenceServer + "/rpc/xmlrpc"));
		client = new XmlRpcClient();
		client.setConfig(config);
	}	


	/**
	 * Send content to target URL via PUT method
	 * 
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
	@Override
	public String putToHPALM(String targetURL, String urlParameters, String cookie) throws MojoFailureException
	{
		HttpURLConnection connection = null;
		MojoFailureException exceptionToThrow = null;
		String resultToReturn = null;

		try
		{
			// Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();

			// set the send method
			connection.setRequestMethod("PUT");

			// set the header parameters
			connection.setRequestProperty("Content-Type", "application/xml");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setRequestProperty("Accept", "application/xml");

			// set the cookie if one has been given
			if (cookie != null) connection.setRequestProperty("Cookie", cookie);

			// setup connection features
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			// Get Response
			StringBuffer response = new StringBuffer();
			InputStream is = connection.getInputStream();

			// parse response
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();

			resultToReturn = response.toString();

		}
		// try to gently close the connection
		catch (Exception e)
		{
			String detailInfo = null;

			// in case of exception try to obtain more information
			if (connection != null) try
			{
				detailInfo = "Response code: " + connection.getResponseCode();
				detailInfo += "; Response message: " + connection.getResponseMessage();
			}
			catch (IOException eInternal)
			{/* ignore */
			}

			// prepare the exception to be thrown after the connection disposal
			exceptionToThrow = new MojoFailureException(e, "Error occurced while posting data to HP ALM", "Error occurced while posting data to HP ALM: " + (detailInfo != null ? detailInfo : e.getMessage()));
		}
		finally
		{

			if (connection != null)
			{
				connection.disconnect();
			}
		}

		if (exceptionToThrow != null) throw exceptionToThrow;

		return resultToReturn;
	}

	/**
	 * Get the content from target URL via GET method
	 * 
	 * @param targetURL
	 *            - target to get the content from
	 * @param cookie
	 *            - cookie to be set in Cookie header file
	 * @return response from the target URL
	 * @throws MojoFailureException
	 *             if server responded with code different than 200
	 */
	@Override
	public String getFromHPALM(String targetURL, String cookie) throws MojoFailureException
	{
		HttpURLConnection connection = null;
		MojoFailureException exceptionToThrow = null;
		String resultToReturn = null;

		try
		{
			// Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();

			// set the send method
			connection.setRequestMethod("GET");

			// set the header parameters
			connection.setRequestProperty("Accept", "application/xml");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

			// set the cookie if one has been given
			if (cookie != null) connection.setRequestProperty("Cookie", cookie);

			// set connection features
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Get Response
			StringBuffer response = new StringBuffer();
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,Charset.forName( "UTF-8" )));
			String line;
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();

			resultToReturn = response.toString();

		}
		// try to gently close the connection
		catch (Exception e)
		{
			String detailInfo = null;

			// in case of exception try to obtain more information
			if (connection != null) try
			{
				detailInfo = "Response code: " + connection.getResponseCode();
				detailInfo += "; Response message: " + connection.getResponseMessage();
			}
			catch (IOException eInternal)
			{/* ignore */
			}

			// prepare the exception to be thrown after the connection disposal
			exceptionToThrow = new MojoFailureException(e, "Error occurced while retrieving data from HP ALM", "Error occurced while retrieving data from HP ALM: " + (detailInfo != null ? detailInfo : e.getMessage()));
		}
		finally
		{

			if (connection != null)
			{
				connection.disconnect();
			}
		}

		if (exceptionToThrow != null) throw exceptionToThrow;

		return resultToReturn;
	}

	/**
	 * Authenticate to HP ALM
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
	@Override
	public String authenticateToHPALM(String targetURL, String username, String password) throws MojoFailureException
	{
		HttpURLConnection connection = null;
		MojoFailureException exceptionToThrow = null;
		String resultToReturn = null;

		try
		{
			// Create connection
			URL url = new URL(targetURL+"/qcbin/authentication-point/authenticate");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			// prepare the authorization statement
			String authorization = null;
			if (username != null && password != null)
			{
				authorization = username + ":" + password;
			}

			if (authorization != null)
			{
				String encodedBytes;
				BASE64Encoder enc = new sun.misc.BASE64Encoder();
				encodedBytes = enc.encode(authorization.getBytes());
				authorization = "Basic " + encodedBytes;
				connection.setRequestProperty("Authorization", authorization);
			}

			// set the connection features
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Get Response
			connection.getResponseCode();
			resultToReturn = connection.getHeaderField("Set-Cookie");

		}
		// try to gently close the connection
		catch (Exception e)
		{
			String detailInfo = null;

			// in case of exception try to obtain more information
			if (connection != null) try
			{
				detailInfo = "Response code: " + connection.getResponseCode();
				detailInfo += "; Response message: " + connection.getResponseMessage();
			}
			catch (IOException eInternal)
			{/* ignore */
			}

			// prepare the exception to be thrown after the connection disposal
			exceptionToThrow = new MojoFailureException(e, "Error occurced while authenticating to HP ALM", "Error occurced while retrieving authenticating to HP ALM: " + (detailInfo != null ? detailInfo : e.getMessage()));
		}
		finally
		{

			if (connection != null)
			{
				connection.disconnect();
			}
		}

		if (exceptionToThrow != null) throw exceptionToThrow;

		return resultToReturn;
	}	
}