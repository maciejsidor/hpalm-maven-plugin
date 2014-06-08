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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
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

/**
 * @author Maciej SIDOR
 * 
 *         Implementation of "generate-change" goal.
 * 
 *         <p>
 *         This goal generates change-log based on HP ALM issue management
 *         system. The issues to be included in change-log are queried based on
 *         the given HP ALM query statement (see ALM 11.0 REST API reference
 *         guide for more informations). Obtained issues can then be publish to
 *         CONFLUENCE page under the given keyword (or at the top of page in
 *         none given nor found). Additionally, one may select fields (and
 *         define their translations) that will be published in change-log
 *         report. There is also an option to update all queried defects with
 *         predefined values. The change-log output is compatible with maven
 *         announce plugin.
 *         </p>
 * 
 *         <p>
 *         For more informations about HP ALM see ALM 11.0 REST API reference
 *         guide at
 *         https://ovrd.external.hp.com/rd/sign-in?TYPE=33554433&REALMOID
 *         =06-000d
 *         bac2-dc02-1680-9aa0-a14d91440000&GUID=&SMAUTHREASON=0&METHOD=GET
 *         &SMAGENTNAME
 *         =$SM$iZH9ShXntQjxWQQV0Oqhh1Bk9OnxvC2w1G6RIFVaWUyeFBPUD93KOaOhpUkS
 *         %2fp7X&TARGET=$SM$http%3a%2f%2fsupport%
 *         2eopenview%2ehp%2ecom%2fselfsolve%2fdocument%2fKM997956%2fbinary%2fALM11%2e00_R
 *         E
 *         </p>
 * @goal generate-change
 * */
public class HPALMMavenPlugin extends AbstractMojo
{
	/**
	 * The HP ALM server address
	 * 
	 * @parameter
	 */
	private String url = null;

	/**
	 * The HP ALM user name
	 * 
	 * @parameter expression="${hpalm.user}"
	 */
	private String login = null;

	/**
	 * The HP ALM user password
	 * 
	 * @parameter  expression="${hpalm.password}"
	 */
	private String password = null;

	/**
	 * The HP ALM defects query statement
	 * 
	 * @parameter
	 */
	private String query = null;

	/**
	 * List of fields and corresponding values that will be used to update the
	 * queried defects
	 * 
	 * @parameter
	 */
	private Map<String, String> valuesToUpdate = null;

	/**
	 * Alternatively, if defects query statement is not set, the key/value pairs
	 * may be used to generate it automatically
	 * 
	 * @parameter
	 */
	private Map<String, String> queryValues = null;

	/**
	 * Translations for defect fields that will be published in change-log
	 * 
	 * @parameter
	 */
	private Map<String, String> translationOfValuesToExport = null;

	/**
	 * List of defect fields that will be published in change-log
	 * 
	 * @parameter
	 */
	private List<String> valuesToExport = null;

	/**
	 * The HP ALM domain
	 * 
	 * @parameter
	 */
	private String domain = null;

	/**
	 * The HP ALM project
	 * 
	 * @parameter
	 */
	private String project = null;

	/**
	 * The CONFLUENCE server address. REQUIRED
	 * 
	 * @parameter
	 */
	private String confluenceServer = null;

	/**
	 * The CONFLUENCE user. REQUIRED
	 * 
	 * @parameter expression="${hpalm.confluence.user}"
	 */
	private String confleunceUser = null;

	/**
	 * The CONFLUENCE password. REQUIRED
	 * 
	 * @parameter expression="${hpalm.confluence.password}"
	 */
	private String confluencePassword = null;

	/**
	 * The ID of CONFLUENCE page to update. REQUIRED
	 * 
	 * @parameter
	 */
	private String confluencePageID = null;

	/**
	 * The keyword on CONFLUENCE page under which the new content will be put.
	 * 
	 * @parameter
	 */
	private String confluenceKeyWordForUpdate = null;

	/**
	 * The header for updated content.
	 * 
	 * @parameter
	 */
	private String updateHeader = null;
	
    /**
     * Path that the changes fill will be created 
     * 
     * @parameter
     */
	private String              changesOutputFilePath          = null;	
	
