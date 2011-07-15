// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.npm;

import org.openstreetmap.josm.io.auth.CredentialsAgent;
import org.openstreetmap.josm.io.auth.CredentialsManager.CredentialsAgentFactory;

public class NPMCredentialsAgentFactory implements CredentialsAgentFactory {
    private CredentialsAgent instance;
        
    private NPMType type;

    public NPMCredentialsAgentFactory(NPMType type) {
        this.type = type;
    }

    @Override
    public CredentialsAgent getCredentialsAgent() {
        if (instance == null) {
            instance = new NPMCredentialsAgent(type);
        }
        return instance;
    }

}
