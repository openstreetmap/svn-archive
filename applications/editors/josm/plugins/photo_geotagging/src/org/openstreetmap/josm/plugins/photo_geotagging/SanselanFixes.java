// This file contains fixes for some bugs of sanselan.
// License: Apache Software License, Version 2.0
package org.openstreetmap.josm.plugins.photo_geotagging;

import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.fieldtypes.FieldType;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

public class SanselanFixes implements TiffConstants {
    /**
     * The setGPSInDegrees method produces Strings without trailing NULL character.
     * We simply redirect the create() call to SanselanFixes.create().
     */

    /**
     * A convenience method to update GPS values in EXIF metadata.
     *
     * @param longitude Longitude in degrees E, negative values are W.
     * @param latitude latitude in degrees N, negative values are S.
     * @throws ImageWriteException
     */
    public static void setGPSInDegrees(TiffOutputSet outputSet, double longitude, double latitude)
            throws ImageWriteException
    {
        TiffOutputDirectory gpsDirectory = outputSet.getOrCreateGPSDirectory();

        String longitudeRef = longitude < 0 ? "W" : "E";
        longitude = Math.abs(longitude);
        String latitudeRef = latitude < 0 ? "S" : "N";
        latitude = Math.abs(latitude);

        {
            TiffOutputField longitudeRefField = SanselanFixes.create(
                    TiffConstants.GPS_TAG_GPS_LONGITUDE_REF, outputSet.byteOrder,
                    longitudeRef);
            gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
            gpsDirectory.add(longitudeRefField);
        }

        {
            TiffOutputField latitudeRefField = SanselanFixes.create(
                    TiffConstants.GPS_TAG_GPS_LATITUDE_REF, outputSet.byteOrder,
                    latitudeRef);
            gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
            gpsDirectory.add(latitudeRefField);
        }

        {
            double value = longitude;
            double longitudeDegrees = (long) value;
            value %= 1;
            value *= 60.0;
            double longitudeMinutes = (long) value;
            value %= 1;
            value *= 60.0;
            double longitudeSeconds = value;
            Double values[] = {
                    new Double(longitudeDegrees), new Double(longitudeMinutes),
                    new Double(longitudeSeconds),
            };

            TiffOutputField longitudeField = TiffOutputField.create(
                    TiffConstants.GPS_TAG_GPS_LONGITUDE, outputSet.byteOrder, values);
            gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
            gpsDirectory.add(longitudeField);
        }

        {
            double value = latitude;
            double latitudeDegrees = (long) value;
            value %= 1;
            value *= 60.0;
            double latitudeMinutes = (long) value;
            value %= 1;
            value *= 60.0;
            double latitudeSeconds = value;
            Double values[] = {
                    new Double(latitudeDegrees), new Double(latitudeMinutes),
                    new Double(latitudeSeconds),
            };

            TiffOutputField latitudeField = TiffOutputField.create(
                    TiffConstants.GPS_TAG_GPS_LATITUDE, outputSet.byteOrder, values);
            gpsDirectory.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
            gpsDirectory.add(latitudeField);
        }

    }

    /**
     * fix for 2 Problems:
     *     - ASII Fields have always length 1
     *     - Trailing NULL character is missing
     */
    public static TiffOutputField create(TagInfo tagInfo, int byteOrder,
        String value) throws ImageWriteException
    {
        FieldType fieldType;
        if (tagInfo.dataTypes == null)
            fieldType = FIELD_TYPE_ASCII;
        else if (tagInfo.dataTypes == FIELD_TYPE_DESCRIPTION_ASCII)
            fieldType = FIELD_TYPE_ASCII;
        else
            throw new ImageWriteException("Tag has unexpected data type.");

        // According to EXIF spec, we need to append a NULL byte.
        // This way we have the same output as we would get with the exiftools library.
        String newValue = value+'\0';
        byte bytes[] = fieldType.writeData(newValue, byteOrder);
        // the count "1" in the original code (see commented out original)
        // is wrong as it assumes the field being updated is a single ascii char
        //return new TiffOutputField(tagInfo.tag, tagInfo, fieldType, 1, bytes);
        return new TiffOutputField(tagInfo.tag, tagInfo, fieldType, newValue.length(), bytes);
    }
}
