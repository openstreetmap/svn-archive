// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import static org.openstreetmap.josm.tools.I18n.tr;

/**
 * Exception that wraps any exception thrown by modules. It is used in the JOSM main system
 * and there is no particular reason to use this within the module itself (although there
 * is also no reason against this.. ;)
 *
 * @author Immanuel.Scholz
 */
public class ModuleException extends Exception {
    public final Module module; // NO_UCD
    public final String name;

    public ModuleException(Module module, String name, Throwable cause) { // NO_UCD
        super(tr("An error occurred in module {0}", name), cause);
        this.module = module;
        this.name = name;
    }

    public ModuleException(String name, String message) {
        super(message);
        this.module = null;
        this.name = name;
    }

    public ModuleException(String name, Throwable cause) {
        super(tr("An error occurred in module {0}", name), cause);
        this.module = null;
        this.name = name;
    }
}
