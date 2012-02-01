// License: GPL (v2 or later)
package org.openstreetmap.josm.plugins.roadsigns;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.openstreetmap.josm.tools.ImageProvider;

/**
 * Information about one road sign.
 *
 */
public class Sign {
    public String id;
    public ParametrizedString ref;
    public ParametrizedString traffic_sign_tag;
    public String iconURL;
    public ImageIcon icon;
    public ImageIcon disabledIcon;
    public String name;
    public String long_name;
    public List<Tag> tags = new ArrayList<Tag>();
    public List<Sign> supplements = new ArrayList<Sign>();
    public List<SignParameter> params = new ArrayList<SignParameter>();
    public boolean isSupplementing;
    public String loc_wiki;
    public String wiki;
    public String help;

    public JLabel label; // FIXME: don't put gui stuff here

    public static class Tag {
        public ParametrizedString key;
        public ParametrizedString value;
        public String ident;
        public String tag_ref;
        public ParametrizedString append_value;
        public ParametrizedString condition;
        @Override
        public String toString() {
            return "<TAG key="+key+" value="+value+" ident="+ident+" tag_ref="+tag_ref+
                    " append_value="+append_value+" condition="+condition+">";
        }
    }

    public static class SignParameter {
        public enum Input {TEXTFIELD, COMBO};
        public String ident;
        public Input input;
        String deflt;
        String prefix;
        String suffix;
        Integer fieldWidth;

        public SignParameter(String inputType) {
            if (inputType.equals("textfield")) {
                input = Input.TEXTFIELD;
            } else if (inputType.equals("combo")) {
                input = Input.COMBO;
            } else
                throw new IllegalArgumentException("unknown input type: "+inputType);
        }

        public String getDefault() {
            return deflt == null ? "" : deflt;
        }
        public String getPrefix() {
            return prefix == null ? "" : prefix;
        }
        public String getSuffix() {
            return suffix == null ? "" : suffix;
        }
    }

    public ImageIcon getIcon() {
        if (icon == null) {
            icon = new ImageProvider(iconURL).setDirs(RoadSignsPlugin.iconDirs).setId("plugin.sign."+id).setOptional(true).get();
        }
        return icon;
    }

    /**
     * Creates a generic form of the traffic sign ref.
     * It strips osm specific syntax so it can be
     * used in documentation and help texts.
     * @return null if the format is unknown
     */
    public String getDefaultRef() {
        if (ref == null)
            return null;
        String r = ref.toString();
        /* strip parameters */
        r = r.replaceAll("\\[.*\\]", "");
        if (r.startsWith("DE:")) {
            r=r.replaceAll("DE:", "");
            /* normal sign: starts with 3 digits, then a non-digit */
            {
                Pattern p = Pattern.compile("^\\d{3}(\\D|$)");
                Matcher m = p.matcher(r);
                if (m.find())
                    return tr("Sign {0}", r);
            }
            /* supplementary sign: starts with 4 digits, then a non-digit */
            {
                Pattern p = Pattern.compile("^\\d{4}(\\D|$)");
                Matcher m = p.matcher(r);
                if (m.find())
                    return tr("Additional sign {0}", r);
            }
            return null;
        }
        return null;
    }

    @Override
    public String toString() {
        return id+" - "+name;
    }

}
