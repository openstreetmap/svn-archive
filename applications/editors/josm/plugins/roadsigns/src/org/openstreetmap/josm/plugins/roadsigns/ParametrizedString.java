// License: GPL (v2 or later)
package org.openstreetmap.josm.plugins.roadsigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openstreetmap.josm.plugins.roadsigns.javacc.ParamStringScanner;
import org.openstreetmap.josm.plugins.roadsigns.javacc.ParseException;
import org.openstreetmap.josm.plugins.roadsigns.javacc.TokenMgrError;

/**
 * A parametrized string is a string that contains identifiers that can
 * be substituted when the values of the parameters are known.
 * The syntax is in perl style, e.g.
 *  "Up to $val% in ${num}th row costs 12 \$.".
 *
 * This class represent a parsed string (parameters are identified).
 * It can be constructed from a String. Given a parameter environment,
 * the String value can be determined.
 *
 */
public class ParametrizedString {
    final List<StringOrParameter> token = new ArrayList<StringOrParameter>();

    /**
     * Describes the union of String and Parameter type. Both
     * types have a single String object as their backing data.
     */
    public static interface StringOrParameter {
    }

    public static class Prm implements StringOrParameter {
        public final String ident;
        public Prm(String ident) {
            this.ident = ident;
        }

        @Override
        public String toString() {
            return "<Prm="+ident+">";
        }
    }

    public static class Str implements StringOrParameter {
        String value;
        public Str(String stringValue) {
            this.value = stringValue;
        }

        @Override
        public String toString() {
            return "<Str="+value+">";
        }
    }

    protected ParametrizedString(String input) throws ParseException, TokenMgrError {
        scan(input);
    }

    /**
     * Constructor that returns a null value for null value input.
     * Once it is created, the list of token is immutable.
     */
    public static ParametrizedString create(String input) throws ParseException, TokenMgrError {
        if (input == null)
            return null;
        return new ParametrizedString(input);
    }

    /**
     * The tokenizer. Creates the list of tokens
     * from the input string.
     */
    protected void scan(String input) throws ParseException, TokenMgrError {
        List<StringOrParameter> tmp = null;
        tmp = ParamStringScanner.parseIt(input);
        StringBuffer curString = new StringBuffer();
        for (StringOrParameter sp : tmp) {
            if (sp instanceof Prm) {
                if (curString.length() > 0) {
                    token.add(new Str(curString.toString()));
                    curString = new StringBuffer();
                }
                token.add(sp);
            } else if (sp instanceof Str) {
                curString.append(((Str) sp).value);
            }
        }
        if (curString.length() > 0) {
            token.add(new Str(curString.toString()));
        }
    }

    /**
     * Evaluates the string value, given an environment of
     * parameter mappings.
     */
    public String evaluate(Map<String, String> env) {
        StringBuilder sb = new StringBuilder();
        for (StringOrParameter t : token) {
            if (t instanceof Str) {
                sb.append(((Str) t).value);
            } else if (t instanceof Prm) {
                String val = env.get(((Prm) t).ident);
                if (val == null) {
                    System.err.println("Warning: Parameter not in environment: "+((Prm) t).ident+" ("+this.toString()+")");
                    Thread.dumpStack();
                    val = "<?>";
                }
                sb.append(val);
            } else
                throw new AssertionError();
        }
        return sb.toString();
    }

    /**
     * Converts to a string that could be parsed again to an equal object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (StringOrParameter t : token) {
            if (t instanceof Str) {
                sb.append(((Str) t).value);
            } else if (t instanceof Prm) {
                sb.append("${"+((Prm) t).ident+"}");
            } else
                throw new AssertionError();
        }
        return sb.toString();
    }

    /**
     * Creates a debug string for this object
     */
    public String toDebugString() {
        StringBuilder res = new StringBuilder();
        for (StringOrParameter sop : token) {
            res.append(sop);
        }
        return res.toString();
    }
}
