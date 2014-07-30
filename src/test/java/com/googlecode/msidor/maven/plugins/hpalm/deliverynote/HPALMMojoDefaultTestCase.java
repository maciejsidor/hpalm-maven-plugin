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

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.util.HashMap;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.logging.Logger;

/**
 * Default success test case.
 * This is black box kind of test which means that the test case prepares input data, executes plugin and validates outputs.
 * There are no unit tests on particular methods.  
 * This test case is based on full plugin configuration.
 * Here are some details about the test configuration: 
 * <ul>
 * <li>5 HP ALM Defects : 2 Defects, 1 Evolution, 1 Upgrade and 1 Delete</li>
 * <li>Confleunce page set</li>
 * <li>Plugin configuration file: src/test/resources/com/googlecode/msidor/maven/plugins/hpalm/deliverynote/plugin-conf.xml</li>
 * <li>HP ALM Entities file: src/test/resources/com/googlecode/msidor/maven/plugins/hpalm/deliverynote/hpalm-entities-query-result.xml</li>
 * </ul>
 * 
 */
public class HPALMMojoDefaultTestCase extends AbstractMojoTestCase
{

	/** {@inheritDoc} */
	protected void setUp() throws Exception
	{
		// required
		super.setUp();
	}

	/** {@inheritDoc} */
	protected void tearDown() throws Exception
	{
		// required
		super.tearDown();
	}

	/**
	 * The implementation of the default test case
	 * @throws Exception
	 *             if any
	 */
	public void testBasicExecution() throws Exception
	{
		Logger logger = getContainer().getLogger();		
		logger.info("Launching default test case");		
		logger.info("Setting test data");
		
		
		//cleanup the OUT direcotry
		File changesFile = new File("out/changes.xml");
		if(changesFile.exists())
			changesFile.delete();
		
		//load the plugin configuration
		File pom = getTestFile("src/test/resources/com/googlecode/msidor/maven/plugins/hpalm/deliverynote/plugin-conf.xml");
		assertNotNull("POM file with polugin test configuration could not be found",pom);
		assertTrue("POM file with polugin test configuration could not be found",pom.exists());

		//prepare the mockup DAO
		HPALMMojoDAOMockup dao = new HPALMMojoDAOMockup();
		dao.setLogger(logger);

		// add the result from HP ALM
		dao.addToFromHPALMResultFiles("http://alm.organization.net:8080/qcbin/rest/domains/DEP1/projects/PROJECT_1/defects?page-size=10&start-index=1&query={planned-closing-ver[%22%24%7Bproject.version%7D%22];project[%22Test%20Project%22];status[%22Corrected%22]}LWSSO_COOKIE_KEY=yv5W1bsYKsX-zzlWWq4jdL9qICd-6wYEeq69dwww67lfg1tLMVFXPdXDOZgbUWTUNUKRVPkdlz2Fjfo58B62Mp-dDLpfCPETEXWNFx4wT5Ic1lKtz8YSgz8C3DujgbsbETjVBe_ryFZHxFqmJev2w0mqmo2LIR8aTw9f4tQ2BJjY1xkNLQDt4p8_4g_25OP9wQZ-emAvkBc1lAjuXsXOZpNw7jXRvnaSjkaXt0uIcoU.; Path=/", "src/test/resources/com/googlecode/msidor/maven/plugins/hpalm/deliverynote/hpalm-entities-query-result.xml");

		// add the confluence page
		HashMap<Object, Object> page = new HashMap<Object, Object>();
		page.put("space", "Doc");
		page.put("url", "http://confluence.organization.net/display/Doc/Project1+Release+Notes");
		page.put("version", Integer.valueOf(5));
		page.put("creator", "msidor");
		page.put("modified", "Wed Apr 02 10:45:35 CEST 2014");
		page.put("id", Integer.valueOf(25200500));
		page.put("content", "<p>begining of the page</p><p>end of the page</p>");
		page.put("modifier", "msidor");
		page.put("parentId", Integer.valueOf(25200485));
		page.put("title", "Project1 Release Notes");
		page.put("created", "Mon Mar 31 17:32:46 CEST 2014");
		page.put("contentStatus", "current");
		page.put("permissions", Integer.valueOf(0));
		page.put("current", Boolean.valueOf(true));
		page.put("homePage", Boolean.valueOf(false));
		dao.addConfluencePage("479a88d7bb24391678", page);

		//setup the mojo
		logger.info("Preparing plugin configuration");
		HPALMMojo myMojo = (HPALMMojo) lookupMojo("generate-change", pom);
		myMojo.setDao(dao);
		assertNotNull("Plugin not found",myMojo);
				
		//execute the goal
		logger.info("Executing goal");
		logger.info("-------------------");
		myMojo.execute();
		logger.info("-------------------");
		logger.info("Validating results");
		
		//check the changes.xml file
		assertTrue("Changes file was not generated",changesFile.exists());
		String checksum = createChecksum(new FileReader(changesFile));
		assertTrue("Changes file is different than expected. New checksum : "+checksum,"762c771d601e29faf8aff196d869453a".equals(checksum));
		
		//check the confluence page
		HashMap<Object, Object> updatedPage = dao.getUpdatedConfluencePage();
		assertNotNull("Confluence page has not been updated",updatedPage);
																		
		assertTrue("Updated Confluence page is different than expected. New checksum : "+updatedPage.hashCode(),-687342227==updatedPage.hashCode());
		
		logger.info("Test accomplished succesfully");
		
	}
	
    /**
     * Calculate checksum for file
     * @param filename Path to file
     * @return String representation of binary checksum
     * @throws Exception occurred while computing checksum
     */
    public String createChecksum( Reader fis )
        throws Exception
    {
        byte[] buffer = new byte[1024];
        char[] bufferStr = new char[1024];
        MessageDigest complete = MessageDigest.getInstance( "MD5" );
        int numRead;        
        do
        {
            numRead = fis.read( bufferStr );

            //eliminate the date in checksum calculation. The example of string to eliminate "date="Tue Jun 10 02:24:16 CEST 2014">"
            bufferStr=String.valueOf(bufferStr).replaceFirst("(<release version=\"[^\"]+\" )date=\"[^\"]+\">", "$1>").toCharArray();
              
            buffer = stringToBytesUTFCustom(bufferStr );
            
            if ( numRead > 0 )
            {
                complete.update( buffer, 0, numRead );
            }
        }
        while ( numRead != -1 );

        fis.close();

        String result = "";
        byte[] checkSumInBytes = complete.digest();
        for ( byte b : checkSumInBytes )
        {
            result += Integer.toString( ( b & 0xff ) + 0x100, 16 ).substring( 1 );
        }

        return result;
    }

    /**
     * Transform char array to bytes array
     * @param buffer
     * @return bytes array representation of buffer
     */
    public static byte[] stringToBytesUTFCustom( char[] buffer )
    {
        byte[] b = new byte[buffer.length << 1];
        for ( int i = 0; i < buffer.length; i++ )
        {
            int bpos = i << 1;
            b[bpos] = (byte) ( ( buffer[i] & 0xFF00 ) >> 8 );
            b[bpos + 1] = (byte) ( buffer[i] & 0x00FF );
        }
        return b;
    }	
}
