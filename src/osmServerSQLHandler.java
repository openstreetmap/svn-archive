import java.util.*;
import java.lang.*;
import java.sql.*;

public class osmServerSQLHandler extends Thread
{
  String sSQLConnection;
  String sUser;
  String sPass;

  boolean bSQLSuccess = false;

  
  public osmServerSQLHandler(String sTSQLConnection,
                             String sTUser,
                             String sTPass)
           
  {
  
    sSQLConnection = sTSQLConnection;
    sUser = sTUser;
    sPass = sTPass;

    System.out.println("osmSQLHandler instantiated");
  } // osmServerSQLHandler


  
  public boolean SQLSuccessful()
  {
    
    return bSQLSuccess;

  } // SQLSuccessful


  
  public synchronized Vector getPoints(float p1lat,
                                       float p1lon,
                                       float p2lat,
                                       float p2lon
                                       )
  {

    System.out.println("getPoints");
    
    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection(sSQLConnection,
                                                    sUser,
                                                    sPass);

      Statement stmt = conn.createStatement();

      String sSQL = "select Y(g),X(g),altitude,timestamp from tempPoints"
                    + " where X(g) < " + p1lat
                    + " and X(g) > " + p2lat
                    + " and Y(g) > " + p1lon
                    + " and Y(g) < " + p2lon
                    + " limit 2000";

      System.out.println("querying with sql \n " + sSQL);
      ResultSet rs = stmt.executeQuery(sSQL);

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



      bSQLSuccess = true;

      return v;

    }
    catch(Exception e)
    {
      System.out.println(e);
      e.printStackTrace();

      System.exit(-1);

    }

    return null;
    
  } // getPoints

} // osmServerSQLHandler
