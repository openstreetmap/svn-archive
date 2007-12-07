/**
 * 
 */
package org.openstreetmap.utils.relationbuilder;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author ext.tandogan
 * 
 */
public class ZappyFetcherTest
{

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
			float usak_min_lon = 29.4f;
			float usak_min_lat = 38.6f;
			float usak_max_lon = 29.5f;
			float usak_max_lat = 38.7f;

			ZappyFetcher zf = (new ZappyFetcher());
			URL u = zf.constructURL("node", "place=city", usak_min_lon, usak_max_lon,
					usak_min_lat, usak_max_lat);

			assertEquals("URL contruction failure", ZappyFetcher.getURLBASE()
					+ "node[place=city][bbox=29.4,38.6,29.5,38.7]", u.toString());

		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
			fail("with exception: " + e.getMessage());
		}
	}

}
