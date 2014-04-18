/* Copyright 2014 Malcolm Herring
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */

package jtile;

import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class Jtile {

	static PNGTranscoder trans;
	static String srcdir;
	static String dstdir;
	static int xtile;
	static int ytile;
	static ArrayList<String> send;

	public static void tile(int zoom, int dxy, int xn, int yn) throws Exception {

		trans = new PNGTranscoder();
		trans.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(256));
		trans.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(256));
		trans.addTranscodingHint(PNGTranscoder.KEY_AOI, new Rectangle(256+(xn*dxy), 256+(yn*dxy), dxy, dxy));

		String svgURI = new File(srcdir + xtile + "-" + ytile + "-" + zoom + ".svg").toURI().toURL().toString();
		TranscoderInput input = new TranscoderInput(svgURI);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TranscoderOutput output = new TranscoderOutput(bos);
		trans.transcode(input, output);
		if (bos.size() > 446) {
			int scale = (int) Math.pow(2, zoom - 12);
			int xdir = (scale * xtile) + xn;
			int ynam = (scale * ytile) + yn;
			send.add("put " + dstdir + zoom + "/" + xdir + "/" + ynam + ".png" + " cache/tiles-" + zoom + "-" + xdir + "-" + ynam + ".png");
			File ofile = new File(dstdir + "/" + zoom + "/" + xdir + "/");
			ofile.mkdirs();
			OutputStream ostream = new FileOutputStream(dstdir + "/" + zoom + "/" + xdir + "/" + ynam + ".png");
			bos.writeTo(ostream);
			ostream.flush();
			ostream.close();
			if (send.size() > 10) {
				PrintWriter writer = new PrintWriter(srcdir + zoom  + "-" + xdir + "-" + ynam + ".send", "UTF-8");
				for (String str : send) {
					writer.println(str);
				}
				writer.close();
				send = new ArrayList<String>();
			}
		}
		if ((zoom < 18) && ((zoom < 16) || (bos.size() > 446))) {
			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					tile((zoom + 1), (dxy / 2), (xn * 2 + x), (yn * 2 + y));
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		srcdir = args[0];
		dstdir = args[1];
		xtile = Integer.parseInt(args[2]);
		ytile = Integer.parseInt(args[3]);
		send = new ArrayList<String>();
		tile(12, 256, 0, 0);
		if (send.size() > 0) {
			PrintWriter writer = new PrintWriter(srcdir + "12-" + xtile + "-" + ytile + ".send", "UTF-8");
			for (String str : send) {
				writer.println(str);
			}
			writer.close();
		}
		for (int z = 12; z <= 18; z++) {
			(new File(srcdir + xtile + "-" + ytile + "-" + z + ".svg")).delete();
		}
		System.exit(0);
	}
	
}
