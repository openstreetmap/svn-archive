// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.npm;

import javax.xml.bind.DatatypeConverter;

import org.netbeans.modules.keyring.fallback.FallbackProvider;
import org.netbeans.modules.keyring.win32.Win32Protect;
import org.openstreetmap.josm.Main;

public class Win32Provider extends FallbackProvider {
    
    private static class JOSMPreferences implements IPreferences {
    
        @Override public byte[] getByteArray(String key, byte[] def) {
            String p = Main.pref.get(key, null);
            return p == null ? def : DatatypeConverter.parseBase64Binary(p);
        }

        @Override public void putByteArray(String key, byte[] val) {
            Main.pref.put(key, val == null ? null : DatatypeConverter.printBase64Binary(val));
        }

        @Override public void remove(String key) {
            Main.pref.put(key, null);
        }
    }
    
    public Win32Provider() {
        super(new Win32Protect(), new JOSMPreferences());
    }
}
