#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

#undef USE_ICONV

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


#ifdef USE_ICONV
#include <iconv.h>
#define ICONV_ERROR ((iconv_t)-1)
static iconv_t cd = ICONV_ERROR;
#endif


static char **user_list;
static unsigned long max_uid;

void connection_close(MYSQL *c)
{
    mysql_close(c);
    free(c);
}

MYSQL *connection_open(void)
{
    const char *set_timeout1 = "SET SESSION net_write_timeout=60*60*12";
    const char *set_timeout2 = "SET SESSION interactive_timeout=60*60*12";
    const char *set_timeout3 = "SET SESSION wait_timeout=60*60*12";
    MYSQL *c = malloc(sizeof(MYSQL));

    assert(c);
    mysql_init(c);

#if 0
    if (mysql_options(c, MYSQL_SET_CHARSET_NAME , "utf8")) {
        fprintf(stderr, "set options failed\n");
        exit(1);
    }
#endif

    if (!(mysql_real_connect(c,"","openstreetmap","openstreetmap","openstreetmap",MYSQL_PORT,NULL,0)))
    {
        fprintf(stderr,"FAILED database connection: %s\n",mysql_error(c));
        exit(1);
    }

    if (mysql_query(c, set_timeout1)) {
        fprintf(stderr,"FAILED %s: %s\n", set_timeout1, mysql_error(c));
        exit(1);
    }
    if (mysql_query(c, set_timeout2)) {
        fprintf(stderr,"FAILED %s: %s\n", set_timeout2, mysql_error(c));
        exit(1);
    }
    if (mysql_query(c, set_timeout3)) {
        fprintf(stderr,"FAILED %s: %s\n", set_timeout3, mysql_error(c));
        exit(1);
    }

    return c;
}



/* const char *xmlescape(char *in)
 *
 * Character escaping for valid XML output as per http://www.w3.org/TR/REC-xml/
 *
 * WARNING: this function uses a static buffer so do not rely on the result
 * being constant if called more than once
 */
