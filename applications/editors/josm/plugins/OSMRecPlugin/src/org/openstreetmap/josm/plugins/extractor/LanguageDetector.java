package org.openstreetmap.josm.plugins.extractor;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Detects language of osm textual information
 *
 * @author imis-nkarag
 */
public class LanguageDetector {

    private static LanguageDetector languageDetector = null;

    private LanguageDetector() {
        //prevent instatiation
    }

    public static LanguageDetector getInstance(String languageProfilesPath) {
        //System.out.println("language profile path: \n" + languageProfilesPath + "/el");
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
            //new File(languageProfilesPath).mkdir();
            new File(languageProfilesPath).mkdirs();
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
        }

        FileOutputStream outputStreamEl = null;
        FileOutputStream outputStreamEn = null;
        FileOutputStream outputStreamDe = null;
        FileOutputStream outputStreamFr = null;
        FileOutputStream outputStreamEs = null;
        FileOutputStream outputStreamRu = null;
        FileOutputStream outputStreamTr = null;
        FileOutputStream outputStreamZh = null;
        FileOutputStream outputStreamHi = null;
        
        try {
            
            outputStreamEl = new FileOutputStream(languageProfilesOutputFileEl);
            outputStreamEn = new FileOutputStream(languageProfilesOutputFileEn);
            outputStreamDe = new FileOutputStream(languageProfilesOutputFileDe);
            outputStreamFr = new FileOutputStream(languageProfilesOutputFileFr);
            outputStreamEs = new FileOutputStream(languageProfilesOutputFileEs);
            outputStreamRu = new FileOutputStream(languageProfilesOutputFileRu);
            outputStreamTr = new FileOutputStream(languageProfilesOutputFileTr);
            outputStreamZh = new FileOutputStream(languageProfilesOutputFileZh);
            outputStreamHi = new FileOutputStream(languageProfilesOutputFileHi);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }

        int read = 0;
        byte[] bytes = new byte[1024];
        try {

            while ((read = languageProfilesInputStreamEl.read(bytes)) != -1) {
                outputStreamEl.write(bytes, 0, read);
            }

            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamEn.read(bytes)) != -1) {
                outputStreamEn.write(bytes, 0, read);
            }

            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamDe.read(bytes)) != -1) {
                outputStreamDe.write(bytes, 0, read);
            }

            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamFr.read(bytes)) != -1) {
                outputStreamFr.write(bytes, 0, read);
            }
            
            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamEs.read(bytes)) != -1) {
                outputStreamEs.write(bytes, 0, read);
            }  
            
            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamRu.read(bytes)) != -1) {
                outputStreamRu.write(bytes, 0, read);
            } 

            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamTr.read(bytes)) != -1) {
                outputStreamTr.write(bytes, 0, read);
            } 
            
            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamZh.read(bytes)) != -1) {
                outputStreamZh.write(bytes, 0, read);
            }
            
            read = 0;
            bytes = new byte[1024];

            while ((read = languageProfilesInputStreamHi.read(bytes)) != -1) {
                outputStreamHi.write(bytes, 0, read);
            } 
            
        } catch (IOException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            DetectorFactory.loadProfile(languageProfilesPath);

        } catch (LangDetectException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String detect(String text) {
        try {
            Detector detector = DetectorFactory.create();
            detector.append(text);
            String lang = detector.detect();
            //System.out.println("language detected: " + lang);
            return lang;
        } catch (LangDetectException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            return "en"; //default lang to return if anything goes wrong at detection
        }
    }
}
