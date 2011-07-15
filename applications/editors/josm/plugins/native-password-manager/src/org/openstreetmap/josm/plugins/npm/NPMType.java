// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.npm;

import org.netbeans.modules.keyring.gnome.GnomeProvider;
import org.netbeans.modules.keyring.kde.KWalletProvider;
import org.netbeans.modules.keyring.mac.MacProvider;
import org.netbeans.spi.keyring.KeyringProvider;
import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.Utils.equal;

public enum NPMType {
    PLAIN(
            "josm-standard",
            null,
            "default",
            tr("Plain text, JOSM default")
    ),
    GNOME_KEYRING(
            "gnome-keyring",
            GnomeProvider.class,
            "gnome-keyring",
            tr("Use {0}", "gnome-keyring")
    ),
    KWALLET(
            "kwallet",
            KWalletProvider.class,
            "KWallet",
            tr("Use {0}", "KWallet")),
    KEYCHAIN(
            "keychain",
            MacProvider.class,
            "Apple Keychain",
            tr("Use {0}", "Mac OS X Keychain")),
    CRYPT32(
            "crypt32", 
            Win32Provider.class,
            "Windows data encryption",
            tr("Encrypt data with Windows logon credentials")
    );
    
    private static String genericIntro(String name) {
        return tr("The native password manager plugin detected {0} on your system.", name);
//                + "You can let {1} handle your sensitive data from now on. "
//                + "This means your username and password is no longer saved in plain text in your preferences file. "
//                + "(It is still transferred over the network unencrypted, though.)", name, name);
    }
    
    private final String prefString;
    private final Class<? extends KeyringProvider> providerClass;
    private final String name;
    private KeyringProvider provider;
    private final String introText;
    private final String selectionText;

    NPMType(String prefString, Class<? extends KeyringProvider> providerClass, String name, String selectionText) {
        this.prefString = prefString;
        this.providerClass = providerClass;
        this.name = name;
        this.introText = genericIntro(name);
        this.selectionText = selectionText;
    }

    public static NPMType fromPrefString(String pref) {
        for (NPMType t : NPMType.values()) {
            if (equal(pref, t.prefString)) {
                return t;
            }
        }
        return null;
    }

    public String toPrefString() {
        return prefString;
    }

    public String getSelectionText() {
        return selectionText;
    }

    public String getIntroText() {
        return introText;
    }

    public String getName() {
        return name;
    }
    
    public KeyringProvider getProvider() {
        if (providerClass == null) return null;
        if (provider == null) {
            try {
                provider = providerClass.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException();
            } catch (IllegalAccessException ex) {
                throw new RuntimeException();
            }
        }
        return provider;
    }
}
