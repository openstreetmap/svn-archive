package smed_about;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JLabel;
import java.awt.Rectangle;

public class SmedAbout implements SmedPluggable{

	public SmedPluginManager manager = null;
	
	private boolean visible = true;
	private int index = -1;
	private String msg = "";
	
    private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="43,24"
    private JLabel jLabel = null;
    private JLabel jLabel1 = null;
    private JLabel jLabel2 = null;
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
            jLabel2 = new JLabel();
            jLabel2.setBounds(new Rectangle(30, 90, 340, 30));
            jLabel2.setText("Description:");
            jLabel1 = new JLabel();
            jLabel1.setBounds(new Rectangle(30, 55, 340, 30));
            jLabel1.setText("Version:                           Date:");
            jLabel = new JLabel();
            jLabel.setBounds(new Rectangle(30, 20, 340, 30));
            jLabel.setText("Authors: Werner König and Malclom Herring");
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.setSize(new Dimension(400, 300));
            jPanel.add(jLabel, null);
            jPanel.add(jLabel1, null);
            jPanel.add(jLabel2, null);
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