    /**
     * Map of HP ALM fields and values that will be used to evaluate if a defect's type is "FIX" in chnages.xml.
     * It might happen that the same defect will evaluate correctly for each other filters.    
     * In such a case following order applies:
     * <ol>
     * <Li>FIX</Li>
     * <Li>ADD</Li>
     * <Li>UPDATE</Li>
     * <Li>REMOVE</Li>
     * </ol>
     * 
     * @parameter
     */
    private Map<String, String> changesFixIssuesFilter         = null;	
    
    /**
     * Map of HP ALM fields and values that will be used to evaluate if a defect's type is "ADD" in chnages.xml.
     * It might happen that the same defect will evaluate correctly for each other filters.    
     * In such a case following order applies:
     * <ol>
     * <Li>FIX</Li>
     * <Li>ADD</Li>
     * <Li>UPDATE</Li>
     * <Li>REMOVE</Li>
     * </ol>
     * 
     * @parameter
     */    
    private Map<String, String> changesAddIssuesFilter         = null;
    
    /**
     * Map of HP ALM fields and values that will be used to evaluate if a defect's type is "UPDATE" in chnages.xml.
     * It might happen that the same defect will evaluate correctly for each other filters.    
     * In such a case following order applies:
     * <ol>
     * <Li>FIX</Li>
     * <Li>ADD</Li>
     * <Li>UPDATE</Li>
     * <Li>REMOVE</Li>
     * </ol>
     * 
     * @parameter
     */    
    private Map<String, String> changesUpdateIssuesFilter      = null;

    /**
     * Map of HP ALM fields and values that will be used to evaluate if a defect's type is "REMOVE" in chnages.xml.
     * It might happen that the same defect will evaluate correctly for each other filters.    
     * In such a case following order applies:
     * <ol>
     * <Li>FIX</Li>
     * <Li>ADD</Li>
     * <Li>UPDATE</Li>
     * <Li>REMOVE</Li>
     * </ol>
     * 
     * @parameter
     */        
    private Map<String, String> changesRemoveIssuesFilter      = null;
    
    /**
     * HP ALM defect filed that will be mapped to "dev" attribute in chnages.xml  
     * 
     * @parameter
     */    
    private String              changesDevFiledMapping         = null;
    
    /**
     * HP ALM defect filed that will be mapped to issue description in chnages.xml
     * 
     * @parameter
     */    
    private String              changesDescFiledMapping        = null;
    
    /**
     * HP ALM defect filed that will be mapped to "DueTo" attribute in chnages.xml
     * 
     * @parameter
     */    
    private String              changesDueToFiledMapping       = null;

    
    /**
     * Release version that will be assigned to the set of issues in chnages.xml
     * 
     * @parameter expression="${project.version}"
     */    
	private String 				changesProjectVersion		  = null;

	
	/**
	 * Plugin Data Access Object
	 */
	private HPALMMavenPluginDAOI dao 						  = new DefaultHPALMMavenPluginDAO();

	/**
	 * <p>
	 * Main plugin method
	 * </p>
	 * */
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException
	{

		/***********************************************************
		 * Authenticate to HP ALM
		 ***********************************************************/
		getLog().info("Authenticating to HP ALM...");
		String cokie = null;
		try
		{
		    cokie = dao.authenticateToHPALM(url, login, password);
		}
		catch(Exception e)
		{
		    throw new MojoExecutionException( "Could not authenticate to HP ALM", e );
		}

		/***********************************************************
		 * Prepare HP ALM query statement
		 ***********************************************************/
		getLog().info("Preparing HP ALM query statement...");
		try
        {
            buildQueryStatement();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Could not produce HP ALM query", e );
        }
		
		
		
        /***********************************************************
		 * Retrieve entities
		 ***********************************************************/
		getLog().info("Executing HP ALM query...");
		List<Entity> entities = null;

		try
        {
            entities = retreiveEntities(cokie);
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Could not execute HP ALM query", e );
        }
		
		
		
        /***********************************************************
		 * Generate summary HTML table report for CONLUENCE
		 ***********************************************************/
		if(confluenceServer!=null)
		{
			getLog().info("Generating summary HTML table report for CONLUENCE...");
			StringBuilder html = null;
			try
	        {
	            html = generateHTML(entities);
	        }
	        catch ( Exception e )
	        {
	            throw new MojoExecutionException( "Could not Generate summary HTML table report for CONLUENCE", e );
	        }
		
		
        /***********************************************************
		 * Update CONLUENCE
		 ***********************************************************/

			getLog().info("Updating CONLUENCE page...");
			try
	        {
	            updateConfluencePage( html.toString() );
	        }
	        catch ( Exception e )
	        {
	            throw new MojoExecutionException( "Could not update CONLUENCE page", e );
	        }
		}
		
		
		
        /***********************************************************
		 * Generate changes file
		 ***********************************************************/
		if(changesOutputFilePath!=null)
		{
    		getLog().info("Generating changes file...");
            try
            {
                generateChangesXML(entities);
                
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Could not generate changes file", e );
            }
		}

		/***********************************************************
		 * Update HP ALM entities
		 ***********************************************************/
		if(valuesToUpdate!= null && valuesToUpdate.size()>0)
		{
    		getLog().info("Updating HP ALM entities...");
    		try
            {
                updateEntities(cokie, entities);
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Could not update HP ALM entities", e );
            }
		}
	}

