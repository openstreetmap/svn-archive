package ch.guggis.josm.bern.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.guggis.josm.bern.servlet.exception.IllegalParameterValueException;
import ch.guggis.josm.bern.servlet.exception.MissingParameterException;
import ch.guggis.josm.bern.servlet.exception.OrthofotoBernWMSAdapterException;

/**
 * This servlet adapts the Web Map Service (WMS) of the city of Bern for the
 * JOSM WMS plugin.
 * 
 * It converts lat/lon coordinates (EPSG:4326) used in JOSM to x/y-coordinates
 * in the Swiss Grid (CH1903).
 * 
 * The servlet includes methods for retrieving two cookies from the WMS server
 * of the city of Bern:
 * <ul>
 * <li>{@see #COOKIE_NAME_SESSION_ID1} is the cookie generated by the main map
 * site www.stadthplan.bern.ch</li>
 * <li>{@see #ASPSESSIONIDQSADRCCB} is the cookie generated by the WMS server</li>
 * </ul>
 * 
 * You can still try to retrieve these session IDs invoking the servlet with
 * 
 * <pre>
 *     http://.....?action=show-session-id
 * </pre>
 * 
 * but unfortunately the map server doesn't accept these cookies in subsequent
 * orthofoto tile requests. You therefore have to get a valid session id with
 * your favorite browser and enter it in the servlet using the servlets web form
 * - see README.txt for more information.
 * 
 * <strong>Actions</strong>
 * <dl>
 * <dt>http://....?action=ping</dt>
 * <dd>Checks whether the servlet is alive (not the WMS server) and replies an
 * OK text</dd>
 * 
 * <dt>http://....?action=show-session-id</dt>
 * <dd>Retrieves the session ids from the WMS server and displays them</dd>
 * 
 * <dt>http://....?action=set-session-id[&session-id=thesessionid]</dt>
 * <dd>Displays a form for entering the session id to be used in tile requests.</dd>
 * 
 * <dt>http://....?action=get-map&</dt>
 * <dd>Displays a form for entering the session id to be used in tile requests.
 * Use <code>width</code>, <code>height</code>, and <code>bbox</code> as
 * parameters. Replies the orthofoto tile from the web server of the city of
 * Bern.</dd>
 * <dl>
 * 
 */
public class OrthofotoBernWMSAdapter extends HttpServlet {

