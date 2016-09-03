// License: GPL. For details, see LICENSE file.
package jplotter;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author Malcolm Herring
 */
public final class Jplotter {
    private Jplotter() {
        // Hide default constructor for utilities classes
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chart Plotter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        Container c = frame.getContentPane();

        JPanel map = new JPanel(new BorderLayout());
        map.setSize(600, 600);
        map.setLocation(50, 35);
        map.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder()));
        map.setLayout(null);
        map.setVisible(true);
        c.add(map);

        frame.setSize(700, 700);
        frame.setVisible(true);
    }
}
