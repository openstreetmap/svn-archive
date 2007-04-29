/*
 * OSMspeller 
 *     
 * Copyright (C) 2007 Jonas Svensson (jonass@lysator.liu.se)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *  
 */

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * An application ...
 */
public final class OSMspeller extends JPanel implements ActionListener {
    private static JFrame frame;
    
    private JMenuItem aboutMI;
    private JMenuItem importMI;
    private JMenuItem importMI2;
    private JMenuItem resumeMI;
    private JMenuItem exitMI;
    private JMenuItem debug1MI;

    private JMenuBar mainMenu;
    private JPanel midPanel;
    
    private WorkingStorage ws;
    private config cfg;

    /** 
     * This method.
     *
     */
    public OSMspeller(String[] args) {
	setLayout(new BorderLayout());
	setBorder(new EtchedBorder());

	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	mainMenu = new JMenuBar();
	
	final JMenu file = mainMenu.add(new JMenu("File"));
	
	importMI = file.add(new JMenuItem("Import PlanetOSM dump"));
	importMI.addActionListener(this);
	importMI2 = file.add(new JMenuItem("Import PlanetOSM b2zipped dump"));
	importMI2.addActionListener(this);
	resumeMI = file.add(new JMenuItem("Resume bz2"));
	resumeMI.addActionListener(this);

	file.add(new JSeparator());
	
	aboutMI = file.add(new JMenuItem("About"));
	aboutMI.addActionListener(this);
	exitMI = file.add(new JMenuItem("Exit"));
	exitMI.addActionListener(this);

	add(mainMenu, BorderLayout.NORTH);

	// Create middle panel
	midPanel = new JPanel();
	midPanel.setLayout(new BorderLayout());

	add(midPanel, BorderLayout.CENTER);

	// Create bottom panel

	final JPanel aPanel2 = new JPanel();
	aPanel2.setLayout(new BorderLayout());

	//	aPanel2.add(metalButton, BorderLayout.WEST);
	add(aPanel2, BorderLayout.SOUTH);

	read_config("OSMspeller.cfg");
    }

    /** 
     * This method.
     *
     */
    private void read_config(String filename) {
	cfg = new config();
	cfg.load(filename);
	//cfg.dump();
    }

    /** 
     * This method.
     *
     */
    private static void testQuit() {
	final int svar = JOptionPane.showConfirmDialog(frame,
						 "Bekräfta att du vill avsluta",
						 "Avsluta",
						 JOptionPane.OK_CANCEL_OPTION,
						 JOptionPane.QUESTION_MESSAGE);
	if(svar == 0) {
	    System.exit(0);
	}
    }

    /** 
     * This method.
     *
     * @param e ActionEvent
     */
    public void actionPerformed(final ActionEvent e) {
	if (e.getSource().equals(exitMI)) {
	    System.exit(0);
	} else if (e.getSource().equals(importMI)) {
	    importOldFile();
	} else if (e.getSource().equals(importMI2)) {
	    importOldFile_bz2();
	} else if (e.getSource().equals(resumeMI)) {
	    resume_bz2();
	} else if (e.getSource().equals(debug1MI)) {
	    System.out.println("debug1");
	} else if (e.getSource().equals(aboutMI)) {
	    final Runtime r = Runtime.getRuntime();

	    JOptionPane.showMessageDialog(frame,
					  "OSMspeller v0.01 by Jonas Svensson\n\n" +
					  "Free memory: "+r.freeMemory()/1024+"kB\n"+
					  "Total memory: "+r.totalMemory()/1024+"kB\n"+
					  "Max memory: "+r.maxMemory()/1024+"kB\n"+
					  "Processors: "+r.availableProcessors()+'\n'+
					  "java.version: "+System.getProperty("java.version")+'\n'+
					  "java.vendor: "+System.getProperty("java.vendor")+'\n'+
					  "os.name: "+System.getProperty("os.name")+'\n'+
					  "os.arch: "+System.getProperty("os.arch")+'\n'+
					  "os.version: "+System.getProperty("os.version")+'\n'+
					  "java.vm.specification.version: "+System.getProperty("java.vm.specification.version")+'\n'+
					  "java.vm.version: "+System.getProperty("java.vm.version")+'\n'+
					  "java.specification.version: "+System.getProperty("java.specification.version")+'\n'
					  );
	} else {
	    System.out.println("Unhandled menuitem: "+e);
	}
    }
    
