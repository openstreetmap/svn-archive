/* gpx-import/src/gpx.c
 *
 * GPX load and memory management
 *
 * Copyright Daniel Silverstone <dsilvers@digital-scurf.org>
 *
 * Written for the OpenStreetMap project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; version 2 of the License
 * only.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */

#include <sys/types.h>
#include <expat.h>
#include <stdlib.h>
#include <alloca.h>
#include <unistd.h>
#include <fcntl.h>
#include <stdbool.h>
#include <string.h>
#include <strings.h>
#include <stdio.h>
#include <errno.h>
#include <stdarg.h>
#include <time.h>

#include <archive.h>
#include <archive_entry.h>

#include <zlib.h>
#include <bzlib.h>

#include "gpx.h"

#define GPX_BUFLEN 4096
#define ERR_BUFFER_SIZE 1024

#define PARSE_ERROR(ctx,F...)                           \
  do {                                                  \
    if (ctx->err != NULL && *(ctx->err) == NULL)        \
      gpx_record_error(ctx, F);                         \
  } while (0)

void
gpx_free(GPX *gpx)
{
  GPXTrackPoint *pt = gpx->points, *ptnext;
  while (pt != NULL) {
    ptnext = pt->next;
    free(pt->timestamp);
    free(pt);
    pt = ptnext;
  }
  free(gpx);
}

typedef enum {
  UNKNOWN,
  TRACKPOINT,
  ELEVATION,
  TIMESTAMP,
} GPXParseState;

static const char *
gpx_state_name(GPXParseState p)
{
  switch (p) {
  case UNKNOWN:
    return "UNKNOWN";
  case TRACKPOINT:
    return "TRACKPOINT";
  case ELEVATION:
    return "ELEVATION";
  case TIMESTAMP:
    return "TIMESTAMP";
  }
  return "INVALID";
}

typedef struct {
  XML_Parser p;
  GPX *gpx;
  GPXTrackPoint *point;
  GPXTrackPoint *lastpoint;
  uint32_t curseg;
  char *accumulator;
  int accumulator_size;
  GPXParseState state;
  bool got_lat, got_long, got_ele, got_time;
  char **err;
  const char *subfile;
} GPXParseContext;

static void
gpx_record_error(GPXParseContext *ctx, char *fmt, ...)
{
  va_list va;
  int sz;
  char msg[ERR_BUFFER_SIZE];
  
  va_start(va, fmt);
  sz = vsnprintf(msg, ERR_BUFFER_SIZE, fmt, va);
  va_end(va);
  
  *(ctx->err) = calloc(2, ERR_BUFFER_SIZE);
  snprintf(*(ctx->err), 2 * ERR_BUFFER_SIZE, "%s%s%s%s\n  XML parser at line %ld column %ld",
           (ctx->subfile == NULL) ? "" : "In file ",
           (ctx->subfile == NULL) ? "" : ctx->subfile,
           (ctx->subfile == NULL) ? "" : " inside your upload:\n  ",
           msg,
           XML_GetCurrentLineNumber(ctx->p),
           XML_GetCurrentColumnNumber(ctx->p));
}

static GPXCoord
gpx_parse_coord(const XML_Char *str)
{
  GPXCoord ret = 0;
  bool is_neg = false;
  bool is_afterdot = false;
  int afterdot = 0;
  
  if (*str == '-') {
    is_neg = true;
    str++;
  }
  while (*str != '\0' && afterdot < 9) {
    if (*str == '.')
      is_afterdot = true;
    if (*str != '.') {
      ret *= 10;
      ret += (*str - '0');
      if (is_afterdot == true)
        afterdot++;
    }
    str++;
  }
  
  while (afterdot < 9) {
    afterdot++;
    ret *= 10;
  }
  
  return (is_neg ? -ret : ret);
}

static void
gpx_clear_accumulator(GPXParseContext *ctx)
{
  if (ctx->accumulator != NULL)
    free(ctx->accumulator);
  ctx->accumulator = NULL;
  ctx->accumulator_size = 0;
}

