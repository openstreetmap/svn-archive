package org.openstreetmap.josm.plugins.osmrec.personalization;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.josm.plugins.container.OSMRelation;
import org.openstreetmap.josm.plugins.container.OSMWay;
import org.openstreetmap.josm.plugins.extractor.Analyzer;
import org.openstreetmap.josm.plugins.extractor.LanguageDetector;
import org.openstreetmap.josm.plugins.features.ClassFeatures;
import org.openstreetmap.josm.plugins.features.GeometryFeatures;
import org.openstreetmap.josm.plugins.features.OSMClassification;
import org.openstreetmap.josm.plugins.features.RelationFeatures;
import org.openstreetmap.josm.plugins.features.TextualFeatures;
import org.openstreetmap.josm.plugins.parsers.Mapper;
import org.openstreetmap.josm.plugins.parsers.Ontology;
import org.openstreetmap.josm.plugins.parsers.TextualStatistics;

/**
 *  Trains a model by the history of given user.
 * 
 *  @author imis-nkarag
 */

public class TrainByUser {
    
    private final String inputFilePath;
    private static Map<String,String> mappings;
    private static Map<String,Integer> mapperWithIDs;
    private static List<OSMWay> wayList;
    private static List<OSMRelation> relationList;
    
    private static Map<String, List<String>> indirectClasses;
    private static Map<String, Integer> indirectClassesWithIDs;
    //private static List<OntClass> listHierarchy;
    private static List<String> namesList;       
    private int numberOfTrainingInstances;
    private final String modelDirectoryPath;
    private final File modelDirectory;
    private static double score1 = 0;
    private static double score5 = 0;
    private static double score10 = 0;
    private static double foldScore1 = 0;
    private static double foldScore5 = 0;
    private static double foldScore10 = 0;
    private static double bestScore = 0;
    int trainProgress = 0;
    private final boolean validateFlag;
    private final double cParameterFromUser;
    private double bestConfParam;
    private final int topK;
    private final int frequency;
    private final boolean topKIsSelected;
    private String textualListFilePath;    
    private static boolean USE_CLASS_FEATURES = false;
    private static boolean USE_RELATION_FEATURES = false;
    private static boolean USE_TEXTUAL_FEATURES = true;
    private static int numberOfFeatures;
    private static LanguageDetector languageDetector;
    private final String username;
        
    public TrainByUser(String inputFilePath, String username, boolean validateFlag, double cParameterFromUser, 
            int topK, int frequency, boolean topKIsSelected, LanguageDetector languageDetector, List<OSMWay> wayList) {
        super();
        TrainByUser.languageDetector = languageDetector;        
        this.inputFilePath = inputFilePath; 
        this.username = username;
        this.validateFlag = validateFlag;
        this.cParameterFromUser = cParameterFromUser;
        this.topK = topK;
        this.frequency = frequency;
        this.topKIsSelected = topKIsSelected;
        System.out.println("find parent directory, create osmrec dir for models: " + new File(inputFilePath).getParentFile());
        modelDirectoryPath = new File(inputFilePath).getParentFile() + "/OSMRec_models";
        //textualDirectoryPath = new File(inputFilePath).getParentFile() + "/OSMRec_models/textualList.txt";
        
        modelDirectory = new File(modelDirectoryPath);
        
        if (!modelDirectory.exists()) {
            modelDirectory.mkdir();
            System.out.println("model directory created!");
        }
        else {
            System.out.println("directory already exists!");
        }   
        TrainByUser.wayList = wayList;
    }
    
    
    public void executeTraining(){        
        extractTextualList();
        parseFiles();
        if(validateFlag){            
            validateLoop();
            System.out.println("Training model with the best c: " + bestConfParam);
            clearDataset();
            trainModel(bestConfParam);
            clearDataset();
            trainModelWithClasses(bestConfParam);
            
        }
        else{
            clearDataset();
            trainModel(cParameterFromUser);
            clearDataset();
            trainModelWithClasses(cParameterFromUser);
            System.out.println("done.");
        }
        System.out.println("Train by user process complete.");       
    }
    