	/**
	 * Updating given entities in HP ALM
	 * 
	 * @param cokie HP ALM session cookie
	 * @param entities List of entities to update
	 * @throws Exception
	 */
	private void updateEntities(String cokie, List<Entity> entities) throws Exception
	{
		for (Entity entity : entities)
		{
			String xml = getUpdateStatement(entity);
			getLog().debug("Update statement: " + xml);
			dao.putToHPALM(url + "/qcbin/rest/domains/" + domain + "/projects/" + project + "/defects/" + entity.id, xml, cokie);
		}
	}

	/**
	 * Generate changes XML
	 * 
	 * @param entities List of entities to update to export
	 * @throws Exception
	 */
	private void generateChangesXML(List<Entity> entities) throws Exception
	{
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream ( changesOutputFilePath ), Charset.forName( "UTF-8" )) );
		            
		writer.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
		writer.write( "<document>\n" );
		writer.write( "<body>\n" );
		writer.write( "<release version=\""+changesProjectVersion+"\" date=\""+Calendar.getInstance().getTime().toString()+"\">\n" );
		                
		//put all the issues
		for ( Entity entity : entities )
		{
		    writer.write( "<action dev=\""+entity.dev+"\" type=\""+entity.changeType+"\" issue=\""+entity.id+"\" due-to=\""+entity.dueTo+"\">\n" );
		    writer.write( entity.desc );
		    writer.write( "\n</action>\n" );                    
		}
		
		writer.write( "</release>\n" );
        writer.write( "</body>\n" );		
		writer.write( "</document>\n" );
		writer.flush();
		writer.close();
	}

	/**
	 * Generate HTML table
	 * 
	 * @param entities list of entities to export to HTML
	 * @return HTML content
	 */
	private StringBuilder generateHTML(List<Entity> entities) throws Exception
	{
		StringBuilder html;
		html = new StringBuilder();
		html.append( "<table><tbody>" );
		// --[get headers row]--
		html.append( "<tr>" );
		// for each columns defined
		for ( String key : valuesToExport )
		{
		    String value = null;

		    // check if translation is defined
		    if ( translationOfValuesToExport != null && translationOfValuesToExport.containsKey( key ) )
		        value = translationOfValuesToExport.get( key );
		    else
		        value = key;

		    // for null values use NBSP
		    if ( value == null || value.trim().isEmpty() )
		        value = "&nbsp;";

		    html.append( "<th>" + value + "</th>" );
		}
		html.append( "</tr>" );
		// --[get entity row]--
		for ( Entity entity : entities )
		{
		    getHTMLForEntity( entity, html );
		}
		html.append( "</tbody></table>" );
		getLog().debug( "Generated HTML code: " + html );
		return html;
	}

	/**
	 * Query for entities in the HP ALM
	 * 
	 * @param cokie HP ALM session authentication cookie
	 * @return List of entities from HP ALM
	 * @throws Exception
	 */
	private List<Entity> retreiveEntities(String cokie) throws Exception
	{
		List<Entity> entities;
		boolean hasMoreEntities = true;
		int startIndex = 1;
		entities = new ArrayList<Entity>();
		while ( hasMoreEntities )
		{
		    String finalURL = url + "/qcbin/rest/domains/" + domain + "/projects/" + project + "/defects?page-size=10&start-index=" + startIndex + "&query={" + query + "}";
		    getLog().debug( "Final HP ALM url: " + finalURL );
		    String res = dao.getFromHPALM( finalURL, cokie );

		    List<Entity> entitiesToAdd = parse( res );

		    if ( entitiesToAdd != null )
		        entities.addAll( entitiesToAdd );

		    startIndex += 10;
		    hasMoreEntities = ( entities != null && entities.size() == 10 );
		}
		return entities;
	}

	/**
	 * Building HP ALM query statement
	 */
	private void buildQueryStatement()
	{
		if ( query == null )
		{
		    query = "";
		    Set<String> keys = queryValues.keySet();
		    boolean isFirstItem = true;
		    for ( String key : keys )
		    {
		        String value = queryValues.get( key );

		        try
		        {
		            // the + signs are not parsed as the spaces thus additional
		            // conversion to %20 is needed
		            key = URLEncoder.encode( key, "UTF-8" ).replace( "+", "%20" );
		            value = URLEncoder.encode( value, "UTF-8" ).replace( "+", "%20" );
		        }
		        catch ( UnsupportedEncodingException e )
		        {
		            e.printStackTrace();
		        }

		        query += ( isFirstItem ? "" : ";" ) + key + "[%22" + value + "%22]";
		        isFirstItem = false;
		    }
		}
		getLog().debug( "Final HP ALM query: " + query );
	}

	/**
	 * Update confluence page with given content and defined header under the given keyword (or at the top of page in none given)
	 * @param contentToAdd - content to put to confluence page
	 * @throws Exception if error occurred during the confluence page update
	 */
	private void updateConfluencePage(String contentToAdd) throws Exception
	{
		dao.initializeXmlRpcClient(confluenceServer);

		Object result = dao.authenticateToConfluence(confleunceUser,confluencePassword);

		// get the confluence page
		HashMap<Object, Object> page = dao.getConfluencePage(result,confluencePageID);
		getLog().debug("Current CONFLUENCE page: " + page);

		// try to find the keyword under which the content will be put
		String content = (String) page.get("content");
        int i = -1;

        if ( confluenceKeyWordForUpdate != null )
            i = content.indexOf( confluenceKeyWordForUpdate );

        if ( updateHeader == null )
            updateHeader = "";

        //if keyword was found
        if ( i >= 0 )
        {
            i += confluenceKeyWordForUpdate.length();
            content = content.substring( 0, i ) + updateHeader + contentToAdd + content.substring( i );
        }
        else
        {
            content = updateHeader + contentToAdd + content;
        }
	    

		getLog().debug("and the new content: " + content);

		// update confluence page
		page.put("content", content);
		HashMap<Object, Object> pageUpdateOptions = new HashMap<Object, Object>();
		dao.updateConfluencePage(result, page, pageUpdateOptions);
	}

	/**
	 * Wraps HP ALM entity with HTML table row tags
	 * 
	 * @param entity
	 *            - the entity to be transformed to HTML table row
	 * @param html
	 *            - the String Builder instance to append the content to
	 */
	private void getHTMLForEntity(Entity entity, StringBuilder html)
	{
		html.append("<tr>");
		// for each column configured
		for (String key : valuesToExport)
		{
			// get the value from entity for corresponding column
			String value = "&nbsp;";
			if (entity.fields != null)
			{
				List<Field> fields = entity.fields;
				for (Field field : fields)
					if (key.equalsIgnoreCase(field.name))
					{
						value = field.value;
					}
			}

			html.append("<td>" + value + "</td>");
		}
		html.append("</tr>");
	}

	/**
	 * Prepare update statement for entity
	 * 
	 * @param entity
	 *            - entity to update
	 * @return update statement for entity
	 */
	public String getUpdateStatement(Entity entity)
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><Entity Type=\"defect\"><Fields>";

		Set<String> keys = valuesToUpdate.keySet();
		for (String key : keys)
		{
			String value = valuesToUpdate.get(key);
			xml += "<Field Name=\"" + key + "\"><Value>" + value + "</Value></Field>";
		}
		xml += "</Fields></Entity>";

		return xml;
	}

	/**
	 * Transform the entities to object representation
	 * 
	 * @param xmlBody
	 *            - result of HP ALM query containing entity definitions to
	 *            parse
	 * @return list of entities
	 */
	public List<Entity> parse(String xmlBody)
	{
		JAXBContext context;
		try
		{
			// get the XML parser
			context = JAXBContext.newInstance(EntitiesRoot.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();

			// parse the XML body
			StringReader reader = new StringReader(xmlBody);
			EntitiesRoot entitiesRoot = (EntitiesRoot) unmarshaller.unmarshal(reader);
			
			//parse some attributes to simplify the defects processing
			if (entitiesRoot != null)
			{
				if (entitiesRoot.entities != null) for (Entity entity : entitiesRoot.entities)
				{
				    
		            int countOfFixTypeConditionsValidated    = 0;
		            int countOfAddTypeConditionsValidated    = 0;
		            int countOfUpdateTypeConditionsValidated = 0;
		            int countOfRemoveTypeConditionsValidated = 0;
				    
		            //parse the attributes
					if (entity.fields != null) for (Field field : entity.fields)
					{
					    
			            // update the ID filed of entity due to simplify the entities identification.					    
						if (field.name.equalsIgnoreCase("id"))
						{
							entity.id = field.value;

						}
						if (field.name.equalsIgnoreCase(changesDevFiledMapping))
                        {
                            entity.dev = field.value;
  
                        }
                        if (field.name.equalsIgnoreCase(changesDescFiledMapping))
                        {
                            entity.desc = field.value;
     
                        }				
                        if (field.name.equalsIgnoreCase(changesDueToFiledMapping))
                        {
                            entity.dueTo = field.value;
       
                        }
                        
                        //check changes filter for add type issues
                        if( changesAddIssuesFilter!=null && changesAddIssuesFilter.containsKey(field.name))
                        {
                            if(changesAddIssuesFilter.get( field.name ).equalsIgnoreCase( field.value))
                                countOfAddTypeConditionsValidated++;
                        }
                        
                        //check changes filter for fix type issues
                        if( changesFixIssuesFilter!=null && changesFixIssuesFilter.containsKey(field.name))
                        {
                            if(changesFixIssuesFilter.get( field.name ).equalsIgnoreCase( field.value))
                                countOfFixTypeConditionsValidated++;
                        }    
                        
                        //check changes filter for update type issues
                        if( changesUpdateIssuesFilter!=null && changesUpdateIssuesFilter.containsKey(field.name))
                        {
                            if(changesUpdateIssuesFilter.get( field.name ).equalsIgnoreCase( field.value))
                                countOfUpdateTypeConditionsValidated++;
                        }           
                        
                        //check changes filter for remove type issues
                        if( changesRemoveIssuesFilter!=null && changesRemoveIssuesFilter.containsKey(field.name))
                        {
                            if(changesRemoveIssuesFilter.get( field.name ).equalsIgnoreCase( field.value))
                                countOfRemoveTypeConditionsValidated++;
                        }                             
						
					}
					
					//set the issue type
					                    
                    //check changes filter for fix type issues
                    if( changesFixIssuesFilter!=null && changesFixIssuesFilter.size()==countOfFixTypeConditionsValidated)
                    {
                        entity.changeType="FIX";
                    }
                    
                    //check changes filter for add type issues
                    if( changesAddIssuesFilter!=null && changesAddIssuesFilter.size()==countOfAddTypeConditionsValidated)
                    {
                    	entity.changeType="ADD";
                    }
                    
                    
                    //check changes filter for update type issues
                    if( changesUpdateIssuesFilter!=null && changesUpdateIssuesFilter.size()==countOfUpdateTypeConditionsValidated)
                    {
                    	entity.changeType="UPDATE";
                    }           
                    
                    //check changes filter for remove type issues
                    if( changesRemoveIssuesFilter!=null && changesRemoveIssuesFilter.size()==countOfRemoveTypeConditionsValidated)
                    {
                    	entity.changeType="REMOVE";
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
	
	public HPALMMavenPluginDAOI getDao()
	{
		return dao;
	}

	public void setDao(HPALMMavenPluginDAOI dao)
	{
		this.dao = dao;
	}    

}
