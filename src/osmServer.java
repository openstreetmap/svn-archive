/*
Copyright (C) 2004 Stephen Coast (steve@fractalus.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

:*/



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

