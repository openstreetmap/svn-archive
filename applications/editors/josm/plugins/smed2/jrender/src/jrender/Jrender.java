/* Copyright 2012 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jrender;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

import s57.S57map;
import s57.S57map.Feature;
import s57.S57map.Snode;
import render.*;

public class Jrender {

	static S57map map = null;
	static double minlat = 0;
	static double minlon = 0;
	static double maxlat = 0;
	static double maxlon = 0;
  static double top = 0;
  static double mile = 0;

	public static void main(String[] args) throws IOException {
		Render render;
		BufferedReader in;
		String k = "";
		String v = "";
		
		double lat = 0;
		double lon = 0;
		long id = 0;

		BufferedImage img;
		Graphics2D g2;
		boolean inOsm = false;
		boolean inNode = false;
		boolean inWay = false;
		boolean inRel = false;
		
		if (args.length < 5) {
			System.err.println("Usage: java -jar jrender.jar osm_file minlat, minlon, maxlat, maxlon");
			System.exit(-1);
		}
		in = new BufferedReader(new FileReader(args[0]));
		minlat = Double.parseDouble(args[1]);
		minlon = Double.parseDouble(args[2]);
		maxlat = Double.parseDouble(args[3]);
		maxlon = Double.parseDouble(args[4]);
		top = (1.0 - Math.log(Math.tan(Math.toRadians(maxlat)) + 1.0 / Math.cos(Math.toRadians(maxlat))) / Math.PI) / 2.0 * 256.0 * 4096.0;
		mile = 768 / ((maxlat - minlat) * 60);
		
		render = new Render();
		String ln;
		while ((ln = in.readLine()) != null) {
			if (inOsm) {
				if ((inNode || inWay || inRel) && (ln.contains("<tag"))) {
					k = v = "";
					String[] token = ln.split("k=");
					k = token[1].split("[\"\']")[1];
					token = token[1].split("v=");
					v = token[1].split("[\"\']")[1];
					if (!k.isEmpty() && !v.isEmpty()) {
						map.addTag(k, v);
					}
				}
				if (inNode) {
					if (ln.contains("</node")) {
						inNode = false;
						map.tagsDone(id);
					}
				} else if (ln.contains("<node")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						} else if (token.matches("^lat=.+")) {
							lat = Double.parseDouble(token.split("[\"\']")[1]);
						} else if (token.matches("^lon=.+")) {
							lon = Double.parseDouble(token.split("[\"\']")[1]);
						}
					}
					map.addNode(id, lat, lon);
					if (ln.contains("/>")) {
						map.tagsDone(id);
					} else {
						inNode = true;
					}
				} else if (inWay) {
					if (ln.contains("<nd")) {
						long ref = 0;
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								ref = Long.parseLong(token.split("[\"\']")[1]);
							}
						}
						map.addToEdge(ref);
					}
					if (ln.contains("</way")) {
						inWay = false;
						map.tagsDone(id);
					}
				} else if (ln.contains("<way")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					map.addEdge(id);
					if (ln.contains("/>")) {
						map.tagsDone(0);
					} else {
						inWay = true;
					}
				} else if (ln.contains("</osm")) {
					inOsm = false;
					break;
				} else if (inRel) {
					if (ln.contains("<member")) {
						String type = "";
						String role = "";
						long ref = 0;
						for (String token : ln.split("[ ]+")) {
							if (token.matches("^ref=.+")) {
								ref = Long.parseLong(token.split("[\"\']")[1]);
							} else if (token.matches("^type=.+")) {
								type = (token.split("[\"\']")[1]);
							} else if (token.matches("^role=.+")) {
								role = (token.split("[\"\']")[1]);
							}
						}
						if ((role.equals("outer") || role.equals("inner")) && type.equals("way"))
							map.addToArea(ref, role.equals("outer"));
					}
					if (ln.contains("</relation")) {
						inRel = false;
						map.tagsDone(id);
					}
				} else if (ln.contains("<relation")) {
					for (String token : ln.split("[ ]+")) {
						if (token.matches("^id=.+")) {
							id = Long.parseLong(token.split("[\"\']")[1]);
						}
					}
					map.addArea(id);
					if (ln.contains("/>")) {
						map.tagsDone(id);
					} else {
						inRel = true;
					}
				}
			} else if (ln.contains("<osm")) {
				inOsm = true;
				map = new S57map();
			}
		}
		in.close();
		
		for (int s = 1, z = 12; z <= 18; s *= 2, z++) {
			for (int x = 0; x < s; x++) {
				for (int y = 0; y < s; y++) {
					img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
					g2 = img.createGraphics();
					g2.scale(s, s);
					g2.translate(-(256 + (x * 256 / s)), -(256 + (y * 256 / s)));
					render.drawRendering(g2, z, 1);
					ByteOutputStream bos = new ByteOutputStream();
					ImageIO.write(img, "png", bos);
					if (bos.size() > 334) {
						FileOutputStream fos = new FileOutputStream("/Users/mherring/boatsw/oseam/josm/plugins/smed2/jrender/tst/tst" + z + "_" + x + "_" + y + ".png");
						bos.writeTo(fos);
						fos.close();
					}
				}
			}
		}

		for (int z = 12; z <= 18; z++) {
			DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
			Document document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
			SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
			svgGenerator.setSVGCanvasSize(new Dimension(256, 256));
			svgGenerator.setClip(0, 0, 256, 256);
			svgGenerator.translate(-256, -256);
			render.drawRendering(svgGenerator, z, 1);
			svgGenerator.stream("/Users/mherring/boatsw/oseam/josm/plugins/smed2/jrender/tst/tst" + z + ".svg");
		}

		System.err.println("Finished");
		System.exit(0);
	}
	
	
	static class Render implements MapContext {
		
		public void drawRendering(Graphics2D g2, int zoom, double scale) {
			Renderer.reRender(g2, zoom, scale, map, this);
		}

		@Override
		public Point2D getPoint(Snode coord) {
			double x = (Math.toDegrees(coord.lon) - minlon) * 256.0 * 2048.0 / 180.0;
			double y = ((1.0 - Math.log(Math.tan(coord.lat) + 1.0 / Math.cos(coord.lat)) / Math.PI) / 2.0 * 256.0 * 4096.0) - top;
			return new Point2D.Double(x, y);
		}

		@Override
		public double mile(Feature feature) {
			return mile;
		}
	}
}
