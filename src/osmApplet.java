import javax.swing.*;
import java.awt.*;

public class osmApplet extends JApplet {
    JLabel label = new JLabel("OpenStreetMap pre-pre-pre alpha. (control-)Mouse drag to do things");
    
    public osmApplet() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",
                                        Boolean.TRUE);
    } // osmApplet

    
    public void init() {

        //Add border.  Should use createLineBorder, but then the bottom
        //and left lines don't appear -- seems to be an off-by-one error.

        getContentPane().add(label, BorderLayout.SOUTH);
        

        osmDisplayPanel odp = new osmDisplayPanel(this);
        
        getContentPane().add(odp, BorderLayout.CENTER);

        
    } // init

    
    public void setStatusLabel(String s)
    {

      label.setText(s);

      label.repaint();
    } 
}
