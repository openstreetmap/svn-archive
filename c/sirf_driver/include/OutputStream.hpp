#ifndef OUTPUT_STREAM_H
#define OUTPUT_STREAM_H

#include "Types.hpp"
#include "Stream.hpp"
//#include <istream>

namespace SiRF {

  class OutputStream : public virtual Stream {
  public:

    OutputStream(const char *devicename, unsigned int baud) : 
      Stream(devicename, baud) {
    }

    /* input and output functions 
     */
    int remainingLength() const {
      return length;
    }

    /* read an unsigned char
     */
    OutputStream &operator>>(uint8 &c);

    /* read an unsigned int (16 bits)
     */
    OutputStream &operator>>(uint16 &s);

    /* read an unsigned int (32 bits)
     */
    OutputStream &operator>>(uint32 &i);

    /* read an unsigned int (64 bits)
     */
    OutputStream &operator>>(uint64 &i);

    /* read a signed char
     */
    OutputStream &operator>>(int8 &c);

    /* read a signed int (16 bits)
     */
    OutputStream &operator>>(int16 &s);

    /* read a signed int (32 bits)
     */
    OutputStream &operator>>(int32 &i);

    /* read a signed int (64 bits)
     */
    OutputStream &operator>>(int64 &i);

    /* read a float (32 bits)
     */
    OutputStream &operator>>(float32 &f);

    /* read a float (64 bits)
     */
    OutputStream &operator>>(float64 &f);

    /* read a packet boundary - also sets up state
     */
    OutputStream &operator>>(const Boundary b);

  private:
    /* current state, i.e: length, etc...
     */
    uint16 length, checksum;

  };

}

#endif /* OUTPUT_STREAM_H */
