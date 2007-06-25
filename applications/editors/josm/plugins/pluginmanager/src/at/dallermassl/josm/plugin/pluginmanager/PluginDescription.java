/**
 * 
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.plugins.PluginInformation;

/**
 * @author cdaller
 *
 */
public class PluginDescription {
    private boolean install;
    private String id;
    private String version;
    private String installedVersion;
    private String name;
    private String description;
    private List<PluginResource> resources = new ArrayList<PluginResource>();
    
    /**
     * Add a resource to the plugin description.
     * @param resource the resource to add.
     */
    public void addPluginResource(PluginResource resource) {
        resources.add(resource);
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return this.id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }
    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
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
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the resources
     */
    public List<PluginResource> getResources() {
        return this.resources;
    }
    
    /**
     * @return the installedVersion
     */
    public String getInstalledVersion() {
        return this.installedVersion;
    }

    /**
     * @param installedVersion the installedVersion to set
     */
    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

    /**
     * Returns <code>true</code> if the plugin was selected to install.
     * @return <code>true</code> if the plugin was selected to install.
     */
    public boolean isInstall() {
        return this.install;
    }

    /**
     * @param install the install to set
     */
    public void setInstall(boolean install) {
        this.install = install;
    }

    /**
     * Copies all resources from the update site into the target directory.
     */
    public void install() {
        for(PluginResource resource : resources) {
            resource.install();
            if(resource.getErrorMessage() != null) {
                System.err.println("ERROR: " + resource.getErrorMessage());
            }
            if(resource.getErrorException() != null) {
                resource.getErrorException().printStackTrace();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getSimpleName() + "[id=" + id 
                                          + ", name=" + name 
                                          + ", version=" + version 
                                          + ", desc=" + description 
                                          + ", resources=" + resources 
                                          + "]";
    }
}
