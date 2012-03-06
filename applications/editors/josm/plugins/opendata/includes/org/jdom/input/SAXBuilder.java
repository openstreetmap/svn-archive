/*--

 $Id: SAXBuilder.java,v 1.93 2009/07/23 06:26:26 jhunter Exp $

 Copyright (C) 2000-2007 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows
    these conditions in the documentation and/or other materials
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many
 individuals on behalf of the JDOM Project and was originally
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 */

package org.jdom.input;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.DefaultJDOMFactory;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.EntityRef;
import org.jdom.JDOMException;
import org.jdom.JDOMFactory;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Builds a JDOM document from files, streams, readers, URLs, or a SAX {@link
 * org.xml.sax.InputSource} instance using a SAX parser. The builder uses a
 * third-party SAX parser (chosen by JAXP by default, or you can choose
 * manually) to handle the parsing duties and simply listens to the SAX events
 * to construct a document. Details which SAX does not provide, such as
 * whitespace outside the root element, are not represented in the JDOM
 * document. Information about SAX can be found at <a
 * href="http://www.saxproject.org">http://www.saxproject.org</a>.
 * <p>
 * Known issues: Relative paths for a {@link DocType} or {@link EntityRef} may
 * be converted by the SAX parser into absolute paths.
 *
 * @version $Revision: 1.93 $, $Date: 2009/07/23 06:26:26 $
 * @author  Jason Hunter
 * @author  Brett McLaughlin
 * @author  Dan Schaffer
 * @author  Philip Nelson
 * @author  Alex Rosen
 */
public class SAXBuilder {

    /**
     * Default parser class to use. This is used when no other parser
     * is given and JAXP isn't available.
     */
    private static final String DEFAULT_SAX_DRIVER =
        "org.apache.xerces.parsers.SAXParser";

    /** Whether validation should occur */
    private boolean validate;

    /** Whether expansion of entities should occur */
    private boolean expand = true;

    /** Adapter class to use */
    private String saxDriverClass;

    /** ErrorHandler class to use */
    private ErrorHandler saxErrorHandler = null;

    /** EntityResolver class to use */
    private EntityResolver saxEntityResolver = null;

    /** DTDHandler class to use */
    private DTDHandler saxDTDHandler = null;

    /** XMLFilter instance to use */
    private XMLFilter saxXMLFilter = null;

    /** The factory for creating new JDOM objects */
    private JDOMFactory factory = new DefaultJDOMFactory();

    /** Whether to ignore ignorable whitespace */
    private boolean ignoringWhite = false;

    /** Whether to ignore all whitespace content */
    private boolean ignoringBoundaryWhite = false;

    /** User-specified features to be set on the SAX parser */
    private HashMap features = new HashMap(5);

    /** User-specified properties to be set on the SAX parser */
    private HashMap properties = new HashMap(5);

    /** Whether to use fast parser reconfiguration */
    private boolean fastReconfigure = false;

    /** Whether to try lexical reporting in fast parser reconfiguration */
    private boolean skipNextLexicalReportingConfig = false;

    /** Whether to to try entity expansion in fast parser reconfiguration */
    private boolean skipNextEntityExpandConfig = false;

    /**
     * Whether parser reuse is allowed.
     * <p>Default: <code>true</code></p>
     */
    private boolean reuseParser = true;

    /** The current SAX parser, if parser reuse has been activated. */
    private XMLReader saxParser = null;

    /**
     * Creates a new SAXBuilder which will attempt to first locate
     * a parser via JAXP, then will try to use a set of default
     * SAX Drivers. The underlying parser will not validate.
     */
    public SAXBuilder() {
        this(false);
    }

    /**
     * Creates a new SAXBuilder which will attempt to first locate
     * a parser via JAXP, then will try to use a set of default
     * SAX Drivers. The underlying parser will validate or not
     * according to the given parameter.
     *
     * @param validate <code>boolean</code> indicating if
     *                 validation should occur.
     */
    public SAXBuilder(boolean validate) {
        this.validate = validate;
    }

