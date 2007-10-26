package org.openstreetmap.josm.plugins.ywms;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.InetAddress;

/**
 * Simple HTTP server that spawn a {@link RequestProcessor} for every connection.
 * 
 * @author frsantos
  */
public class HTTPServer extends Thread 
{
    /** Default port for the HTTP server */
	public static final int DEFAULT_PORT = 8000;
	
	/** The server socket */
	private ServerSocket server;

	/**
	 * Constructor
	 * @param port The port this server will listen
	 * @throws IOException when connection errors
	 */
	public HTTPServer(int port)
		throws IOException 
	{
		super("YWMS HTTP Server");
		this.setDaemon(true);
		// Start the server socket with only 1 connection, because we can 
        // only start one firefox process. Also make sure we only listen
        // on the local interface so nobody from the outside can connect!
		this.server = new ServerSocket(port, 1, 
            InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
	}

	/**
	 * The main loop, spawns a {@link RequestProcessor} for each connection
	 */
	public void run() 
	{
		System.out.println("YWMS::Accepting connections on port " + server.getLocalPort());
		while (true) 
		{
			try 
			{
				Socket request = server.accept();
				RequestProcessor.processRequest(request);
			}
			catch( SocketException se)
			{
				if( !server.isClosed() )
					se.printStackTrace();
			}
			catch (IOException ioe) 
			{
				ioe.printStackTrace();
			}
		}
	}
	
	/**
     * Stops the HTTP server
     *  
     * @throws IOException
	 */
	public void stopServer() throws IOException
	{
		server.close();
	}
}
