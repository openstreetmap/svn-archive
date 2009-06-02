#include "srtm.h"
#include <QDir>
#include <QStringList>
#include <QString>
#include <QCoreApplication>
#include <QProcess>
#include "math.h"

QCoreApplication *app;

/** Constructor for SRTMDownloader.
  * \param server Hostname of SRTM server.
  * \param directory Absolute path to base directory of SRTM server. Must start with a slash!
  * \param cachedir Directory in which the downloaded files are stored.
  */
SRTMDownloader::SRTMDownloader(QString server, QString directory, QString cachedir)
{
    this->server = server;
    this->directory = directory+"/";
    this->cachedir = cachedir+"/";
    regex.setPattern("([NS])(\\d{2})([EW])(\\d{3})\\.hgt\\.zip");
    QDir dir;
    if (!dir.exists(cachedir)) {
        dir.mkpath(cachedir);
    }
    connect(&ftp, SIGNAL(commandFinished(int, bool)), this, SLOT(ftpDone(int, bool)));
    connect(&ftp, SIGNAL(listInfo(const QUrlInfo &)), this, SLOT(ftpListInfo(const QUrlInfo &)));
}

/** Create a new file list by getting directory contents from server. */
void SRTMDownloader::createFileList()
{
    QStringList continents;
    continents << "Africa" << "Australia" << "Eurasia" << "Islands" << "North_America" << "South_America";
    connectFtp();
    ftp.cd(directory);
    foreach (QString continent, continents) {
        qDebug() << "Downloading data for" << continent;
        currentContinent = continent;
        ftp.list(continent);
        while (ftp.currentId()) app->processEvents();
    }
    QFile file(cachedir+"filelist");
    if (!file.open(QIODevice::WriteOnly)) {
        qDebug() << "Could not open file" << cachedir+"filelist";
        return;
    }
    QDataStream stream(&file);
    stream << fileList;
    file.close();
}

/** Load a file list or create a new one if it doesn't exist. */
void SRTMDownloader::loadFileList()
{
    QFile file(cachedir+"filelist");
    if (!file.open(QIODevice::ReadOnly)) {
        qDebug() << "Creating new list";
        createFileList();
        return;
    }
    QDataStream stream(&file);
    stream >> fileList; //TODO: Detect corrupted list
    file.close();
}

/** SLOT called by the ftp object to return information about the file. */
void SRTMDownloader::ftpListInfo(const QUrlInfo &info)
{
    if (regex.indexIn(info.name()) == -1) {
        qDebug() << "Regex did not match!";
    }
    //qDebug() << currentContinent << info.name() << regex.capturedTexts();
    int lat = regex.cap(2).toInt();
    int lon = regex.cap(4).toInt();
    if (regex.cap(1) == "S") {
        lat = -lat;
    }
    if (regex.cap(3) == "W") {
        lon = - lon;
    }
    fileList[latLonToIndex(lat, lon)] = currentContinent+"/"+info.name();
}

/** Get tile for a specified location. */
SRTMTile *SRTMDownloader::getTile(float lat, float lon)
{
    int index = latLonToIndex(int(lat), int(lon));
    if (fileList.contains(index)) {
        QStringList splitted = fileList[index].split("/", QString::SkipEmptyParts);
        if (!QFile(cachedir + splitted[1]).exists()) {
            downloadTile(fileList[index]);
        }
        return new SRTMTile(cachedir + splitted[1], int(lat), int(lon)); //TODO: Caching
    } else {
       return new SRTMTile("error", -1000, -1000);
    }
}

/** Download a tile from the server.
    \note You should _not_ call this function when you need tile data. Use getTile instead. */
void SRTMDownloader::downloadTile(QString filename)
{
    qDebug() << "Downloading" << filename;
    QStringList splitted = filename.split("/", QString::SkipEmptyParts);
    connectFtp();
    ftp.cd(directory+splitted[0]);
    QFile file(cachedir+splitted[1]);
    if (!file.open(QIODevice::WriteOnly)) {
        qDebug() << "Could not create file" << cachedir+splitted[1];
        return;
    }
    ftp.get(splitted[1], &file);
    while (ftp.currentId()) app->processEvents();
}

/** Open the FTP connection if not already done.
    FTP connections are reused.
*/
void SRTMDownloader::connectFtp()
{
    qDebug() << "Connecting to FTP";
    if (ftp.state() == QFtp::LoggedIn) return;
    ftp.connectToHost(server);
    ftp.login();
    while (ftp.currentId()) app->processEvents();
    qDebug() << "Connected to FTP";
}

/** FTP error handler. */
void SRTMDownloader::ftpDone(int /*command_id*/, bool error)
{
    if (error) qDebug() << "FTP-Error:" << ftp.errorString();
}

/** Create a new tile object. Unzips the tile data if necessary. */
SRTMTile::SRTMTile(QString filename, int lat, int lon)
{
    qDebug() << "Creating new tile object for " << lat << lon;
    this->lat = lat;
    this->lon = lon;
    //TODO: This problem needs a better solution!
    QStringList args;
    QProcess process;
    args << "-n" << QFileInfo(filename).fileName();
    process.setWorkingDirectory(QFileInfo(filename).path());
    process.setProcessChannelMode(QProcess::ForwardedChannels);
    process.start("unzip", args);
    if (!process.waitForStarted() || !process.waitForFinished()) {
        qDebug() << "Could not unzip" << filename;
    }
    QFileInfo fi(filename);
    QString filename2 = fi.path()+'/'+fi.completeBaseName();
    qDebug() << filename2;
    file.setFileName(filename2);
    if (!file.open(QIODevice::ReadOnly)) {
        qCritical() << "Could not open file" << filename2 << file.errorString();
    } else {
        qDebug() << "file is open";
    }
    Q_ASSERT(file.size() == 2*1201*1201 || file.size() == 2*3601*3601);
    size = sqrt(file.size()/2);
}

/** Get the value of a pixel from the data using a coordinate system
  * starting in the upper left (NW) edge growing to the lower right
  * egde (SE) instead of the SRTM coordinate system.
  */
int SRTMTile::getPixelValue(int x, int y)
{
    int offset = x + size * (size - y - 1);
//     qDebug() << "offset" << offset;
    file.seek(offset * 2); //TODO: Errorhandling
    qint16 value;
//     qDebug() << "bytes" <<
    file.read((char *)&value, 2);
//      << file.errorString() << size;
//     qDebug() << "value raw" << value;
    value = qFromBigEndian(value);
//     qDebug() << "value system" << value;
    return value;
}

SRTMDownloader downloader;
    
int main(int argc, char **argv)
{
    QCoreApplication myApp(argc, argv);
    app = &myApp;
    downloader.loadFileList();
    SRTMTile *tile = downloader.getTile(49.12, 12.12);
    tile->getPixelValue(567, 234);
    int i;
    for (i=0; i<10000000; i++)
    {
        tile->getPixelValue(567, 234);
    }
}