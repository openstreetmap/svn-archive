#ifndef SIRF_DEVICE_H
#define SIRF_DEVICE_H

#include "Types.hpp"

namespace SiRF {

  class SiRFDevice {
  public:
    // construct a SiRF device from the serial port, i.e: /dev/ttyUSB0
    // or something, and the baud speed (literally, as in 9600, not
    // B9600). 
    SiRFDevice(const char *devicename, unsigned int baud);

    // destory the SiRF device
    ~SiRFDevice();

    // get a byte from the SiRF device
    SiRFDevice &operator>>(uint8 &c);

    // get a byte from the SiRF device
    SiRFDevice &operator<<(uint8 &c);

    // flush data to the device
    void flush();

  private:
    // file descriptor
    int fd;
    /* you may wonder why i'm using a file descriptor, rather than a
       proper c++ stream. the answer: you need it to do very low-level
       serial stuff which you can't do on, e.g: an istream.

       yes, this is very annoying. i can't think of a good way around
       it, but i'd like to hear if there is one.

       oh, this does mean its not portable, but the non-portability
       should be restricted to this class only.
     */

    // buffer of incoming data
    uint8 *read_buffer, *write_buffer;

    // current buffer size and position
    // read
    unsigned int read_buf_size;
    int read_buf_len, read_buf_pos;
    // write
    unsigned int write_buf_size;
    int write_buf_pos;

    // default constructor is private, so you can't copy this object
    SiRFDevice(const SiRFDevice &other) {}

    // ditto for the assignment thing
    const SiRFDevice &operator=(const SiRFDevice &other) {}

    // open the serial port
    void open(const char *name);

    // close the serial port
    void close();

    // get some more data and put it in the buffer
    void refillReadBuffer();

    // flush the write buffer (thats _our_ write buffer, not the OS'ses)
    void SiRFDevice::flushWriteBuffer();
    
  };

}

#endif /* SIRF_DEVICE_H */
