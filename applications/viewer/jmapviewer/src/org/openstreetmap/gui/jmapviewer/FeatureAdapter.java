// License: GPL. For details, see Readme.txt file.
package org.openstreetmap.gui.jmapviewer;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * Feature adapter allows to override JMapViewer behaviours from a client application such as JOSM.
 */
public final class FeatureAdapter {

    private static BrowserAdapter browserAdapter = new DefaultBrowserAdapter();
    private static ImageAdapter imageAdapter = new DefaultImageAdapter();
    private static TranslationAdapter translationAdapter = new DefaultTranslationAdapter();
    private static LoggingAdapter loggingAdapter = new DefaultLoggingAdapter();
    private static SettingsAdapter settingsAdapter = new DefaultSettingsAdapter();

    private FeatureAdapter() {
        // private constructor for utility classes
    }

    public interface BrowserAdapter {
        void openLink(String url);
    }

    public interface TranslationAdapter {
        String tr(String text, Object... objects);
        // TODO: more i18n functions
    }

    public interface LoggingAdapter {
        Logger getLogger(String name);
    }

    public interface ImageAdapter {
        BufferedImage read(URL input, boolean readMetadata, boolean enforceTransparency) throws IOException;
    }

    /**
     * Basic settings system allowing to store/retrieve String key/value pairs.
     */
    public interface SettingsAdapter {
        /**
         * Get settings value for a certain key and provide a default value.
         * @param key the identifier for the setting
         * @param def the default value. For each call of get() with a given key, the
         * default value must be the same. {@code def} may be null.
         * @return the corresponding value if the property has been set before, {@code def} otherwise
         */
        String get(String key, String def);

        /**
         * Set a value for a certain setting.
         * @param key the unique identifier for the setting
         * @param value the value of the setting. Can be null or "" which both removes the key-value entry.
         * @return {@code true}, if something has changed (i.e. value is different than before)
         */
        boolean put(String key, String value);
    }

    public static void registerBrowserAdapter(BrowserAdapter browserAdapter) {
        FeatureAdapter.browserAdapter = Objects.requireNonNull(browserAdapter);
    }

    public static void registerImageAdapter(ImageAdapter imageAdapter) {
        FeatureAdapter.imageAdapter = Objects.requireNonNull(imageAdapter);
    }

    public static void registerTranslationAdapter(TranslationAdapter translationAdapter) {
        FeatureAdapter.translationAdapter = Objects.requireNonNull(translationAdapter);
    }

    public static void registerLoggingAdapter(LoggingAdapter loggingAdapter) {
        FeatureAdapter.loggingAdapter = Objects.requireNonNull(loggingAdapter);
    }

    /**
     * Registers settings adapter.
     * @param settingsAdapter settings adapter, must not be null
     * @throws NullPointerException if settingsAdapter is null
     */
    public static void registerSettingsAdapter(SettingsAdapter settingsAdapter) {
        FeatureAdapter.settingsAdapter = Objects.requireNonNull(settingsAdapter);
    }

    public static void openLink(String url) {
        browserAdapter.openLink(url);
    }

    public static BufferedImage readImage(URL url) throws IOException {
        return imageAdapter.read(url, false, false);
    }

    public static String tr(String text, Object... objects) {
        return translationAdapter.tr(text, objects);
    }

    public static Logger getLogger(String name) {
        return loggingAdapter.getLogger(name);
    }

    public static Logger getLogger(Class<?> klass) {
        return loggingAdapter.getLogger(klass.getSimpleName());
    }

    /**
     * Get settings value for a certain key and provide a default value.
     * @param key the identifier for the setting
     * @param def the default value. For each call of get() with a given key, the
     * default value must be the same. {@code def} may be null.
     * @return the corresponding value if the property has been set before, {@code def} otherwise
     */
    public static String getSetting(String key, String def) {
        return settingsAdapter.get(key, def);
    }

    /**
     * Get settings value for a certain key and provide a default value.
     * @param key the identifier for the setting
     * @param def the default value. For each call of get() with a given key, the
     * default value must be the same. {@code def} may be null.
     * @return the corresponding value if the property has been set before, {@code def} otherwise
     */
    public static int getIntSetting(String key, int def) {
        return Integer.parseInt(settingsAdapter.get(key, Integer.toString(def)));
    }

    /**
     * Set a value for a certain setting.
     * @param key the unique identifier for the setting
     * @param value the value of the setting. Can be null or "" which both removes the key-value entry.
     * @return {@code true}, if something has changed (i.e. value is different than before)
     */
    public static boolean putSetting(String key, String value) {
        return settingsAdapter.put(key, value);
    }

    public static class DefaultBrowserAdapter implements BrowserAdapter {
        @Override
        public void openLink(String url) {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } else {
                getLogger(FeatureAdapter.class).log(Level.SEVERE, tr("Opening link not supported on current platform (''{0}'')", url));
            }
        }
    }

    public static class DefaultImageAdapter implements ImageAdapter {
        @Override
        public BufferedImage read(URL input, boolean readMetadata, boolean enforceTransparency) throws IOException {
            return ImageIO.read(input);
        }
    }

    public static class DefaultTranslationAdapter implements TranslationAdapter {
        @Override
        public String tr(String text, Object... objects) {
            return MessageFormat.format(text, objects);
        }
    }

    public static class DefaultLoggingAdapter implements LoggingAdapter {
        @Override
        public Logger getLogger(String name) {
            return Logger.getLogger(name);
        }
    }

    /**
     * Default settings adapter keeping settings in memory only.
     */
    public static class DefaultSettingsAdapter implements SettingsAdapter {
        private final Map<String, String> settings = new TreeMap<>();

        @Override
        public String get(String key, String def) {
            return settings.getOrDefault(key, def);
        }

        @Override
        public boolean put(String key, String value) {
            return !Objects.equals(value == null || value.isEmpty() ? settings.remove(key) : settings.put(key, value), value);
        }
    }
}
