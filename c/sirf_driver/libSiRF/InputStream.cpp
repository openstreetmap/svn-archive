#include "InputStream.hpp"
#include "Exception.hpp"

namespace SiRF {

  /* input and output functions 
   */
  
  /* write an unsigned char
   */
  InputStream &InputStream::operator<<(const uint8 &c) {
    /* write unsigned char... passing it back to the device
     */
    //SiRFDevice::operator<<(c);
    buffer[buf_pos++] = c;

    /* update the checksum
     */
    checksum = (checksum + c) & 0x7fff;

    /* update the length
     */
    length++;

    return (*this);
  }
  
  /* read an unsigned int (16 bits)
   */
  InputStream &InputStream::operator<<(const uint16 &s) {
    /* slightly more difficult - we have to pay attention to byte
     * ordering. for the SiRF this is msb first
     */
    uint8 msb, lsb;
    msb = (uint8)(s >> 8);
    lsb = (uint8)(s & 0xff);
    (*this) << msb << lsb;

    return (*this);
  }
  
  /* read an unsigned int (32 bits)
   */
  InputStream &InputStream::operator<<(const uint32 &i) {
    /* just an extension of the unsigned short 
     */
    uint16 msb, lsb;
    msb = (uint16)(i >> 16);
    lsb = (uint16)(i & 0xffff);
    (*this) << msb << lsb;

    return (*this);
  }

  /* read an unsigned 64 bit (8-byte) value
   */
  InputStream &InputStream::operator<<(const uint64 &i) {
    /* just an extension of the unsigned short 
     */
    uint32 msb, lsb;
    msb = (uint32)(i >> 32);
    lsb = (uint32)(i & 0xffffffff);
    (*this) << msb << lsb;

    return (*this);
  }
  
  /* read in a signed byte value
   */
  InputStream &InputStream::operator<<(const int8 &i) {
    /* this has the same bit-structure as an unsigned value,
     * so just manipulate the types to get an answer
     */
    (*this) << static_cast<uint8>(i);

    return (*this);
  }

  /* read a signed int (16 bits)
   */
  InputStream &InputStream::operator<<(const int16 &s) {
    /* we read in the first (msb) byte signed and the second
     * unsigned
     */
    int8 msb;
    uint8 lsb;
    msb = (int8)(s >> 8);
    lsb = (uint8)(s & 0xff);
    (*this) << msb << lsb;

    return (*this);
  }
  
  /* read a signed int (32 bits)
   */
  InputStream &InputStream::operator<<(const int32 &i) {
    /* just an extension of the signed short 
     */
    int16 msb;
    uint16 lsb;
    msb = (int16)(i >> 16);
    lsb = (uint16)(i & 0xffff);
    (*this) << msb << lsb;

    return (*this);
  }

  /* read a signed 64 bit (8-byte) value
   */
  InputStream &InputStream::operator<<(const int64 &i) {
    /* just an extension of the signed int32
     */
    int32 msb;
    uint32 lsb;
    msb = (int32)(i >> 32);
    lsb = (uint32)(i & 0xffffffff);
    (*this) << msb << lsb;

    return (*this);
  }
  
  /* read a float
   */
  InputStream &InputStream::operator<<(const float32 &f) {
    /* this is more difficult... i don't know if the SiRF buggers
     * about with the IEEE endian-ness... have to look in the docs
     * somewhere.
     */
    uint32 i = 0;
    (*this) << i;

    return (*this);
  }
  
  /* read a packet boundary - also sets up state
   */
  InputStream &InputStream::operator<<(const Boundary b) {
    const unsigned short start_marker = 0xa0a2, end_marker = 0xb0b3;

    /* looking for a starting marker
     */
    if (b == start) {
      buf_pos = 0;
      (*this) << start_marker;
      (*this) << (uint16)0; // just dummies for now
      length = 0; // reset length here
      checksum = 0;

      /* otherwise we are looking for an end marker
       */
    } else if (b == end) {
      unsigned short msgsum = checksum;
      /* go back and re-write the length
       */
      buffer[2] = (length >> 8);
      buffer[3] = (length & 0xff);

      (*this) << msgsum;
      (*this) << end_marker;

      /* write back to the underlying device 
       */
      for (int i = 0; i < buf_pos; i++) {
	SiRFDevice::operator<<(buffer[i]);
      }
      buf_pos = 0;
      
      // make sure data gets written immediately.
      SiRFDevice::flush();
    }

    return (*this);
  }
  
}
