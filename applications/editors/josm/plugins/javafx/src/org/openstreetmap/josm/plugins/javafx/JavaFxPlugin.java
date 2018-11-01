// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.javafx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.io.audio.AudioPlayer;
import org.openstreetmap.josm.plugins.DynamicURLClassLoader;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.javafx.io.audio.JavaFxMediaPlayer;
import org.openstreetmap.josm.tools.Logging;

/**
 * OpenJFX plugin brings OpenJFX (JavaFX) to other plugins.
 */
abstract class JavaFxPlugin extends Plugin {

    /**
     * Constructs a new {@code OpenJfxPlugin}.
     * @param info plugin info
     * @param ext native libraries extension
     * @param orderedNativeLibraries native librarires that must be loaded in this order
     */
    protected JavaFxPlugin(PluginInformation info, String ext, List<String> orderedNativeLibraries) {
        super(info);
        AudioPlayer.setSoundPlayerClass(JavaFxMediaPlayer.class);
        extractNativeLibs(ext);
        loadNativeLibs(ext, orderedNativeLibraries);
    }

    private static void extractNativeLibs(String ext) {
        CodeSource src = JavaFxPlugin.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            try (ZipFile zf = new ZipFile(Paths.get(src.getLocation().toURI()).toFile(), StandardCharsets.UTF_8)) {
                Path dir = getNativeDir();
                Enumeration<? extends ZipEntry> es = zf.entries();
                while (es.hasMoreElements()) {
                    ZipEntry ze = es.nextElement();
                    String name = ze.getName();
                    if (name.endsWith(ext) || name.endsWith(".jar")) {
                        Path targetPath = dir.resolve(name);
                        File targetFile = targetPath.toFile();
                        if (!targetFile.exists() || targetFile.lastModified() < ze.getTime()) {
                            try (InputStream is = zf.getInputStream(ze)) {
                                Logging.debug("Extracting " + targetPath);
                                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    }
                }
            } catch (IOException | URISyntaxException e) {
                Logging.error(e);
            }
        } else {
            Logging.error("Unable to locate javafx jar file");
        }
    }

    private static Path getNativeDir() throws IOException {
        return Files.createDirectories(new File(Preferences.main().getPluginsDirectory(), "javafx").toPath());
    }

    private static class LibVisitor extends SimpleFileVisitor<Path> {
        private final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        private final String ext;
        private final List<String> orderedNativeLibraries;

        public LibVisitor(String ext, List<String> orderedNativeLibraries) {
            this.ext = Objects.requireNonNull(ext);
            this.orderedNativeLibraries = Objects.requireNonNull(orderedNativeLibraries);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (ccl instanceof DynamicURLClassLoader) {
                String path = file.toAbsolutePath().toString();
                if (path.endsWith(ext) && !orderedNativeLibraries.contains(file.getFileName().toString())) {
                    loadNativeLib(path);
                } else if (path.endsWith(".jar")) {
                    Logging.debug("Loading {0}", path);
                    ((DynamicURLClassLoader) ccl).addURL(file.toUri().toURL());
                }
            } else {
                Logging.error("Unexpected context class loader: " + ccl);
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private static void loadNativeLib(String absolutePath) {
        try {
            Logging.debug("Loading {0}", absolutePath);
            System.load(absolutePath);
        } catch (LinkageError e) {
            Logging.error(e);
        }
    }

    private static void loadNativeLibs(String ext, List<String> orderedNativeLibraries) {
        try {
            Path nativeDir = getNativeDir();
            Files.walkFileTree(nativeDir, new LibVisitor(ext, orderedNativeLibraries));
            for (String lib : orderedNativeLibraries) {
                loadNativeLib(nativeDir.resolve(lib).toAbsolutePath().toString());
            }
        } catch (IOException e) {
            Logging.error(e);
        }
    }
}
