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

package org.openstreetmap.josmng.osm;

/**
 * An undo queue token allowing atomic actions having additional limited
 * interaction with the queue. The Token is to be used for
 * {@link DataSet#atomicEdit(java.lang.Runnable, org.openstreetmap.josmng.osm.Token) .

 * It has three purposes:<ul>
 * <li>Allow replacing older edit with newer one.</li>
 * <li>Allow naming the whole edit for the purpose of the Undo action name.</li>
 * <li>Allow performing some additional action once the atomic edit is undone.<li>
 * </ul>
 * @author nenik
 */
public class Token {
    private String name;

    public Token() {
        name = null;
    }

    public Token(String name) {
        this.name = name;
    }
    
    // a callback that is called whenever is given edit undone.
    protected void onUndone() {}

    protected String name() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
