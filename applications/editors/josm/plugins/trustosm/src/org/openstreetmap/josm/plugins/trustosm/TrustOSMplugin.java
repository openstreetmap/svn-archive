package org.openstreetmap.josm.plugins.trustosm;

import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.ExtensionFileFilter;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.preferences.PreferenceSetting;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.trustosm.actions.ExportSigsAction;
import org.openstreetmap.josm.plugins.trustosm.data.TrustOsmPrimitive;
import org.openstreetmap.josm.plugins.trustosm.gui.dialogs.TrustDialog;
import org.openstreetmap.josm.plugins.trustosm.gui.dialogs.TrustPreferenceEditor;
import org.openstreetmap.josm.plugins.trustosm.io.SigExporter;
import org.openstreetmap.josm.plugins.trustosm.io.SigImporter;
import org.openstreetmap.josm.plugins.trustosm.util.TrustGPG;

public class TrustOSMplugin extends Plugin {

	static JMenu gpgJMenu;

	private TrustDialog trustDialog;

	/** Use a TrustGPGPreparer to sign or validate signatures */
	public static TrustGPG gpg;

	/** A global list with all OSM-Ids and corresponding TrustOSMItems */
	public static final Map<String, TrustOsmPrimitive> signedItems = new HashMap<String, TrustOsmPrimitive>();

	/**
	 * Will be invoked by JOSM to bootstrap the plugin
	 *
	 * @param info  information about the plugin and its local installation
	 */
	public TrustOSMplugin(PluginInformation info) {
		// init the plugin
		super(info);
		// check if the jarlibs are already extracted or not and extract them if not
		if (!Main.pref.getBoolean("trustosm.jarLibsExtracted")) {
			Main.pref.put("trustosm.jarLibsExtracted", extractFiles("trustosm","lib"));
			Main.pref.put("trustosm.jarLibsExtracted", extractFiles("trustosm","resources"));
		}

		refreshMenu();
		checkForUnrestrictedPolicyFiles();
		// register new SigImporter and SigExporter
		ExtensionFileFilter.importers.add(new SigImporter());
		ExtensionFileFilter.exporters.add(new SigExporter());

		gpg = new TrustGPG();
		setSettings();
		File gpgDir = new File(getGpgPath());
		if (!gpgDir.exists())
			gpgDir.mkdirs();

	}

	public static void checkForUnrestrictedPolicyFiles() {
		byte[] data = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };

