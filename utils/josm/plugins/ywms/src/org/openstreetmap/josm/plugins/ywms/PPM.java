package org.openstreetmap.josm.plugins.ywms;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.MemoryImageSource;
import java.io.*;

/**
 * PPM Image loader.
 * <p>
 * This loader handles only one type of PPM files: binary(P6) with 255 colors.
 */
public class PPM
{
	/** The image dimension */
	protected Dimension dim = new Dimension();
	
	/** The pixels of the image in ARGB format  */
	protected int[] data;

	/**
	 * Creates a PPM image from a file
	 * <p>
	 * The stream must contain a binary PPM (type P6 in the header) of 255 colors
	 * 
	 * @param filename The name of the file to read the PPM from
	 * @throws IOException when failing to read the file
	 * @throws UnsupportedEncodingException if file is not a binary(P6) PPM of 255 colors 
	 */
	public PPM(String filename) throws IOException, UnsupportedEncodingException
	{
		FileInputStream fis = new FileInputStream(filename);
		
		// XXX: The source data to the StreamTokenizer can't buffered, since we
		// only need a few lines of the beginning of the file, and all remain
		// data must be processed later. Even a InputStreamReader does some
		// internal buffering, so we must use a deprecated constructor, create
		// our own Reader or create some methods to simulate a StreamTokenizer.
		// Easy way chosen :-)
		StreamTokenizer st = new StreamTokenizer(fis);
		st.commentChar('#');

		/* PPM file format:
		 * 
		 * #			  --> Comments allowed anywere before binary data
		 * P3|P6          --> ASCII/Binary
		 * WIDTH          --> image width, in ascii
		 * HEIGHT         --> image height, in ascii
		 * COLORS		  --> num colors, in ascii
		 * [data]		  --> if P6, data in binary, 3 RGB bytes per pixel 
		 */

		st.nextToken();
		if( !st.sval.equals("P6") )
			throw new UnsupportedEncodingException("Not a P6 (binary) PPM");
		
		st.nextToken();
		dim.width = (int) Math.round(st.nval);
		st.nextToken();
		dim.height = (int) Math.round(st.nval);
		data = new int[dim.width * dim.height];

		st.nextToken(); 
		int maxVal = (int) Math.round(st.nval); 
		if( maxVal != 255 )
			throw new UnsupportedEncodingException("Not a 255 color PPM");
				
		// Binary data cann be buffered
		InputStream in = new BufferedInputStream(fis);
		int numPixels = dim.width * dim.height;
		for (int i = 0; i < numPixels; i++)
		{
			int r = in.read();
			int g = in.read();
			int b = in.read();
			if( r== -1 || g == -1 || b == -1)
				throw new IOException("EOF:" + r + " " + g + " " + b);
			
			data[i] = rgb(r, g, b);
		}
		in.close();		
	}


	/**
	 * Creates an AWT image using the raw data
	 * @return The AWT image
	 */
	public Image getImage()
	{
		MemoryImageSource memoryImageSource = new MemoryImageSource(dim.width, dim.height, data, 0, dim.width);
		return Toolkit.getDefaultToolkit().createImage(memoryImageSource);
	}

	/**
	 * Makes an ARGB pixel from three color values
	 * @param r The red value
	 * @param g The green value
	 * @param b The blue value
	 * @return The ARGB value
	 * 
	 */
	private static int rgb(int r, int g, int b)
	{
		return (255 << 24 | (r << 16) | (g << 8) | b);
	}

}
