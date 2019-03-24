// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.parsers;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * MappingsParser takes the Map file which contains the LGD mappings and extracts information
 * about the OSM tags and the ontology classes
 * This info is used in the vector construction.
 *
 * @author imis-nkarag
 */

public class Mapper {
    //private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Mapper.class);
    private final HashMap<String, String> mappings;
    private final HashMap<String, Integer> mappingsWithIDs;
    private final Map<Integer, String> idsWithMappings;

    public Mapper() {

        mappings = new LinkedHashMap<>();
        mappingsWithIDs = new HashMap<>();
        idsWithMappings = new HashMap<>();
    }

    public void parseFile(InputStream inps) throws FileNotFoundException {
        Scanner input = new Scanner(inps, "UTF-8"); //the Map file contains lines of the mappings separated with the symbol "|"
        //e.g. highway motorway | Motorway
        //the key will be the string "highway motorway" and the value "Motorway"
        while (input.hasNext()) {

            String nextLine = input.nextLine();
            String[] splitContent = nextLine.split("\\|", 2);   //split current line in two parts,
            //separated by the "|" symbol
            String key = splitContent[0];                      //this key will be mapped to a class
            String value = splitContent[1];                    //this value is the mapped class
            key = key.trim();
            value = value.trim();
            mappings.put(key, value);
        }
        constructMappingsWithIDs();
        constructIDsWithMappings();
    }

    private void constructMappingsWithIDs() {
        Integer i = 1; //starting ID is 1: SVM multiclass does not accept 0 as a class ID
        for (String ontologyClass : mappings.values()) {
            mappingsWithIDs.put(ontologyClass, i);
            i++;
        }
    }

    public Map<String, Integer> getMappingsWithIDs() {
        return Collections.unmodifiableMap(mappingsWithIDs);
    }

    public Map<String, String> getMappings() {
        return Collections.unmodifiableMap(mappings);
    }

    private void constructIDsWithMappings() {
        int i = 1; //starting ID is 1: SVM multiclass does not accept 0 as a class ID

        for (String ontologyClass : mappings.values()) {
            idsWithMappings.put(i, ontologyClass);
            i++;
        }
    }

    public Map<Integer, String> getIDsWithMappings() {
        return Collections.unmodifiableMap(idsWithMappings);
    }
}
