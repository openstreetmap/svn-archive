/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.keyring.fallback;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.keyring.impl.Utils;
import org.netbeans.modules.keyring.spi.EncryptionProvider;
import org.netbeans.spi.keyring.KeyringProvider;

/**
 * Platform-independent keyring provider using a master password and the user directory.
 */
public class FallbackProvider implements KeyringProvider {

    private static final Logger LOG = Logger.getLogger(FallbackProvider.class.getName());
    private static final String DESCRIPTION = ".description";
    private static final String SAMPLE_KEY = "__sample__";

    private EncryptionProvider encryption;
    private IPreferences prefs;

    // simple interface for a generic preferences store
    public interface IPreferences {
        byte[] getByteArray(String key, byte[] def);
        void putByteArray(String key, byte[] val);
        void remove(String key);
    }

    public FallbackProvider(EncryptionProvider encryption, IPreferences prefs) {
        this.encryption = encryption;
        this.prefs = prefs;
    }

    public boolean enabled() {
        if (encryption.enabled()) {
            if (testSampleKey()) {
                LOG.log(Level.FINE, "Using provider: {0}", encryption);
                return true;
            }
        }
        LOG.fine("No provider");
        return false;
    }
    
    private boolean testSampleKey() {
        encryption.freshKeyring(true);
        if (_save(SAMPLE_KEY, (SAMPLE_KEY + UUID.randomUUID()).toCharArray(),
                "Sample value ensuring that decryption is working.")) {
            LOG.fine("saved sample key");
            return true;
        } else {
            LOG.fine("could not save sample key");
            return false;
        }
    }

    public char[] read(String key) {
        byte[] ciphertext = prefs.getByteArray(key, null);
        if (ciphertext == null) {
            return null;
        }
        try {
            return encryption.decrypt(ciphertext);
        } catch (Exception x) {
            LOG.log(Level.FINE, "failed to decrypt password for " + key, x);
        }
        return null;
    }

    public void save(String key, char[] password, String description) {
        _save(key, password, description);
    }
    private boolean _save(String key, char[] password, String description) {
        try {
            prefs.putByteArray(key, encryption.encrypt(password));
        } catch (Exception x) {
            LOG.log(Level.FINE, "failed to encrypt password for " + key, x);
            return false;
        }
        return true;
    }

    public void delete(String key) {
        prefs.remove(key);
        prefs.remove(key + DESCRIPTION);
    }

}
