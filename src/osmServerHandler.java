import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.zip.*;

public class osmServerHandler implements Runnable
{

  Socket sock;
  osmServerSQLHandler osmSQLH;

  
  public osmServerHandler(Socket sT,
                          osmServerSQLHandler osmTSQLH)
  {
    sock = sT;
    osmSQLH = osmTSQLH;

  } // osmServerHandler


 
  public void run()
  {
    try{

      BufferedReader in = new BufferedReader(new InputStreamReader(
            sock.getInputStream()));

      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
            sock.getOutputStream()));

      String sLine;

      boolean bKeepTalking = true;

      while( (sLine = in.readLine()) != null && bKeepTalking)
      {

        System.out.println("client said " + sLine);
       
        if(sLine.equals("LOGIN"))
        {

          // puth authentication type things here
        }

        if(sLine.startsWith("GETPOINTS"))
        {
          StringTokenizer st = new StringTokenizer(sLine);

          String s = st.nextToken();

          float p1lat = Float.parseFloat( st.nextToken() );
          float p1lon = Float.parseFloat( st.nextToken() );
          float p2lat = Float.parseFloat( st.nextToken() );
          float p2lon = Float.parseFloat( st.nextToken() );

          Vector v = osmSQLH.getPoints(p1lat, p1lon, p2lat, p2lon);
          
          if( osmSQLH.SQLSuccessful() )
          {
            out.write("POINTS\n");

                  
            GZIPOutputStream innergs = new GZIPOutputStream( sock.getOutputStream() ); 
            BufferedWriter gs = new BufferedWriter(
                new OutputStreamWriter( 
                  
                    innergs 
                  ) 
                );

            Enumeration e = v.elements();

            
            while(e.hasMoreElements())
            {
              gpspoint g = (gpspoint)e.nextElement();

              gs.write(g + "\n");
              
            }
        
            gs.write("END\n");

            gs.flush();

            innergs.finish();


            System.out.println("write end");
        
            out.close();
            

          }
          else
          {
            System.out.println("error....");
            out.write("ERROR\n");
            bKeepTalking = false;
          }

        }

      }

    }
    catch(Exception e)
    {
      
      System.out.println("Something went screwy " + e);
    
//      System.exit(-1);
    }
    
    
  } // run


} // osmServerHandler
