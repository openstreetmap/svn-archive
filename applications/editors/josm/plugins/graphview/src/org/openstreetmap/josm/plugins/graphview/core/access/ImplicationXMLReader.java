package org.openstreetmap.josm.plugins.graphview.core.access;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.openstreetmap.josm.plugins.graphview.core.util.TagCondition;
import org.openstreetmap.josm.plugins.graphview.core.util.TagConditionLogic;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * class to be used in SAXHandler implementations for reading implication sections of xml files
 */
public class ImplicationXMLReader {

    private final List<Implication> implications = new LinkedList<Implication>();

    private static enum State {BEFORE_IMPLICATION, BEFORE_CONDITION, CONDITION, BEFORE_IMPLIES, IMPLIES, AFTER_IMPLIES};
    private State state = State.BEFORE_IMPLICATION;

    private ConditionReader currentConditionReader;
    private TagCondition currentCondition;
    private Collection<Tag> currentImpliedTags;

    boolean tagOpen = false;

    public void startElement(String uri, String localName, String name, Attributes attributes)
    throws SAXException {

        switch (state) {

            case BEFORE_IMPLICATION:

                if ("implication".equals(name)) {
                    state = State.BEFORE_CONDITION;
                    return;
                }
                break;

            case BEFORE_CONDITION:

                if ("condition".equals(name)) {
                    currentConditionReader = new ConditionReader();
                    state = State.CONDITION;
                    return;
                }
                break;

            case CONDITION:
                currentConditionReader.startElement(uri, localName, name, attributes);
                return;

            case BEFORE_IMPLIES:

                if ("implies".equals(name)) {
                    currentImpliedTags = new LinkedList<Tag>();
                    state = State.IMPLIES;
                    return;
                }
                break;

            case IMPLIES:

                if ("tag".equals(name)) {
                    if (tagOpen) {
                        throw new SAXException(tr("Tag element inside other tag element"));
                    }
                    currentImpliedTags.add(readTag(attributes));
                    tagOpen = true;
                    return;
                }
                break;

        }

        //all vaild paths end with return; reaching this indicates an invalid tag
        throw new SAXException(tr("Invalid opening xml tag <{0}> in state {1}", name, state));

    }

    public void endElement(String uri, String localName, String name)
    throws SAXException {

        switch (state) {

            case CONDITION:

                if (name.equals("condition")) {
                    if (!currentConditionReader.isFinished()) {
                        throw new SAXException(tr("Condition isn''t finished at </condition> tag"));
                    } else {
                        currentCondition = currentConditionReader.getCondition();
                        currentConditionReader = null;
                        state = State.BEFORE_IMPLIES;
                        return;
                    }
                } else {
                    currentConditionReader.endElement(uri, localName, name);
                    return;
                }

            case IMPLIES:

                if (name.equals("implies")) {
                    state = State.AFTER_IMPLIES;
                    return;
                } else if (name.equals("tag")) {
                    if (!tagOpen) {
                        throw new SAXException(tr("Closing tag element that was not open"));
                    }
                    tagOpen = false;
                    return;
                }
                break;

            case AFTER_IMPLIES:

                if (name.equals("implication")) {
                    implications.add(new Implication(currentCondition, currentImpliedTags));
                    currentCondition = null;
                    currentImpliedTags = null;
                    state = State.BEFORE_IMPLICATION;
                    return;
                }
                break;

        }

        //all vaild paths end with return; reaching this indicates an invalid tag
        throw new SAXException(tr("Invalid closing xml tag </{0}> in state {1}", name, state));

    }

    public List<Implication> getImplications() throws SAXException {

        if (state != State.BEFORE_IMPLICATION) {
            throw new SAXException(tr("Some tags not been closed; now in state {0}", state));
        } else {
            return new ArrayList<Implication>(implications);
        }
    }

    private static Tag readTag(Attributes attributes) throws SAXException {

        String key = attributes.getValue("k");
        String value = attributes.getValue("v");

        if (key == null) {
            throw new SAXException(tr("Tag without key"));
        } else if (value == null) {
            throw new SAXException(tr("Tag without value (key is {0})", key));
        }

        return new Tag(key, value);
    }

    private static String readKey(Attributes attributes) throws SAXException {

        String key = attributes.getValue("k");

        if (key == null) {
            throw new SAXException(tr("Key element without attribute k"));
        }

        return key;
    }

    /**
     * class to be used for reading tag condition sections of xml files
     */
    private static class ConditionReader {

        String openingName;
        TagCondition condition;
        boolean finished;

        private final List<ConditionReader> childReaders = new LinkedList<ConditionReader>();
        private ConditionReader currentChildReader = null;

        public void startElement(String uri, String localName, String name, Attributes attributes)
        throws SAXException {

            if (finished) {
                throw new SAXException(tr("Condition is already finished at <{0}>", name));
            }

            if (currentChildReader != null) {
                currentChildReader.startElement(uri, localName, name, attributes);
                return;
            }

            //first tag is start tag of this condition
            if (openingName == null) {

                openingName = name;

                if ("tag".equals(name)) {
                    condition = TagConditionLogic.tag(readTag(attributes));
                } else if ("key".equals(name)) {
                    condition = TagConditionLogic.key(readKey(attributes));
                } else if (!("or".equals(name)) && !("and".equals(name)) && !("not".equals(name))) {
                    throw new SAXException(tr("Unknown tag for condition: {0}", name));
                }

                //all tags after the first are start tags of child conditions
            } else {

                if ("tag".equals(openingName) || "key".equals(openingName)) {
                    throw new SAXException(tr("Element must not have childs: {0}", openingName));
                }

                currentChildReader = new ConditionReader();
                currentChildReader.startElement(uri, localName, name, attributes);

            }

        }

        public void endElement(String uri, String localName, String name)
        throws SAXException {

            if (finished) {
                throw new SAXException(tr("Condition is already finished at </{0}>", name));
            }

            /* if active child reader exists, pass parameter to it. */
            if (currentChildReader != null) {

                currentChildReader.endElement(uri, localName, name);

                if (currentChildReader.isFinished()) {
                    childReaders.add(currentChildReader);
                    currentChildReader = null;
                }

            } else {

                if (openingName.equals(name)) {

                    List<TagCondition> childConditions = new ArrayList<TagCondition>();
                    for (ConditionReader childReader : childReaders) {
                        childConditions.add(childReader.getCondition());
                    }

                    if ("and".equals(openingName)) {
                        if (childConditions.size() > 0) {
                            condition = TagConditionLogic.and(childConditions);
                        } else {
                            throw new SAXException(tr("<and> needs at least one child"));
                        }
                    } else if ("or".equals(openingName)) {
                        if (childConditions.size() > 0) {
                            condition = TagConditionLogic.or(childConditions);
                        } else {
                            throw new SAXException(tr("<or> needs at least one child"));
                        }
                    } else if ("not".equals(openingName)) {
                        if (childConditions.size() == 1) {
                            condition = TagConditionLogic.not(childConditions.get(0));
                        } else {
                            throw new SAXException(tr("<not> needs at least one child"));
                        }
                    }

                    finished = true;

                } else {
                    throw new SAXException(tr("Wrong closing tag {0} (</{1}> expected)", name, openingName));
                }

            }

        }

        public boolean isFinished() {
            return finished;
        }

        public TagCondition getCondition() {
            if (!finished) {
                throw new IllegalStateException(tr("Condition {0} not yet finished", openingName));
            } else {
                assert condition != null;
                return condition;
            }
        }

    }

}
