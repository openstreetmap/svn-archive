package org.openstreetmap.josm.plugins.validator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.validator.util.Util;
import org.openstreetmap.josm.plugins.validator.util.Util.Version;
import org.openstreetmap.josm.tools.GBC;

/**
 * Preference settings for the validator plugin
 * 
 * @author frsantos
 */
public class PreferenceEditor implements PreferenceSetting
{
	/** The list of all tests */
	private Collection<Test> allTests;

    public void addGui(PreferenceDialog gui)
    {
		JPanel testPanel = new JPanel(new GridBagLayout());
		testPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
        testPanel.add( new JLabel(), GBC.std() );
        testPanel.add( new JLabel("On upload"), GBC.eop() );
        
		allTests = OSMValidatorPlugin.getTests(false);
		for(final Test test: allTests) 
		{
			final JCheckBox testCheck = new JCheckBox(test.name, test.enabled);
			testCheck.setToolTipText(test.description);
			testPanel.add(testCheck, GBC.std().insets(20,0,0,0));

            testCheck.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    boolean selected = testCheck.isSelected();
                    test.enabled = selected;
                    test.setGuiEnabled(selected );
                }
            });
            
            test.addGui(testPanel);
            test.setGuiEnabled(test.enabled);
		}
		
		JScrollPane testPane = new JScrollPane(testPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		testPane.setBorder(null);

		Version ver = Util.getVersion();
		String description = tr("A OSM data validator that checks for common errors made by users and editor programs.");
		if( ver != null )
			description += "<br><br>" + tr("Version: {0}<br>Last change at {1}", ver.revision, ver.time);
    	JPanel tab = gui.createPreferenceTab("validator", tr("Data validator"), description);
		tab.add(testPane, GBC.eol().fill(GBC.BOTH));
		tab.add(GBC.glue(0,10), GBC.eol());
    }

	public void ok() 
	{
		String tests = "";
		
		for (Test test : allTests)
		{
			boolean enabled = test.enabled;
			String name = test.getClass().getSimpleName();
			tests += name + "=" + enabled + ",";
			
			if (enabled)
			{
				test.ok();
			}
		}
		
		if (tests.endsWith(","))
			tests = tests.substring(0, tests.length() - 1);
		
		OSMValidatorPlugin.getPlugin().initializeTests( allTests );
		
		Main.pref.put("tests", tests);
	}

}
