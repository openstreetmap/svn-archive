/*
Copyright (C) Stephen Coast (steve@fractalus.com)

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

*/



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
