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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;

/**
 * A simple service loader, replacement for JDK1.6's one to work on JDK1.5.
 * The API is basically a subset of 1.6's java.util.ServiceLoader, while
 * the implementation is cleanroom. 
 * 
 * @author nenik
 */
public final class ServiceLoader<T> implements Iterable<T> {
    private final Class type;
    private Map<String, Reference<T>> instancesCache;

    private ServiceLoader(Class<T> type) {
        this.type = type;
    }

    public synchronized Iterator<T> iterator() {
        try {
            if (instancesCache == null) parse();
            return new Iterator<T>() {
                Iterator<String> delegate = instancesCache.keySet().iterator();

                public boolean hasNext() {
                    return delegate.hasNext();
                }

                public T next() {
                    String key = delegate.next();
                    Reference<T> ref = instancesCache.get(key);
                    T val = ref.get();
                    if (val == null) {
                        try {
                            Class<T> cls = (Class<T>)Class.forName(key);
                            val = cls.newInstance();
                            instancesCache.put(key, new WeakReference<T>(val));
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    return val;
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    


    private void parse() throws IOException {
        instancesCache = new LinkedHashMap<String, Reference<T>>();
        String name = "META-INF/services/" + type.getName();
        Enumeration<URL> en = getClass().getClassLoader().getResources(name);
        while (en.hasMoreElements()) {
            parse(en.nextElement().openStream());
        }
    }
    
    private void parse(InputStream is) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                if (instancesCache.containsKey(line)) continue;
                instancesCache.put(line, new WeakReference<T>(null));
            }
        } finally {
            is.close();
        }
    }

    public static <T> ServiceLoader<T> load(Class<T> serviceType) {
        return new ServiceLoader(serviceType);
    }
}
