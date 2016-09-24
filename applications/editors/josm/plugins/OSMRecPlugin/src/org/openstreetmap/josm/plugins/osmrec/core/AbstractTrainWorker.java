// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.openstreetmap.josm.plugins.osmrec.container.OSMRelation;
import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;
import org.openstreetmap.josm.plugins.osmrec.extractor.Analyzer;
import org.openstreetmap.josm.plugins.osmrec.extractor.LanguageDetector;
import org.openstreetmap.josm.plugins.osmrec.parsers.TextualStatistics;

public abstract class AbstractTrainWorker extends SwingWorker<Void, Void> implements ActionListener {

    protected final String inputFilePath;
    protected static Map<String, String> mappings;
    protected static Map<String, Integer> mapperWithIDs;

    protected static List<OSMWay> wayList;
    protected static List<OSMRelation> relationList;

    protected static Map<String, List<String>> indirectClasses;
    protected static Map<String, Integer> indirectClassesWithIDs;
    protected static List<String> namesList;
    protected int numberOfTrainingInstances;
    protected final String modelDirectoryPath;
    protected final File modelDirectory;
    protected static double score1;
    protected static double score5;
    protected static double score10;
    protected static double foldScore1;
    protected static double foldScore5;
    protected static double foldScore10;
    protected static double bestScore;
    protected final boolean validateFlag;
    protected final double cParameterFromUser;
    protected double bestConfParam;
    protected final int topK;
    protected final int frequency;
    protected final boolean topKIsSelected;
    protected String textualListFilePath;

    protected static final boolean USE_CLASS_FEATURES = false;
    protected static final boolean USE_RELATION_FEATURES = false;
    protected static final boolean USE_TEXTUAL_FEATURES = true;
    protected static int numberOfFeatures;
    protected static LanguageDetector languageDetector;
    protected final String inputFileName;

    protected AbstractTrainWorker(String inputFilePath, boolean validateFlag, double cParameterFromUser,
            int topK, int frequency, boolean topKIsSelected, LanguageDetector languageDetector) {
        AbstractTrainWorker.languageDetector = languageDetector;
        this.inputFilePath = inputFilePath;
        this.validateFlag = validateFlag;
        this.cParameterFromUser = cParameterFromUser;
        this.topK = topK;
        this.frequency = frequency;
        this.topKIsSelected = topKIsSelected;
        System.out.println("find parent directory, create osmrec dir for models: " + new File(inputFilePath).getParentFile());

        if (System.getProperty("os.name").contains("ux")) {
            inputFileName = inputFilePath.substring(inputFilePath.lastIndexOf('/'));
        } else {
            inputFileName = inputFilePath.substring(inputFilePath.lastIndexOf('\\'));
        }

        modelDirectoryPath = new File(inputFilePath).getParentFile() + "/OSMRec_models";

        modelDirectory = new File(modelDirectoryPath);

        if (!modelDirectory.exists()) {
            modelDirectory.mkdir();
        }
    }

    protected void extractTextualList() {
        System.out.println("Running analysis..");
        //provide top-K
        //Keep the top-K most frequent terms
        //Keep terms with frequency higher than N
        //Use the remaining terms as training features

        Analyzer anal = new Analyzer(inputFilePath, languageDetector);
        anal.runAnalysis();

        textualListFilePath = modelDirectory.getAbsolutePath()+"/textualList.txt";
        File textualFile = new File(textualListFilePath); //decide path of models
        if (textualFile.exists()) {
            textualFile.delete();
        }
        try {
            textualFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        List<Map.Entry<String, Integer>> textualList;
        if (topKIsSelected) {
            textualList = anal.getTopKMostFrequent(topK);
        } else {
            textualList = anal.getWithFrequency(frequency);
        }

        writeTextualListToFile(textualListFilePath, textualList);
        System.out.println("textual list saved at location:\n" + textualListFilePath);
        //write list to file and let parser do the loading from the names file

        //method read default list
        //method extract textual list - > the list will be already in memory, so the names parser doesn t have to be called
        if (USE_CLASS_FEATURES) {
            numberOfFeatures = 1422 + 105 + textualList.size(); //105 is number of geometry features
        } else {
            numberOfFeatures = 105 + textualList.size();
        }
    }

    protected static void clearDataset() {
        for (OSMWay way : wayList) {
            way.getFeatureNodeList().clear();
        }
    }

    protected void readTextualFromDefaultList(InputStream textualFileStream) {
        TextualStatistics textualStatistics = new TextualStatistics();
        textualStatistics.parseTextualList(textualFileStream);
        namesList = textualStatistics.getTextualList();
    }

    protected void writeTextualListToFile(String filePath, List<Map.Entry<String, Integer>> textualList) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            for (Map.Entry<String, Integer> entry : textualList) {
                writer.write(entry.getKey());
                writer.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // cancel button, end process after clearing Dataset
    }
}