    /** 
     * This method.
     *
     */
    private void importOldFile() {
	final JFileChooser chooser = new JFileChooser();
	final ExampleFileFilter filter = new ExampleFileFilter();
	filter.addExtension("osm");
	filter.setDescription("Planet OSM-filer");
	chooser.setFileFilter(filter);
	final int returnVal = chooser.showOpenDialog(this);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    remove(midPanel);
	    midPanel = new WaitView();
	    add(midPanel, BorderLayout.CENTER);
	    frame.pack();
	    repaint();
	    SwingUtilities.invokeLater(
				       new Runnable() {
					   public void run() {
					       ws = new WorkingStorage();
					       ws.load(chooser.getSelectedFile(), "OSMspeller.log");
					       remove(midPanel);
					       midPanel = new JPanel();
					       add(midPanel, BorderLayout.CENTER);
					       frame.pack();
					   }
				       }
				       );
	}
    }
    
    /** 
     * This method.
     *
     */
    private void importOldFile_bz2() {
	final JFileChooser chooser = new JFileChooser();
	final ExampleFileFilter filter = new ExampleFileFilter();
	filter.addExtension("bz2");
	filter.setDescription("Planet OSM-filer");
	chooser.setFileFilter(filter);
	final int returnVal = chooser.showOpenDialog(this);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    remove(midPanel);
	    midPanel = new WaitView();
	    add(midPanel, BorderLayout.CENTER);
	    frame.pack();
	    repaint();
	    SwingUtilities.invokeLater(
				       new Runnable() {
					   public void run() {
					       ws = new WorkingStorage();
					       ws.load2(chooser.getSelectedFile(), "OSMspeller.log");
					       remove(midPanel);
					       midPanel = new JPanel();
					       add(midPanel, BorderLayout.CENTER);
					       frame.pack();
					   }
				       }
				       );
	}
    }
    
    /** 
     * This method.
     *
     */
    private void resume_bz2() {
        //String filename="", filetype="", nodetype="", nodeid="";
	
	// read from file
	try {
	    BufferedReader in = new BufferedReader(new FileReader("resume.osp"));
	    final String filename = in.readLine();
	    final String filetype = in.readLine();
	    final String nodetype = in.readLine();
	    final String nodeid   = in.readLine();
	    in.close();
	    // display for approval
	    String question = "Confirm to resume\n"+
		filename + "\n" +
		filetype + "\n" +
		nodetype + "\n" +
		nodeid ;
	    final int svar = JOptionPane.showConfirmDialog(null,
							   question,
							   "Avsluta",
							   JOptionPane.OK_CANCEL_OPTION,
							   JOptionPane.QUESTION_MESSAGE);
	    
	    if(svar == 0) {
		remove(midPanel);
		midPanel = new WaitView();
		add(midPanel, BorderLayout.CENTER);
		frame.pack();
		repaint();
		SwingUtilities.invokeLater(
					   new Runnable() {
					       public void run() {
						   ws = new WorkingStorage();
						   ws.resume(filename, nodetype, nodeid, "OSMspeller.log");
						   remove(midPanel);
						   midPanel = new JPanel();
						   add(midPanel, BorderLayout.CENTER);
						   frame.pack();
					       }
					   }
					   );
	    }
	} catch (IOException e) {
	    System.err.println("Trouble reading resume file:"+e);
	}
    }
    
    /** 
     * This method.
     *
     * @param val int
     */
    private void debugDump(final int val) {
	ws.debugDump(val);
    }
    
    /** 
     * This method.
     *
     * @param s String[]
     */
    public static void main(final String[] args) {
	final OSMspeller panel = new OSMspeller(args);
	frame = new JFrame("OSMspeller by Jonas Svensson");
	frame.addWindowListener(
				new WindowAdapter() {
				    public void windowClosing(final WindowEvent e) {
					System.exit(0);
				    }
				}
				);
	frame.getContentPane().add("Center", panel);
	frame.pack();
	frame.setVisible(true);
    }
}
