/* gpx-import/src/image.c
 *
 * GPX Importer, thumbnail/icon and animation generator
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

#include <gd.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>

#include "image.h"
#include "mercator.h"

typedef struct {
  uint32_t x, y;
} XYPair;

static XYPair *
image_generate_coords(GPX *gpx, MercatorProjection *proj)
{
  XYPair *ret = calloc(gpx->goodpoints, sizeof(XYPair));
  GPXTrackPoint *pt;
  uint32_t n;
  
  for(n = 0, pt = gpx->points; n < gpx->goodpoints; ++n, pt = pt->next) {
    mercator_projection_project(proj, pt->latitude, pt->longitude,
                                &(ret[n].x), &(ret[n].y));
  }
  
  return ret;
}

void
image_generate_animation(GPX *gpx,
                         const char *outfilename,
                         uint32_t width,
                         uint32_t height,
                         uint32_t nframes)
{
  gdImagePtr frame[nframes];
  FILE *out;
  int black, white, grey;
  XYPair *coords;
  MercatorProjection *proj;
  uint32_t n, oldx, oldy, curx, cury, pt;
  uint32_t ptsper = gpx->goodpoints / nframes;
  

  proj = mercator_projection_new(gpx->minlatitude,
                                 gpx->minlongitude,
                                 gpx->maxlatitude,
                                 gpx->maxlongitude,
                                 width,
                                 height);
  
  coords = image_generate_coords(gpx, proj);
  
  for (n = 0; n < nframes; ++n) {
    gdImagePtr f;
    f = frame[n] = gdImageCreate(width, height);
    if (n == 0) {
      black = gdImageColorAllocate(f, 0, 0, 0);
      white = gdImageColorAllocate(f, 255, 255, 255);
      grey = gdImageColorAllocate(f, 0xBB, 0xBB, 0xBB);
    } else {
      gdImagePaletteCopy(frame[n], frame[0]);
    }
    gdImageFilledRectangle(f, 0, 0, width, height, white);
  }
  
  oldx = coords[0].x;
  oldy = coords[0].y;
  
  for (pt = 1; pt < gpx->goodpoints; ++pt) {
    curx = coords[pt].x;
    cury = coords[pt].y;
    
    for (n = 0; n < nframes; ++n) {
      if ((pt >= (ptsper * n)) && (pt <= (ptsper * (n+1)))) {
        gdImageSetThickness(frame[n], 3);
        gdImageSetAntiAliased(frame[n], black);
      } else {
        gdImageSetThickness(frame[n], 1);
        gdImageSetAntiAliased(frame[n], grey);
      }
      gdImageLine(frame[n], oldx, oldy, curx, cury, gdAntiAliased);
    }
    
    oldx = curx;
    oldy = cury;
  }
  
  out = fopen(outfilename, "wb");
  if (out != NULL) {
    gdImageGifAnimBegin(frame[0], out, 1, 0);
    for (n = 0; n < nframes; ++n) {
      gdImageGifAnimAdd(frame[n], out, 0, 0, 0, 50, gdDisposalNone, (n > 0) ? frame[n-1] : NULL);
    }
    gdImageGifAnimEnd(out);
    fclose(out);
  } else {
    ERROR("Unable to create %s (errno=%s)", outfilename, strerror(errno));
  }
  
  for (n = 0; n < nframes; ++n) {
    gdImageDestroy(frame[n]);
  }
  
  free(coords);
  mercator_projection_free(proj);
}

void
image_generate_icon(GPX *gpx,
                    const char *outfilename,
                    uint32_t width,
                    uint32_t height)
{
  gdImagePtr icon;
  FILE *out;
  int black, white;
  XYPair *coords;
  MercatorProjection *proj;
  uint32_t n, oldx, oldy, curx, cury;
  
  proj = mercator_projection_new(gpx->minlatitude,
                                 gpx->minlongitude,
                                 gpx->maxlatitude,
                                 gpx->maxlongitude,
                                 width,
                                 height);
  
  icon = gdImageCreate(width, height);
  
  black = gdImageColorAllocate(icon, 0, 0, 0);
  white = gdImageColorAllocate(icon, 255, 255, 255);
  
  gdImageFilledRectangle(icon, 0, 0, width, height, white);
  
  gdImageSetAntiAliased(icon, black);
  
  coords = image_generate_coords(gpx, proj);
  
  oldx = coords[0].x;
  oldy = coords[0].y;
  
  for(n = 1; n < gpx->goodpoints; ++n) {
    curx = coords[n].x;
    cury = coords[n].y;
    gdImageLine(icon, oldx, oldy, curx, cury, gdAntiAliased);
    oldx = curx;
    oldy = cury;
  }
  
  out = fopen(outfilename, "wb");
  
  if (out != NULL) {
    gdImageGif(icon, out);
    fclose(out);
  } else {
    ERROR("Unable to create %s (errno=%s)", outfilename, strerror(errno));
  }
  
  gdImageDestroy(icon);
  free(coords);
  mercator_projection_free(proj);
}
