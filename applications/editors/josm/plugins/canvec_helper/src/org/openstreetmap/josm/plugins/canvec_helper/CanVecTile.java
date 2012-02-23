package org.openstreetmap.josm.plugins.canvec_helper;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import java.awt.Point;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.MirroredInputStream;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.openstreetmap.josm.io.OsmImporter;
import org.openstreetmap.josm.io.OsmImporter.OsmImporterData;

public class CanVecTile {
	canvec_helper plugin_self;
	private ArrayList<String> sub_tile_ids = new ArrayList<String>();
	private boolean zip_scanned = false;
	
	private ArrayList<CanVecTile> sub_tiles = new ArrayList<CanVecTile>();
	private boolean sub_tiles_made = false;

	private ArrayList<String> index;
	private int depth;
	
	int corda,cordc;
	private boolean valid = false;
	String cordb,cordd;
	private Bounds bounds;
	private String tileid;
	public CanVecTile(String tileid,canvec_helper self) {
		String parta,partb,partc,partd;
		parta = tileid.substring(0,3);
		partb = tileid.substring(3, 4);
		partc = tileid.substring(4, 6);
		partd = tileid.substring(6);
		int a,c;
		a = Integer.parseInt(parta);
		c = Integer.parseInt(partc);
		real_init(a,partb,c,partd,self,new ArrayList<String>());
	}
	public CanVecTile(int a,String b,int c,String d,canvec_helper self,ArrayList<String> index) {
		real_init(a,b,c,d,self,index);
	}
	public void real_init(int a,String b,int c,String d,canvec_helper self, ArrayList<String> index) {
		this.index = index;
		plugin_self = self;
		corda = a;
		cordb = b;
		cordc = c;
		cordd = d;
		double zero_point_lat,zero_point_lon;
		double lat_span,lon_span;
		double lat2,lon2;
		if ((a >= 0) && (a <= 119)) { // main block of tiles
			int column = a / 10;
			int row = a % 10;
			if (row > 6) {
				// cant handle x7 x8 and x9 yet
				return;
			}
			zero_point_lat = 40 + 4 * row;
			zero_point_lon = -56 - 8 * column;
		
			// size of each grid
			if (row <= 6) {
				// each is 4x8 degrees, broken into a 4x4 grid
				lat_span = 4;
				lon_span = 8;
				depth = 1;
			} else {
				return;
			}
		} else { // last few tiles, very far north
			return;
		}

		// a 4x4 grid of A thru P
		// map A-P to 1-16
		int grid2;
		if (b == "") grid2 = 0;
		else grid2 = b.charAt(0) - 64;
		int rows1[] = { 0, 0,0,0,0, 1,1,1,1, 2,2,2,2, 3,3,3,3 };
		int cols1[] = { 0, 3,2,1,0, 0,1,2,3, 3,2,1,0, 0,1,2,3 };
		lat2 = zero_point_lat + (lat_span/4)*rows1[grid2];
		lon2 = zero_point_lon + (lon_span/4)*cols1[grid2];

		if (grid2 != 0) {
			lat_span = lat_span / 4;
			lon_span = lon_span / 4;
			depth = 2;
		}

		int rows3[] = { 0, 0,0,0,0, 1,1,1,1, 2,2,2,2, 3,3,3,3 };
		lat2 = lat2 + (lat_span/4)*rows3[c];
		int cols3[] = { 0, 3,2,1,0, 0,1,2,3, 3,2,1,0, 0,1,2,3 };
		lon2 = lon2 + (lon_span/4)*cols3[c];

		if (c != 0) {
			lat_span = lat_span / 4;
			lon_span = lon_span / 4;
			depth = 3;
		}
		
		if (cordd != "") {
			depth = 4;
			System.out.println("cordd: "+cordd);
			String foo[] = cordd.split("\\.");
			for (int i = 0; i < foo.length; i++) {
				int cell;
				System.out.println(foo[i]);
				if (foo[i] == "osm") break;
				if (foo[i] == "") continue;
				try {
					cell = Integer.parseInt(foo[i]);
				} catch (NumberFormatException e) {
					continue;
				}
				switch (cell) {
				case 0:
					break;
				case 1:
					lat2 = lat2 + lat_span/2;
					break;
				case 2:
					lat2 = lat2 + lat_span/2;
					lon2 = lon2 + lon_span/2;
					break;
				case 3:
					lon2 = lon2 + lon_span/2;
					break;
				}
				lat_span = lat_span/2;
				lon_span = lon_span/2;
			}
		}

		bounds = new Bounds(lat2,lon2,lat2+lat_span,lon2+lon_span);
		if (cordb == "") this.tileid = String.format("%03d",corda);
		else if (cordc == 0) this.tileid = String.format("%03d%s",corda,cordb);
		else if (cordd == "") this.tileid = String.format("%03d%s%02d",corda,cordb,cordc);
		else this.tileid = String.format("%03d%s%02d%s",corda,cordb,cordc,cordd);
		valid = true;
		//debug(index.toString());
		//debug("creating tileid: "+this.tileid);
	}
	public boolean isValid() { return valid; }
	public String getTileId() {
		return this.tileid;
	}
	private void debug(String line) {
		System.out.println(depth + "_" + tileid + ": " + line);
	}
	public boolean isVisible(Bounds view) {
		return view.intersects(bounds);
	}
	public Point[] getCorners(MapView mv) {
		LatLon min = bounds.getMin();
		LatLon max = bounds.getMax();
		LatLon x1 = new LatLon(min.lat(),max.lon());
		LatLon x2 = new LatLon(max.lat(),min.lon());
		return new Point[] {
			mv.getPoint(min), // south west
			mv.getPoint(x1),
			mv.getPoint(max),
			mv.getPoint(x2) // north west
			};
	}
	public String getDownloadUrl() {
		return String.format("http://ftp2.cits.rncan.gc.ca/osm/pub/%1$03d/%2$s/%1$03d%2$s%3$02d.zip",corda,cordb,cordc);
	}
	private ZipFile open_zip() throws IOException {
		File download_path = new File(plugin_self.getPluginDir() + File.separator);
		download_path.mkdir();
		MirroredInputStream tile_zip;
		tile_zip = new MirroredInputStream(getDownloadUrl(),download_path.toString());
		return new ZipFile(tile_zip.getFile());
	}
	public void downloadSelf() {
		if (zip_scanned) return;
		ZipFile zipFile;
		try {
			zipFile = open_zip();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			sub_tile_ids.add(entry.getName());
			zip_scanned = true;
			CanVecTile final_tile = new CanVecTile(entry.getName(),plugin_self);
			if (final_tile.isValid()) sub_tiles.add(final_tile);
		}
	}
	private void load_raw_osm() {
		ZipFile zipFile;
		try {
			zipFile = open_zip();
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				System.out.println(entry.getName());
				if (false) {
					InputStream rawtile = zipFile.getInputStream(entry);
					OsmImporter importer = new OsmImporter();
					System.out.println("loading raw osm");
					OsmImporterData temp = importer.loadLayer(rawtile, null, entry.getName(), null);
					Main.worker.submit(temp.getPostLayerTask());
					Main.main.addLayer(temp.getLayer());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (IllegalDataException e) {
			e.printStackTrace();
			return;
		}
	}
	private void make_sub_tiles(int layer) {
		ArrayList<String> buffer = new ArrayList<String>();
		Pattern p;
		if (sub_tiles_made) return;
		switch (layer) {
		case 1:
			p = Pattern.compile("\\d\\d\\d([A-Z]).*");
			String last_cell = "";
			for (int i = 0; i < index.size(); i++) {
				Matcher m = p.matcher(index.get(i));
				m.matches();

				String cell = m.group(1);
				if (cell.equals(last_cell)) {
					buffer.add(m.group(0));
				} else if (last_cell == "") {
					buffer.add(m.group(0));
				} else {
					sub_tiles.add(new CanVecTile(corda,last_cell,0,"",plugin_self,buffer));
					buffer = new ArrayList<String>();
					buffer.add(m.group(0));
				}
				last_cell = cell;
			}
			sub_tiles.add(new CanVecTile(corda,last_cell,0,"",plugin_self,buffer));
			break;
		case 2:
			p = Pattern.compile("\\d\\d\\d[A-Z](\\d\\d).*");
			int last_cell2 = -1;
			for (int i = 0; i < index.size(); i++) {
				Matcher m = p.matcher(index.get(i));
				m.matches();

				int cell = Integer.parseInt(m.group(1));
				if (cell == last_cell2) {
					buffer.add(m.group(0));
				} else if (last_cell2 == -1) {
					buffer.add(m.group(0));
				} else {
					sub_tiles.add(new CanVecTile(corda,cordb,last_cell2,"",plugin_self,buffer));
					buffer = new ArrayList<String>();
					buffer.add(m.group(0));
				}
				last_cell2 = cell;
			}
			if (last_cell2 != -1) sub_tiles.add(new CanVecTile(corda,cordb,last_cell2,"",plugin_self,buffer));
			break;
		}
		sub_tiles_made = true;
	}
	public void paint(Graphics2D g, MapView mv, Bounds bounds, int max_zoom) {
		boolean show_sub_tiles = false;
		if (!isVisible(bounds)) return;
		if ((depth == 3) && (bounds.getArea() < 0.5)) { // 022B01
			if (max_zoom == 4) downloadSelf();
			show_sub_tiles = true;
		} else if ((depth == 2) && (bounds.getArea() < 20)) { // its a layer2 tile
			make_sub_tiles(2);
			show_sub_tiles = true;
		} else if ((depth == 1) && (bounds.getArea() < 40)) { // its a layer1 tile and zoom too small
			// draw layer2 tiles for self
			make_sub_tiles(1);
			show_sub_tiles = true;
		}
		if (show_sub_tiles && (depth < max_zoom)) {
			for (int i = 0; i < sub_tiles.size(); i++) {
				CanVecTile tile = sub_tiles.get(i);
				tile.paint(g,mv,bounds,max_zoom);
			}
		} else {
			Point corners[] = getCorners(mv);
			int xs[] = { corners[0].x, corners[1].x, corners[2].x, corners[3].x };
			int ys[] = { corners[0].y, corners[1].y, corners[2].y, corners[3].y };
			Polygon shape = new Polygon(xs,ys,4);
			g.draw(shape);
			g.drawString(getTileId(),corners[0].x,corners[0].y);
		}
	}
}
