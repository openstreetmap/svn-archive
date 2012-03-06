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

/*
 * Créé le 3 mars 2005
 */
package org.jopendocument.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sylvain CUAZ
 */
public class StringUtils {

    public static final class Escaper {

        // eg '
        private final char esc;

        // eg { '=> S, " => D}
        private final Map<Character, Character> substitution;
        private final Map<Character, Character> inv;

        /**
         * A new escaper that will have <code>esc</code> as escape character.
         * 
         * @param esc the escape character, eg '
         * @param name the character that will be appended to <code>esc</code>, eg with S all
         *        occurrences of ' will be replaced by 'S
         */
        public Escaper(char esc, char name) {
            super();
            this.esc = esc;
            this.substitution = new LinkedHashMap<Character, Character>();
            this.inv = new HashMap<Character, Character>();
            this.add(esc, name);
        }

        public Escaper add(char toRemove, char escapedName) {
            if (this.inv.containsKey(escapedName))
                throw new IllegalArgumentException(escapedName + " already replaces " + this.inv.get(escapedName));
            this.substitution.put(toRemove, escapedName);
            this.inv.put(escapedName, toRemove);
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Escaper) {
                final Escaper o = (Escaper) obj;
                return this.esc == o.esc && this.substitution.equals(o.substitution);
            } else
                return false;
        }

        @Override
        public int hashCode() {
            return this.esc + this.substitution.hashCode();
        }
    }

}