#define REQUIRE_STATE(S)                                                \
  if (ctx->state != (S)) {                                              \
    PARSE_ERROR(ctx, "Expected state %s, got state %s", gpx_state_name(S), gpx_state_name(ctx->state)); \
    XML_StopParser(ctx->p, 0);                                          \
    return;                                                             \
  }

static void
gpx_handle_start_element(void *_ctx, const XML_Char *name, const XML_Char **atts)
{
  GPXParseContext *ctx = (GPXParseContext *)(_ctx);
  if (strcmp(name, "trkpt") == 0) {
    REQUIRE_STATE(UNKNOWN);
    ctx->state = TRACKPOINT;
    ctx->point = calloc(1, sizeof(GPXTrackPoint));
    ctx->point->segment = ctx->curseg;
    ctx->got_lat = ctx->got_long = ctx->got_ele = ctx->got_time = false;
    while (*atts != NULL) {
      if (strcmp(*atts, "lat") == 0) {
        atts++;
        ctx->point->latitude = gpx_parse_coord(*atts++);
        ctx->got_lat = true;
      } else if (strcmp(*atts, "lon") == 0) {
        atts++;
        ctx->point->longitude = gpx_parse_coord(*atts++);
        ctx->got_long = true;
      } else {
        atts += 2; /* skip tag + value */
      }
    }
  } else if (strcmp(name, "ele") == 0) {
    REQUIRE_STATE(TRACKPOINT);
    ctx->state = ELEVATION;
    gpx_clear_accumulator(ctx);
  } else if (strcmp(name, "time") == 0) {
    if (ctx->state == TRACKPOINT) {
      ctx->state = TIMESTAMP;
      gpx_clear_accumulator(ctx);
    }
  }
}

#ifndef MIN
#define MIN(a,b) (((a) < (b)) ? (a) : (b))
#endif

#ifndef MAX
#define MAX(a,b) (((a) > (b)) ? (a) : (b))
#endif

static void
gpx_update_minmax(GPX *gpx, GPXTrackPoint *point)
{
  gpx->minlatitude = MIN(gpx->minlatitude, point->latitude);
  gpx->minlongitude = MIN(gpx->minlongitude, point->longitude);
  gpx->maxlatitude = MAX(gpx->maxlatitude, point->latitude);
  gpx->maxlongitude = MAX(gpx->maxlongitude, point->longitude);
}

static void
gpx_handle_end_element(void *_ctx, const XML_Char *name)
{
  GPXParseContext *ctx = (GPXParseContext *)(_ctx);
  if (strcmp(name, "trkpt") == 0) {
    REQUIRE_STATE(TRACKPOINT);
    /* Remove the || true if elevation is mandatory. */
    if (ctx->got_time == false)
      ctx->gpx->missed_time++;
    
    if ((ctx->got_lat == false) ||
        (ctx->point->latitude < -90000000000) ||
        (ctx->point->latitude > 90000000000))
      ctx->gpx->bad_lat++;
    
    if ((ctx->got_long == false) ||
        (ctx->point->longitude < -180000000000) ||
        (ctx->point->longitude > 180000000000))
      ctx->gpx->bad_long++;
    
    if ((ctx->got_lat && ctx->got_long && (ctx->got_ele || true) && ctx->got_time) &&
        (ctx->point != NULL) && (ctx->point->longitude >= -180000000000) &&
        (ctx->point->longitude <= 180000000000) &&
        (ctx->point->latitude >= -90000000000) &&
        (ctx->point->latitude <= 90000000000)) {
      gpx_update_minmax(ctx->gpx, ctx->point);
      
      if (ctx->lastpoint == NULL) {
        INFO("Attaching first point");
        ctx->gpx->firstlatitude = ctx->point->latitude;
        ctx->gpx->firstlongitude = ctx->point->longitude;
        ctx->lastpoint = ctx->point;
        ctx->gpx->points = ctx->point;
        ctx->point = NULL;
      } else {
        ctx->lastpoint->next = ctx->point;
        ctx->lastpoint = ctx->point;
        ctx->point = NULL;
      }
      
      ctx->gpx->goodpoints++;
      
    } else {
      ctx->gpx->badpoints++;
      if (ctx->point->timestamp != NULL)
        free(ctx->point->timestamp);
      free(ctx->point);
      ctx->point = NULL;
    }
    ctx->state = UNKNOWN;
  } else if (strcmp(name, "ele") == 0) {
    REQUIRE_STATE(ELEVATION);
    ctx->point->elevation = strtof(ctx->accumulator, NULL);
    ctx->state = TRACKPOINT;
    ctx->got_ele = true;
  } else if (strcmp(name, "time") == 0) {
    char *pnull = NULL;
    struct tm ignored;
    if (ctx->state == UNKNOWN)
      return;
    REQUIRE_STATE(TIMESTAMP);
    pnull = strptime(ctx->accumulator ? ctx->accumulator : "",
                     "%Y-%m-%dT%H:%M:%S",
                     &ignored);
    if (pnull != NULL && (*pnull == '\0' || *pnull == '.' || *pnull == 'Z')) {
      ctx->point->timestamp = ctx->accumulator;
      ctx->accumulator = NULL;
      ctx->got_time = true;
    }
    ctx->state = TRACKPOINT;
  } else if (strcmp(name, "trkseg") == 0) {
    REQUIRE_STATE(UNKNOWN);
    ctx->curseg++;
  }
}

