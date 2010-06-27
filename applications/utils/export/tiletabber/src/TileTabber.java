import java.io.*;

/**
 * Main logic for TileTabber 
 * 
 * See README for more details
 * 
 * @author Harry Wood 
 * Public domain
 */
public class TileTabber {

	//Main logic
	public TileTabber(int tilexmin,
			          int tilexmax,
			          int tileymin,
			          int tileymax,
			          int tilez,
			          String tileurl,
			          boolean nolimit ) {
		
		System.out.println( "tilexmin=" + tilexmin + " "+
				            "tilexmax=" + tilexmax + " "+
				            "tileymin=" + tileymin + " "+
				            "tileymax=" + tileymax + " "+
				            "tilez=" + tilez + " ");

		int tileCount = (tilexmax-tilexmin+1)*(tileymax-tileymin+1);
		System.out.println( tileCount + " tiles in the specified area" );
		
		if (!nolimit && tileCount>500) {
			System.err.println("Area too big (override limit with 'nolimit' argument)");
			System.exit(-1);
		}
		
		for (int x=tilexmin; x<=tilexmax; x++) {
			for (int y=tileymin; y<=tileymax; y++) {
				
				String tileURL = tileurl + tilez + "/" + x + "/" + y + ".png";
				String filename = tilez + "-" + x + "-" + y;

				System.out.println("Downloading tile "+ tileURL);
				downloadFile(tileURL, "./output/" + filename + ".png");

				double northlat = tile2lat(y, tilez);
				double southlat = tile2lat(y + 1, tilez);
				double westlon  = tile2lon(x, tilez);
				double eastlon  = tile2lon(x + 1, tilez);
				

				System.out.println(northlat + " " + southlat + " " + westlon + " " + eastlon );

				double[] p1CoordSW = OSGB36.LatLon2OSGB(southlat,westlon);
				double[] p2CoordNW = OSGB36.LatLon2OSGB(northlat,westlon);
				double[] p3CoordNE = OSGB36.LatLon2OSGB(northlat,eastlon);

				int p1CoordSWnorthing   = (int) Math.round( p1CoordSW[0] );
				int p1CoordSWeasting    = (int) Math.round( p1CoordSW[1] );
				int p2CoordNWnorthing   = (int) Math.round( p2CoordNW[0] );
				int p2CoordNWeasting    = (int) Math.round( p2CoordNW[1] );
				int p3CoordNEnorthing   = (int) Math.round( p3CoordNE[0] );
				int p3CoordNEeasting    = (int) Math.round( p3CoordNE[1] );
				
			
				String tabdata = "!table\n"+
				                 "!version 300\n"+
				                 "!charset WindowsLatin1\n"+
				                 "\n"+
				                 "Definition Table\n"+
				                 "  File \"" + filename + ".png\"\n"+
				                 "  Type \"RASTER\"\n" + 
				                 "  (" + p1CoordSWeasting +"," + p1CoordSWnorthing +") (0,256) Label \"Pt 1\",\n" +
				                 "  (" + p2CoordNWeasting +"," + p2CoordNWnorthing +") (0,0) Label \"Pt 2\",\n" +
				                 "  (" + p3CoordNEeasting +"," + p3CoordNEnorthing +") (256,0) Label \"Pt 3\"\n" +
				                 "  CoordSys Earth Projection 8, 79, \"m\", -2, 49, 0.9996012717, 400000, -100000\n" +
				                 "  Units \"m\"\n";

				System.out.println("writing TAB file: " + filename + ".TAB");
				outputStringToFile(tabdata, "./output/" + filename + ".TAB");

				tileCount--;
				if (tileCount % 50 == 0) {
					System.out.println("Sleeping 5 seonds (Give the tileserver a break)");
					try { Thread.sleep(5000); } catch (InterruptedException e) { e.printStackTrace(); }
				}

			} //next y
		} //next x


	}
	
	
	// courtesy of http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#compute_bounding_box_for_tile_number
	static double tile2lon(int x, int z) {
		return x / Math.pow(2.0, z) * 360.0 - 180;
	}

	static double tile2lat(int y, int z) {
		double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
		return Math.toDegrees(Math.atan(Math.sinh(n)));
	}
	
	
	static void downloadFile(String remoteURL, String localFilename) 
	{
		try {
			java.io.BufferedInputStream in = new java.io.BufferedInputStream(
					new java.net.URL(remoteURL).openStream()
			);

			java.io.FileOutputStream fos = new java.io.FileOutputStream(localFilename);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);

			byte[] data = new byte[1024];

			int x=0;

			while((x=in.read(data,0,1024))>=0)
			{
				bout.write(data,0,x);
			}

			bout.close();

			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	static void outputStringToFile(String str, String filename) {
		try {
			java.io.FileOutputStream fos = new java.io.FileOutputStream(filename);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
			bout.write(str.getBytes());
			bout.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	
	
	//Parse command line args and run an instance of TileTabber
	public static void main (String args[]) {
	
		String usage ="usage:\n" +
		              "  java TileTabber tilexmin=[int] tilexmax=[int] tileymin=[int] tileymax=[int] tileurl=[http://tile.openstreetmap.org/]";
		
		int tilexmin = -1;
		int tilexmax = -1;
		int tileymin = -1;
		int tileymax = -1;
		int tilez = 16;
		String tileurl = "http://tile.openstreetmap.org/";
		boolean nolimit = false;
		
		for (int i=0; i<args.length; i++ ) {
			try {
				if (args[i].equalsIgnoreCase("tilexmin")) tilexmin = Integer.parseInt(args[i+1]);
				if (args[i].equalsIgnoreCase("tilexmax")) tilexmax = Integer.parseInt(args[i+1]);
				if (args[i].equalsIgnoreCase("tileymin")) tileymin = Integer.parseInt(args[i+1]);
				if (args[i].equalsIgnoreCase("tileymax")) tileymax = Integer.parseInt(args[i+1]);
				if (args[i].equalsIgnoreCase("tileurl"))  tileurl = args[i+1];
				if (args[i].equalsIgnoreCase("nolimit"))  nolimit = true;
				
			} catch (NumberFormatException nfe) {
				System.err.println("Invalid integer\n" + usage);
				System.exit(-1);
			}
		}
		
		if (tilexmin==-1 || tilexmax==-1 || tileymin==-1 || tileymax==-1) {
			System.err.println("Missing params\n" + usage);
			System.exit(-1);
		}
		
		new TileTabber(tilexmin,tilexmax,tileymin,tileymax,tilez,tileurl,nolimit);
		
	}
}
