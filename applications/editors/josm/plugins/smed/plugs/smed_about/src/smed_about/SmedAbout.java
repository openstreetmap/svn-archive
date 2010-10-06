package smed_about;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.JEditorPane;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;
import javax.swing.JPanel;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

public class SmedAbout implements SmedPluggable {

	public SmedPluginManager manager = null;
	
	private boolean visible = true;
	private int index = -1;
	private String msg = "";
	
    private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="43,24"
    private JLabel aboutAuthors = null;
    private JLabel aboutVersion = null;
    private JLabel aboutDescription = null;
	private JLabel aboutCopyright = null;
	private JLabel aboutSmed = null;
	private JLabel aboutAvailable = null;
	private JLabel aboutPlugins = null;

	private JLabel aboutLicense = null;

	private JLabel aboutGPLV3Text = null;

	private JEditorPane jEditorPane = null;

	private JLabel aboutGPLV3Link = null;
	
    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        return "About";
    }

    @Override
    public void setPluginManager(SmedPluginManager manager) {
    	this.manager = manager;
    }

    @Override
    public String getInfo() {
        return "something about the programm";
    }

    @Override
    public JComponent getComponent() {
    	manager.showVisualMessage(msg);
        return getJPanel();
    }


    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            aboutCopyright = new JLabel();
            aboutCopyright.setBounds(new Rectangle(125, 0, 245, 54));
            aboutCopyright.setText("<HTML><BODY>Copyright (c) 2009 / 2010<BR><center>by" +
            				"<BR>Werner König & Malcolm Herring</BODY></HTML>");

            aboutAuthors = new JLabel();
            aboutAuthors.setBounds(new Rectangle(30, 40, 340, 30));
            aboutAuthors.setText("Authors: Werner König and Malclom Herring");

            aboutVersion = new JLabel();
            aboutVersion.setBounds(new Rectangle(30, 50, 340, 30));
            aboutVersion.setText("Version: 23479                     Date: 05.10.2010");

            aboutDescription = new JLabel();
            aboutDescription.setBounds(new Rectangle(30, 60, 340, 30));
            aboutDescription.setText("Description: ");

            aboutSmed = new JLabel();
            aboutSmed.setBounds(new Rectangle(100, 80, 265, 30));
            aboutSmed.setText("SeaMap Editor to map marks & lights");

            aboutAvailable = new JLabel();
            aboutAvailable.setBounds(new Rectangle(30, 140, 141, 27));
            aboutAvailable.setText("available plugins:");

        	aboutPlugins = new JLabel();
            aboutPlugins.setBounds(new Rectangle(58, 160, 303, 60));
            aboutPlugins.setText("<HTML><BODY>SeaMark Editor" +
            		"<BR>SeaLight Editor" +
            		"<BR>Hello - an example plugin" +
            		"<BR>About - this tab</BODY></HTML>");
            
            aboutLicense = new JLabel();
            aboutLicense.setBounds(new Rectangle(5, 225, 390, 70));
            aboutLicense.setText("<HTML><BODY><U>SeaMap Editor license</U>" +
            		"<BR>SeaMapEditor, and all its integral parts, are released under" +
            		" the GNU General Public License v2 or later.</BODY></HTML>");

            aboutGPLV3Text = new JLabel();
            aboutGPLV3Text.setBounds(new Rectangle(3, 280, 246, 40));
            aboutGPLV3Text.setText("The license v3 is accessible here:");

            aboutGPLV3Link = new JLabel();
            aboutGPLV3Link.setBounds(new Rectangle(5, 300, 370, 28));
            aboutGPLV3Link.setText("http://www.gnu.org/licenses/gpl.html");

            
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.setSize(new Dimension(400, 416));
            jPanel.add(aboutAuthors, null);
            jPanel.add(aboutVersion, null);
            jPanel.add(aboutDescription, null);
            jPanel.add(aboutCopyright, null);
            jPanel.add(aboutSmed, null);
            jPanel.add(aboutAvailable, null);
            jPanel.add(aboutPlugins, null);
            jPanel.add(aboutLicense, null);
            jPanel.add(aboutGPLV3Text, null);
            jPanel.add(aboutGPLV3Link, null);
        }
        return jPanel;
    }
    
	@Override
	public String getFileName() { return "smed_about.jar"; }

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public boolean hasFocus() {
		manager.showVisualMessage(msg);
		return true;
	}

	@Override
	public boolean lostFocus() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getIndex() { return index; }

	@Override
	public void setIndex(int index) { this.index = index; }
}
