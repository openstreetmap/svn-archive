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

/** Main function. */
int main(int argc, char **argv)
{
    global_settings.parseSettings(argc, argv);

    OsmWayStorage *ways;
    OsmNodeStorage *nodes;
    if (global_settings.getDatasetSize() == size_small) {
        nodes = new OsmNodeStorageSmall();
        ways = new OsmWayStorageMem();
    } else if (global_settings.getDatasetSize() == size_medium) {
        nodes = new OsmNodeStorageMedium();
        ways = new OsmWayStorageDisk(".");
    } else {
        nodes = new OsmNodeStorageLarge();
        ways = new OsmWayStorageDisk(".");
    }

    curl_global_init(CURL_GLOBAL_DEFAULT);
    /* Setting the locale should not be required but it can't harm.
     * QString::arg() is safe.
     * QString::toDouble first tries converting using the locale, then using the "C" locale.*/
    QLocale::setDefault(QLocale::C);

    //Download file lists first, so we can stop here if we notice an error
    SrtmDownloader downloader(global_settings.getSrtmServer(), global_settings.getCacheDir());

    QFile output(global_settings.getOutput());
    if (!output.open(QIODevice::WriteOnly)) {
        qCritical() << "Could not open output file" << global_settings.getOutput();
        exit(1);
    }

    OsmData data(ways, nodes);
    data.parse(global_settings.getInput());

    RelationWriter writer(&data, &output, &downloader);
    writer.writeRelations();

    output.close();
    delete nodes;
    delete ways;
    curl_global_cleanup();
}
