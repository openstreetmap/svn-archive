import java.util.*;
import java.lang.*;
import java.sql.*;

public class SQLTempPointsReader
{

  public SQLTempPointsReader()
  {
    
  }

  
  public Vector getPoints()
  {

    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection("jdbc:mysql://128.40.59.181/openstreetmap","openstreetmap", "openstreetmap");

      Statement stmt = conn.createStatement();
      

      ResultSet rs = stmt.executeQuery("select Y(g),X(g),altitude,timestamp  from tempPoints order by timestamp desc");

      boolean bFirst = true;

      gpspoint gpFirst = new gpspoint(0,0,0,0);
      
      gpspoint gpLastPoint = new gpspoint(0,0,0,0);
      
      Vector v = new Vector();
      
      while(rs.next())
      {
        v.add( new gpspoint(rs.getFloat(2),
                     rs.getFloat(1),
                     rs.getFloat(3),
                     rs.getLong(4) ));

      }




      return v;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.exit(-1);

    }

    return null;
  }

} // SQLTempPointsReader
