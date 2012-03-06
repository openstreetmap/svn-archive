/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This file is based on an origional contained in the GISToolkit project:
 *    http://gistoolkit.sourceforge.net/
 */
package org.geotools.data.shapefile.dbf;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.geotools.resources.NIOUtilities;

/**
 * Class to represent the header of a Dbase III file. Creation date: (5/15/2001
 * 5:15:30 PM)
 * 
 *
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.7.x/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/dbf/DbaseFileHeader.java $
 */
public class DbaseFileHeader {
    // Constant for the size of a record
    private static final int FILE_DESCRIPTOR_SIZE = 32;

    // type of the file, must be 03h
    private static final byte MAGIC = 0x03;

    // Date the file was last updated.
    private Date date = new Date();

    private int recordCnt = 0;

    private int fieldCnt = 0;

    // set this to a default length of 1, which is enough for one "space"
    // character which signifies an empty record
    private int recordLength = 1;

    // set this to a flagged value so if no fields are added before the write,
    // we know to adjust the headerLength to MINIMUM_HEADER
    private int headerLength = -1;

    private int largestFieldSize = 0;

    /**
     * Returns the number of millis at January 1st 4713 BC
     * 
     *  Calendar refCal = (Calendar) new GregorianCalendar(TimeZone.getTimeZone("UTC"));
     *   refCal.set(Calendar.ERA, GregorianCalendar.BC);
     *   refCal.set(Calendar.YEAR, 4713);
     *   refCal.set(Calendar.MONTH, Calendar.JANUARY);
     *   refCal.set(Calendar.DAY_OF_MONTH, 1);
     *   refCal.set(Calendar.HOUR, 12);
     *   refCal.set(Calendar.MINUTE, 0);
     *   refCal.set(Calendar.SECOND, 0);
     *   refCal.set(Calendar.MILLISECOND, 0);
     *   MILLIS_SINCE_4713 = refCal.getTimeInMillis() - 43200000L; 
     *   //(43200000L: 12 hour correction factor taken from DBFViewer2000)
     */
    public static long MILLIS_SINCE_4713 = -210866803200000L;
    
    /**
     * Class for holding the information associated with a record.
     */
    class DbaseField {

        // Field Name
        String fieldName;

        // Field Type (C N L D @ or M)
        char fieldType;

        // Field Data Address offset from the start of the record.
        int fieldDataAddress;

        // Length of the data in bytes
        int fieldLength;

        // Field decimal count in Binary, indicating where the decimal is
        int decimalCount;

    }

    // Collection of header records.
    // lets start out with a zero-length array, just in case
    private DbaseField[] fields = new DbaseField[0];

    private void read(ByteBuffer buffer, ReadableByteChannel channel)
            throws IOException {
        while (buffer.remaining() > 0) {
            if (channel.read(buffer) == -1) {
                throw new EOFException("Premature end of file");
            }
        }
    }

    /**
     * Determine the most appropriate Java Class for representing the data in
     * the field.
     * 
     * <PRE>
     * All packages are java.lang unless otherwise specified.
     * C (Character) -&gt; String
     * N (Numeric)   -&gt; Integer or Long or Double (depends on field's decimal count and fieldLength)
     * F (Floating)  -&gt; Double
     * L (Logical)   -&gt; Boolean
     * D (Date)      -&gt; java.util.Date (Without time)
     * @ (Timestamp) -&gt; java.sql.Timestamp (With time)
     * Unknown       -&gt; String
     * </PRE>
     * 
     * @param i
     *                The index of the field, from 0 to
     *                <CODE>getNumFields() - 1</CODE> .
     * @return A Class which closely represents the dbase field type.
     */
    public Class getFieldClass(int i) {
        Class typeClass = null;

        switch (fields[i].fieldType) {
        case 'C':
            typeClass = String.class;
            break;

        case 'N':
            if (fields[i].decimalCount == 0) {
                if (fields[i].fieldLength < 10) {
                    typeClass = Integer.class;
                } else {
                    typeClass = Long.class;
                }
            } else {
                typeClass = Double.class;
            }
            break;

        case 'F':
            typeClass = Double.class;
            break;

        case 'L':
            typeClass = Boolean.class;
            break;

        case 'D':
            typeClass = Date.class;
            break;
            
        case '@':
            typeClass = Timestamp.class;
            break;
            
        default:
            typeClass = String.class;
            break;
        }

        return typeClass;
    }

    // Retrieve the length of the field at the given index
    /**
     * Returns the field length in bytes.
     * 
     * @param inIndex
     *                The field index.
     * @return The length in bytes.
     */
    public int getFieldLength(int inIndex) {
        return fields[inIndex].fieldLength;
    }

