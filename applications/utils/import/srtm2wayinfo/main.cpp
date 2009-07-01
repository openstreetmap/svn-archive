/** \file
  * Main function
  * Handles command line processing, etc.
  */

#include "srtm.h"
#include "osm-parse.h"
#include "relations.h"

#include <curl/curl.h>
#include <QDebug>
#include <QLocale>

int main(int argc, char **argv)
{
    curl_global_init(CURL_GLOBAL_DEFAULT);
    /* Setting the locale should not be required but it can't harm.
     * QString::arg() is safe.
     * QString::toDouble first tries converting using the locale, then using the "C" locale.*/
    QLocale::setDefault(QLocale::C);

    
    SrtmDownloader downloader;
    downloader.loadFileList();

    OsmData data;
    if (argc < 2) {
        data.parse("/dev/stdin");
    } else {
        data.parse(argv[1]);
    }

    QFile output("output.xml");
    output.open(QIODevice::WriteOnly); //TODO: Error handling

    RelationWriter writer(&data, &output);
    writer.writeRelations();
    
    output.close();
    curl_global_cleanup();
}
