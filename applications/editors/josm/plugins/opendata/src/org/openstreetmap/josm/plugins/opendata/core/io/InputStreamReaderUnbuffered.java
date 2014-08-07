// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * An InputStreamReader that only consumes as many bytes as is necessary.
 * It does not do any read-ahead. From http://stackoverflow.com/q/2631507/2257172
 */
public class InputStreamReaderUnbuffered extends Reader {
    
    private final CharsetDecoder charsetDecoder;
    private final InputStream inputStream;
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1);

    public InputStreamReaderUnbuffered(InputStream in, Charset charset) {
        inputStream = in;
        charsetDecoder = charset.newDecoder();
    }

    @Override
    public int read() throws IOException {
        boolean middleOfReading = false;

        while (true) {
            int b = inputStream.read();

            if (b == -1) {
                if (middleOfReading)
                    throw new IOException(
                            "Unexpected end of stream, byte truncated");

                return -1;
            }

            byteBuffer.clear();
            byteBuffer.put((byte) b);
            byteBuffer.flip();

            CharBuffer charBuffer = charsetDecoder.decode(byteBuffer);

            // although this is theoretically possible this would violate the
            // unbuffered nature of this class so we throw an exception
            if (charBuffer.length() > 1)
                throw new IOException(
                        "Decoded multiple characters from one byte!");

            if (charBuffer.length() == 1)
                return charBuffer.get();

            middleOfReading = true;
        }
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            int ch = read();

            if (ch == -1)
                return i == 0 ? -1 : i;

            cbuf[i] = (char) ch;
        }

        return len;
    }

    public void close() throws IOException {
        inputStream.close();
    }
}
