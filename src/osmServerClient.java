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
      
      Socket s = new Socket("128.40.59.181", 3141);;

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


        gpsPoints.add( new gpspoint(st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken() ) );
      
        
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
