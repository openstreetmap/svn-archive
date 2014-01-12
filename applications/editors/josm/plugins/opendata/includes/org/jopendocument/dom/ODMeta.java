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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * OpenDocument metadata, obtained through {@link ODPackage#getMeta()}.
 * 
 * @author Sylvain
 * @see "section 3 of OpenDocument v1.1"
 */
public class ODMeta extends ODNode {

    static ODMeta create(ODXMLDocument parent) {
        final Element meta = parent.getChild("meta");
        return meta == null ? null : new ODMeta(meta, parent);
    }

    private static final Map<XMLVersion, List<Element>> ELEMS_ORDER;
    static {
        ELEMS_ORDER = new HashMap<XMLVersion, List<Element>>(2);
        ELEMS_ORDER.put(XMLVersion.getOOo(), createChildren(XMLVersion.getOOo()));
        ELEMS_ORDER.put(XMLVersion.getOD(), createChildren(XMLVersion.getOD()));
    }

    private static final List<Element> createChildren(XMLVersion ins) {
        final Namespace meta = ins.getMETA();
        final Namespace dc = ins.getNS("dc");
        final List<Element> res = new ArrayList<Element>(8);
        res.add(new Element("generator", meta));
        res.add(new Element("title", dc));
        res.add(new Element("description", dc));
        res.add(new Element("subject", dc));
        res.add(new Element("keyword", meta));
        res.add(new Element("initial-creator", meta));
        res.add(new Element("creator", dc));
        res.add(new Element("printed-by", meta));
        res.add(new Element("creation-date", meta));
        res.add(new Element("date", dc));
        res.add(new Element("print-date", meta));
        res.add(new Element("template", meta));
        res.add(new Element("auto-reload", meta));
        res.add(new Element("hyperlink-behaviour", meta));
        res.add(new Element("language", dc));
        res.add(new Element("editing-cycles", meta));
        res.add(new Element("editing-duration", meta));
        res.add(new Element("document-statistic", meta));
        res.add(new Element("user-defined", meta));
        return res;
    }

    // *** instance

    private final ODXMLDocument parent;
    private final ChildCreator childCreator;

    private ODMeta(final Element elem, ODXMLDocument parent) {
        super(elem);
        this.parent = parent;
        this.childCreator = new ChildCreator(this.getElement(), ELEMS_ORDER.get(this.getNS()));
    }

    protected final ODXMLDocument getParent() {
        return this.parent;
    }

    private final XMLVersion getNS() {
        return this.getParent().getVersion();
    }

    public final String getInitialCreator() { // NO_UCD
        return this.getMetaChild("initial-creator").getTextTrim();
    }

    public final String getCreator() { // NO_UCD
        return this.getDCChild("creator").getTextTrim();
    }

    public final Calendar getCreationDate() { // NO_UCD
        return this.getDateChild("creation-date", this.getNS().getMETA());
    }

    public final Calendar getModifDate() { // NO_UCD
        return this.getDateChild("date", this.getNS().getNS("dc"));
    }

    public final String getLanguage() { // NO_UCD
        return this.getDCChild("language").getTextTrim();
    }

    /**
     * Return the metadata with the passed name, optionnaly creating it.
     * 
     * @param name the name of user metadata.
     * @param create <code>true</code> if it should be created.
     * @return the requested metadata, or <code>null</code> if none is found and <code>create</code>
     *         is <code>false</code>.
     */
    public final ODUserDefinedMeta getUserMeta(String name, boolean create) {
        final Element userElem = ODUserDefinedMeta.getElement(this.getElement(), name, this.getNS());
        if (userElem != null)
            return new ODUserDefinedMeta(userElem, this.getParent());
        else if (create) {
            final ODUserDefinedMeta res = ODUserDefinedMeta.create(name, this.getParent());
            this.getElement().addContent(res.getElement());
            return res;
        } else
            return null;
    }

    // * getChild

    public final Element getMetaChild(final String name) {
        return this.getChild(name, this.getNS().getMETA());
    }

    public final Element getDCChild(final String name) {
        return this.getChild(name, this.getNS().getNS("dc"));
    }

    private final Element getChild(final String name, final Namespace ns) {
        return this.childCreator.getChild(ns, name, true);
    }

    private final Calendar getDateChild(final String name, final Namespace ns) {
        final String date = this.getChild(name, ns).getTextTrim();
        if (date.length() == 0)
            return null;
        else {
            final Calendar cal = Calendar.getInstance();
            try {
                cal.setTime((Date) OOUtils.DATE_FORMAT.parseObject(date));
            } catch (ParseException e) {
                throw new IllegalStateException("wrong date: " + date, e);
            }
            return cal;
        }
    }
}
