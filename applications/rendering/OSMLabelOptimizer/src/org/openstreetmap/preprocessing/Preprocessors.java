/**
 * 
 */
package org.openstreetmap.preprocessing;

import java.util.Vector;

/**
 * @author sebi
 *
 */
public class Preprocessors
{
    private Vector<Preprocessor> preprocessors = new Vector<Preprocessor>();
    
    public void addPreprocessor(Preprocessor preprocessor)
    {
        preprocessors.add(preprocessor);
    }
    
    public void preprocessAll()
    {
        for (Preprocessor p : preprocessors)
        {
            p.preprocess();
        }
    }
}
