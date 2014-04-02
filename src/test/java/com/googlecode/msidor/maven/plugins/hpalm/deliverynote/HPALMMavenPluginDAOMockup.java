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
		return null; //"{space=Doc, url=http://confluence.accor.net/display/Doc/FOLSReceiver+Release+Notes, version=5, creator=msidor, modified=Wed Apr 02 10:45:35 CEST 2014, id=25200500, content=<h2>1.1.0</h2><table><tbody><tr><th>ID</th><th>Name</th><th>Correcteur</th><th>Emmiteur</th><th>04</th><th>03</th><th>08</th><th>Cr&eacute;e</th><th>Detect&eacute;e en version</th><th>Gravit&eacute;</th></tr><tr><td>20574</td><td>Am&eacute;lioration de la gestion des codes d&rsquo;arr&ecirc;t UNIX</td><td>msidor</td><td>msidor</td><td>Evolution</td><td>Recette</td><td>3-Technique</td><td>2014-02-18</td><td>1.0.1</td><td>Bloquante</td></tr></tbody></table><p>&nbsp;</p><p>&nbsp;</p>, modifier=msidor, parentId=25200485, title=FOLSReceiver Release Notes, created=Mon Mar 31 17:32:46 CEST 2014, contentStatus=current, permissions=0, current=true, homePage=false}";
	}

	@Override
	public Object authenticateToConfluence(String confleunceUser, String confluencePassword) throws XmlRpcException
	{
		return "479a88d7bb";
	}

	@Override
	public void initializeXmlRpcClient(String confluenceServer) throws MalformedURLException
	{
		// nothing to do here
	}

	@Override
	public String putToHPALM(String targetURL, String urlParameters, String cookie) throws MojoFailureException
	{
		return "content";
	}

	@Override
	public String getFromHPALM(String targetURL, String cookie) throws MojoFailureException
	{
	    //<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Entities TotalResults="0"/>
	    /*
	     * <?xml version="1.0" encoding="UTF-8" standalone="yes"?><Entities TotalResults="1"><Entity Type="defect"><Fields><Field Name="has-change"><Value></Value></Field><Field Name="planned-closing-ver"><Value>1.1.0</Value></Field><Field Name="task-status"><Value></Value></Field><Field Name="reproducible"><Value>Y</Value></Field><Field Name="has-others-linkage"><Value>N</Value></Field><Field Name="description"><Value>&lt;html&gt;&#xD;
&lt;body&gt;&#xD;
&lt;div align=&quot;left&quot;&gt;&lt;font face=&quot;Arial Unicode MS&quot;&gt;&lt;span style=&quot;font-size:8pt&quot;&gt;Amélioration de la gestion &#xD;
des codes d’arrêt UNIX pour mieux gérer les demandes d’arrêt d’application.&lt;/span&gt;&lt;/font&gt;&lt;/div&gt;&#xD;
&lt;div align=&quot;left&quot;&gt;&amp;nbsp;&amp;nbsp;&lt;/div&gt;&#xD;
&lt;/body&gt;&#xD;
&lt;/html&gt;</Value></Field><Field Name="priority"><Value>1-Low</Value></Field><Field Name="run-reference"/><Field Name="alert-data"><Value></Value></Field><Field Name="dev-comments"><Value></Value></Field><Field Name="to-mail"><Value></Value></Field><Field Name="cycle-id"/><Field Name="status"><Value>Corrigée</Value></Field><Field Name="closing-date"/><Field Name="detected-in-rel"><Value></Value></Field><Field Name="bug-ver-stamp"><Value>3</Value></Field><Field Name="estimated-fix-time"/><Field Name="project"><Value>FOLS Receiver</Value></Field><Field Name="target-rel"><Value></Value></Field><Field Name="step-reference"/><Field Name="owner"><Value>msidor</Value></Field><Field Name="actual-fix-time"/><Field Name="request-type"><Value></Value></Field><Field Name="user-05"/><Field Name="user-04"><Value>Evolution</Value></Field><Field Name="user-03"><Value>Recette</Value></Field><Field Name="user-02"><Value>Recette</Value></Field><Field Name="user-01"><Value></Value></Field><Field Name="test-reference"/><Field Name="subject"/><Field Name="request-id"/><Field Name="request-server"><Value></Value></Field><Field Name="user-09"/><Field Name="user-08"><Value>3-Technique</Value></Field><Field Name="user-07"><Value></Value></Field><Field Name="user-06"><Value></Value></Field><Field Name="id"><Value>20574</Value></Field><Field Name="name"><Value>Amélioration de la gestion des codes d’arrêt UNIX</Value></Field><Field Name="has-linkage"><Value>N</Value></Field><Field Name="cycle-reference"><Value></Value></Field><Field Name="creation-time"><Value>2014-02-18</Value></Field><Field Name="request-note"><Value></Value></Field><Field Name="closing-version"><Value></Value></Field><Field Name="detection-version"><Value>1.0.1</Value></Field><Field Name="user-10"><Value>2014-04-02</Value></Field><Field Name="last-modified"><Value>2014-04-02 10:44:25</Value></Field><Field Name="user-12"><Value>2014-02-19</Value></Field><Field Name="user-11"/><Field Name="detected-in-rcyc"><Value></Value></Field><Field Name="severity"><Value>Bloquante</Value></Field><Field Name="attachment"><Value></Value></Field><Field Name="user-17"><Value>2014-02-24</Value></Field><Field Name="extended-reference"><Value></Value></Field><Field Name="detected-by"><Value>msidor</Value></Field><Field Name="target-rcyc"><Value></Value></Field></Fields></Entity></Entities>

	     * */
		return "content";
	}

	@Override
	public String authenticateToHPALM(String targetURL, String username, String password) throws MojoFailureException
	{
		return "LWSSO_COOKIE_KEY=yv5W1bsYKsX-zzlWWq4jdL9qICd-6wYEeq69dwww67lfg1tLMVFXPdXDOZgbUWTUNUKRVPkdlz2Fjfo58B62Mp-dDLpfCPETEXWNFx4wT5Ic1lKtz8YSgz8C3DujgbsbETjVBe_ryFZHxFqmJev2w0mqmo2LIR8aTw9f4tQ2BJjY1xkNLQDt4p8_4g_25OP9wQZ-emAvkBc1lAjuXsXOZpNw7jXRvnaSjkaXt0uIcoU.; Path=/";
	}

}
