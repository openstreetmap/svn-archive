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


  
  public synchronized Vector getPoints()
  {

    System.out.println("getPoints");
    
    try{

      Class.forName("com.mysql.jdbc.Driver").newInstance(); 


      Connection conn = DriverManager.getConnection(sSQLConnection,
                                                    sUser,
                                                    sPass);

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
