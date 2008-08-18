/**
 * 
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author cdaller
 *
 */
public class SiteDescriptionParser extends DefaultHandler {
    private SiteDescription siteDescription;
    private PluginDescription pluginDescription;
    private StringBuilder characters;
    private VariableHelper variableHelper;
    
    /**
     * Constructor using a new SiteDescription object.
     */
    public SiteDescriptionParser() {
        this(new SiteDescription());
    }
    /**
     * Constructor using a description object to fill.
     * @param siteDescription the object to fill.
     * @param in the reader to fill the description with. 
     */
    public SiteDescriptionParser(SiteDescription siteDescription) {
        this.siteDescription = siteDescription;
    }
    
    /**
     * Returns the site description object.
     * @return the site description object.
     */
    public SiteDescription getSiteDescription() {
        return siteDescription;
    }
    /**
     * @return the variableHelper
     */
    public VariableHelper getVariableHelper() {
        return this.variableHelper;
    }
    /**
     * @param variableHelper the variableHelper to set
     */
    public void setVariableHelper(VariableHelper variableHelper) {
        this.variableHelper = variableHelper;
    }
    /* (non-Javadoc)
     * @see uk.co.wilson.xml.MinML2#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        if("site-name".equals(qName)) {
                siteDescription.setName(qName);
        } else if("plugin".equals(qName)) {
            pluginDescription = new PluginDescription();
            pluginDescription.setId(atts.getValue("id"));
            pluginDescription.setVersion(atts.getValue("version"));
        } else if("resource".equals(qName)) {
            PluginResource resource = new PluginResource();
            try {
                resource.setResourceUrl(new URL(atts.getValue("src")));
                String target = atts.getValue("target");
                if(variableHelper != null) {
                    target = variableHelper.replaceVariables(target);
                }
                resource.setTarget(target);
                if(pluginDescription != null) {
                    pluginDescription.addPluginResource(resource);
                } else {
                    throw new SAXException("Resource was defined outside 'plugin' element!");
                }
            } catch (MalformedURLException e) {
                throw new SAXException(e);
            }
        } else if("site".equals(qName)) {
            if(atts.getIndex("ref") >= 0) {
                String urlString = atts.getValue("ref");
                System.out.println("Handling referenced site " + urlString);
                try {
                    SiteDescription subsite = new SiteDescription(urlString);
                    subsite.loadFromUrl();
                    // add the subsite's plugins to this site:
                    for(PluginDescription desc : subsite.getPlugins()) {
                        siteDescription.addPlugin(desc);
                        System.out.println("adding plugin " + desc.getName());
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            } else if(!"1.0".equals(atts.getValue("version"))) {
                throw new SAXException("Unknown version of site description (must be '1.0')!");
            }
        } else if("site-info".equals(qName)) {
        } else if("site-url".equals(qName)) {
        } else if("site-name".equals(qName)) {
        } else if("plugins".equals(qName)) {
        } else if("name".equals(qName)) {
        } else if("description".equals(qName)) {
        } else if("resources".equals(qName)) {
        } else if("sites".equals(qName)) {
        } else {
            System.err.println("unknown tag: " + qName);
        }
    }
    
    /** 
     * Read characters for description.
     */
    @Override public void characters(char[] data, int start, int length) throws org.xml.sax.SAXException {
        if (characters == null) {
            characters = new StringBuilder();
        }
        characters.append(data, start, length);
    }
    
    /* (non-Javadoc)
     * @see uk.co.wilson.xml.MinML2#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
       if("site-name".equals(qName)) {
           siteDescription.setName(clearCharacters());
       } else if("site-url".equals(qName)) {
               try {
                siteDescription.setUrl(new URL(clearCharacters()));
            } catch (MalformedURLException e) {
                throw new SAXException(e);
            }
       } else if("name".equals(qName)) {
           if(pluginDescription != null) {
               pluginDescription.setName(clearCharacters());
           } else {
               throw new SAXException("'" + qName  + "' element is not inside 'plugin' element!");
           }
       } else if("description".equals(qName)) {
           if(pluginDescription != null) {
               pluginDescription.setDescription(clearCharacters());
           } else {
               throw new SAXException("'" + qName  + "' element is not inside 'plugin' element!");
           }
       } else if("plugin".equals(qName)) {
           siteDescription.addPlugin(pluginDescription);
           pluginDescription = null;
       }
    }
    
    /**
     * Clears the characters and returns the its previous content.
     * @return
     */
    private String clearCharacters() {
        String chars = characters.toString();
        characters = new StringBuilder();
        return chars;
    }
    
    

}
