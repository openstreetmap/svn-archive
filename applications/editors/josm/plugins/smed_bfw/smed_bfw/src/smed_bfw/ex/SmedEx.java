package smed_bfw.ex;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import smed_bfw.api.IManager;

@Component
public class SmedEx extends JFrame {
	
	IManager manager = null;
	JTabbedPane myPane = null;
	private JPanel jPanel = null;
	private JButton jButton = null;

	@Activate
	public void start() {
		System.out.println("start");
		init();
	}

	private void init() {
		 if(manager == null) System.out.println("something is wrong");
		 else {
			 myPane = manager.getTabbedPane();
			 myPane.addTab("hello",null,getJPanel(),"hello");
			 add(myPane, BorderLayout.CENTER);
			 setSize(new Dimension(420, 470));
			 setVisible(true);
		 }
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
				public void actionPerformed(ActionEvent arg0) {
					JOptionPane.showMessageDialog( null, "it works" );
				}
            });
        }
        return jButton;
    }

	@Deactivate
	public void stop() {
		System.out.println("stop");
	}

	@Reference
	public void setManager(IManager manager) {
		this.manager = manager;
	}
	
	public static void main(String[] args) {
        new SmedEx().start();
    }
}
