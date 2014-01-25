/* Copyright 2012 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jrender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import symbols.*;
import symbols.Symbols.*;
import render.*;

public class Jrender extends Panel {

	public static void main(String[] args) {
		BufferedImage img;
		Graphics2D g2;
		
		JFrame frame = new JFrame("Jrender");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Panel panel = new Jrender();
    frame.getContentPane().add("Center", panel);
		frame.setSize(256, 256);
		frame.setVisible(true);
		
		img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		g2 = img.createGraphics();
		drawRendering(g2);
		try {
			ImageIO.write(img, "png", new File("/Users/mherring/Desktop/export.png"));
		} catch (Exception e) {
			System.out.println("Exception");
		}
		
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    String svgNS = "http://www.w3.org/2000/svg";
    Document document = domImpl.createDocument(svgNS, "svg", null);
    SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
    svgGenerator.setSVGCanvasSize(new Dimension(256, 256));
		drawRendering(svgGenerator);
    boolean useCSS = true;
    Writer out = null;
			try {
				out = new OutputStreamWriter(new FileOutputStream("/Users/mherring/Desktop/export.svg"), "UTF-8");
			} catch (IOException e1) {
				System.out.println("Exception");
			}
    try {
			svgGenerator.stream(out, useCSS);
		} catch (SVGGraphics2DIOException e) {
			System.out.println("Exception");
		}
	}
	
	public static void drawRendering(Graphics2D g2) {
		double scale = Renderer.symbolScale[8];
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		Scheme scheme = new Scheme(); scheme.pat.add(Patt.H); scheme.col.add(Color.red); scheme.col.add(Color.yellow); scheme.col.add(Color.green);
		Symbols.drawSymbol(g2, Buoys.Pillar, scale, 128.0, 128.0, scheme, null);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		drawRendering(g2);
	}
}
