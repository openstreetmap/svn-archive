import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class osmServer
{
  ServerSocket s;

  public static void main(String[] args)
  {
    new osmServer().startServer();

  } // main

  
  public osmServer()
  {

  } // osmServer


  public void startServer()
  {

     while(true)
    {
      
      try{
      
        Socket tempSocket = s.accept();
    
        System.out.println(new Date() + " got a connection from " + tempSocket.getInetAddress());


        osmServerHandler osmsh = new osmServerHandler(tempSocket);
        
        new Thread(osmsh).start();
      
      }
      catch(Exception e)
      {
      
        /* this should never happen... */

      }
    }


  } // startServer



  private void getSocket()
  {

    try{

      s = new ServerSocket(3141);

    }
    catch(Exception e)
    {

      System.err.println("something went screwy getting a socket " + e);
      System.exit(-1);
    }


  }


} // osmServer

