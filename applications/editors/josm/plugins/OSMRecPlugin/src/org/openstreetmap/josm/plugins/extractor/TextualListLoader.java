package org.openstreetmap.josm.plugins.extractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.apache.log4j.Logger;

/**
 * Provides functionality for parsing the names file
 * This file contains name occurrences obtained from statistical measures on OSM data.
 * 
 * @author imis-nkarag
 */

public class TextualListLoader {
    
    //private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TextualListLoader.class);
    //private static final Logger LOG = Logger.getLogger(TextualListLoader.class);
    private List<String> namesList;
    
        public void parseNamesFile(File file){
            namesList = new ArrayList<>();
            
            Scanner input = null;
            
            try {
                input = new Scanner(file);
            } 
            catch (FileNotFoundException ex) {
                Logger.getLogger(TextualListLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
            while(input.hasNext()) {
                String nextLine = input.nextLine();
                namesList.add(nextLine);
            }
        //LOG.info("Name occurences parsed successfully!");
        }
        
        public List<String> getNamesList(){
            return namesList;
        }
}
