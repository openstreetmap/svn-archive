// License: GPL. For details, see LICENSE file.
package org.openstreetmap.hot.sds;

import java.net.Authenticator.RequestorType;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.openstreetmap.josm.io.auth.CredentialsAgentException;
import org.openstreetmap.josm.io.auth.CredentialsAgentResponse;
import org.openstreetmap.josm.tools.Logging;

/**
 * Base class that handles common things like authentication for the reader and writer
 * to the SDS server.
 *
 * Modeled after JOSM's OsmConnection class.
 */
public class SdsConnection {

    protected boolean cancel = false;
    protected HttpURLConnection activeConnection;
    private SdsCredentialAgent credAgent = new SdsCredentialAgent();

    /**
     * Initialize the http defaults and the authenticator.
     */
    static {
        try {
            HttpURLConnection.setFollowRedirects(true);
        } catch (SecurityException e) {
            Logging.error(e);
        }
    }

    public void cancel() {
        cancel = true;
        synchronized (this) {
            if (activeConnection != null) {
                activeConnection.setConnectTimeout(100);
                activeConnection.setReadTimeout(100);
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logging.trace(ex);
        }

        synchronized (this) {
            if (activeConnection != null) {
                activeConnection.disconnect();
            }
        }
    }

    /**
     * Adds an authentication header for basic authentication
     *
     * @param con the connection
     * @throws SdsTransferException thrown if something went wrong. Check for nested exceptions
     */
    protected void addBasicAuthorizationHeader(HttpURLConnection con) throws SdsTransferException {
        CredentialsAgentResponse response;
        String token;
        try {
            response = credAgent.getCredentials(RequestorType.SERVER, con.getURL().getHost(),
                    false /* don't know yet whether the credentials will succeed */);
        } catch (CredentialsAgentException e) {
            throw new SdsTransferException(e);
        }
        if (response == null) {
            token = ":";
        } else if (response.isCanceled()) {
            cancel = true;
            return;
        } else {
            String username = response.getUsername() == null ? "" : response.getUsername();
            String password = response.getPassword() == null ? "" : String.valueOf(response.getPassword());
            token = username + ":" + password;
            con.addRequestProperty("Authorization", "Basic "+Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)));
        }
    }

     protected void addAuth(HttpURLConnection connection) throws SdsTransferException {
            addBasicAuthorizationHeader(connection);
    }

    /**
     * Replies true if this connection is canceled
     *
     * @return true if this connection is canceled
     */
    public boolean isCanceled() {
        return cancel;
    }
}
