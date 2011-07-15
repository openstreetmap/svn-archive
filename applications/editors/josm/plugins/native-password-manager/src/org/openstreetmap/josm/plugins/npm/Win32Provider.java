// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.npm;

import org.netbeans.modules.keyring.fallback.FallbackProvider;
import org.netbeans.modules.keyring.win32.Win32Protect;
import org.openstreetmap.josm.Main;

public class Win32Provider extends FallbackProvider {
    
    private static class JOSMPreferences implements IPreferences {
        @Override public String get(String key, String def) {
            return Main.pref.get(key, def);
        }

        @Override public void put(String key, String val) {
            Main.pref.put(key, val);
        }

        @Override public void remove(String key) {
            Main.pref.put(key, null);
        }
    }
    
    public Win32Provider() {
        super(new Win32Protect(), new JOSMPreferences());
    }
}
