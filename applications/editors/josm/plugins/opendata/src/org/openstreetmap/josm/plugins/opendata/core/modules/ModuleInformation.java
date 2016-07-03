// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.modules;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.swing.ImageIcon;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.opendata.OdPlugin;
import org.openstreetmap.josm.plugins.opendata.core.OdConstants;
import org.openstreetmap.josm.plugins.opendata.core.util.OdUtils;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.ImageProvider.ImageSizes;
import org.openstreetmap.josm.tools.LanguageInfo;

/**
 * Encapsulate general information about a module. This information is available
 * without the need of loading any class from the module jar file.
 */
public class ModuleInformation {
    public File file = null;
    public String name = null;
    public String className = null;
    public String link = null;
    public String description = null;
    public String author = null;
    public String version = null;
    public String localversion = null;
    public String downloadlink = null;
    public String iconPath;
    public ImageIcon icon;
    public List<URL> libraries = new LinkedList<>();
    public final Map<String, String> attr = new TreeMap<>();

    /**
     * Creates a module information object by reading the module information from
     * the manifest in the module jar.
     *
     * The module name is derived from the file name.
     *
     * @param file the module jar file
     * @throws ModuleException if reading the manifest fails
     */
    /*public ModuleInformation(File file) throws ModuleException {
        this(file, file.getName().substring(0, file.getName().length()-4));
    }*/

    /**
     * Creates a module information object for the module with name {@code name}.
     * Information about the module is extracted from the manifest file in the module jar
     * {@code file}.
     * @param file the module jar
     * @param name the module name
     * @throws ModuleException thrown if reading the manifest file fails
     */
    public ModuleInformation(File file, String name) throws ModuleException {
        this.name = name;
        this.file = file;
        try (
            FileInputStream fis = new FileInputStream(file);
            JarInputStream jar = new JarInputStream(fis);
        ) {
            Manifest manifest = jar.getManifest();
            if (manifest == null)
                throw new ModuleException(name, tr("The module file ''{0}'' does not include a Manifest.", file.toString()));
            scanManifest(manifest);
            libraries.add(0, fileToURL(file));
        } catch (IOException e) {
            throw new ModuleException(name, e);
        }
    }

    /**
     * Creates a module information object by reading module information in Manifest format
     * from the input stream {@code manifestStream}.
     *
     * @param manifestStream the stream to read the manifest from
     * @param name the module name
     * @param url the download URL for the module
     * @throws ModuleException thrown if the module information can't be read from the input stream
     */
    public ModuleInformation(InputStream manifestStream, String name, String url) throws ModuleException {
        this.name = name;
        try {
            Manifest manifest = new Manifest();
            manifest.read(manifestStream);
            if (url != null) {
                downloadlink = url;
            }
            scanManifest(manifest);
        } catch (IOException e) {
            throw new ModuleException(name, e);
        }
    }

    /**
     * Updates the module information of this module information object with the
     * module information in a module information object retrieved from a module
     * update site.
     *
     * @param other the module information object retrieved from the update site
     */
    public void updateFromModuleSite(ModuleInformation other) {
        this.className = other.className;
        this.link = other.link;
        this.description = other.description;
        this.author = other.author;
        this.version = other.version;
        this.downloadlink = other.downloadlink;
        this.icon = other.icon;
        this.iconPath = other.iconPath;
        this.libraries = other.libraries;
        this.attr.clear();
        this.attr.putAll(other.attr);
    }

    private static ImageIcon extractIcon(String iconPath, File jarFile, boolean suppressWarnings) {
        return new ImageProvider(iconPath).setArchive(jarFile).setMaxWidth(24).setMaxHeight(24).setOptional(true)
                .setSuppressWarnings(suppressWarnings).get();
    }

    private void scanManifest(Manifest manifest) {
        String lang = LanguageInfo.getLanguageCodeManifest();
        Attributes attr = manifest.getMainAttributes();
        className = attr.getValue("Module-Class");
        String s = attr.getValue(lang+"Module-Link");
        if (s == null) {
            s = attr.getValue("Module-Link");
        }
        if (s != null) {
            try {
                @SuppressWarnings("unused")
                URL url = new URL(s);
            } catch (MalformedURLException e) {
                Main.error(tr("Invalid URL ''{0}'' in module {1}", s, name));
                s = null;
            }
        }
        link = s;
        s = attr.getValue(lang+"Module-Description");
        if (s == null) {
            s = attr.getValue("Module-Description");
            if (s != null) {
                s = tr(s);
            }
        }
        description = s;
        version = attr.getValue("Module-Version");
        author = attr.getValue("Author");
        iconPath = attr.getValue("Module-Icon");
        if (iconPath != null && file != null) {
            // extract icon from the module jar file
            icon = extractIcon(iconPath, file, true);
            // if not found, extract icon from the plugin jar file
            if (icon == null) {
                icon = extractIcon(iconPath, OdPlugin.getInstance().getPluginInformation().file, true);
            }
            if (icon == null) {
                Main.error("Unable to load module icon: "+iconPath);
            }
        }

        String classPath = attr.getValue(Attributes.Name.CLASS_PATH);
        if (classPath != null) {
            for (String entry : classPath.split(" ")) {
                File entryFile;
                if (new File(entry).isAbsolute() || file == null) {
                    entryFile = new File(entry);
                } else {
                    entryFile = new File(file.getParent(), entry);
                }

                libraries.add(fileToURL(entryFile));
            }
        }
        for (Object o : attr.keySet()) {
            this.attr.put(o.toString(), attr.getValue(o.toString()));
        }
    }

