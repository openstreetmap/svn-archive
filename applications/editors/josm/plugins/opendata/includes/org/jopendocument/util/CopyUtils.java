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

package org.jopendocument.util;

import java.lang.reflect.Method;

public final class CopyUtils {

    /**
     * Copy the passed object. First tries to clone() it, otherwise tries with a copy constructor.
     * 
     * @param <E> the type of object to be copied.
     * @param object the object to be copied, can be <code>null</code>.
     * @return a copy of <code>object</code>, or <code>null</code> if object was
     *         <code>null</code>.
     * @throws IllegalStateException if the object can't be copied.
     */
    @SuppressWarnings("unchecked")
    public static final <E> E copy(E object) {
        if (object == null)
            return null;

        if (object instanceof Cloneable) {
            final Method m;
            try {
                m = object.getClass().getMethod("clone");
            } catch (NoSuchMethodException e) {
                throw ExceptionUtils.createExn(IllegalStateException.class, "Cloneable w/o clone()", e);
            }
            try {
                return (E) m.invoke(object);
            } catch (Exception e) {
                throw ExceptionUtils.createExn(IllegalStateException.class, "clone() failed", e);
            }
        } else {
            try {
                return (E) object.getClass().getConstructor(new Class[] { object.getClass() }).newInstance(new Object[] { object });
            } catch (Exception e) {
                throw ExceptionUtils.createExn(IllegalStateException.class, "Copy constructor failed", e);
            }
        }
    }

}