package org.apache.xmlrpc;

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright(c) 2002 The Apache Software Foundation.  All rights
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
 *        Apache Software Foundation(http://www.apache.org/)."
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
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES(INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Introspects handlers using Java Reflection to call methods matching
 * a XML-RPC call.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 */
public class Invoker implements XmlRpcHandler
{
    private Object invokeTarget;
    private Class targetClass;

    public Invoker(Object target)
    {
        invokeTarget = target;
        targetClass = (invokeTarget instanceof Class) ? (Class) invokeTarget :
                invokeTarget.getClass();
        if (XmlRpc.debug)
        {
            System.out.println("Target object is " + targetClass);
        }
    }

    /**
     * main method, sucht methode in object, wenn gefunden dann aufrufen.
     */
    public Object execute(String methodName, Vector params) throws Exception
    {
        // Array mit Classtype bilden, ObjectAry mit Values bilden
        Class[] argClasses = null;
        Object[] argValues = null;
        if (params != null)
        {
            argClasses = new Class[params.size()];
            argValues = new Object[params.size()];
            for (int i = 0; i < params.size(); i++)
            {
                argValues[i] = params.elementAt(i);
                if (argValues[i] instanceof Integer)
                {
                    argClasses[i] = Integer.TYPE;
                }
                else if (argValues[i] instanceof Double)
                {
                    argClasses[i] = Double.TYPE;
                }
                else if (argValues[i] instanceof Boolean)
                {
                    argClasses[i] = Boolean.TYPE;
                }
                else
                {
                    argClasses[i] = argValues[i].getClass();
                }
            }
        }

        // Methode da ?
        Method method = null;

        // The last element of the XML-RPC method name is the Java
        // method name.
        int dot = methodName.lastIndexOf('.');
        if (dot > -1 && dot + 1 < methodName.length())
        {
            methodName = methodName.substring(dot + 1);
        }

        if (XmlRpc.debug)
        {
            System.out.println("Searching for method: " + methodName +
                               " in class " + targetClass.getName());
            for (int i = 0; i < argClasses.length; i++)
            {
                System.out.println("Parameter " + i + ": " + argValues[i]
                        + " (" + argClasses[i] + ')');
            }
        }

        try
        {
            method = targetClass.getMethod(methodName, argClasses);
        }
        // Wenn nicht da dann entsprechende Exception returnen
        catch(NoSuchMethodException nsm_e)
        {
            throw nsm_e;
        }
        catch(SecurityException s_e)
        {
            throw s_e;
        }

        // Our policy is to make all public methods callable except
        // the ones defined in java.lang.Object.
        if (method.getDeclaringClass() == Object.class)
        {
            throw new XmlRpcException(0, "Invoker can't call methods "
                    + "defined in java.lang.Object");
        }

        // invoke
        Object returnValue = null;
        try
        {
            returnValue = method.invoke(invokeTarget, argValues);
        }
        catch(IllegalAccessException iacc_e)
        {
            throw iacc_e;
        }
        catch(IllegalArgumentException iarg_e)
        {
            throw iarg_e;
        }
        catch(InvocationTargetException it_e)
        {
            if (XmlRpc.debug)
            {
                it_e.getTargetException().printStackTrace();
            }
            // check whether the thrown exception is XmlRpcException
            Throwable t = it_e.getTargetException();
            if (t instanceof XmlRpcException)
            {
                throw (XmlRpcException) t;
            }
            // It is some other exception
            throw new Exception(t.toString());
        }
        if (returnValue == null && method.getReturnType() == Void.TYPE)
        {
            // Not supported by the spec.
            throw new IllegalArgumentException
                ("void return types for handler methods not supported");
        }
        return returnValue;
    }
}
