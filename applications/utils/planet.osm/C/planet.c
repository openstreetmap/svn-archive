#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <limits.h>
#include <time.h>
#include <utime.h>


#include <mysql.h>
#include <mysqld_error.h>
#include <signal.h>
#include <stdarg.h>
#include <sslopt-vars.h>
#include <assert.h>

#include "keyvals.h"

#define INDENT "  "

static char escape_tmp[1024];

const char *xmlescape(const char *in)
{ 
    /* character escaping as per http://www.w3.org/TR/REC-xml/ */

    /* WARNING: this funxtion uses a static buffer so do not rely on the result
     * being constant if called more than once
     */
    bzero(escape_tmp, sizeof(escape_tmp));
    while(*in) {
        int len = strlen(escape_tmp);
        int left = sizeof(escape_tmp) - len - 1;

        if (left < 7)
            break;

        switch(*in) {
            case  '&': strncat(escape_tmp, "&amp;",  left); break;
            //case '\'': strncat(escape_tmp, "&apos;", left); break;
            case  '<': strncat(escape_tmp, "&lt;",   left); break;
            case  '>': strncat(escape_tmp, "&gt;",   left); break;
            case  '"': strncat(escape_tmp, "&quot;", left); break;
            default: escape_tmp[len] = *in;
        }
        in++;
    }
    return escape_tmp;
}

static void osm_tags(struct keyval *tags)
{
    struct keyval *p;

    while ((p = popItem(tags)) != NULL) {
        printf(INDENT INDENT "<tag k=\"%s\"", xmlescape(p->key));
        printf(" v=\"%s\" />\n", xmlescape(p->value));
        freeItem(p);
    }

   resetList(tags);
}

static void osm_node(int id, long double lat, long double lon, struct keyval *tags, const char *ts)
{
    if (listHasData(tags)) {
        printf(INDENT "<node id=\"%d\" lat=\"%.7Lf\" lon=\"%.7Lf\" timestamp=\"%s\">\n", id, lat, lon, ts);
        osm_tags(tags);
        printf(INDENT "</node>\n");
    } else {
        printf(INDENT "<node id=\"%d\" lat=\"%.7Lf\" lon=\"%.7Lf\" timestamp=\"%s\"/>\n", id, lat, lon, ts);
    }
}

static void osm_segment(int id, int from, int to, struct keyval *tags, const char *ts)
{
    if (listHasData(tags)) {
        printf(INDENT "<segment id=\"%d\" from=\"%d\" to=\"%d\" timestamp=\"%s\">\n", id, from, to, ts);
        osm_tags(tags);
        printf(INDENT "</segment>\n");
    } else {
        printf(INDENT "<segment id=\"%d\" from=\"%d\" to=\"%d\" timestamp=\"%s\"/>\n", id, from, to, ts);
    }
}

static void osm_way(int id, struct keyval *segs, struct keyval *tags, const char *ts)
{
    struct keyval *p;

    if (listHasData(tags) || listHasData(segs)) {
        printf(INDENT "<way id=\"%d\" timestamp=\"%s\">\n", id, ts);
        while ((p = popItem(segs)) != NULL) {
            printf(INDENT INDENT "<seg id=\"%s\" />\n", p->value);
            freeItem(p);
        }
        osm_tags(tags);
        printf(INDENT "</way>\n");
    } else {
        printf(INDENT "<way id=\"%d\" timestamp=\"%s\"/>\n", id, ts);
    }
}


void read_tags(const char *str, struct keyval *tags)
{
   enum tagState { sKey, sValue, sDone, sEnd} s;
   char *key, *value;
   const char *p, *key_start, *value_start;

   if (!str || !*str)
    return;
   // key=value;key=value;...
   p = str;
   key_start = p;
   s = sKey;
   value_start = key = value = NULL;
   while(s != sEnd) {
       switch(s) {
           case sKey:
               if (*p == '=') {
                   key = strndup(key_start, p - key_start);
                   s = sValue;
                   value_start = p+1;
               }
               p++;
               break;

           case sValue:
               if (!*p || *p == ';') {
                   value = strndup(value_start, p - value_start);
                   s = sDone;
                   key_start = p+1;
               }
               if (*p) p++;
               break;

           case sDone:
               //printf("%s=%s\n", key, value);
               addItem(tags, key, value, 0);
               free(key);
               free(value);
               s = *p ? sKey : sEnd;
               break;

           case sEnd:
               break;
       }
   }
}

void parseDate(struct tm *tm, const char *str)
{
    time_t tmp;
    // 2007-05-20 13:51:35
    bzero(tm, sizeof(*tm));
    int n = sscanf(str, "%d-%d-%d %d:%d:%d",
                   &tm->tm_year, &tm->tm_mon, &tm->tm_mday, &tm->tm_hour, &tm->tm_min, &tm->tm_sec);

    if (n !=6)
        printf("failed to parse date string, got(%d): %s\n", n, str);
 
    tm->tm_year -= 1900;
    tm->tm_mon  -= 1;
    tm->tm_isdst = -1;

    // Converting to/from time_t ensures the tm_isdst field gets set to indicate GMT/BST
    // Rails stores the timestamps in the DB using UK localtime.
    tmp = mktime(tm);
    localtime_r(&tmp, tm);
}

