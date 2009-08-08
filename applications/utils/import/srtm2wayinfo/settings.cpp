/* Copyright (c) 2009 Hermann Kraus
 * This software is available under a "MIT Style" license
 * (see COPYING).
 */
/** \file
  * Handles command line parsing and stores the options.
  */
#include "settings.h"
#include <getopt.h>
#include <QDebug>

/** Settings are global for the whole program. */
Settings global_settings;

/** Show usage instructions. */
void Settings::usage()
{
    qWarning() <<
    "Usage: srtm2wayinfo [OPTION]...\n"
    "Add information about altitude differences to OSM data.\n"
    "By default data is read from stdin and output goes to stdout.\n\n"
    
    "Example: bzcat planet.osm.bz2 | srtm2wayinfo | bzip2 > altitude.osm.bz2\n\n"
    
    "Mandatory arguments to long options are mandatory for short options too.\n"
    "  -s, --srtm-server=URL      Changes the SRTM server's location.\n"
    "                                You need to delete the cache dir first.\n"
    "  -i, --input=FILE           Read data from file (default: stdin)\n"
    "  -o, --output=FILE          Write data to file (default: stdout)\n"
    "  -k, --keep                 Keep uncompressed SRTM tiles on disk\n"
    "                                (uses more diskspace but less cpu time)\n"
    "  -c, --cache=DIRECTORY      Directory in which the downloaded SRTM tiles\n"
    "                                are cached.\n"
    "  -S, --size=SIZE            Optimize data structures for a certain\n"
    "                                size of the dataset. SIZE can be\n"
    "                                - \"small\" (default, best for files < 300 MB)\n"
    "                                - \"medium\" (300 MB - 2 GB)\n"
    "                                - \"large\" (> 2 GB)\n";
}

/** Parses the command line and fills the settings structure.
  *
  * This function uses the getopt functions provided by glibc
  * and has GNU style option handling.
  */
void Settings::parseSettings(int argc, char **argv)
{
    static struct option long_options[] =
            {
            {"srtm-server", required_argument, 0, 's'},
            {"input",       required_argument, 0, 'i'},
            {"output",      required_argument, 0, 'd'},
            {"cache",       required_argument, 0, 'c'},
            {"keep",        no_argument,       0, 'k'},
            {"help",        no_argument,       0, 'h'},
            {"size",        required_argument, 0, 'S'},
            {0, 0, 0, 0}
            };
    while (1) {
        int option_index = 0;
        int c = getopt_long(argc, argv, "s:i:o:kzS:", long_options, &option_index);
        if (c == -1) break;
        switch (c) {
            case 0:
                break;
            case '?':
                usage();
                exit(1);
                break;
            case 's':
                srtm_server = optarg;
                qDebug() << "Using SRTM server at" << srtm_server;
                break;
            case 'i':
                input = optarg;
                break;
            case 'o':
                output = optarg;
                break;
            case 'c':
                cache_dir = optarg;
                break;
            case 'k':
                store_uncompressed = true;
                break;
            case 'h':
                usage();
                exit(0);
                break;
            case 'S':
                if (!strcasecmp(optarg, "large")) {
                    size = size_large;
                } else if (!strcmp(optarg, "medium")) {
                    size = size_medium;
                } else if (!strcmp(optarg, "small")) {
                    size = size_small;
                } else {
                    qWarning() << "Size must be either \"small\", \"medium\" or \"large\"";
                    usage();
                    exit(1);
                }
                break;
            default:
                qCritical() << "Unhandled option" << c;
        }
    }
    if (optind < argc) {
        qCritical() << "Too many arguments.";
        //Some arguments left
        usage();
        exit(1);
    }
}
