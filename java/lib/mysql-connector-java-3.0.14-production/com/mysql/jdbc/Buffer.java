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

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;


/**
 * Buffer contains code to read and write packets from/to the MySQL server.
 *
 * @version $Id: Buffer.java,v 1.15.2.14 2004/04/18 21:02:49 mmatthew Exp $
 * @author Mark Matthews
 */
class Buffer {
    static final int NO_LENGTH_LIMIT = -1;
    static final long NULL_LENGTH = -1;
    private byte[] byteBuffer;
    private boolean wasMultiPacket = false;
    private int bufLength = 0;
    private int position = 0;
    private int sendLength = 0;

    Buffer(byte[] buf) {
        this.byteBuffer = buf;
        setBufLength(buf.length);
    }

    Buffer(int size) {
        this.byteBuffer = new byte[size];
        setBufLength(this.byteBuffer.length);
        this.position = MysqlIO.HEADER_LENGTH;
    }

    /**
     * Sets the array of bytes to use as a buffer to read from.
     *
     * @param byteBuffer the array of bytes to use as a buffer
     */
    public void setByteBuffer(byte[] byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    /**
     * Returns the array of bytes this Buffer is using to read from.
     *
     * @return byte array being read from
     */
    public byte[] getByteBuffer() {
        return this.byteBuffer;
    }

    /**
     * Set the current position to write to/ read from
     *
     * @param position the position (0-based index)
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the current position to write to/ read from
     *
     * @return the current position to write to/ read from
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Sets whether this packet was part of a multipacket
     *
     * @param flag was this packet part of a multipacket?
     */
    public void setWasMultiPacket(boolean flag) {
        wasMultiPacket = flag;
    }

    /**
     * Skip over a length-encoded string
     *
     * @return The position past the end of the string
     */
    public int fastSkipLenString() {
        long len = this.readFieldLength();

        position += len;

        return (int) len; // this is safe, as this is only
    }

    /**
     * Was this packet part of a multipacket?
     *
     * @return was this packet part of a multipacket?
     */
    public boolean wasMultiPacket() {
        return wasMultiPacket;
    }

    protected final byte[] getBufferSource() {
        return byteBuffer;
    }

    final byte[] getBytes(int len) {
        byte[] b = new byte[len];
        System.arraycopy(this.byteBuffer, this.position, b, 0, len);
        this.position += len; // update cursor

        return b;
    }

    // 2000-06-05 Changed
    final boolean isLastDataPacket() {
        return ((getBufLength() < 9) && ((this.byteBuffer[0] & 0xff) == 254));
    }

    final void clear() {
        this.position = MysqlIO.HEADER_LENGTH;
    }

    final void dump() {
    	StringUtils.dumpAsHex(this.byteBuffer, getBufLength());
    }

    final void dumpHeader() {
        for (int i = 0; i < MysqlIO.HEADER_LENGTH; i++) {
            String hexVal = Integer.toHexString((int) this.byteBuffer[i] & 0xff);

            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }

            System.out.print(hexVal + " ");
        }
    }

    final void dumpNBytes(int start, int nBytes) {
        StringBuffer asciiBuf = new StringBuffer();

        for (int i = start;
                (i < (start + nBytes)) && (i < this.byteBuffer.length); i++) {
            String hexVal = Integer.toHexString((int) this.byteBuffer[i] & 0xff);

            if (hexVal.length() == 1) {
                hexVal = "0" + hexVal;
            }

            System.out.print(hexVal + " ");

            if ((this.byteBuffer[i] > 32) && (this.byteBuffer[i] < 127)) {
                asciiBuf.append((char) this.byteBuffer[i]);
            } else {
                asciiBuf.append(".");
            }

            asciiBuf.append(" ");
        }

        System.out.println("    " + asciiBuf.toString());
    }

