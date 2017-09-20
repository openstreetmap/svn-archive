// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo VEC file.
 */
public class EdigeoFileVEC extends EdigeoFile {

    /**
     * Constructs a new {@code EdigeoFileVEC}.
     * @param path path to VEC file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileVEC(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
