import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

public class osmServerHandler implements Runnable
{

  Socket s;

  public osmServerHandler(Socket sInbound)
  {
    s = sInbound;

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

       
        if(sLine.equals("LOGIN"))
        {

          // puth authentication type things here
        }

        if(sLine.equals("GETPOINTS"))
        {

        }

      }

    }
    catch(Exception e)
    {
      
      System.out.println("Something went screwy " + e);
    
    }
    
    
  } // run


} // osmServerHandler