    /**
     * Replies the description as HTML document, including a link to a web page with
     * more information, provided such a link is available.
     *
     * @return the description as HTML document
     */
    public String getDescriptionAsHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append(description == null ? tr("no description available") : description);
        if (link != null) {
            sb.append(" <a href=\"").append(link).append("\">").append(tr("More info...")).append("</a>");
        }
        if (downloadlink != null && !downloadlink.startsWith(OdConstants.OSM_SITE+"dist/")) {
            sb.append("<p>&nbsp;</p><p>"+tr("<b>Module provided by an external source:</b> {0}", downloadlink)+"</p>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    /**
     * Load and instantiate the module
     *
     * @param the module class
     * @return the instantiated and initialized module
     */
    public Module load(Class<? extends Module> klass) throws ModuleException {
        try {
            return klass.getConstructor(ModuleInformation.class).newInstance(this);
        } catch (Exception t) {
            throw new ModuleException(name, t);
        }
    }

    /**
     * Load the class of the module
     *
     * @param classLoader the class loader to use
     * @return the loaded class
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Module> loadClass(ClassLoader classLoader) throws ModuleException {
        if (className == null)
            return null;
        try {
            return (Class<? extends Module>) Class.forName(className, true, classLoader);
        } catch (Exception t) {
            throw new ModuleException(name, t);
        }
    }

    public static URL fileToURL(File f) {
        try {
            return f.toURI().toURL();
        } catch (MalformedURLException ex) {
            Main.warn(ex.getMessage());
            return null;
        }
    }

    public static Collection<String> getModuleLocations() {
        Collection<String> locations = Main.pref.getAllPossiblePreferenceDirs();
        Collection<String> all = new ArrayList<>(locations.size());
        for (String s : locations) {
            all.add(s+"plugins/opendata/modules");
        }
        return all;
    }

    /**
     * Replies true if the module with the given information is most likely outdated with
     * respect to the referenceVersion.
     *
     * @param referenceVersion the reference version. Can be null if we don't know a
     * reference version
     *
     * @return true, if the module needs to be updated; false, otherweise
     */
    public boolean isUpdateRequired(String referenceVersion) {
        if (this.downloadlink == null) return false;
        if (this.version == null && referenceVersion != null)
            return true;
        if (this.version != null && !this.version.equals(referenceVersion))
            return true;
        return false;
    }

    /**
     * Replies true if this this module should be updated/downloaded because either
     * it is not available locally (its local version is null) or its local version is
     * older than the available version on the server.
     *
     * @return true if the module should be updated
     */
    public boolean isUpdateRequired() {
        if (this.downloadlink == null) return false;
        if (this.localversion == null) return true;
        return isUpdateRequired(this.localversion);
    }

    protected boolean matches(String filter, String value) {
        if (filter == null) return true;
        if (value == null) return false;
        return value.toLowerCase().contains(filter.toLowerCase());
    }

    /**
     * Replies true if either the name, the description, or the version match (case insensitive)
     * one of the words in filter. Replies true if filter is null.
     *
     * @param filter the filter expression
     * @return true if this module info matches with the filter
     */
    public boolean matches(String filter) {
        if (filter == null) return true;
        String[] words = filter.split("\\s+");
        for (String word: words) {
            if (matches(word, name)
                    || matches(word, description)
                    || matches(word, version)
                    || matches(word, localversion))
                return true;
        }
        return false;
    }

    /**
     * Replies the name of the module
     */
    public String getName() {
        return name;
    }

    public ImageIcon getScaledIcon() {
        Dimension dim = ImageSizes.MENU.getImageDimension();
        ImageIcon iconToScale = icon != null ? icon : OdUtils.getImageIcon(OdConstants.ICON_EMPTY_24);
        return new ImageIcon(iconToScale.getImage().getScaledInstance(dim.width, dim.height, Image.SCALE_SMOOTH));
    }
}
