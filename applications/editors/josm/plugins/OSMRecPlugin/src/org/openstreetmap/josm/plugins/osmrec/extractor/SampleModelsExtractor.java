// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.osmrec.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author imis-nkarag
 */
public class SampleModelsExtractor {

    public void extractSampleSVMmodel(String modelName, String modelPath) {
        InputStream svmModelStream;
        FileOutputStream outputStream = null;

        File targetFile = new File(modelPath);

        if (targetFile.exists()) {
            return;
        }
        try {
            targetFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(SampleModelsExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("trying to get stream.. for " + "/resources/files/" + modelName);
        svmModelStream = SampleModelsExtractor.class.getResourceAsStream("/resources/files/" + modelName);

        try {
            outputStream = new FileOutputStream(targetFile);

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
