#include "OutputStream.hpp"
#include "Exception.hpp"

namespace SiRF {

  /* input and output functions 
   */
  
  /* read an unsigned char
   */
  OutputStream &OutputStream::operator>>(uint8 &c) {
    /* this is dead easy! we read only unsigned chars from the
     * SiRFDevice anyway
     */
    SiRFDevice::operator>>(c);

    /* update the checksum
     */
    checksum = (checksum + c) & 0x7fff;

    /* update the length
     */
    length--;

    return (*this);
  }
  
  /* read an unsigned int (16 bits)
   */
  OutputStream &OutputStream::operator>>(uint16 &s) {
    /* slightly more difficult - we have to pay attention to byte
     * ordering. for the SiRF this is msb first
     */
    uint8 msb, lsb;
    (*this) >> msb >> lsb;
    s = (msb << 8) | lsb;

    return (*this);
  }
  
  /* read an unsigned int (32 bits)
   */
  OutputStream &OutputStream::operator>>(uint32 &i) {
    /* just an extension of the unsigned short 
     */
    uint16 msb, lsb;
    (*this) >> msb >> lsb;
    i = ((uint32)msb << 16) | (uint32)lsb;

    return (*this);
  }

  /* read an unsigned 64 bit (8-byte) value
   */
  OutputStream &OutputStream::operator>>(uint64 &i) {
    /* just an extension of the unsigned short 
     */
    uint32 msb, lsb;
    (*this) >> msb >> lsb;
    i = ((uint64)msb << 32) | (uint64)lsb;

    return (*this);
  }
  
  /* read in a signed byte value
   */
  OutputStream &OutputStream::operator>>(int8 &i) {
    /* this has the same bit-structure as an unsigned value,
     * so just manipulate the types to get an answer
     */
    uint8 c;

    (*this) >> c;
    i = static_cast<int8>(c);

    return (*this);
  }

  /* read a signed int (16 bits)
   */
  OutputStream &OutputStream::operator>>(int16 &s) {
    /* we read in the first (msb) byte signed and the second
     * unsigned
     */
    int8 msb;
    uint8 lsb;
    (*this) >> msb >> lsb;
    s = (msb << 8) | lsb;

    return (*this);
  }
  
  /* read a signed int (32 bits)
   */
  OutputStream &OutputStream::operator>>(int32 &i) {
    /* just an extension of the signed short 
     */
    int16 msb;
    uint16 lsb;
    (*this) >> msb >> lsb;
    i = ((int32)msb << 16) | (int32)lsb;

    return (*this);
  }

  /* read a signed 64 bit (8-byte) value
   */
  OutputStream &OutputStream::operator>>(int64 &i) {
    /* just an extension of the signed int32
     */
    int32 msb;
    uint32 lsb;
    (*this) >> msb >> lsb;
    i = ((int64)msb << 32) | (int64)lsb;

    return (*this);
  }
  
  /* read a float
   */
  OutputStream &OutputStream::operator>>(float32 &f) {
    /* this is more difficult... i don't know if the SiRF buggers
     * about with the IEEE endian-ness... have to look in the docs
     * somewhere.
     */
    uint32 i;
    (*this) >> i;

    f = *(float32 *)(&i);

    return (*this);
  }
  
  /* read a float (64)
   */
  OutputStream &OutputStream::operator>>(float64 &f) {
    /* this is more difficult... i don't know if the SiRF buggers
     * about with the IEEE endian-ness... have to look in the docs
     * somewhere.
     */
    uint32 i[2];
    (*this) >> i[0] >> i[1];
    
    f = *(float64 *)(i);

    return (*this);
  }
  
  /* read a packet boundary - also sets up state
   */
  OutputStream &OutputStream::operator>>(const Boundary b) {
    unsigned short marker, original_length;
    int i;

    /* looking for a starting marker
     */
    if (b == start) {
      i = 512;
      do {
	(*this) >> marker;
	i--;
	if (i == 0) break;
      } while (marker != 0xA0A2);
      if (i == 0) {
	/* we couldnt find the start-of-packet marker
	 */
	throw MarkerNotFoundException(marker, 0xA0A2);
      }
      (*this) >> original_length; // make sure we're not changing length 
                                  // while we're using it!
      length = original_length;
      if ((length & 0x8000) > 0) {
	/* length > 0x7fff is invalid according to the SiRF spec
	 */
	throw InvalidParameterException(length, "is not less than 0x7fff");
      }
      checksum = 0;
      return (*this);

      /* otherwise we are looking for an end marker
       */
    } else if (b == end) {
      unsigned short msgsum, old_checksum;
      if (length != 0) {
	/* we read too many or too few bytes!
	 */
	throw LengthException(length, original_length);
      }
      old_checksum = checksum; // protect
      (*this) >> msgsum;
      if ((msgsum & 0x8000) > 0) {
	/* msgsum > 0x7fff is invalid according to the SiRF spec
	 */
	throw InvalidParameterException(msgsum, "is not less than 0x7fff");
      }
      if (msgsum != old_checksum) {
	/* checksums do not match!
	 */
	throw ChecksumException();
      }
      (*this) >> marker;
      if (marker != 0xB0B3) {
	/* marker doesnt match...
	 */
	throw MarkerNotFoundException(marker, 0xB0B3);
      }
      return (*this);
    }

    /* shouldnt ever get here...
     */
    return (*this);
  }
  
}
