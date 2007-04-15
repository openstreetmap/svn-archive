package org.openstreetmap.josm.plugins.ywms;

/**
 * Exception when loading images with a mozilla browser
 * 
 * @author frsantos
 *
 */
public class ImageLoaderException extends Exception 
{
    /**
     * Constructor
     */
	public ImageLoaderException() 
	{
		super();
	}


    /**
     * Constructor 
     * 
     * @param msg The exception message
     */
	public ImageLoaderException(String msg) 
	{
		super(msg);
	}

    /**
     * Constructor 
     * @param t the nested exception
     */
	public ImageLoaderException(Throwable t) 
	{
		super(t);
	}
}
