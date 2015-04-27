package org.openstreetmap.josm.plugins.features;

import de.bwaldvogel.liblinear.FeatureNode;
import java.util.List;
import java.util.Map;
import org.openstreetmap.josm.plugins.container.OSMRelation;
import org.openstreetmap.josm.plugins.container.OSMWay;

/**
 * Constructs the relation features for liblinear.
 * 
 * @author imis-nkarag
 */

public class RelationFeatures {
        int id;
    
        public RelationFeatures(int id){
            this.id = id;
        }        
        
        public void createRelationFeatures(OSMWay wayNode, List<OSMRelation> relationList) {      
        id++; //this should be removed when using boolean intervals for mean and variance
        //System.out.println("must be 1468" + id);
        //System.out.println("InstanceVectors rel start" + id);
        boolean hasRelation = false;
        for(OSMRelation relation : relationList){
            if(hasRelation){
                break;
            } 
            
            if(relation.getMemberReferences().contains(wayNode.getID())){
                hasRelation = true;
                Map<String, String> tags = relation.getTagKeyValue();
                
                if(tags.containsKey("route")){   
                    wayNode.getFeatureNodeList().add(new FeatureNode(id, 1.0));      
                }
                else if(tags.containsKey("multipolygon")){
                    wayNode.getFeatureNodeList().add(new FeatureNode(id+1, 1.0));                                      
                }
                else if(tags.containsKey("boundary")){
                    wayNode.getFeatureNodeList().add(new FeatureNode(id+2, 1.0));                                       
                }
                else if(tags.containsKey("restriction")){
                    wayNode.getFeatureNodeList().add(new FeatureNode(id+3, 1.0));                                      
                }
                else{
                    //the instance may be a member of a relation, but the relation has no type or is incomplete.
                    wayNode.getFeatureNodeList().add(new FeatureNode(id+4, 1.0));                  
                }
                id = id + 5;
            }
        }
        
        
        if(!hasRelation){
            id = id + 5;
        }        
    }
        
    public int getLastID(){
        return id;
    }    
}
