// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.extractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

/**
 * Analyzes textual information. Language detection, stop words removal, stemming based on language.
 * Provides methods for retrieving the textual list by frequency and top-K terms.
 * @author imis-nkarag
 */

public class Analyzer {

    private final String osmFilePath;
    private static final HashSet<String> stopWordsList = new HashSet<>(); //add greek list to same file
    private ArrayList<Entry<String, Integer>> frequencies;
    private final LanguageDetector languageDetector;

    public Analyzer(String osmFilePath, LanguageDetector languageDetector) {
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
        for (Map.Entry<String, Integer> frequencyEntry : frequencyEntries) {
            if (iters < 10) {
                sampleList.add(frequencyEntry.getKey());
                iters++;
            }
            //remove parenthesis etc here
            if (!stopWordsList.contains(frequencyEntry.getKey())) {
                String normalizedName = frequencyEntry.getKey().toLowerCase();
                normalizedName = normalizedName.replaceAll("[-+.^:,?;'{}\"!()\\[\\]]", "");

                normalizedList.add(new AbstractMap.SimpleEntry<>(normalizedName, frequencyEntry.getValue()));
            }
        }

        Map<String, Integer> langs = new HashMap<>();
        langs.put("en", 0);
        langs.put("el", 0);
        langs.put("de", 0);
        langs.put("es", 0);
        langs.put("ru", 0);
        langs.put("hi", 0);
        langs.put("zh", 0);
        langs.put("tr", 0);
        langs.put("fr", 0);

        for (String word : sampleList) {
            //System.out.println("to be detected: " + word);
            if (!word.isEmpty()) {
                String lang;
                lang = languageDetector.detect(word);
                switch (lang) {
                case "en":
                    //en++;
                    langs.put("en", langs.get("en")+1);
                    break;
                case "el":
                    //el++;
                    langs.put("el", langs.get("el")+1);
                    break;
                case "de":
                    //de++;
                    langs.put("de", langs.get("de")+1);
                    break;
                case "es":
                    //es++;
                    langs.put("es", langs.get("es")+1);
                    break;
                case "ru":
                    //ru++;
                    langs.put("ru", langs.get("ru")+1);
                    break;
                case "fr":
                    //fr++;
                    langs.put("fr", langs.get("fr")+1);
                    break;
                case "zh":
                    //zh++;
                    langs.put("zh", langs.get("zh")+1);
                    break;
                case "tr":
                    //tr++;
                    langs.put("tr", langs.get("tr")+1);
                    break;
                case "hi":
                    //hi++;
                    langs.put("hi", langs.get("hi")+1);
                    break;
                    //other lang, no support yet
                    //System.out.println("found other language, no support yet :(");
                default:
                    break;
                }
            }
        }

        int maxLangFreq = langs.get("en");
        String dominantLanguage = "en";
        for (Entry<String, Integer> lang : langs.entrySet()) {
            if (lang.getValue() > maxLangFreq) {
                maxLangFreq = lang.getValue();
                dominantLanguage = lang.getKey();
            }
        }

        switch (dominantLanguage) {
        case "en":
            normalizedList = stemEnglish(normalizedList);
            break;
        case "el":
            normalizedList = stemGreek(normalizedList);
            break;
        case "de":
            normalizedList = stemGerman(normalizedList);
            break;
        case "es":
            normalizedList = stemSpanish(normalizedList);
            break;
        case "ru":
            normalizedList = stemRussian(normalizedList);
            break;
        case "fr":
            normalizedList = stemFrench(normalizedList);
            break;
        case "zh":
            normalizedList = stemChinese(normalizedList);
            break;
        case "tr":
            normalizedList = stemTurkish(normalizedList);
            break;
        case "hi":
            normalizedList = stemHindi(normalizedList);
            break;
        default:
            normalizedList = stemEnglish(normalizedList);
            break;
        }

        Collections.sort(normalizedList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        setFrequencies(normalizedList);
    }

    private static ArrayList<Map.Entry<String, Integer>> stemGreek(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer greekAnalyzer = new GreekAnalyzer(Version.LUCENE_36);
        QueryParser greekParser = new QueryParser(Version.LUCENE_36, "", greekAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + greekParser.parse(entry.getKey()));
                    String stemmedWord = greekParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemEnglish(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer englishAnalyzer = new EnglishAnalyzer(Version.LUCENE_36);
        QueryParser englishParser = new QueryParser(Version.LUCENE_36, "", englishAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = englishParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemGerman(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer germanAnalyzer = new GermanAnalyzer(Version.LUCENE_36);
        QueryParser germanParser = new QueryParser(Version.LUCENE_36, "", germanAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = germanParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemSpanish(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer spanishAnalyzer = new SpanishAnalyzer(Version.LUCENE_36);
        QueryParser spanishParser = new QueryParser(Version.LUCENE_36, "", spanishAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = spanishParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemRussian(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer russianAnalyzer = new RussianAnalyzer(Version.LUCENE_36);
        QueryParser russianParser = new QueryParser(Version.LUCENE_36, "", russianAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = russianParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemFrench(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer frenchAnalyzer = new FrenchAnalyzer(Version.LUCENE_36);
        QueryParser frenchParser = new QueryParser(Version.LUCENE_36, "", frenchAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = frenchParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemChinese(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer chineseAnalyzer = new StandardAnalyzer(Version.LUCENE_36);
        QueryParser chineseParser = new QueryParser(Version.LUCENE_36, "", chineseAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = chineseParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemTurkish(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer turkishAnalyzer = new TurkishAnalyzer(Version.LUCENE_36);
        QueryParser turkishParser = new QueryParser(Version.LUCENE_36, "", turkishAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = turkishParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private static ArrayList<Map.Entry<String, Integer>> stemHindi(List<Map.Entry<String, Integer>> normalizedList) {
        org.apache.lucene.analysis.Analyzer hindiAnalyzer = new HindiAnalyzer(Version.LUCENE_36);
        QueryParser hindiParser = new QueryParser(Version.LUCENE_36, "", hindiAnalyzer);
        ArrayList<Map.Entry<String, Integer>> stemmedList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : normalizedList) {
            if (!entry.getKey().isEmpty()) {
                try {
                    //System.out.println("result: " + englishParser.parse(entry.getKey()));
                    String stemmedWord = hindiParser.parse(entry.getKey()).toString();
                    SimpleEntry<String, Integer> stemmed = new SimpleEntry<>(stemmedWord, entry.getValue());
                    if (!stemmedWord.equals("")) {
                        stemmedList.add(stemmed);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return stemmedList;
    }

    private void loadStopWords() {
        //parse stopwordsList
        InputStream fstream = Analyzer.class.getResourceAsStream("/resources/files/stopWords.txt");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(fstream, StandardCharsets.UTF_8))) {
            String strLine;

            while ((strLine = br.readLine()) != null) {
                stopWordsList.add(strLine);
            }

        } catch (IOException ex) {
            Logger.getLogger(Analyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setFrequencies(ArrayList<Map.Entry<String, Integer>> frequencies) {
        this.frequencies = frequencies;
    }

    public List<Map.Entry<String, Integer>> getFrequencies() {
        return Collections.unmodifiableList(frequencies);
    }

    public List<Map.Entry<String, Integer>> getTopKMostFrequent(int topK) {
        //todo recheck
        if (topK > frequencies.size()) {
            return Collections.unmodifiableList(frequencies);
        } else {
            return frequencies.subList(0, topK);
        }
    }

    public List<Map.Entry<String, Integer>> getWithFrequency(int minFrequency) {
        ArrayList<Map.Entry<String, Integer>> withFrequency = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : frequencies) {
            if (entry.getValue() > minFrequency) {
                withFrequency.add(entry);
            } else {
                return withFrequency;
            }
        }
        return withFrequency;
    }
}
