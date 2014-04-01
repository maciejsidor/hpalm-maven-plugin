package com.googlecode.msidor.maven.plugins.hpalm.deliverynote;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class HPALMMavenPluginTestCase extends AbstractMojoTestCase
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
	 * @throws Exception
	 *             if any
	 */
	public void testSomething() throws Exception
	{
		File pom = getTestFile("src/test/resources/com/googlecode/msidor/maven/plugins/hpalm/deliverynote/plugin-config.xml");
		assertNotNull(pom);
		assertTrue(pom.exists());

		HPALMMavenPlugin myMojo = (HPALMMavenPlugin) lookupMojo("generate-change", pom);
		myMojo.setDao(new HPALMMavenPluginDAOMockup());
				
		assertNotNull(myMojo);
		myMojo.execute();
	}
}
