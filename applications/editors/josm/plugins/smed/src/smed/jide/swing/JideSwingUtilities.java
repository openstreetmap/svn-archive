package smed.jide.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.security.AccessControlException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class JideSwingUtilities {

    /**
     * Inserts the mouse listener at the particular index in the listeners' chain.
     *
     * @param component
     * @param l
     * @param index
     */
    public static void insertMouseListener(Component component, MouseListener l, int index) {
        MouseListener[] listeners = component.getMouseListeners();
        for (MouseListener listener : listeners) {
            component.removeMouseListener(listener);
        }
        for (int i = 0; i < listeners.length; i++) {
            MouseListener listener = listeners[i];
            if (index == i) {
                component.addMouseListener(l);
            }
            component.addMouseListener(listener);
        }
        // index is too large, add to the end.
        if (index < 0 || index > listeners.length - 1) {
            component.addMouseListener(l);
        }
    }
    /**
     * This method can be used to fix two JDK bugs. One is to fix the row height is wrong when the first element in the
     * model is null or empty string. The second bug is only on JDK1.4.2 where the vertical scroll bar is shown even all
     * rows are visible. To use it, you just need to override JList#getPreferredScrollableViewportSize and call this
     * method.
     * <pre><code>
     * public Dimension getPreferredScrollableViewportSize() {
     *    return JideSwingUtilities.adjustPreferredScrollableViewportSize(this, super.getPreferredScrollableViewportSize());
     * }
     * <p/>
     * </code></pre>
     *
     * @param list                the JList
     * @param defaultViewportSize the default viewport size from JList#getPreferredScrollableViewportSize().
     * @return the adjusted size.
     */
    public static Dimension adjustPreferredScrollableViewportSize(JList list, Dimension defaultViewportSize) {
        // workaround the bug that the list is tiny when the first element is empty
        Rectangle cellBonds = list.getCellBounds(0, 0);
        if (cellBonds != null && cellBonds.height < 3) {
            ListCellRenderer renderer = list.getCellRenderer();
            if (renderer != null) {
                Component c = renderer.getListCellRendererComponent(list, "DUMMY STRING", 0, false, false);
                if (c != null) {
                    Dimension preferredSize = c.getPreferredSize();
                    if (preferredSize != null) {
                        int height = preferredSize.height;
                        if (height < 3) {
                            try {
                                height = list.getCellBounds(1, 1).height;
                            }
                            catch (Exception e) {
                                height = 16;
                            }
                        }
                        list.setFixedCellHeight(height);
                    }
                }
            }
        }
        if (/*SystemInfo.*/isJdk15Above()) {
            return defaultViewportSize;
        }
        else {
            // in JDK1.4.2, the vertical scroll bar is shown because of the wrong size is calculated.
            defaultViewportSize.height++;
            return defaultViewportSize;
        }
    }
    
    private static JavaVersion _currentVersion;
    
    /**
     * Gets the system property.
     *
     * @param key          the property key
     * @param defaultValue the default value for the property.
     * @return the system property.
     */
    public static String getProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        }
        catch (AccessControlException e) {
            return defaultValue;
        }
    }
    
    /**
     * Returns the version of java we're using.
     *
     * @return the java version.
     */
    public static String getJavaVersion() {
        return /* SecurityUtils.*/ getProperty("java.version", "1.4.2");
    }
    
    private static void checkJdkVersion() {
        if (_currentVersion == null) {
            _currentVersion = new JavaVersion(getJavaVersion());
        }
    }

    
    /**
     * Returns whether or no the JDK version is 1.5 and above.
     *
     * @return <tt>true</tt> if the application is running on JDK 1.5 and above, <tt>false</tt> otherwise.
     */
    public static boolean isJdk15Above() {
        checkJdkVersion();
        return _currentVersion.compareVersion(1.5, 0, 0) >= 0;
    }


public static class JavaVersion {
    /**
     * For example: 1.6.0_12: Group 1 = major version (1.6) Group 3 = minor version (0) Group 5 = build number (12)
     */
    private static Pattern SUN_JAVA_VERSION = Pattern.compile("(\\d+\\.\\d+)(\\.(\\d+))?(_([^-]+))?(.*)");
    private static Pattern SUN_JAVA_VERSION_SIMPLE = Pattern.compile("(\\d+\\.\\d+)(\\.(\\d+))?(.*)");

    private double _majorVersion;
    private int _minorVersion;
    private int _buildNumber;
    private String _patch;

    public JavaVersion(String version) {
        _majorVersion = 1.4;
        _minorVersion = 0;
        _buildNumber = 0;
        try {
            Matcher matcher = SUN_JAVA_VERSION.matcher(version);
            if (matcher.matches()) {
                int groups = matcher.groupCount();
                _majorVersion = Double.parseDouble(matcher.group(1));
                if (groups >= 3 && matcher.group(3) != null) {
                    _minorVersion = Integer.parseInt(matcher.group(3));
                }
                if (groups >= 5 && matcher.group(5) != null) {
                    try {
                        _buildNumber = Integer.parseInt(matcher.group(5));
                    }
                    catch (NumberFormatException e) {
                        _patch = matcher.group(5);
                    }
                }
                if (groups >= 6 && matcher.group(6) != null) {
                    String s = matcher.group(6);
                    if (s != null && s.trim().length() > 0) _patch = s;
                }
            }
        }
        catch (NumberFormatException e) {
            try {
                Matcher matcher = SUN_JAVA_VERSION_SIMPLE.matcher(version);
                if (matcher.matches()) {
                    int groups = matcher.groupCount();
                    _majorVersion = Double.parseDouble(matcher.group(1));
                    if (groups >= 3 && matcher.group(3) != null) {
                        _minorVersion = Integer.parseInt(matcher.group(3));
                    }
                }
            }
            catch (NumberFormatException e1) {
                System.err.println("Please check the installation of your JDK. The version number " + version + " is not right.");
            }
        }
    }

    public JavaVersion(double major, int minor, int build) {
        _majorVersion = major;
        _minorVersion = minor;
        _buildNumber = build;
    }

    public int compareVersion(double major, int minor, int build) {
        double majorResult = _majorVersion - major;
        if (majorResult != 0) {
            return majorResult < 0 ? -1 : 1;
        }
        int result = _minorVersion - minor;
        if (result != 0) {
            return result;
        }
        return _buildNumber - build;
    }

    public double getMajorVersion() {
        return _majorVersion;
    }

    public int getMinorVersion() {
        return _minorVersion;
    }

    public int getBuildNumber() {
        return _buildNumber;
    }

    public String getPatch() {
        return _patch;
    }
	}
}