    final void ensureCapacity(int additionalData) throws SQLException {
        if ((this.position + additionalData) > getBufLength()) {
            if ((this.position + additionalData) < this.byteBuffer.length) {
                // byteBuffer.length is != getBufLength() all of the time
                // due to re-using of packets (we don't shrink them)
                //
                // If we can, don't re-alloc, just set buffer length 
                // to size of current buffer
                setBufLength(this.byteBuffer.length);
            } else {
                //
                // Otherwise, re-size, and pad so we can avoid
                // allocing again in the near future
                //
                int newLength = (int) (this.byteBuffer.length * 1.25);

                if (newLength < (this.byteBuffer.length + additionalData)) {
                    newLength = this.byteBuffer.length
                        + (int) (additionalData * 1.25);
                }

                if (newLength < this.byteBuffer.length) {
                    newLength = this.byteBuffer.length + additionalData;
                }

                byte[] newBytes = new byte[newLength];

                System.arraycopy(this.byteBuffer, 0, newBytes, 0,
                    this.byteBuffer.length);
                this.byteBuffer = newBytes;
                setBufLength(this.byteBuffer.length);
            }
        }
    }

    final long newReadLength() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
        case 251:
            return (long) 0;

        case 252:
            return (long) readInt();

        case 253:
            return (long) readLongInt();

        case 254: // changed for 64 bit lengths
            return (long) readLongLong();

