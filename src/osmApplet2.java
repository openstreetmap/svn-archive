import java.util.*;
import java.lang.*;
import javax.swing.*;
import java.awt.*;



public class osmApplet2 extends JApplet {
    
    //public osmApplet2() {
    //    getRootPane().putClientProperty("defeatSystemEventQueueCheck",
    //                                    Boolean.TRUE);
    //} // osmApplet

    
    public void init() {


      new osmDisplay( getContentPane() );       

        
        
        
    } // init

    
    public static void main(String[] args)
    {

      new osmApplet2().go();

    } // main

    public osmApplet2()
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
