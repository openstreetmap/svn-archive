#include "main.h"

#include "srtm.h"
#include "osm-parse.h"
#include "relations.h"

#include <QCoreApplication>

QCoreApplication *app;

int main(int argc, char **argv)
{
    app = new QCoreApplication(argc, argv);
    
    SrtmDownloader downloader;
    downloader.loadFileList();

    OsmData data;
    data.parse("/dev/stdin");

    QFile output("output.xml");
    output.open(QIODevice::WriteOnly); //TODO: Error handling

    RelationWriter writer(&data, &output);
    writer.writeRelations();
    
    output.close();
}