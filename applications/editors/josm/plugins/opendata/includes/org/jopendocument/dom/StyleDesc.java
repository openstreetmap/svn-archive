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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jdom.Element;
import org.jopendocument.util.CollectionMap;

/**
 * Describe a family of style.
 * 
 * @author Sylvain CUAZ
 * 
 * @param <S> type of style
 */
public abstract class StyleDesc<S extends StyleStyle> {

    public static <C extends StyleStyle> StyleDesc<C> copy(final StyleDesc<C> toClone, final XMLVersion version) {
        final StyleDesc<C> res = new StyleDesc<C>(toClone.getStyleClass(), version, toClone.getFamily(), toClone.getBaseName()) {
            @Override
            public C create(ODPackage pkg, Element e) {
                return toClone.create(pkg, e);
            }
        };
        res.getRefElementsMap().putAll(toClone.getRefElementsMap());
        res.getMultiRefElementsMap().putAll(toClone.getMultiRefElementsMap());
        return res;
    }

    private final Class<S> clazz;
    // need version since each one might have different attributes and elements (plus we need it
    // anyway for the XPath, otherwise it fails when searching for an inexistant namespace)
    private final XMLVersion version;
    private final String family, baseName;
    // { attribute -> element }
    private final CollectionMap<String, String> refElements;
    private final CollectionMap<String, String> multiRefElements;

    protected StyleDesc(final Class<S> clazz, final XMLVersion version, String family, String baseName, String ns) {
        this(clazz, version, family, baseName, ns, Collections.singletonList(ns + ":" + family));
    }

    protected StyleDesc(final Class<S> clazz, final XMLVersion version, String family, String baseName, String ns, final List<String> refQNames) {
        this(clazz, version, family, baseName);
        this.getRefElementsMap().putAll(ns + ":style-name", refQNames);
    }

    protected StyleDesc(final Class<S> clazz, final XMLVersion version, String family, String baseName) {
        super();
        this.clazz = clazz;
        this.version = version;
        this.family = family;
        this.baseName = baseName;
        this.refElements = new CollectionMap<String, String>();
        // 4 since they are not common
        this.multiRefElements = new CollectionMap<String, String>(4);
    }

    public abstract S create(ODPackage pkg, Element e);

    final Class<S> getStyleClass() {
        return this.clazz;
    }

    public final XMLVersion getVersion() {
        return this.version;
    }

    public final String getFamily() {
        return this.family;
    }

    public final String getBaseName() {
        return this.baseName;
    }

    /**
     * The list of elements that can point to this family of style.
     * 
     * @return a list of qualified names, e.g. ["text:h", "text:p"].
     */
    protected final Collection<String> getRefElements() {
        return this.getRefElementsMap().values();
    }

    // e.g. { "text:style-name" -> ["text:h", "text:p"] }
    protected final CollectionMap<String, String> getRefElementsMap() {
        return this.refElements;
    }

    // e.g. { "table:default-cell-style-name" -> ["table:table-column", "table:table-row"] }
    protected final CollectionMap<String, String> getMultiRefElementsMap() {
        return this.multiRefElements;
    }
}