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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import symbols.*;
import symbols.Symbols.Scheme;
import render.*;

public class Jrender extends Panel {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		BufferedImage img;
		Graphics2D g2;
		
		JFrame frame = new JFrame("Jrender");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    Panel panel = new Jrender();
    frame.getContentPane().add("Center", panel);
		frame.setSize(256, 256);
		frame.setVisible(true);
/*		
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
				g2 = img.createGraphics();
				g2.translate(-(x*256), -(y*256));
				drawRendering(g2);
				try {
					ImageIO.write(img, "png", new File("tst" + x + "_" + y + ".png"));
				} catch (IOException e) {
					System.out.println("IO Exception");
				}
			}
		}*/
		
		img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		g2 = img.createGraphics();
		g2.translate(-32, -32);
		drawRendering(g2);
		try {
			ImageIO.write(img, "png", new File("/Users/mherring/Desktop/export.png"));
		} catch (Exception e) {
			System.out.println("Exception");
		}
		
	}
	
	public static void drawRendering(Graphics2D g2) {
		double scale = Renderer.symbolScale[7];
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
//		System.out.println("hello");
   
		Symbols.drawSymbol(g2, Areas.Seaplane, scale/2, 64.0, 64.0, new Scheme(Color.green), null);
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		drawRendering(g2);
	}
}
