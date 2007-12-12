package org.openstreetmap.utils.relationbuilder;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Hakan Tandogan
 * 
 */
public class ZappyFetcher
{
	Log log = LogFactory.getLog(ZappyFetcher.class);

	// private static String DATA_DIR = "data";

	private static final String URLBASE = "http://www.informationfreeway.org/api/0.5/";

	static final String getURLBASE()
	{
		return URLBASE;
	}

	public void fetchWithBB(String type, String predicate, float east, float west,
			float south, float north)
	{
		try
		{
			URL u = constructURL(type, predicate, east, west, south, north);
			log.debug(u);
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	URL constructURL(String type, String predicate, float east, float west, float south,
			float north) throws MalformedURLException
	{
		StringBuffer urlBuffer = new StringBuffer(URLBASE);

		if (type != null)
		{
			urlBuffer.append(type);
		}

		if (predicate != null && (!("".equals(predicate))))
		{
			urlBuffer.append("[").append(predicate).append("]");
		}

		urlBuffer.append("[bbox=");
		urlBuffer.append(east).append(",");
		urlBuffer.append(south).append(",");
		urlBuffer.append(west).append(",");
		urlBuffer.append(north);
		urlBuffer.append("]");

		return new URL(urlBuffer.toString());
	}
}
