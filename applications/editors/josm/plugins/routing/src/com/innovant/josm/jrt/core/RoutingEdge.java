package com.innovant.josm.jrt.core;

import org.openstreetmap.josm.data.coor.LatLon;

public interface RoutingEdge {

      public LatLon fromLatLon();

      public LatLon toLatLon();
      
      public Object fromV();

      public Object toV();

      public double getLength();
      
      public void setLength(double length);
      
      public double getSpeed();

      public void setSpeed(double speed);
      
      public boolean isOneway();
      
      public void setOneway(boolean isOneway);

}