    // Retrieve the Name of the field at the given index
    /**
     * Get the field name.
     * 
     * @param inIndex
     *                The field index.
     * @return The name of the field.
     */
    public String getFieldName(int inIndex) {
        return fields[inIndex].fieldName;
    }

    // Retrieve the type of field at the given index
    /**
     * Get the character class of the field.
     * 
     * @param inIndex
     *                The field index.
     * @return The dbase character representing this field.
     */
    public char getFieldType(int inIndex) {
        return fields[inIndex].fieldType;
    }

    /**
     * Get the date this file was last updated.
     * 
     * @return The Date last modified.
     */
    public Date getLastUpdateDate() { // NO_UCD
        return date;
    }

    /**
     * Return the number of fields in the records.
     * 
     * @return The number of fields in this table.
     */
    public int getNumFields() {
        return fields.length;
    }

    /**
     * Return the number of records in the file
     * 
     * @return The number of records in this table.
     */
    public int getNumRecords() {
        return recordCnt;
    }

    /**
     * Get the length of the records in bytes.
     * 
     * @return The number of bytes per record.
     */
    public int getRecordLength() {
        return recordLength;
    }

    /**
     * Get the length of the header
     * 
     * @return The length of the header in bytes.
     */
    public int getHeaderLength() {
        return headerLength;
    }
    
