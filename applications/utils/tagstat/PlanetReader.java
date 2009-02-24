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

public class PlanetReader extends DefaultHandler {
	private static Connection conn = null;

	public void startElement (String uri, String name, String qName, Attributes atts) {
		if(qName.equals("tag")) {
			handleTag(StringEscapeUtils.escapeSql(atts.getValue("k")), StringEscapeUtils.escapeSql(atts.getValue("v")));
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
			System.err.println("INSERT INTO tagpairs SET tag='"+key+"', value='"+value+"', count=0, newcount="+inc);
			System.err.println("SQLException: " + ex.getMessage());
			System.err.println("SQLState: " + ex.getSQLState());
			System.err.println("VendorError: " + ex.getErrorCode());
		}
	}

	private static void handleTag(String key, String value) {
		if(key.equals("created_by")) {
			return;
		}
		incrementInDB(key, value, 1);
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
		s.executeUpdate("ALTER TABLE tagpairs ADD COLUMN newcount INT DEFAULT 0 AFTER count");

		XMLReader xr = XMLReaderFactory.createXMLReader();
		PlanetReader handler = new PlanetReader();
		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);

		FileReader r = new FileReader(args[0]);
		xr.parse(new InputSource(r));

		s.executeUpdate("UPDATE tagpairs SET count=newcount");
		s.executeUpdate("ALTER TABLE tagpairs DROP COLUMN newcount");

	}

	public PlanetReader() {
		super();
	}
}
