#include "settings.h"
#include <getopt.h>
#include <QDebug>

Settings global_settings;

void Settings::usage()
{
    qDebug() << "Write some text here.";
}

/*
-s --srtm-server
-i --input        read from file (default stdin)
-o --output       write to file (default stdout)
-k --keep         keep uncompressed SRTM tiles on disk (uses more diskspace but less cpu time)
*/

void Settings::parseSettings(int argc, char **argv)
{
    static struct option long_options[] =
            {
            {"srtm-server", required_argument, 0, 's'},
            {"input",       required_argument, 0, 'i'},
            {"output",      required_argument, 0, 'd'},
            {"keep",        no_argument,       0, 'k'},
            {0, 0, 0, 0}
            };
    while (1) {
        int option_index = 0;
        int c = getopt_long(argc, argv, "s:i:o:kz", long_options, &option_index);
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
            case 'k':
                store_uncompressed = true;
                break;
            default:
                qCritical() << "Unhandled option" << c;
        }
    }
}
