/*
 *  JOSMng - a Java Open Street Map editor, the next generation.
 * 
 *  Copyright (C) 2008 Petr Nejedly <P.Nejedly@sh.cvut.cz>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package org.openstreetmap.josmng.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation for placing actions into menus and toolbar.
 * An action, to be visible in UI, needs to be listed in the services
 * registry (META-INF/services/javax.swing.Action) and needs to carry
 * this annotation.
 * 
 *
 * @author nenik
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuPosition {
    /**
     * The menu to place this action to. It can declare submenus
     * by using slash, for example an action A annotated with "Tools/Checks"
     * will and up as menu item 'A' placed in 'Checks' submenu of the 'Tools'
     * menu.
     */
    String value();
    
    boolean inToolbar() default false;
    
    /**
     * Assigns a global shortcut to the referenced action
     * @return a String in the {@link javax.swing.KeyStroke#getKeyStroke(String)} format.
     */
    String shortcut() default "";
}
