import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class osmServer
{
  
  ServerSocket s;
  osmServerSQLHandler osmSQLH;
  
  public static void main(String[] args)
  {
    
    System.out.println(new Date() + " openstreetmap server started");
    new osmServer().startServer();

  } // main

  

  public osmServer()
  {
    
    osmSQLH = new osmServerSQLHandler("jdbc:mysql://127.0.0.1/openstreetmap", "openstreetmap","openstreetmap");

  } // osmServer

  

  public void startServer()
  {


    getSocket();

    while(true)
    {
      
      try{
      
        Socket tempSocket = s.accept();
    
        System.out.println(new Date() + " got a connection from " + tempSocket.getInetAddress());


        osmServerHandler osmsh = new osmServerHandler(tempSocket, osmSQLH);
        
        new Thread(osmsh).start();
      
      }
      catch(Exception e)
      {
        System.out.println("eek " + e);
        System.exit(-1);
      

      }
    }


  } // startServer



  private void getSocket()
  {

    try{

      s = new ServerSocket(2001);
     

    }
    catch(Exception e)
    {

      System.err.println("something went screwy getting a socket " + e);
      System.exit(-1);
    }


  }


} // osmServer

