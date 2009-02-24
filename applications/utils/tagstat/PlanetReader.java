import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import java.util.*;
import java.io.*;

public class PlanetReader extends DefaultHandler {
	private int mode = 3;
	private DBCache tagpairs;
	private DBCache tagfriends;
	private Connection conn;
	private HashSet seenTags;

	private static final int TAG_NODE = 0;
	private static final int TAG_WAY = 1;
	private static final int TAG_RELATION = 2;
	private static final int TAG_OTHER   = 3;

	private class DBCache {
		private HashMap cache;
		private PreparedStatement us;
		private PreparedStatement is;
		public int cachesize;

		DBCache(Connection conn, String update, String insert) {
			cache = new HashMap();
			cachesize = 0;
			try {
				us = conn.prepareStatement(update);
				is = conn.prepareStatement(insert);
			} catch (SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
				System.err.println("SQLState: " + ex.getSQLState());
				System.err.println("VendorError: " + ex.getErrorCode());
				System.exit(5);
			}
		}

		public void increment(String key, String value, int inc, int mode) {
			HashMap t;
			if(cache.containsKey(key)) {
				t = (HashMap)cache.get(key);
				if(t.containsKey(value)) {
					int count[] = (int[])t.get(value);
					count[mode] += inc;
					t.put(value, count);
				} else {
					cachesize++;
					int count[] = new int[4];
					count[mode] = inc;
					t.put(value, count);
				}
			} else {
				cachesize++;
				t = new HashMap();
				int count[] = new int[4];
				count[mode] = inc;
				t.put(value, count);
			}
			cache.put(key, t);
		}