	static public final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.0.5) Gecko/2008120122 Firefox/3.0.5";

	/** the name of the session cookie used by www.stadtplan.bern.ch */
	static public final String COOKIE_NAME_SESSION_ID1 = "ASPSESSIONIDQSADRCCB";
	/**
	 * the name of the session cookie used by the WMS server of
	 * www.stadtplan.bern.ch
	 */
	static public final String COOKIE_NAME_SESSION_ID2 = "ASP.NET_SessionId";

	static public final String DEFAULT_URL_GEN_SESSION_ID1 = "http://www.stadtplan.bern.ch/";
	static public final String DEFAULT_URL_GEN_SESSION_ID2 = "http://www.stadtplan.bern.ch/TBInternet/WebMapPageLV.aspx";

	/** the logger */
	private static Logger logger = Logger.getLogger(OrthofotoBernWMSAdapter.class.getName());

	HashMap<String, String> cityMapSessionCookies = new HashMap<String, String>();

	static private final String DEFAULT_LAYER = "TBI_orthofoto_08.mwf"; 
	private String layer = DEFAULT_LAYER;
	
	/**
	 * remembers session cookie retrieved from Berns map server
	 * 
	 * @param headerField
	 *            the header field with the session cookie
	 * 
	 */
	protected void rememberSessionCookie(String headerField) {
		String cookie = headerField;
		cookie = cookie.substring(0, cookie.indexOf(";"));
		String cookieName = cookie.substring(0, cookie.indexOf("="));
		String cookieValue = cookie.substring(cookie.indexOf("=") + 1, cookie
				.length());
		cityMapSessionCookies.put(cookieName, cookieValue);
		if (!(cookieName.equals(COOKIE_NAME_SESSION_ID1) || cookieName
				.equals(COOKIE_NAME_SESSION_ID2))) {
			logger.warning("unexpected name for session cookie. name="
					+ cookieName + ",value=" + cookieValue);
		}
	}

	/**
	 * retrieves the main session ID from Berns map application
	 * 
	 * @exception IOException
	 *                thrown, if an IO exception occurs
	 */
	protected void getCityMapSessionID1() throws IOException {
		URL url = null;

		try {
			url = new URL(DEFAULT_URL_GEN_SESSION_ID1);
		} catch (MalformedURLException e) {
			// should not happen, but log it anyway
			logger.log(Level.SEVERE, e.toString());
			return;
		}

		cityMapSessionCookies.remove(COOKIE_NAME_SESSION_ID1);

		try {
			URLConnection con = url.openConnection();
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.connect();
			String headerName = null;
			for (int i = 1; (headerName = con.getHeaderFieldKey(i)) != null; i++) {
				logger.log(Level.INFO, headerName + "="
						+ con.getHeaderField(headerName));
				if (headerName.equals("Set-Cookie")) {
					String cookie = con.getHeaderField(i);
					rememberSessionCookie(cookie);
				}
			}
			if (cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID1) == null) {
				logger.log(Level.WARNING, "response did not include cookie "
						+ COOKIE_NAME_SESSION_ID1
						+ ". Further requests to the WMS server will timeout.");
			} else {
				logger.info("successfully retrieved cookie "
						+ COOKIE_NAME_SESSION_ID1 + ". value is <"
						+ cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID1)
						+ ">");
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"failed to retrieve Session from Stadtplan Bern. "
							+ e.toString());
			throw e;
		}
	}

	/**
	 * retrieves the session ID from Berns WMS server. It mimics the behaviour
	 * of a standard browser as closely as possible. The request header fields
	 * sent to the remote server are those Firefox sends. They include a valid
	 * session ID from www.stadtplan.bern.ch.
	 * 
	 * @exception IOException
	 *                thrown, if an IO exception occurs
	 */

	protected void getCityMapSessionID2() throws IOException {
		URL url = null;

		try {
			url = new URL(DEFAULT_URL_GEN_SESSION_ID2);
		} catch (MalformedURLException e) {
			// should not happen, but log it anyway
			logger.log(Level.SEVERE, e.toString());
			return;
		}

		cityMapSessionCookies.remove(COOKIE_NAME_SESSION_ID2);

		try {
			URLConnection con = url.openConnection();
			con.setRequestProperty("Host", "www.stadtplan.bern.ch");
			con.setRequestProperty("Referer", "http://www.stadtplan.bern.ch/");
			con.setRequestProperty("Cookie", COOKIE_NAME_SESSION_ID1 + "="
					+ cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID1));
			con.setRequestProperty("User-Agent", USER_AGENT);
			con
					.setRequestProperty("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			con.setRequestProperty("Accept-Language",
					"de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
			con.setRequestProperty("Accept-Encoding", "gzip,deflate");
			con.setRequestProperty("Accept-Charset",
					"ISO-8859-1,utf-8;q=0.7,*;q=0.7");

			con.connect();
			String headerName = null;
			for (int i = 1; (headerName = con.getHeaderFieldKey(i)) != null; i++) {
				logger.log(Level.INFO, headerName + "="
						+ con.getHeaderField(headerName));
				if (headerName.equals("Set-Cookie")) {
					String cookie = con.getHeaderField(i);
					rememberSessionCookie(cookie);
				}
			}
			if (cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID2) == null) {
				logger.log(Level.WARNING, "response did not include cookie "
						+ COOKIE_NAME_SESSION_ID2
						+ ". Further requests to the WMS server will timeout.");
			} else {
				logger.info("successfully retrieved cookie "
						+ COOKIE_NAME_SESSION_ID2 + ". value is <"
						+ cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID2)
						+ ">");
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE,
					"failed to retrieve Session from Stadtplan Bern. "
							+ e.toString());
			throw e;
		}
	}

	/**
	 * retrieves a session id from the map application provided by the city of
	 * bern
	 * 
	 * @throws IOException
	 *             thrown, if the connection to the map application fails
	 */
	protected void getCityMapSessionIDs() throws IOException {
		getCityMapSessionID1();
		getCityMapSessionID2();
	}

	/**
	 * true, if both the session IDs are known; false, otherwise
	 * 
	 * @return true, if both the session IDs are known; false, otherwise
	 */
	protected boolean hasCityMapSessionIDs() {
		return cityMapSessionCookies != null
				&& cityMapSessionCookies.containsKey(COOKIE_NAME_SESSION_ID1)
				&& cityMapSessionCookies.containsKey(COOKIE_NAME_SESSION_ID2);
	}

	/**
	 * handles simple ping request
	 * 
	 * @param request
	 *            the http request
	 * @param response
	 *            the http response
	 * @throws IOException
	 */
	protected void handlePing(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		PrintWriter pw = new PrintWriter(response.getWriter());
		pw.println("OK");

	}

	/**
	 * handles a request for the current session ID
	 * 
	 * @param req
	 * @param resp
	 * @throws IOException
	 */
	protected void handleShowSessionId(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		if (!hasCityMapSessionIDs()) {
			getCityMapSessionIDs();
		}

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/html");
		PrintWriter pw = new PrintWriter(resp.getWriter());

		if (cityMapSessionCookies.containsKey(COOKIE_NAME_SESSION_ID1)) {
			pw.println("OK: " + COOKIE_NAME_SESSION_ID1 + "="
					+ cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID1));
		} else {
			pw.println("FAIL: " + COOKIE_NAME_SESSION_ID1 + " missing.");
		}
		pw.println("</br>");
		if (cityMapSessionCookies.containsKey(COOKIE_NAME_SESSION_ID2)) {
			pw.println("OK: " + COOKIE_NAME_SESSION_ID2 + "="
					+ cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID2));
		} else {
			pw.println("FAIL: " + COOKIE_NAME_SESSION_ID2 + " missing.");
		}

	}

	protected String buildDefaultUrlForMapRequests() {		
		StringBuffer sb = new StringBuffer();
		sb.append("http://www.stadtplan.bern.ch/TBInternet/WebMapServer.aspx?");
		sb.append("VERSION=1.0.0").append("&");
		sb.append("REQUEST=GETMAP").append("&");
		sb.append("TYPE=11").append("&");
		sb.append("LAYERS=").append(layer).append("&");
		sb.append("FORMAT=image/jpeg").append("&");
		sb.append("EXCEPTIONS=image/jpeg");
		return sb.toString();
	}
	
	/**
	 * handles a tile request
	 * 
	 * @param req
	 *            the request
	 * @param resp
	 *            the response
	 * 
	 * @throws IOException
	 *             thrown, if an IO exception occurs
	 * @throws OrthofotoBernWMSAdapterException
	 *             thrown, if an exception occurs
	 */
	protected void handleTileRequest(HttpServletRequest req,
			HttpServletResponse resp) throws IOException,
			OrthofotoBernWMSAdapterException {
		int width;
		int height;

		// check parameters
		//
		if (req.getParameter("width") == null) {
			throw new MissingParameterException("width");
		}
		try {
			width = Integer.parseInt(req.getParameter("width"));
		} catch (NumberFormatException e) {
			throw new IllegalParameterValueException("width",
					"illegal int value", e);
		}

		if (req.getParameter("height") == null) {
			throw new MissingParameterException("height");
		}
		try {
			height = Integer.parseInt(req.getParameter("height"));
		} catch (NumberFormatException e) {
			throw new IllegalParameterValueException("height",
					"illegal int value", e);
		}

		if (req.getParameter("bbox") == null) {
			throw new MissingParameterException("bbox");
		}
		BoundingBox bbox = new BoundingBox();
		try {
			bbox.fromString(req.getParameter("bbox"));
		} catch (Exception e) {
			throw new IllegalParameterValueException("bbox",
					"failed to parse value", e);
		}

		// translate bounding box
		//
		bbox = BoundingBox.convertWGS84toCH1903(bbox);

		if (!cityMapSessionCookies.containsKey(COOKIE_NAME_SESSION_ID2)) {
			throw new OrthofotoBernWMSAdapterException(
					"required session IDs missing. Can't proceed with request.");
		}

		// build request URL
		//
		StringBuffer sb = new StringBuffer();
		sb.append(buildDefaultUrlForMapRequests());
		sb.append("&WIDTH=");
		sb.append(width);
		sb.append("&HEIGHT=");
		sb.append(height);
		sb.append("&");
		sb.append(bbox.toString());

		logger.info("requesting tile with URL <" + sb.toString() + ">");

		URL url;
		try {
			url = new URL(sb.toString());
		} catch (MalformedURLException e) {
			// should not happen, but log it anyway
			logger.log(Level.SEVERE, e.toString());
			throw new OrthofotoBernWMSAdapterException("failed to build URL", e);
		}

		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			String cookie = String.format("%s=%s", COOKIE_NAME_SESSION_ID2,
					cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID2));
			logger.info("setting cookie <" + cookie + ">");
			con.setRequestProperty("Cookie", cookie);
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Host", "www.stadtplan.bern.ch");
			con
					.setRequestProperty("Referer",
							"http://www.stadtplan.bern.ch/TBInternet/WebMapPageLV.aspx");
			con.setRequestProperty("Accept",
					"image/jpeg,image/*;q=0.8,*/*;q=0.5");
			con.setRequestProperty("Accept-Language",
					"de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
			con.setRequestProperty("Accept-Encoding", "gzip,deflate");
			con.setRequestProperty("Accept-Charset",
					"ISO-8859-1,utf-8;q=0.7,*;q=0.7");
			con.connect();
			resp.setContentType("image/jpeg");
			InputStream in = con.getInputStream();
			OutputStream out = resp.getOutputStream();
			byte[] buf = new byte[1024];
			int read = in.read(buf);
			while (read > 0) {
				out.write(buf, 0, read);
				read = in.read(buf);
			}
			in.close();

		} catch (Exception e) {
			throw new OrthofotoBernWMSAdapterException(
					"failed to fetch orthofoto tile from map server", e);
		}
	}

	protected void errorIllegalAction(HttpServletRequest request,
			HttpServletResponse response, String action) throws IOException {
		response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED,
				"unexpected value for parameter 'action'. Got " + action);
	}

	protected String buildConfigurationForm(HttpServletRequest req) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head></head><body>").append("\n");
		sb.append("<h1>WMS Adapter for Orthofotos of Bern</h1>").append("\n");
		sb.append("Please open <a href=\"http://www.stadtplan.bern.ch/TBInternet/default.aspx?User=1\">the city map of Bern</a> in your browser.</br>").append("\n");
		sb.append("Then lookup the cookie <strong>ASP.Net_SessionId</strong> for domain <strong>www.stadtplan.bern.ch</strong> in your browser and enter it in the form below.</br>").append("\n");
		sb.append("<form action=\"").append(req.getRequestURL()).append("\">").append("\n");
		sb.append("<input type=\"hidden\" name=\"action\" value=\"set-session-id\">").append("\n");
		sb.append("Session ID: <input type=\"text\" name=\"session-id\" value=\"\"><br/>").append("\n");
		sb.append("Select a layer:<br/>").append("\n");
		String checked;
		if (layer == null || layer.equals("TBI_orthofoto_08.mwf")) {
			checked=" checked ";			
		} else {
			checked = "";
		}		
		sb.append("<input type=\"radio\" name=\"layer\" value=\"TBI_orthofoto_08.mwf\"").append(checked).append(">Luftbilder 2008 Stadt Bern<br>").append("\n");
		if (layer != null && layer.equals("orthofoto_Regio_08.mwf")) {
			checked=" checked ";			
		} else {
			checked = "";
		}
		sb.append("<input type=\"radio\" name=\"layer\" value=\"orthofoto_Regio_08.mwf\"").append(checked).append(">Luftbilder 2008 Region Bern<br>").append("\n");
		sb.append("<input type=\"submit\" value=\"Submit\">").append("\n");
		sb.append("</form").append("\n");
		sb.append("</body></html>").append("\n");
		return sb.toString();

	}
	protected void renderSessionIDInputForm(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		PrintWriter pw = new PrintWriter(resp.getWriter());
		pw.println(buildConfigurationForm(req));
	}

	protected void handleSetSessionId(HttpServletRequest req,
			HttpServletResponse resp) throws OrthofotoBernWMSAdapterException,
			IOException {
		if (req.getParameter("session-id") == null) {
			throw new MissingParameterException("session-id");
		}
		if (req.getParameter("layer") != null) {
			layer = req.getParameter("layer");
		} else {
			layer = DEFAULT_LAYER;
		}

		cityMapSessionCookies.put(COOKIE_NAME_SESSION_ID2, req
				.getParameter("session-id"));
		logger.info("set session id <"
				+ cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID2) + ">");

		String msg = "<html><head></head><body>\n"
				+ "<h1>WMS Adapter for Orthofots of Bern</h1>"
				+ "Session ID &lt;"
				+ cityMapSessionCookies.get(COOKIE_NAME_SESSION_ID2)
				+ "&gt; successfully set. You may now start to request map tiles from JOSM.</br></br>"
				+ "<a href=\""
				+ req.getRequestURL()
				+ "?action=getmap&bbox=7.4441276,46.9539095,7.4458911,46.9556731&width=500&height=499\">Click here for an example</a>"
				+ "</body></html>";

		PrintWriter pw = new PrintWriter(resp.getWriter());
		pw.println(msg);

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		logger.info("handling request <" + req.getRequestURI() + ">");

		String action = req.getParameter("action");
		if (action == null && req.getParameter("bbox") != null) {
			action = "getmap";
		}
		logger.info("action is <" + action + ">");
		if ("ping".equals(action)) {
			handlePing(req, resp);
		} else if ("show-session-id".equals(action)) {
			handleShowSessionId(req, resp);
		} else if ("getmap".equals(action)) {
			try {
				handleTileRequest(req, resp);
			} catch (Exception e) {
				logger.log(Level.SEVERE,
						"exception while handling tile request.", e);
			}
		} else if ("set-session-id".equals(action)) {
			try {
				handleSetSessionId(req, resp);
			} catch (OrthofotoBernWMSAdapterException e) {
				throw new ServletException(
						"exception caught while handing setting session id", e);
			}
		} else {
			renderSessionIDInputForm(req, resp);
		}
	}
}