		// create a cipher and attempt to encrypt the data block with our key
		try{
			Cipher c = Cipher.getInstance("AES");
			// create a 192 bit secret key from raw bytes

			SecretKey key192 = new SecretKeySpec(new byte[] { 0x00, 0x01, 0x02,
					0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c,
					0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16,
					0x17 }, "AES");

			// now try encrypting with the larger key

			c.init(Cipher.ENCRYPT_MODE, key192);
			c.doFinal(data);
		} catch (Exception e) {
			//e.printStackTrace();
			System.err.println("Warning: It seems that the Unrestricted Policy Files are not available in this JVM. So high level crypto is not allowed. Problems may occure.");
			//extractFiles("trustosm","jce");
			installUnrestrictedPolicyFiles();
		}
	}

	public static boolean installUnrestrictedPolicyFiles() {
		/*
		String[] cmd = new String[3];
		cmd[0] = "sudo";
		cmd[1] = "-S";
		cmd[2] = "/tmp/skript.sh";

		try
		{
			Process p = Runtime.getRuntime().exec(cmd);
			OutputStream os = p.getOutputStream();
			Writer writer = new OutputStreamWriter(os);

			JPasswordField passwordField = new JPasswordField(10);
			JOptionPane.showMessageDialog(null, passwordField, "Enter password", JOptionPane.OK_OPTION);
			String password = passwordField.getPassword().toString();

			writer.write(password + "\n");
			writer.close();
			InputStream in = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String satz = reader.readLine();
			while (satz != null)
			{
				System.out.println(satz);
				satz = reader.readLine();
			}
			int rc = p.waitFor();
		}
		catch (Exception e)
		{
			System.out.println(e.toString());
		}

		/*

		Process	p;
		int exitCode;
		String stdout,stderr;
		String sysSecPath = System.getProperty("java.home")+"/lib/security";
		File localPolicy = new File(sysSecPath+"/local_policy.jar");
		if (!localPolicy.exists()) {
			System.err.println("No local_policy.jar file found in "+sysSecPath+"\n Is this the right java directory?");
			return false;
		}


		String cmd = "sh -c sudo -S mv "+sysSecPath+"/local_policy.jar "+sysSecPath+"/local_policy.jar.restricted";
		/*		String cmd2 = "sudo -S mv "+sysSecPath+"/US_export_policy.jar "+sysSecPath+"/US_export_policy.jar.restricted";
		String cmd3 = "sudo -S cp "+Main.pref.getPluginsDirectory().getPath()+"/trustosm/jce/US_export_policy.jar "+sysSecPath;
		String cmd4 = "sudo -S cp "+Main.pref.getPluginsDirectory().getPath()+"/trustosm/jce/local_policy.jar "+sysSecPath;


		//System.out.println (cmd);

		try
		{
			p = Runtime.getRuntime().exec(cmd);
		}
		catch(IOException io)
		{
			System.err.println ("io Error" + io.getMessage ());
			return false;
		}

		JPasswordField passwordField = new JPasswordField(10);
		JOptionPane.showMessageDialog(null, passwordField, "Enter password", JOptionPane.OK_OPTION);
		String password = passwordField.getPassword().toString();

		if (password != null)
		{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			try
			{
				out.write(password);
				out.close();
			}
			catch(IOException io)
			{
				System.err.println("Exception at write! " + io.getMessage ());
				return false;
			}
		}

		try
		{
			exitCode = p.exitValue ();
			if (exitCode==0) {
				System.err.println("Everything seems to be ok.");
			} else {
				System.err.println("Exit code was not 0.");
				StringBuffer buf = new StringBuffer();
				InputStream errIn = p.getErrorStream();
				int read;
				while ((read = errIn.read()) != -1) {
					buf.append(read);
				}
				System.err.println(buf.toString());
			}
		}
		catch (IllegalThreadStateException itse)
		{
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		 */
		return false;
	}

	public static boolean extractFiles(String pluginname, String extractDir) {
		try {
			if (extractDir == null) extractDir = "lib";
			JarFile jar = new JarFile(Main.pref.getPluginsDirectory().getPath()+"/"+pluginname+".jar");
			Enumeration<JarEntry> entries = jar.entries();
			InputStream is;
			FileOutputStream fos;
			File file;
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith(extractDir+"/") && !entry.isDirectory()) {
					System.out.println(Main.pref.getPluginsDirectory().getPath()+"/"+pluginname+"/"+name);
					file = new File(Main.pref.getPluginsDirectory().getPath()+"/"+pluginname+"/"+name);
					file.getParentFile().mkdirs();
					is = jar.getInputStream(entry);
					fos = new FileOutputStream(file);
					while (is.available() > 0) {  // write contents of 'is' to 'fos'
						fos.write(is.read());
					}
					fos.close();
					is.close();
				}
			}
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	public static void refreshMenu() {
		MainMenu menu = Main.main.menu;

		if (gpgJMenu == null) {
			gpgJMenu = menu.addMenu(marktr("GPG"), KeyEvent.VK_G, menu.defaultMenuPos, ht("/Plugin/TrustOSM"));
			gpgJMenu.add(new JMenuItem(new ExportSigsAction()));
		}

	}

	public static void setSettings() {
		Map<String,String> prefs = Main.pref.getAllPrefix("trustosm.");

		// if setting isn't present, we set a default
		// This makes sense for example when we start the plugin for the first time
		if (!prefs.containsKey("trustosm.gpg")) Main.pref.put("trustosm.gpg", "gpg");
		if (!prefs.containsKey("trustosm.gpg.separateHomedir")) Main.pref.put("trustosm.gpg.separateHomedir", true);
	}


	@Override
	public PreferenceSetting getPreferenceSetting() {
		return new TrustPreferenceEditor();
	}

	@Override
	public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
		if (oldFrame==null && newFrame!=null) {
			trustDialog = new TrustDialog();
			newFrame.addToggleDialog(trustDialog);
		}
	}

	public static String getGpgPath() {
		return Main.pref.getPluginsDirectory().getPath() + "/trustosm/gnupg/";
	}

}
