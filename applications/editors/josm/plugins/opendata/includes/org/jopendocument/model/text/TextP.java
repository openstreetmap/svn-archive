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

import java.util.List;
import java.util.Vector;

/**
 * 
 */
public class TextP {

    private final List<TextSpan> textSpans = new Vector<TextSpan>();

    public void addTextSpan(final TextSpan p) {
        if (p == null) {
            throw new IllegalArgumentException("null argument");
        }
        this.textSpans.add(p);
    }

    public void addToLastTextSpan(final String string) {

        if (this.isEmpty()) {
            final TextSpan s = new TextSpan();
            s.setValue(string);
            this.textSpans.add(s);
        } else {
            final TextSpan s = this.textSpans.get(this.textSpans.size() - 1);
            s.setValue(s.getValue() + string);
        }
    }


    public boolean isEmpty() {
        return this.textSpans.isEmpty();
    }


    @Override
    public String toString() {

        return "TextP:" + this.textSpans;
    }
}
