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

      FileReader fr = new FileReader("/home/steve/wikimap/gpsdata"); 
      BufferedReader br = new BufferedReader(fr);

      String line;

      while( (line = br.readLine()) != null )
      {
        StringTokenizer t = new StringTokenizer(line);

        System.out.println("insert into tempPoints values ("
            + " GeomFromText('Point("  + t.nextToken() + " " + t.nextToken() + ")'),"
            + " " + t.nextToken() + ", "
            + " " + t.nextToken() + ");");

      } 

    }
    catch(Exception e)
    {
      System.out.println("arrrrrgggghhhhhhh" + e);
    }

  } //go




} // convert
