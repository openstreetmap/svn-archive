/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
    Read SRTM zip files. */
#ifndef __SRTM_ZIP_H__
#define __SRTM_ZIP_H__

#include <QString>

/** Helper class to read SRTM zip files. */
class SrtmZipFile
{
    public:
        static int getData(QString filename, qint16 **buffer);
};

#endif