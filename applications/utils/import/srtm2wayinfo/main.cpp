/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Main function
  *
  * Handles command line processing, etc.
  */

#include "srtm.h"
#include "osm-parse.h"
#include "relations.h"
#include "settings.h"

#include <curl/curl.h>
#include <QDebug>
#include <QLocale>

int main(int argc, char **argv)
{
    qDebug() << sizeof(OsmNode) << sizeof(OsmWay) << sizeof(OsmData);
    global_settings.parseSettings(argc, argv);

    curl_global_init(CURL_GLOBAL_DEFAULT);
    /* Setting the locale should not be required but it can't harm.
     * QString::arg() is safe.
     * QString::toDouble first tries converting using the locale, then using the "C" locale.*/
    QLocale::setDefault(QLocale::C);

    //Download file lists first, so we can stop here if we notice an error
    SrtmDownloader downloader(global_settings.getSrtmServer(), global_settings.getCacheDir());
    downloader.loadFileList();

    OsmData data;
    data.parse(global_settings.getInput());
    QFile output(global_settings.getOutput());
    output.open(QIODevice::WriteOnly); //TODO: Error handling

    RelationWriter writer(&data, &output, &downloader);
    writer.writeRelations();

    output.close();
    curl_global_cleanup();
}
