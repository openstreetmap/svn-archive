package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "XML-RPC" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @version $Id: ServerInputStream.java,v 1.3 2002/03/20 15:11:03 mpoeschl Exp $
 */
class ServerInputStream extends InputStream
{
    // bytes remaining to be read from the input stream. This is
    // initialized from CONTENT_LENGTH (or getContentLength()).
    // This is used in order to correctly return a -1 when all the
    // data POSTed was read. If this is left to -1, content length is
    // assumed as unknown and the standard InputStream methods will be used
    private long available = -1;
    private long markedAvailable;

    private BufferedInputStream in;

    /**
     *
     * @param in
     * @param available
     */
    public ServerInputStream(BufferedInputStream in, int available)
    {
        this.in = in;
        this.available = available;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public int read() throws IOException
    {
        if (available > 0)
        {
            available--;
            return in.read();
        }
        else if (available == -1)
        {
            return in.read ();
        }
        return -1;
    }

    /**
     *
     * @param b
     * @return
     * @throws IOException
     */
    public int read(byte b[]) throws IOException
    {
        return read(b, 0, b.length);
    }

    /**
     *
     * @param b
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public int read(byte b[], int off, int len) throws IOException
    {
        if (available > 0)
        {
            if (len > available)
            {
                // shrink len
                len = (int) available;
            }
            int read = in.read(b, off, len);
            if (read != -1)
            {
                available -= read;
            }
            else
            {
                available = -1;
            }
            return read;
        }
        else if (available == -1)
        {
            return in.read(b, off, len);
        }
        return -1;
    }

    /**
     *
     * @param n
     * @return
     * @throws IOException
     */
    public long skip(long n) throws IOException
    {
        long skip = in.skip(n);
        if (available > 0)
        {
            available -= skip;
        }
        return skip;
    }

    /**
     *
     * @param readlimit
     */
    public void mark(int readlimit)
    {
        in.mark(readlimit);
        markedAvailable = available;
    }

    /**
     *
     * @throws IOException
     */
    public void reset() throws IOException
    {
        in.reset();
        available = markedAvailable;
    }

    /**
     *
     * @return
     */
    public boolean markSupported()
    {
        return true;
    }
}
