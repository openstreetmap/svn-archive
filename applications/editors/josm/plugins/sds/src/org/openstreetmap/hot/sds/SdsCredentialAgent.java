// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.net.PasswordAuthentication;
import java.net.Authenticator.RequestorType;
import java.util.Map;
import java.util.HashMap;

import javax.swing.text.html.HTMLEditorKit;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.oauth.OAuthToken;
import org.openstreetmap.josm.gui.io.CredentialDialog;
import org.openstreetmap.josm.gui.preferences.server.ProxyPreferencesPanel;
import org.openstreetmap.josm.gui.widgets.HtmlPanel;
import org.openstreetmap.josm.io.auth.AbstractCredentialsAgent;
import org.openstreetmap.josm.io.auth.CredentialsAgentException;
import org.openstreetmap.josm.io.auth.CredentialsAgentResponse;

/**
 * Factored after JOSM's JosmPreferencesCredentialAgent. 
 */
public class SdsCredentialAgent extends AbstractCredentialsAgent {

    Map<RequestorType, PasswordAuthentication> sdsMemoryCredentialsCache = new HashMap<RequestorType, PasswordAuthentication>();

    /**
     * @see CredentialsAgent#lookup(RequestorType)
     */
    @Override
    public PasswordAuthentication lookup(RequestorType requestorType, String host) throws CredentialsAgentException{
        if (requestorType == null)
            return null;
        String user;
        String password;
        switch(requestorType) {
        case SERVER:
            user = Main.pref.get("sds-server.username", null);
            password = Main.pref.get("sds-server.password", null);
            if (user == null)
                return null;
            return new PasswordAuthentication(user, password == null ? new char[0] : password.toCharArray());
        case PROXY:
            user = Main.pref.get(ProxyPreferencesPanel.PROXY_USER, null);
            password = Main.pref.get(ProxyPreferencesPanel.PROXY_PASS, null);
            if (user == null)
                return null;
            return new PasswordAuthentication(user, password == null ? new char[0] : password.toCharArray());
        }
        return null;
    }

    /**
     * @see CredentialsAgent#store(RequestorType, PasswordAuthentication)
     */
    @Override
    public void store(RequestorType requestorType, String host, PasswordAuthentication credentials) throws CredentialsAgentException {
        if (requestorType == null)
            return;
        switch(requestorType) {
        case SERVER:
            Main.pref.put("sds-server.username", credentials.getUserName());
            if (credentials.getPassword() == null) {
                Main.pref.put("sds-server.password", null);
            } else {
                Main.pref.put("sds-server.password", String.valueOf(credentials.getPassword()));
            }
            break;
        case PROXY:
            Main.pref.put(ProxyPreferencesPanel.PROXY_USER, credentials.getUserName());
            if (credentials.getPassword() == null) {
                Main.pref.put(ProxyPreferencesPanel.PROXY_PASS, null);
            } else {
                Main.pref.put(ProxyPreferencesPanel.PROXY_PASS, String.valueOf(credentials.getPassword()));
            }
            break;
        }
    }

    /**
     * Lookup the current OAuth Access Token to access the OSM server. Replies null, if no
     * Access Token is currently managed by this CredentialManager.
     *
     * @return the current OAuth Access Token to access the OSM server.
     * @throws CredentialsAgentException thrown if something goes wrong
     */
    @Override
    public OAuthToken lookupOAuthAccessToken() throws CredentialsAgentException {
        String accessTokenKey = Main.pref.get("oauth.access-token.key", null);
        String accessTokenSecret = Main.pref.get("oauth.access-token.secret", null);
        if (accessTokenKey == null && accessTokenSecret == null)
            return null;
        return new OAuthToken(accessTokenKey, accessTokenSecret);
    }

    @Override
    public Component getPreferencesDecorationPanel() {
        HtmlPanel pnlMessage = new HtmlPanel();
        HTMLEditorKit kit = (HTMLEditorKit)pnlMessage.getEditorPane().getEditorKit();
        kit.getStyleSheet().addRule(".warning-body {background-color:rgb(253,255,221);padding: 10pt; border-color:rgb(128,128,128);border-style: solid;border-width: 1px;}");
        pnlMessage.setText(
                tr(
                        "<html><body>"
                        + "<p class=\"warning-body\">"
                        + "<strong>Warning:</strong> The password is stored in plain text in the JOSM preferences file. "
                        + "</p>"
                        + "</body></html>"
                )
        );
        return pnlMessage;
    }
    
    @Override
    public String getSaveUsernameAndPasswordCheckboxText() {
        return tr("Save user and password (unencrypted)");
    }

	@Override
	public void storeOAuthAccessToken(OAuthToken accessToken)
			throws CredentialsAgentException {
		// no-op
		
	}
	
    @Override
    public CredentialsAgentResponse getCredentials(RequestorType requestorType, String host, boolean noSuccessWithLastResponse) throws CredentialsAgentException{
        if (requestorType == null)
            return null;
        PasswordAuthentication credentials =  lookup(requestorType, host);
        String username = (credentials == null || credentials.getUserName() == null) ? "" : credentials.getUserName();
        String password = (credentials == null || credentials.getPassword() == null) ? "" : String.valueOf(credentials.getPassword());

        CredentialsAgentResponse response = new CredentialsAgentResponse();

        /*
         * Last request was successful and there was no credentials stored
         * in file (or only the username is stored).
         * -> Try to recall credentials that have been entered
         * manually in this session.
         */
        if (!noSuccessWithLastResponse && sdsMemoryCredentialsCache.containsKey(requestorType) &&
                (credentials == null || credentials.getPassword() == null || credentials.getPassword().length == 0)) {
            PasswordAuthentication pa = sdsMemoryCredentialsCache.get(requestorType);
            response.setUsername(pa.getUserName());
            response.setPassword(pa.getPassword());
            response.setCanceled(false);
        /*
         * Prompt the user for credentials. This happens the first time each
         * josm start if the user does not save the credentials to preference
         * file (username=="") and each time after authentication failed
         * (noSuccessWithLastResponse == true).
         */
        } else if (noSuccessWithLastResponse || username.equals("") || password.equals("")) {
            CredentialDialog dialog = null;
            switch(requestorType) {
            case SERVER: dialog = SdsCredentialDialog.getSdsApiCredentialDialog(username, password, host, getSaveUsernameAndPasswordCheckboxText()); break;
            case PROXY: dialog = CredentialDialog.getHttpProxyCredentialDialog(username, password, host, getSaveUsernameAndPasswordCheckboxText()); break;
            }
            dialog.setVisible(true);
            response.setCanceled(dialog.isCanceled());
            if (dialog.isCanceled())
                return response;
            response.setUsername(dialog.getUsername());
            response.setPassword(dialog.getPassword());
            if (dialog.isSaveCredentials()) {
                store(requestorType, host, new PasswordAuthentication(
                        response.getUsername(),
                        response.getPassword()
                ));
            /*
             * User decides not to save credentials to file. Keep it
             * in memory so we don't have to ask over and over again.
             */
            } else {
                PasswordAuthentication pa = new PasswordAuthentication(dialog.getUsername(), dialog.getPassword());
                sdsMemoryCredentialsCache.put(requestorType, pa);
            }
        /*
         * We got it from file.
         */
        } else {
            response.setUsername(username);
            response.setPassword(password.toCharArray());
            response.setCanceled(false);
        }
        return response;
    }
    
}
