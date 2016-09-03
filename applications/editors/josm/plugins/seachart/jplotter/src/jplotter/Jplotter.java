package jplotter;

import java.awt.*;
import javax.swing.*;

public class Jplotter {

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
