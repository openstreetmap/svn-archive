/* gpx-import/src/interpolate.c
 *
 * GPX file importer, email interpolation
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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <limits.h>

#include <errno.h>

#include "interpolate.h"

static void
do_interpolate(DBJob *job, FILE *input, FILE *output)
{
  int c;
  
  while ((c = fgetc(input)) != EOF) {
    if (c != '%') {
      fputc(c, output);
      continue;
    }
    c = fgetc(input);
    switch (c) {
    case -1:
    case '%':
      fputc('%', output);
      break;
    case 'e':
      fputs(job->email, output);
      break;
    case 'E':
      fputs(job->error, output);
      break;
    case 't':
      fputs(job->title, output);
      break;
    case 'd':
      fputs(job->description, output);
      break;
    case 'g':
      fprintf(output, "%d", job->gpx->goodpoints);
      break;
    case 'p':
      fprintf(output, "%d", job->gpx->goodpoints + job->gpx->badpoints);
      break;
    case 'T':
      if (strlen(job->tags) > 0) {
        fputs("and the following tags:\n\n  ", output);
        fputs(job->tags, output);
      } else {
        fputs("and no tags.", output);
      }
      break;
    case 'm':
      if (job->gpx->missed_time > 0) {
        fprintf(output, "Of the failed points, %d lacked <time>", job->gpx->missed_time);
      }
      break;
    case 'l':
      if (job->gpx->bad_lat > 0) {
        fprintf(output, "Of the failed points, %d had bad latitude", job->gpx->bad_lat);
      }
      break;
    case 'L':
      if (job->gpx->bad_long > 0) {
        fprintf(output, "Of the failed points, %d had bad longitude", job->gpx->bad_long);
      }
      break;
    default:
      fputs("\n\n[Unknown % escape: ", output);
      fputc(c, output);
      fputs("]\n\n", output);
    }
  }
}

void
interpolate(DBJob *job, const char *template)
{
  FILE *outputfile, *inputfile;
  char inputpath[PATH_MAX];
  
  if (getenv("GPX_INTERPOLATE_STDOUT") != NULL) {
    outputfile = stdout;
  } else {
    outputfile = popen("/usr/lib/sendmail -t -r '<>'", "w");
    if (outputfile == NULL) {
      ERROR("Unable to open sendmail! (errno=%s)", strerror(errno));
      return;
    }
  }
  
  snprintf(inputpath, PATH_MAX, "%s/%s", getenv("GPX_PATH_TEMPLATES"), template);
  
  inputfile = fopen(inputpath, "rb");
  
  if (inputfile == NULL) {
    ERROR("Unable to open input file %s (errno=%s)", inputpath, strerror(errno));
  } else {
    do_interpolate(job, inputfile, outputfile);
    fclose(inputfile);
  }
  
  if (outputfile != stdout) {
    if (pclose(outputfile) == -1) {
      ERROR("Failure while closing sendmail! (errno=%s)", strerror(errno));
    }
  }
}
