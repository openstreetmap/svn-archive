import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;

public class osmServerClient
{

  public osmServerClient()
  {


  } // osmServerClient


  public Vector getPoints()
  {
    Vector gpsPoints = new Vector();
    
    try{
      
      Socket s = new Socket("128.40.59.181", 2001);;

      BufferedReader in = new BufferedReader(new InputStreamReader(
            s.getInputStream()));

      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
            s.getOutputStream()));


      out.write("GETPOINTS\n");

      out.flush();

      System.out.println("reading POINTS");
      
      String sLine = in.readLine();

      System.out.println("Server said " + sLine);

      while( (sLine = in.readLine()) != null)
      {

        if(sLine.equals("END"))
        {
          System.out.println("breaking on END");
          break;
        }

        
        StringTokenizer st = new StringTokenizer(sLine);

        String a = st.nextToken();
        String b = st.nextToken();
        String c = st.nextToken();
        String d = st.nextToken();

        gpsPoints.add( new gpspoint(a,b,c,d) );
      
        
      }

      System.out.println("done getting points");
    
    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      
      System.exit(-1);

    }

    return gpsPoints;
  
  } // getPoints


} // osmServerClient
