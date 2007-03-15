package org.openstreetmap.josm.plugins.ywms;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;


/**
 * Loads a stellite image from Yahoo accordin to a WMS request.
 * <p>
 * The image is captured from firefox using one of its features: when the
 * environment variable MOZ_FORCE_PAINT_AFTER_ONLOAD is set, it causes firefox
 * to dump, as PPM files, the contents of all the pages loaded. The value of the
 * variable is used to locate where the files are dumped.
 * <p>
 * As the image served by Yahoo is bigger than the one requested, it is cropped
 * and resized to the desired size.
 * <p>
 * Currently, there is no way of knowing whether a zone has imagery or not. If
 * not, the Yahoo message "We're sorry, the data you have requested is not
 * available. Please zoom out to see more map information or refresh your
 * browser to try again".
 * <p>
 * <br>
 * <b>Implementation note:</b> <lu>
 * <li>Some information is passed from Javascript to Java, so Firefox must be
 * configured with the method "dump" to work. To allow this method in firefox,
 * create or modify the option "browser.dom.window.dump.enabled=true" in
 * "about:config"
 * <p>
 * <li>Also, as firefox must be started and killed once and again, it is
 * recommended to create a profile with the option
 * "browser.sessionstore.resume_from_crash" to false and set other profile to
 * default, so no nag screens are shown. </lu>
 * 
 * @author frsantos
 * 
 */
public class ImageLoader 
{
	/** Firefox writes lines starting with this for each file loadded */
	public static final String GECKO_DEBUG_LINE = "GECKO: PAINT FORCED AFTER ONLOAD:";

	private URL yahooUrl;
	double[] orig_bbox = null;
	double[] final_bbox = null;
	int width = -1;
	int height = -1;
	Image image;
	List<String> firefoxFiles = new ArrayList<String>();

