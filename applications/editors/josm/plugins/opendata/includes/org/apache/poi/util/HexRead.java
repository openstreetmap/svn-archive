/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to read hex from files.
 * TODO - move to test packages
 *
 * @author Marc Johnson
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class HexRead
{
    static public byte[] readData( InputStream stream, int eofChar )
            throws IOException
    {
        int characterCount = 0;
        byte b = (byte) 0;
        List<Byte> bytes = new ArrayList<Byte>();
        boolean done = false;
        while ( !done )
        {
            int count = stream.read();
            char baseChar = 'a';
            if ( count == eofChar ) break;
            switch ( count )
            {
                case '#':
                    readToEOL( stream );
                    break;
                case '0': case '1': case '2': case '3': case '4': case '5':
                case '6': case '7': case '8': case '9':
                    b <<= 4;
                    b += (byte) ( count - '0' );
                    characterCount++;
                    if ( characterCount == 2 )
                    {
                        bytes.add( Byte.valueOf( b ) );
                        characterCount = 0;
                        b = (byte) 0;
                    }
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                    baseChar = 'A';
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                    b <<= 4;
                    b += (byte) ( count + 10 - baseChar );
                    characterCount++;
                    if ( characterCount == 2 )
                    {
                        bytes.add( Byte.valueOf( b ) );
                        characterCount = 0;
                        b = (byte) 0;
                    }
                    break;
                case -1:
                    done = true;
                    break;
                default :
                    break;
            }
        }
        Byte[] polished = bytes.toArray( new Byte[0] );
        byte[] rval = new byte[polished.length];
        for ( int j = 0; j < polished.length; j++ )
        {
            rval[j] = polished[j].byteValue();
        }
        return rval;
    }

    static public byte[] readFromString(String data) {
        try {
            return readData(new ByteArrayInputStream( data.getBytes() ), -1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static private void readToEOL( InputStream stream ) throws IOException
    {
        int c = stream.read();
        while ( c != -1 && c != '\n' && c != '\r' )
        {
            c = stream.read();
        }
    }
}