        default:
            return (long) sw;
        }
    }

    final byte readByte() {
        return this.byteBuffer[this.position++];
    }

    final long readFieldLength() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
        case 251:
            return NULL_LENGTH;

        case 252:
            return (long) readInt();

        case 253:
            return (long) readLongInt();

        case 254:
            return readLongLong();

        default:
            return (long) sw;
        }
    }

    // 2000-06-05 Changed
    final int readInt() {
        byte[] b = this.byteBuffer; // a little bit optimization

        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8);
    }

    final byte[] readLenByteArray(int offset) {
        long len = this.readFieldLength();

        if (len == NULL_LENGTH) {
            return null;
        }

        if (len == 0) {
            return new byte[0];
        }

        this.position += offset;

        return getBytes((int) len);
    }

    final long readLength() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
        case 251:
            return (long) 0;

        case 252:
            return (long) readInt();

        case 253:
            return (long) readLongInt();

        case 254:
            return (long) readLong();

        default:
            return (long) sw;
        }
    }

    // 2000-06-05 Fixed
    final long readLong() {
        byte[] b = this.byteBuffer;

        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8)
        | ((b[this.position++] & 0xff) << 16)
        | ((b[this.position++] & 0xff) << 24);
    }

    // 2000-06-05 Changed
    final int readLongInt() {
        byte[] b = this.byteBuffer;

        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8)
        | ((b[this.position++] & 0xff) << 16);
    }

    // 2000-06-05 Fixed
    final long readLongLong() {
        byte[] b = this.byteBuffer;

        return (long) (b[this.position++] & 0xff)
        | ((long) (b[this.position++] & 0xff) << 8)
        | ((long) (b[this.position++] & 0xff) << 16)
        | ((long) (b[this.position++] & 0xff) << 24)
        | ((long) (b[this.position++] & 0xff) << 32)
        | ((long) (b[this.position++] & 0xff) << 40)
        | ((long) (b[this.position++] & 0xff) << 48)
        | ((long) (b[this.position++] & 0xff) << 56);
    }

    //
    // Read a null-terminated string
    //
    // To avoid alloc'ing a new byte array, we
    // do this by hand, rather than calling getNullTerminatedBytes()
    //
    final String readString() {
        int i = this.position;
        int len = 0;
		int maxLen = getBufLength();
		
        while ((i < maxLen) && (this.byteBuffer[i] != 0)) {
            len++;
            i++;
        }

        String s = new String(this.byteBuffer, this.position, len);
        this.position += (len + 1); // update cursor

        return s;
    }
    
    final String readString(String encoding) throws SQLException {
		int i = this.position;
		int len = 0;
		int maxLen = getBufLength();
		
		while ((i < maxLen) && (this.byteBuffer[i] != 0)) {
			len++;
			i++;
		}

		this.position += (len + 1); // update cursor
		
		try {
			return new String(this.byteBuffer, this.position, len, encoding);
			
		} catch (UnsupportedEncodingException uEE) {
			throw new SQLException("Unsupported character encoding '" + encoding + "'", SQLError.SQL_STATE_ILLEGAL_ARGUMENT);
		}
    }

    final int readnBytes() {
        int sw = this.byteBuffer[this.position++] & 0xff;

        switch (sw) {
        case 1:
            return this.byteBuffer[this.position++] & 0xff;

        case 2:
            return this.readInt();

        case 3:
            return this.readLongInt();

        case 4:
            return (int) this.readLong();

        default:
            return 255;
        }
    }

    final void writeByte(byte b) throws SQLException {
        ensureCapacity(1);

        this.byteBuffer[this.position++] = b;
    }

    // Write a byte array
    final void writeBytesNoNull(byte[] bytes) throws SQLException {
        int len = bytes.length;
        ensureCapacity(len);
        System.arraycopy(bytes, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    // Write a byte array with the given offset and length
    final void writeBytesNoNull(byte[] bytes, int offset, int length)
        throws SQLException {
        ensureCapacity(length);
        System.arraycopy(bytes, offset, this.byteBuffer, this.position, length);
        this.position += length;
    }

    final void writeDouble(double d) {
        long l = Double.doubleToLongBits(d);
        writeLongLong(l);
    }

    final void writeFloat(float f) {
        int i = Float.floatToIntBits(f);
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
    }

    // 2000-06-05 Changed
    final void writeInt(int i) {
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
    }

    // 2000-06-05 Changed
    final void writeLong(long i) {
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
    }

    // 2000-06-05 Changed
    final void writeLongInt(int i) {
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
    }

    final void writeLongLong(long i) {
        byte[] b = this.byteBuffer;
        b[this.position++] = (byte) (i & 0xff);
        b[this.position++] = (byte) (i >>> 8);
        b[this.position++] = (byte) (i >>> 16);
        b[this.position++] = (byte) (i >>> 24);
        b[this.position++] = (byte) (i >>> 32);
        b[this.position++] = (byte) (i >>> 40);
        b[this.position++] = (byte) (i >>> 48);
        b[this.position++] = (byte) (i >>> 56);
    }

    // Write null-terminated string
    final void writeString(String s) throws SQLException {
    	ensureCapacity(s.length() + 1);
    	
        writeStringNoNull(s);
        this.byteBuffer[this.position++] = 0;
    }

    // Write string, with no termination
    final void writeStringNoNull(String s) throws SQLException {
        int len = s.length();
        ensureCapacity(len);
        System.arraycopy(s.getBytes(), 0, byteBuffer, position, len);
        position += len;

        //         for (int i = 0; i < len; i++)
        //         {
        //             this.byteBuffer[this.position++] = (byte)s.charAt(i);
        //         }
    }

    // Write a String using the specified character
    // encoding
    final void writeStringNoNull(String s, String encoding, 
    		SingleByteCharsetConverter converter, boolean parserKnowsUnicode)
        throws UnsupportedEncodingException, SQLException {
        byte[] b = null;
        
        if (converter != null) {
            b = converter.toBytes(s);
        } else {
            b = StringUtils.getBytes(s, encoding, parserKnowsUnicode);
        }

        int len = b.length;
        ensureCapacity(len);
        System.arraycopy(b, 0, this.byteBuffer, this.position, len);
        this.position += len;
    }

    void setBufLength(int bufLength) {
        this.bufLength = bufLength;
    }

    int getBufLength() {
        return bufLength;
    }

    void setSendLength(int sendLength) {
        this.sendLength = sendLength;
    }
}
