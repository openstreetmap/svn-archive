
import java.awt.*;
import java.lang.*;
import javax.swing.*;
import javax.swing.event.*;

public class osmAppletModeListener implements ChangeListener
{
  osmDisplay od;

  public osmAppletModeListener(osmDisplay d)
  {
    
    od = d;
    
  } // osmAppletModeListener

  
  public void stateChanged(ChangeEvent e)
  {
    JTabbedPane pane = (JTabbedPane)e.getSource();
    
    int sel = pane.getSelectedIndex();

    
    System.out.println(sel);

    switch(sel)
    {
      case 0:
        od.setMode(od.MODE_POINTS);

      case 1:
        od.setMode(od.MODE_LINES);

      case 2:
        od.setMode(od.MODE_AREAS);


    }

  } // itemStateChanged



} // osmAppletModeListener
