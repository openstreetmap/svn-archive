/**
 * 
 */
package test;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * @author cdaller
 *
 */
public class DialogTest {
    
    public void openDialog(JFrame frame) {
        
        JTextField textField = new JTextField(10);

        //Create an array of the text and components to be displayed.
        String msgString1 = "What was Dr. SEUSS's real last name?";
        String msgString2 = "(The answer is .)";
        Object[] array = {msgString1, msgString2, textField};

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {"button1", "button2"};

        //Create the JOptionPane.
        final JOptionPane optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);

//        final JOptionPane optionPane = new JOptionPane("The only way to close this dialog is by\n"
//                        + "pressing one of the following buttons.\n" + "Do you understand?",
//            JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);


        final JDialog dialog = new JDialog(frame, "Click a button", true);
        DialogClosingThread closer = new DialogClosingThread(dialog);
        closer.observe(textField);
        dialog.setContentPane(optionPane);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (dialog.isVisible() && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                    // If you were going to check something
                    // before closing the window, you'd do
                    // it here.
                    dialog.setVisible(false);
                }
            }
        });
        closer.start();
        dialog.pack();
        dialog.setVisible(true);
        

        System.out.println("value: " + optionPane.getValue());

//        int value = ((Integer) optionPane.getValue()).intValue();
//        if (value == JOptionPane.YES_OPTION) {
//            System.out.println("yes");
//        } else if (value == JOptionPane.NO_OPTION) {
//            System.out.println("no");
//        }

    }

    public static void main(String[] args) {
        //1. Create the frame.
          JFrame frame = new JFrame("FrameDemo");

          //2. Optional: What happens when the frame closes?
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

          //3. Create components and put them in the frame.
          //...create emptyLabel...
          frame.getContentPane().add(new JLabel("test"), BorderLayout.CENTER);

          //4. Size the frame.
          frame.pack();
          frame.setSize(600,400);
          frame.setLocation(0,0);

          //5. Show it.
          frame.setVisible(true);
          new DialogTest().openDialog(frame);
      }
}