	/** The regular expression used to locate the bounding boxes */
	private static final Pattern BBOX_RE = Pattern.compile("bbox=([+-]?\\d+\\.\\d+),([+-]?\\d+\\.\\d+),([+-]?\\d+\\.\\d+),([+-]?\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE); 

	/**
	 * Constructor.
	 * 
	 * @param wmsUrl The WMS request
	 * @param pluginDir The directory of the plugin
	 * @throws ImageLoaderException When error loading the image
	 */
	public ImageLoader(String wmsUrl) throws ImageLoaderException
	{
		System.out.println("YWMS::Requested WMS URL: " + wmsUrl);
		try {
			URL request = new URL("file:///page" + wmsUrl);
			String query = request.getQuery().toLowerCase();
			yahooUrl = new File(YWMSPlugin.getStaticPluginDir(), "ymap.html").toURI().toURL();
			yahooUrl = new URL( yahooUrl.toExternalForm() + "?" + query);
			
			// Parse query to find original bounding box and dimensions
        	StringTokenizer st = new StringTokenizer(query, "&");
        	while( st.hasMoreTokens() )
        	{
        		String param = st.nextToken();
        		if( param.startsWith("width=") )
        			width=Integer.parseInt(param.substring("width=".length()));
        		else if( param.startsWith("height=") )
        			height=Integer.parseInt(param.substring("height=".length()));
        		else if( param.startsWith("bbox=") )
        		{
        			orig_bbox = getBbox(param);
        		}
        	}
        	
        	if( width == -1 || height == -1)
        		throw new ImageLoaderException("Can't find dimensions");
        	
        	load();
		} 
		catch (MalformedURLException e) {
			throw new ImageLoaderException(e);
		}
	}

	/**
	 * Does the hard work.
	 * <p>
	 * It spawns a Firefox process with an HTML page that loads Yahoo imagery
	 * using Yahoo's AJAX API. Firefox must be configured to allow the "dump"
	 * method for this to work.
	 * <p>
	 * The image is cropped and reescaled to meet requested dimensions.
	 * @throws ImageLoaderException When error loading the page
	 */
	private void load() throws ImageLoaderException
	{
		Process browser = null;
		try 
		{
			browser = GeckoSupport.browse(yahooUrl.toString(), true);
			// TODO: set focus in main window
			File imageFilePpm = null;

			// Parse output
			BufferedReader in = new BufferedReader( new InputStreamReader( browser.getInputStream() ) );
			String line = in.readLine();
	        while( line != null ) 
	        {
	        	System.out.println("YWMS::" + line);
	            if( line.startsWith("bbox=") )
	            {
	            	final_bbox = getBbox(line);
	                // System.out.println("YWMS::BBOX: (" + final_bbox[0] + "," + final_bbox[1] + "), (" + final_bbox[2] + "," + final_bbox[3] + ")");
	            }
	            else if( line.startsWith(GECKO_DEBUG_LINE))
	            {
	            	// Find out the screenshot file
	            	StringTokenizer st = new StringTokenizer(line);
	            	// Skip header
	            	for( int i = 0; i < 5; i++) st.nextToken();
	            	String url = st.nextToken();
	            	String file = st.nextToken();
	            	firefoxFiles.add(file);
	            	
	            	URL browserUrl;
	            	try {
	            		browserUrl = new URL(url);	
		            	if( browserUrl.sameFile(yahooUrl))
		            	{
			            	String status = st.nextToken();
			            	if( !"(OK)".equals(status) )
			            		throw new ImageLoaderException("Firefox couldn't load image");
			            	
			            	imageFilePpm = new File(file);
			            	break;
		            	}
					} 
	            	catch (MalformedURLException mue) 
	            	{
						// Probably a mozilla "chrome://" URL. Do nothing
					}
	            }
	            else if( line.startsWith("WYMS ERROR:") )
	            {
		        	throw new ImageLoaderException("Error in JavaScript page:" + line);
	            }
	            line = in.readLine();
	        }

	        if( final_bbox == null  && imageFilePpm == null && !firefoxFiles.isEmpty() )
	        {
	        	throw new ImageLoaderException("Is there any other firefox window open with same profile?");
	        }
	        if( final_bbox == null )
	        {
	        	throw new ImageLoaderException("Couldn't find bounding box. Is browser.dom.window.dump.enabled set in Firefox config?");
	        }
	        if( imageFilePpm == null )
	        {
	        	throw new ImageLoaderException("Couldn't find dumped image. Is it a modern Gecko browser (i.e., firefox 1.5)?");
	        }
	        
            PPM ppmImage = new PPM(imageFilePpm.getAbsolutePath());
            image = ppmImage.getImage();
            cleanImages();

	        resizeImage();
		} catch (IOException e) 
		{
			throw new ImageLoaderException(e);
		}
		finally
		{
			if( browser != null )
			    browser.destroy();
		}
	}

	/**
	 * Transforms the Image into a BufferedImage
	 * @return The current image as a BufferedImage
	 */
	public BufferedImage getBufferedImage() 
	{
		if( image == null )
			return null;
		
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bufferedImage;
	}

	/**
	 * Resizes the image to meet the requested dimensions
	 */
	private void resizeImage() 
	{
		int calcwidth = (int)Math.round((final_bbox[2] - final_bbox[0]) * width / (orig_bbox[2] - orig_bbox[0]));
		int calcheight = (int)Math.round((final_bbox[3] - final_bbox[1]) * height / (orig_bbox[3] - orig_bbox[1]));

		int cropWidth = (calcwidth - width) / 2;
		int cropHeight = (calcheight - height) / 2;
	
		Toolkit tk = Toolkit.getDefaultToolkit();
        // save("/tmp/image_orig.jpg");
        image = tk.createImage (new FilteredImageSource (image.getSource(), new CropImageFilter(0, 0, width, height)));
        image = tk.createImage (new FilteredImageSource (image.getSource(), new ReplicateScaleFilter(calcwidth, calcheight)));
        image = tk.createImage (new FilteredImageSource (image.getSource(), new CropImageFilter(cropWidth, cropHeight, width, height)));
		// BufferedImage img = (BufferedImage)image.getScaledInstance(calcwidth, calcwidth, BufferedImage.SCALE_DEFAULT);
		// image = img.getSubimage(cropl, cropt, width, height);
	}

	/**
	 * Parses a line of the form bbox=xmin,ymin,xmax,ymax and extracts the bounding box
	 * 
	 * @param line The string to parse
	 * @return The bound box as a double array[4]
	 * @throws ImageLoaderException
	 */
	private double[] getBbox(String line) throws ImageLoaderException
	{
    	Matcher matcher = BBOX_RE.matcher(line);
    	if( !matcher.matches() )
    	{
    		throw new ImageLoaderException("Can't find bounding box");
    	}
    	
    	double[] bbox = new double[4];
    	for( int i = 0; i < 4; i++)
    	{
    		bbox[i] = Double.parseDouble( matcher.group(i+1) );
    	}
    	
    	return bbox;
	}
	
	/**
	 * Saves the current image as a PNG file
	 * @param fileName The name of the new file 
	 * @throws IOException When error saving the file
	 */
	public void save(String fileName) throws IOException
	{
        FileOutputStream fileStream = new FileOutputStream(fileName);
        ImageIO.write(getBufferedImage(), "png", fileStream);
        fileStream.close();
	}
	
	/**
	 * Delete all images created by firefox when they are not longer used
	 */
	public void cleanImages() 
	{
		for(String fileName : firefoxFiles)
		{
			try
			{
				File file = new File(fileName);
				file.delete();
			}
			catch(Exception e) { }
		}
		
	}
}
