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

package org.jopendocument.model.text;


public class TextSpan {

    private String value;

    public void concantValue(final String string) {
        if (string == null) {
            throw new IllegalArgumentException("Style null");
        }

        if (this.value == null) {
            this.value = string;
        } else {
            this.value += string;
        }
    }



    /**
     * Gets the value of the value property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setValue(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("null argument");
        }
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