    private void extractTextualList(){
        System.out.println("Running analysis..");
        //provide top-K
        //Keep the top-K most frequent terms
        //Keep terms with frequency higher than N
        //Use the remaining terms as training features
        
        Analyzer anal = new Analyzer(inputFilePath, languageDetector);
        anal.runAnalysis();

        textualListFilePath = modelDirectory.getAbsolutePath()+"/textualList.txt";
        File textualFile = new File(textualListFilePath); //decide path of models
        if(textualFile.exists()){
            textualFile.delete();
        }        
        try {
            textualFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        }           
        
        //writeTextualListToFile(textualFilePath, anal.getTopKMostFrequent(15));
        List<Map.Entry<String, Integer>> textualList;
        if(topKIsSelected){
            textualList = anal.getTopKMostFrequent(topK);
            System.out.println(textualList);
        }
        else{
            textualList = anal.getWithFrequency(frequency);
            System.out.println(textualList);
        }

        writeTextualListToFile(textualListFilePath, textualList);
        System.out.println("textual list saved at location:\n" + textualListFilePath);
        //write list to file and let parser do the loading from the names file
        
        //method read default list
        //method extract textual list - > the list will be already in memory, so the names parser doesn t have to be called
        if(USE_CLASS_FEATURES){
            numberOfFeatures = 1422 + 105 + textualList.size();
        }
        else{          
            numberOfFeatures = 105 + textualList.size();
        }
    }  
    
