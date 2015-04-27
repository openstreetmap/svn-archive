package org.openstreetmap.josm.plugins.parsers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides functionality for parsing the names file
 * This file contains name occurrences obtained from statistical measures on OSM data.
 * 
 * @author imis-nkarag
 */

public class TextualStatistics {
    
    //private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TextualStatistics.class);
    private List<String> textualList;
    
        public void parseTextualList(InputStream textualFileStream){
            textualList = new ArrayList<>();
            
            Scanner input = new Scanner(textualFileStream);
            while(input.hasNext()) {
                String nextLine = input.nextLine();
                textualList.add(nextLine);
            }
            //LOG.info("Name occurences parsed successfully!");
        }
        
        public List<String> getTextualList(){
            return textualList;
        }
}

