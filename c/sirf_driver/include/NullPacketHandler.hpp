#ifndef NULL_PACKET_HANDLER_H
#define NULL_PACKET_HANDLER_H

#include "PacketHandler.hpp"

namespace SiRF {

  /* a templated class to handle all types of packet by doing nothing
   * with them. essentially a silent PrintPacketHandler
   */
  template <class T>
  class NullPacketHandler : public PacketHandler<T> {
  public:

    /* handle a packet by doing nothing with it
     */
    void handle(T packet) {
    }

  };

}

#endif /* NULL_PACKET_HANDLER_H */
