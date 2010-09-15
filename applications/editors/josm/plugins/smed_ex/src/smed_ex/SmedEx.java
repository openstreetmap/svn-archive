package smed_ex;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import smed.plug.ifc.SmedPluggable;
import smed.plug.ifc.SmedPluginManager;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import javax.swing.JButton;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SmedEx implements SmedPluggable {

	private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="78,30"
	private JButton jButton = null;
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

		return "hello";
	}

	@Override
	public String getInfo() {

		return "say hello";
	}

	@Override
	public JComponent getComponent() {
		
		return getJPanel();
	}

	@Override
	public void setPluginManager(SmedPluginManager manager) {
		// TODO Auto-generated method stub

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
					JOptionPane.showMessageDialog( null, "it works" );
				}
				
			});
		}
		return jButton;
	}

}
