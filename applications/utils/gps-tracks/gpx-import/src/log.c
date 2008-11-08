/* gpx-import/src/log.h
 *
 * GPX importer, logging primitives
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

#include <stdio.h>
#include <stdarg.h>
#include <time.h>
#include <string.h>
#include <limits.h>
#include <stdbool.h>
#include <stdlib.h>

#include "log.h"

#define LOGBUFLEN 1024

static char logfilename[PATH_MAX];
static bool logfilename_initialised = false;

static FILE *logfile;

void
log_reopen(void)
{
  if (!logfilename_initialised) {
    strncpy(logfilename, getenv("GPX_LOG_FILE") ? getenv("GPX_LOG_FILE") : "-", PATH_MAX);
    INFO("Initialising logfile '%s'", logfilename);
    logfile = stdout;
    logfilename_initialised = true;
  }
  if (strcmp(logfilename, "-") == 0)
    return;
  if (logfile != stdout)
    INFO("Rotating logfile");
  fclose(logfile);
  logfile = fopen(logfilename, "a");
  if (logfile == NULL) {
    fprintf(stderr, "Unable to open logfile %s!\n", logfilename);
    exit(2);
  }
  INFO("Logfile opened");
}

void
log_close(void)
{
  if (strcmp(logfilename, "-") == 0)
    return;
  INFO("Log terminated");
  fclose(logfile);
}

void
_gpxlog(const char *level, const char *fmt, ...)
{
  char buffer[LOGBUFLEN];
  va_list ap;
  time_t ttnow;
  struct tm tmnow;
  
  time(&ttnow);
  gmtime_r(&ttnow, &tmnow);
  
  strftime(buffer, LOGBUFLEN, "[%Y-%m-%dT%H:%M:%SZ] ", &tmnow);
  strcat(buffer, level);
  strcat(buffer, ": ");
  va_start(ap, fmt);
  vsnprintf(buffer + strlen(buffer), LOGBUFLEN - strlen(buffer) - 1, fmt, ap);
  va_end(ap);
  if (!logfilename_initialised) {
    puts(buffer);
  } else {
    fputs(buffer, logfile);
    fputc('\n', logfile);
    fflush(logfile);
  }
}
