#ifndef STREAM_H
#define STREAM_H

#include "Types.hpp"
#include "SiRFDevice.hpp"
#include <istream>

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
    Stream(const char *devicename, int baud = 4800);

    /* has to close the stream when done
     */
    ~Stream();

    /* how many bytes remaining in the current packet?
     */
    inline unsigned int remainingLength() {
      return length;
    }

    /* input and output functions 
     */

    /* read an unsigned char
     */
    Stream &operator>>(uint8 &c);

    /* read an unsigned int (16 bits)
     */
    Stream &operator>>(uint16 &s);

    /* read an unsigned int (32 bits)
     */
    Stream &operator>>(uint32 &i);

    /* read an unsigned int (64 bits)
     */
    Stream &operator>>(uint64 &i);

    /* read a signed char
     */
    Stream &operator>>(int8 &c);

    /* read a signed int (16 bits)
     */
    Stream &operator>>(int16 &s);

    /* read a signed int (32 bits)
     */
    Stream &operator>>(int32 &i);

    /* read a signed int (64 bits)
     */
    Stream &operator>>(int64 &i);

    /* read a float (32 bits)
     */
    Stream &operator>>(float32 &f);

    /* read a packet boundary - also sets up state
     */
    Stream &operator>>(const Boundary b);

  private:

    /* current state, i.e: length, etc...
     */
    unsigned short length, checksum, original_length;

  };

}

#endif /* STREAM_H */
