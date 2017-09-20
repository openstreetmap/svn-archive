// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo GEN file.
 */
public class EdigeoFileGEN extends EdigeoFile {

    /**
     * Constructs a new {@code EdigeoFileGEN}.
     * @param path path to GEN file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileGEN(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
