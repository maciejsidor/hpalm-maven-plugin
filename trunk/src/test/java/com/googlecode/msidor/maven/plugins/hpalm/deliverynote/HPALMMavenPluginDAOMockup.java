package com.googlecode.msidor.maven.plugins.hpalm.deliverynote;

import java.net.MalformedURLException;
import java.util.HashMap;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.xmlrpc.XmlRpcException;

public class HPALMMavenPluginDAOMockup implements HPALMMavenPluginDAOI
{

	@Override
	public void updateConfluencePage(Object cookie, HashMap<Object, Object> page, HashMap<Object, Object> pageUpdateOptions) throws XmlRpcException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public HashMap<Object, Object> getConfluencePage(Object cookie, String confluencePageID) throws XmlRpcException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object authenticateToConfluence(String confleunceUser, String confluencePassword) throws XmlRpcException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initializeXmlRpcClient(String confluenceServer) throws MalformedURLException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String putToHPALM(String targetURL, String urlParameters, String cookie) throws MojoFailureException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFromHPALM(String targetURL, String cookie) throws MojoFailureException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String authenticateToHPALM(String targetURL, String username, String password) throws MojoFailureException
	{
		// TODO Auto-generated method stub
		return null;
	}

}
