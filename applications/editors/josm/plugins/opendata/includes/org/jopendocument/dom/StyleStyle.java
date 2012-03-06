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

import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jopendocument.dom.spreadsheet.CellStyle;
import org.jopendocument.dom.spreadsheet.ColumnStyle;
import org.jopendocument.dom.spreadsheet.RowStyle;
import org.jopendocument.dom.spreadsheet.TableStyle;

/**
 * A style:style, see section 14.1. Maintains a map of family to classes.
 * 
 * @author Sylvain
 */
public class StyleStyle extends ODNode {

    private static final Map<XMLVersion, Map<String, StyleDesc<?>>> family2Desc;
    private static final Map<XMLVersion, Map<Class<? extends StyleStyle>, StyleDesc<?>>> class2Desc;
    private static boolean descsLoaded = false;
    static {
        family2Desc = new HashMap<XMLVersion, Map<String, StyleDesc<?>>>();
        class2Desc = new HashMap<XMLVersion, Map<Class<? extends StyleStyle>, StyleDesc<?>>>();
        for (final XMLVersion v : XMLVersion.values()) {
            family2Desc.put(v, new HashMap<String, StyleDesc<?>>());
            class2Desc.put(v, new HashMap<Class<? extends StyleStyle>, StyleDesc<?>>());
        }
    }

    // lazy initialization to avoid circular dependency (i.e. ClassLoader loads PStyle.DESC which
    // loads StyleStyle which needs PStyle.DESC)
    private static void loadDescs() {
        if (!descsLoaded) {
            registerAllVersions(CellStyle.DESC);
            registerAllVersions(RowStyle.DESC);
            registerAllVersions(ColumnStyle.DESC);
            registerAllVersions(TableStyle.DESC);
            descsLoaded = true;
        }
    }

    // until now styles have remained constant through versions
    private static void registerAllVersions(StyleDesc<? extends StyleStyle> desc) {
        for (final XMLVersion v : XMLVersion.values()) {
            if (v == desc.getVersion())
                register(desc);
            else
                register(StyleDesc.copy(desc, v));
        }
    }

    public static void register(StyleDesc<? extends StyleStyle> desc) {
        if (family2Desc.get(desc.getVersion()).put(desc.getFamily(), desc) != null)
            throw new IllegalStateException(desc.getFamily() + " duplicate");
        if (class2Desc.get(desc.getVersion()).put(desc.getStyleClass(), desc) != null)
            throw new IllegalStateException(desc.getStyleClass() + " duplicate");
    }

    static <S extends StyleStyle> StyleDesc<S> getStyleDesc(Class<S> clazz, final XMLVersion version) {
        return getStyleDesc(clazz, version, true);
    }

    @SuppressWarnings("unchecked")
    private static <S extends StyleStyle> StyleDesc<S> getStyleDesc(Class<S> clazz, final XMLVersion version, final boolean mustExist) {
        loadDescs();
        final Map<Class<? extends StyleStyle>, StyleDesc<?>> map = class2Desc.get(version);
        if (map.containsKey(clazz))
            return (StyleDesc<S>) map.get(clazz);
        else if (mustExist)
            throw new IllegalArgumentException("unregistered " + clazz + " for version " + version);
        else
            return null;
    }

    private final StyleDesc<?> desc;
    private final ODPackage pkg;
    private final String name, family;
    private final XMLVersion ns;

    public StyleStyle(final ODPackage pkg, final Element styleElem) {
        super(styleElem);
        this.pkg = pkg;
        this.name = this.getElement().getAttributeValue("name", this.getSTYLE());
        this.family = this.getElement().getAttributeValue("family", this.getSTYLE());
        this.ns = this.pkg.getVersion();
        this.desc = getStyleDesc(this.getClass(), this.ns, false);
        if (this.desc != null && !this.desc.getFamily().equals(this.getFamily()))
            throw new IllegalArgumentException("expected " + this.desc.getFamily() + " but got " + this.getFamily() + " for " + styleElem);
        // assert that styleElem is in pkg (and thus have the same version)
        assert this.pkg.getXMLFile(getElement().getDocument()) != null;
        assert this.pkg.getVersion() == XMLVersion.getVersion(getElement());
    }

    protected final Namespace getSTYLE() {
        return this.getElement().getNamespace("style");
    }

    public final XMLVersion getNS() { // NO_UCD
        return this.ns;
    }

    public final String getName() {
        return this.name;
    }

    public final String getFamily() {
        return this.family;
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof StyleStyle))
            return false;
        final StyleStyle o = (StyleStyle) obj;
        return this.getName().equals(o.getName()) && this.getFamily().equals(o.getFamily());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode() + this.getFamily().hashCode();
    }
}
