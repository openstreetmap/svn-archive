/* Copyright (c) 2008, Henrik Niehaus
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its 
 *    contributors may be used to endorse or promote products derived from this 
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.osb.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Messages {
    private ResourceBundle bundle;

    private static Messages instance;

    private Messages() {
        try {
            loadBundle(Locale.getDefault());
        } catch (IOException e) {
        	e.printStackTrace();
            try {
                loadBundle(Locale.ENGLISH);
            } catch (IOException e1) {
                System.err.println("Couldn't load default resource bundle");
                e.printStackTrace();
            }
        }
    }

    public static ResourceBundle getBundle() {
        if (instance == null) {
            instance = new Messages();
        }
        return instance.getResourceBundle();
    }
    
    @SuppressWarnings("unchecked")
    public static String translate(Class clazz, String key) {
        key = clazz.getName() + "." + key;
        return getBundle().getString(key);
    }
    
    private void loadBundle(Locale locale) throws IOException {
        InputStream in = Messages.class.getResourceAsStream("language_" + locale.toString() + ".properties");
        bundle = new PropertyResourceBundle(in);
    }
    
    private ResourceBundle getResourceBundle() {
        return bundle;
    }

    /**
     * Translates a given key and replaces placeholders with the given parameters.
     * @param clazz
     * @param key
     * @param params
     *      An Object[] of the params, which will be used to replace the placeholders.
     *      The message has to be compatible to {@link MessageFormat}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String translate(Class clazz, String key, Object[] params) {
        String mesg = translate(clazz, key);
        MessageFormat format = new MessageFormat("");
        format.applyPattern(mesg);
        return format.format(params);
    }
}