package smed_ex;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.JButton;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SmedEx implements SmedPluggable {

	private boolean visible = true;
	public SmedPluginManager manager = null;
	private int index = -1;
	private String msg = "press button 'Hello World!' and see, how it works";
	
    private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="78,30"
    private JButton jButton = null;

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {

        return "Hello";
    }

    @Override
    public String getInfo() {

        return "say hello";
    }

    @Override
    public JComponent getComponent() {
    	manager.showVisualMessage(msg);
        return getJPanel();
    }

    @Override
    public void setPluginManager(SmedPluginManager manager) {
    	this.manager = manager;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(null);
            jPanel.setPreferredSize(new Dimension(200, 130));
            jPanel.add(getJButton(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setBounds(new Rectangle(15, 40, 160, 40));
            jButton.setText("Hello World!");

            jButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if(manager.getString() == null) JOptionPane.showMessageDialog( null, "it works" );
                    else JOptionPane.showMessageDialog( null, manager.getString() );
                }

            });
        }
        return jButton;
    }

    @Override
    public boolean start() {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public String getFileName() { return "smed_ex.jar"; }

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
