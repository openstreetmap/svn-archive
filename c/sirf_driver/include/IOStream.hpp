#ifndef IO_STREAM_H
#define IO_STREAM_H

#include "InputStream.hpp"
#include "OutputStream.hpp"

namespace SiRF {
  
  class IOStream : public InputStream, public OutputStream {

  public:
    // hopefully this doesnt cause everything to happen twice...
    IOStream(char *devicename, unsigned int baud = 4800) : 
      Stream(devicename, baud),
      InputStream(devicename, baud),
      OutputStream(devicename, baud) {
    }
    
  };

}

#endif /* IO_STREAM_H */
