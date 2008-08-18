/**
 * 
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.plugins.PluginInformation;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author cdaller
 *
 */
public class SiteDescription {
    public static final String SITE_FILE_NAME = "josm-site.xml";
    private String name;
    private URL url;
    List<PluginDescription>plugins = new ArrayList<PluginDescription>();
    
    /**
     * Default Constructor
     */
    public SiteDescription() {
    }

    /**
     * @param url
     * @throws MalformedURLException
     */
    public SiteDescription(String url) throws MalformedURLException {
        if(!url.endsWith("/")) {
            url = url + "/";
        }
        this.url = new URL(url);
    }
    
    /**
     * @param name
     * @param url
     */
    public SiteDescription(String name, String url) throws MalformedURLException {
        this(url);
        this.name = name;
    }
    

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    
    /**
     * Load the site description from the url.
     * @throws IOException 
     */
    public void loadFromUrl() throws IOException {
        plugins.clear();
        URL xmlUrl = new URL(url, SITE_FILE_NAME);
        System.out.println("loading from url " + xmlUrl);
        URLConnection connection = xmlUrl.openConnection();
        // <FIXXME date="20.06.2007" author="cdaller">
        // TODO check and remember modified since date to compare and not load sites that did not change
        // connection.getIfModifiedSince()
        // </FIXXME> 
        
        Reader in = new InputStreamReader(connection.getInputStream());
        SiteDescriptionParser parser = new SiteDescriptionParser(this);
        try {
//            VariableHelper varHelper = new VariableHelper();
//            varHelper.add("josm.user.dir", "${user.home}/.josm");
//            parser.setVariableHelper(varHelper);
            parser.setVariableHelper(PluginHelper.getInstance().getVariableHelper());
            SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(in), parser);
            //System.out.println("site describes plugins: " + plugins);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e1) {
            e1.printStackTrace(); // broken SAXException chaining
        }
        finally {
            in.close();
        }

        // check if the plugins are already installed:
        PluginHelper helper = PluginHelper.getInstance();
        PluginInformation info;
        for(PluginDescription plugin : plugins) {
            info = helper.getPluginInfo(plugin.getId());
            if(info != null) {
                if(info.version == null) {
                    plugin.setInstalledVersion("?");                    
                } else {
                    plugin.setInstalledVersion(info.version);
                }
            }
        }
    }
    
    /**
     * @return the plugins
     */
    public List<PluginDescription> getPlugins() {
        return this.plugins;
    }

    /**
     * @param pluginDescription
     */
    public void addPlugin(PluginDescription pluginDescription) {
        plugins.add(pluginDescription);
    }

    
    /**
     * @return the url
     */
    public URL getUrl() {
        return this.url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Returns the name if not <code>null</code> or the url (must not be <code>null</code>).
     * @return the name or the url.
     */
    public String getLabelName() {
        if(name == null) {
            return url.toString();
        } else {
            return name;
        }
        
    }
    
    /**
     * Used by ListCellRenderer, so not only a debug method!
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getLabelName();
    }
    
    public static void main(String[] args) {
        try {
            new SiteDescription("file:site/").loadFromUrl();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
