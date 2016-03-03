/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jrender;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import s57.S57map;
import s57.S57osm;
import s57.S57map.*;
import render.*;

public class Jrender {

	static String srcdir;
	static String dstdir;
	static int xtile;
	static int ytile;
	static int zoom;
	static ArrayList<String> send;
	static HashMap<String, Boolean> deletes;
	static Context context;
	static S57map map;
	static int empty;

	static class Context implements ChartContext {
		
	  static double top;
	  static double mile;
	  
	  public Context () {
			top = (1.0 - Math.log(Math.tan(map.bounds.maxlat) + 1.0 / Math.cos(map.bounds.maxlat)) / Math.PI) / 2.0 * 256.0 * 4096.0;
			mile = 768 / ((Math.toDegrees(map.bounds.maxlat) - Math.toDegrees(map.bounds.minlat)) * 60);
	  }
	  
		public Point2D getPoint(Snode coord) {
			double x = (Math.toDegrees(coord.lon) - Math.toDegrees(map.bounds.minlon)) * 256.0 * 2048.0 / 180.0;
			double y = ((1.0 - Math.log(Math.tan(coord.lat) + 1.0 / Math.cos(coord.lat)) / Math.PI) / 2.0 * 256.0 * 4096.0) - top;
			return new Point2D.Double(x, y);
		}

		public double mile(Feature feature) {
			return mile;
		}

		public boolean clip() {
			return false;
		}

		public Color background(S57map map) {
			return new Color(0, true);
		}

		public RuleSet ruleset() {
			return RuleSet.SEAMARK;
		}
	}
	
	static final boolean test = true;
	
	public static void tileMap(String idir, int zoom) throws IOException {
		BufferedImage img;
		context = new Context();

		int border = 256 / (int)Math.pow(2, (12 - zoom));
		int size = 256;
		for (int i = 0; i < (12 - zoom); i++) size *= 2;
		Rectangle rect = new Rectangle((size + (2 * border)), (size + (2 * border)));
		img = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
		Renderer.reRender(img.createGraphics(), rect, zoom, 1.0, map, context);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", bos);
		empty = bos.size();

		if (test) {
			for (int z = 12; z <= 18; z++) {
				DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
				Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
				SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
				svgGenerator.clearRect(rect.x, rect.y, rect.width, rect.height);
				svgGenerator.setSVGCanvasSize(rect.getSize());
				svgGenerator.setClip(rect.x, rect.y, rect.width, rect.height);
				svgGenerator.translate(-border, -border);
				Renderer.reRender(svgGenerator, rect, z, 1.0, map, context);
				svgGenerator.stream(dstdir + "tst_" + z + "-" + xtile + "-" + ytile + ".svg");
			}
		} else {
			tile(zoom, 1, 0, 0);
		}
	}
	
	static void tile(int zoom, int s, int xn, int yn) throws IOException {
		int scale = (int) Math.pow(2, zoom - 12);
		int xdir = (scale * xtile) + xn;
		int ynam = (scale * ytile) + yn;
		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		g2.scale(s, s);
		g2.translate(-(256 + (xn * 256 / s)), -(256 + (yn * 256 / s)));
		Renderer.reRender(g2, new Rectangle(256, 256), zoom, 1, map, context);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", bos);
		if (bos.size() > empty) {
			String dstnam = dstdir + zoom + "/" + xdir + "/" + ynam + ".png";
			deletes.remove(dstnam);
			send.add("put " + dstnam + " tiles/" + zoom + "/" + xdir + "/" + ynam + ".png");
			File ofile = new File(dstdir + "/" + zoom + "/" + xdir + "/");
			ofile.mkdirs();
			FileOutputStream fos = new FileOutputStream(dstdir + "/" + zoom + "/" + xdir + "/" + ynam + ".png");
			bos.writeTo(fos);
			fos.close();
			if (send.size() > 10) {
				PrintWriter writer = new PrintWriter(srcdir + zoom + "-" + xdir + "-" + ynam + ".send", "UTF-8");
				for (String str : send) {
					writer.println(str);
				}
				writer.close();
				send = new ArrayList<String>();
			}
		}
		if ((zoom >= 12) && (zoom < 18) && ((zoom < 16) || (bos.size() > empty))) {
			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					tile((zoom + 1), (s * 2), (xn * 2 + x), (yn * 2 + y));
				}
			}
		}
	}
	
	static void clean(int z, int xn, int yn) throws Exception {
		int scale = (int) Math.pow(2, z - 12);
		int xdir = (scale * xtile) + xn;
		int ynam = (scale * ytile) + yn;
		String delnam = dstdir + z + "/" + xdir + "/" + ynam + ".png";
		File delfile = new File(delnam);
		if (delfile.exists()) {
			deletes.put(delnam, true);
			delfile.delete();
		}
		if ((z < 18)) {
			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					clean((z + 1), (xn * 2 + x), (yn * 2 + y));
				}
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		srcdir = args[0];
		dstdir = args[1];
		zoom = Integer.parseInt(args[2]);
		xtile = Integer.parseInt(args[3]);
		ytile = Integer.parseInt(args[4]);
		send = new ArrayList<String>();
		deletes = new HashMap<String, Boolean>();
		BufferedReader in = new BufferedReader(new FileReader(srcdir + zoom + "-" + xtile + "-" + ytile + ".osm"));
		map = new S57map(true);
		S57osm.OSMmap(in, map, false);
		in.close();
		if (zoom == 12) {
			clean(12, 0, 0);
		}
		tileMap(dstdir, zoom);
//		if ((send.size() + deletes.size()) > 0) {
//			PrintWriter writer = new PrintWriter(srcdir + zoom + "-" + xtile + "-" + ytile + ".send", "UTF-8");
//			for (String str : send) {
//				writer.println(str);
//			}
//			for (String del : deletes.keySet()) {
//				writer.println("rm " + del);
//			}
//			writer.close();
//		}
		System.exit(0);
	}
}
