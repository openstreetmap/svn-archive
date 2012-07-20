package smed_about;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.net.URL;

public class SmedAbout implements SmedPluggable {
/* Teste svn */
	public SmedPluginManager manager = null;
	
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

	private JLabel aboutGPLV3Link = null;

	private JLabel aboutGPLV2Text = null;

	private JLabel aboutGPLV2Link = null;

	private JLabel jLabel = null;
	
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
            URL url = getClass().getResource("/images/oseam_56x56.png");
            
        	jLabel = new JLabel();
            jLabel.setBounds(new Rectangle(45, 0, 58, 58));
            if(url != null) jLabel.setIcon(new ImageIcon(url));
            jLabel.setText("");
            
            aboutCopyright = new JLabel();
            aboutCopyright.setBounds(new Rectangle(125, 0, 245, 55));
            aboutCopyright.setText("<HTML><BODY>Copyright (c) 2009/10/11<BR><center>by" +
            				"<BR>Werner K\u00f6nig & Malcolm Herring</BODY></HTML>");

            aboutAuthors = new JLabel();
            aboutAuthors.setBounds(new Rectangle(30, 50
            		, 340, 30));
            aboutAuthors.setText("Authors: Werner K\u00f6nig and Malcolm Herring");

            aboutVersion = new JLabel();
            aboutVersion.setBounds(new Rectangle(30, 65, 340, 30));
            aboutVersion.setText("Version: 26218                     Date: 2011.7.8");

            aboutDescription = new JLabel();
            aboutDescription.setBounds(new Rectangle(30, 80, 340, 30));
            aboutDescription.setText("Description: ");

            aboutSmed = new JLabel();
            aboutSmed.setBounds(new Rectangle(100, 95, 265, 30));
            aboutSmed.setText("Plugin to map seamarks & lights");

            aboutAvailable = new JLabel();
            aboutAvailable.setBounds(new Rectangle(30, 115, 141, 27));
            aboutAvailable.setText("Available plugins:");

        	aboutPlugins = new JLabel();
            aboutPlugins.setBounds(new Rectangle(58, 135, 303, 60));
            aboutPlugins.setText("<HTML><BODY>SeaMark Editor" +
            		"<BR>Way & Area Editor" +
            		"<BR>Harbour Editor" +
            		"<BR>About - this tab</BODY></HTML>");
            
            aboutLicense = new JLabel();
            aboutLicense.setBounds(new Rectangle(5, 185, 390, 70));
            aboutLicense.setText("<HTML><BODY><U>SeaMap Editor license</U>" +
            		"<BR>SeaMapEditor, and all its integral parts, are released under" +
            		" the GNU General Public License v2 or later.</BODY></HTML>");

            aboutGPLV3Text = new JLabel();
            aboutGPLV3Text.setBounds(new Rectangle(5, 235, 246, 40));
            aboutGPLV3Text.setText("The license v3 is accessible here:");

            aboutGPLV3Link = new JLabel();
            aboutGPLV3Link.setBounds(new Rectangle(5, 255, 370, 28));
            aboutGPLV3Link.setText("  http://www.gnu.org/licenses/gpl.html");

            aboutGPLV2Text = new JLabel();
            aboutGPLV2Text.setBounds(new Rectangle(5, 275, 363, 28));
            aboutGPLV2Text.setText("The GPL v2 is accessible here:");

            aboutGPLV2Link = new JLabel();
            aboutGPLV2Link.setBounds(new Rectangle(5, 290, 391, 31));
            aboutGPLV2Link.setText("  http://www.gnu.org/licenses/old-licenses/gpl-2.0.html");
            
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
            jPanel.add(aboutGPLV2Text, null);
            jPanel.add(aboutGPLV2Link, null);
            jPanel.add(jLabel, null);
        }
        return jPanel;
    }
    
	@Override
	public String getFileName() { return "smed_about.jar"; }

	@Override
	public ImageIcon getIcon() {
		URL url = getClass().getResource("/images/Smed.png");
		
		if(url == null) return null;
		else return new ImageIcon(url);
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
