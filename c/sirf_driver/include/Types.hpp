#ifndef TYPES_H
#define TYPES_H

#include "config.h"

namespace SiRF {

  /* setup default types which are the correct sizes
   * this setup is valid on my ibook G3 with gcc
   * but YMMV from compiler to compiler and cpu to cpu...
   */
#if SIZEOF_UNSIGNED_CHAR == 1
  typedef unsigned char uint8;
#else 
#error unsigned byte type not available
#endif

#if SIZEOF_UNSIGNED_SHORT == 2
  typedef unsigned short uint16;
#else
#error unsigned 16-bit type not available
#endif

#if SIZEOF_UNSIGNED_INT == 4
  typedef unsigned int uint32;
#else
#if SIZEOF_UNSIGNED_LONG_INT == 4
  typedef unsigned long int uint32;
#else
#error unsigned 32-bit type not available
#endif
#endif

#if SIZEOF_UNSIGNED_LONG_INT == 8
  typedef unsigned long int uint64;
#else
#if SIZEOF_UNSIGNED_LONG_LONG_INT == 8
  typedef unsigned long long int uint64;
#else
#error unsigned 64-bit type not available
#endif
#endif

#if SIZEOF_CHAR == 1
  typedef char int8;
#else
#error signed byte type not available
#endif


#if SIZEOF_SHORT == 2
  typedef short int16;
#else
#error signed 16-bit type not available
#endif

#if SIZEOF_INT == 4
  typedef int int32;
#else
#if SIZEOF_LONG_INT == 4
  typedef long int int32;
#else
#error signed 32-bit type not available
#endif
#endif

#if SIZEOF_LONG_INT == 8
  typedef long int int64;
#else
#if SIZEOF_LONG_LONG_INT == 8
  typedef long long int int64;
#else
#error signed 64-bit type not available
#endif
#endif

#if SIZEOF_FLOAT == 4
  typedef float float32;
#else
#if SIZEOF_DOUBLE == 4
  typedef double float32;
#else
#error 32-bit floating point type not available
#endif
#endif

#if SIZEOF_DOUBLE == 8
  typedef double float64;
#else
#if SIZEOF_LONG_DOUBLE == 8
  typedef long double float64;
#else
#error 64-bit floating point type not available
#endif
#endif

}

#endif /* TYPES_H */