    /**
     * This builds a document from the supplied
     * input source.
     *
     * @param in <code>InputSource</code> to read from
     * @return <code>Document</code> resultant Document object
     * @throws JDOMException when errors occur in parsing
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed
     */
    public Document build(InputSource in)
     throws JDOMException, IOException {
        SAXHandler contentHandler = null;

        try {
            // Create and configure the content handler.
            contentHandler = createContentHandler();
            configureContentHandler(contentHandler);

            XMLReader parser = this.saxParser;
            if (parser == null) {
                // Create and configure the parser.
                parser = createParser();

                // Install optional filter
                if (saxXMLFilter != null) {
                    // Connect filter chain to parser
                    XMLFilter root = saxXMLFilter;
                    while (root.getParent() instanceof XMLFilter) {
                        root = (XMLFilter)root.getParent();
                    }
                    root.setParent(parser);

                    // Read from filter
                    parser = saxXMLFilter;
                }

                // Configure parser
                configureParser(parser, contentHandler);

                if (reuseParser) {
                    this.saxParser = parser;
                }
            }
            else {
                // Reset content handler as SAXHandler instances cannot
                // be reused
                configureParser(parser, contentHandler);
            }

            // Parse the document.
            parser.parse(in);

            return contentHandler.getDocument();
        }
        catch (SAXParseException e) {
            Document doc = contentHandler.getDocument();
            if (doc.hasRootElement() == false) {
                doc = null;
            }

            String systemId = e.getSystemId();
            if (systemId != null) {
                throw new JDOMParseException("Error on line " +
                    e.getLineNumber() + " of document " + systemId, e, doc);
            } else {
                throw new JDOMParseException("Error on line " +
                    e.getLineNumber(), e, doc);
            }
        }
        catch (SAXException e) {
            throw new JDOMParseException("Error in building: " +
                e.getMessage(), e, contentHandler.getDocument());
        }
        finally {
            // Explicitly nullify the handler to encourage GC
            // It's a stack var so this shouldn't be necessary, but it
            // seems to help on some JVMs
            contentHandler = null;
        }
    }

    /**
     * This creates the SAXHandler that will be used to build the Document.
     *
     * @return <code>SAXHandler</code> - resultant SAXHandler object.
     */
    protected SAXHandler createContentHandler() {
        SAXHandler contentHandler = new SAXHandler(factory);
        return contentHandler;
    }

    /**
     * This configures the SAXHandler that will be used to build the Document.
     * <p>
     * The default implementation simply passes through some configuration
     * settings that were set on the SAXBuilder: setExpandEntities() and
     * setIgnoringElementContentWhitespace().
     * </p>
     * @param contentHandler The SAXHandler to configure
     */
    protected void configureContentHandler(SAXHandler contentHandler) {
        // Setup pass through behavior
        contentHandler.setExpandEntities(expand);
        contentHandler.setIgnoringElementContentWhitespace(ignoringWhite);
        contentHandler.setIgnoringBoundaryWhitespace(ignoringBoundaryWhite);
    }

    /**
     * This creates the XMLReader to be used for reading the XML document.
     * <p>
     * The default behavior is to (1) use the saxDriverClass, if it has been
     * set, (2) try to obtain a parser from JAXP, if it is available, and
     * (3) if all else fails, use a hard-coded default parser (currently
     * the Xerces parser). Subclasses may override this method to determine
     * the parser to use in a different way.
     * </p>
     *
     * @return <code>XMLReader</code> - resultant XMLReader object.
     * @throws org.jdom.JDOMException
     */
    protected XMLReader createParser() throws JDOMException {
        XMLReader parser = null;
        if (saxDriverClass != null) {
            // The user knows that they want to use a particular class
            try {
                parser = XMLReaderFactory.createXMLReader(saxDriverClass);

                // Configure parser
                setFeaturesAndProperties(parser, true);
            }
            catch (SAXException e) {
              throw new JDOMException("Could not load " + saxDriverClass, e);
            }
        } else {
            // Try using JAXP...
            // Note we need JAXP 1.1, and if JAXP 1.0 is all that's
            // available then the getXMLReader call fails and we skip
            // to the hard coded default parser
            try {
                // Get factory class and method.
                Class factoryClass =
                    Class.forName("org.jdom.input.JAXPParserFactory");

                Method createParser =
                    factoryClass.getMethod("createParser",
                        new Class[] { boolean.class, Map.class, Map.class });

                // Create SAX parser.
                parser = (XMLReader)createParser.invoke(null,
                                new Object[] { validate ? Boolean.TRUE : Boolean.FALSE,
                                               features, properties });

                // Configure parser
                setFeaturesAndProperties(parser, false);
            }
            catch (JDOMException e) {
                throw e;
            }
            catch (NoClassDefFoundError e) {
                // The class loader failed to resolve the dependencies
                // of org.jdom.input.JAXPParserFactory. This probably means
                // that no JAXP parser is present in its class path.
                // => Ignore and try allocating default SAX parser instance.
            }
            catch (Exception e) {
                // Ignore and try allocating default SAX parser instance.
            }
        }

        // Check to see if we got a parser yet, if not, try to use a
        // hard coded default
        if (parser == null) {
            try {
                parser = XMLReaderFactory.createXMLReader(DEFAULT_SAX_DRIVER);
                // System.out.println("using default " + DEFAULT_SAX_DRIVER);
                saxDriverClass = parser.getClass().getName();

                // Configure parser
                setFeaturesAndProperties(parser, true);
            }
            catch (SAXException e) {
                throw new JDOMException("Could not load default SAX parser: "
                  + DEFAULT_SAX_DRIVER, e);
            }
        }

        return parser;
    }

