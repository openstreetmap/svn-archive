
package org.openstreetmap.util;

import java.util.*;
import java.lang.*;
import org.apache.xmlrpc.*;

class osmTestServer
{
  public static void main(String[] args)
  {

    new osmTestServer().go(args[0], args[1]);

  } // main

  public void go(String user, String pass)
  {
    try
    {
      XmlRpcClient xmlrpc = new XmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");

      Vector v = new Vector();

      v.addElement(user);
      v.addElement(pass);
     
      String token = (String)xmlrpc.execute("openstreetmap.login", v);
      System.out.println(token);

      v = new Vector();

      v.addElement(token);
      v.addElement(new Boolean(true));
      
      
      Vector vResults = (Vector)xmlrpc.execute("openstreetmap.getAllKeys",v);

      Enumeration e = vResults.elements();

      while(e.hasMoreElements())
      {
        String sA = (String)e.nextElement();
        System.out.println(sA);
      }
      
    }
    catch(Exception e)
    {
      System.out.println("eek");
      System.out.println(e);
      e.printStackTrace();
      System.exit(-1);
    }
    

  } // go

}
