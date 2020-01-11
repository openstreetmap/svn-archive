// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.WordUtils;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.ToulouseDataSetHandler;
import org.openstreetmap.josm.tools.Logging;

public class HorodateurHandler extends ToulouseDataSetHandler {

    public HorodateurHandler() {
        super("horodateurs", "vending=parking_tickets");
        setWikiPage("Horodateurs");
        setCategory(CAT_TRANSPORT);
        setMenuIcon("presets/transport/ticket-machine.svg");
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Horodateur");
    }

    private String parseHour(String hour) {
        String s = hour.replaceFirst("[hH]", ":");
        if (s.endsWith(":")) {
            s += "00";
        }
        return s;
    }

    protected String parseOpeningHours(String horaire) {
        final String hour = "\\p{Digit}{1,2}[hH]\\p{Digit}{0,2}";
        final String sep  = "[ -/]+";
        final String hours = "("+hour+sep+hour+")";
        final Pattern p = Pattern.compile(hours+"(?:"+sep+hours+")*");
        final Matcher m = p.matcher(horaire);
        String opening_hours = "";
        if (m.matches()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                if (m.group(i) != null) {
                    if (!opening_hours.isEmpty()) {
                        opening_hours += "; ";
                    }
                    final Pattern p2 = Pattern.compile("("+hour+")"+sep+"("+hour+")");
                    final Matcher m2 = p2.matcher(m.group(i));
                    if (m2.matches()) {
                        opening_hours += parseHour(m2.group(1)) + "-" + parseHour(m2.group(2));
                    } else {
                        Logging.error(m.group(i)+" does not match "+p2);
                    }
                }
            }
        } else {
            Logging.error(horaire+" does not match "+p);
        }
        return opening_hours;
    }

    @Override
    public void updateDataSet(DataSet ds) {
        for (Node n : ds.getNodes()) {
            n.put("amenity", "vending_machine");
            n.put("vending", "parking_tickets");
            n.remove("name");
            n.remove("Code_Insee");
            n.remove("Lib_voie");
            n.remove("Mot_dir");
            n.remove("No");
            n.remove("Reglementation");
            n.remove("color");
            replace(n, "commune", "operator", value -> "Mairie de "+WordUtils.capitalizeFully(value));
            replace(n, "horaire", "opening_hours", this::parseOpeningHours);
            replace(n, "maj_date", "source:date", value -> value.substring(0, 4)+"-"+value.substring(4, 6)+"-"+value.substring(6, 8));
            replace(n, "observations", "note");
            replace(n, "quartier_residant", "parking:ticket:zone", value -> WordUtils.capitalizeFully(value.trim())
                    .replace(" Iv", " IV").replace("Sebastopol", "Sébastopol")
                    .replace("St ", "Saint-").replace("Peri", "Péri"));
        }
    }
}
