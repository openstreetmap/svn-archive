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

    private static ApiKeyAdapter apiKeyAdapter = new DefaultApiKeyAdapter();
    private static BrowserAdapter browserAdapter = new DefaultBrowserAdapter();
    private static ImageAdapter imageAdapter = new DefaultImageAdapter();
    private static TranslationAdapter translationAdapter = new DefaultTranslationAdapter();
    private static LoggingAdapter loggingAdapter = new DefaultLoggingAdapter();
    private static SettingsAdapter settingsAdapter = new DefaultSettingsAdapter();

    private FeatureAdapter() {
        // private constructor for utility classes
    }

    /**
     * Provider of confidential API keys.
     */
    @FunctionalInterface
    public interface ApiKeyAdapter {
        /**
         * Retrieves the API key for the given imagery id.
         * @param imageryId imagery id
         * @return the API key for the given imagery id
         * @throws IOException in case of I/O error
         */
        String retrieveApiKey(String imageryId) throws IOException;
    }

    /**
     * Link browser.
     */
    @FunctionalInterface
    public interface BrowserAdapter {
        /**
         * Browses to a given link.
         * @param url link
         */
        void openLink(String url);
    }

    /**
     * Translation support.
     */
    public interface TranslationAdapter {
        /**
         * Translates some text for the current locale.
         * <br>
         * For example, <code>tr("JMapViewer''s default value is ''{0}''.", val)</code>.
         * <br>
         * @param text the text to translate.
         * Must be a string literal. (No constants or local vars.)
         * Can be broken over multiple lines.
         * An apostrophe ' must be quoted by another apostrophe.
         * @param objects the parameters for the string.
         * Mark occurrences in {@code text} with <code>{0}</code>, <code>{1}</code>, ...
         * @return the translated string.
         */
        String tr(String text, Object... objects);
        // TODO: more i18n functions
    }

    /**
     * Logging support.
     */
    @FunctionalInterface
    public interface LoggingAdapter {
        /**
         * Retrieves a logger for the given name.
         * @param name logger name
         * @return logger for the given name
         */
        Logger getLogger(String name);
    }

    /**
     * Image provider.
     */
    @FunctionalInterface
    public interface ImageAdapter {
        /**
         * Returns a <code>BufferedImage</code> as the result of decoding a supplied <code>URL</code>.
         *
         * @param input a <code>URL</code> to read from.
         * @param readMetadata if {@code true}, makes sure to read image metadata to detect transparency color for non translucent images,
         * if any.
         * Always considered {@code true} if {@code enforceTransparency} is also {@code true}
         * @param enforceTransparency if {@code true}, makes sure to read image metadata and, if the image does not
         * provide an alpha channel but defines a {@code TransparentColor} metadata node, that the resulting image
         * has a transparency set to {@code TRANSLUCENT} and uses the correct transparent color.
         *
         * @return a <code>BufferedImage</code> containing the decoded contents of the input, or <code>null</code>.
         *
         * @throws IllegalArgumentException if <code>input</code> is <code>null</code>.
         * @throws IOException if an error occurs during reading.
         */
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

    /**
     * Registers API key adapter.
     * @param apiKeyAdapter API key adapter
     */
    public static void registerApiKeyAdapter(ApiKeyAdapter apiKeyAdapter) {
        FeatureAdapter.apiKeyAdapter = Objects.requireNonNull(apiKeyAdapter);
    }

    /**
     * Registers browser adapter.
     * @param browserAdapter browser adapter
     */
    public static void registerBrowserAdapter(BrowserAdapter browserAdapter) {
        FeatureAdapter.browserAdapter = Objects.requireNonNull(browserAdapter);
    }

    /**
     * Registers image adapter.
     * @param imageAdapter image adapter
     */
    public static void registerImageAdapter(ImageAdapter imageAdapter) {
        FeatureAdapter.imageAdapter = Objects.requireNonNull(imageAdapter);
    }

    /**
     * Registers translation adapter.
     * @param translationAdapter translation adapter
     */
    public static void registerTranslationAdapter(TranslationAdapter translationAdapter) {
        FeatureAdapter.translationAdapter = Objects.requireNonNull(translationAdapter);
    }

    /**
     * Registers logging adapter.
     * @param loggingAdapter logging adapter
     */
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

    /**
     * Retrieves the API key for the given imagery id using the current {@link ApiKeyAdapter}.
     * @param imageryId imagery id
     * @return the API key for the given imagery id
     * @throws IOException in case of I/O error
     */
    public static String retrieveApiKey(String imageryId) throws IOException {
        return apiKeyAdapter.retrieveApiKey(imageryId);
    }

    /**
     * Opens a link using the current {@link BrowserAdapter}.
     * @param url link to open
     */
    public static void openLink(String url) {
        browserAdapter.openLink(url);
    }

    /**
     * Reads an image using the current {@link ImageAdapter}.
     * @param url image URL to read
     * @return a <code>BufferedImage</code> containing the decoded contents of the input, or <code>null</code>.
     * @throws IOException if an error occurs during reading.
     */
    public static BufferedImage readImage(URL url) throws IOException {
        return imageAdapter.read(url, false, false);
    }

    /**
     * Translates a text using the current {@link TranslationAdapter}.
     * @param text the text to translate.
     * Must be a string literal. (No constants or local vars.)
     * Can be broken over multiple lines.
     * An apostrophe ' must be quoted by another apostrophe.
     * @param objects the parameters for the string.
     * Mark occurrences in {@code text} with <code>{0}</code>, <code>{1}</code>, ...
     * @return the translated string.
     */
    public static String tr(String text, Object... objects) {
        return translationAdapter.tr(text, objects);
    }

    /**
     * Returns a logger for the given name using the current {@link LoggingAdapter}.
     * @param name logger name
     * @return logger for the given name
     */
    public static Logger getLogger(String name) {
        return loggingAdapter.getLogger(name);
    }

    /**
     * Returns a logger for the given class using the current {@link LoggingAdapter}.
     * @param klass logger class
     * @return logger for the given class
     */
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

    /**
     * Default API key support that relies on system property named {@code <imageryId>.api-key}.
     */
    public static class DefaultApiKeyAdapter implements ApiKeyAdapter {
        @Override
        public String retrieveApiKey(String imageryId) {
            return System.getProperty(imageryId + ".api-key");
        }
    }

    /**
     * Default browser support that relies on Java Desktop API.
     */
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

    /**
     * Default image support that relies on Java Image IO API.
     */
    public static class DefaultImageAdapter implements ImageAdapter {
        @Override
        public BufferedImage read(URL input, boolean readMetadata, boolean enforceTransparency) throws IOException {
            return ImageIO.read(input);
        }
    }

    /**
     * Default "translation" support that do not really translates strings, but only takes care of formatting arguments.
     */
    public static class DefaultTranslationAdapter implements TranslationAdapter {
        @Override
        public String tr(String text, Object... objects) {
            return MessageFormat.format(text, objects);
        }
    }

    /**
     * Default logging support that relies on Java Logging API.
     */
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
