
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
      XmlRpcClientLite xmlrpc = new XmlRpcClientLite("http://128.40.59.181:4000/");

      Vector v = new Vector();

      v.addElement(user);
      v.addElement(pass);
     
      String token = (String)xmlrpc.execute("openstreetmap.login", v);
      System.out.println(token);
      
      
      v = new Vector();

      v.addElement(token);
      v.addElement(new Double(100));
      v.addElement(new Double(100));
      v.addElement(new Double(-100));
      v.addElement(new Double(-100));
      
      Vector o = (Vector)xmlrpc.execute("openstreetmap.getFullPoints",v);

      Enumeration e = o.elements();

      while(e.hasMoreElements())
      {
        System.out.print(e.nextElement() +  " ");

        
      }
      
      System.out.println();
      System.out.println();
      System.out.println("adding a point");
      
      v = new Vector();

      v.addElement(token);
      v.addElement(new Double(-1)); // lat
      v.addElement(new Double(-1)); // lon
      v.addElement(new Double(-1)); // alt
      v.addElement( new Date() ); // timestamp for point
      v.addElement(new Double(-1)); // hor_dilution
      v.addElement(new Double(-1)); // vert_dilution
      v.addElement(new Integer(-1)); // track_id
      v.addElement(new Integer(255)); // quality
      v.addElement(new Integer(255)); // satellites
      
      
      boolean b = ((Boolean)xmlrpc.execute("openstreetmap.addPoint", v)).booleanValue();

      System.out.println(b);
      
      System.out.println();
      System.out.println();
      System.out.println("dropping the point");

      v = new Vector();
      v.addElement(token);
      v.addElement( new Double(-1));
      v.addElement( new Double(-1));
    
      b = ((Boolean)xmlrpc.execute("openstreetmap.dropPoint", v)).booleanValue();
      
      System.out.println(b);
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
