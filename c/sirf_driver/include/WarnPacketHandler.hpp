#ifndef WARN_PACKET_HANDLER_H
#define WARN_PACKET_HANDLER_H

#include "PacketHandler.hpp"
#include "UnknownPacket.hpp"

namespace SiRF {

  class WarnPacketHandler : public PacketHandler<UnknownPacket> {
  public:

    /* interface to handle a packet
     */
    void handle(UnknownPacket);

  };

}

#endif /* WARN_PACKET_HANDLER_H */
