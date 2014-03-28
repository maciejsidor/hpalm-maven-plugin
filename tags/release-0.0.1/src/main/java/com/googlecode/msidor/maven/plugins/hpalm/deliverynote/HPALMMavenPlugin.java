package com.googlecode.msidor.maven.plugins.hpalm.deliverynote;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import sun.misc.BASE64Encoder;


/**
 * 		Useful HP ALM REST API :
 * 
 *      
 * 
 * 		to see all the API
 *		qcbin/rest/api-descriptor?doc=resources&login-form-required=y
 *
 *		to see all the lists
 *		qcbin/rest/domains/RES_AND_REV/projects/TD_RESA/customization/lists?id=35
 *		
 *		paging
 *		qcbin/rest/domains/RES_AND_REV/projects/TD_RESA/defects?page-size=10&start-index=30
 *		
 *		order by
 *		qcbin/rest/domains/RES_AND_REV/projects/TD_RESA/defects?order-by={status[ASC];name[DESC]}
 *		
 *		filters
 *		qcbin/rest/domains/RES_AND_REV/projects/TD_RESA/defects?query={id[>1 AND NOT 5]; status[Ready or Design]}
 *
 *
 * @goal generate-change
 * */
@SuppressWarnings("restriction")
public class HPALMMavenPlugin extends AbstractMojo
{
	private String url;
	private String login;
	private String password;
	private String query;
	private Map<String,String> valuesToUpdate;
	private Map<String,String> queryValues;
	private Map<String,String> translationOfValuesToExport;
	private List<String> valuesToExport;
    private String domain;
    private String project;
    private String confluencePassword;
    private String confluenceServer;
    private String confleunceUser;
    private Object confluencePageID;
    private String confluenceKeyWordForUpdate;
    private String updateHeader;
	
	

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException 
	{

	    /***********************************************************
	     * Authenticate to HP ALM
	     ***********************************************************/
	    getLog().info( "Authenticate to HP ALM" );
		String cokie = authenticate(url+"/qcbin/authentication-point/authenticate",login,password);
		
        /***********************************************************
         * Prepare HP ALM query statement
         ***********************************************************/		
		getLog().info( "Prepare HP ALM query statement" );
		if(query==null)
		{
			query="";
			Set<String> keys = queryValues.keySet();
			boolean isFirstItem = true;
			for (String key : keys) 
			{
				String value = queryValues.get(key);
				
				try 
				{
				    //the + signs are not parsed as the spaces thus additional conversion to %20 is needed
					key = URLEncoder.encode(key, "UTF-8").replace("+", "%20");
					value = URLEncoder.encode(value, "UTF-8").replace("+", "%20");
				} 
				catch (UnsupportedEncodingException e) 
				{				
					e.printStackTrace();
				}
				
				query += (isFirstItem?"":";") + key+"[%22"+value+"%22]";
				isFirstItem = false;
			}
		}
		
		getLog().debug("Final HP ALM query: "+query);
		
		
        /***********************************************************
         * Retrieve entities
         ***********************************************************/   	
		getLog().info( "Execute HP ALM query" );
		boolean hasMoreentities = true;
		int startIndex=1;
		List<Entity> entities = new ArrayList<Entity>();
				
		while(hasMoreentities)
		{
			String res = get(url+"/qcbin/rest/domains/"+domain+"/projects/"+project+"/defects?page-size=10&start-index="+startIndex+"&query={"+query+"}",cokie);

			List<Entity> entitiesToAdd =parse(res);  
			
			if(entitiesToAdd!=null)
				entities.addAll(entitiesToAdd);

			startIndex+=10;
			hasMoreentities=(entities!=null && entities.size()==10);				
		}

		
        /***********************************************************
         * Generate summary HTML table report for CONLUENCE 
         ***********************************************************/          
		getLog().info( "Generate summary HTML table report for CONLUENCE" );
		StringBuilder html = new StringBuilder();
		html.append("<table><tbody>");

		//--[get headers row]--
		html.append("<tr>");

        //for each columns defined 
        for (String key : valuesToExport) 
        {           
            String value = null;
            
            //check if translation is defined
            if(translationOfValuesToExport!=null && translationOfValuesToExport.containsKey(key))
                value = translationOfValuesToExport.get(key);
            else
                value = key;
            
            //for null values use NBSP
            if(value==null || value.trim().isEmpty())
                value="&nbsp;";
            
            html.append("<th>"+value+"</th>");
        }
        html.append("</tr>");		
		
        //--[get entity row]--
		for (Entity entity : entities) 
		{
		   getHTMLForEntity(entity,html);			
		}
		html.append("</tbody></table>");
		getLog().debug("Generated HTML code: "+html);
				
        /***********************************************************
         * Update CONLUENCE 
         ***********************************************************/   
		getLog().info( "Update CONLUENCE" );
		try
        {
            updateConfluencePage(html.toString());
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
		
        /***********************************************************
         * Generate changes file
         ***********************************************************/ 	
		getLog().info( "Generate changes file" );
		
		//TODO generate change file
		
        /***********************************************************
         * Update HP ALM entities
         ***********************************************************/  
		getLog().info( "Update HP ALM entities" );
		for (Entity entity : entities) 
		{
			String xml = getUpdateStatement(entity);
			getLog().debug("Update statement: "+xml);
			put(url+"/qcbin/rest/domains/"+domain+"/projects/"+project+"/defects/"+entity.id,xml,cokie); 			
		}
	}
	
    @SuppressWarnings("unchecked")
    /**
     * Update confluence page with given content and defined header under the given keyword (or at the top of page in none given)
     * @param contentToAdd - content to put to confluence page
     * @throws Exception if error occurred during the confluence page update
     */
    private void updateConfluencePage(String contentToAdd) throws Exception
    {
       //get the connection string
       XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
       config.setServerURL(new URL(confluenceServer +"/rpc/xmlrpc"));
       XmlRpcClient client = new XmlRpcClient();
       client.setConfig(config);      
       
       //authenticate with user and password
       Object result = client.execute("confluence2.login",new String[]{confleunceUser,confluencePassword} );
       
       //get the confluence page
       HashMap<Object,Object> page = (HashMap<Object,Object>)client.execute("confluence2.getPage",new Object[]{result,confluencePageID} );
       getLog().debug("Current CONFLUENCE page: "+ page);
       
       //try to find the keyword under which the content will be put 
       String content = (String)page.get("content");
       int i = content.indexOf(confluenceKeyWordForUpdate);
       i+=confluenceKeyWordForUpdate.length();           
       
       //if keyword was found
       if(i>=0)
       {
           content = content.substring(0, i)+updateHeader+contentToAdd+content.substring(i);
       }
       else
       {
           content = updateHeader+contentToAdd+content;                                      
       }
    
       
       getLog().debug("and the new content: "+content);
       
       //update confluence page
       page.put("content",content);       
       HashMap<Object,Object> pageUpdateOptions  = new HashMap<Object,Object>();
       page = (HashMap<Object,Object>)client.execute("confluence2.updatePage",new Object[]{result,page,pageUpdateOptions} );
    }
	

    /**
     * Wraps HP ALM entity with HTML table row tags
     * @param entity - the entity to be transformed to HTML table row
     * @param html - the String Builder instance to append the content to
     */	
	private void getHTMLForEntity(Entity entity,StringBuilder html) 
	{
	    html.append("<tr>");
		//for each column configured
		for (String key : valuesToExport) 
		{
		    //get the value from entity for corresponding column
			String value = "&nbsp;";
			if(entity.fields!=null)
			{
				List<Field> fields = entity.fields;
				for (Field field : fields) 
					if(key.equalsIgnoreCase(field.name))
					{
						value = field.value;
					}
			}
			
			html.append( "<td>"+value+"</td>");
		}
		html.append( "</tr>");
	}

	/**
	 * Prepare update statement for entity
	 * @param entity - entity to update
	 * @return update statement for entity
	 */
	public String getUpdateStatement(Entity entity)	
	{	
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><Entity Type=\"defect\"><Fields>";		
		
		Set<String> keys = valuesToUpdate.keySet();
		for (String key : keys) 
		{
			String value = valuesToUpdate.get(key);
			xml += "<Field Name=\""+key+"\"><Value>"+value+"</Value></Field>";
		}
		xml += "</Fields></Entity>";
		
		
		return xml;
	}
	
	
	/**
	 * Transform the entities to object representation
	 * @param xmlBody - result of HP ALM query containing entity definitions to parse
	 * @return list of entities 
	 */
	public List<Entity> parse(String xmlBody)
	{
		JAXBContext context;
		try 
		{
		    //get the XML parser
			context = JAXBContext.newInstance(EntitiesRoot.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			//parse the XML body
			StringReader reader = new StringReader(xmlBody);			
			EntitiesRoot entitiesRoot = (EntitiesRoot) unmarshaller.unmarshal(reader);
								
			//update the ID filed of entity due to simplify the entities identification.
			if(entitiesRoot!=null)
			{
				if(entitiesRoot.entities!=null)
					for (Entity entity : entitiesRoot.entities) 
					{
						if(entity.fields!=null)
							for (Field field : entity.fields) 
							{
								if(field.name.equalsIgnoreCase("id"))
								{
									entity.id = field.value;
									break;
								}
							}	
					}
				
				return entitiesRoot.entities;
			}
						
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * Send content to target URL via PUT method
	 * @param targetURL - target to send the content to
	 * @param urlParameters - the content to be sent
	 * @param cookie - cookie to be set in Cookie header file
	 * @return response from the target URL
	 * @throws MojoFailureException if server responded with code different than 200
	 */
	public String put(String targetURL,String urlParameters, String cookie ) throws MojoFailureException
	{
		HttpURLConnection connection = null;
        MojoFailureException exceptionToThrow = null;
        String resultToReturn = null;
		
				
		try 
		{
			// Create connection
		    URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			
			//set the send method
			connection.setRequestMethod("PUT");
			
			//set the header parameters
			connection.setRequestProperty("Content-Type","application/xml");			
			connection.setRequestProperty("Content-Length","" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setRequestProperty("Accept","application/xml");
			
			//set the cookie if one has been given
			if(cookie!=null)
				connection.setRequestProperty("Cookie", cookie);
			
			//setup connection features
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
			
			//parse response
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
        //try to gently close the connection
        catch (Exception e) 
        {
            String detailInfo = null;
            
            //in case of exception try to obtain more information
            if(connection!=null)
            try
            {
                detailInfo = "Response code: "+ connection.getResponseCode();
                detailInfo += "; Response message: "+connection.getResponseMessage();               
            }
            catch(IOException eInternal){/*ignore*/}
            
            //prepare the exception to be thrown after the connection disposal
            exceptionToThrow = new MojoFailureException(e,"Error occurced while posting data to HP ALM","Error occurced while posting data to HP ALM: "+(detailInfo!=null?detailInfo:e.getMessage()));
        } 
        finally 
        {

            if (connection != null) 
            {
                connection.disconnect();
            }
        }
        
        if(exceptionToThrow!=null)
            throw exceptionToThrow;
        
        return resultToReturn;
	}		
	
    /**
     * Get the content from target URL via GET method
     * @param targetURL - target to get the content from
     * @param cookie - cookie to be set in Cookie header file
     * @return response from the target URL
     * @throws MojoFailureException if server responded with code different than 200
     */	
	public String get(String targetURL,String cookie) throws MojoFailureException
	{
		HttpURLConnection connection = null;
        MojoFailureException exceptionToThrow = null;
        String resultToReturn = null;
		
		
		try 
		{
			// Create connection
		    URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			
			//set the send method
			connection.setRequestMethod("GET");

            //set the header parameters
            connection.setRequestProperty("Accept","application/xml");			
			
			//set the cookie if one has been given
			if(cookie!=null)
				connection.setRequestProperty("Cookie", cookie);		
			
			//set connection features 
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Get Response
			StringBuffer response = new StringBuffer();
			InputStream is = connection.getInputStream();
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
		//try to gently close the connection
		catch (Exception e) 
		{
		    String detailInfo = null;
		    
		    //in case of exception try to obtain more information
		    if(connection!=null)
		    try
		    {
		        detailInfo = "Response code: "+ connection.getResponseCode();
		        detailInfo += "; Response message: "+connection.getResponseMessage();		        
		    }
		    catch(IOException eInternal){/*ignore*/}
		    
		    //prepare the exception to be thrown after the connection disposal
		    exceptionToThrow = new MojoFailureException(e,"Error occurced while retrieving data from HP ALM","Error occurced while retrieving data from HP ALM: "+(detailInfo!=null?detailInfo:e.getMessage()));
		} 
		finally 
		{

			if (connection != null) 
			{
				connection.disconnect();
			}
		}
		
		if(exceptionToThrow!=null)
		    throw exceptionToThrow;
		
		return resultToReturn;
	}	

	/**
	 * Authenticate to HP ALM 
	 * @param targetURL - target to authenticate to
	 * @param username - HP ALM user
	 * @param password - HP ALM password
	 * @return authentication cookie 
	 * @throws MojoFailureException if server responded with code different than 200
	 */
	public String authenticate(String targetURL,String username, String password ) throws MojoFailureException 
	{
		HttpURLConnection connection = null;
		MojoFailureException exceptionToThrow = null;
		String resultToReturn = null;

		try 
		{
			// Create connection
		    URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			//prepare the authorization statement
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

	        //set the connection features
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Get Response
			connection.getResponseCode();
            resultToReturn = connection.getHeaderField("Set-Cookie");

        } 
        //try to gently close the connection
        catch (Exception e) 
        {
            String detailInfo = null;
            
            //in case of exception try to obtain more information
            if(connection!=null)
            try
            {
                detailInfo = "Response code: "+ connection.getResponseCode();
                detailInfo += "; Response message: "+connection.getResponseMessage();               
            }
            catch(IOException eInternal){/*ignore*/}
            
            //prepare the exception to be thrown after the connection disposal
            exceptionToThrow = new MojoFailureException(e,"Error occurced while authenticating to HP ALM","Error occurced while retrieving authenticating to HP ALM: "+(detailInfo!=null?detailInfo:e.getMessage()));
        } 
        finally 
        {

            if (connection != null) 
            {
                connection.disconnect();
            }
        }
        
        if(exceptionToThrow!=null)
            throw exceptionToThrow;
        
        return resultToReturn;	
	}




}
