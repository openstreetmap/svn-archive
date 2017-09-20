// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo QAL file.
 */
public class EdigeoFileQAL extends EdigeoFile {

    /**
     * Constructs a new {@code EdigeoFileQAL}.
     * @param path path to QAL file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileQAL(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
