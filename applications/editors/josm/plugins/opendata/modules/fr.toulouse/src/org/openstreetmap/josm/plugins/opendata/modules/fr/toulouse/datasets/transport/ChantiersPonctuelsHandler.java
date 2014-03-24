// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.modules.fr.toulouse.datasets.transport;

public class ChantiersPonctuelsHandler extends ChantiersHandler {

    public ChantiersPonctuelsHandler() {
        super(14071, "Chantiers en cours (ponctuel)");
        getCsvHandler().setCharset(ISO8859_15);
    }

    @Override
    public boolean acceptsFilename(String filename) {
        return acceptsCsvKmzTabFilename(filename, "Chantiers_Ponctuels");
    }
}