    /**
     * Read the header data from the DBF file.
     * 
     * @param channel
     *                A readable byte channel. If you have an InputStream you
     *                need to use, you can call
     *                java.nio.Channels.getChannel(InputStream in).
     * @throws IOException
     *                 If errors occur while reading.
     */
    public void readHeader(ReadableByteChannel channel, Charset charset) throws IOException {
        // we'll read in chunks of 1K
        ByteBuffer in = NIOUtilities.allocate(1024);
        try {
            // do this or GO CRAZY
            // ByteBuffers come preset to BIG_ENDIAN !
            in.order(ByteOrder.LITTLE_ENDIAN);
    
            // only want to read first 10 bytes...
            in.limit(10);
    
            read(in, channel);
            in.position(0);
    
            // type of file.
            byte magic = in.get();
            if (magic != MAGIC) {
                throw new IOException("Unsupported DBF file Type "
                        + Integer.toHexString(magic));
            }
    
            // parse the update date information.
            int tempUpdateYear = in.get();
            int tempUpdateMonth = in.get();
            int tempUpdateDay = in.get();
            // ouch Y2K uncompliant
            if (tempUpdateYear > 90) {
                tempUpdateYear = tempUpdateYear + 1900;
            } else {
                tempUpdateYear = tempUpdateYear + 2000;
            }
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, tempUpdateYear);
            c.set(Calendar.MONTH, tempUpdateMonth - 1);
            c.set(Calendar.DATE, tempUpdateDay);
            date = c.getTime();
    
            // read the number of records.
            recordCnt = in.getInt();
    
            // read the length of the header structure.
            // ahhh.. unsigned little-endian shorts
            // mask out the byte and or it with shifted 2nd byte
            headerLength = (in.get() & 0xff) | ((in.get() & 0xff) << 8);
    
            // if the header is bigger than our 1K, reallocate
            if (headerLength > in.capacity()) {
                NIOUtilities.clean(in, false);
                in = NIOUtilities.allocate(headerLength - 10);
            }
            in.limit(headerLength - 10);
            in.position(0);
            read(in, channel);
            in.position(0);
    
            // read the length of a record
            // ahhh.. unsigned little-endian shorts
            recordLength = (in.get() & 0xff) | ((in.get() & 0xff) << 8);
    
            // skip the reserved bytes in the header.
            in.position(in.position() + 20);
    
            // calculate the number of Fields in the header
            fieldCnt = (headerLength - FILE_DESCRIPTOR_SIZE - 1)
                    / FILE_DESCRIPTOR_SIZE;
    
            // read all of the header records
            List lfields = new ArrayList();
            for (int i = 0; i < fieldCnt; i++) {
                DbaseField field = new DbaseField();
    
                // read the field name
                byte[] buffer = new byte[11];
                in.get(buffer);
                String name = new String(buffer, charset.name());
                int nullPoint = name.indexOf(0);
                if (nullPoint != -1) {
                    name = name.substring(0, nullPoint);
                }
                field.fieldName = name.trim();
    
                // read the field type
                field.fieldType = (char) in.get();
    
                // read the field data address, offset from the start of the record.
                field.fieldDataAddress = in.getInt();
    
                // read the field length in bytes
                int length = in.get();
                if (length < 0) {
                    length = length + 256;
                }
                field.fieldLength = length;
    
                if (length > largestFieldSize) {
                    largestFieldSize = length;
                }
    
                // read the field decimal count in bytes
                field.decimalCount = in.get();
    
                // reserved bytes.
                // in.skipBytes(14);
                in.position(in.position() + 14);
    
                // some broken shapefiles have 0-length attributes. The reference
                // implementation
                // (ArcExplorer 2.0, built with MapObjects) just ignores them.
                if (field.fieldLength > 0) {
                    lfields.add(field);
                }
            }
    
            // Last byte is a marker for the end of the field definitions.
            // in.skipBytes(1);
            in.position(in.position() + 1);
    
            fields = new DbaseField[lfields.size()];
            fields = (DbaseField[]) lfields.toArray(fields);
        } finally {
            NIOUtilities.clean(in, false);
        }
    }
    
    /**
     * Read the header data from the DBF file.
     * 
     * @param channel
     *                A readable byte channel. If you have an InputStream you
     *                need to use, you can call
     *                java.nio.Channels.getChannel(InputStream in).
     * @throws IOException
     *                 If errors occur while reading.
     */
    public void readHeader(ByteBuffer in) throws IOException {
        // do this or GO CRAZY
        // ByteBuffers come preset to BIG_ENDIAN !
        in.order(ByteOrder.LITTLE_ENDIAN);

        // type of file.
        byte magic = in.get();
        if (magic != MAGIC) {
            throw new IOException("Unsupported DBF file Type "
                    + Integer.toHexString(magic));
        }

        // parse the update date information.
        int tempUpdateYear = in.get();
        int tempUpdateMonth = in.get();
        int tempUpdateDay = in.get();
        // ouch Y2K uncompliant
        if (tempUpdateYear > 90) {
            tempUpdateYear = tempUpdateYear + 1900;
        } else {
            tempUpdateYear = tempUpdateYear + 2000;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, tempUpdateYear);
        c.set(Calendar.MONTH, tempUpdateMonth - 1);
        c.set(Calendar.DATE, tempUpdateDay);
        date = c.getTime();

        // read the number of records.
        recordCnt = in.getInt();

        // read the length of the header structure.
        // ahhh.. unsigned little-endian shorts
        // mask out the byte and or it with shifted 2nd byte
        headerLength = (in.get() & 0xff) | ((in.get() & 0xff) << 8);

        // if the header is bigger than our 1K, reallocate
        if (headerLength > in.capacity()) {
            throw new IllegalArgumentException("The contract says the buffer should be long enough to fit all the header!");
        }

        // read the length of a record
        // ahhh.. unsigned little-endian shorts
        recordLength = (in.get() & 0xff) | ((in.get() & 0xff) << 8);

        // skip the reserved bytes in the header.
        in.position(in.position() + 20);

        // calculate the number of Fields in the header
        fieldCnt = (headerLength - FILE_DESCRIPTOR_SIZE - 1)
                / FILE_DESCRIPTOR_SIZE;

        // read all of the header records
        List lfields = new ArrayList();
        for (int i = 0; i < fieldCnt; i++) {
            DbaseField field = new DbaseField();

            // read the field name
            byte[] buffer = new byte[11];
            in.get(buffer);
            String name = new String(buffer);
            int nullPoint = name.indexOf(0);
            if (nullPoint != -1) {
                name = name.substring(0, nullPoint);
            }
            field.fieldName = name.trim();

            // read the field type
            field.fieldType = (char) in.get();

            // read the field data address, offset from the start of the record.
            field.fieldDataAddress = in.getInt();

            // read the field length in bytes
            int length = in.get();
            if (length < 0) {
                length = length + 256;
            }
            field.fieldLength = length;

            if (length > largestFieldSize) {
                largestFieldSize = length;
            }

            // read the field decimal count in bytes
            field.decimalCount = in.get();

            // reserved bytes.
            // in.skipBytes(14);
            in.position(in.position() + 14);

            // some broken shapefiles have 0-length attributes. The reference
            // implementation
            // (ArcExplorer 2.0, built with MapObjects) just ignores them.
            if (field.fieldLength > 0) {
                lfields.add(field);
            }
        }

        // Last byte is a marker for the end of the field definitions.
        // in.skipBytes(1);
        in.position(in.position() + 1);

        fields = new DbaseField[lfields.size()];
        fields = (DbaseField[]) lfields.toArray(fields);
    }

    /**
     * Get a simple representation of this header.
     * 
     * @return A String representing the state of the header.
     */
    public String toString() {
        StringBuffer fs = new StringBuffer();
        for (int i = 0, ii = fields.length; i < ii; i++) {
            DbaseField f = fields[i];
            fs.append(f.fieldName + " " + f.fieldType + " " + f.fieldLength
                    + " " + f.decimalCount + " " + f.fieldDataAddress + "\n");
        }

        return "DB3 Header\n" + "Date : " + date + "\n" + "Records : "
                + recordCnt + "\n" + "Fields : " + fieldCnt + "\n" + fs;

    }

}
