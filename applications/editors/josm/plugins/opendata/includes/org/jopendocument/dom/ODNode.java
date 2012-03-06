/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 jOpenDocument, by ILM Informatique. All rights reserved.
 * 
 * The contents of this file are subject to the terms of the GNU
 * General Public License Version 3 only ("GPL").  
 * You may not use this file except in compliance with the License. 
 * You can obtain a copy of the License at http://www.gnu.org/licenses/gpl-3.0.html
 * See the License for the specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each file.
 * 
 */

package org.jopendocument.dom;

import org.jdom.Element;

/**
 * A node in an XML document.
 * 
 * @author Sylvain
 */
public abstract class ODNode {

    private final Element localElement;

    public ODNode(final Element elem) {
        if (elem == null)
            throw new NullPointerException();
        this.localElement = elem;
    }

    public final Element getElement() {
        return this.localElement;
    }

}
