/*
   Copyright (C) 2002 MySQL AB

      This program is free software; you can redistribute it and/or modify
      it under the terms of the GNU General Public License as published by
      the Free Software Foundation; either version 2 of the License, or
      (at your option) any later version.

      This program is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      GNU General Public License for more details.

      You should have received a copy of the GNU General Public License
      along with this program; if not, write to the Free Software
      Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 */
package com.mysql.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;


/**
 * Simplistic implementation of java.sql.Clob for MySQL Connector/J
 *
 * @version $Id: Clob.java,v 1.5.2.5 2004/04/18 21:02:51 mmatthew Exp $
 * @author Mark Matthews
 */
public class Clob implements java.sql.Clob, OutputStreamWatcher, WriterWatcher {
    private String charData;

    Clob(String charData) {
        this.charData = charData;
    }

    /**
     * @see java.sql.Clob#setAsciiStream(long)
     */
    public OutputStream setAsciiStream(long indexToWriteAt)
        throws SQLException {
        if (indexToWriteAt < 1) {
            throw new SQLException("indexToWriteAt must be >= 1", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        WatchableOutputStream bytesOut = new WatchableOutputStream();
        bytesOut.setWatcher(this);

        if (indexToWriteAt > 0) {
            bytesOut.write(this.charData.getBytes(), 0,
                (int) (indexToWriteAt - 1));
        }

        return bytesOut;
    }

    /**
     * @see java.sql.Clob#getAsciiStream()
     */
    public InputStream getAsciiStream() throws SQLException {
        if (this.charData != null) {
            return new ByteArrayInputStream(this.charData.getBytes());
        } else {
            return null;
        }
    }

    /**
     * @see java.sql.Clob#setCharacterStream(long)
     */
    public Writer setCharacterStream(long indexToWriteAt)
        throws SQLException {
        if (indexToWriteAt < 1) {
            throw new SQLException("indexToWriteAt must be >= 1", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        WatchableWriter writer = new WatchableWriter();
        writer.setWatcher(this);

		//
		// Don't call write() if nothing to write...
		//
		
        if (indexToWriteAt > 1) {
            writer.write(this.charData, 0, (int) (indexToWriteAt - 1));
        }

        return writer;
    }

    /**
     * @see java.sql.Clob#getCharacterStream()
     */
    public Reader getCharacterStream() throws SQLException {
        if (this.charData != null) {
            return new StringReader(this.charData);
        } else {
            return null;
        }
    }

    /**
     * @see java.sql.Clob#setString(long, String)
     */
    public int setString(long pos, String str) throws SQLException {
        if (pos < 1) {
            throw new SQLException("Starting position can not be < 1", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        if (str == null) {
            throw new SQLException("String to set can not be NULL", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        StringBuffer charBuf = new StringBuffer(this.charData);

        pos--;

        int strLength = str.length();

        charBuf.replace((int) pos, (int) (pos + strLength), str);

        this.charData = charBuf.toString();

        return strLength;
    }

    /**
     * @see java.sql.Clob#setString(long, String, int, int)
     */
    public int setString(long pos, String str, int offset, int len)
        throws SQLException {
        if (pos < 1) {
            throw new SQLException("Starting position can not be < 1", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        if (str == null) {
            throw new SQLException("String to set can not be NULL", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        StringBuffer charBuf = new StringBuffer(this.charData);

        pos--;

        String replaceString = str.substring(offset, len);

        charBuf.replace((int) pos, (int) (pos + replaceString.length()),
            replaceString);

        this.charData = charBuf.toString();

        return len;
    }

    /**
     * @see java.sql.Clob#getSubString(long, int)
     */
    public String getSubString(long startPos, int length)
        throws SQLException {
        if (startPos < 1) {
            throw new SQLException("CLOB start position can not be < 1", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        if (this.charData != null) {
            if (((startPos - 1) + length) > charData.length()) {
                throw new SQLException("CLOB start position + length can not be > length of CLOB",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }

            return this.charData.substring((int) (startPos - 1), length);
        } else {
            return null;
        }
    }

    /**
     * @see java.sql.Clob#length()
     */
    public long length() throws SQLException {
        if (this.charData != null) {
            return this.charData.length();
        } else {
            return 0;
        }
    }

    /**
     * @see java.sql.Clob#position(String, long)
     */
    public long position(String stringToFind, long startPos)
        throws SQLException {
        if (startPos < 1) {
            throw new SQLException("Illegal starting position for search, '"
                + startPos + "'", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
        }

        if (this.charData != null) {
            if ((startPos - 1) > this.charData.length()) {
                throw new SQLException("Starting position for search is past end of CLOB",
                    SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
            }

            int pos = this.charData.indexOf(stringToFind, (int) (startPos - 1));

            return (pos == -1) ? (-1) : (pos + 1);
        } else {
            return -1;
        }
    }

    /**
     * @see java.sql.Clob#position(Clob, long)
     */
    public long position(java.sql.Clob arg0, long arg1)
        throws SQLException {
        return position(arg0.getSubString(0L, (int) arg0.length()), arg1);
    }

    /**
     * @see com.mysql.jdbc.OutputStreamWatcher#streamClosed(byte[])
     */
    public void streamClosed(WatchableOutputStream out) {
    	int streamSize = out.size();
    	
    	if (streamSize < this.charData.length()) {
    		try {
    			out.write(StringUtils.getBytes(this.charData, null, null, false), streamSize, this.charData.length() - streamSize);
    		} catch (UnsupportedEncodingException ex) {
    			//
    		}
    	}
    	
        this.charData = StringUtils.toAsciiString(out.toByteArray());
    }

    /**
     * @see java.sql.Clob#truncate(long)
     */
    public void truncate(long length) throws SQLException {
    	if (length > this.charData.length()) {
    		throw new SQLException("Cannot truncate CLOB of length " 
    		+ this.charData.length() 
			+ " to length of " 
			+ length 
			+ ".");
    	}
    	
        this.charData = this.charData.substring(0, (int) length);
    }

    /**
     * @see com.mysql.jdbc.WriterWatcher#writerClosed(char[])
     */
    public void writerClosed(WatchableWriter out) {
    	int dataLength = out.size();
    	
    	if (dataLength < this.charData.length()) {
    		out.write(this.charData, dataLength, this.charData.length() - dataLength);
    	}
    	
        this.charData = out.toString();
    }
}
