// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo MAT file.
 */
public class EdigeoFileMAT extends EdigeoFile {

    /**
     * Constructs a new {@code EdigeoFileMAT}.
     * @param path path to MAT file
     * @throws IOException if any I/O error occurs
     */
    EdigeoFileMAT(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