    /**
     * This configures the XMLReader to be used for reading the XML document.
     * <p>
     * The default implementation sets various options on the given XMLReader,
     *  such as validation, DTD resolution, entity handlers, etc., according
     *  to the options that were set (e.g. via <code>setEntityResolver</code>)
     *  and set various SAX properties and features that are required for JDOM
     *  internals. These features may change in future releases, so change this
     *  behavior at your own risk.
     * </p>
     * @param parser
     * @param contentHandler
     * @throws org.jdom.JDOMException
     */
    protected void configureParser(XMLReader parser, SAXHandler contentHandler)
        throws JDOMException {

        // Setup SAX handlers.

        parser.setContentHandler(contentHandler);

        if (saxEntityResolver != null) {
            parser.setEntityResolver(saxEntityResolver);
        }

        if (saxDTDHandler != null) {
            parser.setDTDHandler(saxDTDHandler);
        } else {
            parser.setDTDHandler(contentHandler);
        }

        if (saxErrorHandler != null) {
             parser.setErrorHandler(saxErrorHandler);
        } else {
             parser.setErrorHandler(new BuilderErrorHandler());
        }

        // If fastReconfigure is enabled and we failed in the previous attempt
        // in configuring lexical reporting, then we skip this step.  This
        // saves the work of repeated exception handling on each parse.
        if (!skipNextLexicalReportingConfig) {
            boolean success = false;

            try {
                parser.setProperty("http://xml.org/sax/handlers/LexicalHandler",
                                   contentHandler);
                success = true;
            } catch (SAXNotSupportedException e) {
                // No lexical reporting available
            } catch (SAXNotRecognizedException e) {
                // No lexical reporting available
            }

            // Some parsers use alternate property for lexical handling (grr...)
            if (!success) {
                try {
                    parser.setProperty("http://xml.org/sax/properties/lexical-handler",
                                       contentHandler);
                    success = true;
                } catch (SAXNotSupportedException e) {
                    // No lexical reporting available
                } catch (SAXNotRecognizedException e) {
                    // No lexical reporting available
                }
            }

            // If unable to configure this property and fastReconfigure is
            // enabled, then setup to avoid this code path entirely next time.
            if (!success && fastReconfigure) {
                skipNextLexicalReportingConfig = true;
            }
        }

        // If fastReconfigure is enabled and we failed in the previous attempt
        // in configuring entity expansion, then skip this step.  This
        // saves the work of repeated exception handling on each parse.
        if (!skipNextEntityExpandConfig) {
            boolean success = false;

            // Try setting the DeclHandler if entity expansion is off
            if (!expand) {
                try {
                    parser.setProperty("http://xml.org/sax/properties/declaration-handler",
                                       contentHandler);
                    success = true;
                } catch (SAXNotSupportedException e) {
                    // No lexical reporting available
                } catch (SAXNotRecognizedException e) {
                    // No lexical reporting available
                }
            }

            /* If unable to configure this property and fastReconfigure is
             * enabled, then setup to avoid this code path entirely next time.
             */
            if (!success && fastReconfigure) {
                skipNextEntityExpandConfig = true;
            }
        }
    }

