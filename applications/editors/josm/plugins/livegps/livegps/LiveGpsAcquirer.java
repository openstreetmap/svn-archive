package livegps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;

public class LiveGpsAcquirer implements Runnable {
	LiveGpsLayer view;
	Socket gpsdSocket;
	BufferedReader gpsdReader;
	boolean connected = false;
	String gpsdHost = "localhost";
	int gpsdPort = 2947;
	boolean shutdownFlag = false;
	
	public LiveGpsAcquirer() {
		
	}
	
	public void run() {	
		while(!shutdownFlag) {
			double lat = 0;
			double lon = 0;
			boolean haveFix = false;

			try
			{
				if (!connected)
				{
					view.setStatus("connecting...");
					InetAddress[] addrs = InetAddress.getAllByName(gpsdHost);
					for (int i=0; i < addrs.length; i++) {
						try {
							gpsdSocket = new Socket(addrs[i], gpsdPort);
							break;
						} catch (Exception e) {
							gpsdSocket = null;
						}
					}
					
					if (gpsdSocket != null)
					{
						gpsdReader = new BufferedReader(new InputStreamReader(gpsdSocket.getInputStream()));
						gpsdSocket.getOutputStream().write(new byte[] { 'w', 13, 10 });
					}
					view.setStatus("connected");
					connected = true;
				}


				String line = gpsdReader.readLine();
				if (line == null) break;
				String words[] = line.split(",");

				if ((words.length == 0) || (!words[0].equals("GPSD"))) {
					// unexpected response.
					continue;
				}

				for (int i = 1; i < words.length; i++) {
					
					if ((words[i].length() < 2) || (words[i].charAt(1) != '=')) {
						// unexpected response.
						continue;
					}
					
					char what = words[i].charAt(0);
					String value = words[i].substring(2);

					switch(what) {
					case 'O':
						// full report, tab delimited.
						String[] status = value.split("\\s+");
						if (status.length >= 5) {
							lat = Double.parseDouble(status[3]);
							lon = Double.parseDouble(status[4]);
							try {
								view.setSpeed(Float.parseFloat(status[9]));
								view.setCourse(Float.parseFloat(status[8]));
							} catch (NumberFormatException nex) {}
							haveFix = true;
						}
						break;
					case 'P':	
						// position report, tab delimited.
						String[] pos = value.split("\\s+");
						if (pos.length >= 2) {
							lat = Double.parseDouble(pos[0]);
							lon = Double.parseDouble(pos[1]);
							haveFix = true;
						}
					default:
						// not interested
					}

				}

				if (haveFix) {
					view.setCurrentPosition(lat, lon);
				}
				
			} catch(IOException iox) {
				connected = false;
				view.setStatus("connection failed");
				try { Thread.sleep(1000); } catch (Exception x) {};
				// send warning to layer

			}

		}
		view.setStatus("disconnected");
		if (gpsdSocket != null) try { gpsdSocket.close(); } catch (Exception ex) {};
	}
	
	public void shutdown()
	{
		shutdownFlag = true;
	}
	
	public void setOutputLayer(LiveGpsLayer o)
	{
		view = o;
	}
}