		public void flush() {
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
					int count[] = (int[])n.getValue();
					incrementInDB(key, value, count);
				}
				values.clear();
			}
			cache.clear();
			cachesize = 0;
		}

		private void incrementInDB(String key, String value, int inc[]) {
			int count;
			try {
				us.setInt(1, inc[TAG_NODE]);
				us.setInt(2, inc[TAG_WAY]);
				us.setInt(3, inc[TAG_RELATION]);
				us.setInt(4, inc[TAG_OTHER]);
				us.setString(5, key);
				us.setString(6, value);
				count = us.executeUpdate();
				if(count != 1) {
					is.setString(1, key);
					is.setString(2, value);
					is.setInt(3, inc[TAG_NODE]);
					is.setInt(4, inc[TAG_WAY]);
					is.setInt(5, inc[TAG_RELATION]);
					is.setInt(6, inc[TAG_OTHER]);
					is.executeUpdate();
				}
			} catch (SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
				System.err.println("SQLState: " + ex.getSQLState());
				System.err.println("VendorError: " + ex.getErrorCode());
			}
		}
	}

	public void endElement(String uri, String name, String qName) {
		if(qName.equals("node") || qName.equals("way") || qName.equals("relation")) {
			if(seenTags != null) {
				handleTagFriends(seenTags, mode);
			}
			mode=TAG_OTHER;
			seenTags = null;
		}
	}

	public void startElement(String uri, String name, String qName, Attributes atts) {
		if(qName.equals("node")) {
			if(seenTags != null) {
				System.err.println("Dangling tagSeen found at "+uri);
				System.exit(6);
			} else {
				seenTags = new HashSet();
			}
			mode=TAG_NODE;
		}
		if(qName.equals("way")) {
			if(seenTags != null) {
				System.err.println("Dangling tagSeen found at "+uri);
				System.exit(6);
			} else {
				seenTags = new HashSet();
			}
			mode=TAG_WAY;
		}
		if(qName.equals("relation")) {
			mode=TAG_RELATION;
			if(seenTags != null) {
				System.err.println("Dangling tagSeen found at "+uri);
				System.exit(6);
			} else {
				seenTags = new HashSet();
			}
		}
		if(qName.equals("tag")) {
			handleTag(atts.getValue("k"), atts.getValue("v"), mode);
			if(seenTags == null) {
				System.err.println("Missing tagSeen found at "+uri);
				System.exit(7);
			} else {
				seenTags.add(atts.getValue("k"));
			}
		}
	}

	private void handleTag(String key, String value, int mode) {
		tagpairs.increment(key, value, 1, mode);
		if(tagpairs.cachesize > 500000) {
			tagpairs.flush();
		}
	}

	private void handleTagFriends(HashSet friend, int mode) {
		Iterator outer = friend.iterator();
		while(outer.hasNext()) {
			String tag1 = (String)outer.next();
			Iterator inner = friend.iterator();
			while(inner.hasNext()) {
				String tag2 = (String)inner.next();
				if(tag1.compareTo(tag2) < 0) {
					tagfriends.increment(tag1, tag2, 1, mode);
				}
			}
		}
		if(tagfriends.cachesize > 500000) {
			tagfriends.flush();
		}
	}

	private Connection getConnection(String host, String db, String user, String password) {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			return DriverManager.getConnection("jdbc:mysql://"+host+"/"+db+"", user, password);
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
		return null;
	}

	public static void main (String args[]) throws Exception {
		PlanetReader p = new PlanetReader();

		Statement s = p.conn.createStatement();
		s.executeUpdate("ALTER TABLE tagpairs ADD COLUMN nc_node INT DEFAULT 0, ADD COLUMN nc_way INT DEFAULT 0, ADD COLUMN nc_relation INT DEFAULT 0, ADD COLUMN nc_other INT DEFAULT 0 AFTER c_total");
		s.executeUpdate("ALTER TABLE tagfriends ADD COLUMN nc_node INT DEFAULT 0, ADD COLUMN nc_way INT DEFAULT 0, ADD COLUMN nc_relation INT DEFAULT 0, ADD COLUMN nc_other INT DEFAULT 0 AFTER c_total");

		XMLReader xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(p);
		xr.setErrorHandler(p);

		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		xr.parse(new InputSource(r));

		p.tagpairs.flush();
		p.tagfriends.flush();

		// Update tagpairs table
		s.executeUpdate("UPDATE tagpairs SET c_node=nc_node, c_way=nc_way, c_relation=nc_relation, c_other=nc_other, c_total=nc_node+nc_way+nc_relation+nc_other");
		s.executeUpdate("ALTER TABLE tagpairs DROP COLUMN nc_node, DROP COLUMN nc_way, DROP COLUMN nc_relation, DROP COLUMN nc_other");
		s.executeUpdate("DELETE FROM tagpairs WHERE c_total=0");

		// Update tagfriends table
		s.executeUpdate("UPDATE tagfriends SET c_node=nc_node, c_way=nc_way, c_relation=nc_relation, c_other=nc_other, c_total=nc_node+nc_way+nc_relation+nc_other");
		s.executeUpdate("ALTER TABLE tagfriends DROP COLUMN nc_node, DROP COLUMN nc_way, DROP COLUMN nc_relation, DROP COLUMN nc_other");
		s.executeUpdate("DELETE FROM tagfriends WHERE c_total=0");

		// Update tags table
		s.executeUpdate("DELETE FROM tags");
		s.executeUpdate("INSERT INTO tags (tag, uses, uniq_values) SELECT p.tag, SUM(p.c_total), COUNT(*) FROM tagpairs p GROUP BY tag;");
	}

	public PlanetReader() {
		super();
		conn = getConnection("localhost", "tagstat", "tagstat", "iyZscbZU");
		tagpairs = new DBCache(conn, "UPDATE tagpairs SET nc_node=(nc_node+?), nc_way=(nc_way+?), nc_relation=(nc_relation+?), nc_other=(nc_other+?) WHERE tag=? AND value=?", "INSERT INTO tagpairs SET tag=?, value=?, c_node=0, c_way=0, c_relation=0, c_other=0, c_total=0, nc_node=?, nc_way=?, nc_relation=?, nc_other=?");
		tagfriends = new DBCache(conn, "UPDATE tagfriends SET nc_node=(nc_node+?), nc_way=(nc_way+?), nc_relation=(nc_relation+?), nc_other=(nc_other+?) WHERE tag1=? AND tag2=?", "INSERT INTO tagfriends SET tag1=?, tag2=?, c_node=0, c_way=0, c_relation=0, c_other=0, c_total=0, nc_node=?, nc_way=?, nc_relation=?, nc_other=?");
	}
}
