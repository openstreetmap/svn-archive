package org.apache.xmlrpc.applet;

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

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;


/**
 * An applet that can be accessed via LiveConnect from JavaScript. It provides
 * methods for adding arguments and triggering method execution for XML-RPC
 * requests. This works on recent Netscape 4.x browsers as well as Internet
 * Explorer 4.0 on Windows 95/NT, but not on IE/Mac. <p>
 *
 * Results from XML-RPC calls are exposed to JavaScript as the are, i.e.
 * &lt;structs>s are <a href=http://java.sun.com/products/jdk/1.1/docs/api/java.util.Hashtable.html>Hashtables</a>
 * and &lt;array>s are <a href=http://java.sun.com/products/jdk/1.1/docs/api/java.util.Vector.html>Vectors</a>
 * and can be accessed thru their public methods. It seems like Date objects are
 * not converted properly between JavaScript and Java, so the dateArg methods
 * take long values instead of Date objects as parameters (date.getTime()).
 *
 * @version $Id: JSXmlRpcApplet.java,v 1.3 2002/03/20 15:11:03 mpoeschl Exp $
 */
public class JSXmlRpcApplet extends XmlRpcApplet
{
    public Object loaded = null;

    private String errorMessage;
    private Vector arguments;

    /**
     *
     */
    public void init()
    {
        initClient();
        arguments = new Vector();
        loaded = Boolean.TRUE;
        System.out.println("JSXmlRpcApplet initialized");
    }

    // add ints (primitve != object) to structs, vectors
    public void addIntArg(int value)
    {
        arguments.addElement(new Integer(value));
    }

    public void addIntArgToStruct(Hashtable struct, String key, int value)
    {
        struct.put(key, new Integer(value));
    }

    public void addIntArgToArray(Vector ary, int value)
    {
        ary.addElement(new Integer(value));
    }

    // add floats/doubles to structs, vectors
    public void addDoubleArg(float value)
    {
        arguments.addElement(new Double(value));
    }

    public void addDoubleArgToStruct(Hashtable struct, String key, float value)
    {
        struct.put(key, new Double(value));
    }

    public void addDoubleArgToArray(Vector ary, float value)
    {
        ary.addElement(new Double(value));
    }

    public void addDoubleArg(double value)
    {
        arguments.addElement(new Double(value));
    }

    public void addDoubleArgToStruct(Hashtable struct, String key, double value)
    {
        struct.put(key, new Double(value));
    }

    public void addDoubleArgToArray(Vector ary, double value)
    {
        ary.addElement(new Double(value));
    }

    // add bools to structs, vectors
    public void addBooleanArg(boolean value)
    {
        arguments.addElement(new Boolean(value));
    }

    public void addBooleanArgToStruct(Hashtable struct, String key,
            boolean value)
    {
        struct.put(key, new Boolean(value));
    }

    public void addBooleanArgToArray(Vector ary, boolean value)
    {
        ary.addElement(new Boolean(value));
    }

    // add Dates to structs, vectors Date argument in SystemTimeMillis (seems to be the way)
    public void addDateArg(long dateNo)
    {
        arguments.addElement(new Date(dateNo));
    }

    public void addDateArgToStruct(Hashtable struct, String key, long dateNo)
    {
        struct.put(key, new Date(dateNo));
    }

    public void addDateArgToArray(Vector ary, long dateNo)
    {
        ary.addElement(new Date(dateNo));
    }

    // add String arguments
    public void addStringArg(String str)
    {
        arguments.addElement(str);
    }

    public void addStringArgToStruct(Hashtable struct, String key, String str)
    {
        struct.put(key, str);
    }

    public void addStringArgToArray(Vector ary, String str)
    {
        ary.addElement (str);
    }

    // add Array arguments
    public Vector addArrayArg()
    {
        Vector v = new Vector();
        arguments.addElement(v);
        return v;
    }

    public Vector addArrayArgToStruct(Hashtable struct, String key)
    {
        Vector v = new Vector();
        struct.put(key, v);
        return v;
    }

    public Vector addArrayArgToArray(Vector ary)
    {
        Vector v = new Vector();
        ary.addElement(v);
        return v;
    }

    // add Struct arguments
    public Hashtable addStructArg()
    {
        Hashtable ht = new Hashtable();
        arguments.addElement(ht);
        return ht;
    }

    public Hashtable addStructArgToStruct(Hashtable struct, String key)
    {
        Hashtable ht = new Hashtable();
        struct.put(key, ht);
        return ht;
    }

    public Hashtable addStructArgToArray(Vector ary)
    {
        Hashtable ht = new Hashtable();
        ary.addElement(ht);
        return ht;
    }

    // get the errorMessage, null if none
    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void reset()
    {
        arguments = new Vector();
    }

    public Object execute(String methodName)
    {
        // XmlRpcSupport.setDebug (true);
        errorMessage = null;
        showStatus("Connecting to Server...");
        Object returnValue = null;
        try
        {
            returnValue = execute(methodName, arguments);
        }
        catch (Exception e)
        {
            errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage == "")
            {
                errorMessage = e.toString();
            }
        }
        // reset argument array for reuse
        arguments = new Vector();

        showStatus("");
        return returnValue;
    }
}
