/**
 *
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author cdaller
 *
 */
public class PluginResource {
    private static final int BUFFER_SIZE = 16384;
    private URL resourceUrl;
    private String target;
    private String errorMessage;
    private Exception errorException;

    /**
     * @return the resourceUrl
     */
    public URL getResourceUrl() {
        return this.resourceUrl;
    }
    /**
     * @param resourceUrl the resourceUrl to set
     */
    public void setResourceUrl(URL resourceUrl) {
        this.resourceUrl = resourceUrl;
    }
    /**
     * @return the target
     */
    public String getTarget() {
        return this.target;
    }
    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }
    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    /**
     * @return the errorException
     */
    public Exception getErrorException() {
        return this.errorException;
    }
    /**
     * @param errorException the errorException to set
     */
    public void setErrorException(Exception errorException) {
        this.errorException = errorException;
    }
    /**
     * Installs the resource into to the target location.
     * @throws IOException if the resource could not be read or written.
     */
    public void install() {
       File targetFile = new File(target);
       if(targetFile.isDirectory()
          || targetFile.getAbsolutePath().endsWith("/")
          || targetFile.getAbsolutePath().endsWith("\\")) {
           targetFile = new File(targetFile, resourceUrl.getFile());
       }
       File parentDir = targetFile.getParentFile();
       if(!parentDir.exists() && !parentDir.mkdirs()) {
           errorMessage = "Could not create the target directory: " + parentDir.getAbsolutePath();
           return;
       }

       // copy resource to local filesystem:
       System.out.println("Install " + resourceUrl + " to " + targetFile);
       byte[] buffer = new byte[BUFFER_SIZE];
       int read;
       InputStream in = null;
       OutputStream out = null;
       try {
           in = resourceUrl.openConnection().getInputStream();
           out = new BufferedOutputStream(new FileOutputStream(targetFile));
           while((read = in.read(buffer)) >= 0) {
               out.write(buffer, 0, read);
           }
       } catch(IOException e) {
           errorMessage = e.getMessage();
           errorException = e;
       } finally {
           try {
             if(in != null) in.close();
           } catch(IOException ignore) {}
           try {
               if(out != null) out.close();
             } catch(IOException ignore) {}
       }

    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getSimpleName() + "[url=" + resourceUrl + ", target=" + target + "]";
    }

}
