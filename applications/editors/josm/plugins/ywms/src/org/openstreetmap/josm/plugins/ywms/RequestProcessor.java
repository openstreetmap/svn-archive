package org.openstreetmap.josm.plugins.ywms;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
    
/**
 * Processes each of the WMS cliente requests, and serves the PNG image of the
 * area.
 * <p>
 * This class acts as a very simple HTTP server, that only understands WMS GET
 * requests.
 * 
 * @author frsantos
 */
public class RequestProcessor extends Thread 
{
	private Socket request;
  
	/**
	 * Constructor
	 * 
	 * @param request The WMS request
	 * @param pluginDir The directory of the plugin
	 */
	public RequestProcessor(Socket request) 
	{
	    super("YWMS request processor");
	    this.setDaemon(true);
		this.request = request;
	}
  
	/**
	 * Spawns a new thread for the request
	 * 
	 * @param request The WMS request
	 */
	public static void processRequest(Socket request) 
	{
		RequestProcessor processor = new RequestProcessor(request);
		processor.start();
	}  
  
	/**
	 * The work is done here.
	 * <p>
	 * It parses the request and extracts the URL requested, used to load the
	 * image, and serves it back
	 */
	public void run() 
	{
		Writer out = null;
		try 
		{            
	        OutputStream raw = new BufferedOutputStream( request.getOutputStream() );         
	        out = new OutputStreamWriter(raw);
	        Reader in = new InputStreamReader( new BufferedInputStream( request.getInputStream(  ) ), "ASCII" );
	        
	        StringBuffer requestLine = new StringBuffer();
	        while (true) 
	        {
	            int c = in.read();
	            if (c == '\r' || c == '\n') break;
	            requestLine.append((char) c);
	        }
	        
	        String get = requestLine.toString();
	        StringTokenizer st = new StringTokenizer(get);
	        String method = st.nextToken();
	        String url = st.nextToken();

	        if( !method.equals("GET") )
	        {
	        	sendNotImplemented(out);
	        	return;
	        }

	        // Load the image
	        ImageLoader imageLdr = new ImageLoader(url);
            BufferedImage wmsImage = imageLdr.getBufferedImage();
            
            // send the image data
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            ImageIO.write(wmsImage, "jpeg", imageStream);
            byte[] data = imageStream.toByteArray();
            
			sendHeader(out, "200 OK", "image/jpeg", false);
            out.write("Content-length: " + data.length + "\r\n");
            out.write("\r\n");
            out.flush();
            
            raw.write(data);
            raw.flush();
		}
		catch (IOException ioe) { }
		catch(Exception e)
		{
			e.printStackTrace();
			try 
			{
				sendError(out);
			} 
			catch (IOException e1) { }
		}
		finally 
		{
	        try 
	        {
	        	request.close();        
	        }
	        catch (IOException e) {} 
		}
	}

	/**
	 * Sends a 500 error: server error
	 */
	private void sendError(Writer out) throws IOException
	{
		sendHeader(out, "500 Internal Server Error", "text/html", true);
		out.write("<HTML>\r\n");
		out.write("<HEAD><TITLE>Internal Error</TITLE>\r\n");
		out.write("</HEAD>\r\n");
		out.write("<BODY>");
		out.write("<H1>HTTP Error 500: Internal Server Error</h2>\r\n");
		out.write("</BODY></HTML>\r\n");
		out.flush();
	}

	/**
	 * Sends a 501 error: not implemented
	 */
	private void sendNotImplemented(Writer out) throws IOException
	{
		sendHeader(out, "501 Not Implemented", "text/html", true);
		out.write("<HTML>\r\n");
		out.write("<HEAD><TITLE>Not Implemented</TITLE>\r\n");
		out.write("</HEAD>\r\n");
		out.write("<BODY>");
		out.write("<H1>HTTP Error 501: Not Implemented</h2>\r\n");
		out.write("</BODY></HTML>\r\n");
		out.flush();
	}
	
	/**
	 * Send common HTTP headers to the client.
	 * 
	 * @param out The Writer
	 * @param status The status string ("200 OK", "500", etc)
	 * @param contentType The content type of the data sent
	 * @param endHeaders If true, adds a new line, ending the headers.
	 * @throws IOException When error
	 */
	private void sendHeader(Writer out, String status, String contentType, boolean endHeaders) throws IOException
	{
		out.write("HTTP/1.1 " + status + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
        out.write("Server: YWMS Server 1.0\r\n");
        out.write("Content-type: " + contentType + "\r\n");
        if( endHeaders )
        	out.write("\r\n");
	}
}
