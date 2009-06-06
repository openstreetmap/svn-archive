#include "main.h"

#include "srtm.h"
#include "osm-parse.h"

#include <QCoreApplication>

QCoreApplication *app;

int main(int argc, char **argv)
{
    app = new QCoreApplication(argc, argv);
    
    OsmData data;
    SRTMDownloader downloader;
    downloader.loadFileList();

    QFile output("output.xml");
    output.open(QIODevice::WriteOnly); //TODO: Error handling
    data.parse("/dev/stdin");
}