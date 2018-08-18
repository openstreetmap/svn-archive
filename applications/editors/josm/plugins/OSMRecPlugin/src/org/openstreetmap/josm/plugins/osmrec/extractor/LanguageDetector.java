// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.tools.Utils;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * Detects language of osm textual information
 *
 * @author imis-nkarag
 */
public final class LanguageDetector {

    private static LanguageDetector languageDetector = null;

    private LanguageDetector() {
        //prevent instatiation
    }

    public static LanguageDetector getInstance(String languageProfilesPath) {
        if (languageDetector == null) {
            languageDetector = new LanguageDetector();
            loadProfilesFromStream(languageProfilesPath);
            //profilesLoaded = true;
        }
        return languageDetector;
    }

    public static void loadProfilesFromStream(String languageProfilesPath) { //create profiles directory in system from stream and load them

        /*  supported languages
            el:greek, en:english, de:german, fr:french, es:spanish, ru:russian, tr:turkish, zh-cn:chinese, hi:hindi
         */
        InputStream languageProfilesInputStreamEl = LanguageDetector.class.getResourceAsStream("/profiles/el");
        InputStream languageProfilesInputStreamEn = LanguageDetector.class.getResourceAsStream("/profiles/en");
        InputStream languageProfilesInputStreamDe = LanguageDetector.class.getResourceAsStream("/profiles/de");
        InputStream languageProfilesInputStreamFr = LanguageDetector.class.getResourceAsStream("/profiles/fr");
        InputStream languageProfilesInputStreamEs = LanguageDetector.class.getResourceAsStream("/profiles/es");
        InputStream languageProfilesInputStreamRu = LanguageDetector.class.getResourceAsStream("/profiles/ru");
        InputStream languageProfilesInputStreamTr = LanguageDetector.class.getResourceAsStream("/profiles/tr");
        InputStream languageProfilesInputStreamZh = LanguageDetector.class.getResourceAsStream("/profiles/zh-cn");
        InputStream languageProfilesInputStreamHi = LanguageDetector.class.getResourceAsStream("/profiles/hi");
        //InputStream languageProfilesInputStream2 = LanguageDetector.class.getResourceAsStream("/resources/profiles/en");

        if (!new File(languageProfilesPath).exists()) {
            Utils.mkDirs(new File(languageProfilesPath));
        }

        File languageProfilesOutputFileEl = new File(languageProfilesPath + "/el");
        File languageProfilesOutputFileEn = new File(languageProfilesPath + "/en");
        File languageProfilesOutputFileDe = new File(languageProfilesPath + "/de");
        File languageProfilesOutputFileFr = new File(languageProfilesPath + "/fr");
        File languageProfilesOutputFileEs = new File(languageProfilesPath + "/es");
        File languageProfilesOutputFileRu = new File(languageProfilesPath + "/ru");
        File languageProfilesOutputFileTr = new File(languageProfilesPath + "/tr");
        File languageProfilesOutputFileZh = new File(languageProfilesPath + "/zh-cn");
        File languageProfilesOutputFileHi = new File(languageProfilesPath + "/hi");

        try {
            languageProfilesOutputFileEl.createNewFile();
            languageProfilesOutputFileEn.createNewFile();
            languageProfilesOutputFileDe.createNewFile();
            languageProfilesOutputFileFr.createNewFile();
            languageProfilesOutputFileEs.createNewFile();
            languageProfilesOutputFileRu.createNewFile();
            languageProfilesOutputFileTr.createNewFile();
            languageProfilesOutputFileZh.createNewFile();
            languageProfilesOutputFileHi.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            Logging.error(ex);
        }

        try {
            Files.copy(languageProfilesInputStreamEl, languageProfilesOutputFileEl.toPath());
            Files.copy(languageProfilesInputStreamEn, languageProfilesOutputFileEn.toPath());
            Files.copy(languageProfilesInputStreamDe, languageProfilesOutputFileDe.toPath());
            Files.copy(languageProfilesInputStreamFr, languageProfilesOutputFileFr.toPath());
            Files.copy(languageProfilesInputStreamEs, languageProfilesOutputFileEs.toPath());
            Files.copy(languageProfilesInputStreamRu, languageProfilesOutputFileRu.toPath());
            Files.copy(languageProfilesInputStreamTr, languageProfilesOutputFileTr.toPath());
            Files.copy(languageProfilesInputStreamZh, languageProfilesOutputFileZh.toPath());
            Files.copy(languageProfilesInputStreamHi, languageProfilesOutputFileHi.toPath());
        } catch (IOException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            Logging.error(ex);
        }

        try {
            DetectorFactory.loadProfile(languageProfilesPath);
        } catch (LangDetectException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            Logging.error(ex);
        }
    }

    public String detect(String text) {
        try {
            Detector detector = DetectorFactory.create();
            detector.append(text);
            return detector.detect();
        } catch (LangDetectException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            Logging.error(ex);
            return "en"; //default lang to return if anything goes wrong at detection
        }
    }
}
