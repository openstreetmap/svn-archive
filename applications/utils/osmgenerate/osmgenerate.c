#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <libgen.h>

#define INDENT "    "



void create_data(int ways, int segs_per_way, int negative)
{
    int id, way, phase;
    int nodes_per_way = segs_per_way + 1;
    double sqrt_ways = sqrt(ways * 1.0) + 1.0;
    const char *sign = negative ? "-" : "";

    for (phase = 0; phase < 3; phase++) {
        for (way=0; way < ways; way++) {
            int visible = way % 2; // Every alternate way is deleted
            int node_offset = 1 + way * nodes_per_way;
            int seg_offset  = 1 + way * segs_per_way;
            time_t t = 31 * 365 * 24 * 60 * 60 + way;
            struct tm *ts = gmtime(&t);

            switch(phase) {
                case 0: { // Nodes, place in a grid like pattern
                    double lon = (90.0 * way) / ways - 45.0;
                    double lat = (way % ((int)sqrt_ways)) * (90.0 / sqrt_ways) - 45.0;
                    double lat_per = (90.0 / sqrt_ways) / (nodes_per_way + 1.0);

                    //  <node id='17233948' timestamp='2007-08-04 22:24:51' visible='true' lat='45.4052394197691' lon='-75.6987485112884' />
                    for (id = 0; id < nodes_per_way; id++) {
                        printf(INDENT "<node id='%s%d' timestamp='%d-%d-%d %d:%d:%d' visible='%s' lat='%.8f' lon='%.8f' />\n",
                               sign, node_offset + id, ts->tm_year + 1900, ts->tm_mon, ts->tm_mday, ts->tm_hour, ts->tm_min, ts->tm_sec,
                               visible ? "true" : "false", lat + lat_per * id, lon);
                    }
                }
                break;
                    
                case 1: // Segments
                    // <segment id='13794186' timestamp='2007-08-04 22:28:47' visible='true' from='17233948' to='24959146' />
                    for (id = 0; id < segs_per_way; id++) {
                        printf(INDENT "<segment id='%s%d' timestamp='%d-%d-%d %d:%d:%d' visible='%s' from='%s%d' to='%s%d' />\n",
                               sign, seg_offset + id, ts->tm_year + 1900, ts->tm_mon, ts->tm_mday, ts->tm_hour, ts->tm_min, ts->tm_sec,
                               visible ? "true" : "false", sign, node_offset + id, sign, node_offset + id + 1);
                    }

                    break;
                    
                    case 2: // Ways
                        printf(INDENT "<way id='%s%d' timestamp='%d-%d-%d %d:%d:%d' visible='%s'>\n",
                               sign, way + 1, ts->tm_year + 1900, ts->tm_mon, ts->tm_mday, ts->tm_hour, ts->tm_min, ts->tm_sec,
                               visible ? "true" : "false");

                        for (id = 0; id < segs_per_way; id++)
                            printf(INDENT INDENT "<seg id='%s%d' />\n", sign, seg_offset + id);

                        printf(INDENT INDENT "<tag k='name' v='way%d' />\n", way);
                        printf(INDENT INDENT "<tag k='highway' v='primary' />\n");
                        printf(INDENT "</way>\n");
                        break;
            }
        }
        printf("\n");
    }
}


int main(int argc, char **argv)
{
    int ways = 0, segs_per_way = 0;

    if (argc < 3) {
      const char *name = basename(argv[0]);
      fprintf(stderr, "Usage:\n");
      fprintf(stderr, "\t%s <num-ways> <segments-per-way>\n", name);
      fprintf(stderr, "\n\nThis is a trival tool to generate OSM data of an arbitrary size.\n");
      fprintf(stderr, "This may be used to test the performance and scalability of a variety of OSM tools.\n");
      fprintf(stderr, "\ne.g. %s 1000 4 > fake1k.osm\n", name);
      fprintf(stderr, "     josm fake1k.osm\n\n");
      fprintf(stderr, "This is intended as a tool for developers. If you want something other than a simple\n");
      fprintf(stderr, "grid of ways then you will have to edit the source!\n\n");
      
      return 1;
    }

    ways = strtoul(argv[1], NULL, 10);
    segs_per_way = strtoul(argv[2], NULL, 10);

    fprintf(stderr, "Creating %d ways with %d segments per way.\n\n", ways, segs_per_way);

    printf("<?xml version='1.0' encoding='UTF-8'?>\n");
    printf("<osm version='0.3' generator='dbscale'>\n");
    create_data(ways, segs_per_way, 1);
    printf("</osm>\n");

    return 0;
}
