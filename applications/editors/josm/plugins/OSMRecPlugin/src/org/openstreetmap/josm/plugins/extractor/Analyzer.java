package org.openstreetmap.josm.plugins.extractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

/**
 * Analyzes textual information. Languager detection, stop words removal, stemming based on language.
 * Provides methods for retrieving the textual list by frequency and top-K terms.
 * @author imis-nkarag
 */

public class Analyzer {
    
    private final String osmFilePath;
    private static final HashSet<String> stopWordsList = new HashSet<>(); //add greek list to same file
    private ArrayList<Entry<String, Integer>> frequencies;
    private final LanguageDetector languageDetector;
    
    public Analyzer(String osmFilePath, LanguageDetector languageDetector){
        this.osmFilePath = osmFilePath;
        this.languageDetector = languageDetector;
    }
    
    public void runAnalysis() {
        //textual list
        FrequenceExtractor frequenceExtractor = new FrequenceExtractor(osmFilePath);
        frequenceExtractor.parseDocument();
        Set<Map.Entry<String, Integer>> frequencyEntries = frequenceExtractor.getFrequency().entrySet();
        
        //parse stop words
        loadStopWords();
       
        //send some samples
        ArrayList<Map.Entry<String, Integer>> normalizedList = new ArrayList<>();
        ArrayList<String> sampleList = new ArrayList<>();
        int iters = 0;  
        for(Map.Entry<String, Integer> frequencyEntry : frequencyEntries){
            if(iters <10){
                sampleList.add(frequencyEntry.getKey());
                iters++;
            }
            //remove parenthesis etc here
            if(!stopWordsList.contains(frequencyEntry.getKey())){//stopwords
                String normalizedName = frequencyEntry.getKey().toLowerCase();
                normalizedName = normalizedName.replaceAll("[-+.^:,?;'{}\"!()\\[\\]]", "");

                AbstractMap.SimpleEntry<String, Integer> normalizedEntry = new AbstractMap.SimpleEntry<>(normalizedName,frequencyEntry.getValue());                       
                normalizedList.add(normalizedEntry);
            }
        }

        int en=0;
        int el=0;
        for(String word : sampleList){
            //System.out.println("to be detected: " + word);
            if(!word.isEmpty()){
                String lang;
                lang = languageDetector.detect(word);
                switch (lang) {
                    case "en":
                        en++;
                        break;
                    case "el":
                        el++;
                        break;
                        //other lang, no support yet
                        //System.out.println("found other language, no support yet :(");
                    default:
                        break;
                }
            }
        }
        if(el>en){
            //language = "el"; 
            normalizedList = stemGreek(normalizedList);
        }
        else{
            //language = "en";
            normalizedList = stemEnglish(normalizedList);
        }
        
        //List<Entry<String, Integer>> list = new ArrayList<>(set);
        Collections.sort( normalizedList, new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );
        
        setFrequencies(normalizedList);
    }
    
    private ArrayList<Map.Entry<String, Integer>> stemGreek(ArrayList<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer greekAnalyzer = new GreekAnalyzer(Version.LUCENE_36);
        QueryParser greekParser = new QueryParser(Version.LUCENE_36, "", greekAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : normalizedList){
            if(!entry.getKey().isEmpty()){
                try {
                    //System.out.println("result: " + greekParser.parse(entry.getKey())); 
                    String stemmedWord = greekParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    stemmedList.add(stemmed);
                    
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }  
        return stemmedList;
    }
    
    private ArrayList<Map.Entry<String, Integer>> stemEnglish(ArrayList<Map.Entry<String, Integer>> normalizedList){
        org.apache.lucene.analysis.Analyzer englishAnalyzer = new EnglishAnalyzer(Version.LUCENE_36);
        QueryParser englishParser = new QueryParser(Version.LUCENE_36, "", englishAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : normalizedList){
            if(!entry.getKey().isEmpty()){
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey())); 
                    String stemmedWord = englishParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    stemmedList.add(stemmed);
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }  
        return stemmedList;
    }
    
    private void loadStopWords(){
        //parse stopwordsList
        InputStream fstream = Analyzer.class.getResourceAsStream("/resources/files/stopWords.txt");
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream))) {
            String strLine;

            while ((strLine = br.readLine()) != null)   {
                //System.out.println (strLine);
                stopWordsList.add(strLine);
            }
            //System.out.println(stopWordsList.size());

        } catch (IOException ex) {
        Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        //return stopWordsList;
    }
    
    private void setFrequencies(ArrayList<Map.Entry<String, Integer>> frequencies){
        this.frequencies = frequencies;
        
    }
    
    public List<Map.Entry<String, Integer>> getFrequencies(){       
        return frequencies;
    }
    
    public List<Map.Entry<String, Integer>> getTopKMostFrequent(int topK){
        return frequencies.subList(0, topK);
    }
    
    public List<Map.Entry<String, Integer>> getWithFrequency(int minFrequency){
        ArrayList<Map.Entry<String, Integer>> withFrequency = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : frequencies){
            if(entry.getValue()> minFrequency){
                withFrequency.add(entry);
            }
            else{
                return withFrequency;
            }
        }
        return withFrequency;
    }
}