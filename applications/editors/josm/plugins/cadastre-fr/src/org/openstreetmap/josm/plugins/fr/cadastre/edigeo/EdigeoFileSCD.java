// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.edigeo;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Edigeo SCD file.
 */
public class EdigeoFileSCD extends EdigeoFile {

    /**
     * Constructs a new {@code EdigeoFileSCD}.
     * @param path path to SCD file
     * @throws IOException if any I/O error occurs
     */
    public EdigeoFileSCD(Path path) throws IOException {
        super(path);
    }

    @Override
    protected Block createBlock(String type) {
        // TODO Auto-generated method stub
        return null;
    }
}
