// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.parsers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Provides functionality for parsing the names file
 * This file contains name occurrences obtained from statistical measures on OSM data.
 *
 * @author imis-nkarag
 */
public class TextualStatistics {

    private List<String> textualList;

    public void parseTextualList(InputStream textualFileStream) {
        textualList = new ArrayList<>();

        Scanner input = new Scanner(textualFileStream, "UTF-8");
        while (input.hasNext()) {
            String nextLine = input.nextLine();
            textualList.add(nextLine);
        }
    }

    public List<String> getTextualList() {
        return Collections.unmodifiableList(textualList);
    }
}

