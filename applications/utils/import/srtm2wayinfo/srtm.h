#ifndef __SRTM_H__
#define __SRTM_H__

#include <QString>
#include <QObject>
#include <QMap>
#include <QRegExp>
#include <QFile>
#include <QCache>
#include <curl/curl.h>

#define SRTM_DATA_VOID -32768

class SrtmTile
{
    public:
        SrtmTile(QString file, int lat, int lon);
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

class SrtmDownloader : public QObject
{
    Q_OBJECT
    public:
        SrtmDownloader(QString url="http://dds.cr.usgs.gov/srtm/version2/SRTM3/", QString cachedir="cache");
        ~SrtmDownloader() { curl_easy_cleanup(curl); }
        void createFileList();
        void loadFileList();
        SrtmTile *getTile(float lat, float lon);
        QMap<int, QString> fileList;
        void downloadTile(QString filename);
        float getAltitudeFromLatLon(float lat, float lon);
        void curlAddData(void *ptr, int size);
        QString curlData;
    private:
        QString url;
        QString cachedir;
        QRegExp regex;
        CURL *curl;
        QCache<int, SrtmTile> tileCache;
        int latLonToIndex(int lat, int lon) { return lat * 1000 + lon; }
};

#endif