    private void parseFiles() {
       // File tagsToClassesFile = new File(tagsToClassesFilePath);        

        //osmRecProperties.load(Train.class.getResourceAsStream("/resources/properties/osmRec.properties"));
        InputStream tagsToClassesMapping = TrainByUser.class.getResourceAsStream("/resources/files/Map");

        Mapper mapper = new Mapper();
        try {   
            mapper.parseFile(tagsToClassesMapping);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        mappings = mapper.getMappings();
        mapperWithIDs = mapper.getMappingsWithIDs(); 
        //System.out.println("success. mappings empty? " + mappings.isEmpty());
        
        InputStream ontologyStream = TrainByUser.class.getResourceAsStream("/resources/files/owl.xml");
        Ontology ontology = new Ontology(ontologyStream);            

        ontology.parseOntology();
        System.out.println("ontology parsed ");
        indirectClasses = ontology.getIndirectClasses();
        indirectClassesWithIDs = ontology.getIndirectClassesIDs();
        //listHierarchy = ontology.getListHierarchy();        

        //InputStream textualFileStream = TrainWorker.class.getResourceAsStream("/resources/files/names");     
        InputStream textualFileStream = null;  
        try {
            textualFileStream = new FileInputStream(new File(textualListFilePath));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        readTextualFromDefaultList(textualFileStream);      
        
        //osmParser.parseDocument();
        //relationList = osmParser.getRelationList();
        //wayList = osmParser.getWayList();
        numberOfTrainingInstances = wayList.size();
        System.out.println("number of instances: " + numberOfTrainingInstances);   
        System.out.println("end of parsing files."); 
        
    }

    private void validateLoop(){
//        Double[] confParams = new Double[] {Math.pow(2, -10), Math.pow(2, -5),Math.pow(2, -3),Math.pow(2, -1),
//            Math.pow(2, 0),Math.pow(2, 1),Math.pow(2, 2),Math.pow(2, 3),Math.pow(2, 4),Math.pow(2, 5),Math.pow(2, 6),
//            Math.pow(2, 8),Math.pow(2, 10),Math.pow(2, 12),Math.pow(2, 14),Math.pow(2, 15),Math.pow(2, 16),Math.pow(2, 17)};
        
        Double[] confParams = new Double[] {Math.pow(2, -10),Math.pow(2, -10), Math.pow(2, -5),Math.pow(2, -3),Math.pow(2, -1),Math.pow(2, 0)};        
                
        double bestC = Math.pow(2, -10);
        
        for(Double param : confParams){
            
            foldScore1 = 0;
            foldScore5 = 0;
            foldScore10 = 0;
            System.out.println("\n\n\nrunning for C = " + param);
            clearDataset();
            System.out.println("fold1");
            //crossValidateFold(0, 3, 3, 4, false, param);
            crossValidateFold(0, 4, 4, 5, false, param); //4-1
            //setProgress(4*((5*(trainProgress++))/confParams.length)); 
            //System.out.println((5*trainProgress)/confParams.length);
            
            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold2");
            //crossValidateFold(1, 4, 4, 5, false, param);
            crossValidateFold(1, 5, 0, 1, false, param);
            //setProgress(4*((5*(trainProgress++))/confParams.length));
            //System.out.println((5*trainProgress)/confParams.length);
            
            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold3");
            //crossValidateFold(2, 5, 0, 1, false, param);
            crossValidateFold(0, 5, 1, 2, true, param);
            //setProgress(4*((5*(trainProgress++))/confParams.length));
            //System.out.println((5*trainProgress)/confParams.length);
            
            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold4");
            //crossValidateFold(0, 5, 1, 2, true, param); 
            crossValidateFold(0, 5, 2, 3, true, param);
            //setProgress(4*((5*(trainProgress++))/confParams.length));
            //System.out.println((5*trainProgress)/confParams.length);
            
            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            clearDataset();
            System.out.println("fold5");
            //crossValidateFold(0, 5, 2, 3, true, param);
            crossValidateFold(0, 5, 3, 4, true, param);
            //setProgress(4*((5*(trainProgress++))/confParams.length));
            //System.out.println((5*trainProgress)/confParams.length);
            
            foldScore1 = foldScore1 + score1;
            foldScore5 = foldScore5 + score5;
            foldScore10 = foldScore10 + score10;
            System.out.println("\n\nC=" + param + ", average score 1-5-10: " + foldScore1/5 +" "+ foldScore5/5 + " "+ foldScore10/5);
            if(bestScore < foldScore1 ){
                bestScore = foldScore1;
                bestC = param;
            }
            
        }
        System.out.println(4*((5*(trainProgress++))/confParams.length));
        //setProgress(100);
        bestConfParam = bestC;
        System.out.println("best c param= " + bestC + ", score: " + bestScore/5 );
    }
    
    private void crossValidateFold(int a, int b, int c, int d, boolean skip, double param){
        System.out.println("Starting cross validation");
        int testSize = wayList.size()/5;

        List<OSMWay> trainList= new ArrayList<>();
        for(int g = a*testSize; g<b*testSize; g++){  // 0~~1~~2~~3~~4~~5
            if(skip){
                if(g == (c)*testSize){g=(c+1)*testSize;}
            
            }
            trainList.add(wayList.get(g)); 
        }  

        int wayListSizeWithoutUnclassified = trainList.size();
        int u = 0;
        System.out.println("trainList size: " + wayListSizeWithoutUnclassified);
        
        //set classes for each osm instance
        int sizeToBeAddedToArray = 0; //this will be used to proper init the features array, adding the multiple vectors size 
        int lalala = 0;
        for(OSMWay way : trainList){

            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

            if(way.getClassIDs().isEmpty()){
                wayListSizeWithoutUnclassified = wayListSizeWithoutUnclassified-1;
                u++;
            }
            else {
                sizeToBeAddedToArray = sizeToBeAddedToArray + way.getClassIDs().size()-1;
            }                
        } 
        double C = param;        
        double eps = 0.001;
        double[] GROUPS_ARRAY2 = new double[wayListSizeWithoutUnclassified+sizeToBeAddedToArray];//new double[117558];//                
        FeatureNode[][] trainingSetWithUnknown2 = new FeatureNode[wayListSizeWithoutUnclassified+sizeToBeAddedToArray][numberOfFeatures]; //working[3812];
        int k =0;    
        
        for(OSMWay way : trainList){
            //adding multiple vectors                     
            int id;           
            if(USE_CLASS_FEATURES){                
                ClassFeatures class_vector = new ClassFeatures();
                class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                id = 1422;
            }
            else{
                id = 1;
            }
            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            if(USE_RELATION_FEATURES){
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);    
                id = relationFeatures.getLastID();
            }
            else {
                id = geometryFeatures.getLastID();
            }
            //id 1531
            
            TextualFeatures textualFeatures;
            if(USE_TEXTUAL_FEATURES){
                textualFeatures = new TextualFeatures(id, namesList, languageDetector);
                textualFeatures.createTextualFeatures(way);
            }
           
            List<FeatureNode> featureNodeList = way.getFeatureNodeList();
            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];
                        
            if(!way.getClassIDs().isEmpty()){
                int i =0;
                for(FeatureNode featureNode : featureNodeList){
                    featureNodeArray[i] = featureNode;
                    i++;
                }               
                for(int classID : way.getClassIDs()){
                    lalala++;
                    trainingSetWithUnknown2[k] = featureNodeArray;
                    GROUPS_ARRAY2[k] = classID;
                    k++;                    
                }                                
            }           
        }
        
        //Linear.enableDebugOutput();
        Problem problem = new Problem();  
        problem.l = wayListSizeWithoutUnclassified+sizeToBeAddedToArray;//wayListSizeWithoutUnclassified;//wayList.size();
        problem.n = numberOfFeatures; //(geometry 105 + textual        //3797; // number of features //the largest index of all features //3811;//3812 //1812 with classes
        problem.x = trainingSetWithUnknown2; // feature nodes
        problem.y = GROUPS_ARRAY2; // target values
        //SolverType solver = SolverType.MCSVM_CS; //Cramer and Singer for multiclass classification - equivalent of SVMlight
        SolverType solver2 = SolverType.getById(2); //2 -- L2-regularized L2-loss support vector classification (primal)
  
        Parameter parameter = new Parameter(solver2, C, eps);
        //System.out.println("param set ok");
        //System.out.println("number of features: " + vc.getNumOfFeatures());       
        
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
        //System.out.println("trained");
        
        //decide model path and naming and/or way of deleting/creating 1 or more models. 
        //File inFile = new File(inputFilePath).getParentFile();
        
        File modelFile;
        if(USE_CLASS_FEATURES){
            modelFile = new File(modelDirectory.getAbsolutePath()+"/user_" + username + "_model_with_classes_c=" + param);
        }
        else{
            modelFile = new File(modelDirectory.getAbsolutePath()+"/user__" + username + "_model_geometries_textual_c=" + param);
        }
  
        if(modelFile.exists()){
            modelFile.delete();
        }        
        try {
            //System.out.println("file created");
            model.save(modelFile);
            //System.out.println("saved"); 
        } catch (IOException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        }

        //test set 
        List<OSMWay> testList= new ArrayList<>();
        for(int g = c*testSize; g<d*testSize; g++){
            testList.add(wayList.get(g));
            //liblinear test                       
        }
        System.out.println("testList size: " + testList.size());
        int succededInstances=0;
        int succededInstances5=0;
        int succededInstances10=0;
        try {
            model = Model.load(modelFile);
        } catch (IOException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        }
        int modelLabelSize = model.getLabels().length;
        int[] labels = model.getLabels();
        Map<Integer, Integer> mapLabelsToIDs = new HashMap<>();
        for(int h =0; h < model.getLabels().length; h++){
            mapLabelsToIDs.put(labels[h], h);
            //System.out.println(h + "   <->    " + labels[h]);
        }
        
        int wayListSizeWithoutUnclassified2 = testList.size();
        for(OSMWay way : testList){

            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
            if(way.getClassIDs().isEmpty()){
                //System.out.println("found unclassified" + way.getClassIDs() + "class: " +way.getClassID());
                wayListSizeWithoutUnclassified2 = wayListSizeWithoutUnclassified2-1;
                //u++;
            }
        }         
        
        FeatureNode[] testInstance2;
        for(OSMWay way : testList){
            
            int id;
            
            if(USE_CLASS_FEATURES){                
                ClassFeatures class_vector = new ClassFeatures();
                class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                id = 1422;
            }
            else{
                id = 1;
            }
            
            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            //System.out.println("id 1526 -> " + geometryFeatures.getLastID());
            if(USE_RELATION_FEATURES){
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);       
                id = relationFeatures.getLastID();
            }
            else {
                id = geometryFeatures.getLastID();
                //System.out.println("geom feat " + id);
            }
            //id 1531
            //System.out.println("id 1532 -> " + relationFeatures.getLastID());
            if(USE_TEXTUAL_FEATURES){
                TextualFeatures textualFeatures = new TextualFeatures(id, namesList, languageDetector);
                textualFeatures.createTextualFeatures(way);
                //System.out.println("last textual id: " + textualFeatures.getLastID());
                //System.out.println("full:  " + way.getFeatureNodeList());
            }
            else{
                
            }
            List<FeatureNode> featureNodeList = way.getFeatureNodeList();

            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];

