package org.openstreetmap.josm.plugins.graphview.core.access;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openstreetmap.josm.plugins.graphview.core.data.Tag;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * utility class that can read access rulesets from xml files
 */
public class AccessRulesetReader {

    public static class AccessRulesetSyntaxException extends IOException {
        private static final long serialVersionUID = 1L;
        public AccessRulesetSyntaxException(String message) {
            super(message);
        };
        public AccessRulesetSyntaxException(Throwable t) {
            super(t.toString());
        }
    }

    /** private constructor to prevent instantiation */
    private AccessRulesetReader() { }

    public static AccessRuleset readAccessRuleset(InputStream inputStream)
    throws AccessRulesetSyntaxException, IOException {

        RulesetHandler rulesetHandler = new RulesetHandler();

        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            InputSource input = new InputSource(inputStream);
            reader.setContentHandler(rulesetHandler);
            reader.setErrorHandler(null);
            reader.parse(input);
        } catch (SAXException e) {
            throw new AccessRulesetSyntaxException(e);
        }

        return rulesetHandler.getAccessRuleset();
    }

    private static class RulesetHandler extends DefaultHandler {

        private static class AccessClass {
            final String name;
            final AccessClass parent;
            public AccessClass(String name, AccessClass parent) {
                this.name = name;
                this.parent = parent;
            }
            List<String> getAncestorHierarchy() {
                List<String> names;
                if (parent == null) {
                    names = new LinkedList<String>();
                } else {
                    names = parent.getAncestorHierarchy();
                }
                names.add(0, name);
                return names;
            }
        }

        private final Collection<AccessClass> accessClasses = new LinkedList<AccessClass>();
        private final Collection<Tag> baseTags = new LinkedList<Tag>();

        private static enum Section {NONE, CLASSES, BASETAGS, IMPLICATIONS};
        private Section currentSection = Section.NONE;

        private AccessClass currentAccessClass = null;

        private ImplicationXMLReader implicationReader = null;
        private final List<Implication> implications = new LinkedList<Implication>();

        /** returns the AccessRuleset that was read */
        AccessRuleset getAccessRuleset() {

            return new AccessRuleset() {

                public List<String> getAccessHierarchyAncestors(String transportMode) {
                    for (AccessClass accessClass : accessClasses) {
                        if (accessClass.name.equals(transportMode)) {
                            return accessClass.getAncestorHierarchy();
                        }
                    }
                    return new LinkedList<String>();
                }

                public Collection<Tag> getBaseTags() {
                    return baseTags;
                }

                public List<Implication> getImplications() {
                    return implications;
                }

            };
        }

        @Override
        public void startElement(String uri, String localName, String name,
                Attributes attributes) throws SAXException {

            if (implicationReader != null) {
                implicationReader.startElement(uri, localName, name, attributes);
                return;
            }

            if ("classes".equals(name)) {

                if (currentSection != Section.NONE) {
                    throw new SAXException(tr("Classes element below root child level"));
                }

                currentSection = Section.CLASSES;

            } else if ("class".equals(name)) {

                String className = attributes.getValue("name");

                if (currentSection != Section.CLASSES) {
                    throw new SAXException(tr("Class element ({0}) outside classes element", className));
                } else if (className == null) {
                    throw new SAXException(tr("Class element without name"));
                }

                AccessClass newAccessClass = new AccessClass(className, currentAccessClass);

                accessClasses.add(newAccessClass);

                currentAccessClass = newAccessClass;

            } else if ("basetags".equals(name)) {

                if (currentSection != Section.NONE) {
                    throw new SAXException(tr("Classes element below root child level"));
                }

                currentSection = Section.BASETAGS;

            } else if ("tag".equals(name)) {

                if (currentSection == Section.BASETAGS) {
                    baseTags.add(readTag(attributes));
                } else {
                    throw new SAXException(tr("Tag element outside basetag and implication elements"));
                }

            } else if ("implications".equals(name)) {

                if (currentSection != Section.NONE) {
                    throw new SAXException(tr("Implications element below root child level"));
                }

                implicationReader = new ImplicationXMLReader();
                currentSection = Section.IMPLICATIONS;

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

        @Override
        public void endElement(String uri, String localName, String name)
        throws SAXException {

            if (implicationReader != null && !"implications".equals(name)) {
                implicationReader.endElement(uri, localName, name);
            }

            if ("classes".equals(name)) {

                if (currentSection != Section.CLASSES) {
                    throw new SAXException(tr("Closed classes while it was not open"));
                } else if (currentAccessClass != null) {
                    throw new SAXException(tr("Closed classes element before all class elements were closed"));
                }

                currentSection = Section.NONE;

            } else if ("class".equals(name)) {

                if (currentAccessClass == null) {
                    throw new SAXException(tr("Closed class element while none was open"));
                }

                currentAccessClass = currentAccessClass.parent;

            } else if ("basetags".equals(name)) {

                if (currentSection != Section.BASETAGS) {
                    throw new SAXException(tr("Closed basetags while it was not open"));
                }

                currentSection = Section.NONE;

            } else if ("implications".equals(name)) {

                if (currentSection != Section.IMPLICATIONS) {
                    throw new SAXException(tr("Closed implications while it was not open"));
                }

                implications.addAll(implicationReader.getImplications());
                implicationReader = null;
                currentSection = Section.NONE;

            }

        }

    };
}
