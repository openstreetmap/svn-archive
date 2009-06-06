/*
 * Copyright (C) 2008 Innovant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, please contact:
 *
 *  Innovant
 *   juangui@gmail.com
 *   vidalfree@gmail.com
 *
 *  http://public.grupoinnovant.com/blog
 *
 */
package com.innovant.josm.jrt.core;

public enum PreferencesKeys {
    KEY_ACTIVE_ROUTE_COLOR ("routing.active.route.color"),
    KEY_INACTIVE_ROUTE_COLOR ("routing.inactive.route.color"),
    KEY_ROUTE_WIDTH ("routing.route.width"),
    KEY_ROUTE_SELECT ("routing.route.select");

    public final String key;
    PreferencesKeys (String key) {
        this.key=key;
    }

    public String getKey() {return key;};
}
