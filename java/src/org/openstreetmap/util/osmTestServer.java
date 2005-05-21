
package org.openstreetmap.util;

import java.util.*;
import java.lang.*;
import org.apache.xmlrpc.applet.*;

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
      SimpleXmlRpcClient xmlrpc = new SimpleXmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");

      Vector v = new Vector();

      v.addElement(user);
      v.addElement(pass);
     
      String token = (String)xmlrpc.execute("openstreetmap.login", v);

      System.out.println("logged in with token " + token);

      v = new Vector();

      v.addElement(new Integer(1));
      v.addElement(token);
      
      Vector bob = (Vector)xmlrpc.execute("openstreetmap.getNode",v);
      
      System.out.println(bob);
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
