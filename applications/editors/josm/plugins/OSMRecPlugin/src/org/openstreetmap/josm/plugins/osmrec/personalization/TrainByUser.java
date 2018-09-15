// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.personalization;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;
import org.openstreetmap.josm.plugins.osmrec.core.AbstractTrainWorker;
import org.openstreetmap.josm.plugins.osmrec.extractor.LanguageDetector;
import org.openstreetmap.josm.plugins.osmrec.features.ClassFeatures;
import org.openstreetmap.josm.plugins.osmrec.features.GeometryFeatures;
import org.openstreetmap.josm.plugins.osmrec.features.OSMClassification;
import org.openstreetmap.josm.plugins.osmrec.features.RelationFeatures;
import org.openstreetmap.josm.plugins.osmrec.features.TextualFeatures;
import org.openstreetmap.josm.plugins.osmrec.parsers.Mapper;
import org.openstreetmap.josm.plugins.osmrec.parsers.Ontology;

import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

/**
 *  Trains a model by the history of given user.
 *
 *  @author imis-nkarag
 */
public class TrainByUser extends AbstractTrainWorker {

    private final String username;

    public TrainByUser(String inputFilePath, String username, boolean validateFlag, double cParameterFromUser,
            int topK, int frequency, boolean topKIsSelected, LanguageDetector languageDetector, List<OSMWay> wayList) {
        super(inputFilePath, validateFlag, cParameterFromUser, topK, frequency, topKIsSelected, languageDetector);
        this.username = username;
        AbstractTrainWorker.wayList = wayList;
    }

    @Override
    public Void doInBackground() throws Exception {
        extractTextualList();
        parseFiles();
        if (validateFlag) {
            firePropertyChange("progress", getProgress(), 5);
            validateLoop();
            firePropertyChange("progress", getProgress(), 40);
            System.out.println("Training model with the best c: " + bestConfParam);
            clearDataset();
            trainModel(bestConfParam);
            firePropertyChange("progress", getProgress(), 60);
            clearDataset();
            trainModelWithClasses(bestConfParam);
            firePropertyChange("progress", getProgress(), 100);
            setProgress(100);
        } else {
            clearDataset();
            firePropertyChange("progress", getProgress(), 10);
            trainModel(cParameterFromUser);
            firePropertyChange("progress", getProgress(), 60);
            clearDataset();
            firePropertyChange("progress", getProgress(), 65);
            trainModelWithClasses(cParameterFromUser);

            firePropertyChange("progress", getProgress(), 100);
            setProgress(100);
            System.out.println("done.");
        }
        System.out.println("Train by user process complete.");
        return null;
    }

