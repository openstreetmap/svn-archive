// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.datasets.at;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.plugins.opendata.core.io.geographic.DefaultGmlHandler;

public class AustrianGmlHandler extends DefaultGmlHandler {

    @Override
    public CoordinateReferenceSystem getCrsFor(String crsName)
            throws NoSuchAuthorityCodeException, FactoryException {

        // See http://www.esri-austria.at/downloads/coords_at.html

        if (crsName != null && crsName.startsWith("AUT")) {
            // CHECKSTYLE.OFF: LineLength
            if (crsName.equalsIgnoreCase("AUT-GK28")) {             // Gauß-Krüger, Meridianstreifen M28
                return CRS.decode("EPSG:31281");

            } else if (crsName.equalsIgnoreCase("AUT-GK31")) {      // Gauß-Krüger, Meridianstreifen M31
                return CRS.decode("EPSG:31282");

            } else if (crsName.equalsIgnoreCase("AUT-GK34")) {      // Gauß-Krüger, Meridianstreifen M34
                return CRS.decode("EPSG:31283");

            } else if (crsName.equalsIgnoreCase("AUT-GK28-5")) {    // Gauß-Krüger, Meridianstreifen M28, ohne 5 Mio. im Hochwert
                return CRS.decode("EPSG:31251");

            } else if (crsName.equalsIgnoreCase("AUT-GK31-5")) {    // Gauß-Krüger, Meridianstreifen M31, ohne 5 Mio. im Hochwert
                return CRS.decode("EPSG:31252");

            } else if (crsName.equalsIgnoreCase("AUT-GK34-5")) {    // Gauß-Krüger, Meridianstreifen M34, ohne 5 Mio. im Hochwert
                return CRS.decode("EPSG:31253");

            } else if (crsName.equalsIgnoreCase("AUT-BM28")) {      // Bundesmeldenetz, Meridianstreifen M28
                return CRS.decode("EPSG:31288");

            } else if (crsName.equalsIgnoreCase("AUT-BM31")) {      // Bundesmeldenetz, Meridianstreifen M31
                return CRS.decode("EPSG:31289");

            } else if (crsName.equalsIgnoreCase("AUT-BM34")) {      // Bundesmeldenetz, Meridianstreifen M34
                return CRS.decode("EPSG:31290");

            } else if (crsName.equalsIgnoreCase("AUT-LM")) {        // Lambertsche Kegelprojektion (geogr. Breite des Koo.Ursprungs = 47°30')
                return CRS.decode("EPSG:31287");

            } else if (crsName.equalsIgnoreCase("AUT-LL-BESSEL")) { // Geographische Koordinaten auf dem Bessel-Ellipsoid, Längenzählung nach Greenwich
                // See http://josebatiz.com/granvision/Almap/Install/Data1/_B5694C166D6A4B5390B1E547C6A1FAF6
                // FIXME
            }
            // CHECKSTYLE.ON: LineLength
        }
        return super.getCrsFor(crsName);
    }
}
