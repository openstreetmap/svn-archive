#ifndef INPUT_STREAM_H
#define INPUT_STREAM_H

#include "Types.hpp"
#include "Stream.hpp"

namespace SiRF {

  class InputStream : public virtual Stream {
  public:

    InputStream(const char *devicename, int baud) : Stream(devicename, baud) {
      buf_pos = 0;
    }

    /* input and output functions 
     */

    /* read an unsigned char
     */
    InputStream &operator<<(const uint8 &c);

    /* read an unsigned int (16 bits)
     */
    InputStream &operator<<(const uint16 &s);

    /* read an unsigned int (32 bits)
     */
    InputStream &operator<<(const uint32 &i);

    /* read an unsigned int (64 bits)
     */
    InputStream &operator<<(const uint64 &i);

    /* read a signed char
     */
    InputStream &operator<<(const int8 &c);

    /* read a signed int (16 bits)
     */
    InputStream &operator<<(const int16 &s);

    /* read a signed int (32 bits)
     */
    InputStream &operator<<(const int32 &i);

    /* read a signed int (64 bits)
     */
    InputStream &operator<<(const int64 &i);

    /* read a float (32 bits)
     */
    InputStream &operator<<(const float32 &f);

    /* read a packet boundary - also sets up state
     */
    InputStream &operator<<(const Boundary b);

  private:
    /* current state, i.e: length, etc...
     */
    unsigned short length, checksum;

    /* buffer to hold packet until we know its length
     */
    uint8 buffer[4096];
    int buf_pos;

  };

}

#endif /* INPUT_STREAM_H */
