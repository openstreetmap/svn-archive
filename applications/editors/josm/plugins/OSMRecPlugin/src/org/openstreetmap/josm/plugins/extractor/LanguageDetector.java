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
    
    private LanguageDetector(){
        //prevent instatiation
    }
    
    public static LanguageDetector getInstance(String languageProfilesPath){
        System.out.println("language profile path: \n" + languageProfilesPath + "/el");
        if(languageDetector == null){
            languageDetector = new LanguageDetector();  
            loadProfilesFromStream(languageProfilesPath);
            //profilesLoaded = true;
        }
        return languageDetector;        
    }  
    
    public static void loadProfilesFromStream(String languageProfilesPath){ //create profiles directory in system from stream and load them

        InputStream languageProfilesInputStream1 = LanguageDetector.class.getResourceAsStream("/resources/profiles/el");
        InputStream languageProfilesInputStream2 = LanguageDetector.class.getResourceAsStream("/resources/profiles/en");
        //languageProfilesInputStream.

        if(!new File(languageProfilesPath).exists()){
            new File(languageProfilesPath).mkdir();
        }
        File languageProfilesOutputFile1 = new File(languageProfilesPath + "/el");
        File languageProfilesOutputFile2 = new File(languageProfilesPath +"/en");
        //languageProfilesOutputFile.mkdirs();

        FileOutputStream outputStream = null;
        //FileOutputStream outputStream2 = null;
        try {
            outputStream = new FileOutputStream(languageProfilesOutputFile1);
            //outputStream2 = new FileOutputStream(languageProfilesOutputFile2);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("deb1");

        FileOutputStream outputStream2 = null;
        //FileOutputStream outputStream2 = null;
        try {
            outputStream2 = new FileOutputStream(languageProfilesOutputFile2);
            //outputStream2 = new FileOutputStream(languageProfilesOutputFile2);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }        


        int read = 0;
        byte[] bytes = new byte[1024];

        try {
            while ((read = languageProfilesInputStream1.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }        
        } catch (IOException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }


        int read2 = 0;
        byte[] bytes2 = new byte[1024];

        try {
            while ((read2 = languageProfilesInputStream2.read(bytes2)) != -1) {
                outputStream2.write(bytes2, 0, read2);
            }        
        } catch (IOException ex) {
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }        
        try {


            DetectorFactory.loadProfile(languageProfilesPath);
            //profilesLoaded = true;

        } catch (LangDetectException ex) {
            //profilesLoaded = false;
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }
    
    public String detect(String text) {
        try {
            Detector detector = DetectorFactory.create();
            detector.append(text);
            return detector.detect();
        } catch (LangDetectException ex) {            
            Logger.getLogger(LanguageDetector.class.getName()).log(Level.SEVERE, null, ex);
            return "en"; //default lang to return
        }       
    }   
}
