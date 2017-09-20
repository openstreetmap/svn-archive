// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo GEO file.
 */
public class EdigeoFileGEO extends EdigeoFile {

    /**
     * Constructs a new {@code EdigeoFileGEO}.
     * @param path path to GEO file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileGEO(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
