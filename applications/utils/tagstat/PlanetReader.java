import java.util.regex.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileReader;

public class PlanetReader {
	private static Connection conn = null;
	private static Pattern pattern;

	private static void handleTag(String key, String value) {
		if(key.equals("created_by")) {
			return;
		}
		try {
			Statement s = conn.createStatement();
			int count = s.executeUpdate("UPDATE tags SET count=count+1 WHERE tag=\""+key+"\" AND value=\""+value+"\"");
			if(count != 1) {
				s.executeUpdate("INSERT INTO tags SET tag=\""+key+"\", value=\""+value+"\", count=1");
			}
			s.close();
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	private static void handleLine(String line) {
		if(line.lastIndexOf("tag") > 0) {
			Matcher matcher = pattern.matcher(line);
			if(matcher.matches()) {
				handleTag(matcher.group(1), matcher.group(2));
			}
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

	private static String getLine(BufferedReader from) {
		String line;
		try {
			line = from.readLine();
		} catch (IOException ex) {
			line = null;
		}
		return line;
	}

	private static BufferedReader getInput(String filename) {
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found");
			System.exit(5);
		}
		return input;
	}

	public static void main(String[] args) {
		getConnection("localhost", "tagstat", "tagstat", "iyZscbZU");
		pattern = Pattern.compile(" *<tag k=['\"](.*)['\"] v=['\"](.*)['\"].*");
		BufferedReader input = getInput(args[0]);
		String line;

		line = "";
		while(line != null) {
			handleLine(line);
			line = getLine(input);
		}
	}
}

