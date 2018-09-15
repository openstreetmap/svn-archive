// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.features;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;

import de.bwaldvogel.liblinear.FeatureNode;

/**
 * Constructs the class feature nodes for liblinear.
 * @author imis-nkarag
 */
public class ClassFeatures {

    private int directClassID = 0;

    public void createClassFeatures(OSMWay wayNode, Map<String, String> mappings, Map<String, Integer> mappingsWithIDs,
            Map<String, List<String>> indirectClasses, Map<String, Integer> indirectClassesIDs) {

        //iteration for each way node in the wayList
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
        for (Integer dirID : sortedDirectIDs) {
            wayNode.getFeatureNodeList().add(new FeatureNode(dirID, 1));
        }
        for (Integer indID : sortedIndirectIDs) {
            wayNode.getFeatureNodeList().add(new FeatureNode(indID, 1));
        }
        wayNode.setClassID(directClassID);
        //System.out.println("class: " + wayNode.getFeatureNodeList());
    }
}
