// License: GPL. Copyright 2007 by Immanuel Scholz and others
package org.openstreetmap.josm.io;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Font;
import java.awt.GridBagLayout;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.tools.Base64;
import org.openstreetmap.josm.tools.GBC;

/**
 * Base class that handles common things like authentication for the reader and writer
 * to the osm server.
 *
 * @author imi
 */
public class OsmConnection {

	public static class OsmParseException extends Exception {
		public OsmParseException() {super();}
		public OsmParseException(String message, Throwable cause) {super(message, cause);}
		public OsmParseException(String message) {super(message);}
		public OsmParseException(Throwable cause) {super(cause);}
	}

	protected boolean cancel = false;
	protected HttpURLConnection activeConnection;

	private static OsmAuth authentication = new OsmAuth();
	/**
	 * Initialize the http defaults and the authenticator.
	 */
	static {
		//TODO: refactor this crap (maybe just insert the damn auth http-header by yourself)
		try {
	        HttpURLConnection.setFollowRedirects(true);
	        Authenticator.setDefault(authentication);
        } catch (SecurityException e) {
        }
	}

	/**
	 * The authentication class handling the login requests.
	 */
	private static class OsmAuth extends Authenticator {
		/**
		 * Set to true, when the autenticator tried the password once.
		 */
		boolean passwordtried = false;
		/**
		 * Whether the user cancelled the password dialog
		 */
		boolean authCancelled = false;

		@Override protected PasswordAuthentication getPasswordAuthentication() {
			String username = Main.pref.get("osm-server.username");
			String password = Main.pref.get("osm-server.password");
			if (passwordtried || username.equals("") || password.equals("")) {
				JPanel p = new JPanel(new GridBagLayout());
				if (!username.equals("") && !password.equals(""))
					p.add(new JLabel(tr("Incorrect password or username.")), GBC.eop());
				p.add(new JLabel(tr("Username")), GBC.std().insets(0,0,10,0));
				JTextField usernameField = new JTextField(username, 20);
				p.add(usernameField, GBC.eol());
				p.add(new JLabel(tr("Password")), GBC.std().insets(0,0,10,0));
				JPasswordField passwordField = new JPasswordField(password, 20);
				p.add(passwordField, GBC.eol());
				JLabel warning = new JLabel(tr("Warning: The password is transferred unencrypted."));
				warning.setFont(warning.getFont().deriveFont(Font.ITALIC));
				p.add(warning, GBC.eop());

				JCheckBox savePassword = new JCheckBox(tr("Save user and password (unencrypted)"), !username.equals("") && !password.equals(""));
				p.add(savePassword, GBC.eop());

				int choice = JOptionPane.showConfirmDialog(Main.parent, p, tr("Enter Password"), JOptionPane.OK_CANCEL_OPTION);
				if (choice == JOptionPane.CANCEL_OPTION) {
					authCancelled = true;
					return null;
				}
				username = usernameField.getText();
				password = String.valueOf(passwordField.getPassword());
				if (savePassword.isSelected()) {
					Main.pref.put("osm-server.username", username);
					Main.pref.put("osm-server.password", password);
				}
				if (username.equals(""))
					return null;
			}
			passwordtried = true;
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}

	/**
	 * Must be called before each connection attemp to initialize the authentication.
	 */
	protected final void initAuthentication() {
		authentication.authCancelled = false;
		authentication.passwordtried = false;
	}

	/**
	 * @return Whether the connection was cancelled.
	 */
	protected final boolean isAuthCancelled() {
		return authentication.authCancelled;
	}

	public void cancel() {
		Main.pleaseWaitDlg.currentAction.setText(tr("Aborting..."));
		cancel = true;
		if (activeConnection != null) {
			activeConnection.setConnectTimeout(1);
			activeConnection.setReadTimeout(1);
			activeConnection.disconnect();
		}
	}

	protected void addAuth(HttpURLConnection con) {
        con.addRequestProperty("Authorization", "Basic "+Base64.encode(Main.pref.get("osm-server.username")+":"+Main.pref.get("osm-server.password")));
    }
}
