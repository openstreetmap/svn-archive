/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * SRTM data downloader and tile handler.
  */
#ifndef __SRTM_H__
#define __SRTM_H__

#include <QString>
#include <QObject>
#include <QMap>
#include <QRegExp>
#include <QFile>
#include <QCache>
#include <curl/curl.h>

/** Value of pixels where no SRTM data is available. */
#define SRTM_DATA_VOID -32768
/** Total number of SRTM files on NASA's server. */
#define SRTM_FILE_COUNT 14047

/** Handles the loading and parsing of an SRTM tile.
  *
  * This class should work for both SRTM1 and SRTM3 data.
  * But currently only SRTM3 is used.
  */
class SrtmTile
{
    public:
        SrtmTile(QString file, int lat, int lon);
        ~SrtmTile();
        int getPixelValue(int x, int y);
        float getAltitudeFromLatLon(float lat, float lon);
    private:
        /** Returns the weighted average of a and b.
          *
          * weight == 0 => a only
          *
          * weight == 1 => b only
          *
          * If one of the values is SRTM_DATA_VOID the other
          * value is returned, if both void then SRTM_DATA_VOID
          * is returned.
          */
        float avg(float a, float b, float weight) {
            if (a == SRTM_DATA_VOID) return b;
            if (b == SRTM_DATA_VOID) return a;
            return b * weight + a * (1 - weight);
        }
        int lat;
        int lon;
        qint16 *buffer;
        QFile file;
        int size;
        bool valid;
};

/** Downloads SRTM data from NASA's server. */
class SrtmDownloader
{
    public:
        SrtmDownloader(QString url, QString cachedir);
        ~SrtmDownloader() { curl_easy_cleanup(curl); }
        void createFileList();
        void loadFileList();
        SrtmTile *getTile(float lat, float lon);
        void downloadTile(QString filename);
        float getAltitudeFromLatLon(float lat, float lon);
        void curlAddData(void *ptr, int size);
    private:
        QString url;
        QString cachedir;
        QRegExp regex;
        CURL *curl;
        QString curlData;
        QMap<int, QString> fileList;
        QCache<int, SrtmTile> tileCache;
        int latLonToIndex(int lat, int lon) { return lat * 1000 + lon; }
};

#endif