    private void setFeaturesAndProperties(XMLReader parser,
                                          boolean coreFeatures)
                                                        throws JDOMException {
        // Set any user-specified features on the parser.
        Iterator iter = features.keySet().iterator();
        while (iter.hasNext()) {
            String  name  = (String)iter.next();
            Boolean value = (Boolean)features.get(name);
            internalSetFeature(parser, name, value.booleanValue(), name);
        }

        // Set any user-specified properties on the parser.
        iter = properties.keySet().iterator();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            internalSetProperty(parser, name, properties.get(name), name);
        }

        if (coreFeatures) {
            // Set validation.
            try {
                internalSetFeature(parser,
                        "http://xml.org/sax/features/validation",
                        validate, "Validation");
            } catch (JDOMException e) {
                // If validation is not supported, and the user is requesting
                // that we don't validate, that's fine - don't throw an
                // exception.
                if (validate)
                    throw e;
            }

            // Setup some namespace features.
            internalSetFeature(parser,
                        "http://xml.org/sax/features/namespaces",
                        true, "Namespaces");
            internalSetFeature(parser,
                        "http://xml.org/sax/features/namespace-prefixes",
                        true, "Namespace prefixes");
        }

        // Set entity expansion
        // Note SAXHandler can work regardless of how this is set, but when
        // entity expansion it's worth it to try to tell the parser not to
        // even bother with external general entities.
        // Apparently no parsers yet support this feature.
        // XXX It might make sense to setEntityResolver() with a resolver
        // that simply ignores external general entities
        try {
            if (parser.getFeature("http://xml.org/sax/features/external-general-entities") != expand) {
                parser.setFeature("http://xml.org/sax/features/external-general-entities", expand);
            }
        }
        catch (SAXNotRecognizedException e) { /* Ignore... */ }
        catch (SAXNotSupportedException  e) { /* Ignore... */ }
    }

    /**
     * Tries to set a feature on the parser. If the feature cannot be set,
     * throws a JDOMException describing the problem.
     */
    private void internalSetFeature(XMLReader parser, String feature,
                    boolean value, String displayName) throws JDOMException {
        try {
            parser.setFeature(feature, value);
        } catch (SAXNotSupportedException e) {
            throw new JDOMException(
                displayName + " feature not supported for SAX driver " + parser.getClass().getName());
        } catch (SAXNotRecognizedException e) {
            throw new JDOMException(
                displayName + " feature not recognized for SAX driver " + parser.getClass().getName());
        }
    }

    /**
     * <p>
     * Tries to set a property on the parser. If the property cannot be set,
     * throws a JDOMException describing the problem.
     * </p>
     */
    private void internalSetProperty(XMLReader parser, String property,
                    Object value, String displayName) throws JDOMException {
        try {
            parser.setProperty(property, value);
        } catch (SAXNotSupportedException e) {
            throw new JDOMException(
                displayName + " property not supported for SAX driver " + parser.getClass().getName());
        } catch (SAXNotRecognizedException e) {
            throw new JDOMException(
                displayName + " property not recognized for SAX driver " + parser.getClass().getName());
        }
    }

    /**
     * <p>
     * This builds a document from the supplied
     *   input stream.
     * </p>
     *
     * @param in <code>InputStream</code> to read from
     * @return <code>Document</code> resultant Document object
     * @throws JDOMException when errors occur in parsing
     * @throws IOException when an I/O error prevents a document
     *         from being fully parsed.
     */
    public Document build(InputStream in)
     throws JDOMException, IOException {
        return build(new InputSource(in));
    }

//    /**
//     * Imitation of File.toURL(), a JDK 1.2 method, reimplemented
//     * here to work with JDK 1.1.
//     *
//     * @see java.io.File
//     *
//     * @param f the file to convert
//     * @return the file path converted to a file: URL
//     */
//    protected URL fileToURL(File f) throws MalformedURLException {
//        String path = f.getAbsolutePath();
//        if (File.separatorChar != '/') {
//            path = path.replace(File.separatorChar, '/');
//        }
//        if (!path.startsWith("/")) {
//            path = "/" + path;
//        }
//        if (!path.endsWith("/") && f.isDirectory()) {
//            path = path + "/";
//        }
//        return new URL("file", "", path);
//    }
}
