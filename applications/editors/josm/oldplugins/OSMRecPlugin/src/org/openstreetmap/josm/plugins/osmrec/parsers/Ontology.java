// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.parsers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Loads the ontology based on the owl.xml file.
 * @author imis-nkarag
 */

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Parses the ontology from the owl.xml file
 * The owl file contains information about the ontology and hierarchy of the classes
 * Provides methods for retrieving information about the ontology.
 *
 *
 * @author imis-nkarag
 */

public class Ontology {
    //private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(Ontology.class);

    private OntModel ontologyModel;
    private List<OntClass> listHierarchy;
    private static final int additiveID = 1373;
    private Map<String, List<String>> indirectClasses;
    private final Map<String, Integer> indirectClassesIDs;
    private final InputStream owlFile;

    public Ontology(InputStream owlFile) {

        this.owlFile = owlFile;
        indirectClassesIDs = new HashMap<>();
        indirectClasses = new HashMap<>();
        listHierarchy = new ArrayList<>();
    }

    public void parseOntology() {
        try {

            //create the ontology model using the base
            ontologyModel = ModelFactory.createOntologyModel();

            org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ERROR);
            ontologyModel.read(owlFile, null);    //Hide RDFDefaultErrorHandler from terminal to keep clear output.
            org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);

            listHierarchy = ontologyModel.listHierarchyRootClasses().toList();
            setListHierarchy(listHierarchy);

            ExtendedIterator<OntClass> classes = ontologyModel.listClasses();
            while (classes.hasNext()) {
                String className;
                OntClass obj = classes.next();

                //compare localname with class name from map and call getSuperclass
                if (obj.hasSubClass()) {

                    for (Iterator<OntClass> i = obj.listSubClasses(true); i.hasNext();) {
                        OntClass currentClass = i.next();

                        List<OntClass> superClasses = currentClass.listSuperClasses().toList();
                        List<String> superClassesStrings = new ArrayList<>();

                        for (OntClass superClass : superClasses) {
                            className = superClass.toString().replace("http://linkedgeodata.org/ontology/", "");
                            superClassesStrings.add(className);
                        }
                        indirectClasses.put(currentClass.getLocalName(), superClassesStrings);
                    }
                }
            }
            createIndirectClassesWithIDs();
            setIndirectClasses(indirectClasses);
            setOntologyModel(ontologyModel);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createIndirectClassesWithIDs() {
        for (int i = 0; i < listHierarchy.size(); i++) {

            String key = listHierarchy.get(i).toString().replace("http://linkedgeodata.org/ontology/", "");
            //we add 1 to the ID because we want IDs beginning from 1. listHierarchy has index beginning from 0
            indirectClassesIDs.put(key, i + additiveID); //the IDs start from 1373 to avoid duplicate IDs at the vectorConstructor
        }
    }

    private void setOntologyModel(OntModel ontologyModel) {
        this.ontologyModel = ontologyModel;
    }

    private void setIndirectClasses(Map<String, List<String>> indirectClasses) {
        this.indirectClasses = indirectClasses;

    }

    private void setListHierarchy(List<OntClass> listHierarchy) {
        this.listHierarchy = listHierarchy;
    }

    public OntModel getOntologyModel() {
        return this.ontologyModel;
    }

    public Map<String, List<String>> getIndirectClasses() {
        return this.indirectClasses;
    }

    public List<OntClass> getListHierarchy() {
        return this.listHierarchy;
    }

    public Map<String, Integer> getIndirectClassesIDs() {
        return this.indirectClassesIDs;
    }

}
