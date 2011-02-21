import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.openstreetmap.osmosis.core.xml.common.DateParser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class OsmHealthCheck extends PircBot {
	
	protected static OsmHealthCheck bot;
	protected static String ircNetwork = "irc.oftc.net";
	protected static String ircChanel = "#osm-dev";
	protected static String ircChanelVerbose = "#osm-healthcheck";
	private static boolean activateOutputToIRC = false;
	private static boolean outputToIRC = false;
	
	private static boolean runningTimmer;
	protected static final int noServices = 35;
	protected static String[] SERVICE = new String[noServices];
	
	private static BufferedWriter bwCurrent = null;
	private static String fnCurrent = null;
	
	private int slowResponses[] = new int[noServices];
	private int failedResponses[] = new int[noServices];
	private boolean reported[] = new boolean[noServices];
	private long reported_at[] = new long[noServices];
	private String msgs[] = new String[noServices];
	private Thread threads[] = new Thread[noServices];
	
	public class CheckHealth extends TimerTask {
		public int recheck;

		@Override
		public void run() {
			String msg = "Nothing to report";
			synchronized (this) {
				if (runningTimmer) return;
				recheck--;
				if (recheck > 0) return;
				recheck = 3;
				if (activateOutputToIRC) {
					outputToIRC = true;
					activateOutputToIRC = false;
				}
			}
			boolean fastCheck = false;
			
			msg = "Running OSM health check: " + new Date();
			reportString(msg, outputToIRC);
			if (outputToIRC) { bot.sendMessage(ircChanel, msg); bot.sendMessage(ircChanelVerbose, msg); }
			
			int idx = 0;
			
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org", "Main page", "The main page", 1, idx++, true);
			threads[idx] = alarmPageParallel("http://puff.openstreetmap.org", "Main page (puff)", "The main page (puff)", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://fuchur.openstreetmap.org", "Main page (fuchur)", "The main page (fuchur)", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/diary", "Diary page", "The diary pages", 1,idx++, true);
			threads[idx] = alarmPageParallel("https://www.openstreetmap.org/login", "https-login", "The https login page", 1,idx++, true);
			
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/browse/way/100", "Way browser", "The way browser page", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/browse/node/100", "Node browser","The Node browser page", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/browse/relation/200", "Relation browser", "The relation browser page", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://api.openstreetmap.org/api/0.6/map?bbox=0,0,0,0", "Map call", "The map call", 2,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/api/0.6/node/100", "Node API", "the node API", 2,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/api/0.6/way/100", "Way API", "the way API", 2,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/api/0.6/relation/200", "Relation API", "The relation API", 2,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/api/0.6/node/100/history", "Node history", "The node history", 2,idx++, true);
			threads[idx] = alarmPageParallel("http://api.openstreetmap.org/api/0.6/relation/13/full","Relation API - full call", "The Relation API -full call", 2,idx++, true);
			threads[idx] = alarmPageParallel("http://api.openstreetmap.org/api/0.6/trackpoints?bbox=0,0,0,0", "GPX API", "The GPX API", 2,idx++, true);
			threads[idx] = alarmPageParallel("http://www.openstreetmap.org/trace/526419/data", "GPX trace", "The GPX traces", 4,idx++, true);

			threads[idx] = alarmPageParallel("http://tile.openstreetmap.org/0/0/0.png", "Mapnik tile server", "The Mapnik tile server", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://128.40.168.104/0/0/0.png", "Mapnik-direct", "The Mapnik tile server (proxyless)", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://tile.openstreetmap.org/cgi-bin/export?bbox=0.0,0.0,0.40,0.40&scale=13867008.5226&format=png", "Mapnik export", "The Mapnik export service", 4, idx++, false);
			threads[idx] = alarmPageParallel("http://tah.openstreetmap.org/Tiles/tile/0/0/0.png", "tah", "", 1,idx++, false);
			threads[idx] = alarmPageParallel("http://nominatim.openstreetmap.org/reverse?format=xml&lat=52&lon=-2&zoom=1", "Nominatim", "Nominatim", 2,idx++, true);
			
			threads[idx] = alarmPageParallel("http://xapi.openstreetmap.org/api/0.6/map?bbox=0,0,0,0","XAPI (fafnir)","XAPI (fafnir)", 2,idx++, false);
			threads[idx] = alarmPageParallel("http://azure.openstreetmap.org/xapi/api/0.6/capabilities","java XAPI", "jXAPI", 2,idx++, false);
			threads[idx] = alarmPageParallel("http://api1.osm.absolight.net/api/0.6/map?bbox=0,0,0,0","TRAPI","TRAPI", 2,idx++, false);
			
			threads[idx] = alarmPageParallel("http://lists.openstreetmap.org/listinfo","List Archive", "The list archives", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://forum.openstreetmap.org", "Forum", "The forum", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://wiki.openstreetmap.org/", "Wiki","The wiki", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://help.openstreetmap.org/", "QA-help-system", "The QA-help pages", 1,idx++, true);
			
			threads[idx] = alarmPageParallel("http://dev.openstreetmap.org", "dev server", "The dev server", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://dev.openstreetmap.org/osm-healthcheck.php", "dev server - PHP", "The dev server PHP system", 1,idx++, true);

			threads[idx] = alarmPageParallel("http://foundation.openstreetmap.org/", "Foundation", "The foundation webpage", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://munin.openstreetmap.org", "Munin stats", "The munin stats", 1,idx++, true);
			threads[idx] = alarmPageParallel("http://donate.openstreetmap.org/", "Donate page", "The donate page", 1,idx++, true);
			
			
			//threads[idx] = alarmPageParallel(null, null, 3, idx++, true);
			//threads[24] = alarmPageParallel(testPage("http://tile.opencyclemap.org/cycle/16/31975/20453.png", "cyclemap");
			//threads[24] = alarmPageParallel(testPage("http://www.toolserver.org/tiles/osm/16/31975/20453.png", "toolserver tiles");

			//testPage("http://openstreetbugs.schokokeks.org/", "OpenStreetBugs");
			//testPage("http://openstreetbugs.schokokeks.org/api/0.1/getBugs?b=0&t=0&l=0&r=0", "OpenStreetBugs-API");
			
//			threads[idx] = alarmPageParallel("http://osmxapi.hypercube.telascience.org/api/0.6/map?bbox=0,0,0,0", "XAPI", "XAPI", 2,idx++, false);
			

			
			
			for (int i = 0; i < noServices; i++) {
				if (threads[i] != null) {
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					threads[i] = null;
				}
			}
			for (String s: msgs) reportString(s, outputToIRC);
			
			msg = "OSM healthcheck finished";
			reportString(msg, outputToIRC);
			
			resetCurrent();	
			
			fastCheck = false;
			int j = 0;
			for (int i : failedResponses) {
				if (i == 0) {
					if (reported[j]) {
						String ircmsg = "INFO: " + SERVICE[j] + " appears to be fine again";
						System.out.println("MSG to IRC: " + ircmsg);
						sendMessage(ircChanel, ircmsg);
						sendMessage(ircChanelVerbose, ircmsg);
						reported[j] = false;
					}
				}
				if (i > 0) fastCheck = true;
				if (i > 3) {
					if (!reported[j] || (System.currentTimeMillis() - reported_at[j] > 24*60*60*1000)) {
						String ircmsg = "WARNING:  " + SERVICE[j] + " appears to be down";
						System.out.println("MSG to IRC: " + ircmsg);
						sendMessage(ircChanel, ircmsg);
						sendMessage(ircChanelVerbose, ircmsg);
						reported[j] = true;
						reported_at[j] = System.currentTimeMillis();
					}
				}
				j++;
			}
			for (int i : slowResponses) {
				if (i > 0) fastCheck = true;
			}
			
			if (fastCheck) {
				recheck = 1;
			}
			synchronized (this) {
				runningTimmer = false;
				outputToIRC = false;
			}
		}
		
		public Thread alarmPage(int error, int category) {
			switch (error) {
			case -1:
				slowResponses[category]++;
				break;
			case 0:
				slowResponses[category] = 0;
				failedResponses[category] = 0;
				break;
			case 1:
				failedResponses[category]++;
				break;
			case 2:
				failedResponses[category]++;
				break;
			}
			return null;
		}
		
		
	}
	


	
	private Timer t;
	private CheckHealth tt;
	
	public OsmHealthCheck() {
		setVerbose(false);
		setAutoNickChange(true);
		setName("OSM-HealthCheck");
		try {
			connect(ircNetwork);
			identify("OSM-HealthCheckPwd3");
			joinChannel(ircChanel);
			joinChannel(ircChanelVerbose);
			System.out.println("Joined Channel");
		} catch (NickAlreadyInUseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IrcException e) {
			e.printStackTrace();
		}
		
		tt = new CheckHealth();
		t = new Timer();
		t.scheduleAtFixedRate(tt, 1, 60000);
		System.out.println("Setup schedule");
		
	}
	
	public void onDisconnect() {
		try {
			reconnect();
		} catch (NickAlreadyInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IrcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String mesage) {
		if (channel.equalsIgnoreCase(ircChanel)) {
			if (mesage.startsWith("!check")) {
				sendMessage(ircChanel, sender + ": OK will recheck. Give me a minute");
				sendMessage(ircChanel, " The full details are posted to channel " + ircChanelVerbose + " with a summary here");
				activateOutputToIRC = true;
				tt.recheck = 1;
			}
		}
	}
	
	public synchronized static void reportString(String msg, boolean outputToIRC) {
		if (msg == null) return;
		System.out.println(msg);
		if (bwCurrent != null) {
			try {
				bwCurrent.write(msg);
				bwCurrent.newLine();
			} catch (IOException ioe) {
				System.out.println("Could not write to current status file: " + ioe.getMessage());
			}
		}
		if (outputToIRC) { bot.sendMessage(ircChanel, msg); bot.sendMessage(ircChanelVerbose, msg); }
	}
	
	/**
	 * Prepare and reset the current log file for next set of outputs.
	 */
	public synchronized static void resetCurrent() {
		if (fnCurrent == null) return;
		try {
			if (bwCurrent != null) {
				bwCurrent.flush();
				bwCurrent.close();
			}
			File f = new File(fnCurrent + ".tmp");
			f.renameTo(new File(fnCurrent));
			f = new File(fnCurrent + ".tmp");
			f.createNewFile();
			bwCurrent = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
		} catch (IOException ioe) {
			bwCurrent = null;
		}
	}
	
	public static ReturnObject testReplicaDiffs() {

		String msg = "Nothing to report";
		ReturnObject ro = new ReturnObject();
		try {
			long t1 = System.currentTimeMillis();
			HttpURLConnection conn = (HttpURLConnection) new URL("http://planet.openstreetmap.org/minute-replicate/state.txt").openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			int respCode = conn.getResponseCode();
			long t2 = System.currentTimeMillis();
			switch (respCode) {
			case 200: {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(
								new BufferedInputStream(conn.getInputStream())));
				br.readLine();
				String seqNumStr = br.readLine();
				long seqNum = Integer.parseInt(seqNumStr.substring(seqNumStr
						.indexOf("=") + 1));
				br.readLine();
				String timeStamp = br.readLine();
				timeStamp = timeStamp.substring(timeStamp.indexOf('=') + 1);
				TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
				DateParser dp = new DateParser();
				Date maxDate = dp.parse(timeStamp);
				
				long outDated = new Date().getTime() - maxDate.getTime();
				
				DecimalFormat myFormat = new DecimalFormat("000");
				String url = "http://planet.openstreetmap.org/minute-replicate/"
					+ myFormat.format(seqNum/1000000) +"/" 
					+ myFormat.format((seqNum/1000) % 1000) + "/"
					+ myFormat.format(seqNum % 1000) + ".osc.gz";
				HttpURLConnection conn2 = (HttpURLConnection) new URL(url).openConnection();
				conn2.setConnectTimeout(10000);
				conn2.setReadTimeout(10000);
				int respCode2 = conn2.getResponseCode();
				switch (respCode2) {
				case 200: {
					if (outDated > 30*60*1000) {
						msg = "OUTDATED: Replica diffs:  (" + (t2 - t1) + " ms) Behind time: " + (outDated / 1000) + "s";
					} else {
						if (conn2.getContentLength() == 0) {
							msg = "FAIL:     Replica diffs: (" + (t2 - t1) + " ms) Reason: zero size response";
						} else {

							try {
								BufferedInputStream bis = new BufferedInputStream(new GZIPInputStream(conn2.getInputStream()));
								SAXParserFactory factory = SAXParserFactory.newInstance();
								// Parse the input
								factory.setValidating(false);
								SAXParser saxParser = factory.newSAXParser();
								saxParser.parse(bis, new DefaultHandler());
								msg = "OK:       Replica diffs: (" + (t2 - t1) + " ms) Behind time: " + (outDated / 1000) + "s";
							} catch (IOException e) {
								msg = "FAIL IO:  Replica diffs: (" + (t2 - t1) + " ms) Reason: " + e.getMessage();
							} catch (SAXException e) {
								msg = "FAIL XML: Replica diffs: (" + (t2 - t1) + " ms) Reason: " + e.getMessage();
							} catch (Exception e) {
								msg = "FAIL OTH: Replica diffs (" + (t2 - t1) + " ms) Reason: " + e.getMessage();
							}
						}

					}
					ro.msg = msg;
					if (outDated > 30*60*1000) {
						ro.error = 2;
						return ro;
					} else if (msg.startsWith("OK")){
						ro.error = 0;
						return ro;
					} else {
						ro.error = 1;
						return ro;
					}
				}
				default: {
					msg = "Replica diffs: FAIL (" + (t2 - t1) + " ms) " + respCode + " " + conn.getResponseMessage();
					break;
				}
				}
				break;
			}
			case 503: {
				msg = "Replica diffs: FAIL (" + (t2 - t1) + " ms) 503 " + conn.getResponseMessage();
				break;
			}
			default: {
				msg = "Replica diffs: FAIL (" + (t2 - t1) + " ms) " + respCode + " " + conn.getResponseMessage();
				break;
			}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			msg = "FAIL: IO  Replica Diffs " +  e.getMessage();
		} catch (Exception e) {
			msg = "FAIL:     Replica diffs: Reason: Something is different to normal ";
		}
		ro.msg = msg;
		ro.error = 1;
		return ro;
	}
	
	public static ReturnObject testXML(String url, String name) {
		String msg = "Nothing to report";
		ReturnObject ro = new ReturnObject();
		try {
			long t1 = System.currentTimeMillis();
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "OSM-HealthCheck");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(45000);
			int respCode = conn.getResponseCode();
			long t2 = System.currentTimeMillis();
			switch (respCode) {
			case 200: {
				BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
				try {
					SAXParserFactory factory = SAXParserFactory.newInstance();
					// Parse the input
					factory.setValidating(false);
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(bis, new DefaultHandler());
					msg = "OK:       " + name + " (" + (t2 - t1) + " ms)";
					ro.msg = msg;
					ro.error = 0;
					return ro;
				} catch (IOException e) {
					msg = "FAIL IO:  " + name + " (" + (t2 - t1) + " ms) Reason: " + e.getMessage();
				} catch (SAXException e) {
					msg = "FAIL XML: " + name + " (" + (t2 - t1) + " ms) Reason: " + e.getMessage();
				} catch (Exception e) {
					msg = "FAIL OTH:  " + name + " (" + (t2 - t1) + " ms) Reason: " + e.getMessage();
				}
				break;
			}
			case 503: {
				msg = "FAIL 503: " + name + " (" + (t2 - t1) + " ms) Reason: " + conn.getResponseMessage();
				break;
			}
			default: {
				msg = "FAIL + " + respCode + " " + name + " (" + (t2 - t1) + " ms) Reason: "  + conn.getResponseMessage();
				break;
			}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			msg = "FAIL: IO  " + name + " " +  e.getMessage();
		}
		ro.msg = msg;
		ro.error = 1;
		return ro;
	}
	
	public static ReturnObject testPage(String url, String name) {
		return testPageSlow(url, name, 10);
	}
	
	public static ReturnObject testPageSlow(String url, String name, int timeout) {
		String content = "";
		String msg = "Nothing to report";
		ReturnObject ro = new ReturnObject();
		try {
			long t1 = System.currentTimeMillis();
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestProperty("User-Agent", "OSM-HealthCheck");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(timeout * 1000);
			int respCode = conn.getResponseCode();
			long t2 = System.currentTimeMillis();
			switch (respCode) {
			case 200: {
				boolean slow = (t2 - t1) > 5000;
				if (slow) {
					msg = "SLOW:       " + name + " (" + (t2 - t1) + " ms)";
				} else {
					msg = "OK:       " + name + " (" + (t2 - t1) + " ms)";
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String tmp = br.readLine();
				while (tmp != null) {
					content = content + tmp;
					tmp = br.readLine();
				}
				ro.msg = msg;
				if (slow) {
					ro.error = -1;
					return ro;
				} else {
					ro.error = 0;
					return ro;
				}
			}
			case 503: {
				msg = "FAIL 503: " + name + " (" + (t2 - t1) + " ms) Reason: " + conn.getResponseMessage();
				break;
			}
			default: {
				msg = "FAIL + " + respCode + " " + name + " (" + (t2 - t1) + " ms) Reason: "  + conn.getResponseMessage();
				break;
			}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			msg = "FAIL: IO  " + name + " " +  e.getMessage();
		}
		ro.msg = msg;
		ro.error = 1;
		return ro;
	}
	
	public Thread alarmPageParallel(final String url, final String name, final String ircServiceName, final int type, final int category, final boolean alarm) {
		Thread worker = new Thread() {
			@Override
			public void run() {
				ReturnObject ro = null;
				switch (type) {
				case 1:
					ro = testPage(url, name);
					break;
				case 2:
					ro = testXML(url, name);
					break;
				case 3:
					ro = testReplicaDiffs();
					break;
				case 4:
					ro = testPageSlow(url, name,45);
					break;
				}
				msgs[category] = ro.msg;
				if (alarm) {
					switch (ro.error) {
					case -1:
						slowResponses[category]++;
						break;
					case 0:
						slowResponses[category] = 0;
						failedResponses[category] = 0;
						break;
					case 1:
						failedResponses[category]++;
						break;
					case 2:
						failedResponses[category]++;
						break;
					}
				}
			}
		};
		SERVICE[category] = ircServiceName;
		worker.start();
		try {
			Thread.sleep(200);
		} catch (InterruptedException ie) {
		}
		return worker;
	}
	
	
	
	
	public static void main (String [] args) {
		System.out.println("Starting up OSM health check");
		if (args.length > 0) {
			fnCurrent = args[0];
		}
		resetCurrent();
		bot = new OsmHealthCheck();
	}

}