void
gpx_handle_string_data(void *_ctx, const XML_Char *str, int len)
{
  GPXParseContext *ctx = (GPXParseContext *)(_ctx);
  
  if ((ctx->state != ELEVATION) &&
      (ctx->state != TIMESTAMP))
    /* We only accumulate during elevation or timestamp */
    return;
  
  if (ctx->accumulator == NULL) {
    ctx->accumulator = malloc(len + 1);
    memcpy(ctx->accumulator, str, len);
    ctx->accumulator_size = len + 1;
  } else {
    ctx->accumulator = realloc(ctx->accumulator, ctx->accumulator_size + len);
    memcpy(ctx->accumulator + ctx->accumulator_size - 1, str, len);
    ctx->accumulator_size += len;
  }
  
  ctx->accumulator[ctx->accumulator_size - 1] = '\0';
}

static bool
gpx_create_parser(GPXParseContext *ctx)
{
  ctx->p = XML_ParserCreate(NULL);
  
  if (ctx->p == NULL)
    return false;
  
  XML_SetElementHandler(ctx->p, gpx_handle_start_element, gpx_handle_end_element);
  XML_SetDefaultHandler(ctx->p, gpx_handle_string_data);
  XML_SetUserData(ctx->p, ctx);
  
  return true;
}

static bool
gpx_parse_buffer(GPXParseContext *ctx, const char *buffer, ssize_t buflen)
{
  if (XML_Parse(ctx->p, buffer, buflen, XML_FALSE) != XML_STATUS_OK) {
    if (*(ctx->err) == NULL) {
      PARSE_ERROR(ctx, "Generic XML parse error");
    }
    return false;
  }
  return true;
}

static void
gpx_free_parser(GPXParseContext *ctx)
{
  if (ctx->p != NULL)
    XML_ParserFree(ctx->p);
  ctx->p = NULL;
}

static void
gpx_abort_context(GPXParseContext *ctx)
{
  gpx_free(ctx->gpx);
  if (ctx->point != NULL) {
    if (ctx->point->timestamp)
      free(ctx->point->timestamp);
    free(ctx->point);
  }
  if (ctx->accumulator != NULL)
    free(ctx->accumulator);
  gpx_free_parser(ctx);
}

static bool
gpx_parse_plain_file(GPXParseContext *ctx, const char *gpxfile)
{
  char *buffer = alloca(GPX_BUFLEN);
  ssize_t bufread;
  int fd = open(gpxfile, O_RDONLY);
  
  if (fd == -1) {
    PARSE_ERROR(ctx, "Error opening file %s: %s", gpxfile, strerror(errno));
    return false;
  }
  
  if (gpx_create_parser(ctx) == false) {
    PARSE_ERROR(ctx, "Unable to create XML parser");
    close(fd);
    return false;
  }
  
  while ((bufread = read(fd, buffer, GPX_BUFLEN)) > 0) {
    if (gpx_parse_buffer(ctx, buffer, bufread) == false) {
      close(fd);
      return false;
    }
  }
  
  close(fd);
  
  gpx_free_parser(ctx);
  
  return true;
}

