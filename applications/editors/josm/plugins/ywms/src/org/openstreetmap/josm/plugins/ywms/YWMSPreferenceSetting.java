package org.openstreetmap.josm.plugins.ywms;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.PleaseWaitRunnable;
import org.openstreetmap.josm.gui.preferences.PreferenceDialog;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.ywms.Util.Version;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.I18n;

/**
 * Preference settings for the YWMS plugin
 * 
 * @author frsantos
 */
public class YWMSPreferenceSetting implements PreferenceSetting
{
    /** WMS server name */
    public static final String WMS_NAME  = "Yahoo";
    /** WMS URL server parameters */
    public static final String WMS_URL_PARAMS = "/ymap?request=GetMap&format=image/jpeg";

    /** Firefox path text field */
    private JTextField firefox = new JTextField(10);
    /** Server port text field */
    private JTextField port    = new JTextField(10);
    /** Firefox profile text field */
    private JTextField profile = new JTextField(10);
    
    public void addGui( final PreferenceDialog gui ) 
    {
    	firefox.setToolTipText(tr("<html>Path to firefox executable.<br>" + 
    							  "The Firefox profile used in this plugin <b>must</b> be configured with the javascript 'dump' method,<br>" + 
    							  "that can be activated with the property 'browser.dom.window.dump.enabled=true' in the about:config page.</html>"));
    	port.setToolTipText(tr("<html>The port that the server will use to listen WMS requests<br>" +
    						   "The WMS plugin need to be configured to use this port"));
    	profile.setToolTipText(tr("<html>Name of the profile.<br>" + 
    							  "This profile is used to avoid nag firefox screens asking you to resume failed sessions.<br>" +
    							  "Just set the selected profile as not default in the profile selection window and configure to not ask<br>" + 
    							  "about failed sessions with 'browser.sessionstore.resume_from_crash=false' in the about:config page"
    							 ));

		Version ver = Util.getVersion();
		String description = tr("A WMS server for Yahoo imagery based on Firefox.");
		if( ver != null )
			description += "<br><br>" + tr("Version: {0}<br>Last change at {1}", ver.revision, ver.time);
    	JPanel ywms = gui.createPreferenceTab("yahoo.gif", I18n.tr("Yahoo! WMS server"), description + I18n.tr("Settings for the Yahoo! imagery server."));
    	ywms.add(new JLabel(tr("YWMS options")), GBC.eol().insets(0,5,0,0));

    	ywms.add(new JLabel(tr("Firefox executable")), GBC.std().insets(10,5,5,0));
        ywms.add(firefox, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
        
        ywms.add(new JLabel(tr("Firefox profile")), GBC.std().insets(10,5,5,0));
        ywms.add(profile, GBC.std().insets(0,5,0,0).fill(GBC.HORIZONTAL));
		JButton create = new JButton(tr("Create"));
		ywms.add(create, GBC.eol().insets(5,0,0,0).fill(GBC.EAST));
		create.addActionListener(new ProfileCreatorActionListener());
        
        ywms.add(new JLabel(tr("Server port")), GBC.std().insets(10,5,5,0));
        ywms.add(port, GBC.eol().insets(0,5,0,0).fill(GBC.HORIZONTAL));
		ywms.add(Box.createVerticalGlue(), GBC.eol().fill(GBC.VERTICAL));
        WMSConfigurationActionListener configurationActionListener = new WMSConfigurationActionListener();
        port.addActionListener(configurationActionListener);
        port.addFocusListener(configurationActionListener);
		
        firefox.setText(Main.pref.get("ywms.firefox", "firefox"));
        profile.setText(Main.pref.get("ywms.profile"));
        port.setText(Main.pref.get("ywms.port", "8000"));
    }
    
    public void ok() 
    {
        Main.pref.put("ywms.firefox", firefox.getText());
        Main.pref.put("ywms.profile", profile.getText());

        String oldPort = Main.pref.get("ywms.port");
        Main.pref.put("ywms.port", port.getText());
        if( !oldPort.equals(port.getText()) )
        {
            YWMSPlugin plugin = (YWMSPlugin)Util.getPlugin(YWMSPlugin.class);
            plugin.restartServer();
        }
    }

    /**
     * ActionListener for the configuration of WMS plugin  
     * @author frsantos
     */
    private final class WMSConfigurationActionListener implements ActionListener, FocusListener
    {
    	/** If the action is already handled */
        boolean alreadyHandled = false;
        public void actionPerformed(ActionEvent e) 
        {
            if( !alreadyHandled )
                configureWMSPluginPreferences();
            alreadyHandled = true;
        }

        public void focusGained(FocusEvent e)
        {
            alreadyHandled = false;
        }

        public void focusLost(FocusEvent e)
        {
            if( !alreadyHandled )
                configureWMSPluginPreferences();
            alreadyHandled = true;
        }
    }
    
    /**
     * ActionListener for the creation of a Mozilla profile  
     * @author frsantos
     */
    private final class ProfileCreatorActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) 
        {
            String profileName = profile.getText();
            if( profileName == null || profileName.length() == 0)
            {
                JOptionPane.showMessageDialog(Main.parent, tr("Please name the profile you want to create."));
                return;
            }
            
            try
            {
                PleaseWaitRunnable createProfileTask = new PleaseWaitRunnable(tr("Creating profile"))
                {
                    Process process = null;
                    @Override 
                    protected void realRun() throws IOException
                    {
                        process = GeckoSupport.createProfile(firefox.getText(), profile.getText());
                        try {
                            process.waitFor();
                        } 
                        catch (InterruptedException e) 
                        {
                            IOException ioe = new IOException();
                            ioe.initCause(e);
                            throw ioe;
                        }
                        
                        String configFile = new File(Util.getPluginDir(), "config.html").toURL().toString(); 
                        GeckoSupport.browse(firefox.getText(), profile.getText(), configFile, false);
                        configureWMSPluginPreferences();
                    }
                    
                    @Override 
                    protected void finish() {}
                    
                    @Override 
                    protected void cancel() 
                    {
                        if( process != null )
                            process.destroy();
                    }
                };                  
                Main.worker.execute(createProfileTask);
            }
            catch(Exception e2)
            {
                
            }
        }
    }
    
    /**
     * Configures WMSPlugin preferences with a server "Yahoo" pointing to YWMS  
     */
    private void configureWMSPluginPreferences()
    {
        try 
        {
            PreferenceSetting wmsSetting = null;
            for( PreferenceSetting setting : PreferenceDialog.settings)
            {
                if( setting.getClass().getName() == "wmsplugin.WMSPreferenceEditor" )
                {
                    wmsSetting = setting;
                    break;
                }
            }
            
            if( wmsSetting == null )
                return;
            
            int portNumber = Integer.parseInt(port.getText());
            String strUrl = (String)wmsSetting.getClass().getMethod("getServerUrl", String.class).invoke(wmsSetting, WMS_NAME);
            if( strUrl == null )
                strUrl = new URL("http", "localhost", portNumber, WMS_URL_PARAMS).toString();
            else
            {
                URL oldUrl = new URL(strUrl);
                strUrl = new URL("http", oldUrl.getHost(), portNumber, WMS_URL_PARAMS).toString();
            }
            wmsSetting.getClass().getMethod("setServerUrl", String.class, String.class).invoke(wmsSetting, WMS_NAME, strUrl);
        } catch (NoSuchMethodException e) {
        } catch (NumberFormatException nfe) {
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}
