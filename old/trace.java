import java.net.Socket;
import java.net.InetAddress;
import java.io.*;
import java.lang.*;
import java.util.*;

public class trace {

  public static void main(String[] args){

    ssocket cheese = new ssocket();

    cheese.go();

  } // main

}

class ssocket extends Thread
{
  ssocket()
  {

  } // socket


  public void go()
  {
    String hostName = "localhost";

    try {
      BufferedWriter fileout = new BufferedWriter(new FileWriter("gpsdata", true));
      Socket serverSocket = new Socket(hostName, 2947);


      BufferedWriter out = new BufferedWriter( new OutputStreamWriter( serverSocket.getOutputStream()));
      BufferedReader is = new BufferedReader(
          new InputStreamReader(serverSocket.getInputStream()));

      int s = 0;
      float x=0;
      float y=0;
      float a=0;
      while(true)
      {
        // Read from the socket

        out.write("aps\n");
        out.flush();
        String line;

        line = is.readLine();

        StringTokenizer myTokenizer = new StringTokenizer(line, ",");

        while(myTokenizer.hasMoreTokens() )
        {
          String sToken = myTokenizer.nextToken();

          if(sToken.startsWith("P="))
          {
            //position

            StringTokenizer posTokenizer = new StringTokenizer(sToken.substring(2));

            x = Float.parseFloat( posTokenizer.nextToken() );
            y = Float.parseFloat( posTokenizer.nextToken() );

    //        System.out.println("x is " + x );
      //      System.out.println("y is " + y );


          }

          if(sToken.startsWith("A="))
          {
            //position

            a = Float.parseFloat( sToken.substring(2) );

  //          System.out.println("a is " + a );


          }


          if(sToken.startsWith("S="))
          {
            //position

            s = Integer.parseInt( sToken.substring(2) );

//            System.out.println("s is " + s );


          }


         
        }

        if( s == 1)
        {
          //satellites in view
          //
          
          fileout.write(x + " " + y + " " + a + " " + System.currentTimeMillis() + "\n");
          fileout.flush();
          System.out.print(".");
        }
        else
        {
          System.out.print("_");
        }
         


        this.sleep(1000);
      }
    } catch (Exception e) {

      e.printStackTrace();

    }


  } // go

}

