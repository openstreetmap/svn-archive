import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

public class OsmMapCallValidator {

	static public HashMap<Long, Node> nodes;
	static public HashMap<Long, Way> ways;
	static public HashMap<Long, Relation> relations;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String base_url1 = args[0];
		String base_url2 = args[1];
		boolean singleQuery = false;
		float min_lat = Float.parseFloat(args[3]); // 18.20f;
		float min_lon = Float.parseFloat(args[2]); // -73.00f;
		float max_lat = Float.parseFloat(args[5]); // 19.70f;
		float max_lon = Float.parseFloat(args[4]); // -71.50f;
		
		if (args.length > 6)
			singleQuery = true;

		Random ran = new Random();

		System.out.println("Starting OsmMapCallValidator");
		System.out.println("Generating requests in a bbox of (" + min_lon + ","
				+ min_lat + "," + max_lon + "," + max_lat + ")");

		try {
			for (int ii = 0; ii < 1000; ii++) {
				if (singleQuery && (ii > 0))
					break;
				System.out.println("** Iteration " + ii + " **");
				nodes = new HashMap<Long, Node>();
				ways = new HashMap<Long, Way>();
				relations = new HashMap<Long, Relation>();

				String request_uri = null;
				if (singleQuery) {
					request_uri = "/api/0.6/map?bbox=" + min_lon + ","
					+ min_lat + "," + max_lon + "," + max_lat;
				} else {
				float base_lat = ran.nextFloat() * (max_lat - min_lat)
						+ min_lat;
				float base_lon = ran.nextFloat() * (max_lon - min_lon)
						+ min_lon;
				request_uri = "?bbox=" + base_lon + ","
						+ base_lat + "," + (base_lon + 0.25 * ran.nextFloat())
						+ "," + (base_lat + 0.25 * ran.nextFloat());
				}

				long start_t1 = System.currentTimeMillis();
				System.out.println("Quering " + base_url1 + request_uri);
				BufferedInputStream bis1 = null;
				try {
					bis1 = new BufferedInputStream(new URL(base_url1
							+ request_uri).openStream());
				} catch (IOException ioe) {
					if (ioe.getMessage().contains("HTTP response code: 400")) {
						System.out
								.println("API returned bad request, probably due to too many nodes");
						continue;
					} else {
						ioe.printStackTrace();
						System.exit(2);
					}
				}
				MapParserBasis mpb = new MapParserBasis(bis1);
				long end_t1 = System.currentTimeMillis();
				System.out.println("Request + parsing took "
						+ ((end_t1 - start_t1) / 1000.0f) + "s");

				long start_t2 = System.currentTimeMillis();
				System.out.println("Quering " + base_url2 + request_uri);
				BufferedInputStream bis2 = null;
				try {
					bis2 = new BufferedInputStream(new URL(base_url2
							+ request_uri).openStream());
				} catch (IOException ioe) {
					if (ioe.getMessage().contains("HTTP response code: 400")) {
						System.out
								.println("WARNING! API returned bad request, probably due to too many nodes");
						continue;
					} else {
						ioe.printStackTrace();
						System.exit(2);
					}
				}
				MapParserComparator mpc = new MapParserComparator(bis2);
				long end_t2 = System.currentTimeMillis();
				System.out.println("Request + parsing took "
						+ ((end_t2 - start_t2) / 1000.0f) + "s");

				for (Node n : nodes.values()) {
					if (!n.validated) {
						System.out.println("Error: Node " + n.id + " missing!");
					}
				}

				for (Way w : ways.values()) {
					if (!w.validated) {
						System.out.println("Error: Way " + w.id + " missing!");
					}
				}

				for (Relation r : relations.values()) {
					if (!r.validated) {
						System.out.println("Error: Relation " + r.id
								+ " missing!");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Finished Validation");

	}

}
