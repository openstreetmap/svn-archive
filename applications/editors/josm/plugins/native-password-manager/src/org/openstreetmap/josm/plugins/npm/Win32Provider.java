// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.npm;

import org.netbeans.modules.keyring.fallback.FallbackProvider;
import org.netbeans.modules.keyring.win32.Win32Protect;
import org.openstreetmap.josm.spi.preferences.Config;

public class Win32Provider extends FallbackProvider {
    
    private static class JOSMPreferences implements IPreferences {
    
        @Override public byte[] getByteArray(String key, byte[] def) {
            String p = Config.getPref().get(key, null);
            return p == null ? def : DatatypeConverter._parseBase64Binary(p);
        }

        @Override public void putByteArray(String key, byte[] val) {
            Config.getPref().put(key, val == null ? null : DatatypeConverter._printBase64Binary(val));
        }

        @Override public void remove(String key) {
            Config.getPref().put(key, null);
        }
    }
    
    public Win32Provider() {
        super(new Win32Protect(), new JOSMPreferences());
    }
}