            int i =0;
            for(FeatureNode featureNode : featureNodeList){
                featureNodeArray[i] = featureNode;
                i++;
            }

            testInstance2 = featureNodeArray;
            //double prediction = Linear.predict(model, testInstance2);
            //System.out.println("test prediction: " + prediction);
            double[] scores = new double[modelLabelSize];
            Linear.predictValues(model, testInstance2, scores);

            //find index of max values in scores array: predicted classes are the elements of these indexes from array model.getlabels
            //iter scores and find 10 max values with their indexes first. then ask those indexes from model.getlabels
            Map<Double, Integer> scoresValues = new HashMap<>();
            for(int h = 0; h < scores.length; h++){
                scoresValues.put(scores[h], h);                
                //System.out.println(h + "   <->    " + scores[h]);
            }          

            Arrays.sort(scores);
            //System.out.println("max value: " + scores[scores.length-1] + " second max: " + scores[scores.length-2]);
            //System.out.println("ask this index from labels: " + scoresValues.get(scores[scores.length-1]));
            //System.out.println("got from labels: " +  labels[scoresValues.get(scores[scores.length-1])]);
            //System.out.println("next prediction: " +  labels[scoresValues.get(scores[scores.length-2])]);
            //System.out.println("way labels: " + way.getClassIDs());
            //System.out.println("test prediction: " + prediction);
            if(way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-1])])){
                succededInstances++;              
            }
            if(
                    
                way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-1])]) || 
                way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-2])]) ||
                way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-3])]) ||
                way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-4])]) ||
                way.getClassIDs().contains(labels[scoresValues.get(scores[scores.length-5])])
              ){
                succededInstances5++;
            }
            if(
                    
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
              ){
                succededInstances10++;
            } 
            //System.out.println("labels: " + Arrays.toString(model.getLabels()));
            //System.out.println("label[0]: " + model.getLabels()[0]);            
        }        
        
        System.out.println("Succeeded " + succededInstances + " of " + testList.size() + " total (1 class prediction)");
        double precision1 = (double)succededInstances/(double)wayListSizeWithoutUnclassified2;
        score1 = precision1;
        System.out.println(precision1);
        
        System.out.println("Succeeded " + succededInstances5 + " of " + testList.size()+ " total (5 class prediction)");
        double precision5 = (double)succededInstances5/(double)wayListSizeWithoutUnclassified2;
        score5 = precision5;
        System.out.println(precision5);
        
        System.out.println("Succeeded " + succededInstances10 + " of " + testList.size()+ " total (10 class prediction)");
        double precision10 = (double)succededInstances10/(double)wayListSizeWithoutUnclassified2;
        score10 = precision10;
        System.out.println(precision10);               
    }  
    
    private void trainModel(double param){
        
        int wayListSizeWithoutUnclassified = wayList.size();
        int u = 0;
        System.out.println("trainList size: " + wayListSizeWithoutUnclassified);
        
        //set classes for each osm instance
        int sizeToBeAddedToArray = 0; //this will be used to proper init the features array, adding the multiple vectors size 
        int lalala = 0;

        //System.out.println("starting classify instances");
        for(OSMWay way : wayList){
            //setProgress(trainProgress++);
            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

            if(way.getClassIDs().isEmpty()){
                wayListSizeWithoutUnclassified = wayListSizeWithoutUnclassified-1;
                u++;
            }
            else {
                sizeToBeAddedToArray = sizeToBeAddedToArray + way.getClassIDs().size()-1;
            }                
        } 
        //System.out.println("end classify instances");
        double C = param;        
        double eps = 0.001;
        double[] GROUPS_ARRAY2 = new double[wayListSizeWithoutUnclassified+sizeToBeAddedToArray];//new double[117558];//                
        FeatureNode[][] trainingSetWithUnknown2 = new FeatureNode[wayListSizeWithoutUnclassified+sizeToBeAddedToArray][numberOfFeatures]; //working[3812];
        int k =0; 
        
        //setProgress(trainProgress+5);
        for(OSMWay way : wayList){
            //adding multiple vectors                     
            int id;           
            if(USE_CLASS_FEATURES){                
                ClassFeatures class_vector = new ClassFeatures();
                class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                id = 1422;
            }
            else{
                id = 1;
            }
            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            if(USE_RELATION_FEATURES){
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);    
                id = relationFeatures.getLastID();
            }
            else {
                id = geometryFeatures.getLastID();
            }
            //id 1531
            
            TextualFeatures textualFeatures;
            if(USE_TEXTUAL_FEATURES){
                textualFeatures = new TextualFeatures(id, namesList,languageDetector);
                textualFeatures.createTextualFeatures(way);
            }
           
            List<FeatureNode> featureNodeList = way.getFeatureNodeList();
            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];
                        
            if(!way.getClassIDs().isEmpty()){
                int i =0;
                for(FeatureNode featureNode : featureNodeList){
                    featureNodeArray[i] = featureNode;
                    i++;
                }               
                for(int classID : way.getClassIDs()){
                    lalala++;
                    trainingSetWithUnknown2[k] = featureNodeArray;
                    GROUPS_ARRAY2[k] = classID;
                    k++;                    
                }                                
            }           
        } 
        //System.out.println("feature construction completed.");
        //Linear.enableDebugOutput();
        Problem problem = new Problem();  
        problem.l = wayListSizeWithoutUnclassified+sizeToBeAddedToArray;//wayListSizeWithoutUnclassified;//wayList.size();
        problem.n = numberOfFeatures;//3797; // number of features //the largest index of all features //3811;//3812 //1812 with classes
        problem.x = trainingSetWithUnknown2; // feature nodes
        problem.y = GROUPS_ARRAY2; // target values
        //SolverType solver = SolverType.MCSVM_CS; //Cramer and Singer for multiclass classification - equivalent of SVMlight
        SolverType solver2 = SolverType.getById(2); //2 -- L2-regularized L2-loss support vector classification (primal)
  
        Parameter parameter = new Parameter(solver2, C, eps);
        //System.out.println("param set ok");
        //System.out.println("number of features: " + vc.getNumOfFeatures());       
        
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
        //System.out.println("trained");
        
        //decide model path and naming and/or way of deleting/creating 1 or more models. 
        //File inFile = new File(inputFilePath).getParentFile();
        
        File modelFile = new File(modelDirectory.getAbsolutePath()+"/best_model"); //decide path of models
        if(modelFile.exists()){
            modelFile.delete();
        }        
        try {
            //System.out.println("file created");
            model.save(modelFile);
            //System.out.println("saved"); 
        } catch (IOException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    private void trainModelWithClasses(double param){
        
        int wayListSizeWithoutUnclassified = wayList.size();
        int u = 0;
        System.out.println("trainList size: " + wayListSizeWithoutUnclassified);
        
        //set classes for each osm instance
        int sizeToBeAddedToArray = 0; //this will be used to proper init the features array, adding the multiple vectors size 
        //int lalala = 0;
        //setProgress(trainProgress-10);
        for(OSMWay way : wayList){
            //setProgress(trainProgress++);
            OSMClassification classifyInstances = new OSMClassification();
            classifyInstances.calculateClasses(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);

            if(way.getClassIDs().isEmpty()){
                wayListSizeWithoutUnclassified = wayListSizeWithoutUnclassified-1;
                u++;
            }
            else {
                sizeToBeAddedToArray = sizeToBeAddedToArray + way.getClassIDs().size()-1;
            }                
        } 
        double C = param;        
        double eps = 0.001;
        double[] GROUPS_ARRAY2 = new double[wayListSizeWithoutUnclassified+sizeToBeAddedToArray];//new double[117558];//                
        FeatureNode[][] trainingSetWithUnknown2 = new FeatureNode[wayListSizeWithoutUnclassified+sizeToBeAddedToArray][numberOfFeatures+1422]; //working[3812];
        int k =0; 
        
        //setProgress(trainProgress+5);
        for(OSMWay way : wayList){
            //adding multiple vectors                     
            int id;           
            //if(USE_CLASS_FEATURES){                
                ClassFeatures class_vector = new ClassFeatures();
                class_vector.createClassFeatures(way, mappings, mapperWithIDs, indirectClasses, indirectClassesWithIDs);
                id = 1422;
//            }
//            else{
//                id = 1;
//            }
            //pass id also: 1422 if using classes, 1 if not
            GeometryFeatures geometryFeatures = new GeometryFeatures(id);
            geometryFeatures.createGeometryFeatures(way);
            id = geometryFeatures.getLastID();
            //id after geometry, cases: all geometry features with mean-variance boolean intervals:
            //id = 1526
            if(USE_RELATION_FEATURES){
                RelationFeatures relationFeatures = new RelationFeatures(id);
                relationFeatures.createRelationFeatures(way, relationList);    
                id = relationFeatures.getLastID();
            }
            else {
                id = geometryFeatures.getLastID();
            }
            //id 1531
            
            TextualFeatures textualFeatures;
            if(USE_TEXTUAL_FEATURES){
                textualFeatures = new TextualFeatures(id, namesList,languageDetector);
                textualFeatures.createTextualFeatures(way);
            }
           
            List<FeatureNode> featureNodeList = way.getFeatureNodeList();
            FeatureNode[] featureNodeArray = new FeatureNode[featureNodeList.size()];
                        
            if(!way.getClassIDs().isEmpty()){
                int i =0;
                for(FeatureNode featureNode : featureNodeList){
                    featureNodeArray[i] = featureNode;
                    i++;
                }               
                for(int classID : way.getClassIDs()){
                    //lalala++;
                    trainingSetWithUnknown2[k] = featureNodeArray;
                    GROUPS_ARRAY2[k] = classID;
                    k++;                    
                }                                
            }           
        } 
        
        //Linear.enableDebugOutput();
        Problem problem = new Problem();  
        problem.l = wayListSizeWithoutUnclassified+sizeToBeAddedToArray;//wayListSizeWithoutUnclassified;//wayList.size();
        problem.n = numberOfFeatures+1422;//3797; // number of features //the largest index of all features //3811;//3812 //1812 with classes
        problem.x = trainingSetWithUnknown2; // feature nodes
        problem.y = GROUPS_ARRAY2; // target values
        //SolverType solver = SolverType.MCSVM_CS; //Cramer and Singer for multiclass classification - equivalent of SVMlight
        SolverType solver2 = SolverType.getById(2); //2 -- L2-regularized L2-loss support vector classification (primal)
  
        Parameter parameter = new Parameter(solver2, C, eps);
        //System.out.println("param set ok");
        //System.out.println("number of features: " + vc.getNumOfFeatures());       
        
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
        //System.out.println("trained");
        
        //decide model path and naming and/or way of deleting/creating 1 or more models. 
        //File inFile = new File(inputFilePath).getParentFile();
        
        File modelFile = new File(modelDirectory.getAbsolutePath()+"/model_with_classes"); 
        if(modelFile.exists()){
            modelFile.delete();
        }        
        try {
            //System.out.println("file created");
            model.save(modelFile);
            //System.out.println("saved"); 
        } catch (IOException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private static void clearDataset(){
        for(OSMWay way : wayList){
            way.getFeatureNodeList().clear();
        }
    }  
    
    private void readTextualFromDefaultList(InputStream textualFileStream) {
                 
        TextualStatistics textualStatistics = new TextualStatistics();
        textualStatistics.parseTextualList(textualFileStream);
        namesList = textualStatistics.getTextualList();
  
    }

    private static void writeTextualListToFile(String filePath, List<Map.Entry<String, Integer>> textualList) 
    {
        
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8"))) {            
            for(Map.Entry<String, Integer> entry : textualList){
                writer.write(entry.getKey());
                writer.newLine();
                System.out.println(entry.getKey());
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TrainByUser.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
}
