package org.openstreetmap.applet;

import java.awt.*;
import java.lang.*;
import java.awt.event.*;

public class osmAppletModeListener implements ItemListener
{
  osmDisplay od;

  public osmAppletModeListener(osmDisplay d)
  {
    
    od = d;
    
  } // osmAppletModeListener

  
  public void itemStateChanged(ItemEvent e)
  {
    if( e.getStateChange() == ItemEvent.DESELECTED)
    {
      return;

    }
    String s = (String)e.getItem();
 
    
    if( s.equals("add lines") )
    {
      od.setMode(od.MODE_DRAW_LINES);

    }

    if( s.equals("drop points") )
    {
      od.setMode(od.MODE_DROP_POINTS);

    }
    
  } // itemStateChanged
   


} // osmAppletModeListener
