/**
 * @author Stefan Kaintoch
 * @version $Id: FileInfoRec.java 3 2007-10-30 19:40:04Z ramack $
 */
package org.kaintoch.gps.globalsat.dg100;

import java.nio.ByteBuffer;

/**
 * @author skaintoch
 *
 */
public class FileInfoRec
{
	private int timeZ = 0;
	private int date = 0;
	private int idx = 0;
	
	public FileInfoRec(ByteBuffer buf)
	{
		timeZ =  buf.getInt();
		date =  buf.getInt();
		idx =  buf.getInt();
	}
	
	public String toString()
	{
		return "[FileInfoRec: timeZ = " + timeZ + ", date = " + date + ", idx = " + idx + "]";
	}

	/**
	 * @return Returns the idx.
	 */
	public int getIdx()
	{
		return idx;
	}
}
