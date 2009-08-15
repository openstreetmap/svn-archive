/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * SRTM data downloader and tile handler.
  */
#include "srtm.h"
#include "zip.h"

#include <math.h>
#include <QDir>
#include <QStringList>
#include <QString>
#include <QProcess>
#include <QDebug>
#include <qendian.h>

/** Standard error tile that is returned when something goes wrong. */
SrtmTile errorTile("error", -1000, -1000); //TODO

/** Callback from curl for data that is not stored in a file but kept in memory. */
size_t curl_data_callback(void *ptr, size_t size, size_t nmemb, void *stream)
{
    SrtmDownloader *downloader = static_cast<SrtmDownloader*>(stream);
    downloader->curlAddData(ptr, size*nmemb);
    return size*nmemb;
}

/** Callback from curl for data that is stored in a file. */
size_t curl_file_callback(char *ptr, size_t size, size_t nmemb, void *stream)
{
    QFile *file = static_cast<QFile *>(stream);
    int size_left = size * nmemb;
    while (size_left > 0) {
        int result = file->write(ptr, size_left);
        if (result == -1) {
            qCritical() << "Error while writing to file!" << file->errorString();
        } else {
            ptr += result;
            size_left -= result;
        }
    }
    return size*nmemb;
}

/** Constructor for SrtmDownloader.
  * \note This downloader currently only supports SRTM3 data.
  * \param url URL to the SRTM data. 
  * \param cachedir Directory in which the downloaded files are stored.
  */
SrtmDownloader::SrtmDownloader(QString url, QString cachedir)
{
    this->url = url;
    this->cachedir = cachedir+"/";
    regex.setPattern("<a href=\"([NS])(\\d{2})([EW])(\\d{3})\\.hgt\\.zip");
    QDir dir;
    if (!dir.exists(cachedir)) {
        dir.mkpath(cachedir);
    }
    curl = curl_easy_init();
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1);
    curl_easy_setopt(curl, CURLOPT_FAILONERROR, 1); //Make sure error reporting works
    loadFileList();
}

/** One line helper function. */
void SrtmDownloader::curlAddData(void *ptr, int size)
{
    curlData += QString::fromAscii(static_cast<char*>(ptr), size);
}

/** Create a new file list by getting directory contents from server. */
void SrtmDownloader::createFileList()
{
    //Store data in memory
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, curl_data_callback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, this);
    
    QStringList continents;
    continents << "Africa" << "Australia" << "Eurasia" << "Islands" << "North_America" << "South_America";
    foreach (QString continent, continents) {
        qDebug() << "Downloading data from" << url+continent+"/";
        curlData.clear();
        curl_easy_setopt(curl, CURLOPT_URL, QString(url+continent+"/").toAscii().constData());
        CURLcode error = curl_easy_perform(curl);
        if (error) {
            qCritical() << "Error downloading data for" << continent << "(" << curl_easy_strerror(error) << ")";
        }
        int index = -1;
        while ((index = curlData.indexOf(regex, index+1)) != -1) {
            int lat = regex.cap(2).toInt();
            int lon = regex.cap(4).toInt();
            if (regex.cap(1) == "S") {
                lat = -lat;
            }
            if (regex.cap(3) == "W") {
                lon = - lon;
            }
            //S00E000.hgt.zip
            //123456789012345 => 15 bytes long
            fileList[latLonToIndex(lat, lon)] = continent+"/"+regex.cap().right(15);
        }
    }
    curlData.clear(); //Free mem

    if (fileList.size() != SRTM_FILE_COUNT) {
        qCritical() << "Could not download complete list of tiles from SRTM server. Got" << fileList.size() << "tiles but" << SRTM_FILE_COUNT << "were expected.";
        exit(1);
    }
    
    QFile file(cachedir+"filelist");
    if (!file.open(QIODevice::WriteOnly)) {
        qCritical() << "Could not open file" << cachedir+"filelist";
        //Not a fatal error. We just can't cache the list.
        return;
    }
    QDataStream stream(&file);
    stream << fileList;
    file.close();
}

/** Load a file list or create a new one if it doesn't exist. */
void SrtmDownloader::loadFileList()
{
    QFile file(cachedir+"filelist");
    if (!file.open(QIODevice::ReadOnly)) {
        createFileList();
        return;
    }
    QDataStream stream(&file);
    stream >> fileList;
    file.close();
    if (fileList.size() != SRTM_FILE_COUNT) {
        createFileList();
    }
}

