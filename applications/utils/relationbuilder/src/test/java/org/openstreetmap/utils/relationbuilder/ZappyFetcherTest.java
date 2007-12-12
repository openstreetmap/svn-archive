/**
 * 
 */
package org.openstreetmap.utils.relationbuilder;

import static org.junit.Assert.*;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hakan Tandogan
 * 
 */
public class ZappyFetcherTest
{
	Log log = LogFactory.getLog(ZappyFetcherTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link org.openstreetmap.utils.relationbuilder.ZappyFetcher#constructURL(java.lang.String, java.lang.String, float, float, float, float)}.
	 */
	@Test
	public void testConstructURL_Usak()
	{
		try
		{
			float usak_east = 29.4f;
			float usak_south = 38.6f;
			float usak_west = 29.5f;
			float usak_north = 38.7f;

			ZappyFetcher zf = (new ZappyFetcher());
			URL u = zf.constructURL("node", "place=city", usak_east, usak_west,
					usak_south, usak_north);

			assertEquals("URL contruction failure", ZappyFetcher.getURLBASE()
					+ "node[place=city][bbox=29.4,38.6,29.5,38.7]", u.toString());

		}
		catch (Exception e)
		{
			log.error("URL construction failure", e);
			fail("with exception: " + e.getMessage());
		}
	}

}
