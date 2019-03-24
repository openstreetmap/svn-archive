// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.features;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;

/**
 * Defines the class/category of an OSM instance by its tags.
 * @author imis-nkarag
 */

public class OSMClassification {

    private int directClassID = 0;

    public void calculateClasses(OSMWay wayNode, Map<String, String> mappings, Map<String, Integer> mappingsWithIDs,
            Map<String, List<String>> indirectClasses, Map<String, Integer> indirectClassesIDs) {

        Set<Integer> sortedIndirectIDs = new TreeSet<>();
        Set<Integer> sortedDirectIDs = new TreeSet<>();
        for (Map.Entry<String, String> wayTagKeyValue : wayNode.getTagKeyValue().entrySet()) {
            //iteration for each tag (key-value) in the current way node

            //concat key and value to use it later for checking
            String key = wayTagKeyValue.getKey() + " " + wayTagKeyValue.getValue();

            for (Map.Entry<String, String> tagMappedToClass : mappings.entrySet()) {
                //entry of mappings is e.g "highway residential <-> ResidentialHighway"
                //iteration to discover the wayNode class. This class's ID will be the start of the vector

                if (key.equals(tagMappedToClass.getKey())) {
                    String className = tagMappedToClass.getValue();
                    directClassID = mappingsWithIDs.get(className);

                    sortedDirectIDs.add(directClassID);

                    //the direct class id is the last direct class that the instance is found to belong

                    List<String> superClassesList = indirectClasses.get(className);

                    if (superClassesList != null) { //check if the class has no superclasses

                        for (String superClass: superClassesList) {

                            Integer indirectID = indirectClassesIDs.get(superClass); //to save time here
                            if (indirectID != null) { // there is a chance here that the ID is null,
                                //cause the list of super Classes  might contain extra classes  with no ID
                                //in the indirectClassesIDs map which is constructed from listHierarchyRootClasses method
                                //at the OntologyParser.
                                //so this condition check will remain for now

                                if (!sortedIndirectIDs.contains(indirectID)) {
                                    sortedIndirectIDs.add(indirectID);
                                    //wayNode.getIndexVector().put(indirectID, 1.0);
                                }
                                //the construction of the indirectClassVectorPortion has been moved below, sorted
                                //indirectClassVectorPortion = indirectClassVectorPortion + indirectID + ":1 ";
                            }
                        }
                    }
                }
            }
        }
        wayNode.setClassIDs(sortedDirectIDs);
        //System.out.println("OSMClassification, selected instance classes: " + sortedDirectIDs);
        wayNode.setClassID(directClassID);
    }
}
