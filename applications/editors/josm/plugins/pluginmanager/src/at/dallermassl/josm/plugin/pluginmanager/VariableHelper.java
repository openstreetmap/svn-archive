/**
 *
 */
package at.dallermassl.josm.plugin.pluginmanager;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that replaces variables in strings with its values. The variables are in the
 * form ${name}. Replacement values may be set. As a fallback (if the variable is not found)
 * the system properties are used. If neither is found, the variable is not replaced.
 *
 * @author cdaller
 *
 */
public class VariableHelper {
    private Pattern varPattern = Pattern.compile("\\$\\{(.+?)\\}");
    private Map<String, String> variables;

    public VariableHelper() {
        variables = new HashMap<String, String>();
    }

    /**
     * Adds all key/values as variables.
     * @param pref the values to add.
     */
    public void addAll(Map<String,String> values) {
        variables.putAll(values);
    }

    /**
     * Adds a single key/value pair.
     * @param key
     * @param value
     */
    public void add(String key, String value) {
        variables.put(key, value);
    }

    /**
     * Replaces all variable placeholder in the given string with the replacement.
     * If the variable cannot be found and is not a System.property, the placeholder remains
     * untouched. The placeholder has the form of "${varname}".
     * @param value
     * @return
     */
    public String replaceVariables(String value) {
        StringBuilder source = new StringBuilder(value);
        Matcher matcher = varPattern.matcher(source);
        int index = 0;
        String varName;
        String replacement;
        while(matcher.find(index)) {
            varName = matcher.group(1);
            replacement = variables.get(varName);
            if(replacement == null) {
                replacement = System.getProperty(varName);
            }
            if(replacement != null) {
                source.replace(matcher.start(), matcher.end(), replacement);
                index = matcher.start();
            } else {
                // key not found, move on
                index = index + 4; // "${x}".length
            }
        }
        return source.toString();
    }

    public static void main(String[] args) {
        VariableHelper helper = new VariableHelper();
        System.out.println(helper.replaceVariables("abc${java.version}cde${os.name}${user.name}xx${unknoqn}"));
    }

}
