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

import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.xmlrpc.XmlRpcException;

/** 
 * HPALMMavenPlugin Data Access Object interface.
 * An implementation of this interface should provide API that allows to execute remote commands on Confluence and HP ALM servers. 
 *
 * @author Maciej SIDOR
 *
 */
public interface HPALMMojoDAOI
{

	/**
	 * Update Confluence page with given content
	 * @param cookie Confluence session authentication object
	 * @param page Confluence page content
	 * @param pageUpdateOptions Update options
	 * @throws XmlRpcException
	 */
	public abstract void updateConfluencePage(Object cookie, HashMap<Object, Object> page, HashMap<Object, Object> pageUpdateOptions) throws XmlRpcException;

	/**
	 * Retrieves Confluence page content
	 * @param cookie Confluence session authentication object
	 * @param confluencePageID CID of Confluence page to retrieve
	 * @return Confluence page content
	 * @throws XmlRpcException
	 */
	public abstract HashMap<Object, Object> getConfluencePage(Object cookie, String confluencePageID) throws XmlRpcException;

	/**
	 * Authenticates to Confluence
	 * @param confleunceUser Confluence user
	 * @param confluencePassword Confluence password
	 * @return Confluence session authentication object
	 * @throws XmlRpcException
	 */
	public abstract Object authenticateToConfluence(String confleunceUser, String confluencePassword) throws XmlRpcException;

	/**
	 * Initialize XML RPC Client
	 * @param confluenceServer Confluence address
	 * @throws MalformedURLException
	 */
	public abstract void initializeXmlRpcClient(String confluenceServer) throws MalformedURLException;

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
	public abstract String putToHPALM(String targetURL, String urlParameters, String cookie) throws MojoFailureException;

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
	public abstract String getFromHPALM(String targetURL, String cookie) throws MojoFailureException;

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
	public abstract String authenticateToHPALM(String targetURL, String username, String password) throws MojoFailureException;

}