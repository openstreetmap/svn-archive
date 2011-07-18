// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.npm;

import org.apache.commons.codec.binary.Base64;

import org.netbeans.modules.keyring.fallback.FallbackProvider;
import org.netbeans.modules.keyring.win32.Win32Protect;
import org.openstreetmap.josm.Main;

public class Win32Provider extends FallbackProvider {
    
    private static class JOSMPreferences implements IPreferences {
    
        @Override public byte[] getByteArray(String key, byte[] def) {
            String p = Main.pref.get(key, null);
            return p == null ? def : Base64.decodeBase64(p);
        }

        @Override public void putByteArray(String key, byte[] val) {
            Main.pref.put(key, val == null ? null : Base64.encodeBase64String(val));
        }

        @Override public void remove(String key) {
            Main.pref.put(key, null);
        }
    }
    
    public Win32Provider() {
        super(new Win32Protect(), new JOSMPreferences());
    }
}
