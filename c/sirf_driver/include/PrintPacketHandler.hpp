#ifndef PRINT_PACKET_HANDLER_H
#define PRINT_PACKET_HANDLER_H

/* NOTE: we don't include whatever you template on, so make sure
 * its included from the file which declares the template!
 */
#include "PacketHandler.hpp"
#include <iostream>

namespace SiRF {

  /* a templated class to handle all types of packet and print them
   * to stdout.
   */
  template <class T>
  class PrintPacketHandler : public PacketHandler<T> {
  public:

    /* handle a packet by printing it
     */
    void handle(T packet) {
      std::cout << packet << std::endl;
    }

  };

}

#endif /* PRINT_PACKET_HANDLER_H */
