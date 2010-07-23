/**
 * logbuffer
 *
 * subclass of stream buffer class that
 * collects output and writes it to a log file, with time stamps.
 *
 * used to direct Mapnik console messages to a log file.
 *
 * part of the Mapnik WMS server module for Apache
 */

#include <sys/time.h>
#include <time.h>

#include "logbuffer.h"

logbuffer::logbuffer(FILE* f)
   : std::streambuf(), fptr(f) 
{
}

void logbuffer::dump_buffer()
{
    if (buffer.empty()) return;
    struct timeval tv;
    gettimeofday(&tv,NULL);
    time_t now = (time_t)tv.tv_sec;
    struct tm *mtim = localtime(&now);

    mtim = localtime(&now);
    fprintf(fptr, "%04d%02d%02d %02d:%02d:%02d.%06d %s\n",
       mtim->tm_year+1900, mtim->tm_mon+1,
       mtim->tm_mday, mtim->tm_hour, mtim->tm_min, mtim->tm_sec,
       (int)(tv.tv_usec), buffer.c_str());
    buffer.clear();
}

logbuffer::~logbuffer()
{
   //dump_buffer();
}

int logbuffer::overflow(int c) 
{
   switch (c) {
      case EOF: return EOF;
      case 10: dump_buffer(); return 10;
      default: buffer.append(1, c); return c;
   }
}

int logbuffer::sync() 
{
   return fflush(fptr);
}

