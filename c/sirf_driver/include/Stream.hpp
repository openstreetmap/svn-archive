#ifndef STREAM_H
#define STREAM_H

#include "SiRFDevice.hpp"

namespace SiRF {

  class Stream : public SiRFDevice {
  public:
    /* special hack to align to packet boundaries
     */
    enum Boundary {
      start,
      end
    };
    /* use a standard io stream
     */
    Stream(const char *devicename, unsigned int baud) : 
      SiRFDevice(devicename, baud) {}

    /* has to close the stream when done
     */
    //    ~Stream();

  private:

  };

}

#endif /* STREAM_H */
