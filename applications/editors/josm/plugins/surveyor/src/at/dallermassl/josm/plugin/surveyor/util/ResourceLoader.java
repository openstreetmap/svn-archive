/**
 * 
 */
package at.dallermassl.josm.plugin.surveyor.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author cdaller
 *
 */
public class ResourceLoader {
    
    private ResourceLoader() {
        
    }
    
    /**
     * Returns an inputstream from urls, files and classloaders, depending on the name.
     * @param source the source: if starting with &quot;http://&quot;, &quot;ftp://&quot; or
     * &quot;file://&quot; source is interpreted as an URL. If starting with &quot;resource://&quot;
     * the classloader is used. All other sources are interpreted as filenames.
     * @return the inputstream.
     * @throws IOException if an error occurs on opening the url, or if the file is not found.
     */
    public static InputStream getInputStream(String source) throws IOException {
        InputStream in = null;
        if (source.startsWith("http://") || source.startsWith("ftp://") || source.startsWith("file:")) {
            in = new URL(source).openStream();
        } else if (source.startsWith("resource://")) {
            in = ResourceLoader.class.getResourceAsStream(source.substring("resource:/".length()));
        } else {
            in = new FileInputStream(source);
        }
        System.out.println("stream for resource is " + in);
        return in;
    }

}
