//License: GPL. See README for details.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;

@SuppressWarnings("serial")
public class SdsMenu extends JMenu implements LayerChangeListener {

    private JMenuItem saveItem;
    private JMenuItem loadItem;
    private JMenuItem prefsItem;
    private JMenuItem aboutItem;
    private JMenu menu;

    public SdsMenu(final SeparateDataStorePlugin thePlugin) {
        MainMenu mm = Main.main.menu;
        menu = mm.addMenu(marktr("SDS"), KeyEvent.VK_S, mm.defaultMenuPos, null);
        saveItem = new JMenuItem(new SdsSaveAction());
        menu.add(saveItem);
        loadItem = new JMenuItem(new SdsLoadAction(thePlugin));
        menu.add(loadItem);
        menu.addSeparator();
        prefsItem = new JMenuItem(new SdsPreferencesAction());
        menu.add(prefsItem);
        menu.addSeparator();
        aboutItem = new JMenuItem(new SdsAboutAction());
        menu.add(aboutItem);
        
        MapView.addLayerChangeListener(this);
        setEnabledState();
    }

    void setEnabledState() {
    	boolean en = (Main.map != null) && (Main.map.mapView != null) && (Main.map.mapView.getActiveLayer() instanceof OsmDataLayer);
    	loadItem.setEnabled(en);
    	saveItem.setEnabled(en);
    }
  
	@Override
	public void activeLayerChange(Layer oldLayer, Layer newLayer) {	setEnabledState(); }

	@Override
	public void layerAdded(Layer newLayer) { setEnabledState(); }

	@Override
	public void layerRemoved(Layer oldLayer) { setEnabledState(); }

	private class SdsAboutAction extends JosmAction {

	    public SdsAboutAction() {
	        super(tr("About"), "sds", tr("Information about SDS."), null, true);
	    }

	    public void actionPerformed(ActionEvent e) {
	        JPanel about = new JPanel();

	        JTextArea l = new JTextArea();
	        l.setLineWrap(true);
	        l.setWrapStyleWord(true);
	        l.setEditable(false);
	        l.setText("Separate Data Store\n\nThis plugin provides access to a \"Separate Data Store\" server. " +
	        		"Whenever data is loaded from the OSM API, it queries the SDS for additional tags that have been stored for the objects just loaded, " +
	        		"and adds these tags. When you upload data to JOSM, SDS tags will again be separated and, instead of sending them to OSM, they will be uplaoded to SDS." +
	        		"\n\n" +
	        		"This depends on SDS tags starting with a special prefix, which can be configured in the SDS preferences." + 
	        		"\n\n" + 
	        		"Using the SDS server will usually require an account to be set up there, which is completely independent of your OSM account.");
	        
	        l.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	        l.setOpaque(false);
	        l.setPreferredSize(new Dimension(500,300));
	        JScrollPane sp = new JScrollPane(l);
	        sp.setBorder(null);
	        sp.setOpaque(false);
	        	        
	        about.add(sp);
	        
	        about.setPreferredSize(new Dimension(500,300));

	        JOptionPane.showMessageDialog(Main.parent, about, tr("About SDS..."),
	                JOptionPane.INFORMATION_MESSAGE, null);
	    }
	}
	
	private class SdsPreferencesAction extends JosmAction implements Runnable {

	    private SdsPreferencesAction() {
	        super(tr("Preferences..."), "preference", tr("Open a preferences dialog for SDS."),
	                null, true);
	        putValue("help", ht("/Action/Preferences"));
	    }

	    /**
	     * Launch the preferences dialog.
	     */
	    public void actionPerformed(ActionEvent e) {
	        run();
	    }

	    public void run() {
	    	PreferenceDialog pd = new PreferenceDialog(Main.parent);
	    	// unusual reflection mechanism to cater for older JOSM versions where 
	    	// the selectPreferencesTabByName method was not public
	    	try {
	    		Method sptbn = pd.getClass().getMethod("selectPreferencesTabByName", String.class);
	    		sptbn.invoke(pd, "sds");
	    	} catch (Exception ex) {
	    		// ignore
	    	}
	    	pd.setVisible(true);
	    }
	}


}
