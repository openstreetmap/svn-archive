#ifndef __SRTM_H__
#define __SRTM_H__

#include <QString>
#include <QObject>
#include <QtNetwork>
#include <QMap>
#include <QRegExp>

class SRTMTile
{
    public:
        SRTMTile(QString file, int lat, int lon);
        int getPixelValue(int x, int y);
    private:
        int lat;
        int lon;
        qint16 *buffer;
        QFile file;
        int size;
};

class SRTMDownloader : public QObject
{
    Q_OBJECT
    public:
        SRTMDownloader(QString server_="e0srp01u.ecs.nasa.gov",
            QString directory_="/srtm/version2/SRTM3", QString cachedir_="cache");
        void createFileList();
        void loadFileList();
        SRTMTile *getTile(float lat, float lon);
        QMap<int, QString> fileList;
        void downloadTile(QString filename);
    private:
        QString server;
        QString directory;
        QString cachedir;
        QFtp ftp;
        int listCommand;
        QString currentContinent;
        QRegExp regex;
        int latLonToIndex(int lat, int lon) { return lat * 1000 + lon; }
        void connectFtp();
    public slots:
        void ftpListInfo(const QUrlInfo & info);
        void ftpDone(int command_id, bool error);
};

#endif