static const char *xmlescape(char *in)
{ 
    static char escape_tmp[1024];
    int len;
    // Convert from DB charset to UTF8
    // Note: this assumes that inbuf is C-string compatible, i.e. has no embedded NUL like UTF16!
    // To fix this we'd need to fix the DB output parameters too
#ifdef USE_ICONV 
    if (cd != ICONV_ERROR) {
        char iconv_tmp[1024];
        char *inbuf = in, *outbuf = iconv_tmp;
        size_t ret;
        size_t inlen = strlen(inbuf);
        size_t outlen = sizeof(iconv_tmp);
        bzero(iconv_tmp, sizeof(iconv_tmp));
        iconv(cd, NULL, 0, NULL, 0);

        ret = iconv(cd, &inbuf, &inlen, &outbuf, &outlen);

        if (ret == -1) {
            fprintf(stderr, "failed to convert '%s'\n", in);
            // Carry on regardless
        }
        in = iconv_tmp;
    }
#endif

    len = 0;
    while(*in) {
        int left = sizeof(escape_tmp) - len - 1;

        if (left < 7)
            break;

        if (*in == '&') {
            strcpy(&escape_tmp[len], "&amp;");
            len += strlen("&amp;");
        } else if (*in == '<') {
            strcpy(&escape_tmp[len], "&lt;");
            len += strlen("&lt;");
        } else if (*in == '>') {
            strcpy(&escape_tmp[len], "&gt;");
            len += strlen("&lt;");
        } else if (*in == '"') {
            strcpy(&escape_tmp[len], "&quot;");
            len += strlen("&quot;");
        } else if ((*in >= 0) && (*in < 32)) {
            escape_tmp[len] = '?';
            len++;
        } else {
            escape_tmp[len] = *in;
            len++;
        }
	
        in++;
    }
    escape_tmp[len] = '\0';
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

static const char *lookup_user(const char *s)
{
    unsigned long int user_id = strtoul(s, NULL, 10);
    const char *user = (user_id >= 0 && user_id <= max_uid) ? user_list[user_id] : "";
    return user ? user : "";
}

static void osm_node(int id, long double lat, long double lon, struct keyval *tags, const char *ts, const char *user)
{
    if (listHasData(tags)) {
        printf(INDENT "<node id=\"%d\" lat=\"%.7Lf\" lon=\"%.7Lf\" timestamp=\"%s\"%s>\n", id, lat, lon, ts, user);
        osm_tags(tags);
        printf(INDENT "</node>\n");
    } else {
        printf(INDENT "<node id=\"%d\" lat=\"%.7Lf\" lon=\"%.7Lf\" timestamp=\"%s\"%s/>\n", id, lat, lon, ts, user);
    }
}

static void osm_way(int id, struct keyval *nodes, struct keyval *tags, const char *ts, const char *user)
{
    struct keyval *p;

    if (listHasData(tags) || listHasData(nodes)) {
        printf(INDENT "<way id=\"%d\" timestamp=\"%s\"%s>\n", id, ts, user);
        while ((p = popItem(nodes)) != NULL) {
            printf(INDENT INDENT "<nd ref=\"%s\" />\n", p->value);
            freeItem(p);
        }
        osm_tags(tags);
        printf(INDENT "</way>\n");
    } else {
        printf(INDENT "<way id=\"%d\" timestamp=\"%s\"%s/>\n", id, ts, user);
    }
}

static void osm_relation(int id, struct keyval *members, struct keyval *roles, struct keyval *tags, const char *ts, const char *user)
{
    struct keyval *p, *q;

    if (listHasData(tags) || listHasData(members)) {
        printf(INDENT "<relation id=\"%d\" timestamp=\"%s\"%s>\n", id, ts, user);
        while (((p = popItem(members)) != NULL) && ((q = popItem(roles)) != NULL)) {
            const char *m_type = p->key;
            const char *m_id   = p->value;
            const char *m_role = q->value;
            printf(INDENT INDENT "<member type=\"%s\" ref=\"%s\" role=\"%s\"/>\n", m_type, m_id, m_role);
            freeItem(p);
            freeItem(q);
        }
        osm_tags(tags);
        printf(INDENT "</relation>\n");
    } else {
        printf(INDENT "<relation id=\"%d\" timestamp=\"%s\"%s/>\n", id, ts, user);
    }
}

/* In-place unescape the tag string. The char * passed in will
   be returned from the function. Unescaping can only ever make
   the string shorter so this is safe */
char *tag_unescape(char *str)
{
   char *p = str;
   char *w = str;
   char ch;

   while ( *p ) {
       ch = *p++;
       if ( ch == '\\' ) {
           ch = *p++;
           switch ( ch ) {
           case 's': ch = ';'; break;
           case 'e': ch = '='; break;
           case '\\': ch = '\\'; break;
           default:
               // Drop the \ and allow any other char to pass unchanged
               break;
           }
       }
       *w++ = ch;
   }
   *w = '\0';
   return str;
}

void read_tags(const char *str, struct keyval *tags)
{
   enum tagState { sKey, sValue, sDone, sEnd} s;
   char *key, *value;
   const char *p, *key_start, *value_start;

   if (!str || !*str)
    return;
   // key=value;key=value;...
   // Note: This is a simple algorithm which makes no attempt to reconstruct odd data like:
   // a=b=c  or
   // key=value1;value2
   p = str;
   key_start = p;
   s = sKey;
   value_start = key = value = NULL;
   while(s != sEnd) {
       switch(s) {
           case sKey:
               if (!*p) {
                   s = sEnd;
                   break;
               }
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
               addItem(tags, tag_unescape(key), tag_unescape(value), 0);
               free(key);
               free(value);
               s = sKey;
               break;

           case sEnd: /* Never reached, but keeps compiler happy */
               break;
       }
   }
}

const char *reformDate(const char *str)
{
    static char out[64], prev[64]; // Not thread safe

    time_t tmp;
    struct tm tm;

    // Re-use the previous answer if we asked to convert the same timestamp twice
    // This accelerates bulk uploaded data where sequential features often have the same timestamp
    if (!strncmp(prev, str, sizeof(prev)))
        return out;
    else
        strncpy(prev, str, sizeof(prev));

    // 2007-05-20 13:51:35
    bzero(&tm, sizeof(tm));
    int n = sscanf(str, "%d-%d-%d %d:%d:%d",
                   &tm.tm_year, &tm.tm_mon, &tm.tm_mday, &tm.tm_hour, &tm.tm_min, &tm.tm_sec);

    if (n !=6)
        printf("failed to parse date string, got(%d): %s\n", n, str);

    tm.tm_year -= 1900;
    tm.tm_mon  -= 1;
    tm.tm_isdst = -1;

    // Rails stores the timestamps in the DB using UK localtime (ugh), convert to UTC
    tmp = mktime(&tm);
    gmtime_r(&tmp, &tm);

    //2007-07-10T11:32:32Z
    snprintf(out, sizeof(out), "%d-%02d-%02dT%02d:%02d:%02dZ",
             tm.tm_year+1900, tm.tm_mon+1, tm.tm_mday, tm.tm_hour, tm.tm_min, tm.tm_sec);

    return out;
}


#define TAG_CACHE (10000)

struct tCache {
    int id;
    struct keyval tags;
};

static struct tCache cache[TAG_CACHE+1];
static MYSQL_STMT *tags_stmt;
static MYSQL *tags_mysql;
static int cache_off;

void tags_init(const char *table)
{
    int i;
    char query[255];
    MYSQL_RES *prepare_meta_result;
    
    assert(tags_mysql == NULL); // Only allowed a single table open at once
    tags_mysql = connection_open();
    tags_stmt = mysql_stmt_init(tags_mysql);
    assert(tags_stmt);

    snprintf(query, sizeof(query), "SELECT id, k, v FROM %s WHERE id >= ? ORDER BY id LIMIT 10000", table); // LIMIT == TAG_CACHE

    if (mysql_stmt_prepare(tags_stmt, query, strlen(query))) {
        fprintf(stderr,"Cannot setup prepared query for %s: %s\n", table, mysql_error(tags_mysql));
        exit(1);
    }
    assert(mysql_stmt_param_count(tags_stmt) == 1);
    prepare_meta_result = mysql_stmt_result_metadata(tags_stmt);
    assert(prepare_meta_result);
    assert(mysql_num_fields(prepare_meta_result) == 3);
    mysql_free_result(prepare_meta_result);

    for (i=0; i< TAG_CACHE; i++) {
        initList(&cache[i].tags);
        cache[i].id = 0;
    }
    cache_off = 0;
}

void tags_exit(void)
{
    int i;

    mysql_stmt_close(tags_stmt);
    tags_stmt = NULL;
    connection_close(tags_mysql);
    tags_mysql = NULL;

    for (i=0; i< TAG_CACHE; i++)
        resetList(&cache[i].tags);
}

void refill_tags(const int id)
{
    unsigned long length[3];
    my_bool       is_null[3];
    my_bool       error[3];
    MYSQL_BIND tags_bind_param[1];
    MYSQL_BIND tags_bind_res[3];
    char key[256], value[256];
    int i, row_id, last_id, cache_slot, result;

    for (i=0; i<TAG_CACHE; i++) {
        if (!cache[i].id)
            break;
        resetList(&cache[i].tags);
        cache[i].id = 0;
    }

    memset(tags_bind_param, 0, sizeof(tags_bind_param));
    tags_bind_param[0].buffer_type= MYSQL_TYPE_LONG;
    tags_bind_param[0].buffer= (char *)&id;
    tags_bind_param[0].is_null= 0;
    tags_bind_param[0].length= 0;

    if (mysql_stmt_bind_param(tags_stmt, tags_bind_param)) {
        fprintf(stderr, " mysql_stmt_bind_param() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(tags_stmt));
        exit(1);
    }

    if (mysql_stmt_execute(tags_stmt))
    {
        fprintf(stderr, " mysql_stmt_execute(), 1 failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(tags_stmt));
        exit(1);
    }

    memset(tags_bind_res, 0, sizeof(tags_bind_res));

    tags_bind_res[0].buffer_type= MYSQL_TYPE_LONG;
    tags_bind_res[0].buffer= (char *)&row_id;
    tags_bind_res[0].is_null= &is_null[0];
    tags_bind_res[0].length= &length[0];
    tags_bind_res[0].error= &error[0];

    tags_bind_res[1].buffer_type= MYSQL_TYPE_VAR_STRING;
    tags_bind_res[1].buffer_length= sizeof(key);
    tags_bind_res[1].buffer= key;
    tags_bind_res[1].is_null= &is_null[0];
    tags_bind_res[1].length= &length[0];
    tags_bind_res[1].error= &error[0];

    tags_bind_res[2].buffer_type= MYSQL_TYPE_VAR_STRING;
    tags_bind_res[2].buffer_length= sizeof(value);
    tags_bind_res[2].buffer= value;
    tags_bind_res[2].is_null= &is_null[1];
    tags_bind_res[2].length= &length[1];
    tags_bind_res[2].error= &error[1];


    if (mysql_stmt_bind_result(tags_stmt, tags_bind_res))
    {
        fprintf(stderr, " mysql_stmt_bind_result() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(tags_stmt));
        exit(1);
    }

    if (mysql_stmt_store_result(tags_stmt))
    {
        fprintf(stderr, " mysql_stmt_store_result() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(tags_stmt));
        exit(1);
    }

    cache_slot = 0;
    last_id = 0;
    while ((result = mysql_stmt_fetch(tags_stmt)) == 0) {
        if (last_id != row_id) {
            if (last_id)
               cache_slot++;
            cache[cache_slot].id = row_id;
            last_id = row_id;
        }
        addItem(&cache[cache_slot].tags, key, value, 0);
    }

    if (result != MYSQL_NO_DATA) {
        fprintf(stderr, " mysql_stmt_fetch() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(tags_stmt));
        exit(1);
    }

    if (mysql_stmt_free_result(tags_stmt))
    {
        fprintf(stderr, " mysql_stmt_free_result() failed\n");
        fprintf(stderr, " %s\n", mysql_stmt_error(tags_stmt));
        exit(1);
    }

    // We need to clean out final slot since it may be truncated, unless
    // we only got a single slot filled then we hit the end of the table
    // which we assume _is_ complete
    if (cache_slot) {
        resetList(&cache[cache_slot].tags);
        cache[cache_slot].id = 0;
    } else {
        // This algorithm can not cope with > TAG_CACHE on a single way
        assert(countList(&cache[cache_slot].tags) != TAG_CACHE);
    }
}

struct keyval *get_generic_tags(const int id)
{
    while (1) {
        if (!cache[cache_off].id) {
            if (cache_off == 1)
                return NULL; // No more tags in DB table
            refill_tags(id);
            cache_off = 0;
        }

        if (cache[cache_off].id > id)
            return NULL; // No tags for this way ID

        if (cache[cache_off].id == id)
            return &cache[cache_off++].tags;

        cache_off++;
        assert (cache_off <= TAG_CACHE);
    }
}

void nodes(void)
{
    char query[255];
    MYSQL_RES *res;
    MYSQL_ROW row;
    struct keyval tags;
    MYSQL *mysql = connection_open();

    initList(&tags);

    snprintf(query, sizeof(query), "select id, latitude, longitude, timestamp, tags, user_id from current_nodes where visible = 1 order by id");

    if ((mysql_query(mysql, query)) || !(res= mysql_use_result(mysql)))
    {
        fprintf(stderr,"Cannot query nodes: %s\n", mysql_error(mysql));
        exit(1);
    }

    while ((row= mysql_fetch_row(res))) {
        long int id;
        long double latitude,longitude;

        assert(mysql_num_fields(res) == 6);

        id = strtol(row[0], NULL, 10);
        latitude  = strtol(row[1], NULL, 10) / 10000000.0;
        longitude = strtol(row[2], NULL, 10) / 10000000.0;
        read_tags(row[4], &tags);

        osm_node(id, latitude, longitude, &tags, reformDate(row[3]), lookup_user(row[5]));
    }

    if (mysql_errno(mysql)) {
        fprintf(stderr, " mysql_fetch_row() failed\n");
        fprintf(stderr, " %s\n", mysql_error(mysql));
        exit(1);
    }

    mysql_free_result(res);
    connection_close(mysql);
}

void ways(void)
{
    char ways_query[255], nodes_query[255];
    MYSQL_RES *ways_res, *nodes_res;
    MYSQL_ROW ways_row, nodes_row;
    struct keyval *tags, nodes;
    MYSQL *ways_mysql = connection_open();
    MYSQL *nodes_mysql = connection_open();

    initList(&nodes);

    snprintf(ways_query, sizeof(ways_query),
             "select id, timestamp, user_id from current_ways where visible = 1 order by id");
    snprintf(nodes_query, sizeof(nodes_query),
             "select id, node_id from current_way_nodes ORDER BY id, sequence_id");

    if ((mysql_query(ways_mysql, ways_query)) || !(ways_res= mysql_use_result(ways_mysql)))
    {
        fprintf(stderr,"Cannot query current_ways: %s\n", mysql_error(ways_mysql));
        exit(1);
    }
    if ((mysql_query(nodes_mysql, nodes_query)) || !(nodes_res= mysql_use_result(nodes_mysql)))
    {
        fprintf(stderr,"Cannot query current_way_nodes: %s\n", mysql_error(nodes_mysql));
        exit(1);
    }

    tags_init("current_way_tags");

    ways_row = mysql_fetch_row(ways_res);
    nodes_row = mysql_fetch_row(nodes_res);

    while (ways_row) {
        int way_id = strtol(ways_row[0], NULL, 10);
        // Terminating way_nd_id is necessary to ensure final way is generated.
        int way_nd_id = nodes_row ? strtol(nodes_row[0], NULL, 10): INT_MAX;

        if (way_id < way_nd_id) {
            // no more nodes in this way
            tags = get_generic_tags(way_id);
            osm_way(way_id, &nodes, tags, reformDate(ways_row[1]), lookup_user(ways_row[2]));
            // fetch new way
            ways_row= mysql_fetch_row(ways_res);
            assert(mysql_num_fields(ways_res) == 3);
        } else if (way_id > way_nd_id) {
            // we have entries in current_way_nodes for a missing way, discard!
            // fetch next way_seg
            nodes_row = mysql_fetch_row(nodes_res);
            assert(mysql_num_fields(nodes_res) == 2);
        } else {
            // in step, add current node and fetch the next one
            addItem(&nodes, "", nodes_row[1], 0);
            nodes_row = mysql_fetch_row(nodes_res);
            assert(mysql_num_fields(nodes_res) == 2);
        }
    }

    if (mysql_errno(ways_mysql)) {
        fprintf(stderr, " mysql_fetch_row() failed\n");
        fprintf(stderr, " %s\n", mysql_error(ways_mysql));
        exit(1);
    }
    if (mysql_errno(nodes_mysql)) {
        fprintf(stderr, " mysql_fetch_row() failed\n");
        fprintf(stderr, " %s\n", mysql_error(nodes_mysql));
        exit(1);
    }

    mysql_free_result(ways_res);
    mysql_free_result(nodes_res);
    tags_exit();
    connection_close(ways_mysql);
    connection_close(nodes_mysql);
}

void relations(void)
{
    char relations_query[255], members_query[255];
    MYSQL_RES *relations_res, *members_res;
    MYSQL_ROW relations_row, members_row;
    struct keyval *tags, members, roles;
    MYSQL *relations_mysql = connection_open();
    MYSQL *members_mysql = connection_open();

    initList(&members);
    initList(&roles);

    snprintf(relations_query, sizeof(relations_query),
             "select id, timestamp, user_id from current_relations where visible = 1 ORDER BY id");
    snprintf(members_query, sizeof(members_query),
             "select id, member_id, member_type, member_role from current_relation_members ORDER BY id");

    if ((mysql_query(relations_mysql, relations_query)) || !(relations_res= mysql_use_result(relations_mysql)))
    {
        fprintf(stderr,"Cannot query current_relations: %s\n", mysql_error(relations_mysql));
        exit(1);
    }
    if ((mysql_query(members_mysql, members_query)) || !(members_res= mysql_use_result(members_mysql)))
    {
        fprintf(stderr,"Cannot query current_relation_members: %s\n", mysql_error(members_mysql));
        exit(1);
    }

    tags_init("current_relation_tags");

    relations_row = mysql_fetch_row(relations_res);
    members_row = mysql_fetch_row(members_res);

    while (relations_row) {
        int relation_id     = strtol(relations_row[0], NULL, 10);
        // Terminating relation_memb_id is necessary to ensure final way is generated.
        int relation_memb_id = members_row ? strtol(members_row[0], NULL, 10): INT_MAX;

        if (relation_id < relation_memb_id) {
            // no more members in this way
            tags = get_generic_tags(relation_id);
            osm_relation(relation_id, &members, &roles, tags, reformDate(relations_row[1]), lookup_user(relations_row[2]));
            // fetch new way
            relations_row= mysql_fetch_row(relations_res);
            assert(mysql_num_fields(relations_res) == 3);
        } else if (relation_id > relation_memb_id) {
            // we have entries in current_way_members for a missing way, discard!
            // fetch next way_seg
            members_row = mysql_fetch_row(members_res);
            assert(mysql_num_fields(members_res) == 4);
        } else {
            // in step, add current member and fetch the next one
            const char *m_id   = members_row[1];
            const char *m_type = members_row[2];
            const char *m_role = members_row[3];

            addItem(&members, m_type, m_id, 0);
            addItem(&roles, "", m_role, 0);
            members_row = mysql_fetch_row(members_res);
            assert(mysql_num_fields(members_res) == 4);
        }
    }

    if (mysql_errno(relations_mysql)) {
        fprintf(stderr, " mysql_fetch_row() failed\n");
        fprintf(stderr, " %s\n", mysql_error(relations_mysql));
        exit(1);
    }
    if (mysql_errno(members_mysql)) {
        fprintf(stderr, " mysql_fetch_row() failed\n");
        fprintf(stderr, " %s\n", mysql_error(members_mysql));
        exit(1);
    }

    mysql_free_result(relations_res);
    mysql_free_result(members_res);
    tags_exit();
    connection_close(relations_mysql);
    connection_close(members_mysql);
}

unsigned long int max_userid(void)
{
    const char *sql = "SELECT MAX(id) FROM users";
    MYSQL_RES *res;
    MYSQL_ROW row;
    unsigned long int max = 0;
    MYSQL *mysql = connection_open();

    if ((mysql_query(mysql, sql)) || !(res= mysql_use_result(mysql))) {
        fprintf(stderr,"Cannot query users: %s\n", mysql_error(mysql));
        exit(1);
    }

    while ((row=mysql_fetch_row(res))) {
        max = strtol(row[0], NULL, 10);
    }

    if (mysql_errno(mysql)) {
        fprintf(stderr, " mysql_fetch_row() failed\n");
        fprintf(stderr, " %s\n", mysql_error(mysql));
        exit(1);
    }

    mysql_free_result(res);
    //printf("Maximum user id = %lu\n", max);
    connection_close(mysql);
    return max;
}


void fetch_users(void)
{
    char sql[256];
    MYSQL_RES *res;
    MYSQL_ROW row;
    MYSQL *mysql = connection_open();

    sprintf(sql, "SELECT id,display_name from users where id <= %lu and data_public = 1", max_uid);

    if ((mysql_query(mysql, sql)) || !(res= mysql_use_result(mysql))) {
        fprintf(stderr,"Cannot query users: %s\n", mysql_error(mysql));
        exit(1);
    }

    while ((row=mysql_fetch_row(res))) {
        unsigned long int id;
        const char *display_name;
        char tmp[1024];
        assert(mysql_num_fields(res) == 2);

        id = strtoul(row[0], NULL, 10);
        display_name = xmlescape(row[1]);
        assert(id <= max_uid);
        //user_list[id] = display_name;
        //printf("User: %lu %s\n", id, display_name);
        snprintf(tmp, sizeof(tmp), " user=\"%s\" uid=\"%lu\"", display_name, id);
        user_list[id] = strdup(tmp);
        assert(user_list[id]);
    }

    if (mysql_errno(mysql)) {
        fprintf(stderr, " mysql_fetch_row() failed\n");
        fprintf(stderr, " %s\n", mysql_error(mysql));
        exit(1);
    }

    mysql_free_result(res); 
    connection_close(mysql);
}


int main(int argc, char **argv)
{
#ifdef USE_ICONV
    MYSQL *mysql = NULL;
    MYSQL_ROW row;
    MYSQL_RES *res;
#endif
    int i;
    int want_nodes, want_ways, want_relations;

    // Database timestamps use UK localtime
    setenv("TZ", ":GB", 1);

    if (argc == 1)
        want_nodes = want_ways = want_relations = 1;
    else {
        want_nodes = want_ways = want_relations = 0;
        for(i=1; i<argc; i++) {
            if (!strcmp(argv[i], "--nodes"))
                want_nodes = 1;
            else if (!strcmp(argv[i], "--ways"))
                want_ways = 1;
            else if (!strcmp(argv[i], "--relations"))
                want_relations = 1;
            else {
                fprintf(stderr, "Usage error:\n");
                fprintf(stderr, "\t%s [--nodes] [--ways] [--relations]\n\n", argv[0]);
                fprintf(stderr, "Writes OSM planet dump to STDOUT. If no flags are specified then all data is output.\n");
                fprintf(stderr, "If one or more flags are set then only the requested data is dumped.\n");
                exit(2);
            }
        }
    }

#ifdef USE_ICONV
    mysql = connection_open();
    if (mysql_query(mysql, "SHOW VARIABLES like 'character_set_results'") || !(res= mysql_use_result(mysql))) {
            fprintf(stderr,"FAILED show variables: %s\n", mysql_error(mysql));
            exit(1);
    }

    if ((row= mysql_fetch_row(res))) {
        fprintf(stderr, "Setting up iconv for %s = %s\n", row[0], row[1]);
        cd = iconv_open("UTF8", row[1]);
        if (cd == (iconv_t)-1) {
            perror("iconv_open");
            exit(1);
        }
        row = mysql_fetch_row(res);
        assert(!row);
    } else {
        fprintf(stderr, "Failed to fetch DB charset, assuming UTF8\n");
    }
    connection_close(mysql);
    mysql = NULL;
#else
    /* Check we can connect to database */
    connection_close(connection_open());
#endif

    printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    printf("<osm version=\"0.5\" generator=\"OpenStreetMap planet.c\">\n");
    printf("  <bound box=\"-90,-180,90,180\" origin=\"http://www.openstreetmap.org/api/0.5\" />\n");

    max_uid = max_userid();
    user_list = calloc(max_uid + 1, sizeof(char *));
    if (!user_list) {
        fprintf(stderr, "Malloc of user_list failed for %lu users\n", max_uid);
        exit(1);
    }

    fetch_users();

    if (want_nodes)
        nodes();

    if (want_ways)
        ways();

    if (want_relations)
        relations();

    printf("</osm>\n");

#ifdef USE_ICONV
    iconv_close(cd);
#endif

    for(i=0; i<=max_uid; i++)
        free(user_list[i]);
    free(user_list);

    return 0;
}
