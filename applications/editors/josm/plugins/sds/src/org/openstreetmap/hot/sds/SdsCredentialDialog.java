// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;

import org.openstreetmap.josm.gui.io.CredentialDialog;

@SuppressWarnings("serial")
public class SdsCredentialDialog extends CredentialDialog {

	static public SdsCredentialDialog getSdsApiCredentialDialog(String username, String password, String host, String saveUsernameAndPasswordCheckboxText) {
        SdsCredentialDialog dialog = new SdsCredentialDialog(saveUsernameAndPasswordCheckboxText);
        dialog.prepareForSdsApiCredentials(username, password);
        dialog.pack();
        return dialog;
    }

    String saveUsernameAndPasswordCheckboxText;

    public SdsCredentialDialog(String saveUsernameAndPasswordCheckboxText) {
    	super(saveUsernameAndPasswordCheckboxText);
    }
    	 
    public void prepareForSdsApiCredentials(String username, String password) {
        setTitle(tr("Enter credentials for Separate Data Store API"));
        getContentPane().add(pnlCredentials = new SdsApiCredentialsPanel(this), BorderLayout.CENTER);
        pnlCredentials.init(username, password);
        validate();
    }
 
    private static class SdsApiCredentialsPanel extends CredentialPanel {

		@Override
        protected void build() {
            super.build();
            tfUserName.setToolTipText(tr("Please enter the user name of your SDS account"));
            tfPassword.setToolTipText(tr("Please enter the password of your SDS account"));
            lblHeading.setText(
                    "<html>" + tr("Authenticating at the SDS API ''{0}'' failed. Please enter a valid username and a valid password.",
                            SdsApi.getSdsApi().getBaseUrl()) + "</html>");
            lblWarning.setText(tr("Warning: The password is transferred unencrypted."));
        }

        public SdsApiCredentialsPanel(SdsCredentialDialog owner) {
            super(owner);
            build();
        }
    }

  }