/** Get tile for a specified location.
  * \note The tile object returned is owned by this SrtmDownloader instance. It
  *       _must_ _not_ be deleted by the user. However it _may_ be deleted by
  *       SrtmDownloader during any later call to getTile()
  */
SrtmTile *SrtmDownloader::getTile(float lat, float lon)
{
    int intlat = int(floor(lat)), intlon = int(floor(lon));
    int index = latLonToIndex(intlat, intlon);
    SrtmTile *tile = tileCache[index];

    if (tile) return tile;

    if (fileList.contains(index)) {
        QStringList splitted = fileList[index].split("/", QString::SkipEmptyParts);
        if (!QFile(cachedir + splitted[1]).exists()) {
            downloadTile(fileList[index]);
        }
        tile = new SrtmTile(cachedir + splitted[1], intlat, intlon);
        tileCache.insert(index, tile);
        Q_ASSERT(tileCache[index] != 0);
        return tile;
    } else {
       return &errorTile;
    }
}

/** Download a tile from the server.
    \param filename must be in the format continent/tilename.hgt.zip
    \note You should _not_ call this function when you need tile data. Use getTile instead. */
void SrtmDownloader::downloadTile(QString filename)
{
    qDebug() << "Downloading" << filename;
    QStringList splitted = filename.split("/", QString::SkipEmptyParts);
    
    QFile file(cachedir+splitted[1]);
    if (!file.open(QIODevice::WriteOnly)) {
        qCritical() << "Could not create file" << cachedir+splitted[1];
        return;
    }
    curlData.clear();
    curl_easy_setopt(curl, CURLOPT_URL, QString(url+filename).toAscii().constData());
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, curl_file_callback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &file);
    CURLcode error;
    if ((error = curl_easy_perform(curl))) {
        qCritical() << "Could not download" << filename << "("<< curl_easy_strerror(error) << ")";
    }
    //File is closed automatically
}

/** Get altitude and download necessary tiles automatically. */
float SrtmDownloader::getAltitudeFromLatLon(float lat, float lon)
{
    SrtmTile *tile = getTile(lat, lon);
    if (tile)
        return tile->getAltitudeFromLatLon(lat, lon);
    else
        return SRTM_DATA_VOID;
}

/** Create a new tile object. Unzips the tile data if necessary.
  * \note Special filename: "error" returns an invalid tile.
  */
SrtmTile::SrtmTile(QString filename, int lat, int lon)
{
    this->lat = lat;
    this->lon = lon;
    valid = false;
    if (filename == "error") return;
    buffer = 0;
    size = SrtmZipFile::getData(filename, &buffer);
    Q_ASSERT(size == 1201 || size == 3601);
    Q_ASSERT(buffer != 0);
    valid = true;
}

/** Free resources. */
SrtmTile::~SrtmTile()
{
    if (buffer) delete buffer;
}

/** Get the value of a pixel from the data using a coordinate system
  * starting in the upper left (NW) edge growing to the lower right
  * egde (SE) instead of the SRTM coordinate system.
  */
int SrtmTile::getPixelValue(int x, int y)
{
    Q_ASSERT(x >= 0 && x < size && y >= 0 && y < size);
    int offset = x + size * (size - y - 1);
    qint16 value;
    value = qFromBigEndian(buffer[offset]);
    return value;
}

/** Gets the altitude in meters for a given coordinate. */
float SrtmTile::getAltitudeFromLatLon(float lat, float lon)
{
    if (!valid) return SRTM_DATA_VOID;
    lat -= this->lat;
    lon -= this->lon;
    Q_ASSERT(lat >= 0.0 && lat < 1.0 && lon >= 0.0 && lon < 1.0);
    float x = lon * (size - 1);
    float y = lat * (size - 1);
    /* Variable names:
        valueXY with X,Y as offset from calculated value, _ for average
    */
    float value00 = getPixelValue(x, y);
    float value10 = getPixelValue(x+1, y);
    float value01 = getPixelValue(x, y+1);
    float value11 = getPixelValue(x+1, y+1);
    float value_0 = avg(value00, value10, x-int(x));
    float value_1 = avg(value01, value11, x-int(x));
    float value__ = avg(value_0, value_1, y-int(y));
    return value__;
}