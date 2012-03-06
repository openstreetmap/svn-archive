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

import java.lang.reflect.Constructor;

/**
 * Utilitaires pour les exceptions.
 * 
 * @author Sylvain CUAZ 25 nov. 2004
 */
public class ExceptionUtils {
    /** Static only. */
    private ExceptionUtils() {
        super();
    }

    /**
     * Crée une exception avec message et cause.
     * 
     * @param <T> le type d'exception à créer.
     * @param exnClass la classe de l'exception à créer, eg IOException.class.
     * @param msg le message.
     * @param cause la cause.
     * @return une exception initialisée.
     */
    static public <T extends Exception> T createExn(Class<T> exnClass, String msg, Throwable cause) {
        T instance = null;
        try {
            Constructor<T> ctor = exnClass.getConstructor(new Class[] { String.class });
            instance = ctor.newInstance(new Object[] { msg });
        } catch (Exception exn) {
            throw new IllegalArgumentException(exnClass + " has no working String constructor");
        }
        instance.initCause(cause);
        return instance;
    }
}