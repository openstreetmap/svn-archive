#include "main.h"

#include "srtm.h"
#include "osm-parse.h"
#include "relations.h"

#include <curl/curl.h>

int main(/*int argc, char **argv*/)
{
    curl_global_init(CURL_GLOBAL_DEFAULT);
    
    SrtmDownloader downloader;
    downloader.loadFileList();

    OsmData data;
    data.parse("/dev/stdin");

    QFile output("output.xml");
    output.open(QIODevice::WriteOnly); //TODO: Error handling

    RelationWriter writer(&data, &output);
    writer.writeRelations();
    
    output.close();
    curl_global_cleanup();
}
