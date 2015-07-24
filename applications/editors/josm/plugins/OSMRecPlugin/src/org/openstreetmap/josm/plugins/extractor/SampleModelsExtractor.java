/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openstreetmap.josm.plugins.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.nio.charset.Charset;
//import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
//import org.openstreetmap.josm.plugins.osmrec.OSMRecPluginHelper;

/**
 *
 * @author imis-nkarag
 */
public class SampleModelsExtractor {

    public void extractSampleSVMmodel(String modelName, String modelPath) {
        InputStream svmModelStream;
        FileOutputStream outputStream = null;

        //File modelFile = new File(bestModelPath);
        File targetFile = new File(modelPath);
        try {
            targetFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(SampleModelsExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //svmModelStream = SampleModelsExtractor.class.getResourceAsStream("/resources/profiles/el");
        System.out.println("trying to get stream.. for " + "/resources/files/" + modelName);
        svmModelStream = SampleModelsExtractor.class.getResourceAsStream("/resources/files/" + modelName);
        
        
        //Scanner input = new Scanner(svmModelStream);
        //svmModelStream.
        
        
//            while (input.hasNext()) {
//                String nextLine = input.nextLine();
//
//                //outputStream.write(nextLine.getBytes(Charset.forName("UTF-8")));
//                System.out.println(nextLine);
//                //outputStream.write(nextLine);
//                //textualList.add(nextLine);
//
//            }       
//            System.out.println("GER RESOURCE SUCCESS");
            
        try {
            outputStream = new FileOutputStream(targetFile);
            //Scanner input = new Scanner(svmModelStream);
            
            
            int read = 0;
            byte[] bytes = new byte[1024];

            try {
                while ((read = svmModelStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException ex) {
                Logger.getLogger(SampleModelsExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(SampleModelsExtractor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (svmModelStream != null) {
                try {
                    svmModelStream.close();
                } catch (IOException e) {
                    Logger.getLogger(SampleModelsExtractor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            if (outputStream != null) {
                try {
                    // outputStream.flush();
                    outputStream.close();
                } catch (IOException e) {
                    Logger.getLogger(SampleModelsExtractor.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }
}