const char *strTime(struct tm *tm)
{
    static char out[64]; // Not thread safe

    //2000-01-04T12:02:09+00:00
    snprintf(out, sizeof(out), "%d-%02d-%02dT%02d:%02d:%02d+0%c:00",
             tm->tm_year+1900, tm->tm_mon+1, tm->tm_mday, tm->tm_hour, tm->tm_min, tm->tm_sec,
             tm->tm_isdst ? '1':'0');

    return out;
}

void nodes(MYSQL *mysql)
{
    char query[255];
    MYSQL_RES *res;
    MYSQL_ROW row;
    struct keyval tags;

    initList(&tags);

    snprintf(query, sizeof(query), "select id, latitude, longitude, timestamp, tags from current_nodes where visible = 1 order by id");

    if ((mysql_query(mysql, query)) || !(res= mysql_use_result(mysql)))
    {
        fprintf(stderr,"Cannot query nodes: %s\n", mysql_error(mysql));
        exit(1);
    }

    while ((row= mysql_fetch_row(res))) {
        long int id;
        long double latitude,longitude;
        const char *tag_str;
        struct tm date;

        assert(mysql_num_fields(res) == 5);

        id = strtol(row[0], NULL, 10);
#ifdef SCHEMA_V6
        latitude  = strtol(row[1], NULL, 10) / 10000000.0;
        longitude = strtol(row[2], NULL, 10) / 10000000.0;
#else
        latitude  = strtold(row[1], NULL);
        longitude = strtold(row[2], NULL);
#endif
        parseDate(&date, row[3]);
        tag_str = row[4];
        read_tags(tag_str, &tags);

        osm_node(id, latitude, longitude, &tags, strTime(&date));
    }

    mysql_free_result(res);
}

void segments(MYSQL *mysql)
{
    char query[255];
    MYSQL_RES *res;
    MYSQL_ROW row;
    struct keyval tags;

    initList(&tags);

    snprintf(query, sizeof(query), "select id, node_a, node_b, timestamp, tags from current_segments where visible = 1 order by id");

    if ((mysql_query(mysql, query)) || !(res= mysql_use_result(mysql)))
    {
        fprintf(stderr,"Cannot query segments: %s\n", mysql_error(mysql));
        exit(1);
    }

    while ((row= mysql_fetch_row(res))) {
        long int id, node_a, node_b;
        const char *tag_str;
        struct tm date;

        assert(mysql_num_fields(res) == 5);

        id     = strtol(row[0], NULL, 10);
        node_a = strtol(row[1], NULL, 10);
        node_b = strtol(row[2], NULL, 10);
        parseDate(&date, row[3]);
        tag_str = row[4];

        read_tags(tag_str, &tags);
        osm_segment(id, node_a, node_b, &tags, strTime(&date));
    }

    mysql_free_result(res);
}

