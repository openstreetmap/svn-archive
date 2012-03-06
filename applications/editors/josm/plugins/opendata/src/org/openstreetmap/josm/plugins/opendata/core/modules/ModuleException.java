//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
