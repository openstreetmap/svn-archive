

import java.lang.*;
import java.util.*;
import java.io.*;

public class convert{


  public static void main(String args[])
  {

    convert blah = new convert();

    blah.go();

  } // main


  public convert()
  {

  } // convert


  public void go()
  {
    try{

      FileReader fr = new FileReader("/home/steve/mine2"); 
      BufferedReader br = new BufferedReader(fr);

      String line;

      while( (line = br.readLine()) != null )
      {
        StringTokenizer t = new StringTokenizer(line);

        String alt = t.nextToken();
        String lat = t.nextToken();
        String lon = t.nextToken();
        String time = t.nextToken();
        
        System.out.println("insert into tempPoints values ("
            + " GeomFromText('Point("  + lat + " " + lon + ")'),"
            + " " + alt + ", "
            + " " + time + ");");

      } 

    }
    catch(Exception e)
    {
      System.out.println("arrrrrgggghhhhhhh" + e);
    }

  } //go




} // convert