void get_way_tags(MYSQL *mysql, const int id, struct keyval *tags)
{
    unsigned long length[2];
    my_bool       is_null[2];
    my_bool       error[2];
    static MYSQL_STMT *stmt = NULL;
    MYSQL_BIND tags_bind_param[1];
    MYSQL_BIND tags_bind_res[2];
    char key[256], value[256];

    if (!stmt) {
        const char *query = "SELECT k, v FROM current_way_tags WHERE id=?";
        MYSQL_RES *prepare_meta_result;
        stmt = mysql_stmt_init(mysql);
        assert(stmt);
        if (mysql_stmt_prepare(stmt, query, strlen(query))) {
            fprintf(stderr,"Cannot setup prepared query for current_way_tags: %s\n", mysql_error(mysql));
            exit(1);
        }
        assert(mysql_stmt_param_count(stmt) == 1);
        prepare_meta_result = mysql_stmt_result_metadata(stmt);
        assert(prepare_meta_result);
        assert(mysql_num_fields(prepare_meta_result) == 2);
        mysql_free_result(prepare_meta_result);

        // FIXME: The prepared statment handle is leaked (but only once)
        // mysql_stmt_close(stmt)
    }

    memset(tags_bind_param, 0, sizeof(tags_bind_param));
    tags_bind_param[0].buffer_type= MYSQL_TYPE_LONG;
    tags_bind_param[0].buffer= (char *)&id;
    tags_bind_param[0].is_null= 0;
    tags_bind_param[0].length= 0;

    if (mysql_stmt_bind_param(stmt, tags_bind_param))
    {
        fprintf(stderr, " mysql_stmt_bind_param() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(stmt));
        exit(0);
    }

    if (mysql_stmt_execute(stmt))
    {
        fprintf(stderr, " mysql_stmt_execute(), 1 failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(stmt));
        exit(0);
    }

    memset(tags_bind_res, 0, sizeof(tags_bind_res));

    tags_bind_res[0].buffer_type= MYSQL_TYPE_VAR_STRING;
    tags_bind_res[0].buffer_length= sizeof(key);
    tags_bind_res[0].buffer= key;
    tags_bind_res[0].is_null= &is_null[0];
    tags_bind_res[0].length= &length[0];
    tags_bind_res[0].error= &error[0];

    tags_bind_res[1].buffer_type= MYSQL_TYPE_VAR_STRING;
    tags_bind_res[1].buffer_length= sizeof(value);
    tags_bind_res[1].buffer= value;
    tags_bind_res[1].is_null= &is_null[1];
    tags_bind_res[1].length= &length[1];
    tags_bind_res[1].error= &error[1];


    if (mysql_stmt_bind_result(stmt, tags_bind_res))
    {
        fprintf(stderr, " mysql_stmt_bind_result() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(stmt));
        exit(0);
    }

    if (mysql_stmt_store_result(stmt))
    {
        fprintf(stderr, " mysql_stmt_store_result() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(stmt));
        exit(0);
    }

    while (!mysql_stmt_fetch(stmt))
        addItem(tags, key, value, 0);
}


void ways(MYSQL *ways_mysql, MYSQL *segs_mysql, MYSQL *tags_mysql)
{
    char ways_query[255], segs_query[255];
    MYSQL_RES *ways_res, *segs_res;
    MYSQL_ROW ways_row, segs_row;
    struct keyval tags, segs;

    initList(&tags);
    initList(&segs);

    snprintf(ways_query, sizeof(ways_query),
             "select id, timestamp from current_ways where visible = 1 order by id");
    snprintf(segs_query, sizeof(segs_query),
             "select id, segment_id from current_way_segments ORDER BY id, sequence_id");

    if ((mysql_query(ways_mysql, ways_query)) || !(ways_res= mysql_use_result(ways_mysql)))
    {
        fprintf(stderr,"Cannot query current_ways: %s\n", mysql_error(ways_mysql));
        exit(1);
    }
    if ((mysql_query(segs_mysql, segs_query)) || !(segs_res= mysql_use_result(segs_mysql)))
    {
        fprintf(stderr,"Cannot query current_way_segments: %s\n", mysql_error(segs_mysql));
        exit(1);
    }

    ways_row = mysql_fetch_row(ways_res);
    segs_row = mysql_fetch_row(segs_res);

    while (ways_row) {
        int way_id     = strtol(ways_row[0], NULL, 10);
        // Terminating way_seg_id is necessary to ensure final way is generated.
        int way_seg_id = segs_row ? strtol(segs_row[0], NULL, 10): INT_MAX;

        if (way_id < way_seg_id) {
            // no more segments in this way
            struct tm date;
            parseDate(&date, ways_row[1]);
            get_way_tags(tags_mysql, way_id, &tags);
            osm_way(way_id, &segs, &tags, strTime(&date));
            // fetch new way
            ways_row= mysql_fetch_row(ways_res);
            assert(mysql_num_fields(ways_res) == 2);
        } else if (way_id > way_seg_id) {
            // we have entries in current_way_segs for a missing way, discard!
            // fetch next way_seg
            segs_row = mysql_fetch_row(segs_res);
            assert(mysql_num_fields(segs_res) == 2);
        } else {
            // in step, add current segment and fetch the next one
            addItem(&segs, "", segs_row[1], 0);
            segs_row = mysql_fetch_row(segs_res);
            assert(mysql_num_fields(segs_res) == 2);
        }
    }

    mysql_free_result(ways_res);
    mysql_free_result(segs_res);
}

int main(int argc, char **argv)
{
    // 3 MySQL connections are required to fetch way data from multiple tables
#define NUM_CONN (3)
    MYSQL mysql[NUM_CONN];
    int i;

    // Database timestamps use UK localtime
    setenv("TZ", ":GB", 1);

    for (i=0; i<NUM_CONN; i++) {
        mysql_init(&mysql[i]);

        if (mysql_options(&mysql[i], MYSQL_SET_CHARSET_NAME , "utf8")) {
            fprintf(stderr, "set options failed\n");
            exit(1);
        }

        if (!(mysql_real_connect(&mysql[i],"","openstreetmap","openstreetmap","openstreetmap",MYSQL_PORT,NULL,0)))
        {
            fprintf(stderr,"%s: %s\n",argv[0],mysql_error(&mysql[i]));
            exit(1);
        }
        mysql[i].reconnect= 1;
    }
    printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    printf("<osm version=\"0.3\" generator=\"OpenStreetMap planet.c\">\n");
    printf("  <bound box=\"-90,-180,90,180\" origin=\"http://www.openstreetmap.org/api/0.4\" />\n");

    nodes(&mysql[0]);
    segments(&mysql[0]);
    ways(&mysql[0], &mysql[1], &mysql[2]);

    printf("</osm>\n");

    for (i=0; i<NUM_CONN; i++)
        mysql_close(&mysql[i]);

    return 0;
}
