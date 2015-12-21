/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jbasemap;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import s57.S57map;
import s57.S57obj.Obj;
import s57.S57osm;
import s57.S57map.*;
import symbols.*;
import render.*;

public class Jbasemap {

	static String src;
	static String dst;
	static Context context;
	static S57map map;

	static class Context implements ChartContext {
		
	  static double top = 0;
	  static double mile = 0;
	  
	  public Context () {
			top = (1.0 - Math.log(Math.tan(map.bounds.maxlat) + 1.0 / Math.cos(map.bounds.maxlat)) / Math.PI) / 2.0 * 256.0 * 512.0;
			mile = 256 / ((Math.toDegrees(map.bounds.maxlat) - Math.toDegrees(map.bounds.minlat)) * 60);
	  }
	  
		public Point2D getPoint(Snode coord) {
			double x = (Math.toDegrees(coord.lon) - Math.toDegrees(map.bounds.minlon)) * 256.0 * 256.0 / 180.0;
			double y = ((1.0 - Math.log(Math.tan(coord.lat) + 1.0 / Math.cos(coord.lat)) / Math.PI) / 2.0 * 256.0 * 512.0) - top;
			return new Point2D.Double(x, y);
		}

		public double mile(Feature feature) {
			return mile;
		}

		public boolean clip() {
			return true;
		}

		public Color background() {
			if (map.features.containsKey(Obj.COALNE)) {
				return Symbols.Bwater;
			} else {
				return Symbols.Yland;
			}
		}

		public RuleSet ruleset() {
			return RuleSet.BASE;
		}
	}
	
	public static void main(String[] args) throws IOException {
		src = args[0];
		dst = args[1];
		try {
			BufferedReader in = new BufferedReader(new FileReader(src));
			map = new S57map(false);
			if (args.length == 4) {
				map.bounds.maxlat = Math.atan(Math.sinh(Math.PI * (1 - 2 * Double.parseDouble(args[3]) / 512))) * 180.0 / Math.PI;
				map.bounds.minlat = Math.atan(Math.sinh(Math.PI * (1 - 2 * (Double.parseDouble(args[3]) + 1) / 512))) * 180.0 / Math.PI;
				map.bounds.minlon = Double.parseDouble(args[2]) / 512 * 360.0 - 180.0;
				map.bounds.maxlon = (Double.parseDouble(args[2]) + 1) / 512 * 360.0 - 180.0;
			}
			try {
				S57osm.OSMmap(in, map);
			} catch (Exception e) {
				System.err.println("Input data error");
				System.exit(-1);
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Input file: " + e.getMessage());
			System.exit(-1);
		}
		context = new Context();
		Rectangle rect = new Rectangle(256, 256);
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
		svgGenerator.clearRect(rect.x, rect.y, rect.width, rect.height);
		svgGenerator.setSVGCanvasSize(rect.getSize());
		svgGenerator.setClip(rect.x, rect.y, rect.width, rect.height);
		Renderer.reRender(svgGenerator, rect, 9, 0.002, map, context);
		svgGenerator.stream(dst);
		System.exit(0);
	}
}
