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
      v.addElement(new Double(-1));
      v.addElement(new Double(-1));
      v.addElement(new Double(-1));
      v.addElement( new Date() );
      
      
      boolean b = ((Boolean)xmlrpc.execute("openstreetmap.addPoint", v)).booleanValue();

      System.out.println(b);
    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();
      System.exit(-1);
    }
    

  } // go

}
