import javax.swing.*;
import java.awt.*;

public class osmApplet extends JApplet {

    public osmApplet() {
        getRootPane().putClientProperty("defeatSystemEventQueueCheck",
                                        Boolean.TRUE);
    } // osmApplet

    
    public void init() {
        JLabel label = new JLabel("blah");

        //Add border.  Should use createLineBorder, but then the bottom
        //and left lines don't appear -- seems to be an off-by-one error.

        getContentPane().add(label, BorderLayout.SOUTH);
        

        osmDisplayPanel odp = new osmDisplayPanel();
        
        getContentPane().add(odp, BorderLayout.CENTER);

        
    } // init
}
