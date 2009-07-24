/** \file
    Read SRTM zip files. */
#include "zip.h"
#include <QFileInfo>
#include <QDebug>

#include "math.h"

#include <zzip/lib.h>

bool zip_store_uncompressed_data = false;

/** Creates a buffer of all the data in the SRTM file. The uncompressed version is used if available.
  *
  * \returns The length of the side of the data (e.g. 1201 or 3601)
  * \note The buffer returned is owned by the caller of this function and _must_ be freed after usage.
  */
int SrtmZipFile::getData(QString filename, qint16 **buffer)
{
    *buffer = 0;
    
    QFileInfo fi(filename);
    QString uncompressedFile = fi.path()+'/'+fi.completeBaseName();

    int size = 0;
    if (QFileInfo(uncompressedFile).exists()) {
        QFile file(uncompressedFile);
        if (!file.open(QIODevice::ReadOnly)) {
            qCritical() << "ZIP(Uncompressed): Could not open file" << uncompressedFile << file.errorString();
            return 0;
        }
        size = sqrt(file.size()/2);
        if (size*size*2 != file.size()) {
            qCritical() << "ZIP(Uncompressed): Invalid data: Not a square!";
        }
        *buffer = new qint16[file.size()/2];
        if (!*buffer) {
            qCritical() << "ZIP(Uncompressed): Could not allocate buffer.";
            return 0;
        }
        if (file.read((char *)*buffer, file.size()) != file.size()) {
            qCritical() << "ZIP(Uncompressed): Could not read all bytes.";
        }
        file.close();
    } else {
        ZZIP_DIR* dir = zzip_dir_open(filename.toAscii(), 0);
        if (!dir) {
            qCritical() << "ZIP: Could not open zip file" << filename;
            return 0;
        }
        ZZIP_FILE* fp = zzip_file_open(dir, fi.completeBaseName().toAscii(), 0);
        if (!fp) {
            qCritical() << "ZIP: Could not find" <<  fi.completeBaseName() << "in" << filename;
            return 0;
        }
        ZZIP_STAT stat;
        if (zzip_file_stat(fp, &stat) == -1) {
            qCritical() << "ZIP: Could not get info about" << uncompressedFile;
            return 0;
        }
        
        size = sqrt(stat.st_size/2);
        if (size*size*2 != stat.st_size) {
            qCritical() << "ZIP: Invalid data: Not a square!";
        }
        *buffer = new qint16[stat.st_size/2];

        if (zzip_file_read(fp, *buffer, stat.st_size) != stat.st_size) {
            qCritical() << "ZIP: Could not read all bytes.";
            delete *buffer;
            *buffer = 0;
            return 0;
        }

        zzip_file_close(fp);
        zzip_dir_close(dir);
    }
    return size;
}