    private void parseFiles() {
        InputStream tagsToClassesMapping = getClass().getResourceAsStream("/resources/files/Map");

        Mapper mapper = new Mapper();
        try {
            mapper.parseFile(tagsToClassesMapping);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        mappings = mapper.getMappings();
        mapperWithIDs = mapper.getMappingsWithIDs();

        InputStream ontologyStream = getClass().getResourceAsStream("/resources/files/owl.xml");
        Ontology ontology = new Ontology(ontologyStream);

        ontology.parseOntology();
        System.out.println("ontology parsed ");
        indirectClasses = ontology.getIndirectClasses();
        indirectClassesWithIDs = ontology.getIndirectClassesIDs();

        InputStream textualFileStream = null;
        try {
            textualFileStream = new FileInputStream(new File(textualListFilePath));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        readTextualFromDefaultList(textualFileStream);

        numberOfTrainingInstances = wayList.size();
        System.out.println("number of instances: " + numberOfTrainingInstances);
        System.out.println("end of parsing files.");
        if (numberOfTrainingInstances == 0) {
            System.out.println("This user has not edited the loaded area. Cannot train a model!");
        }
    }

    public void validateLoop() {
        Double[] confParams = new Double[] {
                Math.pow(2, -10), Math.pow(2, -10), Math.pow(2, -5), Math.pow(2, -3), Math.pow(2, -1), Math.pow(2, 0)};

        double bestC = Math.pow(2, -10);

        for (Double param : confParams) {
            foldScore1 = 0;
            foldScore5 = 0;
            foldScore10 = 0;
            System.out.println("\n\n\nrunning for C = " + param);
            clearDataset();
            System.out.println("fold1");
            crossValidateFold(0, 4, 4, 5, false, param); //4-1

            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold2");
            crossValidateFold(1, 5, 0, 1, false, param);

            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold3");
            crossValidateFold(0, 5, 1, 2, true, param);

            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold4");
            crossValidateFold(0, 5, 2, 3, true, param);

            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold5");
            crossValidateFold(0, 5, 3, 4, true, param);

            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            System.out.println("\n\nC=" + param + ", average score 1-5-10: " + foldScore1/5 +" "+ foldScore5/5 + " "+ foldScore10/5);
            if (bestScore < foldScore1) {
                bestScore = foldScore1;
                bestC = param;
            }

        }
        bestConfParam = bestC;
        System.out.println("best c param= " + bestC + ", score: " + bestScore/5);
    }

    public void crossValidateFold(int a, int b, int c, int d, boolean skip, double param) {
        System.out.println("Starting cross validation");
        int testSize = wayList.size()/5;

        List<OSMWay> trainList = new ArrayList<>();
        for (int g = a*testSize; g < b*testSize; g++) {  // 0~~1~~2~~3~~4~~5
            if (skip) {
                if (g == c*testSize) {
                    g = (c+1)*testSize;
                }
            }
            trainList.add(wayList.get(g));
        }

        int wayListSizeWithoutUnclassified = trainList.size();
        System.out.println("trainList size: " + wayListSizeWithoutUnclassified);

        //set classes for each osm instance
        int sizeToBeAddedToArray = 0; //this will be used to proper init the features array, adding the multiple vectors size
        for (OSMWay way : trainList) {

            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

            if (way.getClassIDs().isEmpty()) {
                wayListSizeWithoutUnclassified -= 1;
            } else {
                sizeToBeAddedToArray = sizeToBeAddedToArray + way.getClassIDs().size()-1;
            }
        }
        double C = param;
        double eps = 0.001;
        double[] GROUPS_ARRAY2 = new double[wayListSizeWithoutUnclassified+sizeToBeAddedToArray];
        FeatureNode[][] trainingSetWithUnknown2 = new FeatureNode[wayListSizeWithoutUnclassified+sizeToBeAddedToArray][numberOfFeatures];
        int k = 0;

        for (OSMWay way : trainList) {
            //adding multiple vectors
            int id;
            if (USE_CLASS_FEATURES) {
                ClassFeatures class_vector = new ClassFeatures();
                class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                id = 1422;
            } else {
                id = 1;
            }
            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            if (USE_RELATION_FEATURES) {
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);
                id = relationFeatures.getLastID();
            } else {
                id = geometryFeatures.getLastID();
            }
            //id 1531

            TextualFeatures textualFeatures;
            if (USE_TEXTUAL_FEATURES) {
                textualFeatures = new TextualFeatures(id, namesList, languageDetector);
                textualFeatures.createTextualFeatures(way);
            }

            List<FeatureNode> featureNodeList = way.getFeatureNodeList();
            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];

            if (!way.getClassIDs().isEmpty()) {
                int i = 0;
                for (FeatureNode featureNode : featureNodeList) {
                    featureNodeArray[i] = featureNode;
                    i++;
                }
                for (int classID : way.getClassIDs()) {
                    trainingSetWithUnknown2[k] = featureNodeArray;
                    GROUPS_ARRAY2[k] = classID;
                    k++;
                }
            }
        }

        Problem problem = new Problem();
        problem.l = wayListSizeWithoutUnclassified+sizeToBeAddedToArray;
        problem.n = numberOfFeatures;
        //(geometry 105 + textual        //3797; // number of features //the largest index of all features //3811;//3812 //1812 with classes
        problem.x = trainingSetWithUnknown2; // feature nodes
        problem.y = GROUPS_ARRAY2; // target values
        SolverType solver2 = SolverType.getById(2); //2 -- L2-regularized L2-loss support vector classification (primal)

        Parameter parameter = new Parameter(solver2, C, eps);

        long start = System.nanoTime();
        System.out.println("training...");
        PrintStream original = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {

            }
        }));

        Model model = Linear.train(problem, parameter);
        long end = System.nanoTime();
        Long elapsedTime = end-start;
        System.setOut(original);
        System.out.println("training process completed in: " + NANOSECONDS.toSeconds(elapsedTime) + " seconds.");

        //decide model path and naming and/or way of deleting/creating 1 or more models.

        File modelFile;
        if (USE_CLASS_FEATURES) {
            modelFile = new File(modelDirectory.getAbsolutePath()+"/user_" + username + "_model_with_classes_c=" + param);
        } else {
            modelFile = new File(modelDirectory.getAbsolutePath()+"/user_" + username + "_model_geometries_textual_c=" + param);
        }

        if (modelFile.exists()) {
            modelFile.delete();
        }
        try {
            model.save(modelFile);
            System.out.println("model saved at: " + modelFile);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        //end of evaluation training

        //test set
        List<OSMWay> testList = new ArrayList<>();
        for (int g = c*testSize; g < d*testSize; g++) {
            testList.add(wayList.get(g));
            //liblinear test
        }
        System.out.println("testList size: " + testList.size());
        int succededInstances = 0;
        int succededInstances5 = 0;
        int succededInstances10 = 0;
        try {
            model = Model.load(modelFile);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        int modelLabelSize = model.getLabels().length;
        int[] labels = model.getLabels();
        Map<Integer, Integer> mapLabelsToIDs = new HashMap<>();
        for (int h = 0; h < model.getLabels().length; h++) {
            mapLabelsToIDs.put(labels[h], h);
        }

        int wayListSizeWithoutUnclassified2 = testList.size();
        for (OSMWay way : testList) {

            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
            if (way.getClassIDs().isEmpty()) {
                wayListSizeWithoutUnclassified2 -= 1;
            }
        }

        FeatureNode[] testInstance2;
        for (OSMWay way : testList) {

            int id;

            if (USE_CLASS_FEATURES) {
                ClassFeatures class_vector = new ClassFeatures();
                class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                id = 1422;
            } else {
                id = 1;
            }

            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            if (USE_RELATION_FEATURES) {
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);
                id = relationFeatures.getLastID();
            } else {
                id = geometryFeatures.getLastID();
            }
            //id 1531
            if (USE_TEXTUAL_FEATURES) {
                TextualFeatures textualFeatures = new TextualFeatures(id, namesList, languageDetector);
                textualFeatures.createTextualFeatures(way);
            }
            List<FeatureNode> featureNodeList = way.getFeatureNodeList();

            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];

            int i = 0;
            for (FeatureNode featureNode : featureNodeList) {
                featureNodeArray[i] = featureNode;
                i++;
            }

            testInstance2 = featureNodeArray;
            double[] scores = new double[modelLabelSize];
            Linear.predictValues(model, testInstance2, scores);

            //find index of max values in scores array: predicted classes are the elements of these indexes from array model.getlabels
            //iter scores and find 10 max values with their indexes first. then ask those indexes from model.getlabels
            Map<Double, Integer> scoresValues = new HashMap<>();
            for (int h = 0; h < scores.length; h++) {
                scoresValues.put(scores[h], h);
                //System.out.println(h + "   <->    " + scores[h]);
            }

            Arrays.sort(scores);
            if (way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-1])])) {
                succededInstances++;
            }
            if (

                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-1])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-2])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-3])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-4])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-5])])
                    ) {
                succededInstances5++;
            }
            if (

                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-1])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-2])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-3])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-4])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-5])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-6])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-7])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-8])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-9])]) ||
                    way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-10])])
                    ) {
                succededInstances10++;
            }
        }

        System.out.println("Succeeded " + succededInstances + " of " + testList.size() + " total (1 class prediction)");
        double precision1 = succededInstances/(double) wayListSizeWithoutUnclassified2;
        score1 = precision1;
        System.out.println(precision1);

        System.out.println("Succeeded " + succededInstances5 + " of " + testList.size()+ " total (5 class prediction)");
        double precision5 = succededInstances5/(double) wayListSizeWithoutUnclassified2;
        score5 = precision5;
        System.out.println(precision5);

        System.out.println("Succeeded " + succededInstances10 + " of " + testList.size()+ " total (10 class prediction)");
        double precision10 = succededInstances10/(double) wayListSizeWithoutUnclassified2;
        score10 = precision10;
        System.out.println(precision10);
    }

    private void trainModel(double param) {

        int wayListSizeWithoutUnclassified = wayList.size();
        System.out.println("trainList size: " + wayListSizeWithoutUnclassified);
        if (wayListSizeWithoutUnclassified == 0) {
            System.out.println("aborting training process..");
            return;
        }
        //set classes for each osm instance
        int sizeToBeAddedToArray = 0; //this will be used to proper init the features array, adding the multiple vectors size
        for (OSMWay way : wayList) {
            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

            if (way.getClassIDs().isEmpty()) {
                wayListSizeWithoutUnclassified -= 1;
            } else {
                sizeToBeAddedToArray = sizeToBeAddedToArray + way.getClassIDs().size()-1;
            }
        }
        double C = param;
        double eps = 0.001;
        double[] GROUPS_ARRAY2 = new double[wayListSizeWithoutUnclassified+sizeToBeAddedToArray];
        FeatureNode[][] trainingSetWithUnknown2 = new FeatureNode[wayListSizeWithoutUnclassified+sizeToBeAddedToArray][numberOfFeatures];
        int k = 0;

        for (OSMWay way : wayList) {
            //adding multiple vectors
            int id;
            if (USE_CLASS_FEATURES) {
                ClassFeatures class_vector = new ClassFeatures();
                class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                id = 1422;
            } else {
                id = 1;
            }
            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            if (USE_RELATION_FEATURES) {
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);
                id = relationFeatures.getLastID();
            } else {
                id = geometryFeatures.getLastID();
            }
            //id 1531

            TextualFeatures textualFeatures;
            if (USE_TEXTUAL_FEATURES) {
                textualFeatures = new TextualFeatures(id, namesList, languageDetector);
                textualFeatures.createTextualFeatures(way);
            }

            List<FeatureNode> featureNodeList = way.getFeatureNodeList();
            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];

            if (!way.getClassIDs().isEmpty()) {
                int i = 0;
                for (FeatureNode featureNode : featureNodeList) {
                    featureNodeArray[i] = featureNode;
                    i++;
                }
                for (int classID : way.getClassIDs()) {
                    trainingSetWithUnknown2[k] = featureNodeArray;
                    GROUPS_ARRAY2[k] = classID;
                    k++;
                }
            }
        }
        Problem problem = new Problem();
        problem.l = wayListSizeWithoutUnclassified+sizeToBeAddedToArray;
        problem.n = numberOfFeatures;
        //3797; // number of features //the largest index of all features //3811;//3812 //1812 with classes
        problem.x = trainingSetWithUnknown2; // feature nodes
        problem.y = GROUPS_ARRAY2; // target values
        SolverType solver2 = SolverType.getById(2); //2 -- L2-regularized L2-loss support vector classification (primal)

        Parameter parameter = new Parameter(solver2, C, eps);

        long start = System.nanoTime();
        System.out.println("training...");
        PrintStream original = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {

            }
        }));


        Model model = Linear.train(problem, parameter);
        long end = System.nanoTime();
        Long elapsedTime = end-start;
        System.setOut(original);
        System.out.println("training process completed in: " + NANOSECONDS.toSeconds(elapsedTime) + " seconds.");

        //decide model path and naming and/or way of deleting/creating 1 or more models.

        File modelFile = new File(modelDirectory.getAbsolutePath()+"/best_model"); //decide path of models

        File customModelFile;
        if (topKIsSelected) {
            customModelFile = new File(modelDirectory.getAbsolutePath()+"/" +
                    inputFileName + "_model_c" + param + "_topK" + topK + "user" + username + ".0");
        } else {
            customModelFile = new File(modelDirectory.getAbsolutePath()+"/" +
                    inputFileName + "_model_c" + param + "_maxF" + frequency + "user" + username + ".0");
        }

        if (modelFile.exists()) {
            modelFile.delete();
        }
        if (customModelFile.exists()) {
            customModelFile.delete();
        }
        try {
            model.save(modelFile);
            model.save(customModelFile);
            System.out.println("best model saved at: " + modelFile);
            System.out.println("custom model saved at: " + customModelFile);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void trainModelWithClasses(double param) {

        int wayListSizeWithoutUnclassified = wayList.size();
        System.out.println("trainList size: " + wayListSizeWithoutUnclassified);

        if (wayListSizeWithoutUnclassified == 0) {
            System.out.println("aborting training process with classes..");
            return;
        }
        //set classes for each osm instance
        int sizeToBeAddedToArray = 0; //this will be used to proper init the features array, adding the multiple vectors size
        for (OSMWay way : wayList) {
            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

            if (way.getClassIDs().isEmpty()) {
                wayListSizeWithoutUnclassified -= 1;
            } else {
                sizeToBeAddedToArray = sizeToBeAddedToArray + way.getClassIDs().size()-1;
            }
        }
        double C = param;
        double eps = 0.001;
        double[] GROUPS_ARRAY2 = new double[wayListSizeWithoutUnclassified+sizeToBeAddedToArray];
        FeatureNode[][] trainingSetWithUnknown2 = new FeatureNode[wayListSizeWithoutUnclassified+sizeToBeAddedToArray][numberOfFeatures+1422];
        int k = 0;

        for (OSMWay way : wayList) {
            //adding multiple vectors
            int id;
            ClassFeatures class_vector = new ClassFeatures();
            class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
            id = 1422;
            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            if (USE_RELATION_FEATURES) {
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);
                id = relationFeatures.getLastID();
            } else {
                id = geometryFeatures.getLastID();
            }
            //id 1531

            TextualFeatures textualFeatures;
            if (USE_TEXTUAL_FEATURES) {
                textualFeatures = new TextualFeatures(id, namesList, languageDetector);
                textualFeatures.createTextualFeatures(way);
            }

            List<FeatureNode> featureNodeList = way.getFeatureNodeList();
            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];

            if (!way.getClassIDs().isEmpty()) {
                int i = 0;
                for (FeatureNode featureNode : featureNodeList) {
                    featureNodeArray[i] = featureNode;
                    i++;
                }
                for (int classID : way.getClassIDs()) {
                    trainingSetWithUnknown2[k] = featureNodeArray;
                    GROUPS_ARRAY2[k] = classID;
                    k++;
                }
            }
        }

        Problem problem = new Problem();
        problem.l = wayListSizeWithoutUnclassified+sizeToBeAddedToArray;
        problem.n = numberOfFeatures+1422;
        //3797; // number of features //the largest index of all features //3811;//3812 //1812 with classes
        problem.x = trainingSetWithUnknown2; // feature nodes
        problem.y = GROUPS_ARRAY2; // target values
        SolverType solver2 = SolverType.getById(2); //2 -- L2-regularized L2-loss support vector classification (primal)

        Parameter parameter = new Parameter(solver2, C, eps);

        long start = System.nanoTime();
        System.out.println("training...");
        PrintStream original = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {

            }
        }));

        Model model = Linear.train(problem, parameter);
        long end = System.nanoTime();
        Long elapsedTime = end-start;
        System.setOut(original);
        System.out.println("training process completed in: " + NANOSECONDS.toSeconds(elapsedTime) + " seconds.");

        //decide model path and naming and/or way of deleting/creating 1 or more models.

        File modelFile = new File(modelDirectory.getAbsolutePath()+"/model_with_classes");
        File customModelFile;
        if (topKIsSelected) {
            customModelFile = new File(modelDirectory.getAbsolutePath()+"/" +
                    inputFileName + "_model_c" + param + "_topK" + topK + "user" + username + ".1");
        } else {
            customModelFile = new File(modelDirectory.getAbsolutePath()+"/" +
                    inputFileName + "_model_c" + param + "_maxF" + frequency + "user" + username + ".1");
        }

        if (customModelFile.exists()) {
            customModelFile.delete();
        }
        if (modelFile.exists()) {
            modelFile.delete();
        }
        try {
            model.save(modelFile);
            model.save(customModelFile);
            System.out.println("model with classes saved at: " + modelFile);
            System.out.println("custom model with classes saved at: " + customModelFile);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void done() {
        try {
            get();
            firePropertyChange("progress", getProgress(), 100);
            setProgress(100);
        } catch (InterruptedException | ExecutionException ignore) {
            System.out.println("Exception: " + ignore);
        }
    }
}
