import java.util.*;
import java.lang.*;
import javax.swing.*;
import java.awt.*;



public class osmApplet extends JApplet {
    
    public void init() {


      new osmDisplay( getContentPane() );       

        
        
        
    } // init

    
    public static void main(String[] args)
    {

      new osmApplet().go();

    } // main

    public osmApplet()
    {

    } // osmApplet2

    public void go()
    {
      JFrame jf = new JFrame("blah");
      
      jf.setSize(600,600);
      jf.show();
      
      new osmDisplay(jf.getContentPane());
 
      jf.pack();
      
    } 
}
