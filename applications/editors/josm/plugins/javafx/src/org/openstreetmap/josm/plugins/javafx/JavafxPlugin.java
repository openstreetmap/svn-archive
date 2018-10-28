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
import org.openstreetmap.josm.tools.PlatformManager;

/**
 * OpenJFX plugin brings OpenJFX (JavaFX) to other plugins.
 */
public class JavafxPlugin extends Plugin {

    /**
     * Constructs a new {@code OpenJfxPlugin}.
     * @param info plugin info
     */
    public JavafxPlugin(PluginInformation info) {
        super(info);
        AudioPlayer.setSoundPlayerClass(JavaFxMediaPlayer.class);
        String ext = null;
        if (PlatformManager.isPlatformWindows()) {
            ext = ".dll";
        } else if (PlatformManager.isPlatformUnixoid()) {
            ext = ".so";
        } else if (PlatformManager.isPlatformOsx()) {
            ext = ".dylib";
        }
        extractNativeLibs(ext);
        loadNativeLibs(ext);
    }

    private static void extractNativeLibs(String ext) {
        CodeSource src = JavafxPlugin.class.getProtectionDomain().getCodeSource();
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
            Logging.error("Unable to locate openjfx jar file");
        }
    }

    private static Path getNativeDir() throws IOException {
        return Files.createDirectories(new File(Preferences.main().getPluginsDirectory(), "openjfx").toPath());
    }

    private static class LibVisitor extends SimpleFileVisitor<Path> {
        private final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        private final String ext;

        public LibVisitor(String ext) {
            this.ext = Objects.requireNonNull(ext);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (ccl instanceof DynamicURLClassLoader) {
                if (file.endsWith(ext)) {
                    Logging.debug("Loading " + file);
                    System.load(file.toAbsolutePath().toString());
                } else if (file.endsWith(".jar")) {
                    Logging.debug("Loading " + file);
                    ((DynamicURLClassLoader) ccl).addURL(file.toUri().toURL());
                }
            } else {
                Logging.error("Unexpected context class loader: " + ccl);
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private void loadNativeLibs(String ext) {
        try {
            Files.walkFileTree(getNativeDir(), new LibVisitor(ext));
        } catch (IOException e) {
            Logging.error(e);
        }
    }
}
