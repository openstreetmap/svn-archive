#ifndef GPX_STREAM_H
#define GPX_STREAM_H

#include "FileInfo.hpp"

namespace SiRF {

  using namespace XML;

  class GPXStream {
  public:
    GPXStream(const char *, const GPXAuthorInfo &);
    ~GPXStream();
  private:
    xmlDocPtr doc;
    FileInfo header;
  };

}

#endif /* GPX_STREAM_H */
