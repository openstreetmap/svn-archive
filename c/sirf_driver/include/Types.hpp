#ifndef TYPES_H
#define TYPES_H

namespace SiRF {

  /* setup default types which are the correct sizes
   * this setup is valid on my ibook G3 with gcc
   * but YMMV from compiler to compiler and cpu to cpu...
   */
  typedef unsigned char uint8;
  typedef unsigned short uint16;
  typedef unsigned int uint32;
  typedef unsigned long long uint64;
  typedef char int8;
  typedef short int16;
  typedef int int32;
  typedef long long int64;
  typedef float float32;
  
}

#endif /* TYPES_H */