static bool
gpx_parse_archive(GPXParseContext *ctx, const char *gpxfile)
{
  struct archive *arch;
  struct archive_entry *ent;
  bool okay = false;
  int64_t sublen;
  char *buffer = alloca(GPX_BUFLEN);
  int ret;
  
  arch = archive_read_new();
  if (arch == NULL) {
    return false;
  }
  
  archive_read_support_compression_gzip(arch);
  archive_read_support_compression_bzip2(arch);
  archive_read_support_format_all(arch);
  
  if (archive_read_open_filename(arch, gpxfile, 1) < ARCHIVE_OK) {
    goto out;
  }
  
  while ((ret = archive_read_next_header(arch, &ent)) == ARCHIVE_OK) {
    ctx->subfile = archive_entry_pathname(ent);
    sublen = archive_entry_size(ent);
    if (sublen > 0) {
      INFO("Considering sub-entry %s in job", ctx->subfile);
      /* There's data, let's try and parse it */
      gpx_create_parser(ctx);
      while (sublen > 0) {
        if (archive_read_data(arch, buffer, MIN(GPX_BUFLEN, sublen)) < ARCHIVE_OK) {
          PARSE_ERROR(ctx, "Unable to read data from archive.");
          goto out;
        }
        if (gpx_parse_buffer(ctx, buffer, MIN(GPX_BUFLEN, sublen)) == false) {
          goto out;
        }
        sublen -= MIN(GPX_BUFLEN, sublen);
      }
      gpx_free_parser(ctx);
    }
    ctx->subfile = NULL;
  }
  
  
  if ((ret == ARCHIVE_EOF) && ctx->gpx->goodpoints > 0)
    okay = true;
  
  out:
  archive_read_close(arch);
  archive_read_finish(arch);
  return okay;
}

static char gzip_magic[] = { 0x1f, 0x8b, 0x08 };
static char bzip2_magic[] = { 0x42, 0x5a, 0x68, 0x39, 0x31 };

static bool
gpx_try_gzip(GPXParseContext *ctx, const char *gpxfile)
{
  int fd = open(gpxfile, O_RDONLY);
  gzFile *f;
  char *buffer = alloca(GPX_BUFLEN);
  ssize_t bufread;
  
  if (fd == -1) {
    PARSE_ERROR(ctx, "Unable to open %s (errno=%s)", gpxfile, strerror(errno));
    return false;
  }
  
  if (read(fd, buffer, sizeof(gzip_magic)) != sizeof(gzip_magic)) {
    PARSE_ERROR(ctx, "Unable to read data from %s (errno=%s)", gpxfile, strerror(errno));
    close(fd);
    return false;
  }
  
  if (memcmp(buffer, gzip_magic, sizeof(gzip_magic)) != 0) {
    close(fd);
    return false;
  }
  
  lseek(fd, 0, SEEK_SET);
  
  f = gzdopen(fd, "rb"); /* Takes over the FD */
  
  INFO("Detected gzip encoded data");
  
  if (gpx_create_parser(ctx) == false) {
    PARSE_ERROR(ctx, "Unable to create XML parser");
    gzclose(f);
    return false;
  }
  
  while ((bufread = gzread(f, buffer, GPX_BUFLEN)) > 0) {
    if (gpx_parse_buffer(ctx, buffer, bufread) == false) {
      gzclose(f);
      return false;
    }
  }
  
  gzclose(f);
  
  gpx_free_parser(ctx);
  
  return true;
}

