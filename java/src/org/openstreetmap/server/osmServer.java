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
import org.apache.xmlrpc.*;

public class osmServer extends Thread
{
  
  public static void main(String[] args)
  {
    
    System.out.println(new Date() + " openstreetmap server started");
    osmServer os = new osmServer();

  } // main

  

  public osmServer()
  {
    new Thread(this).start();
    
  //  osmSQLH = new osmServerSQLHandler("jdbc:mysql://127.0.0.1/openstreetmap", "openstreetmap","openstreetmap");

  } // osmServer

  

  public void run()
  {
    
    WebServer webserver = new WebServer(4000);

    webserver.addHandler("openstreetmap", new osmServerHandler());

    webserver.start();

    webserver.run();

    System.out.println("sleeping");
   
    while(true)
    {
      try{
        sleep(100000);
      }
      catch(Exception e)
      {
        System.out.print(e);
        e.printStackTrace();
        System.exit(-1);
      }
        
    }
  } // startServer

  
} // osmServer
