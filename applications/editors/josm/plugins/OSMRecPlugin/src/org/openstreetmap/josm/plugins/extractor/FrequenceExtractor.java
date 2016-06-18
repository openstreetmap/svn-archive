package org.openstreetmap.josm.plugins.extractor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.container.OSMNode;
import org.openstreetmap.josm.plugins.container.OSMRelation;
import org.openstreetmap.josm.plugins.container.OSMWay;
import org.openstreetmap.josm.tools.Utils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses OSM xml and extracts the frequencies of the name tag values.
 *
 * @author imis-nkarag
 */

public class FrequenceExtractor extends DefaultHandler {
    
    private final List<OSMNode> nodeList; //will be populated with nodes 
    private final List<OSMRelation> relationList;
    private final Map<String, OSMNode> nodesWithIDs; //map containing IDs as Strings and the corresponding OSMNode objects     
    private final List<OSMWay> wayList;  //populated with ways of the OSM file
    private final String osmXmlFileName;
    private OSMNode nodeTmp; 
    private OSMWay wayTmp; 
    private OSMRelation relationTmp; 
    private boolean inWay = false; //when parser is in a way node becomes true in order to track the parser position 
    private boolean inNode = false; //becomes true when the parser is in a simple node        
    private boolean inRelation = false; //becomes true when the parser is in a relarion node
    private Map<String,Integer> frequency;
    
    public FrequenceExtractor(String osmXmlFileName) {
        this.osmXmlFileName = osmXmlFileName;       
        nodeList = new ArrayList<>();
        wayList = new ArrayList<>();
        relationList = new ArrayList<>();
        nodesWithIDs = new HashMap<>(); 
        frequency = new HashMap<>();
        for (int i =0; i<100; i++){
            frequency.put("", 0);
        }
    }

