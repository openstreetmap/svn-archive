#include "output_osm.h"
#include <string.h>
#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#define INDENT "  "

#ifdef USE_ICONV
#include <iconv.h>
#define ICONV_ERROR ((iconv_t)-1)
static iconv_t cd = ICONV_ERROR;
#endif

/* const char *xmlescape(char *in)
 *
 * Character escaping for valid XML output as per http://www.w3.org/TR/REC-xml/
 *
 * WARNING: this function uses a static buffer so do not rely on the result
 * being constant if called more than once
 */
const char *xmlescape(const char *in)
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

void osm_tags(struct keyval *tags)
{
    struct keyval *p;

    while ((p = popItem(tags)) != NULL) {
        printf(INDENT INDENT "<tag k=\"%s\"", xmlescape(p->key));
        printf(" v=\"%s\" />\n", xmlescape(p->value));
        freeItem(p);
    }

   resetList(tags);
}

void osm_node(int id, long double lat, long double lon, struct keyval *tags, const char *ts, const char *user, int version, int changeset)
{
  if (listHasData(tags)) {
    printf(INDENT "<node id=\"%d\" lat=\"%.7Lf\" lon=\"%.7Lf\" "
	   "timestamp=\"%s\" version=\"%d\" changeset=\"%d\"%s>\n", 
	   id, lat, lon, ts, version, changeset, user);
    osm_tags(tags);
    printf(INDENT "</node>\n");
  } else {
    printf(INDENT "<node id=\"%d\" lat=\"%.7Lf\" lon=\"%.7Lf\" "
	   "timestamp=\"%s\" version=\"%d\" changeset=\"%d\"%s/>\n", 
	   id, lat, lon, ts, version, changeset, user);
  }
}

void osm_way(int id, struct keyval *nodes, struct keyval *tags, const char *ts, const char *user, int version, int changeset)
{
  struct keyval *p;
  
  if (listHasData(tags) || listHasData(nodes)) {
    printf(INDENT "<way id=\"%d\" timestamp=\"%s\" version=\"%d\" changeset=\"%d\"%s>\n", id, ts, version, changeset, user);
    while ((p = popItem(nodes)) != NULL) {
      printf(INDENT INDENT "<nd ref=\"%s\" />\n", p->value);
      freeItem(p);
    }
    osm_tags(tags);
    printf(INDENT "</way>\n");
  } else {
    printf(INDENT "<way id=\"%d\" timestamp=\"%s\" version=\"%d\" changeset=\"%d\"%s/>\n", id, ts, version, changeset, user);
  }
}

void osm_relation(int id, struct keyval *members, struct keyval *roles, struct keyval *tags, const char *ts, const char *user, int version, int changeset)
{
  struct keyval *p, *q;
  
  if (listHasData(tags) || listHasData(members)) {
    printf(INDENT "<relation id=\"%d\" timestamp=\"%s\" version=\"%d\" changeset=\"%d\"%s>\n", id, ts, version, changeset, user);
    while (((p = popItem(members)) != NULL) && ((q = popItem(roles)) != NULL)) {
      char *m_type = p->key;
      char *i; 
      for (i = m_type; *i; i++) *i = tolower(*i);
      const char *m_id   = p->value;
      const char *m_role = q->value;
      printf(INDENT INDENT "<member type=\"%s\" ref=\"%s\" role=\"%s\"/>\n", m_type, m_id, m_role);
      freeItem(p);
      freeItem(q);
    }
    osm_tags(tags);
    printf(INDENT "</relation>\n");
  } else {
    printf(INDENT "<relation id=\"%d\" timestamp=\"%s\" version=\"%d\" changeset=\"%d\"%s/>\n", id, ts, version, changeset, user);
  }
}

/**
 * output the header of the osm XML file.
 *
 * note the *lovely* C-style error handling...
 */
void osm_header() {
  char timestamp[200];
  time_t t;
  struct tm *tmp;

  t = time(NULL);
  tmp = gmtime(&t);
  if (tmp == NULL) {
    perror("gmtime");
    exit(1);
  }

  if (strftime(timestamp, sizeof(timestamp), 
	       "%Y-%m-%dT%H:%M:%SZ", tmp) == 0) {
    fprintf(stderr, "ERROR: strftime returned NULL.\n");
    exit(1);
  }

  printf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
  printf("<osm version=\"0.6\" generator=\"OpenStreetMap planet.c\" "
	 "timestamp=\"%s\">\n", timestamp);
  printf(INDENT "<bound box=\"-90,-180,90,180\" "
	 "origin=\"http://www.openstreetmap.org/api/0.6\" />\n");
}

/**
 * close off the XML file
 */
void osm_footer() {
  printf("</osm>\n");
}
