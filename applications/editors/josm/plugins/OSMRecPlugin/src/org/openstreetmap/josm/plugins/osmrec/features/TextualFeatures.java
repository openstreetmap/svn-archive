// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.features;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.openstreetmap.josm.plugins.osmrec.container.OSMWay;
import org.openstreetmap.josm.plugins.osmrec.extractor.LanguageDetector;

import com.cybozu.labs.langdetect.LangDetectException;

import de.bwaldvogel.liblinear.FeatureNode;

/**
 * Constructs the textual features from the given textual list.
 *
 * @author imis-nkarag
 */

public class TextualFeatures {

    private int id;
    private int numberOfFeatures;
    private final List<String> textualList;
    private static String language;
    private final QueryParser greekParser;
    private final QueryParser englishParser;
    private final LanguageDetector languageDetector;

    public TextualFeatures(int id, List<String> textualList, LanguageDetector languageDetector) {

        this.id = id;
        this.textualList = textualList;
        this.languageDetector = languageDetector;
        numberOfFeatures = textualList.size() + id;
        GreekAnalyzer greekAnalyzer = new GreekAnalyzer(Version.LUCENE_36);
        greekParser = new QueryParser(Version.LUCENE_36, "", greekAnalyzer);
        EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer(Version.LUCENE_36);
        englishParser = new QueryParser(Version.LUCENE_36, "", englishAnalyzer);

    }

    public void createTextualFeatures(OSMWay wayNode) {
        //namesList.indexOf(name) this index can be zero.
        //In that case it conflicts the previous geometry id, so we increment id.

        //idWords: populated with the ID that will be ginen as a feature, mapped with the word found.
        //Chose to store the name for future use.
        Map<Integer, String> idWords = new TreeMap<>();
        Map<String, String> tags = wayNode.getTagKeyValue();
        if (tags.keySet().contains("name")) {
            String nameTag = tags.get("name"); //get the value of the name tag of the current node
            String[] nameTagSplitList = nameTag.split("\\s");    //split the value to compare individually
            //with the namesList
            String lang = "";
            try {
                lang = detectLanguage(nameTag);
            } catch (LangDetectException ex) {
                Logger.getLogger(TextualFeatures.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (String split : nameTagSplitList) {
                try {
                    //TOGGLE
                    split = split.replaceAll("[-+.^:,?;'{}\"!()\\[\\]]", "");
                    if (lang.equals("el")) {
                        split = stemGreek(split);
                    } else {
                        split = stemEnglish(split);
                    }

                    if (textualList.contains(split)) {
                        int currentID = textualList.indexOf(split) + id;
                        idWords.put(currentID, split);
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(TextualFeatures.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            for (Integer wordID : idWords.keySet()) {
                wayNode.getFeatureNodeList().add(new FeatureNode(wordID, 1.0));
                //System.out.println(wordID);
            }
            //System.out.println("until textual " + wayNode.getFeatureNodeList());
        }
    }

    public int getLastID() {
        return numberOfFeatures;
    }

    private String detectLanguage(String nameTag) throws LangDetectException {
        //detect language

        if (!nameTag.isEmpty()) {
            language = languageDetector.detect(nameTag);
            return language;
        } else {
            return "no_lang";
        }
    }

    private String stemGreek(String word) throws ParseException {
        String stemmedWord;

        if (!word.isEmpty()) {
            stemmedWord = greekParser.parse(word).toString();
        } else {
            return word;
        }

        return stemmedWord;
    }

    private String stemEnglish(String word) throws ParseException {

        String stemmedWord;

        if (!word.isEmpty()) {
            stemmedWord = englishParser.parse(word).toString();
        } else {
            return word;
        }

        return stemmedWord;
    }
}
