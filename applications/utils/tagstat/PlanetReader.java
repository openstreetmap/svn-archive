import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileReader;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.apache.commons.lang.StringEscapeUtils;
import java.util.*;
import java.io.*;

public class PlanetReader extends DefaultHandler {
	private static Connection conn = null;
	private static HashMap cache = new HashMap();
	private static int cachesize = 0;

	private static void incrementInHashMap(String key, String value, int inc) {
		HashMap t;
		if(cache.containsKey(key)) {
			t = (HashMap)cache.get(key);
			if(t.containsKey(value)) {
				int count = ((Integer)t.get(value)).intValue();
				t.put(value, new Integer(count +inc));
			} else {
				cachesize++;
				t.put(value, new Integer(inc));
			}
		} else {
			cachesize++;
			t = new HashMap();
			t.put(value, new Integer(inc));
		}
		cache.put(key, t);
	}

	private static void flushHashMap() {
		Set keyset = cache.entrySet();
		Iterator i = keyset.iterator();
		while(i.hasNext()){
			Map.Entry m = (Map.Entry)i.next();
			String key = (String)m.getKey();
			HashMap values = (HashMap)m.getValue();
			Set valueset = values.entrySet();
			Iterator j = valueset.iterator();
			while(j.hasNext()){
				Map.Entry n = (Map.Entry)j.next();
				String value = (String)n.getKey();
				int count = ((Integer)n.getValue()).intValue();
				incrementInDB(key, value, count);
			}
			values.clear();
		}
		cache.clear();
		cachesize = 0;
	}

	public void startElement (String uri, String name, String qName, Attributes atts) {
		if(qName.equals("tag")) {
			//handleTag(StringEscapeUtils.escapeSql(atts.getValue("k")), StringEscapeUtils.escapeSql(atts.getValue("v")));
			handleTag(atts.getValue("k"), atts.getValue("v"));
		}
	}

	private static void incrementInDB(String key, String value, int inc) {
		try {
			Statement s = conn.createStatement();
			int count = s.executeUpdate("UPDATE tagpairs SET newcount=newcount+"+inc+" WHERE tag='"+key+"' AND value='"+value+"'");
			if(count != 1) {
				s.executeUpdate("INSERT INTO tagpairs SET tag='"+key+"', value='"+value+"', count=0, newcount="+inc);
			}
			s.close();
		} catch (SQLException ex) {
			if(ex.getSQLState() == "22001") { // ER_DATA_TOO_LONG
				System.err.println("INSERT INTO tagpairs SET tag='"+key+"', value='snip...too long...', count=0, newcount="+inc);
				System.err.println("SQLException: " + ex.getMessage());
				System.err.println("SQLState: " + ex.getSQLState());
				System.err.println("VendorError: " + ex.getErrorCode());
			} else {
				System.err.println("INSERT INTO tagpairs SET tag='"+key+"', value='"+value+"', count=0, newcount="+inc);
				System.err.println("SQLException: " + ex.getMessage());
				System.err.println("SQLState: " + ex.getSQLState());
				System.err.println("VendorError: " + ex.getErrorCode());
			}
		}
	}

	private static void handleTag(String key, String value) {
		//if(key.equals("created_by")) {
		//	return;
		//}
		incrementInHashMap(key, value, 1);
		if(cachesize > 1000000) {
			flushHashMap();
		}
	}

	private static void getConnection(String host, String db, String user, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://"+host+"/"+db+"", user, password);
		} catch (ClassNotFoundException ex) {
			System.err.println("Class com.mysql.jdbc.Driver not found");
			System.exit(1);
		} catch (InstantiationException ex) {
			System.err.println("Can't instantiate com.mysql.jdbc.Driver");
			System.exit(2);
		} catch (IllegalAccessException ex) {
			System.err.println("Illegal Access to com.mysql.jdbc.Driver");
			System.exit(3);
		} catch (SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("VendorError: " + ex.getErrorCode());
			System.exit(4);
		}
	}

	public static void main (String args[]) throws Exception {
		getConnection("localhost", "tagstat", "tagstat", "iyZscbZU");

		Statement s = conn.createStatement();
		//s.executeUpdate("SET NAMES utf8");
		s.executeUpdate("ALTER TABLE tagpairs ADD COLUMN newcount INT DEFAULT 0 AFTER count");

		XMLReader xr = XMLReaderFactory.createXMLReader();
		PlanetReader handler = new PlanetReader();
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);

		//FileReader r = new FileReader(args[0]);
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		xr.parse(new InputSource(r));

		flushHashMap();
		s.executeUpdate("UPDATE tagpairs SET count=newcount");
		s.executeUpdate("ALTER TABLE tagpairs DROP COLUMN newcount");
		s.executeUpdate("DELETE FROM tagpairs WHERE count=0");

		s.executeUpdate("DELETE FROM tags");
		s.executeUpdate("INSERT INTO tags (tag, uses, uniq_values) SELECT p.tag, SUM(p.count), COUNT(*) FROM tagpairs p GROUP BY tag;");
	}

	public PlanetReader() {
		super();
	}
}