    public void parseDocument() {
        Main.info("extracting frequencies...");
        try {
            Utils.newSafeSAXParser().parse(osmXmlFileName, this);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            Main.error(e);
        }
    }

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) throws SAXException {
    
        // if current element is an OSMNode , create new node and populate with the appropriate values
        if (elementName.equalsIgnoreCase("node")) {
            nodeTmp = new OSMNode();
            inNode = true;
            inWay = false;
            inRelation = false;

        } else if (elementName.equalsIgnoreCase("way")) {
            wayTmp = new OSMWay();
            //wayTmp.setID(attributes.getValue("id"));
            inWay = true;
            inNode = false;
            inRelation = false;
        } else if (elementName.equalsIgnoreCase("relation")) {
            relationTmp = new OSMRelation();
            //relationTmp.setID(attributes.getValue("id"));
            inRelation = true;
            inWay = false;
            inNode = false;
        } else if (elementName.equalsIgnoreCase("nd")) {
            //wayTmp.addNodeReference(attributes.getValue("ref"));

        } else if (elementName.equalsIgnoreCase("tag")) {

            if (inNode) {
                //if the path is in an OSMNode set tagKey and value to the corresponding node     
                nodeTmp.setTagKeyValue(attributes.getValue("k"), attributes.getValue("v"));
            } else if (inWay) {
                //else if the path is in an OSM way set tagKey and value to the corresponding way
                wayTmp.setTagKeyValue(attributes.getValue("k"), attributes.getValue("v"));
            } else if(inRelation){
                //set the key-value pairs of relation tags
                relationTmp.setTagKeyValue(attributes.getValue("k"), attributes.getValue("v"));
            } 
            
        } else if (elementName.equalsIgnoreCase("member")) {
            //relationTmp.addMemberReference(attributes.getValue("ref"));
        }                 
    }

    @Override
    public void endElement(String s, String s1, String element) throws SAXException {
        // if end of node element, add to appropriate list
        if (element.equalsIgnoreCase("node")) {
            //nodeList.add(nodeTmp);
            //nodesWithIDs.put(nodeTmp.getID(), nodeTmp);
                       
            Map<String,String> tags = nodeTmp.getTagKeyValue();
            //System.out.println("tag: " + tags);
            if (tags.keySet().contains("name")){
                for (Map.Entry<String, String> tag : tags.entrySet()){
                    if (tag.getKey().equals("name")){
                        //split name value in each white space and store the values separetely. Count each occurance 
                        String name = tag.getValue();
                        String[] SplitName = name.split("\\s+");

                        for (String split : SplitName){
                        //System.out.println("split:  " + split + "  0");
                        
                        //frequency.put(split, k);
                        //put all splits with zero, at the constructor. put here the incremented values. for tomoro
                            if (frequency.get(split) != null){
                            int k = frequency.get(split) +1;                        
                            frequency.put(split, k); 
                            //System.out.println("get split exists,   k= " + k);
                            }
                            else{
                                frequency.put(split, 1);
                            }
                        
                        //System.out.println("frequency getValue +1  " + frequency.get(tag.getValue()) +1);
                        }
                        //System.out.println("node name:  "  + tag.getValue());
                    }
                }                               
            }
        }
        
        if (element.equalsIgnoreCase("way")) {            
            //name frequency
            Map<String,String> tags = wayTmp.getTagKeyValue();
            
            
            //System.out.println("tag: " + tags);
            if (tags.keySet().contains("name")){
                for (Map.Entry<String, String> tag : tags.entrySet()){
                    if (tag.getKey().equals("name")){
                        //split name value in each white space and store the values separetely. Count each occurance 
                        String name = tag.getValue();
                        String[] SplitName = name.split("\\s+");
                        //String[] SplitName = name.split("[\\W]");
                        for (String split : SplitName){
                        //System.out.println("split:  " + split + "  0");
                        
                        //frequency.put(split, k);
                        //put all splits with zero, at the constructor. put here the incremented values. for tomoro
                            if (frequency.get(split) != null){
                            int k = frequency.get(split) +1;                        
                            frequency.put(split, k); 
                            //System.out.println("get split exists,   k= " + k);
                            }
                            else{
                                frequency.put(split, 1);
                            }
                        
                        //System.out.println("frequency getValue +1  " + frequency.get(tag.getValue()) +1);
                        }
                        //System.out.println("way name:  "  + tag.getValue());
                    }
                }                               
            }
        } 
        
        if(element.equalsIgnoreCase("relation")) {
            
            //name frequency
            Map<String,String> tags = relationTmp.getTagKeyValue();
 
            //System.out.println("tag: " + tags);
            if (tags.keySet().contains("name")){
                for (Map.Entry<String, String> tag : tags.entrySet()){
                    if (tag.getKey().equals("name")){
                        //split name value in each white space and store the values separetely. Count each occurance 
                        String name = tag.getValue();
                        String[] SplitName = name.split("\\s+");

                        for (String split : SplitName){
                        //System.out.println("split:  " + split + "  0");
                        
                        //frequency.put(split, k);
                        //put all splits with zero, at the constructor. put here the incremented values. for tomoro
                            if (frequency.get(split) != null){
                            int k = frequency.get(split) +1;                        
                            frequency.put(split, k); 
                            //System.out.println("get split exists,   k= " + k);
                            }
                            else{
                                frequency.put(split, 1);
                            }
                        
                        //System.out.println("frequency getValue +1  " + frequency.get(tag.getValue()) +1);
                        }
                        //System.out.println("relation name:  "  + tag.getValue());
                    }
                }                               
            }
            //relationList.add(relationTmp);
        }
    }

    public List<OSMNode> getNodeList() {
        return nodeList;
    }

    public List<OSMWay> getWayList() {
        return wayList;
    }
    
    public List<OSMRelation> getRelationList(){
        return relationList;
    }

    public Map<String, OSMNode> getNodesWithIDs() {
        return nodesWithIDs;
    }    
    
    //frequency temp
    public Map<String, Integer> getFrequency(){
        return frequency;
    }
}