import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

public class osmServerHandler implements Runnable
{

  Socket s;
  osmServerSQLHandler osmSQLH;

  
  public osmServerHandler(Socket sT,
                          osmServerSQLHandler osmTSQLH)
  {
    s = sT;
    osmSQLH = osmTSQLH;

  } // osmServerHandler


 
  public void run()
  {
    try{

      BufferedReader in = new BufferedReader(new InputStreamReader(
            s.getInputStream()));

      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
            s.getOutputStream()));

      String sLine;

      boolean bKeepTalking = true;

      while( (sLine = in.readLine()) != null && bKeepTalking)
      {

        System.out.println("client said " + sLine);
       
        if(sLine.equals("LOGIN"))
        {

          // puth authentication type things here
        }

        if(sLine.equals("GETPOINTS"))
        {

          Vector v = osmSQLH.getPoints();
          if( osmSQLH.SQLSuccessful() )
          {
            out.write("POINTS\n");

            Enumeration e = v.elements();

            while(e.hasMoreElements())
            {
              gpspoint g = (gpspoint)e.nextElement();

              out.write(g + "\n");
              
              out.flush();
            }
              
            

          }
          else
          {
            out.write("ERROR\n");
            bKeepTalking = false;
          }
        }

      }

      out.write("END\n");

      out.flush();

      System.out.println("wrote END");
    }
    catch(Exception e)
    {
      
      System.out.println("Something went screwy " + e);
    
    }
    
    
  } // run


} // osmServerHandler
