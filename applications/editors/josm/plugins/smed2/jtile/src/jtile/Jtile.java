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
import java.util.HashMap;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public class Jtile {

	static PNGTranscoder trans;
	static String srcdir;
	static String dstdir;
	static int xtile;
	static int ytile;
	static int zoom;
	static ArrayList<String> send;
	static HashMap<String, Boolean> deletes;

	static void tile(int z, int dxy, int xn, int yn) throws Exception {

		trans = new PNGTranscoder();
		trans.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(256));
		trans.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(256));
		trans.addTranscodingHint(PNGTranscoder.KEY_AOI, new Rectangle(256+(xn*dxy), 256+(yn*dxy), dxy, dxy));

		String svgURI = new File(srcdir + xtile + "-" + ytile + "-" + z + ".svg").toURI().toURL().toString();
		TranscoderInput input = new TranscoderInput(svgURI);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TranscoderOutput output = new TranscoderOutput(bos);
		trans.transcode(input, output);
		if (bos.size() > 446) {
			int scale = (int) Math.pow(2, z - 12);
			int xdir = (scale * xtile) + xn;
			int ynam = (scale * ytile) + yn;
			String dstnam = dstdir + z + "/" + xdir + "/" + ynam + ".png";
			deletes.remove(dstnam);
			send.add("put " + dstnam + " cache/tiles-" + z + "-" + xdir + "-" + ynam + ".png");
			File ofile = new File(dstdir + "/" + z + "/" + xdir + "/");
			ofile.mkdirs();
			OutputStream ostream = new FileOutputStream(dstdir + "/" + z + "/" + xdir + "/" + ynam + ".png");
			bos.writeTo(ostream);
			ostream.flush();
			ostream.close();
			if (send.size() > 10) {
				PrintWriter writer = new PrintWriter(srcdir + z  + "-" + xdir + "-" + ynam + ".send", "UTF-8");
				for (String str : send) {
					writer.println(str);
				}
				writer.close();
				send = new ArrayList<String>();
			}
		}
		if ((z < 18) && ((z < 16) || (bos.size() > 446))) {
			for (int x = 0; x < 2; x++) {
				for (int y = 0; y < 2; y++) {
					tile((z + 1), (dxy / 2), (xn * 2 + x), (yn * 2 + y));
				}
			}
		}
	}

	static void tile91011() throws Exception {

		trans = new PNGTranscoder();
		trans.addTranscodingHint(PNGTranscoder.KEY_WIDTH, new Float(256));
		trans.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, new Float(256));
		trans.addTranscodingHint(PNGTranscoder.KEY_AOI, new Rectangle(512, 512, 256*(int)(Math.pow(2, 12-zoom)), 256*(int)(Math.pow(2, 12-zoom))));

		String svgURI = new File(srcdir + xtile + "-" + ytile + "-" + zoom + ".svg").toURI().toURL().toString();
		TranscoderInput input = new TranscoderInput(svgURI);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TranscoderOutput output = new TranscoderOutput(bos);
		trans.transcode(input, output);
		String dstnam = dstdir + zoom + "/" + xtile + "/" + ytile + ".png";
		if (bos.size() > 446) {
			send.add("put " + dstnam + " cache/tiles-" + zoom + "-" + xtile + "-" + ytile + ".png");
			File ofile = new File(dstdir + "/" + zoom + "/" + xtile + "/");
			ofile.mkdirs();
			OutputStream ostream = new FileOutputStream(dstdir + "/" + zoom + "/" + xtile + "/" + ytile + ".png");
			bos.writeTo(ostream);
			ostream.flush();
			ostream.close();
		} else {
			File old = new File(dstnam);
			if (old.exists()) {
				old.delete();
				deletes.put(dstnam, true);
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
		if (zoom == 12) {
			clean(12, 0, 0);
			tile(12, 256, 0, 0);
		} else {
			tile91011();
		}
		if ((send.size() + deletes.size()) > 0) {
			PrintWriter writer = new PrintWriter(srcdir + zoom + "-" + xtile + "-" + ytile + ".send", "UTF-8");
			for (String str : send) {
				writer.println(str);
			}
			for (String del : deletes.keySet()) {
				writer.println("rm " + del);
			}
			writer.close();
		}
		if (zoom == 12) {
			for (int zz = 12; zz <= 18; zz++) {
				(new File(srcdir + xtile + "-" + ytile + "-" + zz + ".svg")).delete();
			}
		} else {
			(new File(srcdir + xtile + "-" + ytile + "-" + zoom + ".svg")).delete();
		}
		System.exit(0);
	}
	
}