static bool
gpx_try_bzip2(GPXParseContext *ctx, const char *gpxfile)
{
  int fd = open(gpxfile, O_RDONLY);
  BZFILE *f;
  char *buffer = alloca(GPX_BUFLEN);
  ssize_t bufread;
  
  if (fd == -1) {
    PARSE_ERROR(ctx, "Unable to open %s (errno=%s)", gpxfile, strerror(errno));
    return false;
  }
  
  if (read(fd, buffer, sizeof(bzip2_magic)) != sizeof(bzip2_magic)) {
    PARSE_ERROR(ctx, "Unable to read data from %s (errno=%s)", gpxfile, strerror(errno));
    close(fd);
    return false;
  }
  
  if (memcmp(buffer, bzip2_magic, sizeof(bzip2_magic)) != 0) {
    close(fd);
    return false;
  }
  
  close(fd);
  
  f = BZ2_bzopen(gpxfile, "rb");
  
  INFO("Detected bzip2 encoded data");
  
  if (gpx_create_parser(ctx) == false) {
    PARSE_ERROR(ctx, "Unable to create XML parser");
    BZ2_bzclose(f);
    return false;
  }
  
  while ((bufread = BZ2_bzread(f, buffer, GPX_BUFLEN)) > 0) {
    if (gpx_parse_buffer(ctx, buffer, bufread) == false) {
      BZ2_bzclose(f);
      return false;
    }
  }
  
  BZ2_bzclose(f);
  
  gpx_free_parser(ctx);
  
  return true;
}

GPX*
gpx_parse_file(const char *gpxfile, char **err)
{
  GPXParseContext ctx;
  
  ctx.err = err;
  
  ctx.gpx = calloc(1, sizeof(GPX));
  ctx.gpx->minlatitude = 91000000000;
  ctx.gpx->minlongitude = 181000000000;
  ctx.gpx->maxlatitude = -91000000000;
  ctx.gpx->maxlongitude = -181000000000;
  ctx.point = NULL;
  ctx.lastpoint = NULL;
  ctx.curseg = 0;
  ctx.accumulator = NULL;
  ctx.state = UNKNOWN;
  ctx.accumulator_size = 0;
  ctx.subfile = NULL;
  
  if (gpx_parse_archive(&ctx, gpxfile) == true) {
    goto success;
  } else {
    if (*(ctx.err) != NULL) {
      ERROR("Archive failure");
      goto failure;
    }
  }
  
  if (gpx_try_gzip(&ctx, gpxfile) == true) {
    goto success;
  } else {
    if (*(ctx.err) != NULL) {
      ERROR("GZip failure");
      goto failure;
    }
  }
  
  if (gpx_try_bzip2(&ctx, gpxfile) == true) {
    goto success;
  } else {
    if (*(ctx.err) != NULL) {
      ERROR("BZip2 failure");
      goto failure;
    }
  }
  
  if (gpx_parse_plain_file(&ctx, gpxfile) == false) {
    goto failure;
  }
  
  if (ctx.gpx->goodpoints == 0) {
    ERROR("Zero good points, %d bad (%d missed time, %d bad lat, %d bad long)",
          ctx.gpx->badpoints,
          ctx.gpx->missed_time,
          ctx.gpx->bad_lat,
          ctx.gpx->bad_long);
    if (ctx.gpx->missed_time > ((ctx.gpx->badpoints * 3) >> 2)) {
      *(ctx.err) = strdup("Found no good GPX points in the input data. At least 75% of the trackpoints lacked a <time> tag.");
    } else {
      *(ctx.err) = strdup("Found no good GPX points in the input data");
    }
    goto failure;
  }
  
  success:
  if (ctx.point)
    free(ctx.point);
  if (ctx.accumulator != NULL)
    free(ctx.accumulator);
  
  return ctx.gpx;
  
  failure:
  gpx_abort_context(&ctx);
  return NULL;
}

void
gpx_print(GPX *gpx)
{
  GPXTrackPoint *pt;
  int pointnr;
  
  printf("minlat=%ld maxlat=%ld minlon=%ld maxlon=%ld\n",
         gpx->minlatitude, gpx->maxlatitude,
         gpx->minlongitude, gpx->maxlongitude);
  
  printf("goodpoints=%d badpoints=%d\n",
         gpx->goodpoints,
         gpx->badpoints);
  return;
  for (pt = gpx->points, pointnr = 1; pt != NULL; pt = pt->next, pointnr++)
    printf("%4d: lat=%ld lon=%ld ele=%f time=%s seg=%d\n",
           pointnr, pt->latitude, pt->longitude, pt->elevation,
           pt->timestamp, pt->segment);
  
}
